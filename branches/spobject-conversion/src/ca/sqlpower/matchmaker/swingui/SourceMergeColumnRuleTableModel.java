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

package ca.sqlpower.matchmaker.swingui;

import javax.swing.event.TableModelEvent;

import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.ColumnMergeRules.MergeActionType;
import ca.sqlpower.sqlobject.SQLColumn;

/**
 * Implementation of {@link AbstractMergeColumnRuleTableModel}, table model for column 
 * merge rules of a source merge rule. It has two columns: column name and action.
 */
public class SourceMergeColumnRuleTableModel extends
		AbstractMatchMakerTableModel {
	
	public SourceMergeColumnRuleTableModel(TableMergeRules mergeRule) {
		super(mergeRule);
	}

	public int getColumnCount() {
		return 2;
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
	public String getColumnName(int column) {
		if (column == 0) {
			return "Column";
		} else if (column == 1) {
			return "Action";
		} else {
			throw new RuntimeException("getColumnName: Unexcepted column index:"+column);
		}
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return mmo.getChildren().get(rowIndex).getColumn();
		} else if (columnIndex == 1) {
			return mmo.getChildren().get(rowIndex).getActionType();
		} else {
			throw new RuntimeException("getValueAt: Unexcepted column index:"+columnIndex);
		}	
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 1 && getValueAt(rowIndex, columnIndex) != MergeActionType.NA;
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		ColumnMergeRules rule = mmo.getChildren().get(rowIndex);
		if (columnIndex == 0) {
			rule.setColumn((SQLColumn) aValue);
		} else if (columnIndex == 1) {
			rule.setActionType((MergeActionType) aValue);
		} else {
			throw new RuntimeException("setValueAt: Unexcepted column index:"+columnIndex);
		}
		fireTableChanged(new TableModelEvent(this,rowIndex));
	}
}
