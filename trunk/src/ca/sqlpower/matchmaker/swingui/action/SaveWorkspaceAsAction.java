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
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.dao.OpenSaveHandler;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;

/**
 * An action that prompts the user to select a save location for XML files and
 * saves to that location.
 * Also contains static methods and values used for opening and standard saving.
 */
public class SaveWorkspaceAsAction extends AbstractAction {
	
	/**
	 * The file extensions allowed for XML exports. The first element is the default.
	 */
	static final String[] XML_EXTENSIONS = {"dqguru", "xml"};

	private static final Logger logger = Logger.getLogger(SaveWorkspaceAsAction.class);
	
	private final MatchMakerSwingSession session;
	
	public SaveWorkspaceAsAction(MatchMakerSwingSession session) {
		super("Save As...");
		this.session = session;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		doSaveAs(selectFileName(session), session);
	}
	
	
	/**
	 * Asks the user to choose a file. Relies on swing.
	 * @return the chosen file
	 */
	public static File selectFileName(MatchMakerSwingSession session) {
		JFileChooser saveFileChooser = new JFileChooser();
		FileNameExtensionFilter dqextension = new FileNameExtensionFilter("DQguru XML Export", XML_EXTENSIONS);
		String backupString = null;
		String tempString;
		boolean confirmOverwrite = false;
		String selectedFileString = null;
		File selectedFile = null;
		File backupFile = null;
		saveFileChooser.setAcceptAllFileFilterUsed(false);
		saveFileChooser.addChoosableFileFilter(dqextension);
		
		do { // this continues until the user decides where to write the file
			int chosenReturnType = saveFileChooser.showSaveDialog(session.getFrame());
			if (chosenReturnType != JFileChooser.APPROVE_OPTION) return null;
			selectedFileString = saveFileChooser.getSelectedFile().getAbsolutePath().toString();
			for (String ext : XML_EXTENSIONS) {
				String extp = "." + ext;
				if (selectedFileString.indexOf(extp) == selectedFileString.length() - extp.length()) {
					backupString = selectedFileString.substring(0, selectedFileString.length() - extp.length()) + "~";
				}
			}
			if (backupString == null) {
				selectedFileString += "." + XML_EXTENSIONS[0];
				backupString = selectedFileString + "~";
			}
			
			selectedFile = new File(selectedFileString);
			backupFile = new File(backupString);
			
			if (!selectedFile.exists()) break;
			
			int choice = JOptionPane.showConfirmDialog
					(session.getFrame(), "The file " + selectedFile.toString() + 
							(backupFile.exists()? " and its corresponding backup file " : " ") +
							"will be overwritten. Continue?");
			if (choice == JOptionPane.CANCEL_OPTION) return null;
			if (choice == JOptionPane.YES_OPTION) confirmOverwrite = true;
		} while (confirmOverwrite == false);
		
		return selectedFile;
	}
	
	/**
	 * 
	 * @param selectedFile The location to save the file; should always check if empty.
	 * @return Whether the save was successful.
	 */
	public static boolean doSaveAs(File selectedFile, MatchMakerSwingSession session) {
		return OpenSaveHandler.doSaveAs(selectedFile, session);
	}
}
