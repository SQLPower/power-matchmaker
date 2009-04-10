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


package ca.sqlpower.matchmaker.graph;

import java.util.Collection;
import java.util.HashSet;

import ca.sqlpower.matchmaker.MatchPool;
import ca.sqlpower.matchmaker.PotentialMatchRecord;
import ca.sqlpower.matchmaker.SourceTableRecord;

/**
 * Models a match pool as a directed graph, where the nodes are the source table records
 * of the match pool, and the edges are the original match relationships between those
 * records, as decided by the match engine.
 */
public class MatchPoolDirectedGraphModel extends MatchPoolGraphModel {

    public MatchPoolDirectedGraphModel(MatchPool pool) {
        super(pool);
    }

    /**
     * Returns all potential match records that has the given source record
     * as master.
     */
    public Collection<PotentialMatchRecord> getInboundEdges(SourceTableRecord node) {
    	Collection<PotentialMatchRecord> inboundEdges = new HashSet<PotentialMatchRecord>();
    	for (PotentialMatchRecord pmr : node.getOriginalMatchEdges()) {
    		if (pmr.isMatch() && pmr.getMaster() != null 
    				&& pmr.getMaster().equals(node)) {
    			inboundEdges.add(pmr);
    		}
    	}
    	return inboundEdges;
    }

    /**
     * Returns all potential match records that has the given source record
     * as duplicate.
     */
    public Collection<PotentialMatchRecord> getOutboundEdges(SourceTableRecord node) {
    	Collection<PotentialMatchRecord> outboundEdges = new HashSet<PotentialMatchRecord>();
    	for (PotentialMatchRecord pmr : node.getOriginalMatchEdges()) {
    		if (pmr.isMatch() && pmr.getDuplicate() != null 
    				&& pmr.getDuplicate().equals(node)) {
    			outboundEdges.add(pmr);
    		}
    	}
    	return outboundEdges;
    }
    
    /**
     * Returns the source table record that is the master of
     * the given source record.
     */
    public Collection<SourceTableRecord> getAdjacentNodes(SourceTableRecord node) {
        Collection<SourceTableRecord> adjacentNodes = new HashSet<SourceTableRecord>();
        for (PotentialMatchRecord pmr : getOutboundEdges(node)) {
            adjacentNodes.add(pmr.getMaster());
        }
        return adjacentNodes;
    }
}
