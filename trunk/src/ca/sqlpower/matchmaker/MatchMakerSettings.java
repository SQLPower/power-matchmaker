/*
 * Copyright (c) 2007, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

/**
 *
 */
package ca.sqlpower.matchmaker;

import java.io.File;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * The Project (Object) settings that are common to both the match and the merge
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
        result = PRIME * result + ((appendToLog == true) ? 42 : 1234);
        result = PRIME * result + ((debug == true) ? 2345 : 3456);
        result = PRIME * result + ((description == null) ? 0 : description.hashCode());
        result = PRIME * result + ((lastRunDate == null) ? 0 : lastRunDate.hashCode());
        result = PRIME * result + ((log == null) ? 0 : log.hashCode());
        result = PRIME * result + ((processCount == null) ? 0 : processCount.hashCode());
        result = PRIME * result + ((sendEmail == true) ? 4567 : 5678);
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
        if (appendToLog != other.getAppendToLog())       return false;

        logger.debug("comparing debug");
        if (debug != other.debug ) return false;

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

        if (sendEmail != other.sendEmail ) return false;

        if (showProgressFreq == null) {
            if (other.showProgressFreq != null) return false;
        } else if (!showProgressFreq.equals(other.showProgressFreq)) {
            return false;
        }

        return true;
    }

	/**
	 * Enable the debug mode of the engine
	 */
	private boolean debug;

	/** specify append rather than overwrite */
	private boolean appendToLog = false;

	/** Log for the engine using this setting */
	private File log;

	/** Send an email when the job is done */
	private boolean sendEmail = false;

	/** show the progress every so often */
	private Long showProgressFreq;

	/**
	 * Description of the process
	 */
	private String description;
	
	/**
	 * The number of records the Match engine will process.
	 * The default value is 0, which is used to mean unlimited.
	 * This field is usually used for debugging purposes where the
	 * engine is run on a limited number of records for testing purposes.
     *
	 * Also, if showProgressFreq is true, process processCount records before a
	 * progress message is printed
	 */
	private Integer processCount;

	/**
	 * The last time these settings were used in a run
	 */
	private Date lastRunDate;

	public boolean getAppendToLog() {
		return appendToLog;
	}

	public void setAppendToLog(boolean appendToLog) {
		boolean oldValue = this.appendToLog;
		this.appendToLog = appendToLog;
		getEventSupport().firePropertyChange("appendToLog", oldValue,
				appendToLog);
	}

	public boolean getDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		boolean oldValue = this.debug;
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

	public boolean getSendEmail() {
		return sendEmail;
	}

	public void setSendEmail(boolean sendEMail) {
		boolean oldValue = this.sendEmail;
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

	public File getLog() {
		return log;
	}

	public void setLog(File log) {
		File oldValue = this.log;
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
        buf.append("sendEmail->"+sendEmail+", ");
        buf.append("showProgressFreq->"+showProgressFreq +"]");
        return buf.toString();
    }

}