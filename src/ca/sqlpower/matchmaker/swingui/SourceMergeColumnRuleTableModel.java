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

package ca.sqlpower.matchmaker.swingui;

import java.util.ArrayList;

import javax.swing.event.TableModelEvent;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.ColumnMergeRules.MergeActionType;
import ca.sqlpower.swingui.SPSUtils;

/**
 * Implementation of {@link AbstractMergeColumnRuleTableModel}, table model for column 
 * merge rules of a source merge rule. It has two columns: column name and action.
 */
public class SourceMergeColumnRuleTableModel extends
		AbstractMergeColumnRuleTableModel {
	
	public SourceMergeColumnRuleTableModel(TableMergeRules mergeRule) {
		super(mergeRule);
		updatePrimaryKeys();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0) {
			return SQLColumn.class;
		} else if (columnIndex == 1) {
			return ColumnMergeRules.MergeActionType.class;
		} else {
			throw new IllegalArgumentException("unknown columnIndex: "+ columnIndex);
		}
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int column) {
		if (column == 0) {
			return "Column";
		} else if (column == 1) {
			return "Action";
		} else {
			throw new RuntimeException("getColumnName: Unexcepted column index:"+column);
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return mergeRule.getChildren().get(rowIndex).getColumn();
		} else if (columnIndex == 1) {
			if (primaryKeys.contains(rowIndex)) {
				return ColumnMergeRules.MergeActionType.NA;
			} else {
				return mergeRule.getChildren().get(rowIndex).getActionType();
			}
		} else {
			throw new RuntimeException("getValueAt: Unexcepted column index:"+columnIndex);
		}	
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 1 && !primaryKeys.contains(rowIndex)) {
			return true;
		} else {
			return false;
		}
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		ColumnMergeRules rule = mergeRule.getChildren().get(rowIndex);
		if (columnIndex == 0) {
			rule.setColumn((SQLColumn) aValue);
		} else if (columnIndex == 1) {
			if (!primaryKeys.contains(rowIndex)) {
				rule.setActionType((MergeActionType) aValue);
			} else {
				// Do not set the value if the cell is a primary key
				return;
			}
		} else {
			throw new RuntimeException("setValueAt: Unexcepted column index:"+columnIndex);
		}
		fireTableChanged(new TableModelEvent(this,rowIndex));
	}
    
	  /**
	   * This updates the list of primary keys in the source or parent table
	   * of the merge rule. This is used to decide which column in the table
	   * to set as non edit-able.
	   */
		protected void updatePrimaryKeys() {
		primaryKeys = new ArrayList<Integer>();
		try {
			SQLIndex tableIndex = mergeRule.getTableIndex();
			if (tableIndex != null) {
				for (int i = 0; i < getRowCount(); i++) {
					SQLColumn column = (SQLColumn) getValueAt(i, 0);
					if (tableIndex.getChildByName(column.getName()) != null) {
						primaryKeys.add(i);
					}
				}
			}
		} catch (ArchitectException e) {
			MatchMakerSwingSession session = (MatchMakerSwingSession) mergeRule.getSession();
			SPSUtils.showExceptionDialogNoReport(session.getFrame(),
					"Failed to retrieve primary keys of source table", e);
		}
	}

}
