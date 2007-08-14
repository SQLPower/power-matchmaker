package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchPool;
import ca.sqlpower.matchmaker.PotentialMatchRecord;
import ca.sqlpower.matchmaker.SourceTableRecord;
import ca.sqlpower.matchmaker.graph.BreadthFirstSearch;
import ca.sqlpower.matchmaker.graph.ConnectedComponentFinder;
import ca.sqlpower.matchmaker.graph.GraphModel;
import ca.sqlpower.matchmaker.graph.MatchPoolDotExport;
import ca.sqlpower.matchmaker.graph.MatchPoolGraphModel;
import ca.sqlpower.matchmaker.swingui.graphViewer.GraphNodeRenderer;
import ca.sqlpower.matchmaker.swingui.graphViewer.GraphSelectionListener;
import ca.sqlpower.matchmaker.swingui.graphViewer.GraphViewer;
import ca.sqlpower.swingui.SPSUtils;

/**
 * The MatchResultVisualizer produces graphical representations of the matches
 * in a Match Result Table.
 */
public class MatchResultVisualizer implements EditorPane {
    
    private static final Logger logger = Logger.getLogger(MatchResultVisualizer.class);

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
    
    
    private Action viewerAutoLayoutAction = new AbstractAction("Auto layout") {
        
        public void actionPerformed(ActionEvent e) {
            doAutoLayout();
        }
        
    };

    private class SetNoMatchAction extends AbstractAction{
        private final SourceTableRecord record1;
        private final SourceTableRecord record2;
        
        protected SetNoMatchAction (SourceTableRecord record1, SourceTableRecord record2){
            super("No Match");
            this.record1 = record1;
            this.record2 = record2;
        }
        
        public void actionPerformed(ActionEvent e){
            record1.makeNoMatch(record2);
        }
    }
    
    private class SetMasterAction extends AbstractAction {
        
        private final SourceTableRecord master;
        private final SourceTableRecord duplicate;
        
        SetMasterAction(SourceTableRecord master, SourceTableRecord duplicate) {
            super("Master");
            this.master = master;
            this.duplicate = duplicate;
        }
        
        public void actionPerformed(ActionEvent e) {
            master.makeMaster(duplicate);
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
                    
                    //XXX: fix the SetMasterAction with proper parameters, just put in str
                    //so it compiles for now
                    SourceTableRecordViewer recordViewer = 
                        new SourceTableRecordViewer(
                            str, node, new JButton(new SetMasterAction(node, str)),
                            new JButton(new SetNoMatchAction(node,str)));
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
                    recordViewerHeader.add(recordViewer.getButtonPanel());
                }
            } catch (Exception ex) {
                MMSUtils.showExceptionDialog(panel, "Couldn't show potential matches", ex);
            }
            recordViewerPanel.revalidate();
            recordViewerHeader.revalidate();
        }     
    }    
    
    /**
     * This is the match whose result table we're visualizing.
     */
    private final Match match;
    
    /**
     * The session the match lives in.  Used as a source of database connections,
     * and other session-y stuff.
     */
    private final MatchMakerSwingSession session;

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
    
    public MatchResultVisualizer(Match match, MatchMakerSwingSession session) throws SQLException, ArchitectException {
        this.match = match;
        this.session = session;

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(new JButton(exportDotFileAction));
        buttonPanel.add(new JButton(viewerAutoLayoutAction));

        pool = new MatchPool(match);
        pool.findAll();
        graphModel = new MatchPoolGraphModel(pool);
        graph = new GraphViewer<SourceTableRecord, PotentialMatchRecord>(graphModel);
        graph.setNodeRenderer(new SourceTableNodeRenderer());
        graph.setEdgeRenderer(new PotentialMatchEdgeRenderer(graph));
        graph.addSelectionListener(selectionListener );
        
        doAutoLayout();

        JPanel graphPanel = new JPanel(new BorderLayout());
        graphPanel.add(new JScrollPane(graph), BorderLayout.CENTER);
        graphPanel.add(buttonPanel, BorderLayout.SOUTH);
                
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
}