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
import java.util.Collections;
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
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A panel to edit the translate words groups
 */
public class TranslateGroupsEditor implements EditorPane {

	private static final Logger logger = Logger.getLogger(TranslateGroupsEditor.class);
	
	private JPanel panel;
	private JScrollPane translateGroupsScrollPane;
	TranslateGroupsTableModel translateGroupsTableModel;
	private JTable translateGroupsTable;
	
	private JTree menuTree;
	private TreePath menuPath;

	private final MatchMakerSwingSession swingSession;
	private final List<MatchMakerTranslateGroup> translateGroups;

	private FormValidationHandler handler;
	StatusComponent status = new StatusComponent();
	
	public TranslateGroupsEditor(MatchMakerSwingSession swingSession) {
		this.swingSession = swingSession;
		this.translateGroups = swingSession.getTranslations().getChildren();
		
		setupTable();
        buildUI();
        handler = new FormValidationHandler(status);
        handler.resetHasValidated();
		
        //TODO: finish adding it to the tree
        menuTree = swingSession.getTree();
	}
	
	private void setupTable() {
		translateGroupsTableModel = new TranslateGroupsTableModel(swingSession);
		translateGroupsTable = new JTable(translateGroupsTableModel);
        translateGroupsTable.setName("Translate Groups");
        
        //adds an action listener that looks for a double click, that opens the selected 
        //merge rule editor pane  
        translateGroupsTable.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				
				if (e.getClickCount() == 2) {
					int row = TranslateGroupsEditor.this.translateGroupsTable.getSelectedRow();
					TranslateWordsEditor wordsEditor = new TranslateWordsEditor(swingSession,
							translateGroups.get(row));
					swingSession.setCurrentEditorComponent(wordsEditor);
//					menuTree.setSelectionPath(menuPath.pathByAddingChild(child));
				}
			}		
        });

        translateGroupsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	
	private void buildUI() {
		FormLayout layout = new FormLayout(
				"4dlu,46dlu,4dlu,fill:min(pref;"+3*(new JComboBox().getMinimumSize().width)+"px):grow,4dlu,50dlu", // columns
				"10dlu,pref,4dlu,pref,4dlu,fill:40dlu:grow,4dlu,pref,10dlu"); // rows
			//	   1     2   3    4     5   6     7   8     9    10   11
		
		PanelBuilder pb;
		JPanel p = logger.isDebugEnabled() ? 
				new FormDebugPanel(layout) : new JPanel(layout);
				
		pb = new PanelBuilder(layout, p);
		CellConstraints cc = new CellConstraints();
		
		int row = 2;
		pb.add(status, cc.xy(4,row));
		
		row += 2;
		pb.add(new JLabel("Translation Groups:"), cc.xy(4,row,"l,t"));
		
		row += 2;
		translateGroupsScrollPane = new JScrollPane(translateGroupsTable);
		pb.add(translateGroupsScrollPane, cc.xy(4,row,"f,f"));
		
		ButtonBarBuilder bbb = new ButtonBarBuilder();
		//new actions for delete and save should be extracted and be put into its own file.
		bbb.addGridded(new JButton(createGroupAction));
		bbb.addRelatedGap();
		bbb.addGridded(new JButton(deleteGroupAction));

		row+=2;
		pb.add(bbb.getPanel(), cc.xy(4,row,"c,c"));
		
		panel = pb.getPanel();
	}

	public boolean doSave() {
		logger.debug("Do Save: Not implemented :(");
		return false;
	}

	public JComponent getPanel() {
		return panel;
	}

	public boolean hasUnsavedChanges() {
		logger.debug("Has unsaved changes: Not implemented :(");
		return false;
	}
	
	Action createGroupAction = new AbstractAction("Create Group") {
		public void actionPerformed(ActionEvent e) {
            MatchMakerTranslateGroup tg = new MatchMakerTranslateGroup();
            swingSession.setCurrentEditorComponent(new TranslateWordsEditor(swingSession, tg));
		}		
	};
	
	Action deleteGroupAction = new AbstractAction("Delete Group") {
		public void actionPerformed(ActionEvent e) {
			int selectedRow = translateGroupsTable.getSelectedRow();
			int response = JOptionPane.showConfirmDialog(swingSession.getFrame(),
				"Are you sure you want to delete the translate group?");
			if (response != JOptionPane.YES_OPTION) {
				return;
			}
		
			logger.debug("deleting translate group:"+selectedRow);
			
			if ( selectedRow >= 0 && selectedRow < translateGroupsTable.getRowCount()) {
				translateGroupsTableModel.removeRules(selectedRow);
				if (selectedRow >= translateGroupsTable.getRowCount()) {
					selectedRow = translateGroupsTable.getRowCount() - 1;
				}
				if (selectedRow >= 0) {
					translateGroupsTable.setRowSelectionInterval(selectedRow, selectedRow);
				}
			} else {
				JOptionPane.showMessageDialog(swingSession.getFrame(),
						"Please select one merge rule to delete");
			}
		}
	};
	
	/**
	 * table model for the translation groups, it shows the translation groups
	 * in a JTable, allows user add/delete/reorder translation groups
	 * it has 1 column:
	 * 		translation group name  -- name in a string
	 *
	 */
	private class TranslateGroupsTableModel extends AbstractTableModel {

		private List<MatchMakerTranslateGroup> rows;
		private MatchMakerSwingSession swingSession;
		
		public TranslateGroupsTableModel(MatchMakerSwingSession swingSession) {
			this.swingSession = swingSession;
			rows = swingSession.getTranslations().getChildren();
			Collections.sort(rows);
		}
		
		public int getColumnCount() {
			return 1;
		}

		public int getRowCount() {
			return rows.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return rows.get(rowIndex);
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
			return MatchMakerTranslateGroup.class;
		}
		
		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) {
				return "Name";
			}
			return null;
		}

		public List<MatchMakerTranslateGroup> getTranslateGroups() {
			return rows;
		}
		
		public void removeRules(int index) {
			if (swingSession.getTranslations().isInUseInBusinessModel(rows.get(index))) {
				JOptionPane.showMessageDialog(swingSession.getFrame(),
					"This translation group is in use, and cannot be deleted.");
			} else {
				swingSession.getTranslations().removeAndDeleteChild(rows.get(index));
				fireTableDataChanged();
			}
		}
	}
}
