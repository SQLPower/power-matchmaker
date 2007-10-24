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

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;

public class ColumnMergeRules extends AbstractMatchMakerObject<ColumnMergeRules,MatchMakerObject> {

	private static final Logger logger = Logger.getLogger(ColumnMergeRules.class);

	public enum MergeActionType {
		USE_MASTER_VALUE, AUGMENT, CONCAT, MIN, MAX, SUM, NA;
		
		public static MergeActionType getActionTypeFromString(String type) {
			if ("Use master value".equals(type)){
				return USE_MASTER_VALUE;
			} else if ("Augment nulls".equals(type)){
				return AUGMENT;
			} else if ("Concatenate".equals(type)){
				return CONCAT;
			} else if ("Minimum".equals(type)){
				return MIN;
			} else if ("Maximum".equals(type)){
				return MAX;
			} else if ("Sum".equals(type)){
				return SUM;
			} else if ("Not applicable".equals(type)) {
				return NA;
			} else {
				throw new IllegalStateException("No such merge action type: " + type);
			} 
		}
		
		@Override
		public String toString() {
			switch (this) {
			case USE_MASTER_VALUE:
				return "Use master value";
			case AUGMENT:
				return "Augment nulls";
			case CONCAT:
				return "Concatenate";
			case MIN:
				return "Minimum";
			case MAX:
				return "Maximum";
			case SUM:
				return "Sum";
			case NA:
				return "Not applicable";
			default:
				throw new IllegalStateException("Invalid enumeration");
			}
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
	
	/**
	 * This object's unique ID.  Required by Hibernate.
	 */
	@SuppressWarnings("unused")
	private long oid;
	
	private boolean updateAction;
	
	private MergeActionType actionType = MergeActionType.USE_MASTER_VALUE;
	
	private boolean inPrimaryKey = false;
	
	private SQLColumn importedKeyColumn;
	
	private String updateStatement;
	
	private ColumnMergeRulesCachableColumn cachedColumn = new ColumnMergeRulesCachableColumn();
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
		columnRule.setUpdateAction(updateAction);
		columnRule.setInPrimaryKey(inPrimaryKey);
		columnRule.setImportedKeyColumn(importedKeyColumn);
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


	public SQLColumn getImportedKeyColumn() {
		return importedKeyColumn;
	}


	public void setImportedKeyColumn(SQLColumn importedKeyColumn) {
		SQLColumn oldValue = this.getImportedKeyColumn();
		this.importedKeyColumn = importedKeyColumn;
		getEventSupport().firePropertyChange("importedKeyColumn", oldValue, this.importedKeyColumn);
	}


	public boolean isUpdateAction() {
		return updateAction;
	}

	public void setUpdateAction(boolean updateAction) {
		boolean oldValue = this.updateAction;
		this.updateAction = updateAction;
		getEventSupport().firePropertyChange("updateAction", oldValue, this.updateAction);
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
