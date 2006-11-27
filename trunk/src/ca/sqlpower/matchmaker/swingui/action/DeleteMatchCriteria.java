package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.matchmaker.MatchMakerCriteria;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;

public class DeleteMatchCriteria extends AbstractAction {
	MatchMakerCriteria criteria;
	MatchMakerSwingSession swingSession;
	
	public DeleteMatchCriteria(MatchMakerSwingSession swingSession, MatchMakerCriteria criteria) {
		super("Delete Criteria");
		this.criteria = criteria;
		this.swingSession = swingSession;
	}

	public void actionPerformed(ActionEvent e) {
		swingSession.delete(criteria);
	}

}
