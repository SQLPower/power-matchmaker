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
 * Settings specific to the merge engine
 *
 */
public class MergeSettings extends MatchMakerSettings {

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = super.hashCode();
		result = PRIME * result + (augmentNull ? 1231 : 1237);
		result = PRIME * result + (backUp ? 1231 : 1237);
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
		return true;
	}

	public MergeSettings( ) {
	}

	/**
	 * Backup the data that is going to be merged
	 */
	boolean backUp;
	/**
	 * Augments null (an engine parameter)
	 * TODO figure out what this means
	 */
	boolean augmentNull;

	/* Parameters the engine says it accepts
	match id
	username
	password
	debug
	commit freq
	log file
	error file
	process count
	user prompt
	show progress
	rollback segment name
	append to log ind
	send email
	*/
	
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

	public MergeSettings duplicate(MatchMakerObject parent,MatchMakerSession s) {
		MergeSettings settings = new MergeSettings();
		settings.setAppendToLog(getAppendToLog());
		settings.setAugmentNull(getAugmentNull());
		settings.setBackUp(getBackUp());
		settings.setDebug(getDebug());
		settings.setDescription(getDescription()==null?null:new String(getDescription()));
		settings.setLastRunDate(getLastRunDate()==null?null:new Date(getLastRunDate().getTime()));
		settings.setLog(getLog()==null?null:new File(getLog().getPath()));
		settings.setName(getName()==null?null:new String(getName()));
		settings.setProcessCount(getProcessCount()==null?null:new Integer(getProcessCount()));
		settings.setRollbackSegmentName(getRollbackSegmentName()==null?null:new String(getRollbackSegmentName()));
		settings.setSendEmail(getSendEmail());
		settings.setSession(s);
		settings.setShowProgressFreq(getShowProgressFreq()==null?null:new Long(getShowProgressFreq()));
		
		return settings;
	}
}