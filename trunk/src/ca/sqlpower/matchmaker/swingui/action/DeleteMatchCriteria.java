package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;

import org.hibernate.Transaction;

import ca.sqlpower.matchmaker.hibernate.PlMatchCriterion;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;
import ca.sqlpower.matchmaker.util.HibernateUtil;

public class DeleteMatchCriteria extends AbstractAction {
	PlMatchGroup model;
	int[] selectedRows;
	
	
	public DeleteMatchCriteria(PlMatchGroup model, int[] selectedRows) {
		super("Delete Criteria");
		this.model = model;
		this.selectedRows = selectedRows;
	}

	public void actionPerformed(ActionEvent e) {
		List<Integer> selected = new ArrayList<Integer>();
		for (int i:selectedRows){
			selected.add(i);
		}
		Collections.sort(selected);
		Transaction tx = HibernateUtil.primarySession().beginTransaction();
		for(int i= selected.size()-1; i>=0;i--){
			HibernateUtil.primarySession().delete(model.getChildren().get(i));
			model.removePlMatchCriteria((PlMatchCriterion) model.getChildren());
		}
		tx.commit();
	}

}
