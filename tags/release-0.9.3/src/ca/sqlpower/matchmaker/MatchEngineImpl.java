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
import java.sql.SQLException;
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
import ca.sqlpower.matchmaker.munge.MungeResult;
import ca.sqlpower.util.EmailAppender;
import ca.sqlpower.util.Monitorable;

/**
 * The MatchMaker's matching engine.  Runs all of the munge steps in the correct
 * order for each row of input, then sorts the list of output results from the munging,
 * and searches for duplicates in those results.
 */
public class MatchEngineImpl extends AbstractEngine {

	private static final Logger logger = Logger.getLogger(MatchEngineImpl.class);
	
	private static final String DB_OBJECT_TYPE = "MATCH_ENGINE";

	private int jobSize;

	private int progress;

	private MungeProcessor munger;

	private MatchProcessor matcher;

	private String progressMessage;

	/**
	 * The current monitorable that is running in the engine.
	 * <p>
	 * Never ever ever EVER set the currentProcessor directly, call
	 * {@link #setCurrentProcessor(Monitorable)} instead because it
	 * needs synchronized access.
	 * 
	 * Failure to do so is punishable by death by screaming monkeys!
	 */
	private Monitorable currentProcessor;

	private int rowCount;
	
	public MatchEngineImpl(MatchMakerSession session, Project project) {
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
        
        if (!project.doesResultTableExist()) {
			throw new EngineSettingException(
					"PreCondition failed: project result table does not exist");
		}
        
        if (!project.verifyResultTableStructure()) {
			throw new EngineSettingException(
					"PreCondition failed: project result table structure incorrect");
		}
        
        if (settings.getSendEmail()) {
        	// First checks for email settings
        	if (!validateEmailSetting(context)) {
        		throw new EngineSettingException("PreCondition failed: " +
        			 	"missing email setting information, " +
        			 	"the email sender requires smtp host name!");
        	}
        	
        	// Then tries to set up the emails for each status
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
		Level oldLoggerLevel = logger.getLevel();
		logger.setLevel(getMessageLevel());
		FileAppender fileAppender = null;
		EmailAppender emailAppender = null;
		setCancelled(false);
		
		setFinished(false);
		setStarted(true);
		progress = 0;

		boolean inDebugMode = getProject().getMungeSettings().getDebug();

		try {
			progressMessage = "Checking Match Engine Preconditions";
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
			
			progressMessage = "Starting Match Engine";
			logger.info(progressMessage);
			
			rowCount = getNumRowsToProcess();
			
			List<MungeProcess> mungeProcesses = new ArrayList<MungeProcess>();
			for (MungeProcess mp: getProject().getMungeProcessesFolder().getChildren()) {
				if (mp.getActive()) {
					mungeProcesses.add(mp);
				}
			}
			
			jobSize = rowCount * mungeProcesses.size() * 2 + rowCount;
			
			MatchPool pool = new MatchPool(getProject());
			// Fill pool with pre-existing matches
			pool.findAll(null);
			
			// I've currently disabled the clear match pool option in debug mode because
			// changes should be rolled back, but if the clearing of the match
			// pool is rolled back but the engine thinks that it is cleared, it can cause
			// unique key violations when it tries to insert 'new' matches when it calls
			// pool.store() later on in this method. But if the engine is made aware of the rollback,
			// then it would be as if clear match pool wasn't selected in the first place, 
			// so I don't see the point in allowing it in debug mode for now.
			if (!inDebugMode && getProject().getMungeSettings().isClearMatchPool()) {
				int clearJobSize = pool.getPotentialMatches().size() + pool.getOrphanedMatches().size();
				jobSize += clearJobSize;
				progressMessage = "Clearing Match Pool";
				logger.info(progressMessage);
				setCurrentProcessor(pool);
				pool.clear(new Aborter());
				progress += clearJobSize;
				setCurrentProcessor(null);
			}
			
			checkCancelled();
            
			progressMessage = "Searching for matches";
			logger.info(progressMessage);
			mungeAndMatch(rowCount, mungeProcesses, pool);
			
			progressMessage = "Storing matches";
			setCurrentProcessor(pool);
			logger.info(progressMessage);
			pool.store(new Aborter(), inDebugMode);
            checkCancelled();
            progress += rowCount;
            setCurrentProcessor(null);
            
            if (inDebugMode) {
            	logger.info("In Debug Mode, so rolling back changes");
            }
            
			progressMessage = "Match Engine finished successfully";
			logger.info(progressMessage);

			return EngineInvocationResult.SUCCESS;
		} catch (CancellationException cex) {
			logger.warn("Match engine terminated by user");
			return EngineInvocationResult.ABORTED;
		} catch (Exception ex) {
			progressMessage = "Match Engine failed";
			logger.error(getMessage());
			throw new RuntimeException(ex);
		} finally {
			logger.setLevel(oldLoggerLevel);
			setFinished(true);
			
			if (emailAppender != null) {
				try {
					emailAppender.close();
				} catch (RuntimeException e) {
					logger.info("Error while sending emails! " + e.getStackTrace().toString());
				}
				logger.removeAppender(emailAppender);
			}
			if (fileAppender != null) {
				logger.removeAppender(fileAppender);
			}
		}
	
	}

	private void mungeAndMatch(int rowCount, List<MungeProcess> mungeProcesses,
			MatchPool pool) throws Exception {
		for (MungeProcess currentProcess: mungeProcesses) {
			munger = new MungeProcessor(currentProcess, logger);
			setCurrentProcessor(munger);
			progressMessage = "Running munge process " + currentProcess.getName();
			logger.debug(getMessage());
			munger.call(rowCount);
			progress += munger.getProgress();
			setCurrentProcessor(null);

			List<MungeResult> results = currentProcess.getResults();
			
			matcher = new MatchProcessor(pool, currentProcess, results, logger);
			setCurrentProcessor(matcher);
			progressMessage = "Matching munge process " + currentProcess.getName();
			logger.debug(getMessage());
			matcher.call();
            checkCancelled();
			progress += matcher.getProgress();
			setCurrentProcessor(null);
		}
	}

	private int getNumRowsToProcess() throws SQLException {
		Integer processCount = getProject().getMungeSettings().getProcessCount();
		int rowCount;
		Connection con = null;
		Statement stmt = null;
		try {
			con = getProject().getSourceTable().getParentDatabase().getDataSource().createConnection();
			
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
		if (processCount != null && processCount.intValue() > 0 && processCount.intValue() < rowCount) {
			rowCount = processCount.intValue();
		}
		return rowCount;
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
		int currentProgress;
		if (currentProcessor instanceof Processor) {
			currentProgress = progress + currentProcessor.getProgress();
		} else if (currentProcessor instanceof MatchPool) {
			float poolProgress = currentProcessor.getProgress();
			Integer poolJobSizeInteger = currentProcessor.getJobSize();
			if (poolJobSizeInteger == null) {
				currentProgress = progress;
			} else {
				float poolJobSize = poolJobSizeInteger.floatValue();
				float matchPoolProgress = poolProgress / poolJobSize * rowCount;
				currentProgress = progress + Math.round(matchPoolProgress);
			}
		} else {
			currentProgress = progress;
		}
		return currentProgress;
	}

	public String getObjectType() {
		return DB_OBJECT_TYPE;
	}
	
	@Override
	public synchronized void setCancelled(boolean cancelled) {
		super.setCancelled(cancelled);
		if (cancelled && currentProcessor != null) {
			currentProcessor.setCancelled(true);
		}
	}
	
	/**
	 * A small class that can be passed to the merge pool that tells it to stop 
	 * what its doing if the user presses cancel.
	 */
	public class Aborter {
		public void checkCancelled() {
			MatchEngineImpl.this.checkCancelled();
		}
	}
	
	private synchronized void setCurrentProcessor(Monitorable processor) {
		currentProcessor = processor;
	}
}
