package ca.sqlpower.matchmaker.swingui;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLTable;



public class ColumnComboBoxModel implements ComboBoxModel {
	private String tableName;
	private SQLTable t;
	private String selected;
	
	public ColumnComboBoxModel(String catalogName, String schemaName,String tableName) throws ArchitectException {
		super();
		setTableName(catalogName, schemaName, tableName);
		
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String catalogName, String schemaName, String tableName) throws ArchitectException {
		if (this.tableName != tableName) {
			this.tableName = tableName;
			t = MatchMakerFrame.getMainInstance().getDatabase().getTableByName(catalogName,schemaName,tableName);
		}
	}

	public Object getElementAt(int index)  {
		try {
			return t.getColumn(index).getName();
		} catch (ArchitectException e) {
			throw new ArchitectRuntimeException(e);
		}
	}

	public int getSize()  {
		try {
			return t.getColumns().size();
		} catch (ArchitectException e) {
			throw new ArchitectRuntimeException(e);
		}
	}

	public Object getSelectedItem() {
		// TODO Auto-generated method stub
		return selected;
	}

	public void setSelectedItem(Object anItem) {
		selected = (String) anItem;
		
	}

	public void addListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub
		
	}

	public void removeListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub
		
	}

}
