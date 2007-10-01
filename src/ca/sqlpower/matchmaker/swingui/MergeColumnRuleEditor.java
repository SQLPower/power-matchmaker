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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.ColumnMergeRules.MergeActionType;
import ca.sqlpower.matchmaker.util.EditableJTable;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class MergeColumnRuleEditor implements EditorPane {

	private static final Logger logger = Logger.getLogger(MergeColumnRuleEditor.class);
	private JPanel panel;
	private final MatchMakerSwingSession swingSession;
	private final Match match;
	private TableMergeRules mergeRule;
	StatusComponent status = new StatusComponent();
	private FormValidationHandler handler;
	private final SQLObjectChooser chooser;
	private final JCheckBox deleteDup = new JCheckBox();
	private List<ColumnMergeRules> toBeDeleteList = new ArrayList<ColumnMergeRules>();
	
	private MergeColumnRuleTableModel ruleTableModel;
	private ColumnMergeRulesTable ruleTable;
	//keeps track of whether the table has unsaved changes
	private CustomTableModelListener tableListener;
	
	public MergeColumnRuleEditor(MatchMakerSwingSession swingSession,
			Match match, TableMergeRules mergeRule) {
		this.swingSession = swingSession;
		this.match = match;
		this.mergeRule = mergeRule;
		if (match == null) {
			throw new NullPointerException("You can't edit a null match");
		}
		if (mergeRule == null) {
			throw new NullPointerException("You can't edit a null merge rule");
		}

		chooser = new SQLObjectChooser(swingSession);
        handler = new FormValidationHandler(status);
        handler.addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				resetAction();
			}});

        ruleTableModel = new MergeColumnRuleTableModel(mergeRule);
        tableListener = new CustomTableModelListener();
        ruleTableModel.addTableModelListener(tableListener);
        ruleTable = new ColumnMergeRulesTable(ruleTableModel);
        
        buildUI();
        setDefaultSelections();
        handler.resetHasValidated(); // avoid false hits when newly created
        
	}

	public TableMergeRules getMergeRule() {
		return mergeRule;
	}
	
	private void resetAction() {
		ValidateResult result = handler.getWorstValidationStatus();
		saveAction.setEnabled(true);
		if (result.getStatus() == Status.FAIL) {
			saveAction.setEnabled(false);
		}
	}

	private void buildUI() {
		
		FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,fill:min(pref;"+(new JComboBox().getMinimumSize().width)+"px):grow, 4dlu,pref,4dlu", // columns
				"10dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,fill:40dlu:grow,4dlu,pref,4dlu"); // rows
			//	 1     2    3    4               5    6    7     8         9    10   11      
			//    status    cat       schema    table     index     del dup   table      button bar

		PanelBuilder pb;
		JPanel p = logger.isDebugEnabled() ? 
				new FormDebugPanel(layout) : new JPanel(layout);
		pb = new PanelBuilder(layout, p);
		CellConstraints cc = new CellConstraints();

		int row = 2;
		pb.add(status, cc.xy(4,row));
		row += 2;
		pb.add(chooser.getCatalogTerm(), cc.xy(2,row,"r,t"));
		pb.add(chooser.getCatalogComboBox(), cc.xy(4,row,"f,t"));
		row += 2;
		pb.add(chooser.getSchemaTerm(), cc.xy(2,row,"r,t"));
		pb.add(chooser.getSchemaComboBox(), cc.xy(4,row,"f,t"));
		
		row += 2;
		pb.add(new JLabel("Table Name:"), cc.xy(2,row,"r,t"));
		pb.add(chooser.getTableComboBox(), cc.xy(4,row,"f,t"));

		row += 2;
		pb.add(new JLabel("Index Name:"), cc.xy(2,row,"r,t"));
		pb.add(chooser.getUniqueKeyComboBox(), cc.xy(4,row,"f,t"));
		
		row += 2;
		pb.add(new JLabel("Delete Dup:"), cc.xy(2,row,"r,t"));
		pb.add(deleteDup, cc.xy(4,row,"l,t"));
		
		
		row += 2;
		pb.add(new JScrollPane(ruleTable), cc.xy(4,row,"f,f"));

		ButtonBarBuilder bbb = new ButtonBarBuilder();
		bbb.addGridded(new JButton(deriveAction));
		bbb.addRelatedGap();
		bbb.addGridded(new JButton(clearAction));
		bbb.addRelatedGap();
		bbb.addGridded(new JButton(newRuleAction));
		bbb.addRelatedGap();
		bbb.addGridded(new JButton(deleteRuleAction));
		bbb.addRelatedGap();
		bbb.addGridded(new JButton(saveAction));
		
		row+=2;
		pb.add(bbb.getPanel(), cc.xy(4,row,"c,c"));
		panel = pb.getPanel();
	}
	
	private boolean doesColumnExistsInChildren( SQLColumn column) {
		for ( ColumnMergeRules columnMergeRules : mergeRule.getChildren() ) {
			if ( columnMergeRules.getColumnName().equals(column.getName()) )
				return true;
		}
		return false;
	}
	
	public ColumnMergeRules newColumnRule() {
		ColumnMergeRules newRules = new ColumnMergeRules();
		newRules.setActionType(MergeActionType.IGNORE);
		mergeRule.addChild(newRules);
		return newRules;
	}
	
	private Action newRuleAction = new AbstractAction("New") {
		public void actionPerformed(ActionEvent e) {
			newColumnRule();
		}
	};
		
	private Action deleteRuleAction = new AbstractAction("Delete") {
		public void actionPerformed(ActionEvent e) {
			final int selectedRow = ruleTable.getSelectedRow();
			if (selectedRow == -1) {
				JOptionPane.showMessageDialog(swingSession.getFrame(),
						"You have to select a column first!");
				return;
			} else {
				mergeRule.removeChild(mergeRule.getChildren().get(selectedRow));
			}
			if (selectedRow >= 0 && selectedRow < ruleTable.getRowCount()) {
				ruleTable.setRowSelectionInterval(selectedRow, selectedRow);
			}
		}
	};

	private Action saveAction = new AbstractAction("Save") {
		public void actionPerformed(ActionEvent e) {
			
			if ( !handler.hasPerformedValidation() ) {
				ruleTableModel.fireTableChanged(new TableModelEvent(ruleTableModel));
			}
			ValidateResult result = handler.getWorstValidationStatus();
			if ( result.getStatus() == Status.FAIL) {
				JOptionPane.showMessageDialog(swingSession.getFrame(),
						"You have to fix the error before you can save the merge rules",
						"Save",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			if ( doSave() ) {
				JOptionPane.showMessageDialog(swingSession.getFrame(),
						"Merge Column rules saved.",
						"Save",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(swingSession.getFrame(),
						"Merge Column rules not saved.",
						"Save",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	};
		

	private Action deriveAction = new AbstractAction("Derive Collision Criteria") {
		public void actionPerformed(ActionEvent e) {
			try {
				List<SQLColumn> columns = new ArrayList<SQLColumn>(
						((SQLTable) chooser.getTableComboBox().getSelectedItem()).getColumns()); 
				for (SQLColumn column : columns) {
					if (!doesColumnExistsInChildren(column) ) {
						ColumnMergeRules newRules = newColumnRule();
						newRules.setColumn(column);
					}
				}
			} catch (Exception ex) {
				SPSUtils.showExceptionDialogNoReport(panel, "An exception occured while deriving collison criteria", ex);
			}
		}
	};
	
	private Action clearAction = new AbstractAction("Clear Collision Criteria") {
		public void actionPerformed(ActionEvent e) {
			while( mergeRule.getChildren().size() > 0 ) {
				mergeRule.removeChild(mergeRule.getChildren().get(0));
			}
		}
	};

	/** for reversing the change of table combobox	 */
	private SQLTable oldSQLTable;
	private boolean reSetting = false;
	
	private void setDefaultSelections() {
		if (mergeRule.getSourceTable() != null) {
			chooser.getCatalogComboBox().setSelectedItem(mergeRule.getSourceTable().getCatalog());
			chooser.getSchemaComboBox().setSelectedItem(mergeRule.getSourceTable().getSchema());
			chooser.getTableComboBox().setSelectedItem(mergeRule.getSourceTable());
			try {
				chooser.getUniqueKeyComboBox().setSelectedItem(mergeRule.getTableIndex());
			} catch (ArchitectException e1) {
				SPSUtils.showExceptionDialogNoReport(swingSession.getFrame(), 
						"An exception occured while creating the merge column rules editor", e1);
			}
		}
		deleteDup.setSelected(mergeRule.isDeleteDup());
		
		chooser.getTableComboBox().addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				System.out.println("StateChange="+e.getStateChange());
				
				if (!reSetting) {
					if ( e.getStateChange() == ItemEvent.DESELECTED ) {
						oldSQLTable = (SQLTable) e.getItem();
					} else if ( e.getStateChange() == ItemEvent.SELECTED ) {
						if (ruleTable.getRowCount() > 0) {
							int respond = JOptionPane.showConfirmDialog(
									swingSession.getFrame(),
									"Change table will also clear the column merge rules, Do you want to continue?",
									"Clear the column merge rules?", 
									JOptionPane.YES_NO_OPTION,
									JOptionPane.INFORMATION_MESSAGE);
							if (respond == JOptionPane.YES_OPTION) {
								clearAction.actionPerformed(null);
								reSetting = false;
							} else {
								reSetting = true;
								chooser.getTableComboBox().setSelectedItem(oldSQLTable);
							}
						}
					}
				} else {
					if ( e.getStateChange() == ItemEvent.SELECTED ) {
						reSetting = false;
					}
				}
					
			}
		});
		
		MergeColumnRuleComboBoxValidator v1 = new MergeColumnRuleComboBoxValidator(chooser);
		handler.addValidateObject(chooser.getCatalogComboBox(), v1);
		handler.addValidateObject(chooser.getSchemaComboBox(), v1);
		handler.addValidateObject(chooser.getTableComboBox(), v1);
		handler.addValidateObject(chooser.getUniqueKeyComboBox(), v1);
		
		MergeColumnRuleJTableValidator v2 = new MergeColumnRuleJTableValidator();
		handler.addValidateObject(ruleTable, v2);
		
		
	}

	public boolean doSave() {
		
		mergeRule.setTable((SQLTable) chooser.getTableComboBox().getSelectedItem());
		mergeRule.setTableIndex((SQLIndex) chooser.getUniqueKeyComboBox().getSelectedItem());
		mergeRule.setDeleteDup(deleteDup.isSelected());
		for (ColumnMergeRules columnMergeRules : toBeDeleteList) {
			swingSession.delete(columnMergeRules);
		}
        
		if (!match.getTableMergeRules().contains(mergeRule)) {
			match.getTableMergeRulesFolder().addChild(mergeRule);
	        MatchMakerTreeModel treeModel = (MatchMakerTreeModel) swingSession.getTree().getModel();
	        TreePath menuPath = treeModel.getPathForNode(mergeRule);
	        swingSession.getTree().setSelectionPath(menuPath);
        }
        
		swingSession.save(match);
		tableListener.setModified(false);
		return true;
	}

	public JComponent getPanel() {
		return panel;
	}

	public boolean hasUnsavedChanges() {
		
		if (tableListener.isModified()) return true;
		if (chooser.getTableComboBox().getSelectedItem() != mergeRule.getSourceTable()) {
			return true;
		}
		if (this.deleteDup.isSelected() != mergeRule.isDeleteDup()) {
			return true;
		}
		return false;
	}


	
	private class ColumnMergeRulesTable extends EditableJTable {

		public ColumnMergeRulesTable(MergeColumnRuleTableModel columnMergeRuleTableModel) {
			super(columnMergeRuleTableModel);
		}

		@Override
		public TableCellEditor getCellEditor(int row, int column) {
			if (column == 0) {
				return new DefaultCellEditor(chooser.getColumnComboBox());
			} else if (column == 1) {
				return new DefaultCellEditor(
						new JComboBox(
								new DefaultComboBoxModel(
										ColumnMergeRules.MergeActionType.values())));
			} else {
				return super.getCellEditor(row, column);
			}
		}
	}
	
	private class MergeColumnRuleComboBoxValidator implements Validator {

		private SQLObjectChooser chooser;
		public MergeColumnRuleComboBoxValidator(SQLObjectChooser chooser) {
			this.chooser = chooser;
		}
		
		public ValidateResult validate(Object contents) {
			if (chooser.getTableComboBox().getSelectedItem() == null) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Merge table is required");
				
			// Checks if an existing merge rule is already set to operate on 
			// the chosen schema and table.
			} else if (chooser.getTableComboBox().isEnabled()){
				List<TableMergeRules> mergeRules = match.getTableMergeRules();
				String schemaName = ((SQLSchema)chooser.getSchemaComboBox().getSelectedItem()).getName();
				String tableName = ((SQLTable)chooser.getTableComboBox().getSelectedItem()).getName();
				
				for (TableMergeRules rule: mergeRules) {
					if (rule != mergeRule &&
							rule.getSchemaName().equals(schemaName) &&
							rule.getTableName().equals(tableName)) {
						return ValidateResult.createValidateResult(Status.FAIL,
								"Only one merge rule can operate on an individual table");
					}
				}
			}
			if (chooser.getUniqueKeyComboBox().getSelectedItem() == null) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Merge table index is required");
			}
			return ValidateResult.createValidateResult(Status.OK, "");
		}
		
	}
	
	private class MergeColumnRuleJTableValidator implements Validator {

		public ValidateResult validate(Object contents) {
			TableModel model = (TableModel) contents;
			for ( int i=0; i<model.getRowCount(); i++) {
				SQLColumn column = (SQLColumn) model.getValueAt(i, 0);
				if (column == null) {
					logger.debug("column #" + i + " is null");
					return ValidateResult.createValidateResult(Status.FAIL,
						"column name is required");
				} 
				for ( int j=i+1; j<model.getRowCount(); j++) {
					SQLColumn column2 = (SQLColumn) model.getValueAt(j, 0);
					if (column2 == null) {
						logger.debug("column #" + j + " is null");
						return ValidateResult.createValidateResult(Status.FAIL,
							"column name is required");
					}
					if (column.getName().equals(column2.getName())) {
						logger.debug("column #" + j + "[" + 
								column.getName() + "] is duplicate");
						return ValidateResult.createValidateResult(Status.FAIL,
							"column name duplicated");
					}
				}
			}
			return ValidateResult.createValidateResult(Status.OK, "");
		}
		
	}

	public void setSelectedColumn(ColumnMergeRules selectedColumn) {
		if (selectedColumn != null) {
			int selected = mergeRule.getChildren().indexOf(selectedColumn);			
			if (selected >= 0 && selected<ruleTable.getRowCount()) {
				ruleTable.setRowSelectionInterval(selected, selected);
			}
		}
	} 

}
