package ca.sqlpower.matchmaker;

/**
 * Settings that are specific to the Match engine
 */
public class MatchSettings extends MatchMakerSettings {

	public MatchSettings(String appUserName) {
		super(appUserName);
	}

	/**
	 * Breakup the match after each criteria group
	 */
	boolean breakUpMatch;

	public boolean isBreakUpMatch() {
		return breakUpMatch;
	}

	public void setBreakUpMatch(boolean breakUpMatch) {
		boolean oldValue = this.breakUpMatch;
		this.breakUpMatch = breakUpMatch;
		getEventSupport().firePropertyChange("breakUpMatch", oldValue,
				breakUpMatch);
	}
}
