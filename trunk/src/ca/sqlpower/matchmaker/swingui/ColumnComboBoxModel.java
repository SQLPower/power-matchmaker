package ca.sqlpower.matchmaker.swingui;

import javax.swing.AbstractListModel;



public class ColumnComboBoxModel extends AbstractListModel {
	private String tableName;
	
	
	public ColumnComboBoxModel(String tableName) {
		super();
		this.tableName = tableName;
		
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		if (this.tableName != tableName) {
			this.tableName = tableName;
		}
	}

	public Object getElementAt(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getSize() {
		// TODO Auto-generated method stub
		return 0;
	}

}
