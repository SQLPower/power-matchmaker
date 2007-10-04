/*
 * Copyright (c) 2007, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.graph.BreadthFirstSearch;
import ca.sqlpower.graph.ConnectedComponentFinder;
import ca.sqlpower.graph.GraphModel;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchPool;
import ca.sqlpower.matchmaker.PotentialMatchRecord;
import ca.sqlpower.matchmaker.SourceTableRecord;
import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;
import ca.sqlpower.matchmaker.graph.MatchPoolDotExport;
import ca.sqlpower.matchmaker.graph.MatchPoolGraphModel;
import ca.sqlpower.matchmaker.graph.NonDirectedUserValidatedMatchPoolGraphModel;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.swingui.graphViewer.GraphNodeRenderer;
import ca.sqlpower.matchmaker.swingui.graphViewer.GraphSelectionListener;
import ca.sqlpower.matchmaker.swingui.graphViewer.GraphViewer;
import ca.sqlpower.swingui.SPSUtils;

import com.jgoodies.forms.factories.ButtonBarFactory;

/**
 * The MatchResultVisualizer produces graphical representations of the matches
 * in a Match Result Table.
 */
public class MatchResultVisualizer implements EditorPane {
    
    private static final Logger logger = Logger.getLogger(MatchResultVisualizer.class);

    /**
     * Icons used for the match validation actions.
     */
	final private Icon masterIcon = new ImageIcon(getClass().getResource("/icons/master.png"));
	final private Icon duplicateIcon = new ImageIcon(getClass().getResource("/icons/duplicate.png"));
	final private Icon nomatchIcon = new ImageIcon(getClass().getResource("/icons/nomatch.png"));
	final private Icon unmatchIcon = new ImageIcon(getClass().getResource("/icons/unmatch.png"));
    
    /**
     * Pops up a save dialog and saves the match pool to the chosen DOT file.
     */
    private final Action exportDotFileAction = new AbstractAction("Export as Dot file") {
        public void actionPerformed(ActionEvent e) {
            try {
                File dotFile = new File(
                        System.getProperty("user.home"), "matchmaker_graph_"+match.getName()+".dot");
                JFileChooser fc = new JFileChooser(dotFile);
                int choice = fc.showSaveDialog(panel);
                if (choice == JFileChooser.APPROVE_OPTION) {
                    MatchPoolDotExport exporter = new MatchPoolDotExport(match);
                    exporter.setDotFile(fc.getSelectedFile());
                    exporter.exportDotFile();
                }
            } catch (Exception ex) {
                SPSUtils.showExceptionDialogNoReport(panel, "Couldn't export dot file!", ex);
            }
        }
    };
    
    /**
	 * This action calls the reset method on the pool. For more information
	 * see {@link MatchPool#resetPool()}.
	 */
    private final Action resetPoolAction = new AbstractAction("Reset All") {
    	public void actionPerformed(ActionEvent e) {
			if (JOptionPane.showConfirmDialog(panel,
							"You are about to reset the entire match pool! Do you really wish to do this?",
							"Reset Entire Match Pool",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) return;
			try {
				pool.resetPool();
				pool.store();
    		}catch (SQLException ex) {
            	MMSUtils.showExceptionDialog(panel, "An exception occurred while trying to " +
            			"store changes to the database.", ex);
            }
    		graph.repaint();
    	}
    };
    
    private final Action viewerAutoLayoutAction = new AbstractAction("Auto layout") {
        
        public void actionPerformed(ActionEvent e) {
            doAutoLayout();
        }
        
    };

    /**
     * Warning! Will throw a ClassCastException if the node renderer in graph
     * is not a SourceTableRecordRenderer.
     */
    private final Action chooseDisplayedValueAction = new AbstractAction("Choose Displayed Value") {
    	
    	private JDialog dialog;    	
    	private DisplayedNodeValueChooser chooser;
    	
    	private final AbstractAction okAction = new AbstractAction("OK") {
    		public void actionPerformed(ActionEvent e) {
    			try {
    				chooser.getRenderer().setDisplayColumns(chooser.getChosenColumns());
    				MatchResultVisualizer.this.getPool().findAll(chooser.getChosenColumns());
    				MatchResultVisualizer.this.getPanel().repaint();
    				MatchResultVisualizer.this.doAutoLayout();
    			} catch (ArchitectException ex) {
    				MMSUtils.showExceptionDialog((Component) e.getSource(), ex.getMessage(), ex);
    			} catch (SQLException sqlEx) {
    				MMSUtils.showExceptionDialog((Component) e.getSource(), sqlEx.getMessage(), sqlEx);
    			} finally {
    				dialog.dispose();
    			}
    		}
    	};
    	
    	private final AbstractAction cancelAction = new AbstractAction("Cancel") {
    		public void actionPerformed(ActionEvent e) {
    			dialog.dispose();
    		}
    	};
    	
    	public void actionPerformed(ActionEvent e) {
    		try {
    			dialog = SPSUtils.makeOwnedDialog(panel, "Select Graph Display Values");
				chooser = new DisplayedNodeValueChooser((SourceTableNodeRenderer)graph.getNodeRenderer(), match);
				JPanel tablePanel = chooser.makeGUI();
				JPanel buttonPanel = ButtonBarFactory.buildOKCancelBar(new JButton(okAction), new JButton(cancelAction));
				JPanel panel = new JPanel(new BorderLayout());
				panel.add(tablePanel, BorderLayout.CENTER);
				panel.add(buttonPanel, BorderLayout.SOUTH);
				dialog.getContentPane().add(panel);
    			dialog.pack();
    			dialog.setLocationRelativeTo((Component) e.getSource());
    			dialog.setVisible(true);
    		} catch (ArchitectException ex) {
    			MMSUtils.showExceptionDialog((Component) e.getSource(), ex.getMessage(), ex);
    		}
    	}
    };
    
    /**
	 * When this action is fired the nodes will become unrelated.
	 */
    class SetNoMatchAction extends AbstractAction{
    	private final SourceTableRecord record1;
        private final SourceTableRecord record2;
        
        protected SetNoMatchAction (String name, SourceTableRecord record1, SourceTableRecord record2){
            super("", nomatchIcon);
            super.putValue(AbstractAction.SHORT_DESCRIPTION, name);
            this.record1 = record1;
            this.record2 = record2;
        }
        
        public void actionPerformed(ActionEvent e){
            try {
				pool.defineNoMatch(record1, record2);
				pool.store();
            } catch (ArchitectException ex) {
            	MMSUtils.showExceptionDialog(panel, "An exception occurred while trying to " +
            			"define " + record1 + " and " + record2 + " to not be duplicates.", ex);
            } catch (SQLException ex) {
            	MMSUtils.showExceptionDialog(panel, "An exception occurred while trying to " +
            			"store changes to the database.", ex);
            }
            selectionListener.nodeSelected(record1);
            graph.repaint();
        }
    }
    
    /**
	 * When this action is fired the nodes will have the edge directly relating
	 * them set to undecided if the edge exists.
	 */
    class SetUnmatchAction extends AbstractAction{
    	private final SourceTableRecord record1;
        private final SourceTableRecord record2;
        
        protected SetUnmatchAction (String name, SourceTableRecord record1, SourceTableRecord record2){
            super("", unmatchIcon);
            super.putValue(AbstractAction.SHORT_DESCRIPTION, name);
            this.record1 = record1;
            this.record2 = record2;
        }
        
        public void actionPerformed(ActionEvent e){
            try {
				pool.defineUnmatched(record1, record2);
				pool.store();
			} catch (ArchitectException ex) {
				MMSUtils.showExceptionDialog(panel, "An exception occurred when trying to " +
						"unmatch " + record1 + " and " + record2, ex);
			} catch (SQLException ex) {
            	MMSUtils.showExceptionDialog(panel, "An exception occurred while trying to " +
            			"store changes to the database.", ex);
            }
            selectionListener.nodeSelected(record1);
            graph.repaint();
        }
    }
    
    /**
	 * When this action is fired the master given to the constructor will become
	 * the master of the given duplicate
	 */
    class SetMasterAction extends AbstractAction {
        
        private final SourceTableRecord master;
        private final SourceTableRecord duplicate;
        
        SetMasterAction(String name, SourceTableRecord master, SourceTableRecord duplicate) {
            super("", masterIcon);
            super.putValue(AbstractAction.SHORT_DESCRIPTION, name);
            this.master = master;
            this.duplicate = duplicate;
        }
        
        public void actionPerformed(ActionEvent e) {
            try {
				pool.defineMaster(master, duplicate);
				pool.store();
			} catch (ArchitectException ex) {
				MMSUtils.showExceptionDialog(panel, "An exception occurred when trying " +
						"to set " + master + " to be the master of " + duplicate, ex);
			} catch (SQLException ex) {
            	MMSUtils.showExceptionDialog(panel, "An exception occurred while trying to " +
            			"store changes to the database.", ex);
            }
            selectionListener.nodeSelected(duplicate);
            graph.repaint();
        }
    }
    
    /**
	 * When this action is fired the master given to the constructor will become
	 * the master of the given duplicate
	 */
    class SetDuplicateAction extends AbstractAction {
        
        private final SourceTableRecord master;
        private final SourceTableRecord duplicate;
        
        SetDuplicateAction(String name, SourceTableRecord master, SourceTableRecord duplicate) {
            super("", duplicateIcon);
            super.putValue(AbstractAction.SHORT_DESCRIPTION, name);
            this.master = master;
            this.duplicate = duplicate;
        }
        
        public void actionPerformed(ActionEvent e) {
            try {
				pool.defineMaster(master, duplicate);
				pool.store();
            } catch (ArchitectException ex) {
				MMSUtils.showExceptionDialog(panel, "An exception occurred when trying " +
						"to set " + duplicate + " to be a duplicate of " + master, ex);
			} catch (SQLException ex) {
            	MMSUtils.showExceptionDialog(panel, "An exception occurred while trying to " +
            			"store changes to the database.", ex);
            }
            selectionListener.nodeSelected(master);
            graph.repaint();
        }
    }
    
    private class MyGraphSelectionListener implements GraphSelectionListener<SourceTableRecord, PotentialMatchRecord> {

        public void nodeDeselected(SourceTableRecord node) {
            recordViewerPanel.removeAll();
            recordViewerPanel.revalidate();
            recordViewerHeader.removeAll();
            recordViewerHeader.revalidate();
        }

        public void nodeSelected(final SourceTableRecord node) {
            try {
                recordViewerPanel.removeAll();
                recordViewerHeader.removeAll();
                BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord> bfs =
                    new BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord>();
                List<SourceTableRecord> reachableNodes = bfs.performSearch(graphModel, node);
                for (SourceTableRecord rec : reachableNodes) {
                    final SourceTableRecord str = rec;
                    
                    SourceTableRecordViewer recordViewer = 
                        new SourceTableRecordViewer(
                            str, node, getActions(node, str));
                    recordViewer.getPanel().addMouseListener(new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                            if (SwingUtilities.isLeftMouseButton(e)){
                                if (e.getClickCount() == 1) {
                                    graph.setFocusedNode(str);
                                    graph.scrollNodeToVisible(str);
                                } else if (e.getClickCount() == 2) {
                                    graph.setSelectedNode(str);
                                    graph.scrollNodeToVisible(str);
                                }
                            }
                        }
                    });
                    recordViewerPanel.add(recordViewer.getPanel());
                    recordViewerHeader.add(recordViewer.getToolBar());
                }
            } catch (Exception ex) {
                MMSUtils.showExceptionDialog(panel, "Couldn't show potential matches", ex);
            }
            recordViewerPanel.revalidate();
            recordViewerHeader.revalidate();
        }     
    }
    
    /**
     * An action to get at the *shudder* Auto-Match feature.
     */
    private class AutoMatchAction extends AbstractAction {
    	/**
    	 * A message that expresses our concerns about the auto-layout feature
    	 * to the user.
    	 */
    	private final String warningMessage = "WARNING: Performing an the auto-match operation will create\n"
											+ "matches between all records that were matched according to the\n"
											+ "selected rule set. It is imperative that you review these\n"
											+ "matches carefully before merging records because this operation\n"
											+ "does NOT rank records based on their perceived usefulness.";
    	
    	public AutoMatchAction(GraphModel<SourceTableRecord, PotentialMatchRecord> model) {
    		super("Auto-Match");
    	}
    	
		public void actionPerformed(ActionEvent e) {
			int response = JOptionPane.showConfirmDialog(panel, warningMessage, "WARNING", JOptionPane.OK_CANCEL_OPTION);
			if (response == JOptionPane.OK_OPTION) {
				try {
					pool.doAutoMatch((String) ruleSetComboBox.getSelectedItem());
					pool.store();
					graph.repaint();
				} catch (Exception ex) {
					MMSUtils.showExceptionDialog(panel, "Auto-Match failed, most likely a database connection error", ex);
				}
			}
		}
    }
    
    /**
     * This is the match whose result table we're visualizing.
     */
    private final Match match;
    
    /**
     * The panel that holds all the GUI components.
     */
    private final JPanel panel;

    /**
     * The visual component that actually displays the match results graph.
     */
    private final GraphViewer<SourceTableRecord, PotentialMatchRecord> graph;

    /**
     * The component that is used to layout the RecordViewer objects
     */
    private final JPanel recordViewerPanel = new JPanel(new RecordViewerLayout(4));

    /**
     * The header component that is fixed above the record viewer panel (it
     * has the match/no match buttons and stuff).
     * <p>
     * The layout strategy here is iffy. It would be better if this header and the
     * recordViewerPanel itself were managed by the same LayoutManager instance,
     * which would ingore all layout requests for this header, and set the sizes
     * for both the record viewer body panel and related header component at the
     * same time (when laying out the body).  The main problem with the current approach
     * is that it's not possible to ensure there will be enough room for the buttons
     * in the header when laying out the body, but we can't have the two layout
     * managers wrestle each other.
     */
    private final JPanel recordViewerHeader = new JPanel(new RecordViewerLayout(0));

    private final MyGraphSelectionListener selectionListener = new MyGraphSelectionListener();

    private final MatchPool pool;

    private final GraphModel<SourceTableRecord, PotentialMatchRecord> graphModel;
    
    private JComboBox ruleSetComboBox;
    
    /**
     * A list of the SQLColumns that we want to display in each SourceTableRecord
     * node in the match validation graph.
     */
    private List<SQLColumn> displayColumns = new ArrayList<SQLColumn>();
    
    public MatchResultVisualizer(Match match) throws SQLException, ArchitectException {
        this.match = match;

        JPanel buttonPanel = new JPanel(new GridLayout(2,2));
        buttonPanel.add(new JButton(exportDotFileAction));
        buttonPanel.add(new JButton(viewerAutoLayoutAction));
        buttonPanel.add(new JButton(resetPoolAction));
        buttonPanel.add(new JButton(chooseDisplayedValueAction));

        pool = new MatchPool(match);
        pool.findAll(displayColumns);
        graphModel = new MatchPoolGraphModel(pool);
        graph = new GraphViewer<SourceTableRecord, PotentialMatchRecord>(graphModel);
        graph.setNodeRenderer(new SourceTableNodeRenderer(displayColumns));
        graph.setEdgeRenderer(new PotentialMatchEdgeRenderer(graph));
        graph.addSelectionListener(selectionListener );
        
        doAutoLayout();

        JPanel autoMatchPanel = new JPanel(new FlowLayout());
        ruleSetComboBox = new JComboBox();
        for (MungeProcess ruleSet : match.getMatchRuleSets()) {
        	ruleSetComboBox.addItem(ruleSet.getName());
        }
        autoMatchPanel.add(new JButton(new AutoMatchAction(graph.getModel())));
        autoMatchPanel.add(new JLabel(":"));
        autoMatchPanel.add(ruleSetComboBox);
        
        JPanel graphPanel = new JPanel(new BorderLayout());
        graphPanel.add(buttonPanel, BorderLayout.NORTH);
        graphPanel.add(new JScrollPane(graph), BorderLayout.CENTER);
        graphPanel.add(autoMatchPanel, BorderLayout.SOUTH);
                
        final JScrollPane recordViewerScrollPane =
            new JScrollPane(recordViewerPanel,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        recordViewerScrollPane.getHorizontalScrollBar().setBlockIncrement(100);
        recordViewerScrollPane.getHorizontalScrollBar().setUnitIncrement(15);
        recordViewerScrollPane.getVerticalScrollBar().setBlockIncrement(100);
        recordViewerScrollPane.getVerticalScrollBar().setUnitIncrement(15);
        
        recordViewerScrollPane.setColumnHeaderView(recordViewerHeader);
        recordViewerScrollPane.setRowHeaderView(SourceTableRecordViewer.headerPanel(match));

        // put it all together
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                graphPanel,
                recordViewerScrollPane);
        
        panel = new JPanel(new BorderLayout(12, 12));
        panel.add(splitPane, BorderLayout.CENTER);
    }
    
    /**
     * Performs auto-layout on the visible graph of matches managed
     * by this component.
     */
    public void doAutoLayout() {
        final GraphModel<SourceTableRecord, PotentialMatchRecord> model = graph.getModel();
        final GraphNodeRenderer<SourceTableRecord> renderer = graph.getNodeRenderer();
        final int hgap = 10;
        final int vgap = 10;
        final int targetHeight = graph.getPreferredGraphLayoutHeight();
        
        ConnectedComponentFinder<SourceTableRecord, PotentialMatchRecord> ccf =
            new ConnectedComponentFinder<SourceTableRecord, PotentialMatchRecord>();
        Set<Set<SourceTableRecord>> components = ccf.findConnectedComponents(model);
        
        int y = 0;
        int x = 0;
        int nextx = 0;
        
        for (Set<SourceTableRecord> component : components) {
            
            // lay out the nodes of this component in a circle
            double angleStep = Math.PI * 2.0 / component.size();
            double currentAngle = 0.0;
            double radius = 100.0;
            Rectangle componentBounds = null;
            Map<SourceTableRecord, Rectangle> componentNodeBounds = new HashMap<SourceTableRecord, Rectangle>();
            
            for (SourceTableRecord node : component) {
                double xx = radius * Math.cos(currentAngle);
                double yy = radius * Math.sin(currentAngle);
                
                Dimension prefSize = renderer.getGraphNodeRendererComponent(node, false, false).getPreferredSize();
                
                final Rectangle nodeBounds = new Rectangle(
                        (int) xx, (int) yy,
                        prefSize.width, prefSize.height);
                if (componentBounds == null) {
                    componentBounds = new Rectangle(nodeBounds);
                } else {
                    componentBounds.add(nodeBounds);
                }
                componentNodeBounds.put(node, nodeBounds);
                
                currentAngle += angleStep;
            }
            
            // fit the laid out component under previous component, or in a
            // new column if nodes won't fit in current column
            Point translate = new Point(-componentBounds.x, -componentBounds.y);
            if (y + componentBounds.height > targetHeight) {
                y = 0;
                x = nextx + hgap;
            }
            
            for (SourceTableRecord node : component) {
                final Rectangle bounds = componentNodeBounds.get(node);
                bounds.translate(x + translate.x, y + translate.y);
                graph.setNodeBounds(node, bounds);
                nextx = Math.max(nextx, bounds.x + bounds.width);
            }
            
            y += componentBounds.height + vgap;
        }
        graph.repaint();
        graph.revalidate();
    }
    
    /**
	 * This method will return the actions allowed between the two given nodes
	 * in a graph.
	 * 
	 * @param lhs
	 *            The node that should be considered on the left for the methods
	 *            that the actions will run.
	 * @param rhs
	 *            The node that should be considered on the right for the
	 *            methods that the actions will run.
	 */
    public List<Action> getActions(SourceTableRecord lhs, SourceTableRecord rhs) {
    	List<Action> actionsAllowed = new ArrayList<Action>();
    	if (lhs == rhs) {
    		actionsAllowed.add(new SetMasterAction("Master of All", lhs, rhs));
    		actionsAllowed.add(new SetNoMatchAction("No Match to Any", lhs, rhs));
    		actionsAllowed.add(new SetUnmatchAction("Unmatch All", lhs, rhs));
    		return actionsAllowed;
    	}
    	
    	GraphModel<SourceTableRecord, PotentialMatchRecord> nonDirectedGraph =
    		new NonDirectedUserValidatedMatchPoolGraphModel(pool, new HashSet<PotentialMatchRecord>());
		BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord> bfs =
            new BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord>();
        Set<SourceTableRecord> reachable = new HashSet<SourceTableRecord>(bfs.performSearch(nonDirectedGraph, lhs));
        
        logger.debug("lhs record is " + lhs.toString());
        logger.debug("rhs record is " + rhs.toString());
        
        PotentialMatchRecord pmr = pool.getPotentialMatchFromOriginals(lhs, rhs);
        if (reachable.contains(rhs)) {
        	logger.debug("reachable contains rhs");
        	actionsAllowed.add(new SetUnmatchAction("Unmatch", lhs, rhs));
        	actionsAllowed.add(new SetNoMatchAction("No Match", lhs, rhs));
        	return actionsAllowed;
        }
        
        actionsAllowed.add(new SetMasterAction("Master", rhs, lhs));
        actionsAllowed.add(new SetDuplicateAction("Duplicate", lhs, rhs));
        if (pmr != null && pmr.getMatchStatus() == MatchType.NOMATCH) {
        	actionsAllowed.add(new SetUnmatchAction("Unmatch", lhs, rhs));
        } else {
        	actionsAllowed.add(new SetNoMatchAction("No Match", lhs, rhs));
        }
        	
    	return actionsAllowed;
    }
    
    // ======= EditorPane stuff ========

    public boolean doSave() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: MatchResultVisualizer.doSave()");
        return true;
    }

    public JComponent getPanel() {
        return panel;
    }

    public boolean hasUnsavedChanges() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: MatchResultVisualizer.hasUnsavedChanges()");
        return false;
    }

	MatchPool getPool() {
		return pool;
	}
}