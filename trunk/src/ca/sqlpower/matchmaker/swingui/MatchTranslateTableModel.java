package ca.sqlpower.matchmaker.swingui;

import javax.swing.table.AbstractTableModel;

import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.MatchMakerTranslateWord;

public class MatchTranslateTableModel extends AbstractTableModel {

	MatchMakerTranslateGroup translate;
	
	public  MatchTranslateTableModel(MatchMakerTranslateGroup translate){ 
		super();
		this.translate = translate; 
	}

	public int getColumnCount() {		
		return 2;
	}

	public int getRowCount() {
		if(translate == null) return 0;
		int size = translate.getChildCount();		
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
        MatchMakerTranslateWord trans = getRow(rowIndex);
		switch(columnIndex) {
		case 0:
			return trans.getFrom();
		case 1:
			return trans.getTo();
		default:
			throw new IndexOutOfBoundsException("Invalid column index");
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		MatchMakerTranslateWord trans = getRow(rowIndex);
		try {
		switch(columnIndex) {
		case 0:
			for (MatchMakerTranslateWord t: translate.getChildren()){
				if ((t.getFrom() == null ? t.getFrom() == aValue : t.getFrom().equals(aValue)) &&
						(t.getTo() == null ? t.getTo() == trans.getTo() : t.getTo().equals(trans.getTo()))) {
					// We would have a collision.
					return;
				}
			}
			trans.setFrom((String)aValue);
			break;
		case 1:
			for (MatchMakerTranslateWord t: translate.getChildren()){
				if ((t.getTo() == null ? t.getTo() == aValue : t.getTo().equals(aValue)) &&
						(t.getFrom() == null ? t.getFrom() == trans.getFrom() : t.getFrom().equals(trans.getFrom()))) {
					// We would have a collision.
					return;
				}
			}
			trans.setTo((String) aValue);

			break;
		default:
			throw new IndexOutOfBoundsException("Invalid column index");
		}

		} finally {
			
		}
	}
	
	public MatchMakerTranslateWord getRow(int rowIndex) {
		return translate.getChildren().get(rowIndex );
	}
}
