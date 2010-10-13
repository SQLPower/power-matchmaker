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
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;

import ca.sqlpower.dao.SPPersistenceException;
import ca.sqlpower.dao.XMLPersisterReader;
import ca.sqlpower.matchmaker.enterprise.MatchMakerPersisterSuperConverter;
import ca.sqlpower.matchmaker.enterprise.MatchMakerSessionPersister;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;

public class OpenProjectAction extends AbstractAction {
	
	private static final Logger logger = Logger.getLogger(OpenProjectAction.class);
	
	private final MatchMakerSwingSession session;

	public OpenProjectAction(MatchMakerSwingSession session) {
		super("Open");
		this.session = session;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser openFileChooser = new JFileChooser();
		FileNameExtensionFilter dqextension = new FileNameExtensionFilter(
				"DQguru XML Export", ".xml", ".dqguru");
		openFileChooser.addChoosableFileFilter(dqextension);
		int chosenReturnType = openFileChooser.showOpenDialog(session.getFrame());
		if (chosenReturnType != JFileChooser.APPROVE_OPTION) return;
		
		MatchMakerSwingSession newSession = session.getContext().createDefaultSession();
		
		FileReader reader;
		try {
			reader = new FileReader(openFileChooser.getSelectedFile());
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(ex);
		}
		MatchMakerPersisterSuperConverter converter = new MatchMakerPersisterSuperConverter(session.getContext().getPlDotIni(), newSession.getRootNode());
		MatchMakerSessionPersister mmPersister = new MatchMakerSessionPersister("XML Import Persister", newSession.getRootNode(), converter);
		mmPersister.setWorkspaceContainer(newSession);
		
		XMLPersisterReader xmlReader = new XMLPersisterReader(reader, mmPersister, newSession.getUpgradePersisterManager(), "matchmaker-project");
		
		try {
			xmlReader.read();
		} catch (SPPersistenceException ex) {
			throw new RuntimeException(ex);
		}
		
		newSession.showGUI();
	}

}
