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

package ca.sqlpower.matchmaker;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.sql.SPDataSource;
/**
 * Common ground for all C engines.  This class handles events
 * output capture, monitoring and starting and stoping the engine. 
 *
 */
public abstract class AbstractEngine implements MatchMakerEngine {

	private final static Logger logger = Logger.getLogger(AbstractEngine.class);

    /**
	 * The session that this engine operates in.
	 */
	private MatchMakerSession session;
    
    /**
     * The match project this engine operates on.
     */
	private Match match;
	
	/**
	 * Gets set to true when the process has started.
	 */
	private boolean started = false;
	
	/**
	 * Gets set to true when the process has terminated.
	 */
	private boolean finished = false;
	
	/**
	 * Gets set to true when the user attempts to cancel the engine run.
	 */
	private boolean cancelled;
	
	protected Match getMatch() {
		return match;
	}

	protected void setMatch(Match match) {
		this.match = match;
	}

	protected MatchMakerSession getSession() {
		return session;
	}

	protected void setSession(MatchMakerSession session) {
		this.session = session;
	}

	public EngineInvocationResult call() throws EngineSettingException {
		try {
			try {
				checkPreconditions();
			} catch (ArchitectException e) {
				throw new RuntimeException(e);
			}
			
            finished = false;
			started = true;

			getLogger().info("Engine process completed normally.");
			
			return EngineInvocationResult.SUCCESS;
		} finally {
			finished = true;
		}
	}

	///////// Monitorable support ///////////
	
	/**
	 * Right now the job size is always indeterminant
	 */
	public Integer getJobSize() {
		return null;
	}

	public String getMessage() {
		if(started && !finished){
			return "Running MatchMaker Engine";
		} else {
			return "";
		}
	}

	public int getProgress() {
		// since this is always indeterminant  
		return 0;
	}

	public boolean hasStarted() {
		return started;
	}
	
	// The engine is done when it has an exit code
	public boolean isFinished() {
		return finished;
	}

	public synchronized void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
        // TODO interrupt the engine thread
	}

	/**
	 * check the DSN setting for the current database connection,
	 * that's required by the matchmaker odbc engine, since we will not
	 * use this odbc engine forever, check for not null is acceptable for now.
	 */
	protected static boolean hasODBCDSN(SPDataSource dataSource) {
		final String odbcDsn = dataSource.getOdbcDsn();
		if ( odbcDsn == null || odbcDsn.length() == 0 ) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * returns true if the log file of this match is readable.
	 */
	public static boolean canReadLogFile(MatchMakerSettings settings) {
		File file = settings.getLog();
		
		if (file == null) return false;
		
		return file.canRead();
	}

	/**
	 * returns true if the log file of this match is writable.
	 */
	public static boolean canWriteLogFile(MatchMakerSettings settings) {
	    File file = settings.getLog();
	    if (file == null) {
	    	logger.debug("file is null.");
	    	return false;
	    }
	    if (file.exists()) {
	        return file.canWrite();
	    } else {
	        try {
	            file.createNewFile();
	        } catch (IOException e) {
	            logger.debug("IOException thrown when testing write assuming failure");
	            return false;
	        }
	        // See java bug 4939819 (File.canWrite doesn't work properly on windows,
	        // so we would have to assume that the file is writable at this point)
	        // boolean canWrite = file.canWrite();
	        boolean canWrite = true;
	        file.delete();
	        return canWrite;
	    }
	}
    
    public String[] createCommandLine() {
        String javaHome = System.getProperty("java.home");
        String sep = System.getProperty("file.separator");
        String javaPath = javaHome + sep + "bin" + sep + "java";
        String className = getClass().getName();
        Long matchOid = match.getOid();
        
        return new String[] { javaPath, className, "match_oid=" + matchOid };
    }

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}
}
