/*
 * Copyright (c) 2010, SQL Power Group Inc.
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
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.dao.OpenSaveHandler;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;

/**
 * The action for opening a workspace from a DQguru XML file. Creates a new session.
 */
public class OpenWorkspaceAction extends AbstractAction {
	
	private static final Logger logger = Logger.getLogger(OpenWorkspaceAction.class);
	
	private final MatchMakerSwingSession session;

	public OpenWorkspaceAction(MatchMakerSwingSession session) {
		super("Open");
		this.session = session;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String[] extensionsWithBackups = new String[SaveWorkspaceAsAction.XML_EXTENSIONS.length * 2];
		int i=0;
		for (String ext : SaveWorkspaceAsAction.XML_EXTENSIONS) {
			extensionsWithBackups[i] = ext + "~";
			extensionsWithBackups[SaveWorkspaceAsAction.XML_EXTENSIONS.length+i++] = ext;
		}

		JFileChooser openFileChooser = new JFileChooser();
		FileNameExtensionFilter dqextension = new FileNameExtensionFilter(
				"DQguru XML Export and Backup", extensionsWithBackups);
		openFileChooser.setAcceptAllFileFilterUsed(false);
		openFileChooser.addChoosableFileFilter(dqextension);
		int chosenReturnType = openFileChooser.showOpenDialog(session.getFrame());
		if (chosenReturnType != JFileChooser.APPROVE_OPTION) return;
		
		MatchMakerSwingSession newSession = session.getContext().createDefaultSession();
		
		OpenSaveHandler.doOpen(openFileChooser.getSelectedFile(), newSession);
		
		newSession.showGUI();
		newSession.setSavePoint(openFileChooser.getSelectedFile());
	}

}
