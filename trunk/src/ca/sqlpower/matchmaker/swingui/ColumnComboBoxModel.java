package ca.sqlpower.matchmaker.swingui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;



public class ColumnComboBoxModel implements ComboBoxModel {
	private String tableName;
	private SQLTable table;
	private List<String> columnNames = new ArrayList<String>();
	PlMatchGroup group;
	private String selected;
	
	public ColumnComboBoxModel(SQLTable table, PlMatchGroup group) throws ArchitectException {
		super();
		ColumnComboBoxModelImpl(table, group);
		
	}
	
	public ColumnComboBoxModel(SQLTable table) throws ArchitectException {
		super();
		ColumnComboBoxModelImpl(table, null);
		
	}
	
	private void ColumnComboBoxModelImpl(SQLTable table, PlMatchGroup group) throws ArchitectException{
		this.group = group;
		setTable(table);
	}

	public String getTableName() {
		return tableName;
	}



	public Object getElementAt(int index)  {
		
		List<String> curElements = new ArrayList<String>(columnNames);
		if ( group != null){
			curElements.removeAll(group.getUsedColumnNames());
		}
		return curElements.get(index);
		
	}

	public int getSize()  {
		List<String> curElements = new ArrayList<String>(columnNames);
		if(group != null){
			curElements.removeAll(group.getUsedColumnNames());
		}
		return curElements.size();
	}

	public Object getSelectedItem() {
		// TODO Auto-generated method stub
		return selected;
	}

	public void setSelectedItem(Object anItem) {
		if (anItem!=null){
			selected = (String) anItem;
		}
		
	}

	public void addListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub
		
	}

	public void removeListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub
		
	}

	public SQLTable getTable() {
		return table;
	}

	public void setTable(SQLTable table) throws ArchitectException {
		if (this.table != table) {
			this.table = table;
			if (table != null){
				columnNames.clear();
				for (SQLColumn c : table.getColumns()){
					columnNames.add(c.getName());
				}
			}
		}
	}

}
