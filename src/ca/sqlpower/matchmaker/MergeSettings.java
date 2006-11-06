package ca.sqlpower.matchmaker;

/**
 *	Settings specific to the merge engine
 *
 */
public class MergeSettings extends MatchMakerSettings {
	public MergeSettings(String appUserName) {
		super(appUserName);
	}

	/**
	 * Backup the data that is going to be merged
	 */
	boolean backUp;

	public boolean isBackUp() {
		return backUp;
	}

	public void setBackUp(boolean backUp) {
		if (this.backUp != backUp) {
			boolean oldValue = this.backUp;
			this.backUp = backUp;
			getEventSupport().firePropertyChange("backUp", oldValue, backUp);
		}
	}
}
