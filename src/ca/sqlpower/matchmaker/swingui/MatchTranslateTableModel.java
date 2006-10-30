package ca.sqlpower.matchmaker.swingui;

import javax.swing.table.AbstractTableModel;

import org.hibernate.Transaction;

import ca.sqlpower.matchmaker.hibernate.PlMatchTranslate;
import ca.sqlpower.matchmaker.hibernate.PlMatchTranslateGroup;
import ca.sqlpower.matchmaker.util.HibernateUtil;

public class MatchTranslateTableModel extends AbstractTableModel {

	PlMatchTranslateGroup translate;
	
	public  MatchTranslateTableModel(PlMatchTranslateGroup translate){ 
		super();
		this.translate = translate; 
	}

	public int getColumnCount() {		
		return 2;
	}

	public int getRowCount() {
		if(translate == null) return 0;
		int size = translate.getPlMatchTranslations().size();		
		return size;
	}

	@Override
	public String getColumnName(int column) {
		switch(column) {
		case 0:
			return "From";
		case 1:
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
		PlMatchTranslate trans = getRow(rowIndex);
		switch(columnIndex) {
		case 0:
			return trans.getFromWord();
		case 1:
			return trans.getToWord();
		default:
			throw new IndexOutOfBoundsException("Invalid column index");
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		PlMatchTranslate trans = getRow(rowIndex);
		Transaction tx = HibernateUtil.primarySession().beginTransaction();
		try {
		switch(columnIndex) {
		case 0:
			for (PlMatchTranslate t: translate.getPlMatchTranslations()){
				if ((t.getFromWord() == null ? t.getFromWord() == aValue : t.getFromWord().equals(aValue)) &&
						(t.getToWord() == null ? t.getToWord() == trans.getToWord() : t.getToWord().equals(trans.getToWord()))) {
					// We would have a collision.
					return;
				}
			}
			trans.setFromWord((String)aValue);
			HibernateUtil.primarySession().flush();
			break;
		case 1:
			for (PlMatchTranslate t: translate.getPlMatchTranslations()){
				if ((t.getToWord() == null ? t.getToWord() == aValue : t.getToWord().equals(aValue)) &&
						(t.getFromWord() == null ? t.getFromWord() == trans.getFromWord() : t.getFromWord().equals(trans.getFromWord()))) {
					// We would have a collision.
					return;
				}
			}
			trans.setToWord((String) aValue);
			HibernateUtil.primarySession().flush();
			break;
		default:
			tx.rollback();
			throw new IndexOutOfBoundsException("Invalid column index");
		}
		tx.commit();
		} finally {
			tx.rollback();
		}
	}
	
	public PlMatchTranslate getRow(int rowIndex) {
		return translate.getPlMatchTranslations().get(rowIndex );
	}
}
