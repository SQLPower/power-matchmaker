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

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ca.sqlpower.object.SPObject;

/**
 * Settings specific to the Merge engine
 */
public class MergeSettings extends MatchMakerSettings {
	
	public static final List<Class<? extends SPObject>> allowedChildTypes =
        Collections.emptyList();
	/**
	 * Backup the data that is going to be merged
	 */
	private boolean backUp;
	
	/**
	 * When true, the merge engine for this project will attempt to augment nulls
     * in the master record with values from the duplicate records.  Otherwise, the
     * master is left alone.
	 */
	private boolean augmentNull;
		
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

	public boolean getBackUp() {
		return backUp;
	}

	public void setBackUp(boolean backUp) {
		boolean oldValue = this.backUp;
		this.backUp = backUp;
		firePropertyChange("backUp", oldValue, backUp);
	}

	public boolean getAugmentNull() {
		return augmentNull;
	}

	public void setAugmentNull(boolean augmentNull) {
		boolean oldValue = this.augmentNull;
		this.augmentNull = augmentNull;
		firePropertyChange("augmentNull", oldValue,
				this.augmentNull);
	}
	
	public MergeSettings duplicate(MatchMakerObject parent) {
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
		settings.setSendEmail(getSendEmail());
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

	@Override
	public List<? extends SPObject> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public List<Class<? extends SPObject>> getAllowedChildTypes() {
		return allowedChildTypes;
	}
}