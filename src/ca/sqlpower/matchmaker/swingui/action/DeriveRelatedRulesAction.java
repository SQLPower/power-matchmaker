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


package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.TableMergeRules.ChildMergeActionType;
import ca.sqlpower.matchmaker.swingui.ColumnChooserTableModel;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.util.EditableJTable;
import ca.sqlpower.swingui.MonitorableWorker;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SwingWorkerRegistry;
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
public class DeriveRelatedRulesAction extends AbstractAction {

	private static final Logger logger = Logger.getLogger(DeriveRelatedRulesAction.class);
	
	private Project project;
	private MatchMakerSwingSession swingSession;
	private List<String> primaryKeys;

	private JDialog dialog;
	private JTable columnTable;
	private JButton save;
	private JButton exit;

	private SQLTable sourceTable;
	private ColumnChooserTableModel columnTableModel;
	
	private final ActionListener deriveAction; 
	
	/** Displays validation results */
	private StatusComponent statusComponent;

	/**
	 * Handles the validation rules for this form.
	 */
	private FormValidationHandler validationHandler;
	
	private JProgressBar progressBar;

	private class DeriveAction extends MonitorableWorker implements ActionListener {

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
        	
        	dialog.dispose();
		}

		@Override
		/** Called (once) by run() in superclass */
		public void doStuff() throws Exception {
            started = true;
			
			// Adds all the user defined primary keys to the list that will be checked
			primaryKeys = new ArrayList<String>();
			for (SQLColumn column : columnTableModel.getSelectedSQLColumns()) {
				primaryKeys.add(column.getName());
			}
			logger.debug("Sorted list of selected columns: "+primaryKeys);

			if (primaryKeys.size() == 0) {
				dialog.dispose();
				return;
			}

			long start = System.currentTimeMillis();
			logger.debug("Fetching database meta data...");

			String lastTableName = "";
			String lastSchemaName = "";
			String lastCatalogName = "";
			
			Connection con = project.createSourceTableConnection();
			DatabaseMetaData dbMeta;
			ResultSet rs;
			try {
				dbMeta = con.getMetaData();
			} catch (SQLException ex) {
				SPSUtils.showExceptionDialogNoReport(swingSession.getFrame(),
						"An exception occured while retrieving database metadata for deriving collison criteria", ex);
				return;
			}
			
			// Finds all the merge rules that the project already has\
			TableMergeRules sourceTableMergeRule = null;
			List<String> mergeRules = new ArrayList<String>();
			for (TableMergeRules tmr : project.getTableMergeRules()) {
				mergeRules.add(tmr.getTableName());
				if (tmr.isSourceMergeRule()) {
					sourceTableMergeRule = tmr;
				}
			}

			try {
				project.startCompoundEdit();
				logger.debug("Beginning comparison on columns...");
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
						SQLTable table = sourceTable.getParentDatabase().getTableByName(catalogName,
								schemaName, tableName);
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
						count = 0;
					}
				}
			} catch (Exception e1) {
				SPSUtils.showExceptionDialogNoReport(swingSession.getFrame(),
						"Failed to derive related table information.", e1);
			} finally {
				try {
					if (con != null) {
						con.close();
					}
				} catch (SQLException e) {
					logger.error("Error closing connection" + e);
				}
				project.endCompoundEdit();
			}
			
			logger.debug("Finished in " + ((System.currentTimeMillis()-start)/1000) + " seconds!");
			save.setEnabled(true);
		}

		public void actionPerformed(ActionEvent e) {
			save.setEnabled(false);
			started = false;
			finished = false;
			try {
            	progressBar.setVisible(true);
            	logger.debug("Progress Bar has been set to visible");
            	ProgressWatcher watcher = new ProgressWatcher(progressBar, this);
            	watcher.start();
                new Thread(this).start();
            } catch (Exception ex) {
                SPSUtils.showExceptionDialogNoReport(swingSession.getFrame(),
                        "Error in deriving related rules.", ex );
                this.finished = true;
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
	}

	private final AbstractAction cancelAction = new AbstractAction("Cancel") {
		public void actionPerformed(ActionEvent e) {
			dialog.dispose();
		}
	};
	
	/**
	 * Does nothing! This is only here so that the button can be disabled.
	 */
	private final AbstractAction okAction = new AbstractAction("OK") {
		public void actionPerformed(ActionEvent e) {
		}
	};

	public DeriveRelatedRulesAction(MatchMakerSwingSession swingSession, Project project) {
		super("Derive Related Rules");
		this.project = project;
		this.swingSession = swingSession;
		this.deriveAction = new DeriveAction(swingSession);
		dialog = new JDialog(swingSession.getFrame(), "Derive Related Rules");
	}

	public void actionPerformed(ActionEvent e) {
		FormLayout layout = new FormLayout(
				"4dlu,fill:pref:grow,4dlu",
				// column1    2    3
		"10dlu,pref:grow,4dlu,pref:grow,4dlu,fill:min(200dlu;pref):grow,4dlu,pref,4dlu,pref,10dlu");
		// 1        2     3   4           5                    6         7     8   9    10   11
		PanelBuilder pb;
		JPanel panel = logger.isDebugEnabled() ? new FormDebugPanel(layout)
			: new JPanel(layout);
		pb = new PanelBuilder(layout, panel);

		CellConstraints cc = new CellConstraints();

		save = new JButton(okAction);
		save.addActionListener(deriveAction);
		exit = new JButton(cancelAction);

		// List of actions to disable when validation status is fail.
		List<Action> actions = new ArrayList<Action>();
		actions.add(okAction);

		statusComponent = new StatusComponent();
		validationHandler = new FormValidationHandler(statusComponent, actions);
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
		} catch (ArchitectException ex) {
			SPSUtils.showExceptionDialogNoReport(swingSession.getFrame(),
					"Error in deriving related rules.", ex);
		}
		
		JScrollPane scrollPane = new JScrollPane(columnTable);
		pb.add(scrollPane, cc.xy(2, 6, "f,f"));
		
		PrimaryKeySelectionValidator columnValidator = new PrimaryKeySelectionValidator(columnTable);
		validationHandler.addValidateObject(columnTable, columnValidator);

		progressBar = new JProgressBar();
		pb.add(progressBar, cc.xy(2, 8, "f,f"));

		pb.add(ButtonBarFactory.buildOKCancelBar(save, exit), cc.xy(2,10));
		
		dialog.getContentPane().add(panel);
		dialog.getRootPane().setDefaultButton(save);
		SPSUtils.makeJDialogCancellable(dialog, cancelAction, false);
		dialog.setTitle("Related Table Deriver");
		dialog.pack();
		dialog.setLocationRelativeTo(swingSession.getFrame());
		dialog.setVisible(true);
		
		validationHandler.resetHasValidated();
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
}
