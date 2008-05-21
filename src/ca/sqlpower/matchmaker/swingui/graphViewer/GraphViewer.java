/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.swingui.graphViewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import ca.sqlpower.graph.GraphModel;

public class GraphViewer<V, E> extends JPanel implements Scrollable {
	/**
     * The amount of space moved up or down one mouse click on the scroll pane does
	 */
    private static final int SCROLL_UNIT = 50;

	private static final Logger logger = Logger.getLogger(GraphViewer.class);

	private GraphModel<V, E> model;
    private GraphNodeRenderer<V> nodeRenderer;
    private GraphEdgeRenderer<E> edgeRenderer;
    private GraphLayoutCache<V, E> layoutCache;

    /**
     * The list of clients interested in changes to the current selection on this graph.
     */
    private List<GraphSelectionListener<V, E>> selectionListeners = new ArrayList<GraphSelectionListener<V, E>>();
    
    /**
     * The node that is currently selected.  The graph viewer only supports
     * single selection right now.  If we go to multiple selection, it would
     * make sense to factor out the selection stuff to a GraphSelectionModel.
     */
    private V selectedNode;
    
    /**
     * The node that currently has focus (the one that any keyboard actions will
     * affect).
     */
    private V focusedNode;
    
    // don't support editing yet, but will have GraphNodeEditor and GraphEdgeEditor
	private final GraphMouseListener mouseListener = new GraphMouseListener();

    /**
     * A hint for components performing the viaual graph layout: How many
     * pixels tall should the graph be, at most?  The graph should always be
     * laid out as compactly as possible, and this value just sets a limit
     * for the overall height of the layout. Note that this hint defaults
     * to Integer.MAX_VALUE, which does not mean we want the graph to take
     * up that much space; just that we want the graph to be laid out in the
     * shortest possible vertical strip (and never start a new column).
     */
    private int preferredGraphLayoutHeight = Integer.MAX_VALUE;
    
	public GraphViewer(GraphModel<V, E> model) {
        super();
        this.model = model;
        this.layoutCache = new DefaultGraphLayoutCache<V, E>();
        addMouseListener(mouseListener);
        setBackground(Color.WHITE);
        logger.debug("Created new instance: " + System.identityHashCode(this));
    }
    
    public GraphEdgeRenderer<E> getEdgeRenderer() {
        return edgeRenderer;
    }

    public void setEdgeRenderer(GraphEdgeRenderer<E> edgeRenderer) {
        this.edgeRenderer = edgeRenderer;
    }

    public GraphLayoutCache getLayoutCache() {
        return layoutCache;
    }

    public void setLayoutCache(GraphLayoutCache<V, E> layoutCache) {
        this.layoutCache = layoutCache;
    }

    public GraphNodeRenderer<V> getNodeRenderer() {
        return nodeRenderer;
    }

    public void setNodeRenderer(GraphNodeRenderer<V> nodeRenderer) {
        this.nodeRenderer = nodeRenderer;
    }

    /**
     * Paints the components of the graph: first paints the edges then paints the nodes.
     * Do not want the edge to be on top of the nodes.
     */
    @Override
	public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        
        // TODO make antialias on/off a public property of this component
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        g2.setColor(getForeground());
        
        Rectangle rect = getVisibleRect();
        for (E edge : model.getEdges()) {
            JComponent er = edgeRenderer.getGraphEdgeRendererComponent(edge);
            if (er.getPreferredSize().getHeight() > rect.y - 200 && er.getPreferredSize().getHeight() < rect.y + rect.height + 200) {
            	er.setSize(er.getPreferredSize());
            	Graphics erg = g2.create();
            	er.paint(erg);
            	erg.dispose();
            }
        }
        for (V node : model.getNodes()) {
            Rectangle nodePos = layoutCache.getNodeBounds(node);
            if (nodePos.y > rect.y - 200 && nodePos.y < rect.y + rect.height + 200) {
	            JComponent nr = nodeRenderer.getGraphNodeRendererComponent(node, node == selectedNode, node == focusedNode);
	            Dimension nodeSize = nr.getPreferredSize();
	            nr.setSize(nodeSize);
	            nodePos.width = nodeSize.width;
	            nodePos.height = nodeSize.height;
	            layoutCache.setNodeBounds(node, nodePos);
	            Graphics nrg = g2.create(nodePos.x, nodePos.y, nodeSize.width, nodeSize.height);
	            nr.paint(nrg);
	            nrg.dispose();
            }
        }
	}

	@Override
	public Dimension getPreferredSize() {
		Rectangle g = layoutCache.getBounds();
		return new Dimension(g.width, g.height);
	}
	
	public Dimension getPreferredScrollableViewportSize() {
        int width = getPreferredSize().width;
		return new Dimension(width, (int) (1.618 * width));
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		if (orientation == SwingConstants.HORIZONTAL) {
		    return visibleRect.width - SCROLL_UNIT;
        } else {
            return visibleRect.height - SCROLL_UNIT;
        }
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return SCROLL_UNIT;
	}
    
    public GraphModel<V, E> getModel() {
        return model;
    }

    public Rectangle getNodeBounds(V node) {
        Rectangle nodeBounds = layoutCache.getNodeBounds(node);
        if (nodeBounds == null){
            throw new NullPointerException("Node bounds for node "+node+" is null. This is not allowed.");
        }
        return nodeBounds;
    }

    public void setNodeBounds(V node, Rectangle bounds) {
        layoutCache.setNodeBounds(node, bounds);
    }

    public void setSelectedNode(V node) {
        if (selectedNode != null) {
            fireNodeDeselected(selectedNode);
            repaint(layoutCache.getNodeBounds(selectedNode));
        }
        selectedNode = node;
        if (selectedNode != null) {
            fireNodeSelected(selectedNode);
            repaint(layoutCache.getNodeBounds(selectedNode));
        }
    }

    public void setFocusedNode(V node) {
        if (focusedNode != null) {
            repaint(layoutCache.getNodeBounds(focusedNode));
        }
        focusedNode = node;
        if (focusedNode != null) {
            repaint(layoutCache.getNodeBounds(focusedNode));
        }
    }

    private void fireNodeSelected(V node) {
        for (int i = selectionListeners.size() - 1; i >= 0; i--) {
            selectionListeners.get(i).nodeSelected(node);
        }
    }

    private void fireNodeDeselected(V node) {
        for (int i = selectionListeners.size() - 1; i >= 0; i--) {
            selectionListeners.get(i).nodeDeselected(node);
        }
    }

    public void addSelectionListener(GraphSelectionListener<V, E> l) {
        selectionListeners.add(l);
    }

    public void removeSelectionListener(GraphSelectionListener<V, E> l) {
        selectionListeners.remove(l);
    }
    
    private class GraphMouseListener extends MouseAdapter {
    
        @Override
        public void mousePressed(MouseEvent e) {
            V node = layoutCache.getNodeAt(e.getPoint());
            setSelectedNode(node);
            setFocusedNode(node);
        }
    }

    public V getSelectedNode() {
        return selectedNode;
    }

    public void scrollNodeToVisible(V node) {
        Rectangle r = layoutCache.getNodeBounds(node);
        //The multipliers are there to try making it so that the node
        //is at the centre of the screen as much as possible.
        scrollRectToVisible(new Rectangle(r.x-r.width*2, r.y-r.height*2, 
                r.width * 5, r.height * 5));
    }

    /**
     * A hint for components performing the viaual graph layout: How many
     * pixels tall should the graph be, at most?
     */
    public int getPreferredGraphLayoutHeight() {
        return preferredGraphLayoutHeight;
    }
}