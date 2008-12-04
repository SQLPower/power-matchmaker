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


package ca.sqlpower.matchmaker.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.graph.BreadthFirstSearch;
import ca.sqlpower.graph.GraphModel;
import ca.sqlpower.matchmaker.MatchPool;
import ca.sqlpower.matchmaker.PotentialMatchRecord;
import ca.sqlpower.matchmaker.SourceTableRecord;
import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;

/**
 * Models a match pool as a non-directed graph, where the nodes are the source table records
 * of the match pool, and the edges are the original match relationships between those
 * records, as decided by the match engine.
 */
public class MatchPoolGraphModel implements GraphModel<SourceTableRecord, PotentialMatchRecord> {

    private static final Logger logger = Logger.getLogger(MatchPoolGraphModel.class);
    private final MatchPool pool;
    
    public MatchPoolGraphModel(MatchPool pool) {
        logger.debug("Creating new instance");
        this.pool = pool;
    }
    
    public Collection<PotentialMatchRecord> getEdges() {
        return pool.getPotentialMatches();
    }

    public Collection<SourceTableRecord> getNodes() {
        return pool.getSourceTableRecords();
    }

    /**
     * Returns a list of all nodes that are immediate neighbours to the given
     * node.  For all reachable nodes, consider using {@link BreadthFirstSearch}.
     */
    public Collection<SourceTableRecord> getAdjacentNodes(SourceTableRecord node) {
        Collection<SourceTableRecord> adjacentNodes = new HashSet<SourceTableRecord>();
        for (PotentialMatchRecord pmr : node.getOriginalMatchEdges()) {
            if (pmr.getOriginalLhs() != node) {
                adjacentNodes.add(pmr.getOriginalLhs());
            } else {
                adjacentNodes.add(pmr.getOriginalRhs());
            }
        }
        return adjacentNodes;
    }

    /**
     * Returns all potential match records that were originally associated with
     * the given source record (either as the original lhs or original rhs).
     * <p>
     * Since this model is of an undirected graph, the getOutboundEdges method
     * returns the same list.
     */
    public Collection<PotentialMatchRecord> getInboundEdges(SourceTableRecord node) {
        return node.getOriginalMatchEdges();
    }

    /**
     * Returns all potential match records that were originally associated with
     * the given source record (either as the original lhs or original rhs).
     * <p>
     * Since this model is of an undirected graph, the getInboundEdges method
     * returns the same list.
     */
    public Collection<PotentialMatchRecord> getOutboundEdges(SourceTableRecord node) {
        return getInboundEdges(node);
    }
    
    /**
	 * Sets the Match Status to {@link MatchType#UNMATCH} for all
	 * PotentialMatchRecords in the maximally connected component that the given
	 * selected SourceTableRecord belongs to.
	 * 
	 * @param selectedNode
	 *            The SourceTableRecord which belongs to the maximally connected
	 *            component of this graph model (or 'cluster' as we're calling
	 *            it) that we want to reset
	 */
    public void resetCluster(SourceTableRecord selectedNode) {
    	List<SourceTableRecord> resetNodes = new ArrayList<SourceTableRecord>();
    	resetNodeAndNeighbours(selectedNode, resetNodes);
    }
    
    /**
	 * Helper method for {@link #resetCluster(SourceTableRecord)} that resets
	 * all PotentialMatchRecords for a given node and recursively does for for
	 * all of its neighbours that have not been reset yet.
	 * 
	 * @param selectedNode
	 *            The SourceTableRecord for which its PotentialMatchRecords will
	 *            be reset
	 * @param resetNodes
	 *            A list of SourceTableRecords that already have been reset, so
	 *            that we don't keep resetting them and end up with a stack
	 *            overflow (yes it's already happened :P)
	 */
    private void resetNodeAndNeighbours(SourceTableRecord selectedNode, List<SourceTableRecord> resetNodes) {
		resetNodes.add(selectedNode);
		
		for (PotentialMatchRecord p: getOutboundEdges(selectedNode)) {
			p.setMatchStatus(MatchType.UNMATCH);
		}
		
		for (SourceTableRecord s: getAdjacentNodes(selectedNode)) {
			if (!resetNodes.contains(s)) {
				resetNodeAndNeighbours(s, resetNodes);
			}
		}
	}
}
