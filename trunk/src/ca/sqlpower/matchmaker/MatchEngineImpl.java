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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.sql.DefaultParameters;
import ca.sqlpower.sql.PLSchemaException;

/**
 * Sets up and runs the C Match Maker engine
 */
public class MatchEngineImpl extends AbstractCEngine {

	private static final Logger logger = Logger.getLogger(MatchEngineImpl.class);

	public MatchEngineImpl(MatchMakerSession session, Match match) {
		this.setSession(session);
		this.setMatch(match);
	}

	public void checkPreconditions() throws EngineSettingException, ArchitectException {
		MatchMakerSession session = getSession();
        Match match = getMatch();
        final MatchMakerSessionContext context = session.getContext();
        final MatchSettings settings = match.getMatchSettings();
        
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
        
        if (!canExecuteMatchEngine(session.getContext())) {
        	throw new EngineSettingException(
        			"The Matchmaker engine executable at "+
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
        
        if (settings.getSendEmail()) {
            if (!canExecuteEmailEngine(session.getContext())) {
                throw new EngineSettingException(
                        "The email notification executable is not found.\n" +
                " It should be in the directory of pl.ini");
            }
        
            Connection con = null;
            try {
                con = session.getConnection();
                if (!validateEmailSetting(new DefaultParameters(con))) {
                    throw new EngineSettingException(
                            "missing email setting information," +
                            " the email sender requires smtp server name and" +
                    " returning email address!");
                }
            } catch (SQLException e) {
                throw new EngineSettingException("Cannot validate email settings",e);
            } catch (PLSchemaException e) {
                throw new EngineSettingException("Cannot validate email settings",e);
            } finally {
                try {
                    if (con != null) con.close();
                } catch (SQLException ex) {
                    logger.warn("Couldn't close connection", ex);
                }
            }
        }
                
        if (!canWriteLogFile(settings)) {
            throw new EngineSettingException("The log file is not writable.");
        }
	} 
    
	/**
	 * returns true if the DEF_PARAM.EMAIL_NOTIFICATION_RETURN_ADRS and
	 * DEF_PARAM.MAIL_SERVER_NAME column are not null or empty, they are
	 * require to run email_notification engine.
	 */
	static boolean validateEmailSetting(DefaultParameters def) {
		boolean validate;
		String emailAddress = def.getEmailReturnAddress();
		String smtpServer = def.getEmailServerName();
		validate = emailAddress != null &&
					emailAddress.length() > 0 &&
					smtpServer != null &&
					smtpServer.length() > 0;
		return validate;
	}

	static boolean canExecuteMatchEngine(MatchMakerSessionContext context) {
		return canExecuteFile(
				context.getMatchEngineLocation());
	}

	static boolean canExecuteEmailEngine(MatchMakerSessionContext context) {
		return canExecuteFile(
				context.getEmailEngineLocation());
	}

	public String[] createCommandLine(boolean userPrompt) {
		/*
		 * command line sample:
		 * "M:\Program Files\Power Loader Suite\Match_Oracle.exe"
		 * MATCH="MATCH_ORGS" USER=PL/pl@arthur_test DEBUG=Y
		 * TRUNCATE_CAND_DUP=N SEND_EMAIL=N APPEND_TO_LOG_IND=N
		 * LOG_FILE="M:\Program Files\Power Loader Suite\Power Loader\script\MATCH_ORGS.log"
		 * SHOW_PROGRESS=10 PROCESS_CNT=1
		 */
		List<String> command = new ArrayList<String>();
		final SQLDatabase db = getSession().getDatabase();
		final MatchSettings settings = getMatch().getMatchSettings();

		command.add(getSession().getContext().getMatchEngineLocation());
		if ( logger.isDebugEnabled() ) {
			command.add(" -k ");
		}
		command.add("MATCH=" + getMatch().getName());
		command.add("USER=" + db.getDataSource().getUser() +
				"/" + db.getDataSource().getPass() +
				"@" + db.getDataSource().getName());
		command.add("DEBUG=" + (settings.getDebug() ? "Y" : "N"));
		command.add("TRUNCATE_CAND_DUP=" + (settings.getTruncateCandDupe() ? "Y" : "N"));
		command.add("SEND_EMAIL=" + (settings.getSendEmail() ? "Y" : "N"));
		command.add("APPEND_TO_LOG_IND=" + (settings.getAppendToLog() ? "Y" : "N"));
		command.add("LOG_FILE=" + settings.getLog().getPath());
		if ( settings.getShowProgressFreq() != null ) {
			command.add("SHOW_PROGRESS=" + settings.getShowProgressFreq());
		}
		if ( settings.getProcessCount() != null ) {
			command.add("PROCESS_CNT=" + settings.getProcessCount());
		}
		if ( !userPrompt ) {
			command.add("USER_PROMPT=N");
		}
		return command.toArray(new String[0]);
	}

	/**
	 * Returns the logger for the MatchEngineImpl class.
	 */
	public Logger getLogger() {
		return logger;
	}
	
}
