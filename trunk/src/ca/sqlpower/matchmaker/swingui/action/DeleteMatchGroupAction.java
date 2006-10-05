package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JSplitPane;

import org.hibernate.Transaction;

import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;
import ca.sqlpower.matchmaker.hibernate.home.PlMatchGroupHome;
import ca.sqlpower.matchmaker.util.HibernateUtil;


public class DeleteMatchGroupAction extends AbstractAction {

	PlMatchGroup matchGroup;
	JSplitPane splitPane;
	

	public DeleteMatchGroupAction(PlMatchGroup matchGroup,JSplitPane splitPane) {
		super("Delete Match Group");
		this.matchGroup = matchGroup;
		this.splitPane = splitPane;
	}


	public void actionPerformed(ActionEvent e) {
		PlMatchGroupHome home = new PlMatchGroupHome();
		Transaction tx = HibernateUtil.primarySession().beginTransaction();
		splitPane.setRightComponent(null);
		home.delete(matchGroup);
		matchGroup.getPlMatch().removePlMatchGroups(matchGroup);
		tx.commit();
	}
}
