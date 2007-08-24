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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.i 
 */

package ca.sqlpower.matchmaker.swingui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Line2D;

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

            int masterCentreX = masterPosition.x + masterPosition.width / 2;
            int masterCentreY = masterPosition.y + masterPosition.height / 2;
            int duplicateCentreX = duplicatePosition.x + duplicatePosition.width / 2;
            int duplicateCentreY = duplicatePosition.y + duplicatePosition.height / 2;
            
            
            // Draw the solid Match line
            Line2D matchLine = new Line2D.Double(masterCentreX, masterCentreY,
            								duplicateCentreX, duplicateCentreY);
            g2.draw(matchLine);

            // Find the intersect coordinates of the match line with the master node
            int masterIntersectX = masterCentreX;
            int masterIntersectY = masterCentreY;

            // Get the coefficients for the linear equation y = ax + b
            // Calculate the slope of the line (a)
            float deltaY = masterCentreY - duplicateCentreY;
            float deltaX = masterCentreX - duplicateCentreX;
            
            // Set to max value (pseudo-infinity). If deltaX = 0, then calculate slope.
            float a = Float.MAX_VALUE;
            if (deltaX != 0) {
            	a = deltaY / deltaX;
            }
            // Then calculate b
            float b = masterCentreY - a * masterCentreX;
    
            // Find out which side of the node 'rectangle' that the line intersects, and then calculate it's intersection point.
            if (matchLine.intersectsLine(masterPosition.x, masterPosition.y, masterPosition.x, masterPosition.y + masterPosition.height)) {
            	// If it intersects the top side
            	masterIntersectX = masterPosition.x;
            	masterIntersectY = Math.round(a * masterIntersectX + b);
            } else if (matchLine.intersectsLine(masterPosition.x, masterPosition.y, masterPosition.x + masterPosition.width, masterPosition.y)) {
            	// If it intersects the left side
            	masterIntersectY = masterPosition.y;
            	if (deltaX != 0) {
            		masterIntersectX = Math.round((masterIntersectY - b) / a);
            	} else {
            		masterIntersectX = masterCentreX;
            	}
            } else if (matchLine.intersectsLine(masterPosition.x + masterPosition.width, masterPosition.y, masterPosition.x + masterPosition.width, masterPosition.y + masterPosition.height)) {
            	// If it intersects the bottom side
            	masterIntersectX = masterPosition.x + masterPosition.width;
            	masterIntersectY = Math.round(a * masterIntersectX + b);
            } else { 
            	// If it intersects the right side
            	masterIntersectY = masterPosition.y + masterPosition.height;
            	if (deltaX != 0) {
            		masterIntersectX = Math.round((masterIntersectY - b) / a);
            	} else {
            		masterIntersectX = masterCentreX;
            	}
            }
            
            // Draw a small oval around the intersect point to mark which end is the master
            // FIXME: Could draw an arrowhead instead, but is that actually better?
            g2.fillOval(masterIntersectX - 5, masterIntersectY - 5, 10, 10);
        }
    }
}