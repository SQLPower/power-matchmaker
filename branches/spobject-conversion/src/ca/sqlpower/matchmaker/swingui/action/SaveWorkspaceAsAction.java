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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;

import ca.sqlpower.dao.SPPersistenceException;
import ca.sqlpower.dao.SPPersisterListener;
import ca.sqlpower.dao.XMLPersister;
import ca.sqlpower.matchmaker.MMRootNode;
import ca.sqlpower.matchmaker.enterprise.MatchMakerPersisterSuperConverter;
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
	static final String[] XML_EXTENSIONS = {".dqguru", ".xml"};

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
	 * Asks the user to choose a file.
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
		saveFileChooser.addChoosableFileFilter(dqextension);
		
		do { // this continues until the user decides where to write the file
			int chosenReturnType = saveFileChooser.showSaveDialog(session.getFrame());
			if (chosenReturnType != JFileChooser.APPROVE_OPTION) return null;
			selectedFileString = saveFileChooser.getSelectedFile().getAbsolutePath().toString();
			for (String ext : XML_EXTENSIONS) {
				if (selectedFileString.indexOf(ext) == selectedFileString.length() - ext.length()) {
					backupString = selectedFileString.substring(0, selectedFileString.length() - ext.length()) + "~";
				}
			}
			if (backupString == null) {
				selectedFileString += XML_EXTENSIONS[0];
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
		if (selectedFile == null) return false;
		String tempString = selectedFile.toString() + "_tmp";
		File backupFile = new File(selectedFile.toString() + "~");
		
		MMRootNode rootNode = session.getRootNode();
		File tempOutFile = new File(tempString);
		FileOutputStream fileOutputStream;
		String cannotWriteFile = "A temporary file could not be written to " +
				selectedFile.getParentFile() + ".";
		
		try {
			while (tempOutFile.exists()) {
				tempString += "~";
				tempOutFile = new File(tempString);
			}
			tempOutFile.createNewFile();
			fileOutputStream = new FileOutputStream(tempOutFile);
		} catch (FileNotFoundException ex) {
			JOptionPane.showMessageDialog(session.getFrame(), cannotWriteFile);
			return false;
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(session.getFrame(), cannotWriteFile);
			return false;
		}
		
		XMLPersister xmlPersister = new XMLPersister(fileOutputStream, MMRootNode.class.getName(), "matchmaker-project");
		XMLPersister.setUpgradePersisterManager(session.getUpgradePersisterManager());
		MatchMakerPersisterSuperConverter converter = new MatchMakerPersisterSuperConverter(session.getContext().getPlDotIni(), rootNode);
		SPPersisterListener spPersisterListener = new SPPersisterListener(xmlPersister, converter);
		try {
			spPersisterListener.persistObjectInterleaveProperties(rootNode, 0, true, xmlPersister);
		} catch (SPPersistenceException ex) {
			throw new RuntimeException("Couldn't persist state", ex);
		}
		try {
			fileOutputStream.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		
		// Do the rename dance.
        // This is a REALLY bad place for failure (especially if we've made the user wait several hours to save
        // a large project), so we MUST check failures from renameto (both places!)
        boolean fstatus = false;
        fstatus = backupFile.delete();
        logger.debug("deleting backup~ file: " + fstatus);

        // If this is a brand new project, the old file does not yet exist, no point trying to rename it.
        // But if it already existed, renaming current to backup must succeed, or we give up.
        if (selectedFile.exists()) {
            fstatus = selectedFile.renameTo(backupFile);
            logger.debug("rename current file to backupFile: " + fstatus);
            if (!fstatus) {
                throw new RuntimeException(
                        "Cannot rename current file to backup. The new project is temporarily stored at " 
                        + tempOutFile.toString() + ". The file " + selectedFile.toString()
                        + " has not been altered and contains the old project.");
            }
        }
        fstatus = tempOutFile.renameTo(selectedFile);
        logger.debug("rename tempOutFile to current file: " + fstatus);
        if (!fstatus) {
            throw new RuntimeException(
                    "Cannot rename temporary file to final output. The new project is temporarily stored at " 
                    + tempOutFile.toString() + ". The file " + backupFile.toString()
                    + " contains the old project.");
        }
        
        return true;
	}
}
