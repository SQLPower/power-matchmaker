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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.dao.xml.ProjectDAOXML;
import ca.sqlpower.matchmaker.swingui.MMSUtils;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSUtils.FileExtensionFilter;

public class ExportProjectAction extends AbstractAction {

    private final static Logger logger = Logger.getLogger(ExportProjectAction.class);
    
    private final MatchMakerSwingSession swingSession;
	private final JFrame owningFrame;

    /**
     * Creates a new instance of this action which is parented by the given frame and will export the
     * given project object when invoked.
     *
     * @param swingSession The GUI session this action lives in.
     * @param owningFrame The frame that should own any dialogs this action creates.
     * @param project The project to export.  If you specify null, the project to export will be
     * determined by the current selection in the Swing Session's tree.
     */
	public ExportProjectAction(MatchMakerSwingSession swingSession, JFrame owningFrame) {
		super("Export...",
				SPSUtils.createIcon( "general/Export",
						"Export"));
        logger.debug("Creating new project export action");
		putValue(SHORT_DESCRIPTION, "Export Project");
        this.swingSession = swingSession;
		this.owningFrame = owningFrame;
	}


	public void actionPerformed(ActionEvent e) {

	    Project project;  // the project we're exporting
	    project = MMSUtils.getTreeObject(
	            swingSession.getTree(),
	            Project.class );

        if (project == null) {
            JOptionPane.showMessageDialog(owningFrame, "Please select a project to export.");
			return;
		}

		JFileChooser fc = new JFileChooser(swingSession.getLastImportExportAccessPath());
		fc.setFileFilter(SPSUtils.XML_FILE_FILTER);
		fc.setDialogTitle("Export Project");
		fc.setSelectedFile(
				new File("export_project_"+project.getName()+"."+
						((FileExtensionFilter) SPSUtils.XML_FILE_FILTER).getFilterExtension(0)));

		File export = null;

		while (true) {
			int fcChoice = fc.showSaveDialog(owningFrame);
			if (fcChoice == JFileChooser.APPROVE_OPTION) {
				export = fc.getSelectedFile();
				swingSession.setLastImportExportAccessPath(export.getAbsolutePath());

				if (export.exists()) {
					int response = JOptionPane.showConfirmDialog(
							owningFrame,
							"The file\n\n"+export.getPath()+
							"\n\nalready exists. Do you want to overwrite it?",
							"File Exists", JOptionPane.YES_NO_OPTION);
					if (response == JOptionPane.YES_OPTION ) {
						break;
					}
				} else {
					break;
				}
			} else {
				return;
			}
		}

		if ( export != null ) {
        	OutputStream out = null;
        	try {
        		out = new FileOutputStream(export);
                ProjectDAOXML xmldao = new ProjectDAOXML(out);
                xmldao.save(project);
        	} catch (IOException ioe) {
        		SPSUtils.showExceptionDialogNoReport(owningFrame, 
        				"There was an exception while writing to the file " + export.getName(), ioe);
			} catch (Exception ex) {
				SPSUtils.showExceptionDialogNoReport(owningFrame, 
        				"There was an exception while doing the export", ex);
			} finally {
                try {
                    if (out != null) out.close();
                } catch (IOException ex) {
                    logger.error("Failed to close output stream! Squishing this exception:", ex);
                }
            }
        }

	}

}