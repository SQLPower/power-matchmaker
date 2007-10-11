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
		IGNORE, AUGMENT, CONCAT, MIN, MAX, SUM;
		
		public static MergeActionType getActionTypeFromString(String type) {
			if ("IGNORE".equals(type)){
				return IGNORE;
			} else if ("AUGMENT".equals(type)){
				return AUGMENT;
			} else if ("CONCAT".equals(type)){
				return CONCAT;
			} else if ("MIN".equals(type)){
				return MIN;
			} else if ("MAX".equals(type)){
				return MAX;
			} else if ("SUM".equals(type)){
				return SUM;
			} else {
				throw new IllegalStateException("No such merge action type: " + type);
			} 
		}
		
		@Override
		public String toString() {
			switch (this) {
			case IGNORE:
				return "IGNORE";
			case AUGMENT:
				return "AUGMENT";
			case CONCAT:
				return "CONCAT";
			case MIN:
				return "MIN";
			case MAX:
				return "MAX";
			case SUM:
				return "SUM";
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
	
	private MergeActionType actionType;
	
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
		return true;
	}

	/**
	 * Creates a copy of this instance.
	 */
	public ColumnMergeRules duplicate(MatchMakerObject parent,
			MatchMakerSession session) {
		logger.debug("Duplicating...");
		ColumnMergeRules columnRule = new ColumnMergeRules();
		columnRule.setParent(parent);
		columnRule.setSession(session);
		columnRule.setName(getName());
		columnRule.setActionType(actionType);
		columnRule.setUpdateAction(updateAction);
		columnRule.setColumn(getColumn());
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



	public boolean isUpdateAction() {
		return updateAction;
	}


	public void setUpdateAction(boolean updateAction) {
		boolean oldValue = this.updateAction;
		this.updateAction = updateAction;
		getEventSupport().firePropertyChange("updateAction", oldValue, this.updateAction);
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
	
}
