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

package ca.sqlpower.matchmaker;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeResult;

/**
 * A Processor which takes a List of arrays of MungeStepOutputs, which it would
 * typically get from a MungeProcess, and performs matching on the data, and stores
 * the match results into the match repository. This default implementation of matching
 * only considers two records to form a potential match if their munged data is perfectly
 * equal (that is, all of the MungeStepOutputs in the munged data are equal).
 */
public class MatchProcessor extends AbstractProcessor {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MatchProcessor.class);
	
	/**
	 * A list of Munged data that the MatchProcessor would take from a MungeProcessor
	 */
	private List<MungeResult> matchData;
	
	/**
	 * The MungeProces that this MatchProcess is processing
	 */
	private MungeProcess mungeProcess;
	
    private final Logger engineLogger;
	
	/**
	 * The MatchPool that will be used to store the PotentialMatchRecords
	 * and SourceTableRecords that will be marked as the potential matches
	 */
	private MatchPool pool;
	
	public MatchProcessor(MatchPool pool, MungeProcess process,
			List<MungeResult> matchData, Logger logger) {
		this.engineLogger = logger;
		this.matchData = matchData;
		this.pool = pool;
		this.mungeProcess = process; 
		if (mungeProcess.getParent().getMungeSettings().getDebug()) {
			logger.setLevel(Level.DEBUG);
		}
		monitorableHelper.setJobSize(matchData.size());
	}
	
	public Boolean call() throws Exception {
		
		Collections.sort(matchData);
		
		int dataIndex = 0;
		int matchCount = 0;
		
		Set<SourceTableRecord> sourceTableRecords = new HashSet<SourceTableRecord>();
		Set<PotentialMatchRecord> potentialMatchRecords = new HashSet<PotentialMatchRecord>();
		
		for (MungeResult data: matchData) {
            checkCancelled();
			monitorableHelper.incrementProgress();
			dataIndex++;
			for (int i=dataIndex; i<matchData.size(); i++) {
				if (data.compareTo(matchData.get(i)) == 0) {
					boolean nullMatch = false;
					for (int j = 0; j < data.getMungedData().length; j++) {
						if (data.getMungedData()[j] == null && matchData.get(i).getMungedData()[j] == null) {
							engineLogger.debug("Ignoring match on null data");
							nullMatch = true;
							break;
						}
					}
					
					if (!nullMatch) {
						// Potential Match! so store in Match Result Table
						engineLogger.debug("Found Match!\nRecord 1:" + data
								+ "\nRecord 2:" + matchData.get(i));
						matchCount++;
						PotentialMatchRecord pmr = new PotentialMatchRecord(
								mungeProcess, MatchType.UNMATCH, data
										.getSourceTableRecord(), matchData.get(
										i).getSourceTableRecord(), false);
						SourceTableRecord src1 = data.getSourceTableRecord();
						SourceTableRecord src2 = matchData.get(i).getSourceTableRecord();
						sourceTableRecords.add(src1);
						sourceTableRecords.add(src2);
						potentialMatchRecords.add(pmr);
					}
				} else {
					// If data doesn't match perfectly, then there should be 
					// no further matches if list was sorted properly
					break;
				}
			}
		}
		
		engineLogger.debug("Sorting matches into Clusters");
		
		//Sort the new matches into match clusters then add them to the match pool
		List<MatchCluster> matchClusters = MatchPool.sortMatches(sourceTableRecords, potentialMatchRecords);
		
		engineLogger.debug("Merging matches into MatchPool");
		
		pool.mergeInClusters(matchClusters);

		engineLogger.info("Transformation '" + mungeProcess.getName() + "' found " + matchCount + " matches");
		
		return Boolean.TRUE;
	}
}
