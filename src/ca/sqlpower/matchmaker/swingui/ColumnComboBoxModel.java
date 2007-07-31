package ca.sqlpower.matchmaker.swingui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;

/**
 * A combo box data model that treats the columns of a SQLTable as
 * combo box list entries.
 * <P>
 * TODO this class does not currently listen to its SQLTable for changes
 * to the columns, but it should!
 */
public class ColumnComboBoxModel implements ComboBoxModel {
    
	private final SQLTable table;

	private List<SQLColumn> columns = new ArrayList<SQLColumn>();

	private SQLColumn selected;

    /**
     * Creates a combo box model for the given table.  This combo box
     * model will only work with this one table for its whole life.
     * 
     * @param table The table to use
     * @throws ArchitectRuntimeException If the table column populate fails
     */
	public ColumnComboBoxModel(SQLTable table) {
		super();
        if (table == null) throw new NullPointerException("Null table not allowed");
        this.table = table;
        try {
            for (SQLColumn c : table.getColumns()) {
                columns.add(c);
            }
        } catch (ArchitectException ex) {
            throw new ArchitectRuntimeException(ex);
        }
	}

	public String getTableName() {
		return table.getName();
	}

	public SQLColumn getElementAt(int index) {
		return columns.get(index);
	}

	public int getSize() {
		return columns.size();
	}
	
    public SQLColumn getSelectedItem() {
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

}
