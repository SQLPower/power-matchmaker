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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.swingui.action.NewMergeRuleAction;
import ca.sqlpower.matchmaker.util.EditableJTable;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.table.TableUtils;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class MergeTableRuleEditor implements EditorPane {

	private static final Logger logger = Logger.getLogger(MergeTableRuleEditor.class);
	
	private JPanel panel;
	private JTextArea desc = new JTextArea(3,80);
	private JTable mergeRulesTable;
	MergeTableRuleTableModel mergeTableRuleTableModel;
	private JScrollPane mergeRulesScrollPane;
	
	private JTree menuTree;
	private TreePath menuPath;

	private final MatchMakerSwingSession swingSession;
	private Match match;

	StatusComponent status = new StatusComponent();
	private FormValidationHandler handler;
	
	public MergeTableRuleEditor(MatchMakerSwingSession swingSession,Match match) {
		this.swingSession = swingSession;
		this.match = match;
		if (match == null) {
			throw new NullPointerException("You can't edit a null match");
		}
        handler = new FormValidationHandler(status);
        setupRulesTable(swingSession,match);
        buildUI();
        setDefaultSelections();
        handler.resetHasValidated(); // avoid false hits when newly created
        
        //finds the tree and menu path with that will allow the the double click
        //button to open the editor windows
		menuTree = MergeTableRuleEditor.this.swingSession.getTree();
		menuPath = menuTree.getSelectionPath();
	}
	
	private void setupRulesTable(MatchMakerSwingSession swingSession, Match match) {
		mergeTableRuleTableModel = new MergeTableRuleTableModel(match,swingSession);
		mergeRulesTable = new TableMergeRulesTable(mergeTableRuleTableModel);
        mergeRulesTable.setName("Merge Tables");
        
        
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
				"10dlu,pref,4dlu,pref,4dlu,min(pref;40dlu),12dlu,pref,4dlu,fill:40dlu:grow,4dlu,pref,4dlu"); // rows
			//	 1     2    3    4               5    6    7     8         9    10   11      


		PanelBuilder pb;
		JPanel p = logger.isDebugEnabled() ? 
				new FormDebugPanel(layout) : new JPanel(layout);
		pb = new PanelBuilder(layout, p);
		CellConstraints cc = new CellConstraints();

		int row = 2;
		pb.add(status, cc.xy(4,row));
		row += 2;
		
        desc.setWrapStyleWord(true);
        desc.setLineWrap(true);
		pb.add(new JLabel("Description:"), cc.xy(4,row,"l,t"));
		row += 2;
		pb.add(new JScrollPane(desc), cc.xy(4,row,"f,f"));
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
		bbb.addGridded(new JButton(new NewMergeRuleAction(swingSession, match)));
		bbb.addRelatedGap();
		bbb.addGridded(new JButton(deleteRule));
		bbb.addRelatedGap();
		bbb.addGridded(new JButton(saveAction));
		row+=2;
		pb.add(bbb.getPanel(), cc.xy(4,row,"c,c"));
		panel = pb.getPanel();
	}

	private void setDefaultSelections() {
		desc.setText(match.getTableMergeRulesFolder().getFolderDesc());
	}
	
	private Action moveUp = new AbstractAction("", SPSUtils.createIcon("chevrons_up1", "Move Up")) {
		public void actionPerformed(ActionEvent e) {
			final int selectedRow = mergeRulesTable.getSelectedRow();
			logger.debug("moving merge rule "+selectedRow+" up");
			if ( selectedRow > 0 &&	selectedRow < mergeRulesTable.getRowCount()) {
				mergeTableRuleTableModel.moveRuleUp(selectedRow);
				mergeRulesTable.setRowSelectionInterval(selectedRow-1, selectedRow-1);
			}
		}
	};
	
	private Action moveDown = new AbstractAction("", SPSUtils.createIcon("chevrons_down1", "Move Down")) {
		public void actionPerformed(ActionEvent e) {
			final int selectedRow = mergeRulesTable.getSelectedRow();
			logger.debug("moving merge rule "+selectedRow+" down");
			if ( selectedRow >= 0 && selectedRow < mergeRulesTable.getRowCount()-1) {
				mergeTableRuleTableModel.moveRuleDown(selectedRow);
				mergeRulesTable.setRowSelectionInterval(selectedRow+1, selectedRow+1);
			}
		}
	};
	
	private Action deleteRule = new AbstractAction("Delete") {
		public void actionPerformed(ActionEvent e) {
			int selectedRow = mergeRulesTable.getSelectedRow();
			int responds = JOptionPane.showConfirmDialog(swingSession.getFrame(),
			"Are you sure you want to delete the merge rule?");
			if (responds != JOptionPane.YES_OPTION)
				return;
			logger.debug("deleting merge rule:"+selectedRow);
			if ( selectedRow >= 0 && selectedRow < mergeRulesTable.getRowCount()) {
				mergeTableRuleTableModel.removeRules(selectedRow);
				if (selectedRow >= mergeRulesTable.getRowCount()) {
					selectedRow = mergeRulesTable.getRowCount() - 1;
				}
				if (selectedRow >= 0) {
					mergeRulesTable.setRowSelectionInterval(selectedRow, selectedRow);
				}
			} else {
				JOptionPane.showMessageDialog(swingSession.getFrame(),
						"Please select one merge rule to delete");
			}
		}
	};
	
	/**
     * Saves the list in table model to merge rules
     */
	private Action saveAction = new AbstractAction("Save") {
		public void actionPerformed(final ActionEvent e) {
            try {
                doSave();
            } catch (Exception ex) {
                MMSUtils.showExceptionDialog(swingSession.getFrame(),
                		"Merge Interface Not Saved", ex);
            }
		}
	};
	
	/**
	 * Saves the order of the tableMergeRules.  Modifies tree UI if the order
	 * of any children has been changed.
	 */
	public boolean doSave() {
		logger.debug("#1 children size="+match.getTableMergeRules().size());
		long count = 0;
		boolean orderChanged = false;
		for (TableMergeRules t : mergeTableRuleTableModel.getMergeRules()){
			if (!orderChanged && t.getSeqNo()!= count){
				orderChanged = true;
			}
			t.setSeqNo(count++);
		}
		swingSession.save(match);
		if (orderChanged){
			match.getTableMergeRulesFolder().childrenOrderChanged();
		}
		return true;
	}

	public JComponent getPanel() {
		return panel;
	}

	/**
	 * Checks for unsaved changes by checking the ordering of the tableMergeRules.
	 */
	public boolean hasUnsavedChanges() {
		long count = 0;
		for (TableMergeRules t : mergeTableRuleTableModel.getMergeRules()){
			if ( t.getSeqNo()!= count){
				return true;
			}
			count++;
		}
		return false;
	}

	/**
	 * table model for the merge table rules, it shows the merge tables
	 * in a JTable, allows user add/delete/reorder merge tables
	 * it has 4 columns:
	 * 		table catalog    -- merge table catalog in a combo box
	 * 		table schema    -- merge table schema in a combo box
	 * 		table name    -- merge table name in a combo box
	 * 		delete dup ind     -- merge table delete dup ind in a check box
	 *
	 */
	private class MergeTableRuleTableModel extends AbstractTableModel {

		private List<TableMergeRules> rows;
		private SQLObjectChooser chooser;
		private MatchMakerSwingSession swingSession;
		
		public MergeTableRuleTableModel(Match match, 
				MatchMakerSwingSession swingSession) {
			this.swingSession = swingSession;
			this.chooser = new SQLObjectChooser(swingSession);
			rows = match.getTableMergeRules();
		}
		
		public int getColumnCount() {
			return 4;
		}

		public int getRowCount() {
			return rows.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return rows.get(rowIndex).getCatalogName();
			} else if (columnIndex == 1) {
				return rows.get(rowIndex).getSchemaName();
			} else if (columnIndex == 2) {
				return rows.get(rowIndex).getTableName();
			} else if ( columnIndex == 3) {
				return rows.get(rowIndex).isDeleteDup() ? "Yes" : "No";
			} else {
				return null;
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) {
				return SQLCatalog.class;
			} else if (columnIndex == 1) {
				return SQLSchema.class;
			} else if (columnIndex == 2) {
				return SQLTable.class;
			} else if ( columnIndex == 3) {
				return String.class;
			}
			return super.getColumnClass(columnIndex);
		}
		
		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) {
				return chooser.getCatalogTerm().getText();
			} else if (columnIndex == 1) {
				return chooser.getSchemaTerm().getText();
			} else if (columnIndex == 2) {
				return "Name";
			} else if ( columnIndex == 3) {
				return "Delete Duplicates?";
			}
			return null;
		}

		public List<TableMergeRules> getMergeRules() {
			return rows;
		}
		
		public void newRules() {
			rows.add(new TableMergeRules());
			fireTableDataChanged();
		}
		
		public void removeRules(int index) {
			swingSession.delete(rows.get(index));
			fireTableDataChanged();
		}
		
		public void moveRuleUp(int index) {
			TableMergeRules selectedRow = rows.get(index);
			TableMergeRules targetRow = rows.get(index-1);
			rows.remove(targetRow);
			rows.add(rows.indexOf(selectedRow)+1, targetRow);
			fireTableDataChanged();
		}
		public void moveRuleDown(int index) {
			TableMergeRules selectedRow = rows.get(index);
			TableMergeRules targetRow = rows.get(index+1);
			rows.remove(targetRow);
			rows.add(rows.indexOf(selectedRow), targetRow);
			fireTableDataChanged();
		}
	}
	
	private class TableMergeRulesTable extends EditableJTable {

		private MergeTableRuleTableModel mergeTableRuleTableModel;
		public TableMergeRulesTable(MergeTableRuleTableModel mergeTableRuleTableModel) {
			super(mergeTableRuleTableModel);
			this.mergeTableRuleTableModel = mergeTableRuleTableModel;
		}

	}
	
	private class TableMergeRulesValidator implements Validator {

		private SQLObjectChooser chooser;
		public TableMergeRulesValidator(SQLObjectChooser chooser) {
			this.chooser = chooser;
		}
		
		public ValidateResult validate(Object contents) {
			if (chooser.getTableComboBox().getSelectedItem() == null) {
				return ValidateResult.createValidateResult(Status.WARN,
						"Merge table is required");
			}
			return ValidateResult.createValidateResult(Status.OK, "");
		}
    }
}
