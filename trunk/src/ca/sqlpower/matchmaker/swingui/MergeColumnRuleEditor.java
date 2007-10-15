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
import java.sql.Types;
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
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
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

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class MergeColumnRuleEditor implements EditorPane {

	private static final Logger logger = Logger.getLogger(MergeColumnRuleEditor.class);
	private JPanel panel;
	private final MatchMakerSwingSession swingSession;
	private final Match match;
	private final TableMergeRules mergeRule;
	
	StatusComponent status = new StatusComponent();
	private FormValidationHandler handler;
	private final JCheckBox deleteDup = new JCheckBox();
	private final JComboBox parentTable = new JComboBox();
	private final JComboBox childMergeAction;

	private MergeColumnRuleTableModel ruleTableModel;
	private ColumnMergeRulesTable ruleTable;
	//keeps track of whether the table has unsaved changes
	private CustomTableModelListener tableListener;
	
	public MergeColumnRuleEditor(final MatchMakerSwingSession session,
			final Match match, final TableMergeRules mr) {
		this.swingSession = session;
		this.match = match;
		this.mergeRule = mr;
		if (match == null) {
			throw new NullPointerException("You can't edit a null match");
		}
		if (mergeRule == null) {
			throw new NullPointerException("You can't edit a null merge rule");
		}

        ruleTableModel = new MergeColumnRuleTableModel(mergeRule);
        tableListener = new CustomTableModelListener();
        ruleTableModel.addTableModelListener(tableListener);
        ruleTable = new ColumnMergeRulesTable(ruleTableModel);
        ruleTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {
                ColumnMergeRules mergeColumn = mergeRule.getChildren().get(ruleTable.getSelectedRow()); 
                MatchMakerTreeModel treeModel = (MatchMakerTreeModel) swingSession.getTree().getModel();
    	        TreePath menuPath = treeModel.getPathForNode(mergeColumn);
    	        swingSession.getTree().setSelectionPath(menuPath);
			}
		});

        for (TableMergeRules tmr : match.getTableMergeRules()) {
        	if (!tmr.equals(mergeRule)) {
        		parentTable.addItem(tmr.getName());
        	}
        }
        childMergeAction = new JComboBox(TableMergeRules.ChildMergeActionType.values());
        
        buildUI();
        List<Action> actions = new ArrayList<Action>();
        actions.add(saveAction);
        handler = new FormValidationHandler(status,actions);
        handler.addValidateObject(ruleTable, new MergeColumnRuleJTableValidator());
        handler.resetHasValidated(); // avoid false hits when newly created

	}

	public TableMergeRules getMergeRule() {
		return mergeRule;
	}

	private void buildUI() {

		String comboMinSize = "fill:min(pref;"+(new JComboBox().getMinimumSize().width)+"px):grow";
		FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu," + comboMinSize + ",4dlu,pref,4dlu," + comboMinSize + ",4dlu,pref,4dlu", // columns
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
		pb.add(new JLabel("Catalog:"), cc.xy(2,row,"r,c"));
		JTextField temp = new JTextField(mergeRule.getSourceTable().getCatalogName());
		temp.setEditable(false);
		pb.add(temp, cc.xyw(4,row,5,"f,c"));
		row += 2;
		
		pb.add(new JLabel("Schema:"), cc.xy(2,row,"r,c"));
		temp = new JTextField(mergeRule.getSourceTable().getSchemaName());
		temp.setEditable(false);
		pb.add(temp, cc.xyw(4,row,5,"f,c"));
		
		row += 2;
		pb.add(new JLabel("Table Name:"), cc.xy(2,row,"r,c"));
		temp = new JTextField(mergeRule.getTableName());
		temp.setEditable(false);
		pb.add(temp, cc.xyw(4,row,5,"f,c"));

		row += 2;
		pb.add(new JLabel("Index Name:"), cc.xy(2,row,"r,c"));
		String indexName = "";
		try {
			indexName = mergeRule.getTableIndex().getName();
		} catch (ArchitectException e1) {
			SPSUtils.showExceptionDialogNoReport(swingSession.getFrame(), 
					"An exception occured while creating the merge column rules editor", e1);
		}
		temp = new JTextField(indexName);
		temp.setEditable(false);
		pb.add(temp, cc.xyw(4,row,5,"f,c"));
		
		row += 2;
		if (!mergeRule.isSourceMergeRule()) {
			pb.add(new JLabel("Parent Table:"), cc.xy(2,row,"l,c"));
			pb.add(parentTable, cc.xy(4,row,"f,c"));
			parentTable.setSelectedItem(mergeRule.getParentTable());
			pb.add(new JLabel("Merge Action:"), cc.xy(6,row,"r,c"));
			pb.add(childMergeAction, cc.xy(8,row,"f,c"));
			childMergeAction.setSelectedItem(mergeRule.getChildMergeAction());
		} else {
			pb.add(new JLabel("Delete Dup:"), cc.xy(2,row,"r,c"));
			pb.add(deleteDup, cc.xy(4, row, "l, c"));
			deleteDup.setSelected(mergeRule.isDeleteDup());
		}
		
		row += 2;
		pb.add(new JScrollPane(ruleTable), cc.xyw(4,row,5,"f,f"));

		row+=2;
		pb.add(new JButton(saveAction), cc.xyw(4,row,5,"c,c"));
		panel = pb.getPanel();
		
	}

	private Action saveAction = new AbstractAction("Save") {
		public void actionPerformed(ActionEvent e) {
			
			//This should be uncommented when we have a proper handler
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
		
	public boolean doSave() {
		if (handler.getWorstValidationStatus().getStatus() != Status.FAIL) {
			
			if (!mergeRule.isSourceMergeRule()) {
				mergeRule.setParentTable((String) parentTable.getSelectedItem());
				mergeRule.setChildMergeAction(
					(TableMergeRules.ChildMergeActionType) childMergeAction.getSelectedItem());
			} else {
				mergeRule.setDeleteDup(deleteDup.isSelected());
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
		return false;
	}

	public JComponent getPanel() {
		return panel;
	}

	public boolean hasUnsavedChanges() {
		Object curParentTable = parentTable.getSelectedItem();
		String oldParentTable = mergeRule.getParentTable();
		Object curMergeAction = childMergeAction.getSelectedItem();
		TableMergeRules.ChildMergeActionType oldMergeAction = mergeRule.getChildMergeAction();
		
		if (tableListener.isModified()) return true;
		
		// Check if delete duplicate option has been changed
		// for source table merge rule
		if (mergeRule.isSourceMergeRule()) {
			if (this.deleteDup.isSelected() != mergeRule.isDeleteDup()) {
				return true;
			}
			
		// For related table merge rules
		} else {
			
			// Check for changes in parent table
			if ((curParentTable == null && oldParentTable != null) ||
				(curParentTable != null && oldParentTable == null)) {
				return true;
			} else if (curParentTable != null && oldParentTable != null &&
					!curParentTable.equals(oldParentTable)) {
				return true;
			}
			
			// Check for changes in child merge action
			if ((curMergeAction == null && oldMergeAction != null) ||
					(curMergeAction != null && oldMergeAction == null)) {
				return true;
			} else if (curMergeAction != null && oldMergeAction != null &&
					!curMergeAction.equals(oldMergeAction)) {
				return true;

			}
		}
		return false;
	}
	
	private class ColumnMergeRulesTable extends EditableJTable {

		public ColumnMergeRulesTable(MergeColumnRuleTableModel columnMergeRuleTableModel) {
			super(columnMergeRuleTableModel);
		}

		@Override
		public TableCellEditor getCellEditor(int row, int column) {
			if (column == 3) {
				return new DefaultCellEditor(
						new JComboBox(
								new DefaultComboBoxModel(
										ColumnMergeRules.MergeActionType.values())));
			} else {
				return super.getCellEditor(row, column);
			}
		}
	}
	
	
	private class MergeColumnRuleJTableValidator implements Validator {

		public ValidateResult validate(Object contents) {
			TableModel model = (TableModel) contents;
			for ( int i=0; i<model.getRowCount(); i++) {
				SQLColumn column = (SQLColumn) model.getValueAt(i, 0);
				MergeActionType mat = (MergeActionType) model.getValueAt(i, 3);
				if (mat == MergeActionType.CONCAT) {
					if (column.getType() != Types.VARCHAR 
							&& column.getType() != Types.LONGVARCHAR) {
						return ValidateResult.createValidateResult(Status.FAIL, "Invalid type for CONCAT");
					}
				} else if (mat == MergeActionType.SUM) {
					if (column.getType() != Types.BIGINT 
							&& column.getType() != Types.DECIMAL
							&& column.getType() != Types.DOUBLE
							&& column.getType() != Types.FLOAT
							&& column.getType() != Types.INTEGER
							&& column.getType() != Types.NUMERIC
							&& column.getType() != Types.REAL
							&& column.getType() != Types.SMALLINT
							&& column.getType() != Types.TINYINT) {
							
						return ValidateResult.createValidateResult(Status.FAIL, "Invalid type for SUM");
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
