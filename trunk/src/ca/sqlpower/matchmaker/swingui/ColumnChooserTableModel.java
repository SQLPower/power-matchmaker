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

package ca.sqlpower.matchmaker.swingui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;

public class ColumnChooserTableModel extends AbstractTableModel{

	private final List<CustomTableColumn> candidateColumns = 
		new ArrayList<CustomTableColumn>();
	private boolean modified = false;
	private final boolean showPosition;
	
	public ColumnChooserTableModel(SQLTable sqlTable, SQLIndex oldIndex, boolean showPosition) throws SQLObjectException {
		this.showPosition = showPosition;
		for ( SQLColumn column : sqlTable.getColumns()) {
			int positionInIndex = -1;
			if (oldIndex != null){
				positionInIndex = oldIndex.getIndexOfChildByName(column.getName());
			}
			candidateColumns.add(new CustomTableColumn(
							(positionInIndex >= 0),
							(positionInIndex >= 0 ? positionInIndex +1 : null),
							column));
		}
	}

	@Override
	public String getColumnName(int column) {
		if (column == 0) {
			return "In Index?";
		} else if (column == 1 && showPosition) {
			return "Index Seq";
		} else {
			return "Column Name";
		} 
	}

	public int getColumnCount() {
		return showPosition ? 3 : 2;
	}

	public int getRowCount() {
		return candidateColumns.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		CustomTableColumn column = candidateColumns.get(rowIndex);
		if (columnIndex == 0) {
			return column.isKey();
		} else if (columnIndex == 1 && showPosition) {
			return column.getPosition();
		}  else {
			return column.getSQLColumn();
		} 
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0) {
			return Boolean.class;
		} else if (columnIndex == 1 && showPosition) {
			return Integer.class;
		}  else {
			return SQLColumn.class;
		} 
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		CustomTableColumn column = candidateColumns.get(rowIndex);
		modified = true;
		if (columnIndex == 0) {
			Integer removedPosition = candidateColumns.get(rowIndex).getPosition();
			boolean value = (Boolean) aValue;
			if (column.isKey() != value) {
				column.setKey(value);
				if (value && showPosition) {
					int max = 0;
					for ( CustomTableColumn ctc : candidateColumns ) {
						if (ctc.getPosition() != null && max < ctc.getPosition().intValue()) {
							max = ctc.getPosition();
						}
					}
					candidateColumns.get(rowIndex).setPosition(new Integer(max+1));
				} else {
					//use removedPosition to shift all columns positioned at a higher index down by one
					for (CustomTableColumn ctc : candidateColumns ) {
						if (removedPosition != null && ctc.getPosition() != null && 
								removedPosition.intValue() < ctc.getPosition().intValue()) {
							ctc.setPosition(ctc.getPosition() - 1);
						}
					}
					candidateColumns.get(rowIndex).setPosition(null);
				}
				fireTableDataChanged();
			}
		} else if (columnIndex == 1 && showPosition) {
			column.setPosition((Integer) aValue);
		} else {
			throw new IllegalArgumentException("unknown columnIndex or index not editable: "+ columnIndex);
		}
		
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return true;
		} else if (columnIndex == 1 && showPosition) {
			return true;
		} else {
			return false;
		}
	}

	public List<SQLColumn> getSelectedSQLColumns() {
		List<CustomTableColumn> selectedColumns = new ArrayList<CustomTableColumn>();
		for (CustomTableColumn column : candidateColumns) {
			if (column.isKey()) selectedColumns.add(column);
		}
		Collections.sort(selectedColumns);
		
		List<SQLColumn> sqlColumns = new ArrayList<SQLColumn>();
		for (CustomTableColumn column : selectedColumns) {
			sqlColumns.add(column.getSQLColumn());
		}
		
		return sqlColumns;
	}

	/**
	 * This class represents the table row model of the pick your own
	 * column for index table. which has only 3 columns.
	 *
	 */
	private class CustomTableColumn implements Comparable<CustomTableColumn> {
		private boolean key;
		private Integer position;
		private SQLColumn sqlColumn;

		public CustomTableColumn(boolean key, Integer position, SQLColumn column) {
			this.key = key;
			this.position = position;
			this.sqlColumn = column;
		}

		public void setSqlColumn(SQLColumn column) {
			this.sqlColumn = column;
		}

		public void setKey(boolean key) {
			this.key = key;
			if ( !key ) {
				position = null;
			}
		}

		public void setPosition(Integer position) {
			this.position = position;
		}

		public SQLColumn getSQLColumn() {
			return sqlColumn;
		}

		public boolean isKey() {
			return key;
		}

		public Integer getPosition() {
			return position;
		}

		public int compareTo(CustomTableColumn o) {
			if (getPosition() == null)
				return -1;
			else if ( o.getPosition() == null )
				return 1;
			else
				return getPosition().compareTo(o.getPosition());
		}

		@Override
		public String toString() {
			return "[CustomTableColumn: key="+key+"; position="+position+"; column="+sqlColumn.getName()+"]";
		}
	}
	
	public boolean isModified() {
		return modified;
	}
}
