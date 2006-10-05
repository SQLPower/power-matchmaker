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
	private SQLTable t;
	private List<String> columnNames = new ArrayList<String>();
	PlMatchGroup group;
	private String selected;
	
	public ColumnComboBoxModel(String catalogName, String schemaName,String tableName, PlMatchGroup group) throws ArchitectException {
		super();
		this.group = group;
		setTableName(catalogName, schemaName, tableName);
		
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String catalogName, String schemaName, String tableName) throws ArchitectException {
		if (this.tableName != tableName) {
			this.tableName = tableName;
			t = MatchMakerFrame.getMainInstance().getDatabase().getTableByName(catalogName,schemaName,tableName);
			if (t != null){
				for (SQLColumn c : t.getColumns()){
					columnNames.add(c.getName());
				}
			}
		}
	}

	public Object getElementAt(int index)  {
		
		List<String> curElements = new ArrayList<String>(columnNames);
	
		curElements.removeAll(group.getUsedColumnNames());
		return curElements.get(index);
		
	}

	public int getSize()  {
		List<String> curElements = new ArrayList<String>(columnNames);
		curElements.removeAll(group.getUsedColumnNames());
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

}
