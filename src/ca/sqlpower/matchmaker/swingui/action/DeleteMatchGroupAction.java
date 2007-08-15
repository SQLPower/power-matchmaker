package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import ca.sqlpower.matchmaker.MatchMakerCriteriaGroup;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;


public class DeleteMatchGroupAction extends AbstractAction {

    private final MatchMakerSwingSession swingSession;
    private final MatchMakerCriteriaGroup matchGroup;

	public DeleteMatchGroupAction(MatchMakerSwingSession swingSession, MatchMakerCriteriaGroup matchGroup) {
		super("Delete Match Group");
        this.swingSession = swingSession;
		this.matchGroup = matchGroup;
	}

	public void actionPerformed(ActionEvent e) {
		int responds = JOptionPane.showConfirmDialog(swingSession.getFrame(),
		"Are you sure you want to delete the match group?");
		if (responds != JOptionPane.YES_OPTION)
			return;
		swingSession.delete(matchGroup);
		swingSession.setCurrentEditorComponent(null);
	}
}
