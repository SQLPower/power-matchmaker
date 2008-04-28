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

package ca.sqlpower.matchmaker.util;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.swingui.CleanupModel;
import ca.sqlpower.swingui.table.TableUtils;

/**
 * This class implements the workarounds for the bugs in the jTable
 *
 */
public class EditableJTable extends JTable {

	private static final Logger logger = Logger.getLogger(EditableJTable.class);
	
	public EditableJTable() {
		this(null);
	}

	public EditableJTable(TableModel model) {
		super(model);
		putClientProperty("terminateEditOnFocusLost", true);
		setTableHeader(createDefaultTableHeader());
	}
	
	@Override
	public void removeNotify() {
		super.removeNotify();
		logger.debug("Table removed from hierarchy.  Cleaning up model...");
		if (getModel() instanceof CleanupModel) {
			((CleanupModel) getModel()).cleanup();
		}
	}
	
	@Override
	public void columnMarginChanged(ChangeEvent pE) {
		if (getEditingColumn() != -1 || getEditingRow() != -1) {
			editCellAt(0, 0);
		}
		super.columnMarginChanged(pE);
	}

	public String getTextForCell(int row, int col) {
		Object o = getValueAt(row, col);
		if (o != null) {
			return o.toString();
		} else {
			return "N/A";
		}
	}

	public int modelIndex(int viewIndex) {
		return viewIndex;
	}

    /**
     * Just logs the call and forwards to superclass implementation.
     */
    @Override
    public void setModel(TableModel dataModel) {
        logger.debug("Table Model change for EditableJTable "+this+
                ". Old model: "+getModel()+" new model: "+dataModel);
        super.setModel(dataModel);
    }
    
    /**
     * Just logs the call and forwards to superclass implementation.
     */
    @Override
    public void setSelectionModel(ListSelectionModel newModel) {
        logger.debug("Table Selection Model change for EditableJTable "+this+
                ". Old model: "+getSelectionModel()+" new model: "+newModel);
        super.setSelectionModel(newModel);
    }
    
    @Override
    public TableCellEditor getCellEditor() {
        TableCellEditor editor = super.getCellEditor();
        logger.debug("EditableJTable.getCellEditor(): returning "+editor);
        return editor;
    }
    
    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        TableColumn tableColumn = getColumnModel().getColumn(column);

        TableCellEditor returnVal = super.getCellEditor(row, column);
        logger.debug("EditableJTable.getCellEditor("+row+","+column+"): returning "+returnVal
                +" (tableColumn.getCellEditor()="+tableColumn.getCellEditor()+")");
        return returnVal;
    }
    
	/**
	 * Taken from the Bug ID:	4292511 of bug parade as a workaround
	 * 
	 * Thanks milnep1!!
	 */
	protected JTableHeader createDefaultTableHeader() {
		return new JTableHeader(columnModel) {
			private int preferredHeight = -1;

			private Component getHeaderRenderer(int columnIndex) {
				TableColumn aColumn = getColumnModel().getColumn(columnIndex);
				TableCellRenderer renderer = aColumn.getHeaderRenderer();
				if (renderer == null) {
					renderer = getDefaultRenderer();
				}
				return renderer.getTableCellRendererComponent(getTable(), aColumn.getHeaderValue(), false, false, -1,
						columnIndex);
			}

			private int getPreferredHeight() {
				if (preferredHeight == -1) {
					preferredHeight = 0;
					TableColumnModel columnModel = getColumnModel();
					for (int column = 0; column < columnModel.getColumnCount(); column++) {
						Component comp = getHeaderRenderer(column);
						int rendererHeight = comp.getPreferredSize().height;
						preferredHeight = Math.max(preferredHeight, rendererHeight);
					}
				}
				return preferredHeight;
			}

			public Dimension getPreferredSize() {
				return new Dimension(super.getPreferredSize().width, getPreferredHeight());
			}

			public void columnAdded(TableColumnModelEvent e) {
				preferredHeight = -1;
				super.columnAdded(e);
			}

			public void columnRemoved(TableColumnModelEvent e) {
				preferredHeight = -1;
				super.columnRemoved(e);
			}
		};
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		super.tableChanged(e);
		//checks if the table has been initialized
		if (getTableHeader() != null)
			TableUtils.fitColumnWidths(this, 15);
	}
}
