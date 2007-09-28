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
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.swingui.MMSUtils;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSUtils.FileExtensionFilter;

public class PlMatchExportAction extends AbstractAction {

    private final static Logger logger = Logger.getLogger(PlMatchExportAction.class);
    
    private final MatchMakerSwingSession swingSession;
	private final JFrame owningFrame;

    /**
     * Creates a new instance of this action which is parented by the given frame and will export the
     * given match object when invoked.
     *
     * @param swingSession The GUI session this action lives in.
     * @param owningFrame The frame that should own any dialogs this action creates.
     * @param match The match to export.  If you specify null, the match to export will be
     * determined by the current selection in the Swing Session's tree.
     */
	public PlMatchExportAction(MatchMakerSwingSession swingSession, JFrame owningFrame) {
		super("Export",
				SPSUtils.createIcon( "general/Export",
						"Export"));
        logger.debug("Creating new match export action");
		putValue(SHORT_DESCRIPTION, "Export Match");
        this.swingSession = swingSession;
		this.owningFrame = owningFrame;
	}


	public void actionPerformed(ActionEvent e) {

	    Match match;  // the match we're exporting
	    match = MMSUtils.getTreeObject(
	            swingSession.getTree(),
	            Match.class );

        if (match == null) {
            JOptionPane.showMessageDialog(owningFrame, "Please select a match to export.");
			return;
		}

		JFileChooser fc = new JFileChooser(swingSession.getLastImportExportAccessPath());
		fc.setFileFilter(SPSUtils.XML_FILE_FILTER);
		fc.setDialogTitle("Export Match");
		fc.setSelectedFile(
				new File("export_match_"+match.getName()+"."+
						((FileExtensionFilter) SPSUtils.XML_FILE_FILTER).getFilterExtension(0)));
		fc.setApproveButtonText("Save");


		File export = null;

		while (true) {
			int fcChoice = fc.showOpenDialog(owningFrame);
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
        	PrintWriter out = null;
        	try {
        		out = new PrintWriter(export);
                throw new RuntimeException("Export is not currently implemented.");
        	} catch (IOException ioe) {
        		SPSUtils.showExceptionDialogNoReport(owningFrame, 
        				"There was an exception while writing to the file " + export.getName(), ioe);
			} catch (Exception ex) {
				SPSUtils.showExceptionDialogNoReport(owningFrame, 
        				"There was an exception while doing the export", ex);
			} finally {
			    if (out != null) out.close();
            }
        }

	}

}