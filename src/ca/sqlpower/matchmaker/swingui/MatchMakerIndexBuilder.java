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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLIndex.IndexType;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.table.TableUtils;
import ca.sqlpower.validation.RegExValidator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class MatchMakerIndexBuilder implements EditorPane {

	private static final Logger logger = Logger.getLogger(MatchMakerIndexBuilder.class);
	private Project project;
	private JDialog dialog;
	private JPanel panel;
	private List<CustomTableColumn> selectedColumns;
	private JTextField indexName;
	private MatchMakerSwingSession swingSession;
	private JTable columntable;
	private final SQLTable sqlTable;
	private final IndexColumnTableModel indexColumnTableModel;
	private boolean tableModified = false;

    /** Displays validation results */
    private StatusComponent statusComponent;

    /**
     * Handles the validation rules for this form.
     */
    private FormValidationHandler validationHandler;

    private final AbstractAction okAction = new AbstractAction("OK") {
    			public void actionPerformed(ActionEvent e) {
                    boolean success = doSave();
                    if (!success) {
                        JOptionPane.showMessageDialog(dialog, "Validation Error.  Can't save.");
                    } else {
                        dialog.dispose();
                    }
    			}
    		};

    private final AbstractAction cancelAction = new AbstractAction("Cancel") {
    			public void actionPerformed(ActionEvent e) {
    				if (hasUnsavedChanges()) {
    					int choice = JOptionPane.showOptionDialog(
    							dialog,
    							"Your index has unsaved changes", "Unsaved Changes", 0, 0, null,
                                new String[] { "Save", "Discard", "Cancel" }, "Save");
                        if (choice == 0) {
                            boolean success = doSave();
                            if (!success) {
                                JOptionPane.showMessageDialog(dialog, "Validation Error.  Can't save.");
                                return;
                            }
                        } else if (choice == 1) {
                            // fall through
                        } else if (choice == 2 || choice == -1) {
                            return;
                        } else {
                            throw new IllegalStateException("Unknown choice: "+choice);
                        }
    				}
    				dialog.dispose();
    			}
    		};


	private boolean isTableModified() {
		return tableModified;
	}

	private void setTableModified(boolean modified) {
		this.tableModified = modified;
	}

	public MatchMakerIndexBuilder(Project project, MatchMakerSwingSession swingSession) throws ArchitectException {
		this.project = project;
		this.swingSession = swingSession;

		sqlTable = project.getSourceTable();
		SQLIndex oldIndex = project.getSourceTableIndex();

		String name;
		if (oldIndex != null &&
				sqlTable.getIndexByName(oldIndex.getName()) == null) {
			name = oldIndex.getName();
		} else {
			for( int i=0; ;i++) {
				name = project.getSourceTableName()+"_UPK"+(i==0?"":String.valueOf(i));
				if (sqlTable.getIndexByName(name) == null) break;
			}
		}

        statusComponent = new StatusComponent();
        validationHandler = new FormValidationHandler(statusComponent);
		indexName = new JTextField(name,15);
		validationHandler.addValidateObject(indexName,
                new RegExValidator(
                        "[a-z_][a-z0-9_]*",
                        "Index name must be a valid SQL identifier",
                        false));
		indexColumnTableModel = new IndexColumnTableModel(sqlTable,oldIndex);
		columntable = new JTable(indexColumnTableModel);
		columntable.addColumnSelectionInterval(1, 1);
		TableUtils.fitColumnWidths(columntable, 6);

		dialog = new JDialog(swingSession.getFrame());
        dialog.setTitle("Index Column Chooser");
		buildUI();
		dialog.pack();
		dialog.setLocationRelativeTo(swingSession.getFrame());
		dialog.setVisible(true);
	}

	private void buildUI() {

		FormLayout layout = new FormLayout(
				"4dlu,fill:pref:grow,4dlu",
		// column1    2    3
				"10dlu,pref:grow,4dlu,pref:grow,4dlu,pref:grow,10dlu,fill:min(200dlu;pref):grow,4dlu,pref,4dlu");
		//       1     2         3    4         5    6         7     8                          9    10   11
		PanelBuilder pb;
		JPanel panel = logger.isDebugEnabled() ? new FormDebugPanel(layout)
				: new JPanel(layout);
		pb = new PanelBuilder(layout, panel);

		CellConstraints cc = new CellConstraints();

		JButton save = new JButton(okAction);
		JButton exit = new JButton(cancelAction);

        pb.add(statusComponent, cc.xy(2, 2));
		pb.add(new JLabel("Table: " + DDLUtils.toQualifiedName(project.getSourceTable())),
					cc.xy(2, 4));
		pb.add(indexName, cc.xy(2, 6));
		JScrollPane scrollPane = new JScrollPane(columntable);
        pb.add(scrollPane, cc.xy(2, 8, "f,f"));

		pb.add(ButtonBarFactory.buildOKCancelBar(save, exit), cc.xy(2,10));
		dialog.getContentPane().add(panel);
        dialog.getRootPane().setDefaultButton(save);
        SPSUtils.makeJDialogCancellable(dialog, cancelAction, false);
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

	private class IndexColumnTableModel extends AbstractTableModel {

		private List<CustomTableColumn> candidateColumns
							= new ArrayList<CustomTableColumn>();
		public IndexColumnTableModel(SQLTable sqlTable, SQLIndex oldIndex) throws ArchitectException {

			for ( SQLColumn column : sqlTable.getColumns()) {
                int positionInIndex = oldIndex.getIndexOfChildByName(column.getName());
				candidateColumns.add(
						new CustomTableColumn(
                                (positionInIndex >= 0),
								(positionInIndex >= 0 ? positionInIndex +1 : null),
                                column));
			}
		}

        @Override
        public String getColumnName(int column) {
            if (column == 0) {
                return "In Index?";
            } else if (column == 1) {
                return "Index Seq";
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
				return candidateColumns.get(rowIndex).isKey();
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
			setTableModified(true);
			if ( columnIndex == 0 ) {
				candidateColumns.get(rowIndex).setKey((Boolean) aValue);
				if ( (Boolean) aValue ) {
					int max = -1;
					for ( CustomTableColumn column : candidateColumns ) {
						if ( column.getPosition() != null && max < column.getPosition().intValue()) {
							max = column.getPosition().intValue();
						}
					}
					candidateColumns.get(rowIndex).setPosition(new Integer(max+1));
				} else {
					candidateColumns.get(rowIndex).setPosition(null);
				}
			} else if ( columnIndex == 1 ) {
				candidateColumns.get(rowIndex).setPosition((Integer) aValue);
			}  else if ( columnIndex == 2 ) {
			} else {
				throw new IllegalArgumentException("unknown columnIndex: "+ columnIndex);
			}
			fireTableDataChanged();
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

		public List<CustomTableColumn> getCandidateColumns() {
			return candidateColumns;
		}
	}

	public JComponent getPanel() {
		return panel;
	}

	public boolean hasUnsavedChanges() {
		return isTableModified() || validationHandler.hasPerformedValidation();
	}

	public boolean doSave() {

		selectedColumns = new ArrayList<CustomTableColumn>();
		for ( CustomTableColumn column : indexColumnTableModel.getCandidateColumns() ) {
			if ( column.isKey() ) selectedColumns.add(column);
		}
		Collections.sort(selectedColumns);
		logger.debug("Sorted list of selected columns: "+selectedColumns);

		if (selectedColumns.size() == 0) {
			return false;
        }

        if (validationHandler.getFailResults().size() != 0) {
            return false;
        }

		SQLIndex index = new SQLIndex(indexName.getText(),true,null,IndexType.OTHER,null);
		try {
			for ( CustomTableColumn column : selectedColumns ) {
				index.addChild(index.new Column(column.getSQLColumn(),false,false));
			}
			logger.debug("Index columns after save: "+index.getChildren());
		} catch (ArchitectException e) {
			SPSUtils.showExceptionDialogNoReport(swingSession.getFrame(),
				            "Unexpected error when adding Column to the Index",
				            e);
		}

		project.setSourceTableIndex(index);
		return true;
	}

	public boolean discardChanges() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: MatchMakerIndexBuilder.discardChanges()");
		return false;
	}

}
