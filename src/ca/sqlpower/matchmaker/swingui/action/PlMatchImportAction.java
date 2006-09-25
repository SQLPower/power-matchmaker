package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.matchmaker.hibernate.PlMatch;

public class PlMatchImportAction extends AbstractAction {


	private PlMatch match;


	public PlMatchImportAction() {

		super("Import",
				ASUtils.createJLFIcon( "general/Import",
                "Import",
                ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "Import Match");


	}



	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

}
