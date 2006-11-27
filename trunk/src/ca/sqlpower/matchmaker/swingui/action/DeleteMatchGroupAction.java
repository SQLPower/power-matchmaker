package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

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
		swingSession.delete(matchGroup);
	}
}
