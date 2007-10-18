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

package ca.sqlpower.matchmaker;

import java.io.File;
import java.util.Date;

/**
 * Settings specific to the Merge engine
 */
public class MergeSettings extends MatchMakerSettings {
	/**
	 * Backup the data that is going to be merged
	 */
	private boolean backUp;
	
	/**
	 * Augments null (an engine parameter)
	 * TODO figure out what this means
	 */
	private boolean augmentNull;
	
	/**
	 * The file where the engine writes error messages to.
	 * <p>
	 * Note: We cannot persist this value as there is no column
	 * in the PL schema for it. When we've rewritten the engines, we
	 * will probably not even need this parameter, as the error messages
	 * would be able to be written in the normal log file.
	 */
	private File errorLogFile;
	
	/**
	 * The number of records to merge before running a commit.
	 * This parameter only really matters in Oracle, since only
	 * Oracle has trouble with big commits.
	 * <p>
	 * Note: We cannot persist this value as there is no column
	 * in the PL schema for it. Once we've rewritten the engines,
	 * we can change the PL schema to have a column to store it in.
	 */
	private Integer commitFrequency;
	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = super.hashCode();
		result = PRIME * result + (augmentNull ? 1231 : 1237);
		result = PRIME * result + (backUp ? 1231 : 1237);
		result = PRIME * result + ((errorLogFile == null) ? 0 : errorLogFile.hashCode());
		result = PRIME * result + ((commitFrequency == null) ? 0 : commitFrequency.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (!super.equals(obj))
			return false;

		if (getClass() != obj.getClass())
			return false;
		
		final MergeSettings other = (MergeSettings) obj;
		
		if (augmentNull != other.augmentNull)
			return false;
		
		if (backUp != other.backUp)
			return false;
		
		if (errorLogFile == null) {
            if (other.errorLogFile != null) return false;
        } else if (!errorLogFile.equals(other.errorLogFile)) {
            return false;
        }
		
		if (commitFrequency == null) {
            if (other.commitFrequency != null) return false;
        } else if (!commitFrequency.equals(other.commitFrequency)) {
            return false;
        }
		
		return true;
	}

	public boolean getBackUp() {
		return backUp;
	}

	public void setBackUp(boolean backUp) {
		boolean oldValue = this.backUp;
		this.backUp = backUp;
		getEventSupport().firePropertyChange("backUp", oldValue, backUp);
	}

	public boolean getAugmentNull() {
		return augmentNull;
	}

	public void setAugmentNull(boolean augmentNull) {
		boolean oldValue = this.augmentNull;
		this.augmentNull = augmentNull;
		getEventSupport().firePropertyChange("augmentNull", oldValue,
				this.augmentNull);
	}
	
	public Integer getCommitFrequency() {
		return commitFrequency;
	}
	
	public void setCommitFrequency(Integer commitFrequency) {
		Integer oldValue = this.commitFrequency;
		this.commitFrequency = commitFrequency;
		getEventSupport().firePropertyChange("commitFrequency", oldValue, this.commitFrequency);
	}

	public File getErrorLogFile() {
		return errorLogFile;
	}
	
	public void setErrorLogFile(File errorLogFile) {
		File oldValue = this.errorLogFile;
		this.errorLogFile = errorLogFile;
		getEventSupport().firePropertyChange("errorLogFile", oldValue, this.errorLogFile);
	}

	public MergeSettings duplicate(MatchMakerObject parent,MatchMakerSession s) {
		MergeSettings settings = new MergeSettings();
		settings.setAppendToLog(getAppendToLog());
		settings.setAugmentNull(getAugmentNull());
		settings.setBackUp(getBackUp());
		settings.setDebug(getDebug());
		settings.setDescription(getDescription()==null?null:new String(getDescription()));
		settings.setLastRunDate(getLastRunDate()==null?null:new Date(getLastRunDate().getTime()));
		settings.setLog(getLog()==null?null:new File(getLog().getPath()));
		settings.setErrorLogFile(getErrorLogFile()==null?null:new File(getErrorLogFile().getPath()));
		settings.setName(getName()==null?null:new String(getName()));
		settings.setProcessCount(getProcessCount()==null?null:new Integer(getProcessCount()));
		settings.setSendEmail(getSendEmail());
		settings.setSession(s);
		settings.setShowProgressFreq(getShowProgressFreq()==null?null:new Long(getShowProgressFreq()));
		settings.setCommitFrequency(getCommitFrequency()==null?null:new Integer(getCommitFrequency()));
		settings.setVisible(isVisible());
		
		return settings;
	}

	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
        buf.append("MergeSettings [");
        buf.append("augmentNull->" + augmentNull + ", ");
        buf.append("backUp->" + backUp + ", ");
        buf.append(super.toString());
        buf.append("]");
        return buf.toString();
	}
}