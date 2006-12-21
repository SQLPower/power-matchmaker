package ca.sqlpower.matchmaker.graph;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchPool;
import ca.sqlpower.matchmaker.PotentialMatchRecord;
import ca.sqlpower.matchmaker.SourceTableRecord;

public class NonDirectedUserValidatedMatchPoolGraphModel implements
        GraphModel<SourceTableRecord, PotentialMatchRecord> {

    private static final Logger logger = Logger
            .getLogger(NonDirectedUserValidatedMatchPoolGraphModel.class);
    
    private final MatchPool pool;
    
    public NonDirectedUserValidatedMatchPoolGraphModel(MatchPool pool) {
        this.pool = pool;
    }
    
    /**
     * Compiles and returns a list of SourceTableRecord where the PotentialMatchRecord
     * connecting the two nodes has a decided master SourceTableRecord 
     */
    public Collection<SourceTableRecord> getAdjacentNodes(SourceTableRecord node) {
        Collection<SourceTableRecord> adjacentNodes = new ArrayList<SourceTableRecord>();
        for (PotentialMatchRecord pmr : getEdges()){
            if (pmr.getMaster() == node){
                adjacentNodes.add(pmr.getDuplicate());
            } else if (pmr.getDuplicate() == node){
                adjacentNodes.add(pmr.getMaster());
            } else {
                throw new IllegalStateException("The edge in an illegal state, it is not connected to the " +
                        "appropiate node");
            }
        }
        
        return adjacentNodes;
    }

    /**
     * Returns a list of PotentialMatchRecord where the master has been decided
     */
    public Collection<PotentialMatchRecord> getEdges() {
        Collection<PotentialMatchRecord> edges = new ArrayList<PotentialMatchRecord>();
        for (PotentialMatchRecord record : pool.getPotentialMatches()){
            if (!record.isMasterUndecided()){
                edges.add(record);
            }
        }
        return edges;
    }

    
    /**
     * Returns a list of PotentialMatchRecord in which the node is identified
     * as the master.
     * @return list of incoming PotentialMatchRecord of the node
     */
    public Collection<PotentialMatchRecord> getInboundEdges(
            SourceTableRecord node) {
        Collection<PotentialMatchRecord> outboundEdges  = new ArrayList<PotentialMatchRecord>();
        for (PotentialMatchRecord pmr : node.getOriginalMatchEdges()){
            if (pmr.getMaster() == node){
                outboundEdges.add(pmr);
            }
        }
        return outboundEdges;
    }

    public Collection<SourceTableRecord> getNodes() {
        return pool.getSourceTableRecords();
    }

    /**
     * Returns a list of PotentialMatchRecord in which the node is identified
     * as the duplicate.
     * @return list of out going PotentialMatchRecord of the node
     */
    public Collection<PotentialMatchRecord> getOutboundEdges(
            SourceTableRecord node) {
        Collection<PotentialMatchRecord> outboundEdges  = new ArrayList<PotentialMatchRecord>();
        for (PotentialMatchRecord pmr : node.getOriginalMatchEdges()){
            if (pmr.getDuplicate() == node){
                outboundEdges.add(pmr);
            }
        }
        return outboundEdges;
    }

}
