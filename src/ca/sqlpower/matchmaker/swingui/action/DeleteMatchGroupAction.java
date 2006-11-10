package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.hibernate.Transaction;

import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;
import ca.sqlpower.matchmaker.hibernate.home.PlMatchGroupHome;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.util.HibernateUtil;


public class DeleteMatchGroupAction extends AbstractAction {

    private final MatchMakerSwingSession swingSession;
    private final PlMatchGroup matchGroup;

	public DeleteMatchGroupAction(MatchMakerSwingSession swingSession, PlMatchGroup matchGroup) {
		super("Delete Match Group");
        this.swingSession = swingSession;
		this.matchGroup = matchGroup;
	}

	public void actionPerformed(ActionEvent e) {
		PlMatchGroupHome home = new PlMatchGroupHome();
		Transaction tx = HibernateUtil.primarySession().beginTransaction();
		swingSession.setCurrentEditorComponent(null);
		home.delete(matchGroup);
		matchGroup.getPlMatch().removePlMatchGroups(matchGroup);
		tx.commit();
	}
}
