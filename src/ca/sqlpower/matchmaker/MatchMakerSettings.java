/**
 *
 */
package ca.sqlpower.matchmaker;

import java.util.Date;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.util.log.Log;

/**
 * The Match (Object) settings that are common to both the match and the merge
 * engines
 *
 */
public abstract class MatchMakerSettings extends
		AbstractMatchMakerObject<MatchMakerSettings, MatchMakerObject> {

    private static final Logger logger = Logger.getLogger(MatchMakerSettings.class);
    
	@Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 0;
        result = PRIME * result + ((appendToLog == null) ? 0 : appendToLog.hashCode());
        result = PRIME * result + ((debug == null) ? 0 : debug.hashCode());
        result = PRIME * result + ((description == null) ? 0 : description.hashCode());
        result = PRIME * result + ((lastRunDate == null) ? 0 : lastRunDate.hashCode());
        result = PRIME * result + ((log == null) ? 0 : log.hashCode());
        result = PRIME * result + ((processCount == null) ? 0 : processCount.hashCode());
        result = PRIME * result + ((rollbackSegmentName == null) ? 0 : rollbackSegmentName.hashCode());
        result = PRIME * result + ((sendEmail == null) ? 0 : sendEmail.hashCode());
        result = PRIME * result + ((showProgressFreq == null) ? 0 : showProgressFreq.hashCode());
        return result;
    }

	@Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MatchMakerSettings)){
            return false;
        }
        if (this == obj) {
            return true;
        }
        final MatchMakerSettings other = (MatchMakerSettings) obj;
        
        logger.debug("MMSettings.equals(): this date="+this.getLastRunDate()+"; other date="+other.getLastRunDate()+
                "\n this="+this+
                "\nother="+other);
        
        logger.debug("comparing appendToLog");
        if (appendToLog == null) {
            if (other.appendToLog != null) return false;
        } else if (!appendToLog.equals(other.appendToLog)) {
            return false;
        }
        
        logger.debug("comparing debug");
        if (debug == null) {
            if (other.debug != null) return false;
        } else if (!debug.equals(other.debug)) {
            return false;
        }
        logger.debug("comparing description");
        if (description == null) {
            if (other.description != null) return false;
        } else if (!description.equals(other.description)) {
            return false;
        }
        
        logger.debug("Ok, actually comparing lastRunDate now");
        if (lastRunDate == null) {
            if (other.lastRunDate != null) return false;
        } else if (!lastRunDate.equals(other.lastRunDate)) {
            return false;
        }
        
        if (log == null) {
            if (other.log != null) return false;
        } else if (!log.equals(other.log)) {
            return false;
        }
        
        if (processCount == null) {
            if (other.processCount != null) return false;
        } else if (!processCount.equals(other.processCount)) {
            return false;
        }
        
        if (rollbackSegmentName == null) {
            if (other.rollbackSegmentName != null) return false;
        } else if (!rollbackSegmentName.equals(other.rollbackSegmentName)) {
            return false;
        }
        
        if (sendEmail == null) {
            if (other.sendEmail != null) return false;
        } else if (!sendEmail.equals(other.sendEmail)) {
            return false;
        }
        
        if (showProgressFreq == null) {
            if (other.showProgressFreq != null) return false;
        } else if (!showProgressFreq.equals(other.showProgressFreq)) {
            return false;
        }
        
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
	
	public Boolean getAppendToLog() {
		return appendToLog;
	}

	public void setAppendToLog(Boolean appendToLog) {
		Boolean oldValue = this.appendToLog;
		this.appendToLog = appendToLog;
		getEventSupport().firePropertyChange("appendToLog", oldValue,
				appendToLog);
	}

	public Boolean getDebug() {
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

	public Boolean getSendEmail() {
		return sendEmail;
	}

	public void setSendEmail(Boolean sendEMail) {
		Boolean oldValue = this.sendEmail;
		this.sendEmail = sendEMail;
		getEventSupport().firePropertyChange("sendEmail", oldValue, sendEMail);
	}

	public Long getShowProgressFreq() {
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

    /**
     * Stores a defensive copy of the given date.
     * 
     * @param lastRunDate The last time the match or merge was run, it can be null.
     */
	public void setLastRunDate(Date lastRunDate) {
		Date oldValue = this.lastRunDate;
		this.lastRunDate = lastRunDate == null ? null : new Date(lastRunDate.getTime());
		getEventSupport().firePropertyChange("lastRunDate", oldValue, this.lastRunDate);
	}
    
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("MatchMakerSetting [");
        buf.append("appendToLog->"+appendToLog+", ");
        buf.append("debug->"+debug+", ");
        buf.append("description->"+description+", ");
        buf.append("lastRunDate->"+lastRunDate +", ");
        buf.append("log->"+log+", ");
        buf.append("processCount->"+processCount+", ");
        buf.append("rollbackSegmentName->"+rollbackSegmentName+", ");
        buf.append("sendEmail->"+sendEmail+", ");
        buf.append("showProgressFreq->"+showProgressFreq +"]");
        return buf.toString();
    }

}