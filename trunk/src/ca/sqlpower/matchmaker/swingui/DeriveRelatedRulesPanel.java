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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.TableMergeRules.ChildMergeActionType;
import ca.sqlpower.matchmaker.swingui.action.DeriveRelatedRulesAction;
import ca.sqlpower.matchmaker.util.EditableJTable;
import ca.sqlpower.sql.jdbcwrapper.DatabaseMetaDataDecorator;
import ca.sqlpower.sql.jdbcwrapper.DatabaseMetaDataDecorator.CacheType;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.swingui.MonitorableDataEntryPanel;
import ca.sqlpower.swingui.MonitorableWorker;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SwingWorkerRegistry;
import ca.sqlpower.swingui.table.TableUtils;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validated;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;
import ca.sqlpower.validation.swingui.ValidationHandler;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
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
public class DeriveRelatedRulesPanel implements MonitorableDataEntryPanel, Validated {

	private static final Logger logger = Logger.getLogger(DeriveRelatedRulesAction.class);
	
	private final Project project;
	private final MatchMakerSwingSession swingSession;

	private JDialog dialog = null;
	
	private final JPanel panel;
	
	private JTable columnTable;
	private final JProgressBar progressBar = new JProgressBar();
	private JCheckBox deriveByColumnNames;
	private JCheckBox deriveByForeignKeyConstraints;
	
	private SQLTable sourceTable;
	private ColumnChooserTableModel columnTableModel;
	
	private final DeriveAction deriveAction; 
	
    /** Displays validation results */
    private final StatusComponent statusComponent;

    /** Handles the validation rules for this form. */
    private final FormValidationHandler validationHandler;
	

	private class DeriveAction extends MonitorableWorker {

		/**
         * Indicates that the derive process has begun.
         */
        private boolean started;
        
        /**
         * Indicated that the derive process has terminated (with either
         * success or failure).
         */
        private boolean finished;
		
		public DeriveAction(SwingWorkerRegistry registry) {
			super(registry);
		}

		@Override
		public void cleanup() throws Exception {
        	logger.debug("DeriveRelatedRulesAction.cleanup() starting");
        	finished = true;
        	if (dialog != null) dialog.dispose();
        	swingSession.setSelectNewChild(true);
		}

		@Override
		/** Called (once) by run() in superclass */
		public void doStuff() throws Exception {
            started = true;
			
            long start = System.currentTimeMillis();
            
            // Finds all the merge rules that the project already has\
            TableMergeRules sourceTableMergeRule = null;
            List<String> mergeRules = new ArrayList<String>();
            for (TableMergeRules tmr : project.getTableMergeRules()) {
            	mergeRules.add(tmr.getTableName());
            	if (tmr.isSourceMergeRule()) {
            		sourceTableMergeRule = tmr;
            	}
            }

            List<String> primaryKeys = null;
            Connection con = null;
            DatabaseMetaData dbMeta = null;
            
            if (deriveByColumnNames.isSelected()) {
				// Adds all the user defined primary keys to the list that will be checked
				primaryKeys = new ArrayList<String>();
				for (SQLColumn column : columnTableModel
						.getSelectedSQLColumns()) {
					primaryKeys.add(column.getName());
				}
				logger.debug("Sorted list of selected columns: " + primaryKeys);
				if (primaryKeys.size() == 0) {
					if (dialog != null)
						dialog.dispose();
					return;
				}
				logger.debug("Fetching database meta data...");

				con = project.createSourceTableConnection();

				try {
					dbMeta = con.getMetaData();
				} catch (SQLException ex) {
					SPSUtils.showExceptionDialogNoReport(
									swingSession.getFrame(),
									"An exception occured while retrieving database metadata for deriving collison criteria",
									ex);
					return;
				}
			}
            
            
			try {
				DatabaseMetaDataDecorator.putHint(DatabaseMetaDataDecorator.CACHE_TYPE, CacheType.EAGER_CACHE);
				project.startCompoundEdit();
            	if (deriveByForeignKeyConstraints.isSelected()) {
            		deriveMergeRulesByFKConstraints(sourceTable, sourceTableMergeRule, mergeRules);
            	}
            	if (deriveByColumnNames.isSelected()) {
            		deriveMergeRulesByColumnNames(con, dbMeta, primaryKeys, sourceTableMergeRule, mergeRules);
            	}
			} catch (Exception e) {
				if (e instanceof CancellationException) {
					logger.debug("User has cancelled merge rule derivation");
				} else {
					SPSUtils.showExceptionDialogNoReport(swingSession.getFrame(),
						"Failed to derive related table information.", e);
				}
			} finally {
				project.endCompoundEdit();
				DatabaseMetaDataDecorator.putHint(DatabaseMetaDataDecorator.CACHE_TYPE, CacheType.NO_CACHE);
			}
			
			logger.debug("Finished in " + ((System.currentTimeMillis()-start)/1000) + " seconds!");
		}

		/**
		 * The new TableMergeRule deriver. It uses foreign key constraints to
		 * find child tables and create merge rules for them. It then
		 * recursively calls itself on the child tables. If a merge rule for at
		 * table already exists, it checks if the parent table is the same. If
		 * so, then it takes the same table merge rule and trys to add missing
		 * imported keys (in the case that the table imports multiple keys. If
		 * the parent table is different, then it creates a separate merge rule
		 * for the same table but with a different parent. This will help the
		 * Merge Engine deal with data models which contain tables with more
		 * than one parent table.
		 * 
		 * @param table
		 *            The SQLTable whose child tables we are deriving
		 *            TableMergeRules for.
		 * @param sourceTableMergeRule
		 *            The TableMergeRule of the parent table
		 * @param mergeRules
		 *            A List of Strings containing the names of existing
		 *            TableMergeRules for this project.
		 * @throws SQLObjectException
		 */
		private void deriveMergeRulesByFKConstraints(SQLTable table, TableMergeRules sourceTableMergeRule, List<String> mergeRules) throws SQLObjectException {
			if (isCancelled()) {
				throw new CancellationException("Merge rule derivation cancelled by user");
			}
			
			List<SQLRelationship> exportedKeys = table.getExportedKeys();

			for (SQLRelationship exportedKey : exportedKeys) {
				SQLTable fkTable = exportedKey.getFkTable();
				
				// If a merge rule already exists for the table, then check if
				// the imported key columns need supplementing. (In case the table
				// is importing other keys)
				if (mergeRules.contains(fkTable.getName())) {
					boolean alreadyExists = false;
					for (TableMergeRules mergeRule: project.getTableMergeRules()) {
						if (mergeRule.getTableName().equals(fkTable.getName()) && 
								mergeRule.getParentMergeRule().equals(sourceTableMergeRule)) {
							for (ColumnMergeRules cmr : mergeRule.getChildren()) {
								if (exportedKey.containsFkColumn(cmr.getColumn())) {
									SQLColumn pkColumn = exportedKey.getMappingByFkCol(cmr.getColumn()).getPkColumn();
									cmr.setImportedKeyColumnAndAction(pkColumn);
								}
							}
							alreadyExists = true;
						}
					}
					if (alreadyExists) continue;
				}
					
				TableMergeRules mergeRule = new TableMergeRules();
				// TODO: This is just a temporary fix for handling the problem when we are trying
				// to derive column merge rules from a non-table. This resulted in an NPE before
				// A better thing to do would be to only run this on tables.
				if (fkTable == null || !fkTable.getObjectType().equals("TABLE")) continue;
				mergeRule.setTable(fkTable);
				SQLIndex index = fkTable.getPrimaryKeyIndex();
				// if PK is null, then try to get the first available Unique Key
				if (index == null) {
					for (SQLIndex ind: fkTable.getIndices()) {
						if (ind.isUnique()) index = ind;
					}
				}
				mergeRule.setTableIndex(index);
				mergeRule.setParentMergeRule(sourceTableMergeRule);
				mergeRule.setChildMergeAction(ChildMergeActionType.UPDATE_FAIL_ON_CONFLICT);
				mergeRule.deriveColumnMergeRules();
				for (ColumnMergeRules cmr : mergeRule.getChildren()) {
					if (index != null) {
						if (mergeRule.getPrimaryKeyFromIndex().contains(cmr.getColumn())) {
							cmr.setInPrimaryKeyAndAction(true);
						}
					}
					if (exportedKey.containsFkColumn(cmr.getColumn())) {
						SQLColumn pkColumn = exportedKey.getMappingByFkCol(cmr.getColumn()).getPkColumn();
						cmr.setImportedKeyColumnAndAction(pkColumn);
					}
				}
				project.addTableMergeRule(mergeRule);
				mergeRules.add(fkTable.getName());

				// recursively derive merge rules for child tables
				deriveMergeRulesByFKConstraints(fkTable, mergeRule, mergeRules);
			}
		}

		/**
		 * Derives TableMergeRules based on sharing the same column names as the
		 * source table's primary key columns. This is how the MatchMaker used
		 * to derive merge rules. It has a few flaws, including not being able
		 * to derive merge rules on tables that change the name of the imported
		 * primary key columns and also adds false positives on tables that may
		 * have the same column name as the primary key column, but has no real
		 * relationship with it (ex. if you gave all your tables surrogate keys
		 * all called 'id') However, it is still useful in trying to derive
		 * relationships between tables that are not enforced with a foreign key
		 * constraint.
		 * 
		 * @param con
		 *            The Connection object that will be used to query the
		 *            database
		 * @param dbMeta
		 *            The DatabaseMetaData used to get the list of tables that
		 *            share the same column name
		 * @param primaryKeys
		 *            A List of Strings containing the names of all the source
		 *            table's primary key columns
		 * @param sourceTableMergeRule
		 *            The TableMergeRule for the source table
		 * @param mergeRules
		 *            A List of Strings containing all the names of the
		 *            TableMergeRules in thie project
		 * @throws SQLException
		 * @throws SQLObjectException
		 */
		private void deriveMergeRulesByColumnNames(Connection con,
				DatabaseMetaData dbMeta, List<String> primaryKeys,
				TableMergeRules sourceTableMergeRule, List<String> mergeRules)
				throws SQLException, SQLObjectException {

			ResultSet rs = null;
			String lastTableName = "";
			String lastSchemaName = "";
			String lastCatalogName = "";
			
			try {
				logger.debug("Beginning comparison on columns...");
				rs = dbMeta.getColumns(null, null, null, null);
				int count = 0;
	
				while (rs.next()) {
					if (isCancelled()) {
						throw new CancellationException("Merge rule derivation cancelled by user");
					}
					
					String tableName = rs.getString("TABLE_NAME");
					String columnName = rs.getString("COLUMN_NAME");
					String catalogName = rs.getString("TABLE_CAT");
					String schemaName = rs.getString("TABLE_SCHEM");
	
					// If a merge rule already exists for the table, then check if
					// the imported key columns need supplementing. (In case the table
					// is importing other keys)
					if (mergeRules.contains(tableName)) {
						boolean alreadyExists = false;
						for (TableMergeRules mergeRule: project.getTableMergeRules()) {
							if (mergeRule.getTableName().equals(tableName) && 
									!mergeRule.isSourceMergeRule() &&
									mergeRule.getParentMergeRule().equals(sourceTableMergeRule)) {
								for (ColumnMergeRules cmr : mergeRule.getChildren()) {
									if (primaryKeys.contains(cmr.getColumnName())) {
										cmr.setImportedKeyColumnAndAction(sourceTable.getColumnByName(cmr.getColumnName()));
									}
								}
								alreadyExists = true;
							} else if (mergeRule.isSourceMergeRule()) {
								alreadyExists = true;
							}
						}
						if (alreadyExists) continue;
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
						SQLTable table = sourceTable.getParentDatabase().getTableByName(catalogName,
								schemaName, tableName);
						// TODO: This is just a temporary fix for handling the problem when we are trying
						// to derive column merge rules from a non-table. This resulted in an NPE before
						// A better thing to do would be to only run this on tables.
						if (table == null || !table.getObjectType().equals("TABLE")) continue;
						SQLIndex index = table.getPrimaryKeyIndex();
						mergeRule.setTable(table);
						mergeRule.setTableIndex(index);
						mergeRule.setParentMergeRule(sourceTableMergeRule);
						mergeRule.setChildMergeAction(ChildMergeActionType.UPDATE_FAIL_ON_CONFLICT);
						mergeRule.deriveColumnMergeRules();
						for (ColumnMergeRules cmr : mergeRule.getChildren()) {
							if (index != null) {
								if (mergeRule.getPrimaryKeyFromIndex().contains(cmr.getColumn())) {
									cmr.setInPrimaryKeyAndAction(true);
								}
							}
							if (primaryKeys.contains(cmr.getColumnName())) {
								cmr.setImportedKeyColumnAndAction(sourceTable.getColumnByName(cmr.getColumnName()));
							}
						}
						project.addTableMergeRule(mergeRule);
						mergeRules.add(tableName);
						count = 0;
					}
				}
			} finally {
				try {
					if (con != null) {
						con.close();
					}
				} catch (SQLException e) {
					logger.error("Failed to close connection! Squishing this exception: ", e);
				}
				try {
					if (rs != null) {
						rs.close();
					}
				} catch (SQLException e) {
					logger.error("Failed to close result set! Squishing this exception: ", e);
				}
			}
		}

		public Integer getJobSize() {
			return null;
		}

		public String getMessage() {
			return "Deriving related rules...";
		}

		public int getProgress() {
			return 0;
		}

		public boolean hasStarted() {
			return started;
		}

		public boolean isFinished() {
			return finished;
		}

		public void setStarted(boolean started) {
			this.started = started;
		}

		public void setFinished(boolean finished) {
			this.finished = finished;
		}
	}

	public DeriveRelatedRulesPanel(MatchMakerSwingSession swingSession, Project project) {
		this.project = project;
		this.swingSession = swingSession;
		this.deriveAction = new DeriveAction(swingSession);
		statusComponent = new StatusComponent();
		validationHandler = new FormValidationHandler(statusComponent);

		this.panel = buildUI();
		
		addValidators();
		validationHandler.resetHasValidated();
	}

	private JPanel buildUI() {
		FormLayout layout = new FormLayout(
				"4dlu,fill:pref:grow,4dlu",
		// columns: 1         2       3
				"10dlu,pref:grow,4dlu,pref:grow,4dlu,fill:min(200dlu;pref):grow,4dlu,pref,pref,pref,10dlu");
		// rows:   1        2     3   4           5                    6         7     8   9    10   11
		PanelBuilder pb;
		JPanel panel = logger.isDebugEnabled() ? new FormDebugPanel(layout)
			: new JPanel(layout);
		pb = new PanelBuilder(layout, panel);

		CellConstraints cc = new CellConstraints();

		pb.add(statusComponent, cc.xy(2, 2));
		pb.add(new JLabel("Table: " + DDLUtils.toQualifiedName(project.getSourceTable())),
				cc.xy(2, 4));
		
		try {
			sourceTable = project.getSourceTable();
			SQLIndex oldIndex = project.getSourceTableIndex();
			columnTableModel = new ColumnChooserTableModel(sourceTable, oldIndex, false);
			columnTable = new EditableJTable(columnTableModel);
			columnTable.addColumnSelectionInterval(1, 1);
			TableUtils.fitColumnWidths(columnTable, 10);
		} catch (SQLObjectException ex) {
			SPSUtils.showExceptionDialogNoReport(swingSession.getFrame(),
					"Error in deriving related rules.", ex);
		}
		
		JScrollPane scrollPane = new JScrollPane(columnTable);
		pb.add(scrollPane, cc.xy(2, 6, "f,f"));
		
		deriveByColumnNames = new JCheckBox("Derive by column names", true);
		pb.add(deriveByColumnNames, cc.xy(2, 8));
		deriveByForeignKeyConstraints = new JCheckBox("Derive by foreign key constraints", true);
		pb.add(deriveByForeignKeyConstraints, cc.xy(2, 9));
		
		pb.add(progressBar, cc.xy(2, 10, "f,f"));

		return pb.getPanel();
	}

	private void addValidators() {
		PrimaryKeySelectionValidator columnValidator = new PrimaryKeySelectionValidator(columnTable);
		validationHandler.addValidateObject(columnTable, columnValidator);
	}

	/**
	 * This is a simple validator that checks if there is atleast one column selected. It
	 * only works on a {@link PrimaryKeyColumnTableModel}. 
	 * <p>
	 * Adaptation of this for other tables would require adding a getSelectedColumns()
	 * that returns a list of columns that have been selected to the table model.
	 */
	private class PrimaryKeySelectionValidator implements Validator {

	    private JTable table;
	    
	    public PrimaryKeySelectionValidator(JTable table){
	        this.table = table;
	    }
	    public ValidateResult validate(Object contents) {
	    	ColumnChooserTableModel model = (ColumnChooserTableModel)table.getModel();
	    	if (model.getSelectedSQLColumns().size() == 0) {
	    		return ValidateResult.createValidateResult(Status.FAIL, 
	    			"Atleast one primary key column must be selected.");
	    	}

	        return ValidateResult.createValidateResult(Status.OK, "");
	    }

	}
	
	public boolean applyChanges() {
		swingSession.setSelectNewChild(false);
		deriveAction.setStarted(false);
		deriveAction.setFinished(false);
		try {
        	progressBar.setVisible(true);
        	logger.debug("Progress Bar has been set to visible");
        	ProgressWatcher watcher = new ProgressWatcher(progressBar, deriveAction);
        	watcher.start();
            new Thread(deriveAction).start();
        } catch (Exception ex) {
            SPSUtils.showExceptionDialogNoReport(swingSession.getFrame(),
                    "Error in deriving related rules.", ex );
            deriveAction.setFinished(true);
            return false;
        }
        return true;
	}

	public void discardChanges() {
		deriveAction.setCancelled(true);
	}

	public JComponent getPanel() {
		return panel;
	}

	public boolean hasUnsavedChanges() {
		return true;
	}

	public ValidationHandler getHandler() {
		return validationHandler;
	}

	public void setDialog(JDialog dialog) {
		this.dialog = dialog;
	}
}

