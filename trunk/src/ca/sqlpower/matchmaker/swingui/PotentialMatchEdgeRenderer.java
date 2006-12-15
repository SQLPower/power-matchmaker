package ca.sqlpower.matchmaker.swingui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;

import javax.swing.JComponent;

import ca.sqlpower.matchmaker.PotentialMatchRecord;
import ca.sqlpower.matchmaker.SourceTableRecord;
import ca.sqlpower.matchmaker.swingui.graphViewer.GraphEdgeRenderer;
import ca.sqlpower.matchmaker.swingui.graphViewer.GraphViewer;

public class PotentialMatchEdgeRenderer extends JComponent implements
        GraphEdgeRenderer<PotentialMatchRecord> {

    private final GraphViewer<SourceTableRecord, PotentialMatchRecord> graph;
    private Rectangle lhsPosition;
    private Rectangle rhsPosition;
    private Stroke edgeStroke;
    private Color edgeColor;
    
    public PotentialMatchEdgeRenderer(GraphViewer<SourceTableRecord, PotentialMatchRecord> graph) {
        setOpaque(false);
        this.graph = graph;
    }
    
    public JComponent getGraphEdgeRendererComponent(PotentialMatchRecord edge) {
        SourceTableRecord origLHS = edge.getOriginalLhs();
        SourceTableRecord origRHS = edge.getOriginalRhs();
        lhsPosition = graph.getNodeBounds(origLHS);
        rhsPosition = graph.getNodeBounds(origRHS);
        edgeColor = Color.BLACK;
        
        // dotted lines are temporarily disabled so we can see the edges more clearly in the absense of sensible layout
//        if (edge.getMatchStatus() == null) {
//            edgeStroke = new BasicStroke(1.7f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0f, new float[] {1.7f, 10f}, 0f);
//        } else if (edge.getMatchStatus() == MatchType.MATCH) {
//            edgeStroke = new BasicStroke(1f);
//        }
        return this;
    }

    @Override
    public Dimension getPreferredSize() {
        int maxx = Math.max(lhsPosition.x + lhsPosition.width, rhsPosition.x + rhsPosition.width);
        int maxy = Math.max(lhsPosition.y + lhsPosition.height, rhsPosition.y + rhsPosition.height);
        return new Dimension(maxx, maxy);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        if (edgeStroke != null) {
            g2.setStroke(edgeStroke);
        }
        g2.setColor(edgeColor);
        g2.drawLine(
                lhsPosition.x + lhsPosition.width/2, lhsPosition.y + lhsPosition.height/2,
                rhsPosition.x + rhsPosition.width/2, rhsPosition.y + rhsPosition.height/2);
    }
}
