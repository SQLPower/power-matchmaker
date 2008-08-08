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

	/**
	 * Clears the position of the nodes.
	 */
    public void clearNodes(){
    	positions.clear();
    }
}
