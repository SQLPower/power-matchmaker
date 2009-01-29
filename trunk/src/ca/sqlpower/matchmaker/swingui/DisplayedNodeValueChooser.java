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

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.swingui.table.TableUtils;

/**
 * A dialog that is used to create a list of columns that will later
 * be used for displaying the label of a node in the graph. The columns
 * are the SQLColumns of the table we are de-duping on.
 */
public class DisplayedNodeValueChooser {
	Logger logger = Logger.getLogger(DisplayedNodeValueChooser.class);

	/**
	 * This class represents the table row model of the pick your own
	 * display value for a node in the graph. This will have three columns,
	 * one for the check box, one for the SQLColumn's name and one for
	 * the position in the display name of the SQLColumn's value. 
	 */
	private class CustomTableColumn implements Comparable<CustomTableColumn> {
		/**
		 * A flag denoting whether or not the SQLColumn's value should
		 * appear in the graph display.
		 */
		private boolean inDisplay;
		
		/**
		 * The position in the display name the value will appear at.
		 * Indexes start at 1, not 0.
		 */
		private Integer position;
		
		/**
		 * The column whose value will be displayed in the graph if
		 * inDisplay is true.
		 */
		private SQLColumn sqlColumn;

		public CustomTableColumn(boolean key, Integer position, SQLColumn column) {
			this.inDisplay = key;
			this.position = position;
			this.sqlColumn = column;
		}

		public void setSqlColumn(SQLColumn column) {
			this.sqlColumn = column;
		}

		public void setKey(boolean key) {
			this.inDisplay = key;
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

		public boolean isInDisplay() {
			return inDisplay;
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
            return "[CustomTableColumn: inDisplay="+inDisplay+"; position="+position+"; column="+sqlColumn.getName()+"]";
        }
	}
	
	/**
	 * A table model that has three columns:
	 * 	1) One column of checkboxes
	 *  2) One column of text fields that contain the order of selection
	 *  3) One column of source table column types
	 * 
	 * TODO: Note that this class is very similar to the MatchMakerIndexBuilder.IndexColumnTableModel
	 * It may be worth investigating refactoring common elements out into a generic class  
	 */
	private class OrderedColumnChooserTableModel extends AbstractTableModel {
		/**
		 * A collection of all the SQLColumns that we will allow the user
		 * to select from when choosing a display value for a node in the
		 * graph. Corresponds to the list of columns in the table that
		 * we are doing match validation on.
		 */
		private List<CustomTableColumn> candidateColumns
							= new ArrayList<CustomTableColumn>();
		
		public OrderedColumnChooserTableModel(SQLTable sqlTable, List<SQLColumn> displayColumns) throws SQLObjectException {
			List<Integer> oldColumns = new ArrayList<Integer>();
			
			for (SQLColumn col : displayColumns) {
				oldColumns.add(sqlTable.getColumns().indexOf(col));
			}
			for (int i=0; i<sqlTable.getColumns().size(); i++) {
				SQLColumn column = sqlTable.getColumn(i);
				CustomTableColumn newCustomTableColumn = new CustomTableColumn(
				        (oldColumns.contains(new Integer(i))),
						(oldColumns.contains(new Integer(i)) ? oldColumns.indexOf(i) + 1 : null),
				        column);
				candidateColumns.add(newCustomTableColumn);
			}
		}

        @Override
        public String getColumnName(int column) {
            if (column == 0) {
                return "In Display?";
            } else if (column == 1) {
                return "Display Position";
            } else if (column == 2) {
                return "Column Name";
            } else {
                throw new IndexOutOfBoundsException("No such column in table: "+column);
            }
        }

		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return candidateColumns.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if ( columnIndex == 0 ) {
				return candidateColumns.get(rowIndex).isInDisplay();
			} else if ( columnIndex == 1 ) {
				return candidateColumns.get(rowIndex).getPosition();
			}  else if ( columnIndex == 2 ) {
				return candidateColumns.get(rowIndex).getSQLColumn();
			} else {
				throw new IllegalArgumentException("unknown columnIndex: " + columnIndex);
			}
		}


		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if ( columnIndex == 0 ) {
				return Boolean.class;
			} else if ( columnIndex == 1 ) {
				return Integer.class;
			}  else if ( columnIndex == 2 ) {
				return SQLColumn.class;
			} else {
				throw new IllegalArgumentException("unknown columnIndex: "+ columnIndex);
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				// If the column is removed from the display list, record it's position
				Integer removedPosition = candidateColumns.get(rowIndex).getPosition();
				candidateColumns.get(rowIndex).setKey((Boolean) aValue);
				if ((Boolean) aValue) {
					int max = 0;
					for (CustomTableColumn column : candidateColumns) {
						if (column.getPosition() != null && max < column.getPosition().intValue()) {
							max = column.getPosition().intValue();
						}
					}
					candidateColumns.get(rowIndex).setPosition(new Integer(max+1));
				} else {
					// Use the 'removed position' to ensure other columns that were
					// positioned at a higher index get shifted down.
					for (CustomTableColumn column: candidateColumns) {
						if (removedPosition != null && column.getPosition() != null && 
								column.getPosition() > removedPosition) {
							column.setPosition(column.getPosition() - 1);
						}
					}
					candidateColumns.get(rowIndex).setPosition(null);
				}
			} else if (columnIndex == 1) {
				candidateColumns.get(rowIndex).setPosition((Integer) aValue);
			} else if (columnIndex == 2) {
			} else {
				throw new IllegalArgumentException("unknown columnIndex: "+ columnIndex);
			}
			updateChosenColumns();
			fireTableDataChanged();
		}
		
		private void updateChosenColumns() {
			chosenColumns.clear();
			for (CustomTableColumn col : candidateColumns) {
				if (col.inDisplay) {
					chosenColumns.add(col);
				}
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if ( columnIndex == 0 ) {
				return true;
			} else if ( columnIndex == 1 ) {
				return true;
			}  else if ( columnIndex == 2 ) {
				return false;
			} else {
				throw new IllegalArgumentException("unknown columnIndex: "+ columnIndex);
			}
		}
		
		/**
		 * Sets all the values of the given column to the given value.
		 */
		public void setAllRowValues(Object val, int columnIndex) {
			for (int i = 0; i < getRowCount(); i++) {
				Object oldVal = getValueAt(i, columnIndex);
				if (!val.equals(oldVal)) {
					setValueAt(val, i, columnIndex);
				}
			}
		}
	}

	/**
	 * This is the list of the columns that the user has chosen in the
	 * order that they have chosen them in.
	 */
	private List<CustomTableColumn> chosenColumns = new ArrayList<CustomTableColumn>();

	private SourceTableNodeRenderer renderer;
	
	/**
	 * The table that sets and stores user selection of what to display.
	 */
	private JTable table;
	
	private OrderedColumnChooserTableModel occtm;

	/**
	 * Creates a dialog that allows the user to make a selection of
	 * columns in the provided match's result table. This selection is
	 * ordered and will be used for the display value of each node in the
	 * provided node renderer.
	 * 
	 * @throws SQLObjectException if there is a problem finding the columns
	 * 						in the source table of the match
	 */
	public DisplayedNodeValueChooser(SourceTableNodeRenderer renderer,
									Project project,
									List<SQLColumn> displayColumns)
									throws SQLObjectException {
		super();
		this.renderer = renderer;
		this.occtm = new OrderedColumnChooserTableModel(project.getSourceTable(), displayColumns);
	}
	
	public JComponent makeGUI() throws SQLObjectException {
		if (table == null) {
			table = new JTable(occtm);
			TableUtils.fitColumnWidths(table, 250, 10);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		}
        return new JScrollPane(table);
	}

	/**
	 * Returns a list of SQLColumn objects representing the columns that
	 * were selected in the GUI, sorted in the order that the columns
	 * were selected (or set by the user)
	 * @return
	 */
	public List<SQLColumn> getChosenColumns() {
		occtm.updateChosenColumns();
		List<SQLColumn> chosen = new ArrayList<SQLColumn>();
		Collections.sort(chosenColumns);
		logger.debug("chosenColumns size = " + chosenColumns.size());
		for (CustomTableColumn col : chosenColumns) {
			chosen.add(col.getSQLColumn());
		}
		return chosen;
	}
	
	public void setChosenColumns(List<SQLColumn> selectedColumns) {
		for (CustomTableColumn ctc : occtm.candidateColumns) {
			ctc.setKey(selectedColumns.contains(ctc.getSQLColumn()));
		}
		occtm.updateChosenColumns();
	}
	
	public void setAllDefaultChosen(boolean b) {
		occtm.setAllRowValues(b, 0);
	}
	
	public SourceTableNodeRenderer getRenderer() {
		return renderer;
	}
}
