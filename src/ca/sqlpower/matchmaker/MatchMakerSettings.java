/**
 *
 */
package ca.sqlpower.matchmaker;

import java.util.Date;

import ca.sqlpower.matchmaker.util.log.Log;

/**
 * The Match (Object) settings that are common to both the match and the merge
 * engines
 *
 */
public abstract class MatchMakerSettings<T extends MatchMakerSettings> extends
		AbstractMatchMakerObject<T, MatchMakerObject> {

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 0;
		result = PRIME * result + (appendToLog ? 1231 : 1237);
		result = PRIME * result + (debug ? 1231 : 1237);
		result = PRIME * result + ((description == null) ? 0 : description.hashCode());
		result = PRIME * result + ((lastRunDate == null) ? 0 : lastRunDate.hashCode());
		result = PRIME * result + ((log == null) ? 0 : log.hashCode());
		result = PRIME * result + processCount;
		result = PRIME * result + ((rollbackSegmentName == null) ? 0 : rollbackSegmentName.hashCode());
		result = PRIME * result + (sendEmail ? 1231 : 1237);
		result = PRIME * result + (showProgressFreq.intValue());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		final MatchMakerSettings other = (MatchMakerSettings) obj;
		if (appendToLog != other.appendToLog)
			return false;
		if (debug != other.debug)
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (lastRunDate == null) {
			if (other.lastRunDate != null)
				return false;
		} else if (!lastRunDate.equals(other.lastRunDate))
			return false;
		if (log == null) {
			if (other.log != null)
				return false;
		} else if (!log.equals(other.log))
			return false;
		if (processCount != other.processCount)
			return false;
		if (rollbackSegmentName == null) {
			if (other.rollbackSegmentName != null)
				return false;
		} else if (!rollbackSegmentName.equals(other.rollbackSegmentName))
			return false;
		if (sendEmail != other.sendEmail)
			return false;
		if (showProgressFreq != other.showProgressFreq)
			return false;
		return true;
	}

	public MatchMakerSettings( ) {
	}

	/**
	 * Enable the debug mode of the engine
	 */
	private Boolean debug;

	/** specify append rather than overwrite */
	private Boolean appendToLog;

	/** Log for the engine using this setting */
	private Log log;

	/** Send an email when the job is done */
	private Boolean sendEmail;

	/** show the progress every so often */
	private Long showProgressFreq;

	/**
	 * rollback segment name
	 */
	private String rollbackSegmentName;
	/**
	 * Describe the process
	 */
	private String description;
	/**
	 * if showProgressFreq is true, process processCount records before a
	 * progress message is printed
	 */
	private Integer processCount;

	/**
	 * The last time these settings were used in a run
	 */
	private Date lastRunDate;
	
	public Boolean isAppendToLog() {
		return appendToLog;
	}

	public void setAppendToLog(Boolean appendToLog) {
		Boolean oldValue = this.appendToLog;
		this.appendToLog = appendToLog;
		getEventSupport().firePropertyChange("appendToLog", oldValue,
				appendToLog);
	}

	public Boolean isDebug() {
		return debug;
	}

	public void setDebug(Boolean debug) {
		Boolean oldValue = this.debug;
		this.debug = debug;
		getEventSupport().firePropertyChange("debug", oldValue, debug);
	}

	public Integer getProcessCount() {
		return processCount;
	}

	public void setProcessCount(Integer processCount) {
		Integer oldValue = this.processCount;
		this.processCount = processCount;
		getEventSupport().firePropertyChange("processCount", oldValue,
				processCount);
	}

	public Boolean isSendEmail() {
		return sendEmail;
	}

	public void setSendEmail(Boolean sendEMail) {
		Boolean oldValue = this.sendEmail;
		this.sendEmail = sendEMail;
		getEventSupport().firePropertyChange("sendEmail", oldValue, sendEMail);
	}

	public Long isShowProgressFreq() {
		return showProgressFreq;
	}

	public void setShowProgressFreq(Long showProgressFreq) {
		Long oldValue = this.showProgressFreq;
		this.showProgressFreq = showProgressFreq;
		getEventSupport().firePropertyChange("showProgressFreq", oldValue,
				showProgressFreq);
	}

	public Log getLog() {
		return log;
	}

	public void setLog(Log log) {
		Log oldValue = this.log;
		this.log = log;
		getEventSupport().firePropertyChange("log", oldValue, this.log);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		String oldValue = this.description;
		this.description = description;
		getEventSupport().firePropertyChange("description", oldValue, this.description);
	}

	public String getRollbackSegmentName() {
		return rollbackSegmentName;
	}

	public void setRollbackSegmentName(String rollbackSegmentName) {
		String oldValue = this.rollbackSegmentName;
		this.rollbackSegmentName = rollbackSegmentName;
		getEventSupport().firePropertyChange("rollbackSegmentName", oldValue, this.rollbackSegmentName);
	}

	public Date getLastRunDate() {
		return lastRunDate;
	}

	public void setLastRunDate(Date lastRunDate) {
		Date oldValue = this.lastRunDate;
		this.lastRunDate = lastRunDate;
		getEventSupport().firePropertyChange("lastRunDate", oldValue, this.lastRunDate);
	}

}