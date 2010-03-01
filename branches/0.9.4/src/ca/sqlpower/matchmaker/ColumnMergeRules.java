/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;

public class ColumnMergeRules extends AbstractMatchMakerObject<ColumnMergeRules,MatchMakerObject> {

	private static final Logger logger = Logger.getLogger(ColumnMergeRules.class);

    /**
     * An enumeration of all possible types of actions that can be
     * performed during a column-to-column merge operation.  The
     * toString() and getText() methods are equivalent; they both return
     * a user-friendly (assuming the user speaks English) description
     * of the action type.  For long-term storage of one of these action
     * types, use the {@link #name()} method. We promise not to alter the
     * names in the future, but the text might change.
     */
	public static enum MergeActionType {
        
        /**
         * This action type indicates that the corresponding column in
         * the master record should be left untouched by the merge.
         */
		USE_MASTER_VALUE("Use master value"),
		
		/**
         * This action type indicates that the corresponding column in
         * the master record should be set to the duplicate record's 
         * value if it is null.
         */
        AUGMENT("Augment nulls"),
        
        /**
         * This action type indicates that the corresponding column in
         * the master record should be concatenated with the duplicate 
         * record's value.
         */
        CONCAT("Concatenate"),
        
        /**
         * This action type indicates that the corresponding column in
         * the master record should be replaced with the duplicate 
         * record's value if it has a higher value.
         */
        MIN("Minimum"),
        
        /**
         * This action type indicates that the corresponding column in
         * the master record should be replaced with the duplicate 
         * record's value if it has a lower value.
         */
        MAX("Maximum"),
        
        /**
         * This action type indicates that the corresponding column in
         * the master record should be added to the duplicate record's
         * value.
         */
        SUM("Sum"),
        
        /**
         * This action type indicates that the corresponding column in
         * the master record is in the primary key and shall not be
         * changed during a merge.
         */
        NA("Not applicable");
		
        /**
         * The human-readable English text shown to the user for
         * this action type.
         */
        private final String text;
        
        /**
         * Private constructor, only possible for internal use of this enum.
         */
        private MergeActionType(String text) {
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

	public class ColumnMergeRulesCachableColumn extends CachableColumn {
		public ColumnMergeRulesCachableColumn() {
			super(ColumnMergeRules.this, "column");
		}
		
		public SQLTable getTable() {
			TableMergeRules tableMergeRules = (TableMergeRules) eventSource.getParent();
	        if (tableMergeRules == null) throw new NullPointerException("Not attached to a parent");
	        SQLTable st = tableMergeRules.getSourceTable();
			return st;
		}

	}
	
	public class ColumnMergeRulesImportedCachableColumn extends CachableColumn {
		public ColumnMergeRulesImportedCachableColumn() {
			super(ColumnMergeRules.this, "importedKeyColumn");
		}
		
		public SQLTable getTable() {
			TableMergeRules tableMergeRules = (TableMergeRules) eventSource.getParent();
	        if (tableMergeRules == null) throw new NullPointerException("Not attached to a parent");
	        if (tableMergeRules.getParentMergeRule() == null) {
	        	return null;
	        } else {
	        	SQLTable st = tableMergeRules.getParentMergeRule().getSourceTable();
	        	return st;
	        }
		}

	}
	
	/**
	 * This object's unique ID.  Required by Hibernate.
	 */
	@SuppressWarnings("unused")
	private long oid;
	
	private MergeActionType actionType = MergeActionType.USE_MASTER_VALUE;
	
	private boolean inPrimaryKey = false;
	
	private String updateStatement;
	
	private ColumnMergeRulesCachableColumn cachedColumn = new ColumnMergeRulesCachableColumn();
	
	private ColumnMergeRulesImportedCachableColumn importedCachedColumn = new ColumnMergeRulesImportedCachableColumn();
	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((getColumn() == null) ? 0 : getColumn().hashCode());
		result = PRIME * result + ((getParent() == null) ? 0 : getParent().hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ColumnMergeRules))
			return false;
		final ColumnMergeRules other = (ColumnMergeRules) obj;
		if (getColumn() == null) {
			if (other.getColumn() != null)
				return false;
		} else if (!getColumn().equals(other.getColumn()))
			return false;
		if (getParent() == null) {
			if (other.getParent() != null)
				return false;
		} else if (!getParent().equals(other.getParent()))
			return false;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName())) {
			return false;
		}
		if (getActionType() == null) {
			if (other.getActionType() != null)
				return false;
		} else if (!getActionType().equals(other.getActionType())) {
			return false;
		}
		if (isInPrimaryKey() != other.isInPrimaryKey()) {
			return false;
		}
		if (getImportedKeyColumn() == null) {
			if (other.getImportedKeyColumn() != null)
				return false;
		} else if (!getImportedKeyColumn().equals(other.getImportedKeyColumn())) {
			return false;
		}
		
		return (getUpdateStatement() == null) ? (other.getUpdateStatement() == null) : (getUpdateStatement().equals(other.getUpdateStatement()));
	}

	/**
	 * Creates a copy of this instance.
	 */
	public ColumnMergeRules duplicate(MatchMakerObject parent,
			MatchMakerSession session) {
		logger.debug("Duplicating...");
		ColumnMergeRules columnRule = new ColumnMergeRules();
		columnRule.setColumn(getColumn());
		columnRule.setParent(parent);
		columnRule.setSession(session);
		columnRule.setName(getName());
		columnRule.setActionType(actionType);
		columnRule.setInPrimaryKey(inPrimaryKey);
		columnRule.setImportedKeyColumn(getImportedKeyColumn());
		columnRule.setUpdateStatement(updateStatement);
		columnRule.setVisible(isVisible());
		return columnRule;
	}


	public MergeActionType getActionType() {
		return actionType;
	}
	
	public void setActionType(MergeActionType actionType) {
		MergeActionType oldValue = this.actionType;
		this.actionType = actionType;
		getEventSupport().firePropertyChange("actionType", oldValue, this.actionType);
	}

	@Override
	public String getName() {
		return getColumnName();
	}

	public SQLColumn getColumn() {
			return cachedColumn.getColumn();
	}


	public String getColumnName() {
		return cachedColumn.getColumnName();
	}


	public void setColumn(SQLColumn column) {
		cachedColumn.setColumn(column);
	}

	public void setColumnName(String columnName) {
		cachedColumn.setColumnName(columnName);
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("ColumnMergeRule ").append(getName());
		buf.append(", Parent: ").append(getParent());
		buf.append(", ActionType: ").append(getActionType());
		return buf.toString();
	}
	
	@Override
	public boolean allowsChildren() {
		return false;
	}

	public boolean isInPrimaryKey() {
		return inPrimaryKey;
	}


	public void setInPrimaryKey(boolean inPrimaryKey) {
		boolean oldValue = this.inPrimaryKey;
		this.inPrimaryKey = inPrimaryKey;
		getEventSupport().firePropertyChange("inPrimaryKey", oldValue, this.inPrimaryKey);
	}
	
	public void setInPrimaryKeyAndAction(boolean inPrimaryKey) {
		try {
			startCompoundEdit();
			setInPrimaryKey(inPrimaryKey);
			if (inPrimaryKey) {
				setActionType(MergeActionType.NA);
			} else if (getImportedKeyColumn() == null){
				setActionType(MergeActionType.USE_MASTER_VALUE);
			}
		} finally {
			endCompoundEdit();
		}
	}

	public SQLColumn getImportedKeyColumn() {
		return importedCachedColumn.getColumn();
	}

	public String getImportedKeyColumnName() {
		return importedCachedColumn.getColumnName();
	}
	
	public void setImportedKeyColumn(SQLColumn importedKeyColumn) {
		importedCachedColumn.setColumn(importedKeyColumn);
	}
	
	public void setImportedKeyColumnName(String columnName) {
		importedCachedColumn.setColumnName(columnName);
	}
	
	public void setImportedKeyColumnAndAction(SQLColumn importedKeyColumn) {
		if (getImportedKeyColumn() == importedKeyColumn) return;
		try {
			startCompoundEdit();
			setImportedKeyColumn(importedKeyColumn);
			if (importedKeyColumn != null) {
				setActionType(MergeActionType.NA);
			} else if (!isInPrimaryKey()) {
				setActionType(MergeActionType.USE_MASTER_VALUE);
			}
		} finally {
			endCompoundEdit();
		}
	}

	public String getUpdateStatement() {
		return updateStatement;
	}


	public void setUpdateStatement(String updateStatement) {
		String oldValue = this.getUpdateStatement();
		this.updateStatement = updateStatement;
		getEventSupport().firePropertyChange("updateStatement", oldValue, this.updateStatement);
	}
}
