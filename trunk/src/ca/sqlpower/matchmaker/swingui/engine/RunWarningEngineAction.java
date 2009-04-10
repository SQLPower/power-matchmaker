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

package ca.sqlpower.matchmaker.swingui.engine;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import ca.sqlpower.matchmaker.MatchMakerEngine;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.swingui.SPSwingWorker;

/**
 * A subclass of {@link RunEngineAction} that displays a warning
 * {@link JOptionPane} confirm dialog that requires the user to choose the
 * {@link JOptionPane#YES_OPTION} to continue running the engine. A message
 * string containing the reason why the user should be careful before running
 * this engine is passed in through the constructor and is displayed in the
 * confirm dialog when this action is performed.
 */
public class RunWarningEngineAction extends RunEngineAction {

	private MatchMakerSwingSession session;
	private String message;
	
	/**
	 * Sets up the {@link RunWarningEngineAction}.
	 * 
	 * @param session
	 *            The session that this action will register a
	 *            {@link SPSwingWorker} with.
	 * @param project
	 *            The {@link Project} that this engine will be running on.
	 * @param engine
	 *            The engine that this action will be running
	 * @param name
	 *            The name of this action. It will be displayed on whatever
	 *            control (ex. button) is used to run the engine.
	 * @param engineOutputPanel
	 *            The {@link EngineOutputPanel} that the engine output will be
	 *            displayed on.
	 * @param editorPane
	 *            The {@link EngineSettingsPanel} that this
	 *            {@link RunEngineAction} is being run from.
	 * @param startAction
	 *            An action to be run when the engine is started. Can be null.
	 * @param finishAction
	 *            An action to be run when the engine is finished. Can be null.
	 * @param message
	 *            The warning message to be displayed on the confirmation dialog
	 *            before the engine is run. It should explain why the user
	 *            should be warned and should be cautious before running this
	 *            engine.
	 */
	public RunWarningEngineAction(MatchMakerSwingSession session,
			Project project, MatchMakerEngine engine, String name,
			EngineOutputPanel engineOutputPanel,
			EngineSettingsPanel editorPane, Runnable startAction,
			Runnable finishAction, String message) {
		super(session, project, engine, name, engineOutputPanel, editorPane,
				startAction, finishAction);
		this.session = session;
		this.message = message;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int answer = JOptionPane.showConfirmDialog(session.getFrame(), 
				message + "\nAre you sure you want to continue?", 
				"", 
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
		if (answer == JOptionPane.YES_OPTION) {
			super.actionPerformed(e);
		}
	}
}
