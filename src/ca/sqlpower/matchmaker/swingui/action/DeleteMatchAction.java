package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;

/**
 * Simple action that deletes a match from the swing session
 */
public class DeleteMatchAction extends AbstractAction {

	private MatchMakerSwingSession swingSession;
	private Match match;
	
	public DeleteMatchAction(MatchMakerSwingSession swingSession, Match match) {
		super("Delete Match");
		this.swingSession = swingSession;
		this.match = match;
	}
	
	public void actionPerformed(ActionEvent e) {
		swingSession.delete(match);
	}
	
}
