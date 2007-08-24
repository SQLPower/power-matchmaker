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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.i 
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
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchImportor;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.swingui.SPSUtils;

public class PlMatchImportAction extends AbstractAction {

	private static final Logger logger = Logger.getLogger(PlMatchImportAction.class);
    private final MatchMakerSwingSession swingSession;

	private JFrame owningFrame;

	public PlMatchImportAction(MatchMakerSwingSession swingSession, JFrame owningFrame) {
		// FIXME: We need an icon for this
		super("Import",
				SPSUtils.createIcon( "general/Import",
                "Import"));
		putValue(SHORT_DESCRIPTION, "Import Match");
        this.swingSession = swingSession;
		this.owningFrame = owningFrame;
	}


	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser(
				swingSession.getLastImportExportAccessPath());
		fc.setFileFilter(SPSUtils.XML_FILE_FILTER);
		fc.setDialogTitle("Import Match");
	
		File importFile = null;
		int fcChoice = fc.showOpenDialog(owningFrame);

		if (fcChoice == JFileChooser.APPROVE_OPTION) {
			importFile = fc.getSelectedFile();
			swingSession.setLastImportExportAccessPath(
					importFile.getAbsolutePath());

			Match match = new Match();
			BufferedInputStream in = null;
			try {
				in = new BufferedInputStream(new FileInputStream(importFile));
				MatchImportor importer = new MatchImportor();
				match = new Match();
				match.setSession(swingSession);
				importer.load(match,in);
			} catch (FileNotFoundException e1) {
				SPSUtils.showExceptionDialogNoReport(owningFrame,
						"The file " + importFile.getName() + " cannot be found", e1 );
			} catch (IOException e1) {
				SPSUtils.showExceptionDialogNoReport(owningFrame,
						"There was an IO exception while reading the file " + importFile.getName(), e1 );
			} catch (ParserConfigurationException e1) {
				SPSUtils.showExceptionDialogNoReport(owningFrame,
						"There is an error with the XML parser configuration", e1 );
			} catch (SAXException e1) {
				SPSUtils.showExceptionDialogNoReport(owningFrame, 
						"There was an error while parsing the XML import file", e1 );
			} catch (Exception e1) {
				SPSUtils.showExceptionDialogNoReport(owningFrame, 
						"There was an exception while doing the import", e1);
			}

			if ( match == null ) {
				JOptionPane.showConfirmDialog(null,
						"Unable to read match ID from XML",
						"XML File error",
						JOptionPane.ERROR_MESSAGE);
				return;
			} else {

				Match match2 = swingSession.getMatchByName(match.getName());
				if ( match2 != null ) {
					logger.debug("Match ["+match2.getName()+"] exists");
					int option = JOptionPane.showConfirmDialog(
							swingSession.getFrame(),
		                    "Match ["+match2.getName()+"] Exists! Do you want to overwrite it?",
		                    "Match ["+match2.getName()+"] Exists!",
		                    JOptionPane.OK_CANCEL_OPTION );
					if ( option != JOptionPane.OK_OPTION ) {
						return;
					}
					swingSession.delete(match2);
				}

				if ( match.getParent() != null ) {
					List<PlFolder> folders = swingSession.getCurrentFolderParent().getChildren();
					for ( PlFolder<Match> folder : folders ) {
						if ( folder.getName().equals(((PlFolder<Match>)match.getParent()).getName())) {
							logger.debug("Folder ["+folder.getName()+"] exists");
							swingSession.move(match,folder);
							break;
						}
					}
				}
				logger.debug("Saving Match:" + match.getName());
				swingSession.save(match);
			}

		}
	}


}