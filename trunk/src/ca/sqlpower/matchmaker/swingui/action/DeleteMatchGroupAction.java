package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.hibernate.Transaction;

import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;
import ca.sqlpower.matchmaker.hibernate.home.PlMatchGroupHome;
import ca.sqlpower.matchmaker.util.HibernateUtil;


public class DeleteMatchGroupAction extends AbstractAction {

	PlMatchGroup matchGroup;
	

	public DeleteMatchGroupAction(PlMatchGroup matchGroup) {
		super("Delete Match Group");
		this.matchGroup = matchGroup;
	}


	public void actionPerformed(ActionEvent e) {
		PlMatchGroupHome home = new PlMatchGroupHome();
		Transaction tx = HibernateUtil.primarySession().beginTransaction();
		home.delete(matchGroup);
		matchGroup.getPlMatch().getPlMatchGroups().remove(matchGroup);
		tx.commit();
	}
}
