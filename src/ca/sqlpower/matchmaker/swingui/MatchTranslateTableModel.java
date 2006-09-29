package ca.sqlpower.matchmaker.swingui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.hibernate.Transaction;

import ca.sqlpower.matchmaker.hibernate.PlMatchTranslate;
import ca.sqlpower.matchmaker.hibernate.PlMatchTranslateId;
import ca.sqlpower.matchmaker.util.HibernateUtil;

public class MatchTranslateTableModel extends AbstractTableModel {

	List<PlMatchTranslate> translate;
	
	public  MatchTranslateTableModel(List<PlMatchTranslate> translate){ 
		super();
		this.translate = translate; 
	}

	public int getColumnCount() {
		
		return 3;
	}

	public int getRowCount() {
		if(translate == null) return 0;
		return translate.size();
	}

	@Override
	public String getColumnName(int column) {
		switch(column) {
		case 0:
			return "Group Name";
			
		case 1:
			return "From";
		case 2:
			return "To";
		default:
			throw new IndexOutOfBoundsException("Invalid column index");
		}
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}
	
	public Object getValueAt(int rowIndex, int columnIndex) {
		PlMatchTranslate trans = translate.get(rowIndex);
		switch(columnIndex) {
		case 0:
			return trans.getId().getGroupName();
			
		case 1:
			return trans.getFromWord();
		case 2:
			return trans.getToWord();
		default:
			throw new IndexOutOfBoundsException("Invalid column index");
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		PlMatchTranslate trans = translate.get(rowIndex);
		Transaction tx = HibernateUtil.primarySession().beginTransaction();
		switch(columnIndex) {
		case 0:
			if (!trans.getId().getGroupName().equals(aValue)){
				PlMatchTranslate t2 = new PlMatchTranslate(new PlMatchTranslateId((String)aValue,trans.getId().getSeqNo()));
				t2.setFromWord(trans.getFromWord());
				t2.setToWord(trans.getToWord());
				HibernateUtil.primarySession().delete(trans);	
				int index = translate.indexOf(trans);
				translate.remove(index);
				translate.add(index, t2);
				HibernateUtil.primarySession().save(t2);
			}
			break;
			
		case 1:
			trans.setFromWord((String)aValue);
			HibernateUtil.primarySession().flush();
			break;
		case 2:
			trans.setToWord((String) aValue);
			HibernateUtil.primarySession().flush();
			break;
		default:
			tx.rollback();
			throw new IndexOutOfBoundsException("Invalid column index");
		}
		tx.commit();
	}
}
