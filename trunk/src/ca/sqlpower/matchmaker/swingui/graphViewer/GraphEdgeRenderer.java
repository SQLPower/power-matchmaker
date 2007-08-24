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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.i 
 */

package ca.sqlpower.matchmaker.swingui.graphViewer;

import javax.swing.JComponent;

public interface GraphEdgeRenderer<E> {

    /**
     * Returns a JComponent instance that can be used to render the
     * given edge in the graph.
     * <p>
     * We will add more parameters to this method in the future to deal with
     * stuff like selection, focus, etc.
     * 
     * @param edge The edge to render.
     * @return a JComponent that can be used to paint the edge.
     */
    public JComponent getGraphEdgeRendererComponent(E edge);
    
}
