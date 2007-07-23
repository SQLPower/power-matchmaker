package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.DateFormatAllowsNull;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.architect.swingui.ASUtils.FileExtensionFilter;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchExportor;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;

public class PlMatchExportAction extends AbstractAction {

    private final DateFormat df = new DateFormatAllowsNull();

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
				ASUtils.createJLFIcon( "general/Export",
						"Export",
						ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "Export Match");
        this.swingSession = swingSession;
		this.owningFrame = owningFrame;
	}


	public void actionPerformed(ActionEvent e) {

	    Match match;  // the match we're exporting
	    match = ArchitectUtils.getTreeObject(
	            swingSession.getTree(),
	            Match.class );

        if (match == null) {
            JOptionPane.showMessageDialog(owningFrame, "Please select a match to export.");
			return;
		}

		JFileChooser fc = new JFileChooser(swingSession.getLastImportExportAccessPath());
		fc.setFileFilter(ASUtils.XML_FILE_FILTER);
		fc.setDialogTitle("Export Match");
		fc.setSelectedFile(
				new File("export_match_"+match.getName()+"."+
						((FileExtensionFilter) ASUtils.XML_FILE_FILTER).getFilterExtension(0)));
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
        	PrintWriter out;
        	try {
        		out = new PrintWriter(export);
        		MatchExportor exportor = new MatchExportor();
        		exportor.save(match,out, "UTF-8");
        	} catch (IOException e1) {
        		throw new RuntimeException("IO Error during save", e1);
			}
        }

	}

}