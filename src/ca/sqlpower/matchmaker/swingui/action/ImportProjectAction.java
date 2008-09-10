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

import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.dao.xml.ProjectDAOXML;
import ca.sqlpower.matchmaker.swingui.MMSUtils;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.swingui.SPSUtils;

public class ImportProjectAction extends AbstractAction {

	private static final Logger logger = Logger.getLogger(ImportProjectAction.class);
    
    private final MatchMakerSwingSession swingSession;
	private JFrame owningFrame;

	public ImportProjectAction(MatchMakerSwingSession swingSession, JFrame owningFrame) {
		super("Import...",
				SPSUtils.createIcon( "general/Import",
                "Import"));
		putValue(SHORT_DESCRIPTION, "Import Project");
        this.swingSession = swingSession;
		this.owningFrame = owningFrame;
	}


	public void actionPerformed(ActionEvent e) {
	    PlFolder folder = MMSUtils.getTreeObject(swingSession.getTree(), PlFolder.class );
	    if (folder == null) {
	        JOptionPane.showMessageDialog(owningFrame, "Please select a folder to import into.");
	        return;
	    }

		JFileChooser fc = new JFileChooser(
				swingSession.getLastImportExportAccessPath());
		fc.setFileFilter(SPSUtils.XML_FILE_FILTER);
		fc.setDialogTitle("Select the file to import");
	
		File importFile = null;
		int fcChoice = fc.showOpenDialog(owningFrame);

		if (fcChoice != JFileChooser.APPROVE_OPTION) {
		    return;
		}
		importFile = fc.getSelectedFile();
		swingSession.setLastImportExportAccessPath(
		        importFile.getAbsolutePath());

		List<Project> projects;
		BufferedInputStream in = null;
		try {
		    in = new BufferedInputStream(new FileInputStream(importFile));
		    ProjectDAOXML xmldao = new ProjectDAOXML(swingSession, in);
		    projects = xmldao.findAll();
		} catch (FileNotFoundException e1) {
		    SPSUtils.showExceptionDialogNoReport(owningFrame,
		            "The file " + importFile.getName() + " cannot be found", e1 );
		    return;
		} catch (Exception e1) {
		    SPSUtils.showExceptionDialogNoReport(owningFrame, "Import failed", e1);
		    return;
		} finally {
		    try {
		        if (in != null) in.close();
		    } catch (IOException ex) {
		        logger.error(
		                "Couldn't close input file. Swallowing this " +
		                "exception to preserve the possible original one", ex);
		    }         
		}

		if (projects == null || projects.isEmpty()) {
		    JOptionPane.showConfirmDialog(null,
		            "Did not find any projects to import from that file",
		            "Import failed",
		            JOptionPane.ERROR_MESSAGE);
		    return;
		}
			
		for (Project project : projects) {

		    // check for an existing project with the same name
		    if (!swingSession.isThisProjectNameAcceptable(project.getName())) {
		        String alternateName;
		        String baseName = project.getName() + " ";
		        for (int suffix = 1; ; suffix++) {
		            alternateName = baseName + suffix;
		            if (swingSession.isThisProjectNameAcceptable(alternateName)) {
		                break;
		            }
		        }
		        
		        int option = JOptionPane.showOptionDialog(
		                swingSession.getFrame(),
		                "The import file contains a project called\n" +
		                "\""+project.getName()+"\", but there is already\n" +
		                "a project with that name in the repository.\n",
		                "Project Name Conflict",
		                JOptionPane.DEFAULT_OPTION,
		                JOptionPane.QUESTION_MESSAGE,
		                null,
		                new String[] { "Skip", "Replace", "Import as \""+alternateName+"\""},
		        "Skip");
		        if (option == 0) {
		            continue;
		        } else if (option == 1) {
		            Project existingProject = swingSession.getProjectByName(project.getName());
		            swingSession.delete(existingProject);
		        } else if (option == 2) {
		            project.setName(alternateName);
		        } else {
		            // dialog was closed by user--cancel entire operation
		            return;
		        }
		    }

		    folder.addChild(project);
		    logger.debug("Saving Project:" + project.getName());
		    swingSession.save(project);
		}

	}


}