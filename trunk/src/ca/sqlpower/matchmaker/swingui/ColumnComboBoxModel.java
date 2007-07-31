package ca.sqlpower.matchmaker.swingui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.MatchMakerCriteriaGroup;

public class ColumnComboBoxModel implements ComboBoxModel {
	private SQLTable table;

	private List<SQLColumn> columnNames = new ArrayList<SQLColumn>();

	// XXX: This field doesn't look like it's being used anymore. Delete it?
	private MatchMakerCriteriaGroup group;

	private SQLColumn selected;

	public ColumnComboBoxModel(SQLTable table,
			MatchMakerCriteriaGroup group) throws ArchitectException {
		super();
		ColumnComboBoxModelImpl(table, group);

	}

	public ColumnComboBoxModel(SQLTable table) throws ArchitectException {
		super();
		ColumnComboBoxModelImpl(table, null);

	}

	private void ColumnComboBoxModelImpl(SQLTable table,
			MatchMakerCriteriaGroup group) throws ArchitectException {
		this.group = group;
		setTable(table);
	}

	public String getTableName() {
		return table.getName();
	}

	public Object getElementAt(int index) {
		List<SQLColumn> curElements = getAllUsableElement();
		return curElements.get(index);

	}

	public int getSize() {
		List<SQLColumn> curElements = getAllUsableElement();
		return curElements.size();
	}
	private List<SQLColumn> getAllUsableElement() {
		List<SQLColumn> curElements = new ArrayList<SQLColumn>(columnNames);
		return curElements;
	}

	public Object getSelectedItem() {
		return selected;
	}

	public void setSelectedItem(Object anItem) {
		if (anItem != null) {
			selected = (SQLColumn) anItem;
		}
	}

	public void addListDataListener(ListDataListener l) {
		// nothing for now

	}

	public void removeListDataListener(ListDataListener l) {
		// nothing for now

	}

	public SQLTable getTable() {
		return table;
	}

	public void setTable(SQLTable table) throws ArchitectException {
		if (this.table != table) {
			this.table = table;
			if (table != null) {
				columnNames.clear();
				for (SQLColumn c : table.getColumns()) {
					columnNames.add(c);
				}
			}
		}
	}

}
