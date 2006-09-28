package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Date;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.hibernate.Transaction;

import ca.sqlpower.architect.swingui.ArchitectPanel;
import ca.sqlpower.matchmaker.hibernate.PlMatchCriteria;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;
import ca.sqlpower.matchmaker.util.HibernateUtil;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


public class PlMatchCriteriaPanel extends JPanel implements ArchitectPanel {

	private static final long serialVersionUID = 1L;

	private PlMatchCriteria model;
	private PlMatchGroup parent;


	private JPanel criteriaEditPanel;
	JLabel groupId;
	JLabel matches;
	JComboBox column;
	JComboBox translate;
	
	JCheckBox caseSensitive;
	JCheckBox soundex;
	JCheckBox matchStart;
	JCheckBox allowNull;
	JCheckBox countWords;
	JCheckBox matchFirstPlusOneInd;
	JCheckBox removeSpecialChars;
	JCheckBox replaceWithSpaceInd;
	JCheckBox reorder;
	
	JTextField suppressChar;
	JTextField firstNChar;
	JTextField firstNCharsByWord;
	JTextField replaceWithSpace;
	JTextField minWordsInCommon;
	
	JLabel	lastUpdateDate;
	JLabel	lastUpdateUser;
	JLabel	lastUpdateOSUser;
	Color textBackground;


	/**
	 * This is the default constructor
	 */
	public PlMatchCriteriaPanel(PlMatchCriteria model) {
		super();
		buildUI();
		initialize();
		textBackground = replaceWithSpace.getBackground();
		setModel(model);
		
	}

	private void buildUI(){
	
		groupId = new JLabel();
		matches = new JLabel();
		column = new JComboBox();
		translate = new JComboBox();
		caseSensitive = new JCheckBox();
		soundex = new JCheckBox();
		matchStart = new JCheckBox();
		countWords = new JCheckBox();
		allowNull = new JCheckBox();
		matchFirstPlusOneInd = new JCheckBox();
		removeSpecialChars = new JCheckBox();
		replaceWithSpaceInd = new JCheckBox();
		reorder = new JCheckBox();
		minWordsInCommon = new JTextField();
		firstNChar = new JTextField();
		replaceWithSpace = new JTextField();
		firstNCharsByWord = new JTextField();
		suppressChar = new JTextField();
		lastUpdateDate= new JLabel();
		lastUpdateUser= new JLabel();
		lastUpdateOSUser= new JLabel();
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		FormLayout formLayout = new FormLayout("3dlu, pref, 5dlu, pref, 5dlu, pref,5dlu, pref,  5dlu, pref,5dlu, pref,5dlu, pref,5dlu, pref, 3dlu");
		PanelBuilder pb = new PanelBuilder(formLayout);
		pb.appendRelatedComponentsGapRow();
		pb.appendRow("pref");
		CellConstraints cc = new CellConstraints();
		CellConstraints cl= new CellConstraints();
		pb.add(new JLabel("Column"), cl.xy(2,2),column, cc.xy(4,2));
		pb.add(new JLabel(MatchCriteriaColumn.ALLOW_NULL.getName()), cl.xy(6,2),allowNull, cc.xy(8,2));
		pb.add(new JLabel(MatchCriteriaColumn.CASE_SENSITIVE_IND.getName()), cl.xy(10,2),caseSensitive, cc.xy(12,2));
		pb.add(new JLabel(MatchCriteriaColumn.TRANSLATE_GROUP_NAME.getName()), cl.xy(14,2),translate, cc.xy(16,2));
		pb.appendRelatedComponentsGapRow();
		pb.appendRow("pref");
		pb.add(new JLabel(MatchCriteriaColumn.REMOVE_SPECIAL_CHARS.getName()), cl.xy(2,4),getCheckedTextBox(removeSpecialChars,suppressChar), cc.xyw(4,4,5));
		pb.add(new JLabel(MatchCriteriaColumn.REPLACE_WITH_SPACE_IND.getName()), cl.xy(10,4),getCheckedTextBox(replaceWithSpaceInd,replaceWithSpace), cc.xyw(12,4,5));
		pb.appendRelatedComponentsGapRow();
		pb.appendRow("pref");
		pb.add(new JLabel(MatchCriteriaColumn.REORDER_IND.getName()), cl.xy(2,6),reorder, cc.xy(4,6));
		pb.add(new JLabel(MatchCriteriaColumn.FIRST_N_CHARS_BY_WORD.getName()), cl.xy(6,6),firstNCharsByWord, cc.xy(8,6));
		pb.add(new JLabel(MatchCriteriaColumn.FIRST_N_CHAR.getName()), cl.xy(10,6),firstNChar, cc.xy(12,6));
		pb.add(new JLabel(MatchCriteriaColumn.MATCH_FIRST_PLUS_ONE_IND.getName()), cl.xy(14,6),matchFirstPlusOneInd, cc.xy(16,6));
		pb.appendRelatedComponentsGapRow();
		pb.appendRow("pref");
		pb.add(new JLabel(MatchCriteriaColumn.MIN_WORDS_IN_COMMON.getName()), cl.xy(2,8),minWordsInCommon, cc.xy(4,8));	
		pb.add(new JLabel(MatchCriteriaColumn.MATCH_START.getName()), cl.xy(6,8),matchStart, cc.xy(8,8));
		pb.add(new JLabel(MatchCriteriaColumn.SOUND_IND.getName()), cl.xy(10,8),soundex, cc.xy(12,8));
		pb.add(new JLabel(MatchCriteriaColumn.COUNT_WORDS_IND.getName()), cl.xy(14,8),countWords, cc.xy(16,8));
		
		criteriaEditPanel = pb.getPanel();
		
	}

	
	public PlMatchCriteria getModel() {
		return model;
	}

	public void setModel(PlMatchCriteria model) {
		this.model = model;
		
		loadMatchCriteria(model);
	}

	private void loadMatchCriteria(PlMatchCriteria model) {
		// load the new model
		KeyListener l= new KeyListener(){
			
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				validateForm();	
			}
			
			public void keyTyped(KeyEvent e) {
				validateForm();				
			}
			
		};
		firstNChar.addKeyListener(l);
		firstNCharsByWord.addKeyListener(l);
		minWordsInCommon.addKeyListener(l);

		
		ActionListener al = new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				validateForm();
				
			}
			
		};
		
		replaceWithSpaceInd.addActionListener(al);
		removeSpecialChars.addActionListener(al);
		
		
		
		
		groupId = new JLabel(model.getId().getGroupId());
		matches = new JLabel(model.getId().getMatchId());
		// TODO add column junk
		allowNull.setSelected(model.isAllowNullInd());
		caseSensitive.setSelected(model.isCaseSensitiveInd());
		removeSpecialChars.setSelected(model.isRemoveSpecialChars());
		suppressChar.setText(model.getSuppressChar() == null? "":model.getSuppressChar());
		replaceWithSpaceInd.setSelected(model.isReplaceWithSpaceInd());
		replaceWithSpace.setText(model.getReplaceWithSpace() == null ? "" : model.getReplaceWithSpace());
		// TODO add translate
		reorder.setSelected(model.isReorderInd());
		firstNCharsByWord.setText(model.getFirstNCharByWord() == null? "0":model.getFirstNCharByWord().toString());
		firstNChar.setText(model.getFirstNChar()== null? "0":model.getFirstNChar().toString());
		matchFirstPlusOneInd.setSelected(model.isMatchFirstPlusOneInd());
		minWordsInCommon.setText(model.getMinWordsInCommon() == null? "0":model.getMinWordsInCommon().toString());
		matchStart.setSelected(model.isMatchStart());
		soundex.setSelected(model.isSoundInd());
		countWords.setSelected(model.isCountWordsInd());
		Date updated = model.getLastUpdateDate();
		
		lastUpdateDate = new JLabel(updated == null? "N/A" :updated.toString());
		lastUpdateUser = new JLabel(model.getLastUpdateUser() == null? "N/A" :model.getLastUpdateUser());
		lastUpdateOSUser= new JLabel(model.getLastUpdateOsUser()== null?"N/A":model.getLastUpdateOsUser());
	}
	
	private boolean validateForm(){
		Boolean valid = true;
		suppressChar.setEnabled(removeSpecialChars.isSelected());
		replaceWithSpace.setEnabled(replaceWithSpaceInd.isSelected());

		try {
			new Long(firstNChar.getText());
			firstNChar.setBackground(textBackground);
		} catch (NumberFormatException e) {
			firstNChar.setBackground(Color.red);
		}
		try {
			new Long(firstNCharsByWord.getText());
			firstNCharsByWord.setBackground(textBackground);
		} catch (NumberFormatException e) {
			firstNCharsByWord.setBackground(Color.red);
		}
		try {
			new Long(minWordsInCommon.getText());
			minWordsInCommon.setBackground(textBackground);
		} catch (NumberFormatException e) {
			minWordsInCommon.setBackground(Color.red);
		}


		return valid;
		
	}
	private boolean saveMatches(PlMatchCriteria model) {
		if ( validateForm()){
	
			try {
				System.out.println("Saving "+model);
				Transaction tx = HibernateUtil.primarySession().beginTransaction();
				HibernateUtil.primarySession().flush();
				tx.commit();
			
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return validateForm();
	}

	private JPanel getCheckedTextBox(JCheckBox box, JTextField field) {
		JPanel p = new JPanel(new BorderLayout());
		p.add(box,BorderLayout.WEST);
		p.add(field,BorderLayout.CENTER);
		return p;
		
	}
	


	public boolean applyChanges() {
		return saveMatches(model);
	}

	public void discardChanges() {
		
	}

	public JComponent getPanel() {
		return  criteriaEditPanel;
	}
	
} 
