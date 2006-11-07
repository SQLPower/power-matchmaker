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
	/**
	 * Truncate the candidate duplicate table
	 */
	boolean truncateCandDupe;

	public boolean isBreakUpMatch() {
		return breakUpMatch;
	}

	public void setBreakUpMatch(boolean breakUpMatch) {
		boolean oldValue = this.breakUpMatch;
		this.breakUpMatch = breakUpMatch;
		getEventSupport().firePropertyChange("breakUpMatch", oldValue,
				breakUpMatch);
	}

	public boolean isTruncateCandDupe() {
		return truncateCandDupe;
	}

	public void setTruncateCandDupe(boolean truncateCandDupe) {
		boolean oldValue = this.truncateCandDupe;
		this.truncateCandDupe = truncateCandDupe;
		getEventSupport().firePropertyChange("truncateCandDupe", oldValue,
				truncateCandDupe);
	}
}
