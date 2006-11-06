/**
 * 
 */
package ca.sqlpower.matchmaker;

import ca.sqlpower.matchmaker.util.log.Log;

/**
 * The Match (Object) settings that are common to both the match and the merge
 * engines
 * 
 */
public abstract class MatchMakerSettings extends
		AbstractMatchMakerObject<MatchMakerObject> {

	public MatchMakerSettings(String appUserName) {
		super(appUserName);
	}

	/**
	 * Enable the debug mode of the engine
	 */
	private boolean debug;

	/** specify append rather than overwrite */
	private boolean appendToLog;

	/** Log for the engine using this setting */
	private Log log;

	/** Send an email when the job is done */
	private boolean sendEmail;

	/** show the progress every so often */
	private boolean showProgressFreq;

	/**
	 * if showProgressFreq is true, process processCount records before a
	 * progress message is printed
	 */
	private int processCount;

	public boolean isAppendToLog() {
		return appendToLog;
	}

	public void setAppendToLog(boolean appendToLog) {
		boolean oldValue = this.appendToLog;
		this.appendToLog = appendToLog;
		getEventSupport().firePropertyChange("appendToLog", oldValue,
				appendToLog);
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		boolean oldValue = this.debug;
		this.debug = debug;
		getEventSupport().firePropertyChange("debug", oldValue, debug);
	}

	public int getProcessCount() {
		return processCount;
	}

	public void setProcessCount(int processCount) {
		int oldValue = this.processCount;
		this.processCount = processCount;
		getEventSupport().firePropertyChange("processCount", oldValue,
				processCount);
	}

	public boolean isSendEmail() {
		return sendEmail;
	}

	public void setSendEmail(boolean sendEMail) {
		boolean oldValue = this.sendEmail;
		this.sendEmail = sendEMail;
		getEventSupport().firePropertyChange("sendEmail", oldValue, sendEMail);
	}

	public boolean isShowProgressFreq() {
		return showProgressFreq;
	}

	public void setShowProgressFreq(boolean showProgressFreq) {
		boolean oldValue = this.showProgressFreq;
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
		getEventSupport().firePropertyChange("log", oldValue, log);
	}

}