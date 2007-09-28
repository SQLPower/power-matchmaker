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

import javax.swing.table.AbstractTableModel;

import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerTranslateWord;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;

/**
 * A Table model for the merge table rules. Shows the merge tables
 * in a JTable and allows user add/delete/reorder merge tables.
 * <p>
 * It has 4 columns:
 * <dl>
 * 		<dt>table catalog   <dd> merge table catalog in a combo box
 * 		<dt>table schema    <dd> merge table schema in a combo box
 * 		<dt>table name      <dd> merge table name in a combo box
 * 		<dt>delete dup ind  <dd> merge table delete dup ind in a check box
 * </dl>
 */
public class MergeTableRuleTableModel extends AbstractTableModel implements MatchMakerListener {

	private Match match;
	private SQLObjectChooser chooser;
	public MergeTableRuleTableModel(Match match, 
			MatchMakerSwingSession swingSession) {
		this.chooser = new SQLObjectChooser(swingSession);
		this.match = match;
	}
	
	public int getColumnCount() {
		return 4;
	}

	public int getRowCount() {
		return match.getTableMergeRules().size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return match.getTableMergeRules().get(rowIndex).getCatalogName();
		} else if (columnIndex == 1) {
			return match.getTableMergeRules().get(rowIndex).getSchemaName();
		} else if (columnIndex == 2) {
			return match.getTableMergeRules().get(rowIndex).getTableName();
		} else if ( columnIndex == 3) {
			return match.getTableMergeRules().get(rowIndex).isDeleteDup() ? "Yes" : "No";
		} else {
			return null;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0) {
			return SQLCatalog.class;
		} else if (columnIndex == 1) {
			return SQLSchema.class;
		} else if (columnIndex == 2) {
			return SQLTable.class;
		} else if ( columnIndex == 3) {
			return String.class;
		}
		return super.getColumnClass(columnIndex);
	}
	
	@Override
	public String getColumnName(int columnIndex) {
		if (columnIndex == 0) {
			return chooser.getCatalogTerm().getText();
		} else if (columnIndex == 1) {
			return chooser.getSchemaTerm().getText();
		} else if (columnIndex == 2) {
			return "Name";
		} else if ( columnIndex == 3) {
			return "Delete Duplicates?";
		}
		return null;
	}

    public void mmChildrenInserted(MatchMakerEvent evt) {
        if(evt.getSource() instanceof Match || evt.getSource() == match.getTableMergeRulesFolder()){
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
            for (Object columnMergeRule:evt.getChildren()){
                ((ColumnMergeRules) columnMergeRule).addMatchMakerListener(this);
            }
            fireTableRowsInserted(changedIndices.get(0), changedIndices.get(changedIndices.size()-1));
        }
    }

    public void mmChildrenRemoved(MatchMakerEvent evt) {
        if(evt.getSource() instanceof Match || evt.getSource() == match.getTableMergeRulesFolder()) {
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
            for (Object columnMergeRule:evt.getChildren()) {
                ((ColumnMergeRules) columnMergeRule).removeMatchMakerListener(this);
            }
            fireTableRowsDeleted(changedIndices.get(0), changedIndices.get(changedIndices.size()-1));
        }
    }

    public void mmPropertyChanged(MatchMakerEvent evt) { 
        if(evt.getSource() instanceof MatchMakerTranslateWord) {
            fireTableRowsUpdated(match.getTableMergeRules().indexOf(evt.getSource()), match.getTableMergeRules().indexOf(evt.getSource()));
        }
    }

    public void mmStructureChanged(MatchMakerEvent evt) {
        fireTableStructureChanged();
    }
}
