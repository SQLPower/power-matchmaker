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

package ca.sqlpower.matchmaker.munge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ca.sqlpower.graph.GraphModel;

/**
 * Represents a collection of interconnected munge steps as a directed graph.
 * Vertices are the steps themselves; edges are directed away from source steps
 * toward the target steps (in other words, the edges are in the direction that
 * the data flows). However, adjacency is defined to be from target steps toward
 * source steps (the opposite to the direction of data flow). This seemingly
 * backward definition of adjacency is in place because there is not an easy way
 * of finding outbound edges. If necessary, we can make a new graph model class
 * that has an efficient way of implementing the "natural" definition of
 * adjacency, but it probably won't be necessary.
 * <p>
 * Another deficiency of this graph model is that it cannot differentiate between
 * two connections from a single MungeStepOutput to two inputs of the same target
 * step.  For the purposes that this graph model was designed for, this will not
 * matter, but again, if this becomes a problem, a new graph model could be created
 * to address this problem.  The solution would be to include the input index along
 * with the target step in the Edge class.
 * 
 * @version $Id$
 */
public class MungeProcessGraphModel implements GraphModel<MungeStep, MungeProcessGraphModel.Edge> {

    /**
     * Uniquely identifies an edge in this graph model as the combination
     * of a MungeStepOutput and the MungeStep it is connected to.
     */
    public static final class Edge {
        
        private final MungeStepOutput output;
        private final MungeStep target;
        
        /**
         * @param output
         * @param target
         */
        public Edge(final MungeStepOutput output, final MungeStep target) {
            super();
            this.output = output;
            this.target = target;
        }
        
        public MungeStepOutput getOutput() {
            return output;
        }
        public MungeStep getTarget() {
            return target;
        }
        
        /**
         * Performs an equality check based on the output and target members of
         * this edge referring to the same instances as those of the given edge.
         */
        @Override
        public boolean equals(Object obj) {
            if (! (obj instanceof Edge)) return false;
            Edge other = (Edge) obj;
            return this.output == other.output && this.target == other.target;
        }
        
        /**
         * Calculates the hash code based on those of the output and target member
         * variables.
         */
        @Override
        public int hashCode() {
            int hash = 31;
            
            hash += hash * output.hashCode();
            hash += hash * target.hashCode();
            
            return hash;
        }
    }
    
    private final List<MungeStep> nodes;
    private final List<Edge> edges;
    
    public MungeProcessGraphModel(List<MungeStep> steps) {
        nodes = new ArrayList<MungeStep>(steps);
        edges = new ArrayList<Edge>();
        for (MungeStep step : steps) {
            for (MungeStepOutput o : step.getMSOInputs()) {
                if (o != null) {
                    edges.add(new Edge(o, step));
                }
            }
        }
    }
    
    public Collection<MungeStep> getAdjacentNodes(MungeStep node) {
        List<MungeStep> adj = new ArrayList<MungeStep>();
        for (MungeStepOutput o : node.getMSOInputs()) {
            if (o != null) adj.add(o.getParent());
        }
        return adj;
    }

    public Collection<Edge> getEdges() {
        return edges;
    }

    public Collection<Edge> getInboundEdges(MungeStep step) {
        List<Edge> obe = new ArrayList<Edge>();
        for (MungeStepOutput o : step.getMSOInputs()) {
            if (o != null) {
                obe.add(new Edge(o, step));
            }
        }
        return obe;
    }

    public Collection<MungeStep> getNodes() {
        return nodes;
    }

    /**
     * Finds all the edges coming away from a given node.  In its current
     * implementation, this method performs O(E) operations so it is significantly
     * less performant than {@link #getInboundEdges(MungeStep)}.
     * 
     * @param node
     * @return
     */
    public Collection<Edge> getOutboundEdges(MungeStep node) {
        List<MungeStepOutput> nodeOutputs = node.getChildren(MungeStepOutput.class);
        List<Edge> obe = new ArrayList<Edge>();
        for (Edge e : edges) {
            if (nodeOutputs.contains(e.output)) {
                obe.add(e);
            }
        }
        return obe;
    }

}
