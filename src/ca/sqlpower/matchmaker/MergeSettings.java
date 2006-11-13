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

	public boolean isBackUp() {
		return backUp;
	}

	public void setBackUp(boolean backUp) {
		boolean oldValue = this.backUp;
		this.backUp = backUp;
		getEventSupport().firePropertyChange("backUp", oldValue, backUp);
	}
}
