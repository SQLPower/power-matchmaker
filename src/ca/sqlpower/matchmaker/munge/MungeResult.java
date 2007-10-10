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

package ca.sqlpower.matchmaker.munge;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.SourceTableRecord;

/**
 * A representation of a complete munged data row. It contains the munged
 * data that has gone through a MungeProcessor, and a SourceTableRecord
 * representing the source data record that was being munged.
 */
public class MungeResult implements Comparable<MungeResult> {
	
	private static final Logger logger = Logger.getLogger(MungeResult.class);
	
	/**
	 * The data that went through the MungeProcessor
	 */
	private MungeStepOutput[] mungedData;
	
	/**
	 * The SourceTableRecord for the munged row. 
	 */
	private SourceTableRecord sourceTableRecord;
	
	public MungeStepOutput[] getMungedData() {
		return mungedData;
	}
	
	public void setMungedData(MungeStepOutput[] mungedData) {
		this.mungedData = mungedData;
	}
	
	public SourceTableRecord getSourceTableRecord() {
		return sourceTableRecord;
	}
	
	public void setSourceTableRecord(SourceTableRecord source) {
		this.sourceTableRecord = source;
	}

	public int compareTo(MungeResult o) {
		if (mungedData.length != o.getMungedData().length) {
			throw new IllegalStateException("MungeResult's munged data should have the same length");
		} 
		
		for (int i = 0; i < mungedData.length; i++) {
			int compareValue = mungedData[i].compareTo(o.getMungedData()[i]);
			if (compareValue != 0) {
				logger.debug("MungeResults are NOT equal, so return " + compareValue);
				return compareValue;
			}
		}
		logger.debug("MungeResults are equal, so return 0");
		return 0;
	}
	
	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		
		s.append("Key Values:");
		for (Object o: sourceTableRecord.getKeyValues()) {
			s.append(" ").append(o).append(" ");
		}
		
		s.append("Data Values:");
		for (MungeStepOutput o: mungedData) {
			s.append(" ").append(o.getData()).append(" ");
		}

		return s.toString();
	}
}
