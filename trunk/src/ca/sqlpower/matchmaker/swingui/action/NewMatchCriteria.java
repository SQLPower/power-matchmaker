package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.hibernate.PlMatchCriterion;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;
import ca.sqlpower.matchmaker.swingui.ColumnComboBoxModel;

public class NewMatchCriteria extends AbstractAction {
	
	final PlMatchGroup group;
	ColumnComboBoxModel ccbModel;
	
	public NewMatchCriteria(PlMatchGroup group, SQLTable t) throws ArchitectException {
		super("New Criterion");
		this.group = group;
		ccbModel = new ColumnComboBoxModel(t,group);
        PropertyChangeListener pcl = new PropertyChangeListener(){

            public void propertyChange(PropertyChangeEvent evt) {
                NewMatchCriteria.this.setEnabled(ccbModel.getSize() > 0 && NewMatchCriteria.this.group.getGroupId() != null);   
            }
            
        };
        this.group.addPropertyChangeListener(pcl);
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
