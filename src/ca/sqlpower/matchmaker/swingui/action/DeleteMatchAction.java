package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

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
		int responds = JOptionPane.showConfirmDialog(swingSession.getFrame(),
				"Are you sure you want to delete the match?");
		if ( responds != JOptionPane.YES_OPTION )
			return;
		swingSession.delete(match);
		swingSession.setCurrentEditorComponent(null);
	}
	
}
