package ca.sqlpower.matchmaker.swingui.graphViewer;

import java.awt.Rectangle;

public interface GraphLayoutCache {

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
    Rectangle getNodeBounds(Object node);

    /**
     * Updates the position and size of the given node in this layout cache.
     */
    void setNodeBounds(Object node, Rectangle nodePos);
}
