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

import java.io.IOException;
import java.io.InputStream;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.matchmaker.event.EngineListener;
import ca.sqlpower.util.Monitorable;

/**
 * the matchmaker engine interface represents the contexts that required 
 * to run an engine, the engine now is a C program uses ODBC to connect
 * to database as we create this interface. we may change it to java base
 * in the future.
 * 
 * 
 * This interface is not reentrant.  If you want to do multiple runs
 * create a new instance.
 *  
 */
public interface MatchMakerEngine extends Monitorable{

	/**
	 * start the engine!
	 * 
	 * Note this is not reentrant.  
	 * @throws EngineSettingException if not all the preconditions met
	 * @throws IOException 
	 * @throws ArchitectException 
	 */
	public void run() throws EngineSettingException, IOException, ArchitectException;
	
	public boolean isRunning();
	
	/**
	 * stop the engine
	 */
	public void abort();
	
	/**
	 * returns the engine exit code, null if the engine has not been run yet.
	 * @throws InterruptedException 
	 */
	public Integer getEngineReturnCode();
	
	/**
	 * Makes an effort to verify all the assumptions that the engine makes
     * about the local environment and the remote database(s) are valid.
     * Throws an exception if the engine's preconditions are not currently
     * fulfilled.
     * 
	 * @throws EngineSettingException If there is a precondition to running
     * the engine which is not currently met.
	 * @throws ArchitectException If there are errors encountered while attempting
     * to check the precondidions (this is a more severe case than a precondition
     * failure, because it means there's something wrong with the MatchMaker too).
	 */
	public void checkPreconditions() throws EngineSettingException, ArchitectException;
	
	/**
	 * Create the command line to run the match engine or for display
	 * @param session -- the session that contains the database that we are 
	 * going to run engine on 
	 * @param match  -- the match object that we want to create command line for
	 * @param userPrompt -- true if you want to append USER_PARAMPT=Y to the command line
	 * @return the string of the command line
	 */
	public String createCommandLine(MatchMakerSession session, Match match, boolean userPrompt);
	
	/**
	 * returns the standard error of the engine, if it's running, otherwise returns null
	 */
	public InputStream getEngineErrorOutput();

	/**
	 * returns the standard output of the engine, if it's running, otherwise returns null
	 */
	public InputStream getEngineStandardOutput();
	
	/**
	 * Add a engine listener to this engine.  Note the listener cannot be null
	 * @param l EngineListener not null
	 */
	public void addEngineListener(EngineListener l);
	
	/**
	 * Removes a engine listener from this engine.  Note the listener cannot be null
	 * @param l EngineListener not null
	 */
	public void removeEngineListener(EngineListener l);
}