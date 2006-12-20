package ca.sqlpower.matchmaker;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;

public abstract class CachableColumn {
	private SQLColumn cachedColumn;
	private String columnName;
	AbstractMatchMakerObject eventSource;
	private String property;
	
    public CachableColumn(AbstractMatchMakerObject eventSource, String property) {
		super();
		this.eventSource = eventSource;
		this.property = property;
	}

	/**
     * Returns the name of the column this set of criteria applies to.
     * You should use {@link #getColumn()} under normal circumstances.
     */
    public String getColumnName() throws ArchitectException {
        if (cachedColumn == null){
       		return columnName;
        } else {
        	return cachedColumn.getName();
        }
    }

    /**
     * Sets the column name, and nulls out the cached SQLColumn.  The next
     * call to getColumn() will result in an attempt to resolve the SQLColumn
     * that this columnName string refers to.
     * <p>
     * Note, this property is not bound.  However, it is coordinated with the
     * bound property <tt>column</tt>, so setting the column name like this
     * may eventually result in a property change event for the "column" property.
     * 
     * @param columnName the name of the match's source table column these match
     * criteria are associated with.
     */
    public void setColumnName(String columnName) {
        cachedColumn = null;
        this.columnName = columnName;
    }

    /**
     * Attempts to resolve the given column name to a column of the owning
     * Match object's source table.  This functionality is provided for the benefit of the
     * ORM layer, which has difficulty using the business model.
     * 
     * @throws ArchitectException if there is an error populating the SQLTable
     * @throws NullPointerException if any of the business objects required for
     * resolving the column object are missing
     */
    public SQLColumn getColumn() throws ArchitectException {
        if (cachedColumn != null) return cachedColumn;
        if (columnName == null) return null;
        
        SQLTable st = getTable();
        if (st == null) throw new NullPointerException("The table owner has no source table specified");
        SQLColumn newColumn = st.getColumnByName(columnName);
        
        // did we actually make it here?
        setColumn(newColumn);
        return newColumn;
    }

	public abstract SQLTable getTable();

    /**
     * Sets the cached column as well as the simple columnName string.
     */
    public void setColumn(SQLColumn column) {
        SQLColumn oldVal = this.cachedColumn;
        this.cachedColumn = column;
        this.columnName = (column == null ? null : column.getName());
        eventSource.getEventSupport().firePropertyChange(property, oldVal, column);
    }
}
