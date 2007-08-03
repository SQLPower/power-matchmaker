package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
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
            viewer.repaint();
        }
    }
    
    private class MyGraphSelectionListener implements GraphSelectionListener<SourceTableRecord, PotentialMatchRecord> {

        public void nodeDeselected(SourceTableRecord node) {
            recordViewerPanel.removeAll();
            recordViewerPanel.revalidate();
        }

        public void nodeSelected(final SourceTableRecord node) {
            try {
                recordViewerPanel.removeAll();
                recordViewerPanel.add(SourceTableRecordViewer.headerPanel(match));
                BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord> bfs =
                    new BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord>();
                List<SourceTableRecord> reachableNodes = bfs.performSearch(graphModel, node);
                for (SourceTableRecord rec : reachableNodes) {
                    final SourceTableRecord str = rec;
                    
                    //XXX: fix the SetMasterAction with proper parameters, just put in str
                    //so it compiles for now
                    JPanel recordViewer = new SourceTableRecordViewer(
                            str, node, new JButton(new SetMasterAction(node, str)),
                            new JButton(new SetNoMatchAction(node,str))).getPanel();
                    recordViewer.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                            viewer.setFocusedNode(str);
                            viewer.scrollNodeToVisible(str);
                        }
                    });
                    recordViewerPanel.add(recordViewer);
                }
            } catch (Exception ex) {
                MMSUtils.showExceptionDialog(panel, "Couldn't show potential matches", ex);
            }
            recordViewerPanel.revalidate();
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
    private final GraphViewer<SourceTableRecord, PotentialMatchRecord> viewer;

    private final JPanel recordViewerPanel;

    private final MyGraphSelectionListener selectionListener = new MyGraphSelectionListener();

    private final MatchPool pool;

    private final GraphModel<SourceTableRecord, PotentialMatchRecord> graphModel;
    
    public MatchResultVisualizer(Match match, MatchMakerSwingSession session) throws SQLException, ArchitectException {
        this.match = match;
        this.session = session;

        // The top panel
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JButton(exportDotFileAction));
        topPanel.add(new JButton(viewerAutoLayoutAction));

        pool = new MatchPool(match);
        pool.findAll();
        graphModel = new MatchPoolGraphModel(pool);
        viewer = new GraphViewer<SourceTableRecord, PotentialMatchRecord>(graphModel);
        viewer.setNodeRenderer(new SourceTableNodeRenderer());
        viewer.setEdgeRenderer(new PotentialMatchEdgeRenderer(viewer));
        viewer.addSelectionListener(selectionListener );
        
        doAutoLayout();
        
        recordViewerPanel = new JPanel(new GridLayout(1, 0));
        
        // put it all together
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(viewer),
                new JScrollPane(recordViewerPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
        
        panel = new JPanel(new BorderLayout(12, 12));
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);
    }
    
    /**
     * Performs auto-layout on the visible graph of matches managed
     * by this component.
     */
    public void doAutoLayout() {
        final GraphModel<SourceTableRecord, PotentialMatchRecord> model = viewer.getModel();
        final GraphNodeRenderer<SourceTableRecord> renderer = viewer.getNodeRenderer();
        final int hgap = 10;
        final int vgap = 10;
        final int targetHeight = viewer.getPreferredScrollableViewportSize().height;
        
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
                viewer.setNodeBounds(node, bounds);
                nextx = Math.max(nextx, bounds.x + bounds.width);
            }
            
            y += componentBounds.height + vgap;
        }
        
        viewer.repaint();
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