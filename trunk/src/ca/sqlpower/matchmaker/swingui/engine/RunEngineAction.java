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

package ca.sqlpower.matchmaker.swingui.engine;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.matchmaker.MatchMakerEngine;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.swingui.MMSUtils;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.swingui.event.TaskTerminationEvent;
import ca.sqlpower.swingui.event.TaskTerminationListener;

/**
 * This action is used to run the match engine. It also is responsible
 * for constructing the user interface that deals with engine output.
 */
class RunEngineAction extends AbstractAction implements TaskTerminationListener{
	
	/**
	 * The project the engine is running in.
	 */
	private Project project;
	
	/**
	 * The application's swing session.
	 */
	private MatchMakerSwingSession session;
	
	/**
	 * The panel for outputting the engine output
	 */
	private EngineOutputPanel engineOutputPanel;
	
	/**
	 * The MatchMakerEngine to be run
	 */
	private MatchMakerEngine engine;
	
	/**
	 * The EngineSettingsPanel (ideally the engine panel) that the engine is being executed from.
	 */
	private EngineSettingsPanel editorPane;
	
	/**
	 * The "action" that is run when the engine finishes
	 */
	private Runnable finishAction;
	
	/**
	 * The "action" that is run when the engine starts
	 */
	private Runnable startAction;

	
	/**
	 * Sets up the action.
	 * 
	 * @param startAction An action to be run when the engine is started. Can be null.
	 * @param finishAction An action to be run when the engine is finished. Can be null.
	 */
	public RunEngineAction(MatchMakerSwingSession session, Project project, MatchMakerEngine engine, String name, 
			EngineOutputPanel engineOutputPanel, EngineSettingsPanel editorPane, Runnable startAction, 
			Runnable finishAction) {
		super(name);
		this.project = project;
		this.session = session;
		this.engineOutputPanel = engineOutputPanel;
		this.engine = engine;
		this.editorPane = editorPane;
		this.finishAction = finishAction;
		this.startAction = startAction;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (startAction != null) {
			startAction.run();
		}
		editorPane.applyChanges();
		
		engineOutputPanel.getProgressBar().getModel().setValue(0);
		try {
			EngineWorker w = new EngineWorker(engine, engineOutputPanel, session, project, this);
			w.addTaskTerminationListener(this);
			new Thread(w).start();
		} catch (Exception ex) {
			MMSUtils.showExceptionDialog(editorPane.getPanel(), "Engine error", ex);
		}
	}

	public void taskFinished(TaskTerminationEvent e) {
		if (finishAction != null) {
			finishAction.run();
		}
	}
}