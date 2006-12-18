package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.layout.AbstractLayoutNode;
import ca.sqlpower.architect.layout.LayoutEdge;
import ca.sqlpower.architect.layout.LayoutNode;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchPool;
import ca.sqlpower.matchmaker.PotentialMatchRecord;
import ca.sqlpower.matchmaker.SourceTableRecord;
import ca.sqlpower.matchmaker.graph.ConnectedComponentFinder;
import ca.sqlpower.matchmaker.graph.GraphModel;
import ca.sqlpower.matchmaker.graph.MatchPoolDotExport;
import ca.sqlpower.matchmaker.graph.MatchPoolGraphModel;
import ca.sqlpower.matchmaker.swingui.graphViewer.DefaultGraphLayoutCache;
import ca.sqlpower.matchmaker.swingui.graphViewer.GraphLayoutCache;
import ca.sqlpower.matchmaker.swingui.graphViewer.GraphNodeRenderer;
import ca.sqlpower.matchmaker.swingui.graphViewer.GraphViewer;

/**
 * The MatchResultVisualizer produces graphical representations of the matches
 * in a Match Result Table.
 */
public class MatchResultVisualizer {
    
    private static final Logger logger = Logger.getLogger(MatchResultVisualizer.class);

    /**
     * This action just calls the {@link #refreshGraph()} method.
     */
    private final Action exportDotFileAction = new AbstractAction("Export as Dot file") {
        public void actionPerformed(ActionEvent e) {
            try {
                refreshGraph();
            } catch (Exception ex) {
                ASUtils.showExceptionDialogNoReport(dotExportPanel, "Couldn't export dot file!", ex);
            }
        }
    };
    
    /**
     * This panel holds all the interface components for graph settings.
     */
    private final JPanel dotExportPanel;
    
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
     * The dialog that contains the SettingsPanel.  The constructor
     * creates it and sets its parent to the SwingSession's frame. 
     */
    private final JDialog dialog;

    /**
     * Text field that holds the pathname for output location for the generated dot file.
     */
    private JTextField dotFileField;

    private final GraphViewer<SourceTableRecord, PotentialMatchRecord> viewer;
    
    /**
     * Pops up a file chooser for the dotFileField.
     */
    private Action pickDotFileAction = new AbstractAction("...") {
        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser();
            int choice = fc.showDialog(dotExportPanel, "Choose");
            if (choice == JOptionPane.OK_OPTION) {
                dotFileField.setText(fc.getSelectedFile().getAbsolutePath());
            }
        }
    };
    
    private Action viewerAutoLayoutAction = new AbstractAction("Auto layout") {
        
        public void actionPerformed(ActionEvent e) {
            doAutoLayout();
        }
        
    };

    private Map<PotentialMatchRecord, LayoutEdge> edges;
    private Map<SourceTableRecord, LayoutNode> nodes;

    public MatchResultVisualizer(Match match, MatchMakerSwingSession session) throws SQLException, ArchitectException {
        this.match = match;
        this.session = session;
        dotExportPanel = new JPanel();
        dotExportPanel.add(dotFileField = new JTextField());
        dotExportPanel.add(new JButton(pickDotFileAction ));
        dotExportPanel.add(new JButton(exportDotFileAction));
        
        File dotFile = new File(
                System.getProperty("user.home"), "matchmaker_graph_"+match.getName()+".dot");
        dotFileField.setText(dotFile.getAbsolutePath());
        
        MatchPool pool = new MatchPool(match);
        pool.findAll();
        GraphModel<SourceTableRecord, PotentialMatchRecord> model = new MatchPoolGraphModel(pool);
        GraphLayoutCache layoutCache = new DefaultGraphLayoutCache();
        viewer = new GraphViewer<SourceTableRecord, PotentialMatchRecord>(model, layoutCache);
        viewer.setNodeRenderer(new SourceTableNodeRenderer());
        viewer.setEdgeRenderer(new PotentialMatchEdgeRenderer(viewer));

        doAutoLayout();
        
        JPanel viewerSettingsPanel = new JPanel(new FlowLayout());
        viewerSettingsPanel.add(new JButton(viewerAutoLayoutAction));
        
        dialog = new JDialog(session.getFrame());
        
        JComponent cp = (JComponent) dialog.getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(viewerSettingsPanel, BorderLayout.NORTH);
        cp.add(dotExportPanel, BorderLayout.SOUTH);
        cp.add(new JScrollPane(viewer), BorderLayout.CENTER);
        
        dialog.setTitle("Match Result Visualizer");
        dialog.pack();
        dialog.setLocationRelativeTo(session.getFrame());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    public JPanel getDotExportPanel() {
        return dotExportPanel;
    }
    
    public void showDialog() {
        dialog.setVisible(true);
        dialog.requestFocus();
    }

    public void refreshGraph() throws SQLException, IOException, ArchitectException {
        MatchPoolDotExport exporter = new MatchPoolDotExport(match);
        exporter.setDotFile(new File(dotFileField.getText()));
        exporter.exportDotFile();
    }

    /**
     * Performs auto-layout on the visible graph of matches managed
     * by this component.
     */
    public void doAutoLayout() {
        edges = new HashMap<PotentialMatchRecord, LayoutEdge>();
        nodes = new HashMap<SourceTableRecord, LayoutNode>();
        final GraphModel<SourceTableRecord, PotentialMatchRecord> model = viewer.getModel();
        final GraphNodeRenderer<SourceTableRecord> renderer = viewer.getNodeRenderer();
        final int vgap = 10;
        
        ConnectedComponentFinder<SourceTableRecord, PotentialMatchRecord> ccf =
            new ConnectedComponentFinder<SourceTableRecord, PotentialMatchRecord>();
        Set<Set<SourceTableRecord>> components = ccf.findConnectedComponents(model);
        
        int y = 0;
        for (Set<SourceTableRecord> component : components) {
            
            logger.debug("Positioning component with "+component.size()+" nodes");
            double angleStep = Math.PI * 2.0 / component.size();
            double currentAngle = 0.0;
            double radius = 100.0;
            double x = radius;
            Rectangle componentBounds = null;
            Map<SourceTableRecord, Rectangle> componentNodeBounds = new HashMap<SourceTableRecord, Rectangle>();
            
            for (SourceTableRecord node : component) {
                double xx = radius * Math.cos(currentAngle);
                double yy = radius * Math.sin(currentAngle);
                
                Dimension prefSize = renderer.getGraphNodeRendererComponent(node).getPreferredSize();
                
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
            
            Point translate = new Point(-componentBounds.x, -componentBounds.y);
            
            for (SourceTableRecord node : component) {
                final Rectangle bounds = componentNodeBounds.get(node);
                bounds.translate(translate.x, y + translate.y);
                viewer.setNodeBounds(node, bounds);
            }
            
            y += componentBounds.height + vgap;
        }
        
        viewer.repaint();
    }
    
    private LayoutNode makeLayoutNodeAdapter(SourceTableRecord str) {
        LayoutNode adapter = nodes.get(str);
        if (adapter == null) {
            adapter = new MatchResultLayoutNode(str);
            nodes.put(str, adapter);
        }
        return adapter;
    }
    
    private LayoutEdge makeLayoutEdgeAdapter(PotentialMatchRecord pmr) {
        LayoutEdge adapter = edges.get(pmr);
        if (adapter == null) {
            adapter = new MatchResultLayoutEdge(pmr);
            edges.put(pmr, adapter);
        }
        return adapter;
    }
    
    /**
     * An adapter for the PotentialMatchRecord's original match pointers,
     * which are immutable. Does not deal with the LHS/RHS relations that
     * are mutable.
     */
    class MatchResultLayoutEdge implements LayoutEdge {
        
        private final LayoutNode head;
        private final LayoutNode tail;
        
        /**
         * Takes a snapshot of the original LHS and RHS records, which are
         * immutable, so they don't need to be created dynamically.
         */
        MatchResultLayoutEdge(PotentialMatchRecord edge) {
            head = makeLayoutNodeAdapter(edge.getOriginalLhs());
            tail = makeLayoutNodeAdapter(edge.getOriginalRhs());
        }
        
        /**
         * Returns the original LHS of the edge, no matter what.  This
         * is ok because the original matching edge is not directional.
         */
        public LayoutNode getHeadNode() {
            return head;
        }
        
        /**
         * Returns the original RHS of the edge, no matter what.  This
         * is ok because the original matching edge is not directional.
         */
        public LayoutNode getTailNode() {
            return tail;
        }
        
    }
    
    class MatchResultLayoutNode extends AbstractLayoutNode {
        
        private final SourceTableRecord node;
        
        MatchResultLayoutNode(SourceTableRecord node) {
            if (node == null) throw new NullPointerException("Can't create a LayoutNode proxy for null node");
            this.node = node;
        }
        
        @Override
        public String getNodeName() {
            return node.getKeyValues().toString();
        }
        
        @Override
        public Rectangle getBounds(Rectangle b) {
            Rectangle nodeBounds = viewer.getNodeBounds(node);
            b.x = nodeBounds.x;
            b.y = nodeBounds.y;
            return b;
        }
        
        @Override
        public void setBounds(int x, int y, int width, int height) {
            viewer.setNodeBounds(node, new Rectangle(x, y, width, height));
        }
        
        @Override
        public List<LayoutEdge> getInboundEdges() {
            List<LayoutEdge> inbound = new ArrayList<LayoutEdge>();
            for (PotentialMatchRecord pmr : viewer.getModel().getInboundEdges(node)) {
                inbound.add(makeLayoutEdgeAdapter(pmr));
            }
            return inbound;
        }
        
        @Override
        public List<LayoutEdge> getOutboundEdges() {
            List<LayoutEdge> outbound = new ArrayList<LayoutEdge>();
            for (PotentialMatchRecord pmr : viewer.getModel().getOutboundEdges(node)) {
                outbound.add(makeLayoutEdgeAdapter(pmr));
            }
            return outbound;
        }
        
    }
}