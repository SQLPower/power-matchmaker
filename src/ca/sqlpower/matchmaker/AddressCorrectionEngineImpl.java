/*
 * Copyright (c) 2009, SQL Power Group Inc.
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
import java.util.concurrent.CancellationException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeProcessor;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.util.Monitorable;

public class AddressCorrectionEngineImpl extends AbstractEngine {

	private final Logger logger;
	
	private String message;
	
	private int progress = 0;
	
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
	
	public AddressCorrectionEngineImpl(MatchMakerSession session, Project project) {
		logger = Logger.getLogger(AddressCorrectionEngineImpl.class + "." + project.getName());
		this.setSession(session);
		this.setProject(project);
	}
	
	public void checkPreconditions() throws EngineSettingException,
			SQLObjectException, SourceTableException {
		// No preconditions to check yet
	}

	@Override
	public EngineInvocationResult call() throws EngineSettingException,
			SourceTableException {
		Level oldLoggerLevel = logger.getLevel();
		logger.setLevel(getMessageLevel());
		setCancelled(false);
		
		setFinished(false);
		setStarted(true);
		progress = 0;

		try {
			List<MungeProcess> mungeProcesses = new ArrayList<MungeProcess>();
			for (MungeProcess mp: getProject().getMungeProcessesFolder().getChildren()) {
				if (mp.getActive()) {
					mungeProcesses.add(mp);
				}
			}

			message = "Starting Address Correction Engine";
			logger.info(message);
			
			checkCancelled();
			
			message = "Searching for invalid addresses";
			logger.info(message);
			for (MungeProcess process: mungeProcesses) {
				checkCancelled();
				message = "Running munge process " + process.getName();
				logger.debug(getMessage());
				MungeProcessor munger = new MungeProcessor(process, logger);
				setCurrentProcessor(munger);
				message = "Running munge process " + process.getName();
				logger.debug(getMessage());
				munger.call();
				
				setCurrentProcessor(null);
			}
			
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
			logger.setLevel(oldLoggerLevel);
			setFinished(true);
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
}
