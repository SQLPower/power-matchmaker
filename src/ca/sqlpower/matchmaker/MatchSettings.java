package ca.sqlpower.matchmaker;

/**
 * Settings that are specific to the Match engine
 */
public class MatchSettings extends MatchMakerSettings<MatchSettings> {

	@Override
    public int hashCode() {
        final int PRIME = 31;
        int result = super.hashCode();
        result = PRIME * result + ((autoMatchThreshold == null) ? 0 : autoMatchThreshold.hashCode());
        result = PRIME * result + ((breakUpMatch == null) ? 0 : breakUpMatch.hashCode());
        result = PRIME * result + ((lastBackupNo == null) ? 0 : lastBackupNo.hashCode());
        result = PRIME * result + ((truncateCandDupe == null) ? 0 : truncateCandDupe.hashCode());
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
        if (autoMatchThreshold == null) {
            if (other.autoMatchThreshold != null)
                return false;
        } else if (!autoMatchThreshold.equals(other.autoMatchThreshold))
            return false;
        if (breakUpMatch == null) {
            if (other.breakUpMatch != null)
                return false;
        } else if (!breakUpMatch.equals(other.breakUpMatch))
            return false;
        if (lastBackupNo == null) {
            if (other.lastBackupNo != null)
                return false;
        } else if (!lastBackupNo.equals(other.lastBackupNo))
            return false;
        if (truncateCandDupe == null) {
            if (other.truncateCandDupe != null)
                return false;
        } else if (!truncateCandDupe.equals(other.truncateCandDupe))
            return false;
        return true;
    }

	public MatchSettings( ) {
	}

	/**
	 * The threshold above which matches are automatically resolved
	 */
	private Short autoMatchThreshold;
	
	/**
	 * The number of the last backup
	 */
	private Long lastBackupNo;
	
	/**
	 * Breakup the match after each criteria group
	 */
	Boolean breakUpMatch;
	/**
	 * Truncate the candidate duplicate table
	 */
	Boolean truncateCandDupe;

	public Boolean isBreakUpMatch() {
		return breakUpMatch;
	}

	public void setBreakUpMatch(Boolean breakUpMatch) {
		Boolean oldValue = this.breakUpMatch;
		this.breakUpMatch = breakUpMatch;
		getEventSupport().firePropertyChange("breakUpMatch", oldValue,
				this.breakUpMatch);
	}

	public Boolean isTruncateCandDupe() {
		return truncateCandDupe;
	}

	public void setTruncateCandDupe(Boolean truncateCandDupe) {
		Boolean oldValue = this.truncateCandDupe;
		this.truncateCandDupe = truncateCandDupe;
		getEventSupport().firePropertyChange("truncateCandDupe", oldValue,
				this.truncateCandDupe);
	}

	public Short getAutoMatchThreshold() {
		return autoMatchThreshold;
	}

	public void setAutoMatchThreshold(Short autoMatchThreshold) {
		Short oldValue = this.autoMatchThreshold;
		this.autoMatchThreshold = autoMatchThreshold;
		getEventSupport().firePropertyChange("autoMatchThreshold", oldValue,
				this.autoMatchThreshold);
	}

	public Long getLastBackupNo() {
		return lastBackupNo;
	}

	public void setLastBackupNo(Long lastBackupNo) {
		Long oldValue = this.lastBackupNo;
		this.lastBackupNo = lastBackupNo;
		getEventSupport().firePropertyChange("lastBackupNo", oldValue,
				this.lastBackupNo);
	}
}