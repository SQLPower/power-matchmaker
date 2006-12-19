package ca.sqlpower.matchmaker.swingui.graphViewer;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;


public class DefaultGraphLayoutCache<V, E> implements GraphLayoutCache<V, E> {

    private final Map<V, Rectangle> positions = new HashMap<V, Rectangle>(16,0.5f);

    /**
     * Creates a layout cache with no positions assigned to any nodes.
     */
    public DefaultGraphLayoutCache() {
    }

    public Rectangle getBounds() {
        int minx = Integer.MAX_VALUE;
        int miny = Integer.MAX_VALUE;
        int maxx = Integer.MIN_VALUE;
        int maxy = Integer.MIN_VALUE;
        for (Rectangle rect : positions.values()) {
            minx = Math.min(minx, rect.x);
            miny = Math.min(miny, rect.y);
            maxx = Math.max(maxx, rect.x + rect.width);
            maxy = Math.max(maxy, rect.y + rect.height);
        }
        return new Rectangle(minx, miny, maxx-minx, maxy-miny);
    }

    public Rectangle getNodeBounds(V node) {
        Rectangle bounds = positions.get(node);
        if (bounds == null) {
            return null;
        } else {
            return new Rectangle(bounds);
        }
    }

    public void setNodeBounds(V node, Rectangle nodePos) {
        positions.put(node, new Rectangle(nodePos));
    }

    public V getNodeAt(Point p) {
        for (Map.Entry<V, Rectangle> entry : positions.entrySet()) {
            if (entry.getValue().contains(p)) {
                return entry.getKey();
            }
        }
        return null;
    }

}
