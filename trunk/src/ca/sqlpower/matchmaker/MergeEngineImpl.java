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

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.sql.SQL;
import ca.sqlpower.util.EmailAppender;

/**
 * Implements the merging and purging functionality of the MatchMaker.
 */
public class MergeEngineImpl extends AbstractEngine {

	private static final Logger logger = Logger.getLogger(MergeEngineImpl.class);
	
	private static final String DB_OBJECT_TYPE = "MERGE_ENGINE";

	private int jobSize;
	private int progress;
	private String progressMessage;
	
	private MergeProcessor merger;
	
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
        
        if (!project.verifyResultTableStruct() ) {
            throw new EngineSettingException(
            "PreCondition failed: project result table structure incorrect");
        }
        
        if (settings.getSendEmail()) {
        	// First checks if the email settings are correct
        	if (!validateEmailSetting(context)) {
        		throw new EngineSettingException(
        				"missing email setting information," +
        				" the email sender requires smtp host name and" +
        		" smtp localhost name!");
        	}
        	
        	// Then creates the emails for each status
        	try {
				setupEmail(context);
			} catch (Exception e) {
				throw new EngineSettingException("PreCondition failed: " +
						"error while setting up for sending emails.", e);
			}
        }
        
        if (!canWriteLogFile(settings)) {
            throw new EngineSettingException("The log file is not writable.");
        }
	}

	/**
	 * Returns the logger for the MergeEngineImpl class.
	 */
	public Logger getLogger() {
		return logger;
	}
	
	public EngineInvocationResult call() throws EngineSettingException {
		Level oldLevel = logger.getLevel();
		FileAppender fileAppender = null;
		EmailAppender emailAppender = null;
		setCancelled(false);

		try {
			logger.setLevel(getMessageLevel());
			setFinished(false);
			setStarted(true);
			progress = 0;
			progressMessage = "Checking Merge Engine Preconditions";
			logger.info(progressMessage);
			
			try {
				checkPreconditions();
			} catch (ArchitectException e) {
				throw new RuntimeException(e);
			}
			
			String logFilePath = getProject().getMergeSettings().getLog().getAbsolutePath();
			boolean appendToFile = getProject().getMergeSettings().getAppendToLog();
			fileAppender = new FileAppender(new PatternLayout("%d %p %m\n"), logFilePath, appendToFile);
			logger.addAppender(fileAppender);
			
			if (getProject().getMungeSettings().getSendEmail()) {
				String emailSubject = "Project " + getProject().getName() + " Match Engine";
				emailAppender = new EmailAppender(email, emailSubject, greenUsers, yellowUsers, redUsers);
				logger.addAppender(emailAppender);
			}
			
			jobSize = getNumRowsToProcess();

			progressMessage = "Starting Merge Engine";
			logger.info(progressMessage);
			merger = new MergeProcessor(getProject(), getSession(), getLogger());
			merger.call();
			if (isCanceled()) {
				throw new UserAbortException();
			}
			progress += merger.getProgress();
			
			progressMessage = "Merge Engine finished successfully";
			logger.info(progressMessage);
			
			return EngineInvocationResult.SUCCESS;
		} catch (UserAbortException uce) {
			logger.info("Merge aborted by user");
			return EngineInvocationResult.ABORTED;
		} catch (Exception ex) {
			progressMessage = "Cleanse Engine failed";
			logger.error(getMessage());
			throw new RuntimeException(ex);
		} finally {
			logger.setLevel(oldLevel);
			setFinished(true);
			
			if (emailAppender != null) {
				try {
					emailAppender.close();
				} catch (RuntimeException e) {
					logger.warn("Error while sending emails: " + e.getMessage());
				}
				logger.removeAppender(emailAppender);
			}
			if (fileAppender != null) {
				logger.removeAppender(fileAppender);
			}
			
		}
	}

	private int getNumRowsToProcess() throws SQLException {
		Integer processCount = getProject().getMergeSettings().getProcessCount();
		int rowCount;
		Connection con = null;
		Statement stmt = null;
		try {
			con = getSession().getConnection();
			stmt = con.createStatement();
			String rowCountSQL = "SELECT COUNT(*) AS ROW_COUNT FROM " + 
				DDLUtils.toQualifiedName(getProject().getResultTable()) +
				" WHERE MATCH_STATUS = " + SQL.quote("AUTO_MATCH") 
				+ " OR MATCH_STATUS = " + SQL.quote("MATCH");
			ResultSet result = stmt.executeQuery(rowCountSQL);
			logger.debug("Getting result table row count with SQL statment " + rowCountSQL);
			result.next();
			rowCount = result.getInt("ROW_COUNT");
		} finally {
			if (stmt != null) stmt.close();
			if (con != null) con.close();
		}
		if (processCount != null && processCount.intValue() > 0 && processCount.intValue() < rowCount) {
			rowCount = processCount.intValue();
		}
		return rowCount;
	}
	
	///////// Monitorable support ///////////
	
	@Override
	public Integer getJobSize() {
		return jobSize;
	}

	@Override
	public String getMessage() {
		return getProgress() + "/" + jobSize + ": " + progressMessage;
	}

	public int getProgress() {
		if (merger != null) {
			return progress + merger.getProgress();
		} else {
			return progress;
		}
	}

	public String getObjectType() {
		return DB_OBJECT_TYPE;
	}
	
	@Override
	public synchronized void setCancelled(boolean cancelled) {
		super.setCancelled(cancelled);
		if (cancelled && merger != null) {
			merger.setCancelled(true);
		}
	}
}
