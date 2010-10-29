/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker;

import java.util.concurrent.CancellationException;

import ca.sqlpower.util.Monitorable;
import ca.sqlpower.util.MonitorableImpl;

public abstract class MatchMakerMonitorableImpl extends AbstractMatchMakerObject implements Monitorable {

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(MonitorableImpl.class);
    
    private int progress = 0;
    private Integer jobSize = null;
    private String message = null;
    private boolean started = false;
    private boolean cancelled = false;
    private boolean finished = false;
    
    public synchronized boolean isCancelled() {
        return cancelled;
    }
    
    public synchronized void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    public synchronized Integer getJobSize() {
        return jobSize;
    }
    
    public synchronized void setJobSize(Integer jobSize) {
        this.jobSize = jobSize;
    }
    
    public synchronized String getMessage() {
        return message;
    }
    
    public synchronized void setMessage(String message) {
        this.message = message;
    }
    
    public synchronized int getProgress() {
        logger.debug("Returning progress " + progress + "/" + getJobSize() + " message="+getMessage()+" started="+hasStarted()+" finished="+isFinished()+" cancelled="+isCancelled());
        return progress;
    }
    
    public synchronized void setProgress(int progress) {
        this.progress = progress;
    }
    
    public synchronized boolean hasStarted() {
        return started;
    }
    
    public synchronized void setStarted(boolean started) {
        this.started = started;
    }
    
    public synchronized boolean isFinished() {
        return finished;
    }
    
    public synchronized void setFinished(boolean finished) {
        this.finished = finished;
    }

    public synchronized void incrementProgress() {
        this.progress++;
    }
    
    public String toString() {
        return String.format("Job size: %4d " +
                                 "Progress: %4d " +
                                 "Started: %b " +
                                 "Finished: %b " +
                                 "Cancelled: %b " +
                                 "Message: %s ",
                                 jobSize, 
                                 progress, 
                                 started, 
                                 finished, 
                                 cancelled, 
                                 message);
    }
    
    public synchronized void checkCancelled() {
        if (isCancelled()) {
            throw new CancellationException();
        }
    }
}
