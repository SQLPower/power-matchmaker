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

public class SaveProjectAction extends AbstractAction {

	private static final Logger logger = Logger.getLogger(SaveProjectAction.class);
	
	private final MatchMakerSwingSession session;
	
	public SaveProjectAction(MatchMakerSwingSession session) {
		super("Save");
		this.session = session;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser saveFileChooser = new JFileChooser();
		FileNameExtensionFilter dqextension = new FileNameExtensionFilter("DQGuru XML Export", ".xml", ".dqguru");
		saveFileChooser.addChoosableFileFilter(dqextension);
		int chosenReturnType = saveFileChooser.showSaveDialog(session.getFrame());
		if (chosenReturnType != JFileChooser.APPROVE_OPTION) return;
		
		File selectedFile = saveFileChooser.getSelectedFile();
		boolean confirmOverwrite = false;
		
		while (selectedFile.exists() && confirmOverwrite == false) {
			int choice = JOptionPane.showConfirmDialog
					(session.getFrame(), "File " + selectedFile.toString() + " already exists." +
					"Do you want to overwrite it?");
			if (choice == JOptionPane.CANCEL_OPTION) return;
			if (choice == JOptionPane.YES_OPTION) confirmOverwrite = true;
			if (choice == JOptionPane.NO_OPTION) {
				chosenReturnType = saveFileChooser.showSaveDialog(session.getFrame());
				if (chosenReturnType != JFileChooser.APPROVE_OPTION) return;
				selectedFile = saveFileChooser.getSelectedFile();
			}
		}

		MMRootNode rootNode = session.getRootNode();
		File tempOutFile = new File(selectedFile.getParentFile(), selectedFile.getName() + "_tmp.dqguru");
		FileOutputStream fileOutputStream;
		String cannotWriteFile = "A temporary file could not be written to " +
				selectedFile.getParentFile() + ".";
		try {
			if (tempOutFile.exists()) {
				int choice = JOptionPane.showConfirmDialog(session.getFrame(), "A previous temporary backup " +
						"file will be overwritten. Continue?","",JOptionPane.YES_NO_OPTION);
				if (choice == JOptionPane.NO_OPTION) return;
			} else {
				tempOutFile.createNewFile();
			}
			fileOutputStream = new FileOutputStream(tempOutFile);
		} catch (FileNotFoundException ex) {
			JOptionPane.showMessageDialog(session.getFrame(), cannotWriteFile);
			return;
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(session.getFrame(), cannotWriteFile);
			return;
		}
		
		XMLPersister xmlPersister = new XMLPersister(fileOutputStream, MMRootNode.class.getName());
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
		File backupFile = new File(selectedFile.getParentFile(), selectedFile.getName() + "~");
		
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
		
		
	}
}
