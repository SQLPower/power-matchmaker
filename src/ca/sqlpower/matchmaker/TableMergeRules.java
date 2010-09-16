/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
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

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.ColumnMergeRules.MergeActionType;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.sqlobject.SQLTable;

/**
 *
 * Merge strategy handles a per table setup of the merge engine
 * The best way to think of this is a per row merge rules.
 */
public class TableMergeRules
	extends AbstractMatchMakerObject {

	private static final Logger logger = Logger.getLogger(TableMergeRules.class);
	/**
     * An enumeration of all possible types of actions that can be
     * performed during child table merge operation.  The
     * toString() and getText() methods are equivalent; they both return
     * a user-friendly (assuming the user speaks English) description
     * of the action type.  For long-term storage of one of these action
     * types, use the {@link #name()} method. We promise not to alter the
     * names in the future, but the text might change.
     */
	public static enum ChildMergeActionType {
        
        /**
         * This action type indicates that the all child records of the
         * duplicate record shall be deleted
         */
		DELETE_ALL_DUP_CHILD("Delete all child records of duplicate"),
		
		/**
         * This action type indicates that the child records of the
         * duplicate record shall have their imported key updated to the
         * master records' primary key and all other columns will be 
         * updated according to the sql if provided.
         */
		UPDATE_USING_SQL("Reassign child records, then update using the provided SQL"),
		
		/**
         * This action type indicates that the child records of the
         * duplicate record shall have their imported key updated to the
         * master records' primary key and the engine will fail if 
         * there are conflicts generated during this process.
         */
		UPDATE_FAIL_ON_CONFLICT("Reassign child records, fail on conflict"),
		
		/**
         * This action type indicates that the child records of the
         * duplicate record shall have their imported key updated to the
         * master records' primary key and the engine will delete the child 
         * record if the updated foreign key conflicts with another record.
         */
		UPDATE_DELETE_ON_CONFLICT("Reassign child records, delete on conflict"),
		
		/**
         * This action type indicates that the child records of the
         * duplicate record shall have their imported key updated to the
         * master records' primary key and the engine will merge the records
         * if the updated foreign key conflicts with another record.
         */
		MERGE_ON_CONFLICT("Reassign child records, merge on conflict");
		
        /**
         * The human-readable English text shown to the user for
         * this action type.
         */
        private final String text;
        
        /**
         * Private constructor, only possible for internal use of this enum.
         */
        private ChildMergeActionType(String text) {
            this.text = text;
        }
        
        /**
         * Returns the human-readable English text shown to the user for
         * this action type.
         */
        public String getText() {
            return text;
        }
        
        /**
         * Returns the human-readable English text shown to the user for
         * this action type.
         */
		@Override
		public String toString() {
            return getText();
		}
	}

	
	private Long oid;
	
	/**
	 * The action to take for this child table 
	 */
	private ChildMergeActionType childMergeAction;
		
	/**
	 * The table on which we're merging
	 */
	private CachableTable cachableTable = new CachableTable(this, "table");
	
	
	/**
	 * The index for table 
	 */
	private TableIndex tableIndex;
	
	/**
	 * The parent tableMergeRule
	 */
	private TableMergeRules parentMergeRule;
	
	public TableMergeRules() {
		//set defaults
		tableIndex = new TableIndex(this,cachableTable,"tableIndex");
		setChildMergeAction(ChildMergeActionType.UPDATE_FAIL_ON_CONFLICT);
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
		if (getParentMergeRule() == null) {
			if (other.getParentMergeRule() != null) {
				return false;
			}
		} else {
			if (getParentMergeRule() != other.getParentMergeRule()) {
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
		newMergeStrategy.setTableName(getTableName());
		newMergeStrategy.setSpDataSource(getSpDataSource());
		newMergeStrategy.setCatalogName(getCatalogName());
		newMergeStrategy.setSchemaName(getSchemaName());
		newMergeStrategy.setParentMergeRule(getParentMergeRule());
		newMergeStrategy.setChildMergeAction(getChildMergeAction());
		newMergeStrategy.setVisible(isVisible());
		try {
			if (tableIndex.isUserCreated()) {
				newMergeStrategy.setTableIndex(new SQLIndex(getTableIndex()));
			} else {
				newMergeStrategy.setTableIndex(getTableIndex());
			}
		} catch (SQLObjectException e) {
			throw new SQLObjectRuntimeException(e);
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
	
	public String getSpDataSource() {
		return cachableTable.getSPDataSourceName();
	}
	
	public void setSpDataSource(String spDataSourceName) {
		cachableTable.setSPDataSource(spDataSourceName);
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

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Merge Strategy->'").append(getName()).append("' ");
		buf.append("Parent->'").append(getParent()).append("' ");
		return buf.toString();
	}

	public SQLIndex getTableIndex() {
		try {
			return tableIndex.getTableIndex();
		} catch (SQLObjectException e) {
			throw new RuntimeException("Error getting index from table.", e);
		}
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
    
    public void deriveColumnMergeRules() {
    	if (getSourceTable() == null) {
    		throw new IllegalStateException(
    				"cannot derive column merge rules because source table is null");
    	}
    	try {
			for (SQLColumn column : getSourceTable().getColumns()) {
				ColumnMergeRules newRules = new ColumnMergeRules();
				newRules.setColumn(column);
				newRules.setActionType(MergeActionType.AUGMENT);
				addChild(newRules);
			}
		} catch (SQLObjectException e) {
			throw new RuntimeException("Error deriving column merge rules.", e);
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

	public TableMergeRules getParentMergeRule() {
		return parentMergeRule;
	}

	public void setParentMergeRule(TableMergeRules parentTable) {
		if (this.parentMergeRule == parentTable) return;
		TableMergeRules oldValue = this.parentMergeRule;
		this.parentMergeRule = parentTable;
		getEventSupport().firePropertyChange("parentMergeRule", oldValue, this.parentMergeRule);
	}
	
	public void setParentMergeRuleAndImportedKeys(TableMergeRules parentTable) {
		if (this.parentMergeRule == parentTable) return;
		try {
			startCompoundEdit();
			setParentMergeRule(parentTable);

			List<SQLColumn> primarykeyCols = getParentMergeRule().getPrimaryKey();
			// Set each imported key column combo box to null
			for (ColumnMergeRules cmr : getChildren()) {
				if (cmr.getImportedKeyColumn() != null) {
					cmr.setImportedKeyColumnAndAction(null);
				}
			}
			if (primarykeyCols != null) {
				for (SQLColumn column : primarykeyCols) {
					for (ColumnMergeRules cmr : getChildren()) {
						if (cmr.getColumnName() == null) {
							throw new IllegalStateException("Null column name in one of my column merge rules!");
						}
						if (column == null) {
							throw new IllegalStateException("Null primary key index column in parent table!");
						}
						if (cmr.getColumnName().equals(column.getName())) {
							cmr.setImportedKeyColumnAndAction(column);
							break;
						}
					}
				}
			}
		} finally {
			endCompoundEdit();
		}
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
			SQLTable matchSourceTable= getParentProject().getSourceTable();
			return matchSourceTable.equals(getSourceTable());
		} else {
			return false;
		}
	}

	public List<SQLColumn> getPrimaryKeyFromIndex()  {
		if (getSourceTable() == null) {
			return null;
		}
		
		List<SQLColumn> columns = new ArrayList<SQLColumn>();
		SQLIndex index = getTableIndex();
		if (index == null) index = getSourceTable().getPrimaryKeyIndex();
		if (index == null) {
			return null;
		}
		for (SQLIndex.Column column : index.getChildren(SQLIndex.Column.class)) {
		    if (logger.isDebugEnabled()) {
				try {
					logger.debug(BeanUtils.describe(column));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (column.getColumn() == null) {
		        throw new IllegalStateException("Found an index column with a null column name!");
		    }
			columns.add(column.getColumn()); 
		}
		return columns;
	}
	/**
	 * Finds the primary key for the current table merge rule.
	 */
	public List<SQLColumn> getPrimaryKey()  {
		List<SQLColumn> columns;
		
		if (isSourceMergeRule()) {
			columns = getPrimaryKeyFromIndex();
		} else {
			columns = new ArrayList<SQLColumn>();
			for (ColumnMergeRules cmr : getChildren()) {
				if (cmr.isInPrimaryKey()) {
					columns.add(cmr.getColumn());
				}
			}
		}
		return columns;
	}
	
	/**
	 * Returns a {@link List} of {@link SQLColumn} of all columns in this merge rule's
	 * table that belong to a unique key, including the primary key and all alternate keys
	 */
	public List<SQLColumn> getUniqueKeyColumns() {
		List<SQLColumn> columns = new ArrayList<SQLColumn>();
		
		for (ColumnMergeRules cmr: getChildren()) {
			if (cmr.isInPrimaryKey() || cmr.getColumn().isUniqueIndexed()) {
				columns.add(cmr.getColumn());
			}
		}
		
		return columns;
	}
	
	/**
	 * Finds the imported key for the current table merge rule.
	 */
	public List<ColumnMergeRules> getImportedKey() throws SQLObjectException {
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
	
	public void fixColumnMergeAction() {
		//TODO: make a method that fixes the column 
		//merge rules actions when the index changes...
		
	}
}