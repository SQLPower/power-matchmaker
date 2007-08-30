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

import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
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
	private Thread processMonitor;
	private Integer engineExitCode;
	
	public abstract void checkPreconditions() throws EngineSettingException, ArchitectException; 
	
		
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

	/* (non-Javadoc)
	 * @see ca.sqlpower.matchmaker.MatchMakerEngine#getEngineReturnCode()
	 */
	public Integer getEngineReturnCode() {
		return engineExitCode;
	}

	/* (non-Javadoc)
	 * @see ca.sqlpower.matchmaker.MatchMakerEngine#isRunning()
	 */
	public boolean isRunning() {
		if (processMonitor == null) {
			return false;
		} else {
			return processMonitor.isAlive();
		}
	}

	/* (non-Javadoc)
	 * @see ca.sqlpower.matchmaker.MatchMakerEngine#run()
	 */
	public void run() throws EngineSettingException, IOException, ArchitectException {
		checkPreconditions();
		if (proc!=null) throw new IllegalStateException("Engine has already been run");
		String[] commandLine = createCommandLine(session,match,false);
		Runtime rt = Runtime.getRuntime();
		logger.debug("Executing " + Arrays.asList(commandLine));
		proc = rt.exec(commandLine);
		
		StreamLogger errorGobbler = new StreamLogger(proc.getErrorStream(), getLogger(), Level.ERROR);
		StreamLogger outputGobbler = new StreamLogger(proc.getInputStream(), getLogger(), Level.INFO);
		errorGobbler.start();
		outputGobbler.start();
		
		processMonitor = new Thread(new Runnable(){

					public void run() {
						try {
							proc.waitFor();
							engineExitCode = proc.exitValue();
							getLogger().info("Engine process completed with status " + engineExitCode);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					}
				});
		processMonitor.start();
	}

	///////// Monitorabe support ///////////
	/**
	 * Right now the job size is always indeterminant
	 */
	public Integer getJobSize() {
		return null;
	}

	public String getMessage() {
		if(isRunning()){
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
		return isRunning() || getEngineReturnCode() != null;
	}
	
	// The engine is done when it has an exit code
	public boolean isFinished() {
		if (getEngineReturnCode() != null){
			return true;
		} else {
			return false;
		}
	}

	public void setCancelled(boolean cancelled) {
		if (cancelled && proc != null) {
			proc.destroy();
			proc = null;
		}
	}
}
