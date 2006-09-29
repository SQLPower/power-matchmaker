package ca.sqlpower.matchmaker.swingui;

import java.awt.event.ActionEvent;
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
import ca.sqlpower.matchmaker.hibernate.PlMatchTranslate;
import ca.sqlpower.matchmaker.util.EditableJTable;

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
	private JButton searchButton;
	private JButton addCommonWords;
	private JButton copyGroup;
	private JButton helpButton;
	private List<PlMatchTranslate> translates;

	
	public TranslatePanel() {
		buildUI();
	}
	
	private void buildUI(){
		translateTable = new EditableJTable();
		translateTable.setModel(new MatchTranslateTableModel(MatchMakerFrame.getMainInstance().getTranslations()));
		searchGroup = new JTextField();
		searchButton = new JButton (searchAction);
		createGroup = new JButton(createGroupAction);
		deleteGroup = new JButton(deleteGroupAction);
		addCommonWords = new JButton(addCommonWordsAction);
		copyGroup = new JButton(copyGroupAction);
		
		helpButton = new JButton(helpAction);
		
		
		
		FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,fill:120dlu:grow,4dlu, pref, 10dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu");				
		PanelBuilder pb;
		JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
		pb = new PanelBuilder(layout, p);
		CellConstraints cc = new CellConstraints();
		
		pb.appendRow("2dlu");
		pb.appendRow("pref");
		pb.add(new JLabel("Group Name:"), cc.xy(2,2));
		pb.add(searchGroup, cc.xy(4,2));
		pb.add(searchButton, cc.xy(6,2));
		pb.add(createGroup, cc.xy(8,2));
		pb.add(deleteGroup, cc.xy(10,2));
		pb.appendRow("4dlu");
		pb.appendRow("pref");
		pb.add(copyGroup, cc.xy(8,4));
		pb.add(addCommonWords, cc.xy(10,4));
		pb.appendRow("4dlu");
		pb.appendRow("fill:60dlu:grow");
		pb.add(new JScrollPane(translateTable), cc.xyw(2,6,10,"f,f"));
		pb.appendRow("4dlu");
		pb.appendRow("pref");
		translatePanel = pb.getPanel();
	}
	
	
	private void loadTranslateRecords(){
		translates = MatchMakerFrame.getMainInstance().getTranslations();
		if (translates != null){
			
			
		}
		
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

	////////////Action variables///////////////////////
	
	Action searchAction = new AbstractAction("Search"){

		public void actionPerformed(ActionEvent e) {
			String text = searchGroup.getText();
			
		}
		
	};
	
	
	Action createGroupAction = new AbstractAction("Create Group"){

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub	
		}		
	};
	
	
	Action deleteGroupAction = new AbstractAction("Delete Group"){

		public void actionPerformed(ActionEvent e) {
			
		}
		
	};
	
	Action copyGroupAction = new AbstractAction("Copy Group"){

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	Action addCommonWordsAction = new AbstractAction("Add Common Words"){

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	Action helpAction = new AbstractAction("Help"){
		public void actionPerformed(ActionEvent e){
			
		}
	};


}
