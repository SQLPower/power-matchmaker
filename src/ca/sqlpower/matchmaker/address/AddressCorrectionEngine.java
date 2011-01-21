/*
 * Copyright (c) 2009, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.address;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import ca.sqlpower.matchmaker.AbstractEngine;
import ca.sqlpower.matchmaker.EngineInvocationResult;
import ca.sqlpower.matchmaker.EngineSettingException;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MungeSettings;
import ca.sqlpower.matchmaker.Processor;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.SourceTableException;
import ca.sqlpower.matchmaker.munge.AddressCorrectionMungeProcessor;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.util.Monitorable;

public class AddressCorrectionEngine extends AbstractEngine {

	public enum AddressCorrectionEngineMode implements EngineMode {
		ADDRESS_CORRECTION_PARSE_AND_CORRECT_ADDRESSES,
		ADDRESS_CORRECTION_WRITE_BACK_ADDRESSES
	}
	
	private final Logger logger;
	
	private String message;
	
	private int progress = 0;
	
	private int jobSize = 0;
	
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

	private int numRowsToProcess;
	
	private AddressCorrectionEngineMode mode;
	
	public AddressCorrectionEngine(MatchMakerSession session, Project project, AddressCorrectionEngineMode mode) {
		logger = Logger.getLogger(AddressCorrectionEngine.class + "." + project.getName());
		this.setSession(session);
		this.setProject(project);
		this.mode = mode;
	}
	
	public void checkPreconditions() throws EngineSettingException,
			SQLObjectException, SourceTableException {
		// No preconditions to check yet
	}

	@Override
	public EngineInvocationResult call() throws EngineSettingException,
			SourceTableException {
		
		logger.debug("This engine is running in " + mode + " mode");
		
		double startTime = System.currentTimeMillis();
		
		Level oldLoggerLevel = logger.getLevel();
		logger.setLevel(getMessageLevel());
		setCancelled(false);
		
		setFinished(false);
		setStarted(true);
		progress = 0;

		int numValid = 0;
		int numCorrectable = 0;
		int numIncorrectable = 0;
		int numWritten = 0;
		
		try {
			String logFilePath = getProject().getMungeSettings().getLog().getAbsolutePath();
			boolean appendToFile = getProject().getMungeSettings().getAppendToLog();
			FileAppender fileAppender = new FileAppender(new PatternLayout("%d %p %m\n"), logFilePath, appendToFile);
			logger.addAppender(fileAppender);

			List<MungeProcess> mungeProcesses = new ArrayList<MungeProcess>();
			for (MungeProcess mp: getProject().getMungeProcessesFolder().getChildren()) {
				if (mp.getActive()) {
					mungeProcesses.add(mp);
				}
			}

			numRowsToProcess = getNumRowsToProcess();
			jobSize = numRowsToProcess * (mungeProcesses.size());
			// If we're going to be validating addresses, then add some more to
			// the jobsize for writing the invalid addresses to the result table
			if (!getProject().getMungeSettings().isAutoWriteAutoValidatedAddresses()) {
				jobSize += numRowsToProcess;
			}

			if (getProject().getMungeSettings().getDebug()) {
				message = "Engine is running in debug mode so changes will be rolled back";
				logger.info(message);
			}
			
			message = "Starting Address Correction Engine";
			logger.info(message);
			
			checkCancelled();
			
			message = "Loading previously invalidated addresses";
			logger.info(message);
			
			AddressPool pool = new AddressPool(getProject());
			
			if (mode == AddressCorrectionEngineMode.ADDRESS_CORRECTION_PARSE_AND_CORRECT_ADDRESSES && getProject().getMungeSettings().isClearMatchPool()) {
				message = "Clearing Address Pool";
				logger.info(message);
				pool.clear();
			} else {
				pool.load(logger);
			}

			if (mode == AddressCorrectionEngineMode.ADDRESS_CORRECTION_PARSE_AND_CORRECT_ADDRESSES) {
				message = "Searching for invalid addresses";
				logger.info(message);
			}
			
			MungeSettings settings = getProject().getMungeSettings();
			
			for (MungeProcess process: mungeProcesses) {
				checkCancelled();
				message = "Running transformation " + process.getName();
				logger.debug(getMessage());
				AddressCorrectionMungeProcessor munger;

				munger = new AddressCorrectionMungeProcessor(process, pool, settings,
						mode,
						logger);
				
				setCurrentProcessor(munger);
				message = "Running transformation " + process.getName();
				logger.debug(getMessage());
				munger.call(numRowsToProcess);
				progress += munger.getProgress();
				setCurrentProcessor(null);
				
				numValid += munger.getNumValid();
				numCorrectable += munger.getNumCorrectable();
				numIncorrectable += munger.getNumIncorrectable();
				numWritten += munger.getNumWritten();
			}
			
			setCurrentProcessor(pool);
			
			if (mode == AddressCorrectionEngineMode.ADDRESS_CORRECTION_PARSE_AND_CORRECT_ADDRESSES) {
				logger.info("Storing invalid addresses into the result table");
			} else {
				logger.info("Storing corrected addresses into the source table");
			}
			
			pool.store(getLogger(), settings.isUseBatchExecution(), settings.getDebug());
			progress += pool.getProgress();
			setCurrentProcessor(null);
			
			message = "Address Correction Engine finished successfully";
			logger.info(message);
			return EngineInvocationResult.SUCCESS;
		} catch (CancellationException cex) {
			logger.warn("Match engine terminated by user");
			return EngineInvocationResult.ABORTED;
		} catch (Exception e) {
			logger.error("Address Correction Engine Failed", e);
			return EngineInvocationResult.FAILURE;
		} finally {
			int rowsProcessed = Math.min(numRowsToProcess, getProgress());
			logger.setLevel(oldLoggerLevel);
			setFinished(true);
			double time = System.currentTimeMillis() - startTime;
			if (mode == AddressCorrectionEngineMode.ADDRESS_CORRECTION_PARSE_AND_CORRECT_ADDRESSES) {
				logger.info("Number of Valid Addresses found: " + numValid);
				logger.info("Number of Correctable Addresses found: " + numCorrectable);
				logger.info("Number of Incorrectable Addresses found: " + numIncorrectable);
			}
			logger.info("Number of Source Addresses Modified: " + numWritten);
			logger.info(String.format(
									"Address Correction Engine processed %d records in %.3f seconds (%.2f per second)",
									rowsProcessed, time / 1000,
									(rowsProcessed * 1000 / time)));
		}
	}
	
	@Override
	public synchronized void setCancelled(boolean cancelled) {
		super.setCancelled(cancelled);
		if (cancelled && currentProcessor != null) {
			currentProcessor.setCancelled(true);
		}
	}
	
	public Logger getLogger() {
		return logger;
	}

	public String getObjectType() {
		return null;
	}
	
	private synchronized void setCurrentProcessor(Monitorable processor) {
		currentProcessor = processor;
	}
	
	@Override
	public Integer getJobSize() {
		logger.debug("Address Correction Engine jobsize is: " + jobSize);
		return jobSize;
	}
	
	public synchronized int getProgress() {
		int currentProgress;
		if (currentProcessor instanceof Processor) {
			currentProgress = progress + currentProcessor.getProgress();
		} else if (currentProcessor instanceof AddressPool) {
			float poolProgress = currentProcessor.getProgress();
			Integer poolJobSizeInteger = currentProcessor.getJobSize();
			if (poolJobSizeInteger == null) {
				currentProgress = progress;
			} else {
				float poolJobSize = poolJobSizeInteger.floatValue();
				float matchPoolProgress = poolProgress / poolJobSize * numRowsToProcess;
				currentProgress = progress + Math.round(matchPoolProgress);
			}
		} else {
			currentProgress = progress;
		}
		logger.debug("Address Correction Engine progress is: " + currentProgress);
		return currentProgress;
	}
}
