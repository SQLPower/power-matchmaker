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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.matchmaker.event.EngineEvent;
import ca.sqlpower.matchmaker.event.EngineListener;
import ca.sqlpower.matchmaker.event.EngineEvent.EngineEventType;
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
	 * @see ca.sqlpower.matchmaker.MatchMakerEngine#abort()
	 */
	public void abort() {
		if ( proc != null ) {
			proc.destroy();
			proc = null;
		}
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
		String commandLine = createCommandLine(session,match,false);
		Runtime rt = Runtime.getRuntime();
		logger.debug("Executing " + commandLine);
		proc = rt.exec(commandLine);
		fireEngineStart();
		processMonitor = new Thread(new Runnable(){

					public void run() {
						try {
							proc.waitFor();
							engineExitCode = proc.exitValue();
							fireEngineEnd();
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					}
				});
		processMonitor.start();
	}

	public InputStream getEngineErrorOutput() {
		if ( proc != null ) {
			return proc.getErrorStream();
		}
		return null;
	}

	public InputStream getEngineStandardOutput() {
		if ( proc != null ) {
			return proc.getInputStream();
		}
		return null;
	}

	/** ENGINE EVENT SUPPORT **/
	private List<EngineListener> engineListeners = new ArrayList<EngineListener>();
	
	public void addEngineListener(EngineListener l){
		if (l == null) throw new NullPointerException();
		engineListeners.add(l);
	}
	public void removeEngineListener(EngineListener l){
		engineListeners.remove(l);
	}
	
	void fireEngineStart() {
		for (int i = engineListeners.size()-1; i >= 0; i--){
			EngineEvent e = new EngineEvent(this,EngineEventType.ENGINE_START,match);
			engineListeners.get(i).engineStart(e);
		}
	}

	void fireEngineEnd() {
		for (int i = engineListeners.size()-1; i >= 0; i--){
			EngineEvent e = new EngineEvent(this,EngineEventType.ENGINE_START,match);
			engineListeners.get(i).engineEnd(e);
		}
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
		abort();
	}
}
