package ca.sqlpower.matchmaker.swingui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.hibernate.Transaction;

import ca.sqlpower.matchmaker.hibernate.PlMatchTranslate;
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
			return trans.getGroupName();
			
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
			trans.setGroupName((String)aValue);
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
