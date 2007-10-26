/*
 * Copyright (c) 2007, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.ColumnMergeRules.MergeActionType;

/**
 *
 * Merge strategy handles a per table setup of the merge engine
 * The best way to think of this is a per row merge rules.
 */
public class TableMergeRules
	extends AbstractMatchMakerObject<TableMergeRules, ColumnMergeRules> {

	private static final Logger logger = Logger.getLogger(TableMergeRules.class);
	
	public enum ChildMergeActionType {
		DELETE_ALL_DUP_CHILD, UPDATE_USING_SQL, UPDATE_FAIL_ON_CONFLICT, UPDATE_DELETE_ON_CONFLICT, MERGE_ON_CONFLICT;
		
		public static ChildMergeActionType getActionTypeFromString(String type) {
			if ("Delete all duplicate child records".equals(type)){
				return DELETE_ALL_DUP_CHILD;
			} else if ("Update child records and fail if conflict".equals(type)){
				return UPDATE_FAIL_ON_CONFLICT;
			} else if ("Update child records and delete if conflictt".equals(type)){
				return UPDATE_DELETE_ON_CONFLICT;
			} else if ("Update child records using the provided sql".equals(type)){
				return UPDATE_USING_SQL;
			} else if ("Merge child records if conflict".equals(type)){
				return MERGE_ON_CONFLICT;
			} else {
				throw new IllegalStateException("No such merge action type: " + type);
			} 
		}
		
		@Override
		public String toString() {
			switch (this) {
			case DELETE_ALL_DUP_CHILD:
				return "Delete all duplicate child records";
			case UPDATE_FAIL_ON_CONFLICT:
				return "Update child records and fail if conflict";
			case UPDATE_DELETE_ON_CONFLICT:
				return "Update child records and delete if conflict";
			case UPDATE_USING_SQL:
				return "Update child records using the provided sql";
			case MERGE_ON_CONFLICT:
				return "Merge child records if conflict";
			default:
				throw new IllegalStateException("Invalid enumeration");
			}
		}
	}
	
	private Long oid;

	/**
	 * Whether or not we should delete the duplicate row 
	 */
	private boolean deleteDup;
	
	/**
	 * The action to take for this child table 
	 */
	private ChildMergeActionType childMergeAction;
		
	/**
	 * The table on which we're merging
	 */
	private CachableTable cachableTable = new CachableTable(this, "table");;
	
	
	/**
	 * The index for table 
	 */
	private TableIndex tableIndex;
	
	/**
	 * The parent tableMergeRule
	 */
	private SQLTable parentTable;
	
	public TableMergeRules() {
		tableIndex = new TableIndex(this,cachableTable,"tableIndex");
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
		if (isDeleteDup() != other.isDeleteDup()) {
			return false;
		}
		if (getParentTable() == null) {
			if (other.getParentTable() != null) {
				return false;
			}
		} else {
			if (other == null || !getParentTable().equals(other.getParentTable())) {
				return false;
			}
		}
		if (getChildMergeAction() == null) {
			if (other.getChildMergeAction() != null) {
				return false;
			}
		} else {
			if (other == null || !getChildMergeAction().equals(other.getChildMergeAction())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Creates a new table merge rules with the parent and session that 
	 * are passed in.
	 * 
	 * It makes a copy of all non mutable objects.  Except the index when the
	 * index is the default index on the table.
	 */
	public TableMergeRules duplicate(MatchMakerObject parent, MatchMakerSession session) {
		TableMergeRules newMergeStrategy = new TableMergeRules();
		newMergeStrategy.setParent(parent);
		newMergeStrategy.setName(getName());
		newMergeStrategy.setSession(session);
		newMergeStrategy.setDeleteDup(isDeleteDup());
		newMergeStrategy.setTableName(getTableName());
		newMergeStrategy.setCatalogName(getCatalogName());
		newMergeStrategy.setSchemaName(getSchemaName());
		newMergeStrategy.setParentTable(getParentTable());
		newMergeStrategy.setChildMergeAction(getChildMergeAction());
		newMergeStrategy.setVisible(isVisible());
		try {
			if (tableIndex.isUserCreated()) {
				newMergeStrategy.setTableIndex(new SQLIndex(getTableIndex()));
			} else {
				newMergeStrategy.setTableIndex(getTableIndex());
			}
		} catch (ArchitectException e) {
			throw new ArchitectRuntimeException(e);
		}

		for (ColumnMergeRules c : getChildren()) {
			ColumnMergeRules newColumnMergeRules = c.duplicate(newMergeStrategy,session);
			newMergeStrategy.addChild(newColumnMergeRules);
		}
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
		setName(table==null?null:DDLUtils.toQualifiedName(table));
	}

	public void setTableName(String sourceTableName) {
		cachableTable.setTableName(sourceTableName);
		setName(DDLUtils.toQualifiedName(getCatalogName(),getSchemaName(),sourceTableName));
	}

	public boolean isDeleteDup() {
		return deleteDup;
	}

	public void setDeleteDup(boolean deleteDup) {
		setDeleteDup(deleteDup, false);
	}
	
	public void setDeleteDup(boolean deleteDup, boolean isUndo) {
		boolean oldValue = this.deleteDup;
		this.deleteDup = deleteDup;
		if (deleteDup) {
			for (ColumnMergeRules cmr : getChildren()) {
				if (cmr.getActionType() == MergeActionType.USE_MASTER_VALUE) {
					cmr.setActionType(MergeActionType.AUGMENT, true);
				}
			}
		} else {
			for (ColumnMergeRules cmr : getChildren()) {
				if (cmr.getActionType() == MergeActionType.AUGMENT) {
					cmr.setActionType(MergeActionType.USE_MASTER_VALUE, true);
				}
			}
		}
		getEventSupport().firePropertyChange("deleteDup", oldValue, this.deleteDup, isUndo);
	}
	

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Merge Strategy@"+System.identityHashCode(this)+"->'").append(getName()).append("' ");
		buf.append("Parent->'").append(getParent()).append("' ");
		buf.append("isDeletedDup()->'").append(isDeleteDup()).append("' ");
		return buf.toString();
	}

	public SQLIndex getTableIndex() throws ArchitectException {
		return tableIndex.getTableIndex();
	}

	public void setTableIndex(SQLIndex index) {
		tableIndex.setTableIndex(index);
	}
	
	/**
     * Gets the grandparent of this object in the MatchMaker object tree.  If the parent
     * (a folder) is null, returns null.
     */
    public Project getParentProject() {
        MatchMakerObject parentFolder = getParent();
        if (parentFolder == null) {
            return null;
        } else {
            return (Project) parentFolder.getParent();
        }
    }

    /**
     * Sets the parent of this object to be the merge rules folder of the given project object
     *
     * this will fire a <b>parent</b> changed event not a parent match event
     */
    public void setParentProject(Project grandparent) {
        if (grandparent == null) {
            setParent(null);
        } else {
            setParent(grandparent.getTableMergeRulesFolder());
        }
    }

	public Long getOid() {
		return oid;
	}

	public void setOid(Long oid) {
		this.oid = oid;
	}

	public SQLTable getParentTable() {
		return parentTable;
	}

	public void setParentTable(SQLTable parentTable) {
		setParentTable(parentTable, false);
	}
	
	public void setParentTable(SQLTable parentTable, boolean isUndo) {
		SQLTable oldValue = this.parentTable;
		this.parentTable = parentTable;
		
		List<SQLColumn> primarykeyCols = null;
		primarykeyCols = getParentTablePrimaryKey();
		
		// Set each imported key column combo box to null
		for (ColumnMergeRules cmr : getChildren()) {
			if (cmr.getImportedKeyColumn() != null) {
				cmr.setImportedKeyColumn(null, true);
			}
		}

		if (primarykeyCols != null) {
			for (SQLColumn column : primarykeyCols) {
				for (ColumnMergeRules cmr : getChildren()) {
					if (cmr.getColumnName().equals(column.getName())) {
						cmr.setImportedKeyColumn(column, true);
						break;
					}
				}
			}
		}
		getEventSupport().firePropertyChange("parentTable", oldValue, this.parentTable, isUndo);
	}

	public ChildMergeActionType getChildMergeAction() {
		return childMergeAction;
	}

	public void setChildMergeAction(ChildMergeActionType childMergeAction) {
		ChildMergeActionType oldValue = this.childMergeAction;
		this.childMergeAction = childMergeAction;
		getEventSupport().firePropertyChange("childMergeAction", oldValue, this.childMergeAction);
	}

	/**
	 * This returns whether this merge rule is the source table merge 
	 * rule of its parent project.
	 */
	public boolean isSourceMergeRule() {
		if (getParentProject() != null && getParentProject().getSourceTable() != null 
				&& getSourceTable() != null) {
			SQLTable matchSourceTable = getParentProject().getSourceTable();
			return matchSourceTable.equals(getSourceTable());
		} else {
			return false;
		}
	}
	
	/**
	 * Finds the parent table merge rule and returns the primary key of it.
	 * If no such table is found then throw an exception. Returns null if
	 * parentTable or the parent of this merge rule has not been set
	 */
	public List<SQLColumn> getParentTablePrimaryKey() {
		if (getParentTable() != null && getParentProject() != null) {
			for (TableMergeRules tmr : getParentProject().getTableMergeRules()) {
				if (tmr.getSourceTable().equals(parentTable)) {
					return tmr.getPrimaryKey();
				}
			}
		}
		return null;
	}
	
	/**
	 * Finds the primary key for the current table merge rule.
	 */
	public List<SQLColumn> getPrimaryKey()  {
		List<SQLColumn> columns = new ArrayList<SQLColumn>();
		
		if (isSourceMergeRule()) {
			try {
				for (SQLIndex.Column column : getParentProject().getSourceTableIndex().getChildren()) {
					columns.add(column.getColumn()); 
				}
			} catch (ArchitectException e) {
				throw new IllegalStateException("Error when getting primary keys");
			}
		} else {
			for (ColumnMergeRules cmr : getChildren()) {
				if (cmr.isInPrimaryKey()) {
					columns.add(cmr.getColumn());
				}
			}
		}
		return columns;
	}
	
	/**
	 * Finds the imported key for the current table merge rule.
	 */
	public List<ColumnMergeRules> getImportedKey() throws ArchitectException {
		List<ColumnMergeRules> columns = new ArrayList<ColumnMergeRules>();
		
		if (isSourceMergeRule()) {
			return null;
		} else {
			for (ColumnMergeRules cmr : getChildren()) {
				if (cmr.getImportedKeyColumn() != null) {
					columns.add(cmr);
				}
			}
		}
		return columns;
	}
}