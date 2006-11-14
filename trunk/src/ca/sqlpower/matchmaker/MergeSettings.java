package ca.sqlpower.matchmaker;

/**
 * Settings specific to the merge engine
 *
 */
public class MergeSettings extends MatchMakerSettings<MergeSettings> {

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
	Boolean backUp;
	/**
	 * Augments null (an engine parameter)
	 * TODO figure out what this means
	 */
	Boolean augmentNull;

	public Boolean isBackUp() {
		return backUp;
	}

	public void setBackUp(Boolean backUp) {
		Boolean oldValue = this.backUp;
		this.backUp = backUp;
		getEventSupport().firePropertyChange("backUp", oldValue, backUp);
	}

	public Boolean isAugmentNull() {
		return augmentNull;
	}

	public void setAugmentNull(Boolean augmentNull) {
		Boolean oldValue = this.augmentNull;
		this.augmentNull = augmentNull;
		getEventSupport().firePropertyChange("augmentNull", oldValue,
				this.augmentNull);
	}
}