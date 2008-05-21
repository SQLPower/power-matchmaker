/*
 * Copyright (c) 2008, SQL Power Group Inc.
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
import java.sql.SQLException;
import java.util.concurrent.Callable;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.sql.DatabaseObject;
import ca.sqlpower.util.Monitorable;

/**
 * The matchmaker engine interface is a generic way of controlling a
 * process which does some time-consuming operation on a Match.  Currently,
 * there are two major implementations: The Match Engine and Merge Engine.
 */
public interface MatchMakerEngine extends Monitorable,
	Callable<EngineInvocationResult>, DatabaseObject {

	/**
	 * Starts the engine.  This method returns once the engine run has
	 * completed, so if you're running the engine within a Swing GUI, it
	 * is almost always necessary to call this method on a separate worker
	 * thread.
	 *   
	 * @throws EngineSettingException When the preconditions for running the
	 * engine are not met.
	 * @throws IOException for any I/O problems 
	 * @throws SQLException if PreMergeDataFudger fails
	 */
	public EngineInvocationResult call() throws EngineSettingException, IOException, SQLException;
	
	/**
	 * Makes an effort to verify all the assumptions that the engine makes
     * about the local environment and the remote database(s) are valid.
     * Throws an exception if the engine's preconditions are not currently
     * fulfilled.
     * 
	 * @throws EngineSettingException If there is a precondition to running
     * the engine which is not currently met.
	 * @throws ArchitectException If there are errors encountered while attempting
     * to check the preconditions (this is a more severe case than a precondition
     * failure, because it means there's something wrong with the MatchMaker too).
	 */
	public void checkPreconditions() throws EngineSettingException, ArchitectException;
	
	/**
	 * Creates the command line to run the match engine, based on the
	 * current engine settings for the appropriate engine. 
	 * 
	 * @return The command line for running the engine. Each command line parameter
	 * is a separate item in the array, and the first item in the array is the program
	 * name itself.  All characters stand for their literal value. There are no escaping
	 * or quoting characters.
	 */
	public String[] createCommandLine();

	/**
	 * Returns the logger instance that all engine messages are logged to.  Engine messages
	 * are logged at any combination of the standard Log4J logging levels.
	 */
	public Logger getLogger();
	
	/**
	 * Return the level at which to log the engine progress
	 */
	public Level getMessageLevel();
	
	/**
	 * Sets the level at which to log the engine progress
	 */
	public void setMessageLevel(Level lev);
}