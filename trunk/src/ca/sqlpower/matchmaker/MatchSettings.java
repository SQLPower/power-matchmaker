package ca.sqlpower.matchmaker;

/**
 * Settings that are specific to the Match engine
 */
public class MatchSettings extends MatchMakerSettings {

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = super.hashCode();
		result = PRIME * result + (breakUpMatch ? 1231 : 1237);
		result = PRIME * result + (truncateCandDupe ? 1231 : 1237);
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
		final MatchSettings other = (MatchSettings) obj;
		if (breakUpMatch != other.breakUpMatch)
			return false;
		if (truncateCandDupe != other.truncateCandDupe)
			return false;
		return true;
	}

	public MatchSettings( ) {
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
