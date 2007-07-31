package ca.sqlpower.matchmaker;

import java.util.List;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLTable;

public class TableIndex {
	
	AbstractMatchMakerObject mmo;
 	/**
     * The unique index of the source table that we're using.  Not necessarily one of the
     * unique indices defined in the database; the user can pick an arbitrary set of columns.
     */
    private SQLIndex sourceTableIndex;
    
    
    private CachableTable table;
	private String property;
    
    /**
     * Hooks the index up to the source table, attempts to resolve the
     * column names to actual SQLColumn references on the source table,
     * and then returns it!
     */
    public SQLIndex getTableIndex() throws ArchitectException {
    	if (table.getSourceTable() != null && sourceTableIndex != null) {
    		sourceTableIndex.setParent(table.getSourceTable().getIndicesFolder());
    		resolveTableIndexColumns(sourceTableIndex);
    	}
    	return sourceTableIndex;
    }

    /**
     * Attempts to set the column property of each index column in the
     * sourceTableColumns.  The UserType for SQLIndex can't do this because
     * the source table isn't populated yet when it's invoked.
     */
    private void resolveTableIndexColumns(SQLIndex si) throws ArchitectException {
    	SQLTable st = table.getSourceTable();
    	for (SQLIndex.Column col : (List<SQLIndex.Column>) si.getChildren()) {
    		SQLColumn actualColumn = st.getColumnByName(col.getName());
    		col.setColumn(actualColumn);
    	}
	}

	public void setTableIndex(SQLIndex index) {
    	final SQLIndex oldIndex = sourceTableIndex;
    	sourceTableIndex = index;
    	mmo.getEventSupport().firePropertyChange(property, oldIndex, index);
    }

	/**
	 * Returns true if the index is user created.  False if the
	 * index is not user created or is null
	 * we check if the index is user created by if the parent
	 * is null or dosn't contain the sql index.
	 */
	public boolean isUserCreated() throws ArchitectException {
		if (getTableIndex() == null) return false;
		if (getTableIndex().getParent() == null ){ 
			return true;
		} else if (getTableIndex().getParent().getChildren().contains(sourceTableIndex)){
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Manages a sqlindex on cachableTable table for mmo. 
	 * 
	 * The property argument is the property type the index should use for an event.
	 * We assume all arguments are non-null
	 */
	public TableIndex(AbstractMatchMakerObject mmo, CachableTable table, String property) {
		super();
		this.mmo = mmo;
		this.table = table;
		this.property = property;
	}
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Table Index @").append(System.identityHashCode(this));
		buf.append(", index: "+sourceTableIndex);
		return buf.toString();
	}
}
