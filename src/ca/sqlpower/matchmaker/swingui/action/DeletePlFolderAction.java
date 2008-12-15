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

package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.swingui.DeleteFolderDialog;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;

public class DeletePlFolderAction extends AbstractAction {

	private MatchMakerSwingSession session;
	private DeleteFolderDialog dialog;
	
	public DeletePlFolderAction(MatchMakerSwingSession swingSession, String name, PlFolder folder) {
		super(name);
		session = swingSession;
		this.dialog = new DeleteFolderDialog(folder,session.getFrame(),session);
		this.dialog.buildUI();
	}

	public void actionPerformed(ActionEvent e) {
		dialog.setVisible(true);
		session.setCurrentEditorComponent(null);
	}
}
