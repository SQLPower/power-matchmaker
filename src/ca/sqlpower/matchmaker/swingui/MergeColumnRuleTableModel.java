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
import java.util.Collections;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.MatchMakerUtils;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.ColumnMergeRules.MergeActionType;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;

/**
 * table model of the column merge rules that belongs to the table merge rule.
 * columns are column name, action type
 * row count = children count of table merge rule.
 */

public class MergeColumnRuleTableModel extends AbstractTableModel implements MatchMakerListener {

	private TableMergeRules mergeRule;

	public MergeColumnRuleTableModel(TableMergeRules mergeRule) {
		this.mergeRule = mergeRule;
		MatchMakerUtils.listenToHierarchy(this, this.mergeRule);
	}
	
	public int getColumnCount() {
		return 2;
	}

	public int getRowCount() {
		return mergeRule.getChildren().size();
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
			return mergeRule.getChildren().get(rowIndex).getColumn();
		} else if (columnIndex == 1) {
			return mergeRule.getChildren().get(rowIndex).getActionType();
		} else {
			throw new RuntimeException("getValueAt: Unexcepted column index:"+columnIndex);
		}		
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		ColumnMergeRules rule = mergeRule.getChildren().get(rowIndex);
		if (columnIndex == 0) {
			rule.setColumn((SQLColumn) aValue);
		} else if (columnIndex == 1) {
			rule.setActionType((MergeActionType) aValue);
		} else {
			throw new RuntimeException("setValueAt: Unexcepted column index:"+columnIndex);
		}
		fireTableChanged(new TableModelEvent(this,rowIndex));
	}

    public void mmChildrenInserted(MatchMakerEvent evt) {
        if(evt.getSource() == mergeRule){
            int[] changed = evt.getChangeIndices();
            ArrayList<Integer> changedIndices = new ArrayList<Integer>();
            for (int selectedRowIndex:changed){
                changedIndices.add(new Integer(selectedRowIndex));
            }
            Collections.sort(changedIndices);
            for (int i=1; i < changedIndices.size(); i++){
                if (changedIndices.get(i-1)!=changedIndices.get(i)-1){
                    fireTableStructureChanged();
                    return;
                }
            }

            fireTableRowsInserted(changedIndices.get(0), changedIndices.get(changedIndices.size()-1));
        }
    }

    public void mmChildrenRemoved(MatchMakerEvent evt) {
        if(evt.getSource() == mergeRule) {
            int[] changed = evt.getChangeIndices();
            ArrayList<Integer> changedIndices = new ArrayList<Integer>();
            for (int selectedRowIndex:changed){
                changedIndices.add(new Integer(selectedRowIndex));
            }
            Collections.sort(changedIndices);
            for (int i=1; i < changedIndices.size(); i++) {
                if (changedIndices.get(i-1)!=changedIndices.get(i)-1) {
                    fireTableStructureChanged();
                    return;
                }
            }

            fireTableRowsDeleted(changedIndices.get(0), changedIndices.get(changedIndices.size()-1));
        }
    }

    public void mmPropertyChanged(MatchMakerEvent evt) { 
        if(evt.getSource() instanceof ColumnMergeRules) {
            fireTableRowsUpdated(mergeRule.getChildren().indexOf(evt.getSource()), mergeRule.getChildren().indexOf(evt.getSource()));
        }
    }

    public void mmStructureChanged(MatchMakerEvent evt) {
        fireTableStructureChanged();
    }
}