/**
 * 
 */
package ca.sqlpower.matchmaker;

public abstract class MatchMakerSettings extends AbstractMatchMakerObject<MatchMakerObject> {
	
	public MatchMakerSettings(String appUserName) {
		super(appUserName);		
	}

	private boolean debug;
	private boolean appendToLog;
	// FIXME add log
	private int processCount;
	private boolean sendEmail;
	private boolean showProgressFreq;
	
	
	
	public boolean isAppendToLog() {
		return appendToLog;
	}
	public void setAppendToLog(boolean appendToLog) {
		if (this.appendToLog != appendToLog) {
			boolean oldValue = this.appendToLog;
			this.appendToLog = appendToLog;
			getEventSupport().firePropertyChange("appendToLog", oldValue, appendToLog);
		}
	}
	public boolean isDebug() {
		return debug;
	}
	public void setDebug(boolean debug) {
		if (this.debug != debug) {
			boolean oldValue = this.debug;
			this.debug = debug;
			getEventSupport().firePropertyChange("debug", oldValue, debug);
		}
	}
	public int getProcessCount() {
		return processCount;
	}
	public void setProcessCount(int processCount) {
		if (this.processCount != processCount) {
			int oldValue = this.processCount;
			this.processCount = processCount;
			getEventSupport().firePropertyChange("processCount", oldValue, processCount);
		}
	}
	public boolean isSendEmail() {
		return sendEmail;
	}
	public void setSendEmail(boolean sendEMail) {
		if (this.sendEmail != sendEMail) {
			boolean oldValue = this.sendEmail;
			this.sendEmail = sendEMail;
			getEventSupport().firePropertyChange("sendEmail", oldValue, sendEMail);
		}
	}
	public boolean isShowProgressFreq() {
		return showProgressFreq;
	}
	public void setShowProgressFreq(boolean showProgressFreq) {
		if (this.showProgressFreq != showProgressFreq) {
			boolean oldValue = this.showProgressFreq;
			this.showProgressFreq = showProgressFreq;
			getEventSupport().firePropertyChange("showProgressFreq", oldValue, showProgressFreq);
		}
	}
	

}