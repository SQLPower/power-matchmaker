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
import java.util.Arrays;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.util.StreamLogger;
/**
 * Common ground for all C engines.  This class handles events
 * output capture, monitoring and starting and stoping the engine. 
 *
 */
public abstract class AbstractCEngine implements MatchMakerEngine {

	private final static Logger logger = Logger.getLogger(AbstractCEngine.class);
	/**
	 * the session that we are currently connectting to
	 */
	private MatchMakerSession session;
	private Match match;
	private Process proc;
	
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

	public EngineInvocationResult call() throws EngineSettingException, IOException {
		try {
			try {
				checkPreconditions();
			} catch (ArchitectException e) {
				throw new RuntimeException(e);
			}
			
			if (proc!=null) throw new IllegalStateException("Engine has already been run");
			String[] commandLine = createCommandLine(false);
			Runtime rt = Runtime.getRuntime();
			logger.debug("Executing " + Arrays.asList(commandLine));
			proc = rt.exec(commandLine);
			started = true;

			StreamLogger errorGobbler = new StreamLogger(proc.getErrorStream(), getLogger(), Level.ERROR);
			StreamLogger outputGobbler = new StreamLogger(proc.getInputStream(), getLogger(), Level.INFO);
			errorGobbler.start();
			outputGobbler.start();

			for (;;) {
				try {
					proc.waitFor();
					break;
				} catch (InterruptedException e) {
					if (cancelled) return EngineInvocationResult.ABORTED;
				}
			}
			
			int engineExitCode = proc.exitValue();
			getLogger().info("Engine process completed with status " + engineExitCode);
			if (engineExitCode == 0) {
				return EngineInvocationResult.SUCCESS;
			} else {
				return EngineInvocationResult.FAILURE;
			}
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
		if (cancelled && proc != null) {
			proc.destroy();
			proc = null;
		}
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

	/**
	 * returns true if the given file exists and executable, false otherwise.
	 * @param fileName  the name of the file you want to check.
	 */
	public static boolean canExecuteFile(String fileName) {
		final File file = new File(fileName);
		// TODO: switch to file.canExecute when we have java 1.6
		return file.exists() && file.canRead();
	}
}
