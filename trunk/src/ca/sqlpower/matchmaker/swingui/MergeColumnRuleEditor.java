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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.ColumnMergeRules.MergeActionType;
import ca.sqlpower.matchmaker.TableMergeRules.ChildMergeActionType;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.undo.AbstractUndoableEditorPane;
import ca.sqlpower.matchmaker.util.EditableJTable;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.table.TableUtils;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class MergeColumnRuleEditor extends AbstractUndoableEditorPane<TableMergeRules, ColumnMergeRules> {

	private class MergeColumnRuleJTableValidator implements Validator {

		public ValidateResult validate(Object contents) {
			TableModel model = (TableModel) contents;
			if (getMergeRule().isSourceMergeRule()) {
				MergeActionType mat;
				for ( int i=0; i<model.getRowCount(); i++) {
					SQLColumn column = (SQLColumn) model.getValueAt(i, 0);
					mat = (MergeActionType) model.getValueAt(i, 1);
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
			}
			else {
				TableMergeRules tableMergeRule = getMergeRule();
				TableMergeRules parentMergeRule = tableMergeRule.getParentMergeRule();
				
				// checks for invalid foreign keys types
				for (ColumnMergeRules cmr : tableMergeRule.getChildren()) {
					if (cmr.getImportedKeyColumn() != null) {
						if (cmr.getImportedKeyColumn().getType() != cmr.getColumn().getType()) {
							return ValidateResult.createValidateResult(Status.FAIL, "Data type mismatch on imported key columns");
						}
					}
				}
				
				// checks for foreign keys that is not part of the parent's primary keys
				for (ColumnMergeRules cmr : tableMergeRule.getChildren()) {
					if (cmr.getImportedKeyColumn() != null) {
						if (cmr.getImportedKeyColumn().getType() != cmr.getColumn().getType()) {
							return ValidateResult.createValidateResult(Status.FAIL, "Data type mismatch on imported key columns");
						}
					}
				}
				
				// checks for invalid foreign keys
				if (parentMergeRule != null) {
					if (parentMergeRule.isSourceMergeRule()) {
						try {
							int count = 0;
							for (ColumnMergeRules cmr : tableMergeRule.getChildren()) {
								if (cmr.getImportedKeyColumn() != null) {
									boolean found = false;
									for (SQLIndex.Column column : project.getSourceTableIndex().getChildren()) {
										if (column.getColumn().equals(cmr.getImportedKeyColumn())) {
											count++;
											found = true;
											break;
										}
									}
									if (!found) {
										return ValidateResult.createValidateResult(Status.FAIL, 
												"Invalid foreign imported key columns");
									}
								}
							}
							if (count != project.getSourceTableIndex().getChildCount()) {
								return ValidateResult.createValidateResult(Status.FAIL, 
										"Invalid foreign imported key columns");
							}
						} catch (ArchitectException e) {
							throw new RuntimeException("Cannot find the source table index.");
						}
					} else {
						int primaryKeyCount = 0;
						int foreignKeyCount = 0;
						for (ColumnMergeRules parentColumn : parentMergeRule.getChildren()) {
							if (parentColumn.isInPrimaryKey()) {
								primaryKeyCount++;
							}
						}
						for (ColumnMergeRules childColumn : tableMergeRule.getChildren()) {
							if (childColumn.getImportedKeyColumn() != null) {
								boolean found = false;
								for (ColumnMergeRules parentColumn : parentMergeRule.getChildren()) {
									if (parentColumn.getColumn().equals(childColumn.getImportedKeyColumn())) {
										foreignKeyCount++;
										found = true;
										break;
									}
								}
								if (!found) {
									return ValidateResult.createValidateResult(Status.FAIL, 
											"Invalid foreign imported key columns");
								}
							}
						}
						if (primaryKeyCount != foreignKeyCount) {
							return ValidateResult.createValidateResult(Status.FAIL, "Invalid foreign imported key columns");
						}
					}
				}
				
			}
			return ValidateResult.createValidateResult(Status.OK, "");
		}
		
	}

	/**
	 * EditableJTable implementation for related merge rules. It returns 
	 * a combo box of {@link SQLColumn} in the parent table for 
	 * column 2.
	 */
	private class RelatedColumnMergeRulesTable extends EditableJTable {

		public RelatedColumnMergeRulesTable(
				AbstractMatchMakerTableModel<TableMergeRules, ColumnMergeRules> ruleTableModel) {
			super(ruleTableModel);
		}

		@Override
		public TableCellEditor getCellEditor(int row, int column) {
			if (column == 2) {
				JComboBox importedKeyColumns = new JComboBox();
				if (getParentTableComboBox().getSelectedItem() != null) {
					try {
						List<SQLColumn> tableColumns = getParentTablePrimaryKeys();
						importedKeyColumns.setModel(new DefaultComboBoxModel(tableColumns.toArray()));
						importedKeyColumns.insertItemAt(null, 0);
					} catch (ArchitectException e) {
						SPSUtils.showExceptionDialogNoReport(swingSession.getFrame(),
								"Failed to load list of columns from parent table.", e);
					}
				}
				return new DefaultCellEditor(importedKeyColumns);
			} else if (column == 3) {
				List<MergeActionType> comboList = new ArrayList<MergeActionType>();
				for (MergeActionType mat : ColumnMergeRules.MergeActionType.values()) {
					if (mat != MergeActionType.NA) {
						comboList.add(mat);
					}
				}
				return new DefaultCellEditor(
						new JComboBox(
								new DefaultComboBoxModel(comboList.toArray())));
			} else {
				return super.getCellEditor(row, column);
			}
		}
	}
	
	/**
	 * EditableJTable implementation for source merge rules. It returns 
	 * a combo box of the {@link MergeActionType} in the third column.
	 */
	private class SourceColumnMergeRulesTable extends EditableJTable {
		
		public SourceColumnMergeRulesTable(
				AbstractMatchMakerTableModel<TableMergeRules, ColumnMergeRules> ruleTableModel) {
			super(ruleTableModel);
		}

		@Override
		public TableCellEditor getCellEditor(int row, int column) {
			if (column == 1) {
				List<MergeActionType> comboList = new ArrayList<MergeActionType>();
				for (MergeActionType mat : ColumnMergeRules.MergeActionType.values()) {
					if (mat != MergeActionType.NA) {
						comboList.add(mat);
					}
				}
				return new DefaultCellEditor(
						new JComboBox(
								new DefaultComboBoxModel(comboList.toArray())));
			} else {
				return super.getCellEditor(row, column);
			}
		}
	}

	private static final Logger logger = Logger.getLogger(MergeColumnRuleEditor.class);
	private final Project project;

	private final StatusComponent status = new StatusComponent();
	private FormValidationHandler handler;
    
	/**
	 * allows the user to set the parentMergeRule
	 */ 
	private final JComboBox parentMergeRule = new JComboBox();
	
	private final List<TableMergeRules> mergeRules;

	/**
	 * allows the user to set the childMergeAction
	 */ 
	private final JComboBox childMergeAction = new JComboBox(TableMergeRules.ChildMergeActionType.values());;
	

	/**
	 * The table that lists the column merge rules
	 */
	private AbstractMatchMakerTableModel<TableMergeRules, ColumnMergeRules> ruleTableModel;

	private EditableJTable ruleTable; 
	
	private ListSelectionListener tablelistener = new ListSelectionListener(){
		public void valueChanged(ListSelectionEvent e) {
            int selectedRow = ruleTable.getSelectedRow();
            if (selectedRow >= 0) {
                ColumnMergeRules mergeColumn = mmo.getChildren().get(selectedRow); 
                MatchMakerTreeModel treeModel = (MatchMakerTreeModel) swingSession.getTree().getModel();
                TreePath menuPath = treeModel.getPathForNode(mergeColumn);
                swingSession.getTree().setSelectionPath(menuPath);
            }
		}
	};

	private Action saveAction = new AbstractAction("Save") {
		public void actionPerformed(ActionEvent e) {
			if (!applyChanges()) {
				JOptionPane.showMessageDialog(swingSession.getFrame(),
						"Merge Column rules not saved.",
						"Save",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	};
	
	public MergeColumnRuleEditor(final MatchMakerSwingSession session,
			final Project project, final TableMergeRules mr) {
		super(session, mr);
		
		this.project = project;
		
		if (project == null) {
			throw new NullPointerException("You can't edit a null project");
		}
		if (mmo == null) {
			throw new NullPointerException("You can't edit a null merge rule");
		}

		if (mmo.isSourceMergeRule()) {
			ruleTableModel = new SourceMergeColumnRuleTableModel(mmo);
			ruleTable = new SourceColumnMergeRulesTable(ruleTableModel);
		} else {
			ruleTableModel = new RelatedMergeColumnRuleTableModel(mmo);
			ruleTable = new RelatedColumnMergeRulesTable(ruleTableModel);

		}
        ruleTable.getSelectionModel().addListSelectionListener(tablelistener);
        TableUtils.fitColumnWidths(ruleTable, 15);

        mergeRules = project.getTableMergeRules();
        for (TableMergeRules tmr : project.getTableMergeRules()) {
        	if (!mmo.equals(tmr)) {
        		parentMergeRule.addItem(tmr.getSourceTable());
        	}
        }
        
        buildUI();
        addListenerToComponents();
        
        List<Action> actions = new ArrayList<Action>();
        actions.add(saveAction);
        handler = new FormValidationHandler(status,actions);
        handler.addValidateObject(ruleTable, new MergeColumnRuleJTableValidator());
        handler.resetHasValidated(); // avoid false hits when newly created
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
		JTextField temp = new JTextField(mmo.getSourceTable().getCatalogName());
		temp.setEditable(false);
		pb.add(temp, cc.xyw(4,row,5,"f,c"));
		row += 2;
		
		pb.add(new JLabel("Schema:"), cc.xy(2,row,"r,c"));
		temp = new JTextField(mmo.getSourceTable().getSchemaName());
		temp.setEditable(false);
		pb.add(temp, cc.xyw(4,row,5,"f,c"));
		
		row += 2;
		pb.add(new JLabel("Table Name:"), cc.xy(2,row,"r,c"));
		temp = new JTextField(mmo.getTableName());
		temp.setEditable(false);
		pb.add(temp, cc.xyw(4,row,5,"f,c"));

		row += 2;
		pb.add(new JLabel("Index Name:"), cc.xy(2,row,"r,c"));
		String indexName = "";

		if (mmo.getTableIndex() == null) {
			indexName = "";
		} else {
			indexName = mmo.getTableIndex().getName();
		}

		temp = new JTextField(indexName);
		temp.setEditable(false);
		pb.add(temp, cc.xyw(4,row,5,"f,c"));
		
		row += 2;
		if (!mmo.isSourceMergeRule()) {
			pb.add(new JLabel("Parent Table:"), cc.xy(2,row,"l,c"));
			pb.add(parentMergeRule, cc.xy(4,row,"f,c"));
			if (mmo.getParentMergeRule() != null) {
				parentMergeRule.setSelectedItem(mmo.getParentMergeRule().getSourceTable());
			} else {
				parentMergeRule.setSelectedItem(null);
			}
			pb.add(new JLabel("Merge Action:"), cc.xy(6,row,"r,c"));
			pb.add(childMergeAction, cc.xy(8,row,"f,c"));
			childMergeAction.setSelectedItem(mmo.getChildMergeAction());
		} 
		
		row += 2;
		pb.add(new JScrollPane(ruleTable), cc.xyw(4,row,5,"f,f"));

		row+=2;
		pb.add(new JButton(saveAction), cc.xyw(4,row,5,"c,c"));
		panel = pb.getPanel();
	}
	
	private void addListenerToComponents() {
		parentMergeRule.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				mmo.setParentMergeRuleAndImportedKeys((TableMergeRules) mergeRules.get(parentMergeRule.getSelectedIndex()));
			}});
        childMergeAction.addActionListener(new ActionListener(){
    		public void actionPerformed(ActionEvent e) {
    			mmo.setChildMergeAction((ChildMergeActionType) childMergeAction.getSelectedItem());
    		}});
	}
	
	@Override
	public boolean applyChanges() {
		if ( !handler.hasPerformedValidation() ) {
			ruleTableModel.fireTableChanged(new TableModelEvent(ruleTableModel));
		}
		ValidateResult result = handler.getWorstValidationStatus();
		if (result.getStatus() == Status.FAIL) {
			JOptionPane.showMessageDialog(swingSession.getFrame(),
					"You have to fix the error before you can save the merge rules",
					"Save",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		//adds the mergeRule to the project if it is new
		if (!project.getTableMergeRules().contains(mmo)) {
			project.getTableMergeRulesFolder().addChild(mmo);
		}

		return super.applyChanges();
	}
	
	
	public TableMergeRules getMergeRule() {
		return mmo;
	}
	
	protected JComboBox getParentTableComboBox() {
		return parentMergeRule;
	}
	
	private List<SQLColumn> getParentTablePrimaryKeys() throws ArchitectException{
		List<SQLColumn> primaryKeys = null;
		if (parentMergeRule.getSelectedItem() != null) {
			TableMergeRules tmr = mergeRules.get(parentMergeRule.getSelectedIndex());
			primaryKeys = tmr.getPrimaryKey();
		}
		return primaryKeys;
	}

	@Override
	public boolean hasUnsavedChanges() {
		if (mmo.getParent() == null) {
			return true;
		}
		return super.hasUnsavedChanges();
	}

	public void setSelectedColumn(ColumnMergeRules selectedColumn) {
		if (selectedColumn != null) {
			int selected = mmo.getChildren().indexOf(selectedColumn);			
			if (selected >= 0 && selected<ruleTable.getRowCount()) {
				ruleTable.setRowSelectionInterval(selected, selected);
			}
		}
	}

	@Override
	public void undoEventFired(
			MatchMakerEvent<TableMergeRules, ColumnMergeRules> evt) {
		if (!mmo.isSourceMergeRule()) {
			if (mmo.getParentMergeRule() != null) {
				parentMergeRule.setSelectedItem(mmo.getParentMergeRule().getSourceTable());
			} else {
				parentMergeRule.setSelectedItem(null);
			}
			childMergeAction.setSelectedItem(mmo.getChildMergeAction());
		}
		handler.performFormValidation();
	}

}
