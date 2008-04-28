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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

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
