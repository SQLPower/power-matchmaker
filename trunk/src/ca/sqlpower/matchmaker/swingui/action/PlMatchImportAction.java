package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchImportor;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;

public class PlMatchImportAction extends AbstractAction {

	private static final Logger logger = Logger.getLogger(PlMatchImportAction.class);
    private final MatchMakerSwingSession swingSession;

	private Match match;
	private JFrame owningFrame;

	public PlMatchImportAction(MatchMakerSwingSession swingSession, JFrame owningFrame) {
		super("Import",
				ASUtils.createJLFIcon( "general/Import",
                "Import",
                ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "Import Match");
        this.swingSession = swingSession;
		this.owningFrame = owningFrame;
	}


	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser(
				swingSession.getLastImportExportAccessPath());
		fc.setFileFilter(ASUtils.XML_FILE_FILTER);
		fc.setDialogTitle("Import Match");

		File importFile = null;
		int fcChoice = fc.showOpenDialog(owningFrame);

		if (fcChoice == JFileChooser.APPROVE_OPTION) {
			importFile = fc.getSelectedFile();
			swingSession.setLastImportExportAccessPath(
					importFile.getAbsolutePath());

			BufferedInputStream in = null;
			try {
				in = new BufferedInputStream(new FileInputStream(importFile));
				MatchImportor importor = new MatchImportor();
				match = new Match();
				importor.load(match,in);
			} catch (FileNotFoundException e1) {
				ASUtils.showExceptionDialogNoReport(
						"File Not Found", e1 );
			} catch (IOException e1) {
				ASUtils.showExceptionDialogNoReport(
						"IO Error", e1 );
			} catch (ParserConfigurationException e1) {
				ASUtils.showExceptionDialogNoReport(
						"Parser Error", e1 );
			} catch (SAXException e1) {
				ASUtils.showExceptionDialogNoReport(
						"XML Format Error", e1 );
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
					int option = JOptionPane.showConfirmDialog(
							null,
		                    "Match ["+match.getName()+"] Exists! Do you want to overwrite it?",
		                    "Match ["+match.getName()+"] Exists!",
		                    JOptionPane.OK_CANCEL_OPTION );
					if ( option != JOptionPane.OK_OPTION ) {
						return;
					} else {
						swingSession.delete(match2);
						swingSession.save(match);
					}
				}
			}

		}
	}


}