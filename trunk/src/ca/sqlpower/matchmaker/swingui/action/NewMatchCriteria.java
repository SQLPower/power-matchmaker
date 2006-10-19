package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.matchmaker.hibernate.PlMatchCriterion;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;

public class NewMatchCriteria extends AbstractAction {
	
	PlMatchGroup group;
	
	public NewMatchCriteria(PlMatchGroup group) {
		super("New Criterion");
		this.group = group;
	}

	public void actionPerformed(ActionEvent e) {
		PlMatchCriterion plMatchCriterion = new PlMatchCriterion();
		plMatchCriterion.setPlMatchGroup(group);
		group.addPlMatchCriteria(plMatchCriterion);

	}

}
