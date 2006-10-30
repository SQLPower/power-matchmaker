package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JTable;

import ca.sqlpower.matchmaker.hibernate.PlMatchCriterion;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;
import ca.sqlpower.matchmaker.hibernate.home.PlMatchCriteriaHome;

public class DeleteMatchCriteria extends AbstractAction {
	PlMatchGroup model;
	JTable matchTable;
	
	
	public DeleteMatchCriteria(PlMatchGroup model, JTable matchTable) {
		super("Delete Criteria");
		this.model = model;
		this.matchTable = matchTable;
	}

	public void actionPerformed(ActionEvent e) {
		List<Integer> selected = new ArrayList<Integer>();
		for (int i:matchTable.getSelectedRows()){
			selected.add(i);
		}
		Collections.sort(selected);
		PlMatchCriteriaHome home = new PlMatchCriteriaHome();
		System.out.println("BEGIND ELETE \n"+model.getChildren());
		System.out.println(selected);
		for(int i= selected.size()-1; i>=0;i--){
			PlMatchCriterion plMatchCriterion = (PlMatchCriterion) model.getChildren().get(i);
			if ( !model.removePlMatchCriteria(plMatchCriterion)) {
				System.out.println(" DELETE FAILED");;
			}
			home.delete(plMatchCriterion);
		}
		System.out.println(model.getChildren() + "\nEND DELETE");
		home.flush();		
	}

}
