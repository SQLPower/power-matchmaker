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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeProcessor;
import ca.sqlpower.util.EmailAppender;

/**
 * The MatchMaker's cleansing engine.  Runs all of the munge steps in the correct
 * order for each row of input.
 */
public class CleanseEngineImpl extends AbstractEngine {

	private static final Logger logger = Logger.getLogger(CleanseEngineImpl.class);
	
	private static final String DB_OBJECT_TYPE = "CLEANSE_ENGINE";

	private int jobSize;

	private int progress;

	private MungeProcessor munger;

	private String progressMessage;
	
	/**
	 * The current munge processor that is running in the engine.
	 * <p>
	 * Never ever ever EVER set the currentProcessor directly, call
	 * {@link #setCurrentProcessor(Processor)} instead because it
	 * needs synchronized access.
	 * 
	 * Failure to do so is punishable by death by screaming monkeys!
	 */
	private Processor currentProcessor;
	
	public CleanseEngineImpl(MatchMakerSession session, Project project) {
		this.setSession(session);
		this.setProject(project);
	}

	public void checkPreconditions() throws EngineSettingException, ArchitectException, SourceTableException {
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

        if (settings.getSendEmail()) {
        	// First checks the email settings
        	if (!validateEmailSetting(context)) {
        		throw new EngineSettingException(
        				"missing email setting information," +
        				" the email sender requires smtp host name!");
        	}
        	
        	// Then tries to setup the emails to each status
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
	 * Returns the logger for the MatchEngineImpl class.
	 */
	public Logger getLogger() {
		return logger;
	}
	
	@Override
	public EngineInvocationResult call() throws EngineSettingException, SourceTableException {
		Level oldLevel = logger.getLevel();
		setCancelled(false);
		FileAppender fileAppender = null;
		EmailAppender emailAppender = null;
		
		logger.setLevel(getMessageLevel());
		setFinished(false);
		setStarted(true);
		progress = 0;

		try {
			progressMessage = "Checking Cleanse Engine Preconditions";
			logger.info(progressMessage);
			checkPreconditions();
		} catch (ArchitectException e) {
			throw new RuntimeException(e);
		}

		try {
			String logFilePath = getProject().getMungeSettings().getLog().getAbsolutePath();
			boolean appendToFile = getProject().getMungeSettings().getAppendToLog();
			fileAppender = new FileAppender(new PatternLayout("%d %p %m\n"), logFilePath, appendToFile);
			logger.addAppender(fileAppender);
			
			if (getProject().getMungeSettings().getSendEmail()) {
				String emailSubject = "Project " + getProject().getName() + " Match Engine";
				emailAppender = new EmailAppender(email, emailSubject, greenUsers, yellowUsers, redUsers);
				logger.addAppender(emailAppender);
			}
			
			progressMessage = "Starting Cleanse Engine";
			logger.info(progressMessage);
			
			Integer processCount = getProject().getMungeSettings().getProcessCount();
			int rowCount;
			if (processCount == null || processCount <= 0) {
				Connection con = null;
				Statement stmt = null;
				try {
					con = getProject().createSourceTableConnection();
					stmt = con.createStatement();
					String rowCountSQL = "SELECT COUNT(*) AS ROW_COUNT FROM " + DDLUtils.toQualifiedName(getProject().getSourceTable());
					ResultSet result = stmt.executeQuery(rowCountSQL);
					logger.debug("Getting source table row count with SQL statment " + rowCountSQL);
					if (result.next()) {
						rowCount = result.getInt("ROW_COUNT");
					} else {
						throw new AssertionError("No rows came back from source table row count query!");
					}
				} finally {
					if (stmt != null) stmt.close();
					if (con != null) con.close();
				}
			} else {
				rowCount = processCount.intValue();
			}
			
			List<MungeProcess> processes = new ArrayList<MungeProcess>();
			for (MungeProcess mp: getProject().getMungeProcessesFolder().getChildren()) {
				if (mp.getActive()) {
					processes.add(mp);
				}
			}
			
			jobSize = rowCount * processes.size();
			
			
			for (MungeProcess currentProcess: processes) {
				munger = new MungeProcessor(currentProcess, logger);
				setCurrentProcessor(munger);
				progressMessage = "Running cleanse process " + currentProcess.getName();
				logger.debug(getMessage());
				munger.call();
                checkCancelled();
				progress += munger.getProgress();
			}
			
			setCurrentProcessor(null);
			
			progressMessage = "Cleanse Engine finished successfully";
			logger.info(progressMessage);
			return EngineInvocationResult.SUCCESS;
		} catch (CancellationException cex) {
			logger.warn("Cleanse engine terminated by user");
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

	public synchronized int getProgress() {
		if (currentProcessor != null) {
			return progress + currentProcessor.getProgress();
		} else {
			return progress;
		}
	}

	public String getObjectType() {
		return DB_OBJECT_TYPE;
	}
	
	public synchronized void setCancelled(boolean cancelled) {
		super.setCancelled(cancelled);
		if (cancelled && currentProcessor != null) {
			currentProcessor.setCancelled(true);
		}
	}
	
	private synchronized void setCurrentProcessor(Processor processor) {
		currentProcessor = processor;
	}
}
