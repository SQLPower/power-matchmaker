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


package ca.sqlpower.matchmaker.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchPool;
import ca.sqlpower.matchmaker.PotentialMatchRecord;
import ca.sqlpower.matchmaker.SourceTableRecord;
/**
 * A graph model which sits on top of a match pool and considers nodes to
 * be the pool's set of SourceTableRecords, and its edges are the user-validated
 * Master and Duplicate sides of the pool's PotentialMatchRecord set.  Hence,
 * if you take a pristine match pool from a fresh engine run, this graph model
 * will say there are no edges.  Once the user starts fiddling around and assigning
 * master records, this graph model will start having some connectedness.
 * <p>
 * Note, although master/duplicate relationships are inherently directional,
 * this particular model treats them as non-directed.  This is a requirement of
 * the algorithm in {@link SourceTableRecord#makeMaster()}, so don't go changing
 * it if you need directionality.  Make another model instead, or introduce a
 * parameter to this one (and rename the class).
 * 
 * <h3>Bug</h3>
 * The class name is not as long as <i>Pneumonoultramicroscopicsilicovolcanoconiosis</i>.
 */
public class NonDirectedUserValidatedMatchPoolGraphModel implements
        GraphModel<SourceTableRecord, PotentialMatchRecord> {

    private static final Logger logger = Logger
            .getLogger(NonDirectedUserValidatedMatchPoolGraphModel.class);
    
    private final MatchPool pool;
    
    private Set<PotentialMatchRecord> additionalPMRs;
    
    public NonDirectedUserValidatedMatchPoolGraphModel(MatchPool pool, Set<PotentialMatchRecord> additionalPMRs ) {
        this.pool = pool;
        this.additionalPMRs = additionalPMRs;
    }
    
    /**
     * Compiles and returns a list of SourceTableRecord where the PotentialMatchRecord
     * connecting the two nodes has a decided master SourceTableRecord 
     */
    public Collection<SourceTableRecord> getAdjacentNodes(SourceTableRecord node) {
    	Collection<SourceTableRecord> adjacentNodes = new ArrayList<SourceTableRecord>();
    	logger.debug("The number of edges for this node is " + getEdges().size());
    	for (PotentialMatchRecord pmr : getEdges()){
    		if (pmr.getMaster() == node){
    			adjacentNodes.add(pmr.getDuplicate());
    		} else if (pmr.getDuplicate() == node){
    			adjacentNodes.add(pmr.getMaster());
    		} else if (additionalPMRs.contains(pmr) && pmr.getOriginalLhs() == node) {
    			adjacentNodes.add(pmr.getOriginalRhs());
    		} else if (additionalPMRs.contains(pmr) && pmr.getOriginalRhs() == node){
    			adjacentNodes.add(pmr.getOriginalLhs());
    		} else {
    			// edge belongs to some other nodes in the graph
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
