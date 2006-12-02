package ca.sqlpower.matchmaker;

/**
 * Settings that are specific to the Match engine
 */
public class MatchSettings extends MatchMakerSettings {

	@Override
    public int hashCode() {
        final int PRIME = 31;
        int result = super.hashCode();
        result = PRIME * result + ((autoMatchThreshold == null) ? 0 : autoMatchThreshold.hashCode());
        result = PRIME * result + ((breakUpMatch == true) ? 123 : 234);
        result = PRIME * result + ((lastBackupNo == null) ? 0 : lastBackupNo.hashCode());
        result = PRIME * result + ((truncateCandDupe == true) ? 345 : 456);
        return result;
    }

	@Override
    public boolean equals(Object obj) {
        if(!(obj instanceof MatchSettings)){
            return false;
        }
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
        if (breakUpMatch != other.breakUpMatch )  return false;

        if (lastBackupNo == null) {
            if (other.lastBackupNo != null)
                return false;
        } else if (!lastBackupNo.equals(other.lastBackupNo))
            return false;
        if (truncateCandDupe != other.truncateCandDupe ) return false;
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
	private boolean breakUpMatch = false;
	/**
	 * Truncate the candidate duplicate table
	 */
	private boolean truncateCandDupe = false;

	public boolean getBreakUpMatch() {
		return breakUpMatch;
	}

	public void setBreakUpMatch(boolean breakUpMatch) {
		boolean oldValue = this.breakUpMatch;
		this.breakUpMatch = breakUpMatch;
		getEventSupport().firePropertyChange("breakUpMatch", oldValue,
				this.breakUpMatch);
	}

	public boolean getTruncateCandDupe() {
		return truncateCandDupe;
	}

	public void setTruncateCandDupe(boolean truncateCandDupe) {
		boolean oldValue = this.truncateCandDupe;
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
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("MatchSettings [");
        buf.append("autoMatchThreshold->"+autoMatchThreshold+", ");
        buf.append("breakUpMatch->"+breakUpMatch+", ");
        buf.append("lastBackupNo->"+lastBackupNo+", ");
        buf.append("truncateCandDupe->"+truncateCandDupe+", ");
        buf.append(super.toString());
        buf.append("]");
        return buf.toString();
    }
}