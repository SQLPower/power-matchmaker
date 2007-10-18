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

package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.swingui.SPSUtils;

public class ProjectImportAction extends AbstractAction {

	private static final Logger logger = Logger.getLogger(ProjectImportAction.class);
    
    private final MatchMakerSwingSession swingSession;
	private JFrame owningFrame;

	public ProjectImportAction(MatchMakerSwingSession swingSession, JFrame owningFrame) {
		super("Import",
				SPSUtils.createIcon( "general/Import",
                "Import"));
		putValue(SHORT_DESCRIPTION, "Import Project");
        this.swingSession = swingSession;
		this.owningFrame = owningFrame;
	}


	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser(
				swingSession.getLastImportExportAccessPath());
		fc.setFileFilter(SPSUtils.XML_FILE_FILTER);
		fc.setDialogTitle("Import Project");
	
		File importFile = null;
		int fcChoice = fc.showOpenDialog(owningFrame);

		if (fcChoice == JFileChooser.APPROVE_OPTION) {
			importFile = fc.getSelectedFile();
			swingSession.setLastImportExportAccessPath(
					importFile.getAbsolutePath());

			Project project = new Project();
			BufferedInputStream in = null;
			try {
				in = new BufferedInputStream(new FileInputStream(importFile));
                throw new RuntimeException("Import is not currently implemented");
			} catch (FileNotFoundException e1) {
				SPSUtils.showExceptionDialogNoReport(owningFrame,
						"The file " + importFile.getName() + " cannot be found", e1 );
			} catch (Exception e1) {
				SPSUtils.showExceptionDialogNoReport(owningFrame, 
						"There was an exception while doing the import", e1);
			} finally {
			    try {
			        if (in != null) in.close();
			    } catch (IOException ex) {
			        logger.error(
                            "Couldn't close input file. Swallowing this " +
                            "exception to preserve the possible original one", ex);
			    }         
            }

			if ( project == null ) {
				JOptionPane.showConfirmDialog(null,
						"Unable to read project ID from XML",
						"XML File error",
						JOptionPane.ERROR_MESSAGE);
				return;
			} else {

				Project project2 = swingSession.getProjectByName(project.getName());
				if ( project2 != null ) {
					logger.debug("Project ["+project2.getName()+"] exists");
					int option = JOptionPane.showConfirmDialog(
							swingSession.getFrame(),
		                    "Project ["+project2.getName()+"] Exists! Do you want to overwrite it?",
		                    "Project ["+project2.getName()+"] Exists!",
		                    JOptionPane.OK_CANCEL_OPTION );
					if ( option != JOptionPane.OK_OPTION ) {
						return;
					}
					swingSession.delete(project2);
				}

				if ( project.getParent() != null ) {
					List<PlFolder> folders = swingSession.getCurrentFolderParent().getChildren();
					for ( PlFolder<Project> folder : folders ) {
						if ( folder.getName().equals(((PlFolder<Project>)project.getParent()).getName())) {
							logger.debug("Folder ["+folder.getName()+"] exists");
							swingSession.move(project,folder);
							break;
						}
					}
				}
				logger.debug("Saving Project:" + project.getName());
				swingSession.save(project);
			}

		}
	}


}