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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.ddl.DDLUtils;

/**
 * This class is used to run the C implementation of the Merge engine. It can be used
 * to check preconditions for successful running of the engine, as well as generating
 * the necessary command to run the engine.
 */
public class MergeEngineImpl extends AbstractCEngine {

	private static final Logger logger = Logger.getLogger(MergeEngineImpl.class);
	
	public MergeEngineImpl(MatchMakerSession session, Match match) {
		this.setSession(session);
		this.setMatch(match);
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
        Match match = getMatch();
        final MatchMakerSessionContext context = session.getContext();
        final MergeSettings settings = match.getMergeSettings();
        
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
        
        if (!canExecuteMergeEngine(session.getContext())) {
        	throw new EngineSettingException(
        			"The Merge engine executable at "+
                    session.getContext().getMatchEngineLocation()+" is either " +
                    "missing or not accessible");
        }
        
        if (!Match.doesSourceTableExist(session, match)) {
            throw new EngineSettingException(
                    "Your match source table \""+
                    DDLUtils.toQualifiedName(match.getSourceTable())+
            "\" does not exist");
        }
        
        if (!session.canSelectTable(match.getSourceTable())) {
            throw new EngineSettingException(
            "PreCondition failed: can not select match source table");
        }
        
        if (!Match.doesResultTableExist(session, match)) {
            throw new EngineSettingException(
            "PreCondition failed: match result table does not exist");
        }
        
        if (!match.vertifyResultTableStruct() ) {
            throw new EngineSettingException(
            "PreCondition failed: match result table structure incorrect");
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
		return canExecuteFile(
				context.getMergeEngineLocation());
	}
	
	/**
	 * Generates the command-line command necessary to run the Merge engine
	 * with the settings set in the GUI.
	 * <p>
	 * The format for the command to run the current implementation of the engine
	 * is like this.
	 * <p>
	 * <pre>
	 *  e.g.:merge_odbc.exe MERGE=xxx USER=xx/xx
	 *      DEBUG=[N/Y] COMMIT_FREQ=100 LOG_FILE=xxx ERR_FILE=xxx
	 *      PROCESS_CNT=0 USER_PROMPT=[Y/N] SHOW_PROGRESS=0
	 *      ROLLBACK_SEGMENT_NAME=[ROLLBACK_SEGMENT_NAME]
	 *      APPEND_TO_LOG_IND=[N/Y] SEND_EMAIL=[Y/N]
	 * </pre>
	 */
	public String[] createCommandLine(boolean userPrompt) {
		List<String> command = new ArrayList<String>();
		final SQLDatabase db = getSession().getDatabase();
		final MergeSettings settings = getMatch().getMergeSettings();

		command.add(getSession().getContext().getMergeEngineLocation());
		if ( logger.isDebugEnabled() ) {
			command.add(" -k ");
		}
		command.add("MERGE=" + getMatch().getName());
		command.add("USER=" + db.getDataSource().getUser() +
				"/" + db.getDataSource().getPass() +
				"@" + db.getDataSource().getName());
		command.add("DEBUG=" + (settings.getDebug() ? "Y" : "N"));
		if (settings.getCommitFrequency() != null) {
			command.add("COMMIT_FREQ=" + settings.getCommitFrequency());
		}
		command.add("LOG_FILE=" + settings.getLog().getPath());
		command.add("ERR_FILE=" + settings.getErrorLogFile().getPath());
		if ( settings.getProcessCount() != null ) {
			command.add("PROCESS_CNT=" + settings.getProcessCount());
		}
		if ( !userPrompt ) {
			command.add("USER_PROMPT=N");
		}
		if ( settings.getShowProgressFreq() != null ) {
			command.add("SHOW_PROGRESS=" + settings.getShowProgressFreq());
		}
		command.add("APPEND_TO_LOG_IND=" + (settings.getAppendToLog() ? "Y" : "N"));
		return command.toArray(new String[0]);
	}
	
	/**
	 * Returns the logger for the MergeEngineImpl class.
	 */
	public Logger getLogger() {
		return logger;
	}

}
