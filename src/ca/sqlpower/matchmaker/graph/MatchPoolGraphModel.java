package ca.sqlpower.matchmaker.graph;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchPool;
import ca.sqlpower.matchmaker.PotentialMatchRecord;
import ca.sqlpower.matchmaker.SourceTableRecord;

/**
 * Models a match pool as a non-directed graph, where the nodes are the source table records
 * of the match pool, and the edges are the original match relationships between those
 * records, as decided by the match engine.
 */
public class MatchPoolGraphModel implements GraphModel<SourceTableRecord, PotentialMatchRecord> {

    private static final Logger logger = Logger.getLogger(MatchPoolGraphModel.class);
    private final MatchPool pool;
    
    public MatchPoolGraphModel(MatchPool pool) {
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
    
}
