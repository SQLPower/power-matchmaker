package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;

import ca.sqlpower.architect.swingui.ArchitectPanel;
import ca.sqlpower.architect.swingui.table.TableModelColumnAutofit;
import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class PlMatchGroupPanel extends JPanel implements ArchitectPanel {

	private static final long serialVersionUID = 1L;

	
	private PlMatchGroup model;


	private JSplitPane jSplitPane = null;


	private JScrollPane matchCriteriaScrollPane = null;


	private JTable matchCriteriaTable = null;


	private JPanel groupEditPanel;
	JTextField groupId;
	JComboBox matches;
	JTextField description;
	JTextField matchPercent;
	JTextField filterCriteria;
	JCheckBox active;
	JLabel	lastUpdateDate;
	JLabel	lastUpdateUser;
	JLabel	lastUpdateOSUser;
	Color textBackground;


	private Color oldMatchesBG;

	/**
	 * This is the default constructor
	 */
	public PlMatchGroupPanel(PlMatchGroup model) {
		super();
		this.model = model;
		initialize();
		
		
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {

		this.setLayout(new BorderLayout());
		this.add(getJSplitPane(), BorderLayout.CENTER);
		
	}

	public PlMatchGroup getModel() {
		return model;
	}

	public void setModel(PlMatchGroup model) {
		this.model = model;
		TableModelColumnAutofit tableModelColumnAutofit = new TableModelColumnAutofit(new MatchCriteriaTableModel(model),matchCriteriaTable);
		matchCriteriaTable.setModel(tableModelColumnAutofit);
		tableModelColumnAutofit.setTableHeader(matchCriteriaTable.getTableHeader());
		
		loadMatches(model);
	}

	private void loadMatches(PlMatchGroup model) {
		// load the new model
		KeyListener listener = new KeyListener(){
			
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void keyTyped(KeyEvent e) {
				validateForm();				
			}
			
		};
		groupId = new JTextField(model.getId().getGroupId());
		groupId.addKeyListener(listener);
		textBackground = groupId.getBackground();
		matches = new JComboBox();
		oldMatchesBG= matches.getBackground();
		matches.setModel(new FolderComboBoxModel<PlMatch>(MatchMakerFrame.getMainInstance().getMatches()));
		
		matches.setSelectedItem(model.getPlMatch());
		description = new JTextField(model.getDescription());
		matchPercent = new JTextField(model.getMatchPercent().toPlainString());
		matchPercent.addKeyListener(listener);
		filterCriteria = new JTextField(model.getFilterCriteria());
		active = new JCheckBox();
		active.setSelected(model.isActiveInd());
		Date updated = model.getLastUpdateDate();
		
		lastUpdateDate = new JLabel(updated == null? "N/A" :updated.toString());
		lastUpdateUser = new JLabel(model.getLastUpdateUser() == null? "N/A" :model.getLastUpdateUser());
		lastUpdateOSUser= new JLabel(model.getLastUpdateOsUser()== null?"N/A":model.getLastUpdateOsUser());
	}
	
	private boolean validateForm(){
		Boolean valid = true;
		PlMatch parentMatch = (PlMatch) matches.getModel().getSelectedItem();
		if (parentMatch == null){
			matches.setBackground(Color.red);
		} else {
			matches.setBackground(oldMatchesBG);
			for (PlMatchGroup group: parentMatch.getPlMatchGroups()){
				if ( group.getId().getGroupId() == groupId.getText() 
						&& group != model ){
					groupId.setBackground(Color.red);
					valid = false;
					break;
				} else {
					groupId.setBackground(textBackground);
				}
				
			}
		}
		
		try {
			BigDecimal d = new BigDecimal(matchPercent.getText());
			if (d.intValue() >= 0 && d.intValue() <= 100){
				matchPercent.setBackground(textBackground);
			} else {
				valid = false;
				matchPercent.setBackground(Color.red);
			}
			
		} catch (NumberFormatException e){
			valid = false;
			matchPercent.setBackground(Color.red);
		}

		return valid;
		
	}
	private boolean saveMatches(PlMatchGroup model) {
		if ( validateForm()){
			// load the new model
			model.getId().setGroupId(groupId.getText());
			model.getId().setMatchId(((PlMatch)matches.getSelectedItem()).getMatchId());
			model.setDescription(description.getText());
			model.setMatchPercent(new BigDecimal(matchPercent.getText()));
			model.setFilterCriteria(filterCriteria.getText());
			model.setActiveInd(active.isSelected());
		}
		return validateForm();
	}

	private JPanel getGroupEditPanel(){
		if (groupEditPanel == null){
			FormLayout formLayout = new FormLayout("3dlu, pref, 5dlu, fill:200dlu:grow, 3dlu");
			PanelBuilder pb = new PanelBuilder(formLayout);
			pb.appendRelatedComponentsGapRow();
			pb.appendRow("pref");
			CellConstraints cc = new CellConstraints();
			CellConstraints cl= new CellConstraints();
			pb.add(new JLabel("Match"), cl.xy(2,2),matches, cc.xy(4,2));
			pb.appendRelatedComponentsGapRow();
			pb.appendRow("pref");
			pb.add(new JLabel("Match Group"), cl.xy(2,4), groupId, cc.xy(4,4));
			pb.appendRelatedComponentsGapRow();
			pb.appendRow("pref");
			pb.add(new JLabel("Description"), cl.xy(2,6),description , cc.xy(4,6));
			pb.appendRelatedComponentsGapRow();
			pb.appendRow("pref");
			pb.add(new JLabel("Match Percent"), cl.xy(2,8),matchPercent, cc.xy(4,8));
			pb.appendRelatedComponentsGapRow();
			pb.appendRow("pref");
			pb.add(new JLabel("Filter Criteria"), cl.xy(2,10),filterCriteria, cc.xy(4,10));
			pb.appendRelatedComponentsGapRow();
			pb.appendRow("pref");
			pb.add(new JLabel("Is Active"), cl.xy(2,12),active, cc.xy(4,12));
			pb.appendRelatedComponentsGapRow();
			pb.appendRow("pref");
			pb.add(new JLabel("Last Updated"), cl.xy(2,14),lastUpdateDate, cc.xy(4,14));
			pb.appendRelatedComponentsGapRow();
			pb.appendRow("pref");
			pb.add(new JLabel("Last Updated By"), cl.xy(2,16),lastUpdateUser, cc.xy(4,16));
			pb.appendRelatedComponentsGapRow();
			pb.appendRow("pref");
			pb.add(new JLabel("Last Updated OS User"), cl.xy(2,18),lastUpdateOSUser, cc.xy(4,18));
			groupEditPanel = pb.getPanel();
		}
		return groupEditPanel;
	}
	
	/**
	 * This method initializes jSplitPane	
	 * 	
	 * @return javax.swing.JSplitPane	
	 */
	private JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			jSplitPane.setBottomComponent(getMatchCriteriaScrollPane());
			jSplitPane.setTopComponent(getGroupEditPanel());
			
		}
		return jSplitPane;
	}

	/**
	 * This method initializes matchCriteriaScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getMatchCriteriaScrollPane() {
		if (matchCriteriaScrollPane == null) {
			matchCriteriaScrollPane = new JScrollPane(getMatchCriteriaTable());
		}
		return matchCriteriaScrollPane;
	}

	/**
	 * This method initializes matchCriteriaTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getMatchCriteriaTable() {
		if (matchCriteriaTable == null) {
			matchCriteriaTable = new JTable();
			matchCriteriaTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			setModel(model);
			
			matchCriteriaTable.setDefaultRenderer(Boolean.class,new CheckBoxRenderer());
			
		}
		return matchCriteriaTable;
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
	class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {

		  CheckBoxRenderer() {
		    setHorizontalAlignment(JLabel.CENTER);
		  }

		  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		    if (isSelected) {
		      setForeground(table.getSelectionForeground());
		      setBackground(table.getSelectionBackground());
		    } else {
		      setForeground(table.getForeground());
		      setBackground(table.getBackground());
		    }
		    setSelected((value != null && ((Boolean) value).booleanValue()));
		    return this;
		  }
		}

		Action action = new AbstractAction("CheckBox") {
		    
		    public void actionPerformed(ActionEvent evt) {
		        
		        JCheckBox cb = (JCheckBox)evt.getSource();

		        boolean isSel = cb.isSelected();
		        if (isSel) {
		            System.out.println("true");
		        } else {
		            System.out.println("false");
		        }
		    }
		};

} 
