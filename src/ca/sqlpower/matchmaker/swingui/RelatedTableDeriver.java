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
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.ColumnMergeRules.MergeActionType;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.table.TableUtils;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Generate merge rules for the tables that might be related to the 
 * source table.  Allows the user to choose the primary key for the
 * source table, and tries to find it as a foreign key on other tables.
 * 
 * Currently it only find grand child tables if the grand child table
 * contains the source table's primary key.
 */
public class RelatedTableDeriver implements EditorPane {

	private static final Logger logger = Logger.getLogger(RelatedTableDeriver.class);
	
	private Match match;
	private MatchMakerSwingSession swingSession;
	private List<String> primaryKeys;

	private JDialog dialog;
	private JPanel panel;
	private JTable columnTable;

	private final SQLTable sourceTable;
	private final PrimaryKeyColumnTableModel columnTableModel;
	
	/** Displays validation results */
	private StatusComponent statusComponent;

	/**
	 * Handles the validation rules for this form.
	 */
	private FormValidationHandler validationHandler;

	private final AbstractAction okAction = new AbstractAction("OK") {
		public void actionPerformed(ActionEvent e) {
			
			// Adds all the user defined primary keys to the list that will be checked
			primaryKeys = new ArrayList<String>();
			for (CustomTableColumn column : columnTableModel.getSelectedColumns()) {
				primaryKeys.add(column.getSQLColumn().getName());
			}
			logger.debug("Sorted list of selected columns: "+primaryKeys);

			if (primaryKeys.size() == 0) {
				dialog.dispose();
				return;
			}

			long start = System.currentTimeMillis();
			logger.debug("Deriving....");

			String lastTableName = "";
			String lastSchemaName = "";
			String lastCatalogName = "";
			
			Connection con = swingSession.getConnection();
			DatabaseMetaData dbMeta;
			ResultSet rs;
			try {
				dbMeta = con.getMetaData();
			} catch (SQLException ex) {
				SPSUtils.showExceptionDialogNoReport(swingSession.getFrame(),
						"An exception occured while retrieving database metadata for deriving collison criteria", ex);
				return;
			}
			
			// Finds all the merge rules that the match already has
			List<String> mergeRules = new ArrayList<String>();
			for (TableMergeRules tmr : match.getTableMergeRules()) {
				mergeRules.add(tmr.getTableName());
			}

			try {
				rs = dbMeta.getColumns(null, null, null, null);
				int count = 0;

				while (rs.next()) {
					String tableName = rs.getString("TABLE_NAME");
					String columnName = rs.getString("COLUMN_NAME");
					String catalogName = rs.getString("TABLE_CAT");
					String schemaName = rs.getString("TABLE_SCHEM");

					// Skip the column if a merge rule already exists for the table
					if (mergeRules.contains(tableName)) {
						continue;
					} 
					
					if (catalogName == null) {
						catalogName = "";
					}

					// Set the variables accordingly if the table has changed
					if (!(tableName.equals(lastTableName) && catalogName.equals(lastCatalogName) &&
						schemaName.equals(lastSchemaName))) {				
						count = 0;
						lastCatalogName = catalogName;
						lastSchemaName = schemaName;
						lastTableName = tableName;
					}
					
					if (primaryKeys.contains(columnName)) {
						count++;
					}
					
					// Adds a new merge rule for the table since it has all 
					// of the source table's primary keys
					if (count == primaryKeys.size()) {
						TableMergeRules mergeRule = new TableMergeRules();
						SQLTable table = swingSession.getDatabase().getTableByName(catalogName,
								schemaName, tableName);
						mergeRule.setTable(table);
						mergeRule.setTableIndex(match.getSourceTableIndex());

						try {
							List<SQLColumn> columns = new ArrayList<SQLColumn>(
									(match.getSourceTable()).getColumns()); 
							for (SQLColumn column : columns) {
								ColumnMergeRules newRules = new ColumnMergeRules();
								newRules.setActionType(MergeActionType.IGNORE);
								mergeRule.addChild(newRules);
								newRules.setColumn(column);
							}
						} catch (Exception ex) {
							SPSUtils.showExceptionDialogNoReport(swingSession.getFrame(),
									"An exception occured while deriving collison criteria", ex);
						}

						match.getTableMergeRulesFolder().addChild(mergeRule);
						count = 0;
					}
				}
				con.close();
			} catch (Exception e1) {
				SPSUtils.showExceptionDialogNoReport(swingSession.getFrame(),
						"Failed to derive related table information.", e1);
			}
			
			logger.debug("Finished in " + ((System.currentTimeMillis()-start)/1000) + " seconds!");
		
			dialog.dispose();
		}
	};

	private final AbstractAction cancelAction = new AbstractAction("Cancel") {
		public void actionPerformed(ActionEvent e) {
			dialog.dispose();
		}
	};

	public RelatedTableDeriver(Match match,
		MatchMakerSwingSession swingSession) throws ArchitectException {
		this.match = match;
		this.swingSession = swingSession;

		sourceTable = match.getSourceTable();
		SQLIndex oldIndex = match.getSourceTableIndex();

		// List of actions to disable when validation status is fail.
		List<Action> actions = new ArrayList<Action>();
		actions.add(okAction);
		
		statusComponent = new StatusComponent();
		validationHandler = new FormValidationHandler(statusComponent, actions);

		columnTableModel = new PrimaryKeyColumnTableModel(sourceTable,oldIndex);
		columnTable = new JTable(columnTableModel);
		columnTable.addColumnSelectionInterval(1, 1);
		TableUtils.fitColumnWidths(columnTable, 10);
		
		PrimaryKeySelectionValidator columnValidator = new PrimaryKeySelectionValidator(columnTable);
		validationHandler.addValidateObject(columnTable, columnValidator);

		dialog = new JDialog(swingSession.getFrame());
		dialog.setTitle("Related Table Deriver");
		buildUI();
		dialog.pack();
		dialog.setLocationRelativeTo(swingSession.getFrame());
		dialog.setVisible(true);
		
        validationHandler.resetHasValidated();
	}

	private void buildUI() {

		FormLayout layout = new FormLayout(
				"4dlu,fill:pref:grow,4dlu",
				// column1    2    3
		"10dlu,pref:grow,4dlu,pref:grow,4dlu,fill:min(200dlu;pref):grow,4dlu,pref,4dlu");
		//       1     2         3    4         5    6         7     8                          9    10   11
		PanelBuilder pb;
		JPanel panel = logger.isDebugEnabled() ? new FormDebugPanel(layout)
		: new JPanel(layout);
		pb = new PanelBuilder(layout, panel);

		CellConstraints cc = new CellConstraints();

		JButton save = new JButton(okAction);
		JButton exit = new JButton(cancelAction);

		pb.add(statusComponent, cc.xy(2, 2));
		pb.add(new JLabel("Table: " + DDLUtils.toQualifiedName(match.getSourceTable())),
				cc.xy(2, 4));
		JScrollPane scrollPane = new JScrollPane(columnTable);
		pb.add(scrollPane, cc.xy(2, 6, "f,f"));

		pb.add(ButtonBarFactory.buildOKCancelBar(save, exit), cc.xy(2,8));
		dialog.getContentPane().add(panel);
		dialog.getRootPane().setDefaultButton(save);
		SPSUtils.makeJDialogCancellable(dialog, cancelAction, false);
	}

	/**
	 * This class represents the table row model of the pick your own
	 * column for primary keys, which has only 2 columns.
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

	/**
	 * This class represents the table model of the pick your own
	 * column for primary keys table. It has 2 columns.
	 */
	private class PrimaryKeyColumnTableModel extends AbstractTableModel {

		private List<CustomTableColumn> candidateColumns = new ArrayList<CustomTableColumn>();
		public PrimaryKeyColumnTableModel(SQLTable sqlTable, SQLIndex oldIndex) throws ArchitectException {

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
				return "In Primary Key?";
			} else if (column == 1) {
				return "Column Name";
			} else {
				throw new IndexOutOfBoundsException("No such column in table: "+column);
			}
		}

		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return candidateColumns.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if ( columnIndex == 0 ) {
				return candidateColumns.get(rowIndex).isKey();
			}  else if ( columnIndex == 1 ) {
				return candidateColumns.get(rowIndex).getSQLColumn();
			} else {
				throw new IllegalArgumentException("unknown columnIndex: " + columnIndex);
			}
		}


		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if ( columnIndex == 0 ) {
				return Boolean.class;
			}  else if ( columnIndex == 1 ) {
				return SQLColumn.class;
			} else {
				throw new IllegalArgumentException("unknown columnIndex: "+ columnIndex);
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
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
			}  else if ( columnIndex == 1 ) {
			} else {
				throw new IllegalArgumentException("unknown columnIndex: "+ columnIndex);
			}
			fireTableDataChanged();
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if ( columnIndex == 0 ) {
				return true;
			}  else if ( columnIndex == 1 ) {
				return false;
			} else {
				throw new IllegalArgumentException("unknown columnIndex: "+ columnIndex);
			}
		}

		public List<CustomTableColumn> getCandidateColumns() {
			return candidateColumns;
		}
		
		public List<CustomTableColumn> getSelectedColumns() {
			List<CustomTableColumn> columns = new ArrayList<CustomTableColumn>();
			for (CustomTableColumn column : columnTableModel.getCandidateColumns()) {
				if (column.isKey()) {
					columns.add(column);
				}
			}
			return columns;
		}
	}

	public JComponent getPanel() {
		return panel;
	}

	public boolean hasUnsavedChanges() {
		return false;
	}

	public boolean doSave() {
		return true;
	}

	private class PrimaryKeySelectionValidator implements Validator {

	    private JTable table;
	    
	    public PrimaryKeySelectionValidator(JTable table){
	        this.table = table;
	    }
	    public ValidateResult validate(Object contents) {
	    	PrimaryKeyColumnTableModel model = (PrimaryKeyColumnTableModel)table.getModel();
	    	System.out.println(model.getSelectedColumns().size() + "");
	    	if (model.getSelectedColumns().size() == 0) {
	    		return ValidateResult.createValidateResult(Status.FAIL, 
	    			"Atleast one primary key column must be selected.");
	    	}

	        return ValidateResult.createValidateResult(Status.OK, "");
	    }

	}
}
