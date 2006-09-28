package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.hibernate.Transaction;

import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;
import ca.sqlpower.matchmaker.hibernate.home.PlMatchGroupHome;
import ca.sqlpower.matchmaker.swingui.PlMatchGroupPanel;
import ca.sqlpower.matchmaker.util.HibernateUtil;


public class DeleteMatchGroupAction extends AbstractAction {

	PlMatchGroup matchGroup;
	PlMatchGroupPanel panel;
	

	public DeleteMatchGroupAction(PlMatchGroup matchGroup, PlMatchGroupPanel panel) {
		super("Delete Match Group");
		this.matchGroup = matchGroup;
		this.panel = panel;
	}


	public void actionPerformed(ActionEvent e) {
		PlMatchGroupHome home = new PlMatchGroupHome();
		panel.clear();
		Transaction tx = HibernateUtil.primarySession().beginTransaction();
		home.delete(matchGroup);
		matchGroup.getPlMatch().getPlMatchGroups().remove(matchGroup);
		tx.commit();
	}
}
