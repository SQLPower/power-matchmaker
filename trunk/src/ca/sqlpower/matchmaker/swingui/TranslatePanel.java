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
import java.awt.event.ActionListener;
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

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.MatchMakerTranslateWord;
import ca.sqlpower.matchmaker.util.EditableJTable;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.validation.AlwaysOKValidator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class TranslatePanel implements DataEntryPanel {
	
	private static final Logger logger = Logger.getLogger(TranslatePanel.class);
	
    private final MatchMakerSwingSession swingSession;

    private JTable translateTable;
	private JPanel translatePanel;
	private JComboBox translationGroup;
	private JTextField newGroupName;
	private JButton createGroup;
	private JButton deleteGroup;
    private JButton createWord;
    private JButton deleteWord;
	private JButton saveGroup;
	private JButton moveItemUp;
	private JButton moveItemDown;
	private JButton moveItemToTop;
	private JButton moveItemToBottom;
	private JScrollPane tableScrollPane;
    private MatchMakerTranslateGroup matchMakerTranslateGroup; 
    private StatusComponent statusLabel;
    private FormValidationHandler handler;
	
	public TranslatePanel(MatchMakerSwingSession swingSession) {
        this.swingSession = swingSession;
		buildUI();
	}
	
	private void buildUI(){
        statusLabel = new StatusComponent();
        handler = new FormValidationHandler(statusLabel);
		translateTable = new EditableJTable();
		translationGroup = new JComboBox();
		translationGroup.setModel(new TranslationComboBoxModel(swingSession.getTranslations()));
        
		createWord = new JButton(createWordsAction);
        deleteWord = new JButton(deleteWordsAction);
        
		if (translationGroup.getModel().getSize() > 0) {
		    translationGroup.setSelectedIndex(0);
		    matchMakerTranslateGroup = (MatchMakerTranslateGroup) translationGroup.getSelectedItem();
		    translateTable.setModel(new MatchTranslateTableModel(matchMakerTranslateGroup));
		} else {
            translateTable.setModel(new MatchTranslateTableModel(new MatchMakerTranslateGroup()));
            translateTable.setEnabled(false);
            createWord.setEnabled(false);
            deleteWord.setEnabled(false);
        }
        
		translationGroup.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                matchMakerTranslateGroup = (MatchMakerTranslateGroup) translationGroup.getSelectedItem();
                if (matchMakerTranslateGroup != null){
                    translateTable.setModel(new MatchTranslateTableModel(matchMakerTranslateGroup));
                    translateTable.setEnabled(true);
                    createWord.setEnabled(true);
                    deleteWord.setEnabled(true);
                } else {
                    translateTable.setModel(new MatchTranslateTableModel(new MatchMakerTranslateGroup()));
                    translateTable.setEnabled(false);
                    createWord.setEnabled(false);
                    deleteWord.setEnabled(false);
                }
            }
        });
		newGroupName = new JTextField();
		createGroup = new JButton(createGroupAction);
        createGroupAction.setEnabled(false);
		deleteGroup = new JButton(deleteGroupAction);
        
		saveGroup = new JButton(saveGroupAction);
		moveItemUp = new JButton(moveItemUpAction);
		moveItemDown = new JButton(moveItemDownAction);
		moveItemToTop = new JButton(moveItemTopAction);
		moveItemToBottom = new JButton (moveItemBottomAction);
		
		tableScrollPane = new JScrollPane(translateTable);
		
		translateTable.setDragEnabled(true);
		
		FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,fill:120dlu:grow,4dlu, pref, 10dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu,pref,4dlu");				
		PanelBuilder pb;
		JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
		
		pb = new PanelBuilder(layout, p);
		pb.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();
		
		pb.appendRow("2dlu");
		pb.appendRow("pref");
        pb.add(statusLabel, cc.xyw(2,2,4));
        pb.appendRow("4dlu");
        pb.appendRow("pref");        
		pb.add(new JLabel("Group Name:"), cc.xy(2,4));
		pb.add(newGroupName, cc.xy(4,4));
		pb.add(createGroup, cc.xy(8,4));
		pb.appendRow("4dlu");
		pb.appendRow("pref");
		pb.add(translationGroup, cc.xy(4,6));
		pb.add(saveGroup, cc.xy(8,6));
        pb.add(deleteGroup, cc.xy(10,6));
		pb.appendRow("4dlu");
		pb.appendRow("fill:80dlu:grow");
		pb.add(tableScrollPane, cc.xyw(2,8,10,"f,f"));
		
		ButtonStackBuilder bsb = new ButtonStackBuilder();
		bsb.addGridded(moveItemToTop);
		bsb.addRelatedGap();
		bsb.addGlue();
		bsb.addGridded(moveItemUp);
		bsb.addRelatedGap();
		bsb.addGlue();
		bsb.addGridded(moveItemDown);
		bsb.addRelatedGap();
		bsb.addGlue();
		bsb.addGridded(moveItemToBottom);
		bsb.addRelatedGap();
		bsb.addGlue();
		pb.add(bsb.getPanel(), cc.xy(14, 8,"c,c"));
		pb.appendRow("4dlu");
		pb.appendRow("pref");
        ButtonBarBuilder bbb = new ButtonBarBuilder();
        bbb.addGridded(createWord);
        bbb.addUnrelatedGap();
        bbb.addGridded(deleteWord);
        pb.add(bbb.getPanel(),cc.xyw(1, 10, 14));
        pb.appendRow("4dlu");
		pb.appendRow("pref");
		
        List<Action> groupActions = new ArrayList<Action>();
        groupActions.add(createGroupAction);
        MMODuplicateValidator mmoValidator = new MMODuplicateValidator(swingSession.getTranslations(),
                                    groupActions, "translate group name", 35);
        handler.addValidateObject(newGroupName, mmoValidator);
        List<Action> wordsActions = new ArrayList<Action>();
        wordsActions.add(saveGroupAction);
        TranslateWordValidator wordValidator = new TranslateWordValidator(translateTable,wordsActions);
        handler.addValidateObject(translateTable, wordValidator);
        AlwaysOKValidator okValidator = new AlwaysOKValidator();
        handler.addValidateObject(translationGroup, okValidator);
        
        translatePanel = pb.getPanel();
        
        
	}
	
	
	
	public boolean applyChanges() {
		return true;
	}

	public void discardChanges() {
        // not necessary
	}

	public JComponent getPanel() {
		return translatePanel;
	}
	
	public JTable getTranslateTable() {
		return translateTable;
	}
    
    
	////////////Action variables///////////////////////
	
	Action createGroupAction = new AbstractAction("Create Group"){

		public void actionPerformed(ActionEvent e) {
            MatchMakerTranslateGroup mmtg = new MatchMakerTranslateGroup();
            mmtg.setName(newGroupName.getText());
            newGroupName.setText("");
            swingSession.getTranslations().addNewChild(mmtg);
            translationGroup.setSelectedItem(mmtg);
		}		
	};
	
	Action deleteGroupAction = new AbstractAction("Delete Group"){

		public void actionPerformed(ActionEvent e) {
			MatchMakerTranslateGroup tg = (MatchMakerTranslateGroup) translationGroup.getSelectedItem();
			if (swingSession.getTranslations().isInUseInBusinessModel(tg)) {
				JOptionPane.showMessageDialog(translatePanel,
						"This translation group is in use, and cannot be deleted.");
			} else {
                swingSession.getTranslations().removeAndDeleteChild(tg);
			}
		}
		
	};
	
	Action saveGroupAction = new AbstractAction("Save Group"){

		public void actionPerformed(ActionEvent e) {
			MatchMakerTranslateGroup group = (MatchMakerTranslateGroup) translationGroup.getSelectedItem();
            if (group != null){
                group.syncChildrenSeqNo();
                swingSession.getDAO(MatchMakerTranslateGroup.class).save(group);
            }
		}
		
	};
    
    Action createWordsAction = new AbstractAction("Create Translation"){

        public void actionPerformed(ActionEvent e) {
            matchMakerTranslateGroup.addChild(new MatchMakerTranslateWord());
            translateTable.clearSelection();
            translateTable.setRowSelectionInterval(matchMakerTranslateGroup.getChildCount()-1, matchMakerTranslateGroup.getChildCount()-1);
            translateTable.editCellAt(matchMakerTranslateGroup.getChildCount()-1, 0);
            translateTable.grabFocus();
            translateTable.scrollRectToVisible(translateTable.getCellRect(matchMakerTranslateGroup.getChildCount()-1, 0, true).getBounds());
        }       
    };
    
    Action deleteWordsAction = new AbstractAction("Delete Selected Translations"){
        
        public void actionPerformed(ActionEvent e) {
            ArrayList<Integer> selectedIndeces = new ArrayList<Integer>();
            for (int selectedRowIndex:translateTable.getSelectedRows()){
                selectedIndeces.add(new Integer(selectedRowIndex));
            }
            Collections.sort(selectedIndeces);
            for (int i=selectedIndeces.size()-1;i >= 0; i--){
                matchMakerTranslateGroup.removeChild(matchMakerTranslateGroup.getChildren().get((int)selectedIndeces.get(i)));
            }
        }
        
    };
    
	Action moveItemUpAction = new AbstractAction("^"){
		public void actionPerformed(ActionEvent e){
			final int index = getTranslateTable().getSelectedRow();
			if (index >=0 && index < translateTable.getRowCount() ){
				if (getTranslateTable().getSelectedRowCount() == 1 && index > 0){						
					Collections.swap(getTranslations()
									, (index - 1), index);
					translateTable.setRowSelectionInterval(index-1, index-1);
					scrollToSelected(index-1);
				}
			}
		}
	};
	
	Action moveItemDownAction = new AbstractAction("v"){
		public void actionPerformed(ActionEvent e) {
			final int index = getTranslateTable().getSelectedRow();
			if (index >=0 && index < translateTable.getRowCount() ){
				if (getTranslateTable().getSelectedRowCount() == 1 && index < (translateTable.getRowCount() -1) ){						
					Collections.swap( getTranslations()
									, (index + 1), index);

				 	translateTable.setRowSelectionInterval(index+1, index+1);
				 	scrollToSelected(index+1);
				}
			}
		}	
	};
	
	Action moveItemTopAction = new AbstractAction("^^"){
		public void actionPerformed(ActionEvent e){
			final int index = getTranslateTable().getSelectedRow();
			if (index >=0 && index < translateTable.getRowCount() ){
				if (getTranslateTable().getSelectedRowCount() == 1 && index > 0){
					List<MatchMakerTranslateWord> translateList= getTranslations();
                    MatchMakerTranslateWord selectedTranslate=translateList.get(index);
					translateList.remove(index);
					translateList.add(0, selectedTranslate);
					translateTable.setRowSelectionInterval(0,0);
					scrollToSelected(0);
				}
			}
		}
	};
	
	Action moveItemBottomAction = new AbstractAction("vv"){
		public void actionPerformed(ActionEvent e) {
			final int index = getTranslateTable().getSelectedRow();
			if (index >=0 && index < translateTable.getRowCount() ){
				if (getTranslateTable().getSelectedRowCount() == 1 && index < (translateTable.getRowCount() -1) ){						
					List <MatchMakerTranslateWord> translateList=  getTranslations();
                    MatchMakerTranslateWord selectedTranslate=translateList.get(index);
					translateList.remove(index);
					translateList.add(translateList.size(), selectedTranslate);
					translateTable.setRowSelectionInterval(translateList.size()-1,translateList.size()-1);
					scrollToSelected(translateList.size()-1);
				}
			}
		}

			
	};
	private List<MatchMakerTranslateWord> getTranslations() {
		return ((MatchMakerTranslateGroup)translationGroup.getSelectedItem()).getChildren();
	}
	
	/**
	 * By giving it the location of a certain column in the table, it scrolls through the  
	 * translateTable to make sure that the column is visible.
	 * 
	 * @param index - the index location of the column you want to focus on in the translateTable
	 */
	private void scrollToSelected(int index){
		Rectangle cellRect = translateTable.getCellRect(index,0, false);
		if (!translateTable.getVisibleRect().getBounds().contains(cellRect)){	
			translateTable.scrollRectToVisible(cellRect);
		}
	}
	
	

}
