package ca.sqlpower.matchmaker.util;

import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLTable;
/**
 * The source table represents the table in which we're looking for matching records,
 * and the unique index that we are using on that table.
 * 
 *   no setters on this class, whenever you need to change property of the class,
 *   you will have to create a new instance of the class 
 */
public class SourceTable {
	/** The source table */
	final private SQLTable table;
	/** the unique index specified by the user */
	final private SQLIndex uniqueIndex;

	public SourceTable(SQLTable table, SQLIndex index) {
		this.table = table;
		uniqueIndex = index;
	}
	
	/**
	 * Returns true iff the sql table exists and
	 * the specified index is a unique index
	 * in the sql table
	 * 
	 * FIXME Write an actual check
	 * @return
	 */
	public boolean checkValid(){
		return false;
	}

	/** true iff the index is a pk */
	public boolean isIndexIsPk() {
		if ( table == null || table.getPrimaryKeyName() == null )
			return false;
		if ( uniqueIndex == null || uniqueIndex.getName() == null )
			return false;
		return table.getPrimaryKeyName().equals(uniqueIndex.getName());
	}

	public SQLTable getTable() {
		return table;
	}

	public SQLIndex getUniqueIndex() {
		return uniqueIndex;
	}

	
}
