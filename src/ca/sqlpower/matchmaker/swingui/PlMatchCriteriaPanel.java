package ca.sqlpower.matchmaker.swingui;

import java.awt.Color;
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

import com.jgoodies.forms.layout.FormLayout;


public class PlMatchCriteriaPanel extends JPanel implements ArchitectPanel {

	private static final long serialVersionUID = 1L;

	private PlMatchCriteria model;
	private PlMatchGroup parent;


	private JPanel groupEditPanel;
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
		this.model = model;
		initialize();
		
		
	}

	private void buildUI(){
		FormLayout layout = new FormLayout("","");
		groupId = new JLabel();
		matches = new JLabel();
		column = new JComboBox();
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


		
	}

	public PlMatchCriteria getModel() {
		return model;
	}

	public void setModel(PlMatchCriteria model) {
		this.model = model;
		
		loadMatches(model);
	}

	private void loadMatches(PlMatchCriteria model) {
		// load the new model
		KeyListener listener = new KeyListener(){
			
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
		groupId = new JLabel(model.getId().getGroupId());
		matches = new JLabel(model.getId().getMatchId());


		Date updated = model.getLastUpdateDate();
		
		lastUpdateDate = new JLabel(updated == null? "N/A" :updated.toString());
		lastUpdateUser = new JLabel(model.getLastUpdateUser() == null? "N/A" :model.getLastUpdateUser());
		lastUpdateOSUser= new JLabel(model.getLastUpdateOsUser()== null?"N/A":model.getLastUpdateOsUser());
	}
	
	private boolean validateForm(){
		Boolean valid = true;
		


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


	


	public boolean applyChanges() {
		return saveMatches(model);
	}

	public void discardChanges() {
		loadMatches(model);
	}

	public JComponent getPanel() {
		return this;
	}
	
} 
