package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;

public class DeletePlFolderAction extends AbstractAction {

	MatchMakerSwingSession session;
	PlFolder folder;
	public DeletePlFolderAction(MatchMakerSwingSession swingSession, String name, PlFolder folder) {
		super(name);
		session = swingSession;
		this.folder = folder;
	}

	public void actionPerformed(ActionEvent e) {
		
	}

}
