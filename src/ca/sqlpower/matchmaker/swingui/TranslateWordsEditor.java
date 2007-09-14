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

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
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
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.MatchMakerTranslateWord;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


/**
 * An editor pane that allows the user to add/remove/move 
 * translate word pares
 */
public class TranslateWordsEditor implements EditorPane {
	
	private JPanel panel;
	private JScrollPane translateWordsScrollPane;
	TranslateWordsTableModel translateWordsTableModel;
	private JTable translateWordsTable;
	private JTextField from;
	private JTextField to;
	private JTextField groupName;
	
	private final MatchMakerSwingSession swingSession;
	private final MatchMakerTranslateGroup group;
		
	private FormValidationHandler handler;
	StatusComponent status = new StatusComponent();

	private static final Logger logger = Logger.getLogger(TranslateWordsEditor.class);
	
	
	public TranslateWordsEditor(MatchMakerSwingSession swingSession,
			MatchMakerTranslateGroup group) {
		this.swingSession = swingSession;
		this.group = group;
		setupTable();
		buildUI();
	}
	
	private void setupTable() {
		translateWordsTableModel = new TranslateWordsTableModel(swingSession, group);
		translateWordsTable = new JTable(translateWordsTableModel);
        translateWordsTable.setName("Translate Words");
        
        //adds an action listener that looks for a double click, that opens the selected 
        //merge rule editor pane  
        translateWordsTable.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
					int row = TranslateWordsEditor.this.translateWordsTable.getSelectedRow();
					
//					Object parent = menuPath.getLastPathComponent();
//					Object child = menuTree.getModel().getChild(parent, row);
//					menuTree.setSelectionPath(menuPath.pathByAddingChild(child));
			}		
        });
	}
	
	private void buildUI() {
	
		//This created the layout for the internal panel at the top wit
		//the lables and the text fields
		
		String txt = "fill:min(pref;"+(new JTextField().getMinimumSize().width)+"px):grow";
		FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,"+txt+",4dlu,pref,4dlu,"+txt+",4dlu,pref,4dlu,pref,4dlu", // columns
				"4dlu,pref,4dlu,pref,4dlu,pref,4dlu"); // rows
			//	 1     2    3    4    5    6    7 

		PanelBuilder internalPB;
		JPanel internalP = logger.isDebugEnabled() ? 
				new FormDebugPanel(layout) : new JPanel(layout);
		
		internalPB = new PanelBuilder(layout, internalP);
		CellConstraints cc = new CellConstraints();

		int row = 2;
		internalPB.add(status, cc.xy(4,row));
		
		row += 2;
		internalPB.add(new JLabel ("Group Name:"), cc.xy(2,row,"r,t"));
		groupName = new JTextField(group.getName());
		internalPB.add(groupName, cc.xy(4,row,"f,f"));
		
		row += 2;
		internalPB.add(new JLabel ("From:"), cc.xy(2,row,"r,t"));
		from = new JTextField();
		internalPB.add(from, cc.xy(4,row,"f,f"));
		
		internalPB.add(new JLabel ("To:"), cc.xy(6,row,"r,t"));
		to = new JTextField();
		internalPB.add(to, cc.xy(8,row,"f,f"));
		internalPB.add(new JButton(createWordsAction), cc.xy(10,row,"l,t"));
		
		//The layout for the external frame that houses the table and the button bars
		FormLayout bbLayout = new FormLayout(
				"4dlu,pref,50dlu,fill:min(pref;"+(new JComboBox().getMinimumSize().width)+"px):grow, 4dlu,pref,4dlu", // columns
				"4dlu,pref,4dlu,fill:40dlu:grow,4dlu,pref,4dlu,pref,4dlu"); // rows
			//	 1     2    3    4    5    6    7     8   9   

		PanelBuilder externalPB;
		JPanel externalP = logger.isDebugEnabled() ? 
				new FormDebugPanel(bbLayout) : new JPanel(bbLayout);
		
		externalPB = new PanelBuilder(bbLayout, externalP);
		CellConstraints bbcc = new CellConstraints();
		
		row = 4;
		translateWordsScrollPane = new JScrollPane(translateWordsTable);
		externalPB.add(translateWordsScrollPane, bbcc.xy(4,row,"f,f"));

		ButtonStackBuilder bsb = new ButtonStackBuilder();
		bsb.addGridded(new JButton(moveTopAction));
		bsb.addRelatedGap();
		bsb.addGridded(new JButton(moveUpAction));
		bsb.addRelatedGap();
		bsb.addGridded(new JButton(moveDownAction));
		bsb.addRelatedGap();
		bsb.addGridded(new JButton(moveBottomAction));
		externalPB.add(bsb.getPanel(), bbcc.xy(6,row,"c,c"));
		
		row += 2;
		ButtonBarBuilder bbb = new ButtonBarBuilder();
		//new actions for delete and save should be extracted and be put into its own file.
		bbb.addGridded(new JButton(deleteWordsAction));
		bbb.addRelatedGap();
		bbb.addGridded(new JButton(saveGroupAction));
		
		externalPB.add(bbb.getPanel(), bbcc.xy(4,row,"c,c"));
		
		externalPB.add(internalPB.getPanel(), cc.xyw(2,2,4,"f,f"));
		panel = externalPB.getPanel();
	}
	
	public boolean doSave() {
		// TODO Auto-generated method stub
		logger.debug("DO SAVE: NOT implemented");
		return false;
	}

	public JComponent getPanel() {
		return panel;
	}

	public boolean hasUnsavedChanges() {
		logger.debug("has unsaved changes: NOT IMPLEMENTED");
		return false;
	}
	
	/**
	 * By giving it the location of a certain column in the table, it scrolls through the  
	 * translateTable to make sure that the column is visible.
	 * 
	 * @param index - the index location of the column you want to focus on in the translateTable
	 */
	private void scrollToSelected(int index){
		Rectangle cellRect = translateWordsTable.getCellRect(index,0, false);
		if (!translateWordsTable.getVisibleRect().getBounds().contains(cellRect)){	
			translateWordsTable.scrollRectToVisible(cellRect);
		}
	}
	
    Action createWordsAction = new AbstractAction("Create Translation"){

        public void actionPerformed(ActionEvent e) {
        	MatchMakerTranslateWord word = new MatchMakerTranslateWord();
            group.addChild(word);
            word.setFrom(from.getText());
            word.setTo(to.getText());
            translateWordsTable.scrollRectToVisible(translateWordsTable.getCellRect(group.getChildCount()-1,
            		0, true).getBounds());
            from.setText("");
            to.setText("");
            from.requestFocus();
            ((TranslateWordsTableModel) (translateWordsTable.getModel())).updateModel();
        }       
    };
    
    Action deleteWordsAction = new AbstractAction("Delete Selected Translations"){
        
        public void actionPerformed(ActionEvent e) {
            ArrayList<Integer> selectedIndeces = new ArrayList<Integer>();
            for (int selectedRowIndex:translateWordsTable.getSelectedRows()){
                selectedIndeces.add(new Integer(selectedRowIndex));
            }
            for (int i=selectedIndeces.size()-1;i >= 0; i--){
                group.removeChild(group.getChildren().get((int)selectedIndeces.get(i)));
            }
            ((TranslateWordsTableModel) (translateWordsTable.getModel())).updateModel();
        }        
    };
    
	Action saveGroupAction = new AbstractAction("Save Group"){

		public void actionPerformed(ActionEvent e) {
			group.setName(groupName.getText());
			if (!swingSession.getTranslations().getChildren().contains(group)) {
				swingSession.getTranslations().addNewChild(group);
			}
			group.syncChildrenSeqNo();
			swingSession.getDAO(MatchMakerTranslateGroup.class).save(group);
		}
		
	};
    
	Action moveUpAction = new AbstractAction("", SPSUtils.createIcon("chevrons_up1", "Move Up")){
		public void actionPerformed(ActionEvent e){
			final int index = translateWordsTable.getSelectedRow();
			if (index >=0 && index < translateWordsTable.getRowCount() ){
				if (translateWordsTable.getSelectedRowCount() == 1 && index > 0){						
					Collections.swap(group.getChildren()
									, (index - 1), index);
					translateWordsTable.setRowSelectionInterval(index-1, index-1);
					scrollToSelected(index-1);
				}
			}
		}
	};
	
	Action moveDownAction = new AbstractAction("", SPSUtils.createIcon("chevrons_down1", "Move Down")){
		public void actionPerformed(ActionEvent e) {
			final int index = translateWordsTable.getSelectedRow();
			if (index >=0 && index < translateWordsTable.getRowCount() ){
				if (translateWordsTable.getSelectedRowCount() == 1 && index < (translateWordsTable.getRowCount() -1) ){						
					Collections.swap( group.getChildren()
									, (index + 1), index);

					translateWordsTable.setRowSelectionInterval(index+1, index+1);
				 	scrollToSelected(index+1);
				}
			}
		}	
	};
	
	Action moveTopAction = new AbstractAction("", SPSUtils.createIcon("chevrons_up2", "Move To Top")){
		public void actionPerformed(ActionEvent e){
			final int index = translateWordsTable.getSelectedRow();
			if (index >=0 && index < translateWordsTable.getRowCount() ){
				if (translateWordsTable.getSelectedRowCount() == 1 && index > 0){
					List<MatchMakerTranslateWord> translateList = group.getChildren();
                    MatchMakerTranslateWord selectedTranslate=translateList.get(index);
					
                    translateList.remove(index);
					translateList.add(0, selectedTranslate);
					translateWordsTable.setRowSelectionInterval(0,0);
					scrollToSelected(0);
				}
			}
		}
	};
	
	Action moveBottomAction = new AbstractAction("", SPSUtils.createIcon("chevrons_down2", "Move To Bottom")){
		public void actionPerformed(ActionEvent e) {
			final int index = translateWordsTable.getSelectedRow();
			if (index >=0 && index < translateWordsTable.getRowCount() ){
				if (translateWordsTable.getSelectedRowCount() == 1 && index < (translateWordsTable.getRowCount() -1) ){						
					List <MatchMakerTranslateWord> translateList = group.getChildren();
                    MatchMakerTranslateWord selectedTranslate=translateList.get(index);
					
                    translateList.remove(index);
					translateList.add(translateList.size(), selectedTranslate);
					translateWordsTable.setRowSelectionInterval(translateList.size()-1,translateList.size()-1);
					scrollToSelected(translateList.size()-1);
				}
			}
		}

			
	};

	/**
	 * table model for the translation words, it shows the translation words
	 * in a JTable, allows user add/delete/reorder translation words
	 * it has 2 column:
	 * 		from	 -- original words
	 *      to		 -- replaced words 
	 */
	private class TranslateWordsTableModel extends AbstractTableModel {

		private List<MatchMakerTranslateWord> rows;
		private MatchMakerSwingSession swingSession;
		private MatchMakerTranslateGroup group;
		
		public TranslateWordsTableModel(MatchMakerSwingSession swingSession, 
				MatchMakerTranslateGroup group) {
			this.swingSession = swingSession;
			rows = group.getChildren();
		}
		
		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return rows.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return rows.get(rowIndex).getFrom();
			} else if (columnIndex == 1) {
				return rows.get(rowIndex).getTo();
			} else {
				return null;
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}
		
		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) {
				return "From";
			} else if (columnIndex == 1) {
				return "To";
			}
			return null;
		}

		public List<MatchMakerTranslateWord> getTranslateWords() {
			return rows;
		}
		
		public void updateModel() {
			fireTableDataChanged();
		}
		
		public void removeRules(int index) {
			if (swingSession.getTranslations().isInUseInBusinessModel(group)) {
				JOptionPane.showMessageDialog(swingSession.getFrame(),
					"The parent translation group is in use, and cannot be modified.");
			} else {
				group.removeChild(group.getChildren().get(index));
				fireTableDataChanged();
			}
		}
	}
}
