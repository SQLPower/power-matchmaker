/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.swingui.graphViewer;

/**
 * Interface for clients of the GraphViewer which are interested in taking
 * action when nodes and egdes are selected and deselected.
 *
 * @param <V> The type of node managed by the graph
 * @param <E> The type of edge managed by the graph
 */
public interface GraphSelectionListener<V, E> {
    
    /**
     * Called to notify the listener that a node which was selected is not
     * selected any more.
     */
    void nodeDeselected(V node);

    /**
     * Called to notify the listener that a node which was not selected is
     * now selected.
     */
    void nodeSelected(V node);
    
    // TODO edge support
}
