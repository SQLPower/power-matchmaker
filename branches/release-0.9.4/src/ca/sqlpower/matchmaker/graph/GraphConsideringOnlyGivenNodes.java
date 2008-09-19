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
import java.util.Set;

import org.apache.log4j.Logger;

import ca.sqlpower.graph.GraphModel;
import ca.sqlpower.matchmaker.MatchPool;
import ca.sqlpower.matchmaker.PotentialMatchRecord;
import ca.sqlpower.matchmaker.SourceTableRecord;

/**
 * This graph takes a pool to select edges from and a set of nodes to create a
 * graph from based on the edges in the pool. This graph is dependent on the nodes
 * having a correct potentialMatches list. The potentialMatches list is the list of
 * all PotentialMatchRecords that the node is connected to.
 */
public class GraphConsideringOnlyGivenNodes implements
	GraphModel<SourceTableRecord, PotentialMatchRecord> {

	private static final Logger logger = Logger.getLogger(GraphConsideringOnlyGivenNodes.class);
	
	/**
	 * This pool should contain the nodes that are given in this graph as well as 
	 * the edges that connect the nodes in the graph.
	 */
	private MatchPool pool;
	
	/**
	 * This set of nodes will be the only nodes in the graph. No other nodes from
	 * the pool should be considered by this graph.
	 */
	private Set<SourceTableRecord> nodes;
	
	public GraphConsideringOnlyGivenNodes(MatchPool pool, Set<SourceTableRecord> nodes) {
		this.pool = pool;
		this.nodes = nodes;
	}
	
	public Collection<SourceTableRecord> getAdjacentNodes(SourceTableRecord node) {
		List<SourceTableRecord> adjacentNodes = new ArrayList<SourceTableRecord>();
		
		//XXX We should be able to make this faster. Possibly store the edges and listen to
		//the pool for changes in the potential matches to know when to update our edges.
		for (PotentialMatchRecord pmr: node.getOriginalMatchEdges()) {
			if (pmr.getOriginalLhs() == node) {
				if (nodes.contains(pmr.getOriginalRhs())) {
					adjacentNodes.add(pmr.getOriginalRhs());
				}
			} else if (pmr.getOriginalRhs() == node && nodes.contains(pmr.getOriginalLhs())) {
				adjacentNodes.add(pmr.getOriginalLhs());
			}
		}
		if (logger.isDebugEnabled()) {
		    logger.debug("Adjacent nodes to " + node + " are " + adjacentNodes);
		}
		return adjacentNodes;
	}

	public Collection<PotentialMatchRecord> getEdges() {
		Collection<PotentialMatchRecord> edges = new ArrayList<PotentialMatchRecord>();
        for (PotentialMatchRecord pmr : pool.getPotentialMatches()){
            if (nodes.contains(pmr.getOriginalLhs()) && nodes.contains(pmr.getOriginalRhs())){
                edges.add(pmr);
            }
        }
        return edges;
	}

	public Collection<PotentialMatchRecord> getInboundEdges(SourceTableRecord node) {
		Set<PotentialMatchRecord> outboundEdges = new HashSet<PotentialMatchRecord>();
		for (PotentialMatchRecord pmr : node.getOriginalMatchEdges()) {
			if ((pmr.getOriginalLhs() == node && nodes.contains(pmr.getOriginalRhs()) 
					|| (pmr.getOriginalRhs() == node && nodes.contains(pmr.getOriginalLhs())))) {
				if (pmr.getMaster() == node) {
					outboundEdges.add(pmr);
				}
			}
		}
		return outboundEdges;
	}

	public Collection<SourceTableRecord> getNodes() {
		return nodes;
	}

	public Collection<PotentialMatchRecord> getOutboundEdges(SourceTableRecord node) {
		Set<PotentialMatchRecord> outboundEdges = new HashSet<PotentialMatchRecord>();
		for (PotentialMatchRecord pmr : node.getOriginalMatchEdges()) {
			if ((pmr.getOriginalLhs() == node && nodes.contains(pmr.getOriginalRhs()) 
					|| (pmr.getOriginalRhs() == node && nodes.contains(pmr.getOriginalLhs())))) {
				if (pmr.getMaster() != null && pmr.getMaster() != node) {
					outboundEdges.add(pmr);
				}
			}
		}
		return outboundEdges;
	}
}