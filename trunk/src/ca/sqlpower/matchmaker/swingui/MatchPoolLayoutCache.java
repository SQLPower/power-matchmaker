package ca.sqlpower.matchmaker.swingui;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ca.sqlpower.matchmaker.MatchPool;
import ca.sqlpower.matchmaker.swingui.graphViewer.GraphLayoutCache;

public class MatchPoolLayoutCache implements GraphLayoutCache {

    private final MatchPool pool;
    private final Map<Object, Rectangle> positions = new HashMap<Object, Rectangle>(16,0.5f);
    
    public MatchPoolLayoutCache(final MatchPool pool) {
        this.pool = pool;

        // arrange nodes in a silly grid to start
        int sqrt = (int) Math.sqrt(pool.getSourceTableRecords().size());
        Iterator it = pool.getSourceTableRecords().iterator();
        int y = 0;
        while (it.hasNext()) {
            for (int x = 0; x < sqrt && it.hasNext(); x++) {
                Object node = it.next();
                positions.put(node, new Rectangle(x * 100, y, 90, 30));
            }
            y += 50;
        }
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
