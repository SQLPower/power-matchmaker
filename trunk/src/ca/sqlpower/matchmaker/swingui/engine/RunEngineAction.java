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

package ca.sqlpower.matchmaker.swingui.engine;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.matchmaker.MatchMakerEngine;
import ca.sqlpower.matchmaker.swingui.EditorPane;
import ca.sqlpower.matchmaker.swingui.MMSUtils;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;

/**
 * This action is used to run the match engine. It also is responsible
 * for constructing the user interface that deals with engine ouput.
 */
class RunEngineAction extends AbstractAction {
	
	/**
	 * The application swing session
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
	 * The EditorPane (ideally the engine panel) that the engine is being executed from.
	 */
	private EditorPane editorPane;
	
	public RunEngineAction(MatchMakerSwingSession session, MatchMakerEngine engine, String name, EngineOutputPanel engineOutputPanel, EditorPane editorPane) {
		super(name);
		this.session = session;
		this.engineOutputPanel = engineOutputPanel;
		this.engine = engine;
		this.editorPane = editorPane;
	}
	
	public void actionPerformed(ActionEvent e) {
		editorPane.doSave();
		try {
			EngineWorker w = new EngineWorker(engine, engineOutputPanel.getOutputDocument(), engineOutputPanel.getProgressBar(), session);
			new Thread(w).start();
		} catch (Exception ex) {
			MMSUtils.showExceptionDialog(engineOutputPanel.getOutputComponent(), "Engine error", ex);
			return;
		}
	}
}