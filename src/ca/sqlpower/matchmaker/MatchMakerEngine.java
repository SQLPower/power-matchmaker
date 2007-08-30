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

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.util.Monitorable;

/**
 * The matchmaker engine interface is a generic way of controlling a
 * process which does some time-consuming operation on a Match.  Currently,
 * there are two major implementations: The Match Engine and Merge Engine.
 * <p>
 * You can only start a particular instance of MatchMakerEngine once.  If
 * you want to do multiple runs create a new instance.
 * TODO reconsider that design decision: is it necessary and reasonable?
 */
public interface MatchMakerEngine extends Monitorable {

	/**
	 * Starts the engine. Note that you can only call this method once per
	 * instance of a MatchMakerEngine. XXX: maybe we can remove this restriction.
	 *   
	 * @throws EngineSettingException if not all the preconditions met
	 * @throws IOException 
	 * @throws ArchitectException 
	 */
	public void run() throws EngineSettingException, IOException, ArchitectException;
	
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
	 * Creates the command line to run the match engine, based on the
	 * current engine settings for the appropriate engine. 
	 * 
	 * @param session the session that contains the database that we are 
	 * going to run engine on 
	 * @param match the match object that we want to create command line for
	 * @param userPrompt true if you want to append USER_PROMPT=Y to the command line
	 * @return The command line for running the engine. Each command line parameter
	 * is a separate item in the array, and the first item in the array is the program
	 * name itself.  All characters stand for their literal value. There are no escaping
	 * or quoting characters.
	 */
	public String[] createCommandLine(MatchMakerSession session, Match match, boolean userPrompt);

	/**
	 * Returns the logger instance that all engine messages are logged to.  Engine messages
	 * are logged at any combination of the standard Log4J logging levels.
	 */
	public Logger getLogger();
}