package ca.sqlpower.matchmaker.swingui.graphViewer;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * The GraphLayoutCache interface specifies a mechanism for maintaining the positions
 * of nodes and edges in a graph.  It remembers node bounds provided to it, and can
 * provide hit detection on the items it stores.
 *
 * @param <V> The node type for the graph
 * @param <E> The edge type for the graph
 */
public interface GraphLayoutCache<V, E> {

    /**
     * Returns the smallest rectangle that completely encloses the
     * entire graph layout.
     * 
     * @return
     */
    Rectangle getBounds();

    /**
     * Returns the position of the given node in this graph's layout.
     * 
     * @return The last known position and size of the given node.  Returns
     * null if this node is not in the graph.
     */
    Rectangle getNodeBounds(V node);

    /**
     * Updates the position and size of the given node in this layout cache.
     */
    void setNodeBounds(V node, Rectangle nodePos);
    
    /**
     * Returns the node that is located under the given point.
     * 
     * @param p
     *            The point to look for a node at
     * @return The node object at point p, or null if there is not a node on
     *         that point. If two nodes overlap, returns one or the other
     *         arbitrarily.
     */
    V getNodeAt(Point p);
}
