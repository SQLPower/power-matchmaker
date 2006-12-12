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

	public MergeSettings duplicate() {
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
		settings.setSession(getSession());
		settings.setShowProgressFreq(getShowProgressFreq()==null?null:new Long(getShowProgressFreq()));
		
		return settings;
	}
}