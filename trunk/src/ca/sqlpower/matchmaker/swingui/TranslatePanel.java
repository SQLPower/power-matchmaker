package ca.sqlpower.matchmaker.swingui;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ArchitectPanel;
import ca.sqlpower.architect.swingui.table.TableModelSearchDecorator;
import ca.sqlpower.matchmaker.hibernate.PlMatchTranslate;
import ca.sqlpower.matchmaker.util.EditableJTable;

import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class TranslatePanel implements ArchitectPanel {
	
	private static final Logger logger = Logger.getLogger(TranslatePanel.class);
	
	private JTable translateTable;
	private JPanel translatePanel;
	private JTextField searchGroup;
	private JButton createGroup;
	private JButton deleteGroup;
	private JButton addCommonWords;
	private JButton copyGroup;
	private JButton helpButton;
	private JButton moveItemUp;
	private JButton moveItemDown;
	private JButton moveGroupUp;
	private JButton moveGroupDown;
	private JScrollPane tableScrollPane;
	private TableModelSearchDecorator tms;

	
	public TranslatePanel() {
		buildUI();
	}
	
	private void buildUI(){
		translateTable = new EditableJTable();
		refreshTranslateTable();
		
		searchGroup = new JTextField();
		tms.setDoc(searchGroup.getDocument());
		createGroup = new JButton(createGroupAction);
		deleteGroup = new JButton(deleteGroupAction);
		addCommonWords = new JButton(addCommonWordsAction);
		copyGroup = new JButton(copyGroupAction);
		moveItemUp = new JButton(moveItemUpAction);
		moveItemDown = new JButton(moveItemDownAction);
		tableScrollPane = new JScrollPane(translateTable);
		
		translateTable.setDragEnabled(true);
		
		helpButton = new JButton(helpAction);
			
		FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,fill:120dlu:grow,4dlu, pref, 10dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu,pref,4dlu");				
		PanelBuilder pb;
		JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
		pb = new PanelBuilder(layout, p);
		CellConstraints cc = new CellConstraints();
		
		pb.appendRow("2dlu");
		pb.appendRow("pref");
		pb.add(new JLabel("Group Name:"), cc.xy(2,2));
		pb.add(searchGroup, cc.xy(4,2));
		pb.add(createGroup, cc.xy(8,2));
		pb.add(deleteGroup, cc.xy(10,2));
		pb.appendRow("4dlu");
		pb.appendRow("pref");
		pb.add(copyGroup, cc.xy(8,4));
		pb.add(addCommonWords, cc.xy(10,4));
		pb.appendRow("4dlu");
		pb.appendRow("fill:60dlu:grow");
		pb.add(tableScrollPane, cc.xyw(2,6,10,"f,f"));
		
		ButtonStackBuilder bsb = new ButtonStackBuilder();
		bsb.addGridded(moveItemUp);
		bsb.addRelatedGap();
		bsb.addGlue();
		bsb.addGridded(moveItemDown);
		bsb.addRelatedGap();
		bsb.addGlue();
		
		pb.add(bsb.getPanel(), cc.xy(14, 6,"c,c"));
		pb.appendRow("4dlu");
		pb.appendRow("pref");
		translatePanel = pb.getPanel();
	}
	
	/**
	 * This method removes all the contents within translate table and recompiles the translate
	 * data from the translateList from the main instance and sets up the table models again
	 */
	private void refreshTranslateTable(){
		translateTable.removeAll();
		tms = new TableModelSearchDecorator(new MatchTranslateTableModel(MatchMakerFrame.getMainInstance().getTranslations()));
		tms.setTableTextConverter((EditableJTable) translateTable);
		translateTable.setModel(tms);
	}
	
	
	public boolean applyChanges() {
		// TODO Auto-generated method stub
		return false;
	}

	public void discardChanges() {
		// TODO Auto-generated method stub

	}

	public JComponent getPanel() {
		return translatePanel;
	}
	
	public void updateTable(){
		
		
		

	}

	
	public JTable getTranslateTable() {
		return translateTable;
	}
	////////////Action variables///////////////////////
	
	
	Action createGroupAction = new AbstractAction("Create Group"){

		public void actionPerformed(ActionEvent e) {
			//	TODO Auto-generated method stub

		}		
	};
	
	
	Action deleteGroupAction = new AbstractAction("Delete Group"){

		public void actionPerformed(ActionEvent e) {
			List<PlMatchTranslate> translates = MatchMakerFrame.getMainInstance().getTranslations();
			
		}
		
	};
	
	Action copyGroupAction = new AbstractAction("Copy Group"){

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	Action addCommonWordsAction = new AbstractAction("Add Common Words"){

		public void actionPerformed(ActionEvent e) {
			PlMatchTranslate currentSelected = MatchMakerFrame.getMainInstance().getTranslations().get(translateTable.getSelectedColumn());
			PlMatchTranslate newTranslate = new PlMatchTranslate();
			
		}
		
	};
	
	Action helpAction = new AbstractAction("Help"){
		public void actionPerformed(ActionEvent e){
			
		}
	};
	
	Action moveItemUpAction = new AbstractAction("^"){
		public void actionPerformed(ActionEvent e){
			final int index = getTranslateTable().getSelectedRow();
			if (index >=0 && index < translateTable.getRowCount() ){
				if (getTranslateTable().getSelectedRowCount() == 1 && index > 0){						
					Collections.swap(MatchMakerFrame.getMainInstance().getTranslations()
									, (index - 1), index);
					refreshTranslateTable();
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
					Collections.swap(MatchMakerFrame.getMainInstance().getTranslations()
									, (index + 1), index);
					refreshTranslateTable();
				 	translateTable.setRowSelectionInterval(index+1, index+1);
				 	scrollToSelected(index+1);
				}
			}
		}	
	};
	
	//Doesn't work yet
	private void scrollToSelected(int index){
		Rectangle cellRect = translateTable.getCellRect(index,0, false);
		if (!tableScrollPane.getVisibleRect().getBounds().contains(cellRect)){
			tableScrollPane.scrollRectToVisible(cellRect);
		}
	}
	
	

}
