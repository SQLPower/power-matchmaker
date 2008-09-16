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

package ca.sqlpower.matchmaker;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.graph.BreadthFirstSearch;
import ca.sqlpower.graph.GraphModel;
import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;
import ca.sqlpower.matchmaker.graph.NonDirectedUserValidatedMatchPoolGraphModel;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.util.MonitorableImpl;

public class AutoMatcher extends MonitorableImpl {

	private static final Logger logger = Logger.getLogger(AutoMatcher.class);
	
	private MatchPool pool;

	private Set<SourceTableRecord> visited;
	
	public AutoMatcher(MatchPool pool) {
		this.pool = pool;
	}
	
	public void doAutoMatch(MungeProcess mungeProcess, Aborter aborter) throws SQLException, ArchitectException {
		try {
			setStarted(true);
			setFinished(false);
			if (mungeProcess == null) {
				throw new IllegalArgumentException("Auto-Match invoked with an " +
						"invalid munge process");
			}
			
			Collection<SourceTableRecord> records = pool.getSourceTableRecords();
			
			logger.debug("Auto-Matching with " + records.size() + " records.");
			
			visited = new HashSet<SourceTableRecord>();
			SourceTableRecord selected = null;
			for (SourceTableRecord record : records) {
				boolean addToVisited = true;
				for (PotentialMatchRecord pmr : record.getOriginalMatchEdges()) {
					if (pmr.getMatchStatus() != MatchType.NOMATCH
							&& pmr.getMungeProcess() == mungeProcess) {
						addToVisited = false;
					}
				}
				if (addToVisited) {
					visited.add(record);
				} else {
					// rather than iterating through all the records again, looking
					// for one that isn't in visited...
					selected = record;
				}
			}
			
			logger.debug("The size of visited is " + visited.size());

			Set<SourceTableRecord> neighbours = findAutoMatchNeighbours(mungeProcess, selected, visited);
			makeAutoMatches(mungeProcess, selected, neighbours, visited);
			//If we haven't visited all the nodes, we are not done!
			while (visited.size() != records.size()) {
				aborter.checkCancelled();
				SourceTableRecord temp = null;
				for (SourceTableRecord record : records) {
					if (!visited.contains(record)) {
						temp = record;
						break;
					}
				}
				neighbours = findAutoMatchNeighbours(mungeProcess, temp, visited);
				makeAutoMatches(mungeProcess, temp, neighbours, visited);
			}
		} finally {
			setFinished(true);
		}
	}
	
	/**
	 * Creates the matches necessary in an auto-match while maintaining the
	 * 'visited' set and propagating the algorithm to neighbours of selected
	 * nodes.
	 */
	private void makeAutoMatches(MungeProcess mungeProcess,
			SourceTableRecord selected,
			Set<SourceTableRecord> neighbours,
			Set<SourceTableRecord> visited) throws SQLException, ArchitectException {
		logger.debug("makeAutoMatches called, selected's key values = " + selected.getKeyValues());
		visited.add(selected);
		GraphModel<SourceTableRecord, PotentialMatchRecord> nonDirectedGraph =
			new NonDirectedUserValidatedMatchPoolGraphModel(pool, new HashSet<PotentialMatchRecord>());
		BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord> bfs =
			new BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord>();
		Set<SourceTableRecord> reachable = new HashSet<SourceTableRecord>(bfs.performSearch(nonDirectedGraph, selected));
		Set<SourceTableRecord> noMatchNodes = pool.findNoMatchNodes(reachable);
		for (SourceTableRecord record : neighbours) {
			if (!noMatchNodes.contains(record)) {
				pool.defineMaster(selected, record, true);
				nonDirectedGraph = new NonDirectedUserValidatedMatchPoolGraphModel(pool, new HashSet<PotentialMatchRecord>());
				bfs = new BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord>();
				reachable = new HashSet<SourceTableRecord>(bfs.performSearch(nonDirectedGraph, selected));
				noMatchNodes = pool.findNoMatchNodes(reachable);
			}
		}
		for (SourceTableRecord record : neighbours) {
			if (!visited.contains(record)) {
				makeAutoMatches(mungeProcess, record, findAutoMatchNeighbours(mungeProcess, record, visited), visited);
			}
		}
	}

	/**
	 * Finds all the neighbours that auto-match worries about as explained in
	 * the comment for doAutoMatch in the context that 'record' is selected in
	 * step 3
	 */
	private Set<SourceTableRecord> findAutoMatchNeighbours(MungeProcess mungeProcess,
			SourceTableRecord record,
			Set<SourceTableRecord> visited) {
		logger.debug("The size of visited is " + visited.size());
		Set<SourceTableRecord> ret = new HashSet<SourceTableRecord>();
		for (PotentialMatchRecord pmr : record.getOriginalMatchEdges()) {
			if (pmr.getMungeProcess() == mungeProcess 
					&& pmr.getMatchStatus() != MatchType.NOMATCH) {
				if (record == pmr.getOriginalLhs() && !visited.contains(pmr.getOriginalRhs())) {
					ret.add(pmr.getOriginalRhs());
				} else if (record == pmr.getOriginalRhs() && !visited.contains(pmr.getOriginalLhs())) {
					ret.add(pmr.getOriginalLhs());
				}
			}
		}
		logger.debug("findAutoMatchNeighbours: The neighbours to automatch for " + record + " are " + ret);
		return ret;
	}
	
	public Integer getJobSize() {
		return pool.getSourceTableRecords().size();
	}

	public String getMessage() {
		return null;
	}

	public int getProgress() {
		return visited.size();
	}
}
