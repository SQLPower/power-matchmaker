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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CancellationException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.util.EmailAppender;

/**
 * Implements the merging and purging functionality of the MatchMaker.
 */
public class MergeEngineImpl extends AbstractEngine {

	private static final Logger logger = Logger.getLogger(MergeEngineImpl.class);
	
	private static final String DB_OBJECT_TYPE = "MERGE_ENGINE";

	private String progressMessage;
	
	private MergeProcessor merger;
	
	public MergeEngineImpl(MatchMakerSession session, Project project) {
		this.setSession(session);
		this.setProject(project);
	}
	
	public void checkPreconditions() throws EngineSettingException, ArchitectException, SourceTableException {
		
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
        
        if (!project.doesSourceTableExist()) {
            throw new SourceTableException(
                    "PreCondition failed: Your project source table \""+
                    DDLUtils.toQualifiedName(project.getSourceTable())+
            "\" does not exist");
        }
        
        if (!project.verifySourceTableStructure()) {
			throw new SourceTableException(
					"PreCondition failed: Source table structure has changed!");
		}
        
        if (!session.canSelectTable(project.getSourceTable())) {
            throw new EngineSettingException(
            "PreCondition failed: can not select project source table");
        }
        
        if (!project.doesResultTableExist()) {
            throw new EngineSettingException(
            "PreCondition failed: project result table does not exist");
        }
        
        if (!project.verifyResultTableStructure() ) {
            throw new EngineSettingException(
            "PreCondition failed: project result table structure incorrect");
        }
        
        if (settings.getSendEmail()) {
        	// First checks if the email settings are correct
        	if (!validateEmailSetting(context)) {
        		throw new EngineSettingException(
        				"missing email setting information," +
        				" the email sender requires smtp host name!");
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
	
	public EngineInvocationResult call() throws EngineSettingException, SourceTableException {
		Level oldLevel = logger.getLevel();
		FileAppender fileAppender = null;
		EmailAppender emailAppender = null;
		setCancelled(false);
		Connection con = null;

		logger.setLevel(getMessageLevel());
		setFinished(false);
		setStarted(true);

		try {
			progressMessage = "Checking Merge Engine Preconditions";
			logger.info(progressMessage);
			checkPreconditions();
		} catch (ArchitectException e) {
			throw new RuntimeException(e);
		}

		try {
			String logFilePath = getProject().getMergeSettings().getLog().getAbsolutePath();
			boolean appendToFile = getProject().getMergeSettings().getAppendToLog();
			fileAppender = new FileAppender(new PatternLayout("%d %p %m\n"), logFilePath, appendToFile);
			logger.addAppender(fileAppender);
			
			if (getProject().getMungeSettings().getSendEmail()) {
				String emailSubject = "Project " + getProject().getName() + " Match Engine";
				emailAppender = new EmailAppender(email, emailSubject, greenUsers, yellowUsers, redUsers);
				logger.addAppender(emailAppender);
			}
			
			progressMessage = "Starting Merge Engine";
			logger.info(progressMessage);
			con = getProject().createSourceTableConnection();
			con.setAutoCommit(false);
			merger = new MergeProcessor(getProject(), con, getLogger());
			merger.call();
            checkCancelled();
            if (getProject().getMergeSettings().getDebug()) {
            	logger.info("Running in Debug Mode, so rolling back changes");
            	con.rollback();
            } else {
            	logger.debug("Commiting changes");
            	con.commit();
            }
			progressMessage = "Merge Engine finished successfully";
			logger.info(progressMessage);
			return EngineInvocationResult.SUCCESS;
		} catch (CancellationException cex) {
			logger.warn("Merge engine terminated by user");
			return EngineInvocationResult.ABORTED;
		} catch (Exception ex) {
		    logger.error("Merge failed", ex);
			logger.error("Attempting rollback...");
			if (con != null) {
				try {
					con.rollback();
					logger.info("Rollback complete");
				} catch (SQLException e) {
					logger.error("Rollback failed", e);
				}
			}
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
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					logger.error("Cannot close connection after engine run.", e);
				}
			}
			
		}
	}
	
	///////// Monitorable support ///////////
	
	@Override
	public Integer getJobSize() {
		if (merger != null)
			return merger.getJobSize();
		else {
			return 1;
		}
	}

	@Override
	public String getMessage() {
		return getProgress() + "/" + getJobSize() + ": " + progressMessage;
	}

	public int getProgress() {
		if (merger != null) {
			return merger.getProgress();
		} else {
			return 0;
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
