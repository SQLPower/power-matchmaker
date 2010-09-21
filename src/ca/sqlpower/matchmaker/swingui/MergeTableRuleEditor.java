/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */


package ca.sqlpower.matchmaker.swingui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.TableMergeRules.ChildMergeActionType;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.swingui.action.DeriveRelatedRulesAction;
import ca.sqlpower.matchmaker.swingui.action.NewMergeRuleAction;
import ca.sqlpower.matchmaker.undo.AbstractUndoableEditorPane;
import ca.sqlpower.matchmaker.util.EditableJTable;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.table.TableUtils;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class MergeTableRuleEditor extends AbstractUndoableEditorPane {

	private static final Logger logger = Logger.getLogger(MergeTableRuleEditor.class);
	
	private JScrollPane mergeRulesScrollPane;
	
	private JTable mergeRulesTable;
	private MergeTableRuleTableModel mergeTableRuleTableModel;

	private JTree menuTree;
	private TreePath menuPath;

	
	public MergeTableRuleEditor(MatchMakerSwingSession swingSession,Project project) {
		super(swingSession, project);
		
        setupRulesTable(swingSession,project);
        buildUI();
        
        //finds the tree and menu path with that will allow the the double click
        //button to open the editor windows
		menuTree = MergeTableRuleEditor.this.swingSession.getTree();
		menuPath = menuTree.getSelectionPath();
		
	}
	
	/**
	 * EditableJTable implementation for table merge rules. It returns 
	 * a combo box of the {@link ChildMergeActionType} in the third column.
	 */
	private class RelatedTableMergeRulesTable extends EditableJTable {
		
		public RelatedTableMergeRulesTable(MergeTableRuleTableModel ruleTableModel) {
			super(ruleTableModel);
		}

		@Override
		public TableCellEditor getCellEditor(int row, int column) {
			if (column == 3) {
				JComboBox mergeActionComboBox = new JComboBox(TableMergeRules.ChildMergeActionType.values());
				mergeActionComboBox.setSelectedItem(getModel().getValueAt(row, column));
				return new DefaultCellEditor(mergeActionComboBox);
			} else {
				return super.getCellEditor(row, column);
			}
		}
	}
	
	private void setupRulesTable(MatchMakerSwingSession swingSession, Project project) {
		mergeTableRuleTableModel = new MergeTableRuleTableModel(project);

		mergeRulesTable = new RelatedTableMergeRulesTable(mergeTableRuleTableModel);
        mergeRulesTable.setName("Merge Tables");

        //Enables/disables the buttons according to the selected table row
        mergeRulesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {
				int row = MergeTableRuleEditor.this.mergeRulesTable.getSelectedRow();
				
				moveDown.setEnabled(false);
				moveUp.setEnabled(false);
				deleteRule.setEnabled(false);
				
				if (row > 1) {
					moveUp.setEnabled(true);
				}
				if (row > 0 && row < MergeTableRuleEditor.this.mergeRulesTable.getRowCount() - 1) {
					moveDown.setEnabled(true);
				}
				if (row > 0 && row < MergeTableRuleEditor.this.mergeRulesTable.getRowCount()) {
					deleteRule.setEnabled(true);
				}
			}});
        
        //adds an action listener that looks for a double click, that opens the selected 
        //merge rule editor pane  
        mergeRulesTable.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int row = MergeTableRuleEditor.this.mergeRulesTable.getSelectedRow();
					
					Object parent = menuPath.getLastPathComponent();
					Object child = menuTree.getModel().getChild(parent, row);
					menuTree.setSelectionPath(menuPath.pathByAddingChild(child));
				}
			}
        });

        mergeRulesTable.setDefaultRenderer(Boolean.class,new CheckBoxRenderer());
        mergeRulesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		TableUtils.fitColumnWidths(mergeRulesTable, 15);
	}

	private void buildUI() {
		
		FormLayout layout = new FormLayout(
				"4dlu,14dlu,4dlu,fill:min(pref;"+3*(new JComboBox().getMinimumSize().width)+"px):grow, 4dlu,pref,4dlu", // columns
				"10dlu,pref,12dlu,pref,4dlu,fill:40dlu:grow,4dlu,pref,4dlu"); // rows
			//	 1     2    3     4    5    6    7     8         9    10        


		PanelBuilder pb;
		JPanel p = logger.isDebugEnabled() ? 
				new FormDebugPanel(layout) : new JPanel(layout);
		pb = new PanelBuilder(layout, p);
		CellConstraints cc = new CellConstraints();

		int row = 2;
		pb.add(new JLabel("List of table merge rules:"), cc.xy(4,row));
		row += 2;

		pb.add(new JLabel("Merge Rules:"), cc.xy(4,row,"l,t"));
		row += 2;
		mergeRulesScrollPane = new JScrollPane(mergeRulesTable);
		pb.add(mergeRulesScrollPane, cc.xy(4,row,"f,f"));

		ButtonStackBuilder bsb = new ButtonStackBuilder();
		bsb.addGridded(new JButton(moveUp));
		bsb.addRelatedGap();
		bsb.addGridded(new JButton(moveDown));
		pb.add(bsb.getPanel(), cc.xy(6,row,"c,c"));
		
		ButtonBarBuilder bbb = new ButtonBarBuilder();
		//new actions for delete and save should be extracted and be put into its own file.
		bbb.addGridded(new JButton(new NewMergeRuleAction(swingSession, mmo)));
		bbb.addRelatedGap();
		bbb.addGridded(new JButton(new DeriveRelatedRulesAction(swingSession, mmo)));
		bbb.addRelatedGap();
		bbb.addGridded(new JButton(deleteRule));
		bbb.addRelatedGap();
		bbb.addGridded(new JButton(saveAction));
		row+=2;
		pb.add(bbb.getPanel(), cc.xy(4,row,"c,c"));
		panel = pb.getPanel();
		
		moveDown.setEnabled(false);
		moveUp.setEnabled(false);
		deleteRule.setEnabled(false);
	}

	private Action moveUp = new AbstractAction("", SPSUtils.createIcon("chevrons_up1", "Move Up")) {
		public void actionPerformed(ActionEvent e) {
			final int selectedRow = mergeRulesTable.getSelectedRow();
			logger.debug("moving merge rule "+selectedRow+" up");
			mmo.moveChild(selectedRow, selectedRow-1);
			mergeRulesTable.setRowSelectionInterval(selectedRow-1, selectedRow-1);
		}
	};

	private Action moveDown = new AbstractAction("", SPSUtils.createIcon("chevrons_down1", "Move Down")) {
		public void actionPerformed(ActionEvent e) {
			final int selectedRow = mergeRulesTable.getSelectedRow();
			logger.debug("moving merge rule "+selectedRow+" down");
			mmo.moveChild(selectedRow, selectedRow+1);
			mergeRulesTable.setRowSelectionInterval(selectedRow+1, selectedRow+1);
		}
	};
	
	private Action deleteRule = new AbstractAction("Delete") {
		public void actionPerformed(ActionEvent e) {
			int selectedRow = mergeRulesTable.getSelectedRow();
			logger.debug("deleting merge rule:"+selectedRow);
			int responds = JOptionPane.showConfirmDialog(
					swingSession.getFrame(),
					"Are you sure you want to delete the merge rule?", 
					"Delete Confirmation", 
					JOptionPane.YES_NO_OPTION);
			if (responds != JOptionPane.YES_OPTION)
				return;

			TableMergeRules rule = mmo.getChildren(TableMergeRules.class).get(selectedRow);
			try {
				mmo.removeChild(rule);
			} catch (ObjectDependentException e1) {
				throw new RuntimeException(e1);
			}
			if (selectedRow >= mergeRulesTable.getRowCount()) {
				selectedRow = mergeRulesTable.getRowCount() - 1;
			}
			if (selectedRow > 0) {
				mergeRulesTable.setRowSelectionInterval(selectedRow, selectedRow);
			}
		}
	};
	
	/**
     * Saves the list in table model to merge rules
     */
	private Action saveAction = new AbstractAction("Save") {
		public void actionPerformed(final ActionEvent e) {
            try {
                applyChanges();
            } catch (Exception ex) {
                MMSUtils.showExceptionDialog(swingSession.getFrame(),
                		"Merge Interface Not Saved", ex);
            }
		}
	};


	@Override
	public void undoEventFired(MatchMakerEvent evt) {
		//No components needs refresh.
	}
}
