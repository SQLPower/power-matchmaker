package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.hibernate.PlMatchCriterion;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;
import ca.sqlpower.matchmaker.swingui.ColumnComboBoxModel;

public class NewMatchCriteria extends AbstractAction {
	
	PlMatchGroup group;
	ColumnComboBoxModel ccbModel;
	
	public NewMatchCriteria(PlMatchGroup group, SQLTable t) throws ArchitectException {
		super("New Criterion");
		this.group = group;
		ccbModel = new ColumnComboBoxModel(t,group);
	}

	public void actionPerformed(ActionEvent e) {
		if (ccbModel.getSize() > 0){
			PlMatchCriterion plMatchCriterion = new PlMatchCriterion();
			plMatchCriterion.setPlMatchGroup(group);
			plMatchCriterion.setColumnName((String)ccbModel.getElementAt(0));
			group.addPlMatchCriteria(plMatchCriterion);
		}
	}

}
