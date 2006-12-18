package ca.sqlpower.matchmaker.swingui.graphViewer;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;


public class DefaultGraphLayoutCache implements GraphLayoutCache {

    private final Map<Object, Rectangle> positions = new HashMap<Object, Rectangle>(16,0.5f);

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

    public Rectangle getNodeBounds(Object node) {
        Rectangle bounds = positions.get(node);
        if (bounds == null) {
            return null;
        } else {
            return new Rectangle(bounds);
        }
    }

    public void setNodeBounds(Object node, Rectangle nodePos) {
        positions.put(node, new Rectangle(nodePos));
    }

}
