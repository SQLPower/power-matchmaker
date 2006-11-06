package ca.sqlpower.matchmaker.util;

import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLTable;
/**
 * The source table represents the table from which the matches are being obtained.
 *
 *FIXME write me
 */
public class SourceTable {
	/** The source table */
	private SQLTable table;
	/** the unique index specified by the user */
	private SQLIndex uniqueIndex;
	/** true iff the index is a pk */
	private boolean indexIsPk;
	
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

	public boolean isIndexIsPk() {
		return indexIsPk;
	}

	public void setIndexIsPk(boolean indexIsPk) {
		if (this.indexIsPk != indexIsPk) {
			this.indexIsPk = indexIsPk;
		}
	}

	public SQLTable getTable() {
		return table;
	}

	public void setTable(SQLTable table) {
		if (this.table != table) {
			this.table = table;
		}
	}

	public SQLIndex getUniqueIndex() {
		return uniqueIndex;
	}

	public void setUniqueIndex(SQLIndex uniqueIndex) {
		if (this.uniqueIndex != uniqueIndex) {
			this.uniqueIndex = uniqueIndex;
		}
	}
	
}
