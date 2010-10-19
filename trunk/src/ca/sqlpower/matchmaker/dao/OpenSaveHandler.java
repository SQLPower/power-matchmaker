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

package ca.sqlpower.matchmaker.dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

import ca.sqlpower.dao.SPPersistenceException;
import ca.sqlpower.dao.SPPersisterListener;
import ca.sqlpower.dao.XMLPersister;
import ca.sqlpower.dao.XMLPersisterReader;
import ca.sqlpower.matchmaker.MMRootNode;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.enterprise.MatchMakerPersisterSuperConverter;
import ca.sqlpower.matchmaker.enterprise.MatchMakerSessionPersister;

public class OpenSaveHandler {
	private static Logger logger = Logger.getLogger(OpenSaveHandler.class);
	
	public static void doOpen(File openFile, MatchMakerSession session) {
		FileReader reader;
		try {
			reader = new FileReader(openFile);
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(ex);
		}
		MatchMakerPersisterSuperConverter converter = new MatchMakerPersisterSuperConverter(session.getContext().getPlDotIni(), session.getRootNode());
		MatchMakerSessionPersister mmPersister = new MatchMakerSessionPersister("XML Import Persister", session.getRootNode(), converter);
		mmPersister.setWorkspaceContainer(session);
		
		XMLPersisterReader xmlReader = new XMLPersisterReader(reader, mmPersister, session.getUpgradePersisterManager(), "matchmaker-project");
		
		session.getRootNode().setMagicEnabled(false);
		try {
			xmlReader.read();
		} catch (SPPersistenceException ex) {
			throw new RuntimeException(ex);
		} finally {
			session.getRootNode().setMagicEnabled(true);
		}
	}
	
	public static boolean doSaveAs(File selectedFile, MatchMakerSession session) {
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
			//JOptionPane.showMessageDialog(session.getFrame(), cannotWriteFile);
			return false;
		} catch (IOException ex) {
			//JOptionPane.showMessageDialog(session.getFrame(), cannotWriteFile);
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
