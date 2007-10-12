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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.swingui.action.NewMergeRuleAction;
import ca.sqlpower.matchmaker.util.EditableJTable;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.table.TableUtils;
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
	
	//keeps track of whether the editor pane has unsaved changes
	private CustomTableModelListener tableListener;
	
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
		tableListener = new CustomTableModelListener();
		mergeTableRuleTableModel.addTableModelListener(tableListener);
		
		mergeRulesTable = new EditableJTable(mergeTableRuleTableModel);
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
		bbb.addGridded(new JButton(deriveRelated));
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

	private void setDefaultSelections() {
		desc.setText(match.getTableMergeRulesFolder().getFolderDesc());
	}
	
	private Action moveUp = new AbstractAction("", SPSUtils.createIcon("chevrons_up1", "Move Up")) {
		public void actionPerformed(ActionEvent e) {
			final int selectedRow = mergeRulesTable.getSelectedRow();
			logger.debug("moving merge rule "+selectedRow+" up");
			match.getTableMergeRulesFolder().swapChildren(selectedRow, selectedRow-1);
			mergeRulesTable.setRowSelectionInterval(selectedRow-1, selectedRow-1);
			TableUtils.fitColumnWidths(mergeRulesTable, 15);
		}
	};
	
	private Action deriveRelated = new AbstractAction("Derive Related Rules") {
		public void actionPerformed(ActionEvent e) {
			try {
				new RelatedTableDeriver(match,swingSession);
			} catch (ArchitectException e1) {
				SPSUtils.showExceptionDialogNoReport(swingSession.getFrame(),
						"Failed to generate columns for match source table.", e1);
			}
		}
	};
	
	private Action moveDown = new AbstractAction("", SPSUtils.createIcon("chevrons_down1", "Move Down")) {
		public void actionPerformed(ActionEvent e) {
			final int selectedRow = mergeRulesTable.getSelectedRow();
			logger.debug("moving merge rule "+selectedRow+" down");
			match.getTableMergeRulesFolder().swapChildren(selectedRow, selectedRow+1);
			mergeRulesTable.setRowSelectionInterval(selectedRow+1, selectedRow+1);
			TableUtils.fitColumnWidths(mergeRulesTable, 15);
		}
	};
	
	private Action deleteRule = new AbstractAction("Delete") {
		public void actionPerformed(ActionEvent e) {
			int selectedRow = mergeRulesTable.getSelectedRow();
			logger.debug("deleting merge rule:"+selectedRow);
			int responds = JOptionPane.showConfirmDialog(swingSession.getFrame(),
				"Are you sure you want to delete the merge rule?");
			if (responds != JOptionPane.YES_OPTION)
				return;

			TableMergeRules rule = match.getTableMergeRules().get(selectedRow);
			match.removeTableMergeRule(rule);
			if (selectedRow >= mergeRulesTable.getRowCount()) {
				selectedRow = mergeRulesTable.getRowCount() - 1;
			}
			if (selectedRow > 0) {
				mergeRulesTable.setRowSelectionInterval(selectedRow, selectedRow);
			}
			TableUtils.fitColumnWidths(mergeRulesTable, 15);
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
		swingSession.save(match);
		this.tableListener.setModified(false);
		return true;
	}

	public JComponent getPanel() {
		return panel;
	}

	/**
	 * Returns true if there are changes that have not been saved.
	 */
	public boolean hasUnsavedChanges() {
        return this.tableListener.isModified();
	}

	
}
