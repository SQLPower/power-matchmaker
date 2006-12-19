package ca.sqlpower.matchmaker.swingui.graphViewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.graph.GraphModel;

public class GraphViewer<V, E> extends JPanel implements Scrollable {
	/**
     * The amount of space moved up or down one mouse click on the scroll pane does
	 */
    private static final int SCROLL_UNIT = 50;

	private static final Logger logger = Logger.getLogger(GraphViewer.class);
	
	/**
	 * The visual magnification factor for this graph.
	 */
	private double zoom = 1.0;

	private GraphModel<V, E> model;
    private GraphNodeRenderer<V> nodeRenderer;
    private GraphEdgeRenderer<E> edgeRenderer;
    private GraphLayoutCache layoutCache;
    // don't support selection yet, but will have a GraphSelectionModel
    // don't support editing yet, but will have GraphNodeEditor and GraphEdgeEditor
	
    
	public GraphViewer(GraphModel<V, E> model, GraphLayoutCache layoutCache) {
        super();
        this.model = model;
        this.layoutCache = layoutCache;
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

    public void setLayoutCache(GraphLayoutCache layoutCache) {
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
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //Save the default colour to be used later
        Color defaultColor = g2.getColor();
        
        //Set the colour to white and paint a rectangle so the GUI paints properly
        g2.setColor(Color.WHITE);
        //XXX: the x and y co-ordinate should be calculated instead of just using (0,0)
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(defaultColor);
        
        for (E edge : model.getEdges()) {
            JComponent er = edgeRenderer.getGraphEdgeRendererComponent(edge);
            er.setSize(er.getPreferredSize());
            Graphics erg = g2.create();
            er.paint(erg);
            erg.dispose();
        }
        for (V node : model.getNodes()) {
            Rectangle nodePos = layoutCache.getNodeBounds(node);
            JComponent nr = nodeRenderer.getGraphNodeRendererComponent(node);
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

	@Override
	public Dimension getPreferredSize() {
		Rectangle g = layoutCache.getBounds();
		Dimension d = new Dimension((int)(g.width*zoom), (int)(g.height*zoom));
		return d;
	}
	
	public Dimension getPreferredScrollableViewportSize() {
        final int height = 400;
		return new Dimension((int) (1.618 * height), height);
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

	public double getZoom() {
		return zoom;
	}

	public void setZoom(double zoom) {
		this.zoom = zoom;
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

}