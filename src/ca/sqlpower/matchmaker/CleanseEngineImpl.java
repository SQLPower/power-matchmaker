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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeProcessor;
import ca.sqlpower.sql.DefaultParameters;
import ca.sqlpower.sql.PLSchemaException;

/**
 * The MatchMaker's cleansing engine.  Runs all of the munge steps in the correct
 * order for each row of input.
 */
public class CleanseEngineImpl extends AbstractEngine {

	private static final Logger logger = Logger.getLogger(CleanseEngineImpl.class);

	private int jobSize;

	private int progress;

	private MungeProcessor munger;

	private String progressMessage;
	
	private Processor currentProcessor;

	public CleanseEngineImpl(MatchMakerSession session, Project project) {
		this.setSession(session);
		this.setProject(project);
	}

	public void checkPreconditions() throws EngineSettingException, ArchitectException {
		MatchMakerSession session = getSession();
        Project project = getProject();
        final MatchMakerSessionContext context = session.getContext();
        final MungeSettings settings = project.getMungeSettings();
        
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
        
        if (settings.getSendEmail()) {
        
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

	/**
	 * Returns the logger for the MatchEngineImpl class.
	 */
	public Logger getLogger() {
		return logger;
	}
	
	@Override
	public EngineInvocationResult call() throws EngineSettingException {
		try {
			setFinished(false);
			setStarted(true);
			progress = 0;
			progressMessage = "Checking Cleanse Engine Preconditions";
			logger.info(progressMessage);
			
			try {
				checkPreconditions();
			} catch (ArchitectException e) {
				throw new RuntimeException(e);
			}
			
			progressMessage = "Starting Cleanse Engine";
			logger.info(progressMessage);
			
			Integer processCount = getProject().getMungeSettings().getProcessCount();
			int rowCount;
			if (processCount == null || processCount == 0) {
				Connection con = null;
				Statement stmt = null;
				try {
					con = getSession().getConnection();
					stmt = con.createStatement();
					String rowCountSQL = "SELECT COUNT(*) AS ROWCOUNT FROM " + DDLUtils.toQualifiedName(getProject().getSourceTable());
					ResultSet result = stmt.executeQuery(rowCountSQL);
					logger.debug("Getting source table row count with SQL statment " + rowCountSQL);
					result.next();
					rowCount = result.getInt("ROWCOUNT");
				} finally {
					if (stmt != null) stmt.close();
					if (con != null) con.close();
				}
			} else {
				rowCount = processCount.intValue();
			}
			
			List<MungeProcess> mungeProcesses = getProject().getMungeProcessesFolder().getChildren();
			jobSize = rowCount * mungeProcesses.size();
			
			for (MungeProcess currentProcess: mungeProcesses) {
				munger = new MungeProcessor(currentProcess);
				currentProcessor = munger;
				progressMessage = "Running cleanse process " + currentProcess.getName();
				logger.debug(getMessage());
				munger.call();
				progress += munger.getProgress();

			}
			
			currentProcessor = null;
			
			progressMessage = "Cleanse Engine finished successfully";
			logger.info(progressMessage);
			
			return EngineInvocationResult.SUCCESS;
		} catch (Exception ex) {
			progressMessage = "Cleanse Engine failed";
			logger.error(getMessage());
			throw new RuntimeException(ex);
		} finally {
			setFinished(true);
		}
	
	}
	
	///////// Monitorable support ///////////
	
	/**
	 * Right now the job size is always indeterminant
	 */
	
	@Override
	public Integer getJobSize() {
		return jobSize;
	}

	@Override
	public String getMessage() {
		return getProgress() + "/" + jobSize + ": " + progressMessage;
	}

	public int getProgress() {
		if (currentProcessor != null) {
			return progress + currentProcessor.getProgress();
		} else {
			return progress;
		}
	}
}
