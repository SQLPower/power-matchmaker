/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.swingui.engine;

import java.awt.Component;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.Action;
import javax.swing.JTextArea;
import javax.swing.text.Document;

import org.apache.log4j.Appender;

import ca.sqlpower.matchmaker.EngineSettingException;
import ca.sqlpower.matchmaker.MatchEngineImpl;
import ca.sqlpower.matchmaker.MatchMakerEngine;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.SourceTableException;
import ca.sqlpower.matchmaker.swingui.MMSUtils;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.DocumentAppender;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSwingWorker;

/**
 * A SPSwingWorker implementation that runs a MatchMakerEngine.
 */
class EngineWorker extends SPSwingWorker {
	
	/**
	 * The maximum number of characters that will be displayed in the engine
	 * output {@link JTextArea}. This is to keep the buffer from getting too
	 * large and consuming too much memory during an engine run. If the user
	 * wants to view the whole log output, they can just open up the text file
	 * or use the 'Show Log' button.
	 */
	private static final int LOG_OUTPUT_CHAR_LIMIT = 50000;

	/**
	 * The MatchMakerEngine that the worker will run
	 */
	private final MatchMakerEngine engine;
	
	/**
	 * A document appender to append the engine output onto the engineOutputDoc
	 */
	private Appender appender;
	
	/**
	 * A Document to contain the engine output
	 */
	private final Document engineOutputDoc;
	
	/**
	 * A parent Component that would own the exception dialog if it gets displayed
	 */
	private Component parentComponent;
	
	
	/**
	 * Action to re-enable after engine has completed.
	 */
	private Action action;
	
	/**
	 *	The session that this EngineWorker is from.
	 */
	private MatchMakerSwingSession session;
	
	private Project project;
	
	/**
	 * @param engine The MatchMakerEngine that the worker will run
	 * @param outputPanel The EngineOutputPanel where the engine will display it's output, 
	 * including the log output and progress bar
	 * @param registry The SwingWorkerRegistry that this worker will register itself with
	 * @throws EngineSettingException
	 * @throws SQLObjectException
	 * @throws SourceTableException 
	 */
	public EngineWorker(MatchMakerEngine engine, EngineOutputPanel outputPanel,
			MatchMakerSwingSession session, Project project, Action action) throws EngineSettingException, SQLObjectException, SourceTableException {
		super(session);
		this.session = session;
		this.project = project;
		this.engine = engine;
		this.engineOutputDoc = outputPanel.getOutputDocument();
		this.parentComponent = outputPanel.getOutputComponent().getTopLevelAncestor();
		this.action = action;
		ProgressWatcher.watchProgress(outputPanel.getProgressBar(), engine);
	}
	
	@Override
	public void doStuff() throws EngineSettingException, IOException, SQLException, SourceTableException, InterruptedException {
		SQLDatabase db = null;
		if (engine instanceof MatchEngineImpl) {
			 db = MMSUtils.setupProjectGraphTable(session, project, engine.getLogger());
		}
        
	    project.acquireEngineLock(engine);
	    try {
	        appender = new DocumentAppender(engineOutputDoc, true, LOG_OUTPUT_CHAR_LIMIT);
	        engine.getLogger().addAppender(appender);
	        engine.call();
	    } finally {
	        project.releaseEngineLock(engine);
	    }

	    if (engine instanceof MatchEngineImpl) {
	    	MMSUtils.populateProjectGraphTable(project, engine.getLogger(), db);
	    }
	}

	@Override
	public void cleanup() throws Exception {
		if (getDoStuffException() != null) {
			MMSUtils.showExceptionDialog(parentComponent, "Error during engine run", getDoStuffException());
			engine.getLogger().error("Error during engine run", getDoStuffException());
		}
		engine.getLogger().removeAppender(appender);
	}
	
}