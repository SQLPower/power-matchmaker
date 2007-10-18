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

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ddl.DDLUtils;

/**
 * Implements the merging and purging functionality of the MatchMaker.
 */
public class MergeEngineImpl extends AbstractEngine {

	private static final Logger logger = Logger.getLogger(MergeEngineImpl.class);
	
	public MergeEngineImpl(MatchMakerSession session, Project project) {
		this.setSession(session);
		this.setProject(project);
	}
	
	/**
	 * Checks a series of preconditions necessary for the engine to run. If any of the
	 * preconditions fail, it will throw an EngineSettingException. 
	 * <p>
	 * Note: This sort of checking is only necessary because the current C-based implementation
	 * of the engine does not provide detailed enough error messages when the preconditions fail.
	 */
	public void checkPreconditions() throws EngineSettingException, ArchitectException {
		
		MatchMakerSession session = getSession();
        Project project = getProject();
        final MatchMakerSessionContext context = session.getContext();
        final MergeSettings settings = project.getMergeSettings();
        
        if ( context == null ) {
        	throw new EngineSettingException(
        			"PreCondition failed: session context must not be null");
        }
        
        if ( session.getDatabase() == null ) {
        	throw new EngineSettingException(
        			"PreCondition failed: database of the session must not be null");
        }
        if ( session.getDatabase().getDataSource() == null ) {
        	throw new EngineSettingException(
        			"PreCondition failed: data source of the session must not be null");
        }
        
        if (!hasODBCDSN(session.getDatabase().getDataSource())){
        	throw new EngineSettingException(
        			"Your Data Source \""+
                    session.getDatabase().getDataSource().getDisplayName()+
                    "\" doesn't have the ODBC DSN set.");
        }
        
        if (!Project.doesSourceTableExist(session, project)) {
            throw new EngineSettingException(
                    "Your project source table \""+
                    DDLUtils.toQualifiedName(project.getSourceTable())+
            "\" does not exist");
        }
        
        if (!session.canSelectTable(project.getSourceTable())) {
            throw new EngineSettingException(
            "PreCondition failed: can not select project source table");
        }
        
        if (!Project.doesResultTableExist(session, project)) {
            throw new EngineSettingException(
            "PreCondition failed: project result table does not exist");
        }
        
        if (!project.vertifyResultTableStruct() ) {
            throw new EngineSettingException(
            "PreCondition failed: project result table structure incorrect");
        }
        
        if (!canWriteLogFile(settings)) {
            throw new EngineSettingException("The log file is not writable.");
        }
	}

	/**
	 * Returns true if the Merge engine executable specified in the users settings
	 * from the given context can be executed by the user. 
	 */
	static boolean canExecuteMergeEngine(MatchMakerSessionContext context) {
        // TODO check things
        return true;
	}
	
	/**
	 * Returns the logger for the MergeEngineImpl class.
	 */
	public Logger getLogger() {
		return logger;
	}
	
	public EngineInvocationResult call() throws EngineSettingException {
		try {
			try {
				checkPreconditions();
			} catch (ArchitectException e) {
				throw new RuntimeException(e);
			}
			setFinished(false);
			setStarted(true);
			
			Processor merger = new MergeProcessor(getProject(), getSession());
			merger.call();
			
			getLogger().info("Engine process completed normally.");
			
			return EngineInvocationResult.SUCCESS;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			setFinished(true);
		}
	}

}
