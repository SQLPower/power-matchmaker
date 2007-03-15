package ca.sqlpower.matchmaker.swingui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;

import javax.swing.JComponent;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.PotentialMatchRecord;
import ca.sqlpower.matchmaker.SourceTableRecord;
import ca.sqlpower.matchmaker.swingui.graphViewer.GraphEdgeRenderer;
import ca.sqlpower.matchmaker.swingui.graphViewer.GraphViewer;

public class PotentialMatchEdgeRenderer extends JComponent implements
        GraphEdgeRenderer<PotentialMatchRecord> {

    private static final Logger logger = Logger.getLogger(PotentialMatchEdgeRenderer.class);
    
    private final GraphViewer<SourceTableRecord, PotentialMatchRecord> graph;
    
    /**
     * The line style to use for drawing the edges that connect nodes which were
     * originally marked as potential matches.  This is a dashed line.
     */
    private static final Stroke ORIGINAL_EDGE_STROKE =
        new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0f, new float[] {10f, 2.7f}, 0f);

    /**
     * The line style to use for pairs of records which are marked as MATCH,
     * because the user verified and validated the nodes as being related for real.
     * This is a solid line.
     */
    private static final Stroke CURRENT_EDGE_STROKE = new BasicStroke(1.2f);

    private Rectangle origLhsPosition;
    private Rectangle origRhsPosition;
    
    private Rectangle masterPosition;
    private Rectangle duplicatePosition;
    
    private Color edgeColor;
    
    private PotentialMatchRecord edge;
    
    public PotentialMatchEdgeRenderer(GraphViewer<SourceTableRecord, PotentialMatchRecord> graph) {
        setOpaque(false);
        this.graph = graph;
    }
    
    public JComponent getGraphEdgeRendererComponent(PotentialMatchRecord edge) {
        this.edge = edge;
        edgeColor = edge.getCriteriaGroup().getColour();
        
        // original edge
        SourceTableRecord origLHS = edge.getOriginalLhs();
        SourceTableRecord origRHS = edge.getOriginalRhs();
        origLhsPosition = graph.getNodeBounds(origLHS);
        origRhsPosition = graph.getNodeBounds(origRHS);

        // current edge
        SourceTableRecord master = edge.getMaster();
        SourceTableRecord duplicate = edge.getDuplicate();
        if (master != null && duplicate != null) {
            logger.debug("edge="+edge);
            logger.debug("master="+master+"; duplicate="+duplicate);
            
            masterPosition = graph.getNodeBounds(master);
            duplicatePosition = graph.getNodeBounds(duplicate);
        } else {
            masterPosition = null;
            duplicatePosition = null;
        }

        return this;
    }

    @Override
    public Dimension getPreferredSize() {
        // FIXME doesn't calculate minimum x and y... they won't usually be 0!
        int maxx = Math.max(origLhsPosition.x + origLhsPosition.width, origRhsPosition.x + origRhsPosition.width);
        int maxy = Math.max(origLhsPosition.y + origLhsPosition.height, origRhsPosition.y + origRhsPosition.height);
        if (masterPosition != null && duplicatePosition != null) {
            maxx = Math.max(maxx, masterPosition.x + masterPosition.width);
            maxx = Math.max(maxx, duplicatePosition.x + duplicatePosition.width);
            maxy = Math.max(maxy, masterPosition.y + masterPosition.height);
            maxy = Math.max(maxy, duplicatePosition.y + duplicatePosition.height);
        }
        return new Dimension(maxx, maxy);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        if (edgeColor == null){
            edgeColor = Color.BLACK;
        }
        g2.setColor(edgeColor);

        // always draw original edge
        g2.setStroke(ORIGINAL_EDGE_STROKE);
        g2.drawLine(
                origLhsPosition.x + origLhsPosition.width/2, origLhsPosition.y + origLhsPosition.height/2,
                origRhsPosition.x + origRhsPosition.width/2, origRhsPosition.y + origRhsPosition.height/2);

        if (masterPosition != null && duplicatePosition != null) {
            g2.setStroke(CURRENT_EDGE_STROKE);
            g2.drawLine(
                masterPosition.x + masterPosition.width/2, masterPosition.y + masterPosition.height/2,
                duplicatePosition.x + duplicatePosition.width/2, duplicatePosition.y + duplicatePosition.height/2);
            
            // FIXME: we need an arrowhead, and we need to calculate where it intersects the rectangle
            g2.fillOval(masterPosition.x - 7, masterPosition.y - 7, 14, 14);
        }
    }
}