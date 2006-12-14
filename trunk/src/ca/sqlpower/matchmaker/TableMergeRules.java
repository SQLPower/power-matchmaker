package ca.sqlpower.matchmaker;

import ca.sqlpower.architect.SQLTable;

/**
 *
 * Merge strategy handles a per table setup of the merge engine
 * The best way to think of this is a per row merge rules.
 */
public class TableMergeRules
	extends AbstractMatchMakerObject<TableMergeRules, MatchMakerObject> {

	private Long oid;

	/**
	 * The order in which this strategy is run relative to other
	 * Strategies in the table
	 */
	private Long seqNo;
	
	/**
	 * Whether or not we should delete the duplicate row 
	 */
	private boolean deleteDup;
	
	
	/**
	 * The table on which we're merging
	 */
	private CachableTable cachableTable = new CachableTable(this, "table");;
	
	public TableMergeRules() {

	}

	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof TableMergeRules)){
			return false;
		}
		TableMergeRules other = (TableMergeRules) obj;
		if (getParent() != null ){
			if (other == null || !getParent().equals(other.getParent())) {
				return false;
			}
		} else {
			if (other.getParent() != null){
				return false;
			}
		}
		if (getSourceTable() == null) {
			if (other.getSourceTable() != null){
				return false;
			}
		} else {
			if (other == null || !getSourceTable().equals(other.getSourceTable())) {
				return false;
			}
		}
		return true;
	}

	public TableMergeRules duplicate(MatchMakerObject parent, MatchMakerSession session) {
		TableMergeRules newMergeStrategy = new TableMergeRules();
		newMergeStrategy.setParent(parent);
		newMergeStrategy.setName(getName());
		newMergeStrategy.setSession(session);
		newMergeStrategy.setDeleteDup(isDeleteDup());
		newMergeStrategy.setSeqNo(getSeqNo());
		newMergeStrategy.setTableName(getTableName());
		newMergeStrategy.setCatalogName(getCatalogName());
		newMergeStrategy.setSchemaName(getSchemaName());
		return newMergeStrategy;
	}

	public String getCatalogName() {
		return cachableTable.getCatalogName();
	}

	public String getSchemaName() {
		return cachableTable.getSchemaName();
	}

	public SQLTable getSourceTable() {
		return cachableTable.getSourceTable();
	}

	public String getTableName() {
		return cachableTable.getTableName();
	}

	public void setCatalogName(String sourceTableCatalog) {
		cachableTable.setCatalogName(sourceTableCatalog);
	}

	public void setSchemaName(String sourceTableSchema) {
		cachableTable.setSchemaName(sourceTableSchema);
	}

	public void setTable(SQLTable table) {
		this.cachableTable.setTable(table);
	}

	public void setTableName(String sourceTableName) {
		cachableTable.setTableName(sourceTableName);
	}

	public boolean isDeleteDup() {
		return deleteDup;
	}

	public void setDeleteDup(boolean deleteDup) {
		boolean oldValue = this.deleteDup;
		this.deleteDup = deleteDup;
		getEventSupport().firePropertyChange("deleteDup", oldValue, this.deleteDup);
	}

	public Long getSeqNo() {
		return seqNo;
	}

	public void setSeqNo(Long seqNo) {
		Long oldValue = this.seqNo;
		this.seqNo = seqNo;
		getEventSupport().firePropertyChange("seqNo", oldValue, this.seqNo);
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Merge Strategy->'").append(getName()).append("' ");
		buf.append("Parent->'").append(getParent()).append("' ");
		buf.append("Seq No->'").append(getSeqNo()).append("' ");
		buf.append("isDeletedDup()->'").append(isDeleteDup()).append("' ");
		return buf.toString();
	}
}
