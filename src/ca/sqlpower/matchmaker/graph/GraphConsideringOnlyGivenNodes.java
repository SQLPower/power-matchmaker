package ca.sqlpower.matchmaker.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

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

	Logger logger = Logger.getLogger(GraphConsideringOnlyGivenNodes.class);
	
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
			} else if (pmr.getOriginalRhs() == node) {
				if (nodes.contains(pmr.getOriginalLhs())) {
					adjacentNodes.add(pmr.getOriginalLhs());
				}
			}
		}
		logger.debug("Adjacent nodes to " + node + " are " + adjacentNodes);
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
			if ((pmr.getOriginalLhs() == node && nodes.contains(pmr.getOriginalRhs()) || (pmr.getOriginalRhs() == node && nodes.contains(pmr.getOriginalLhs())))) {
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
			if ((pmr.getOriginalLhs() == node && nodes.contains(pmr.getOriginalRhs()) || (pmr.getOriginalRhs() == node && nodes.contains(pmr.getOriginalLhs())))) {
				if (pmr.getMaster() != null && pmr.getMaster() != node) {
					outboundEdges.add(pmr);
				}
			}
		}
		return outboundEdges;
	}
}