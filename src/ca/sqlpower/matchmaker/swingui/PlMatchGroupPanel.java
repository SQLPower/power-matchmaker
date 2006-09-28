package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellRenderer;

import org.hibernate.Transaction;

import ca.sqlpower.architect.swingui.ArchitectPanel;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.matchmaker.hibernate.PlMatchCriteria;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroupId;
import ca.sqlpower.matchmaker.util.HibernateUtil;

import com.jgoodies.forms.builder.ButtonBarBuilder;
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
	JLabel matches;
	JTextField description;
	JTextField matchPercent;
	JTextField filterCriteria;
	JCheckBox active;
	JLabel	lastUpdateDate;
	JLabel	lastUpdateUser;
	JLabel	lastUpdateOSUser;
	Color textBackground;


	private boolean loading = false;


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
		// load the new model
		clear();
		textBackground = description.getBackground();
		this.add(getJSplitPane(), BorderLayout.CENTER);
		
		ButtonBarBuilder bb = new ButtonBarBuilder();
		
		
	}

	public void clear() {
		DocumentListener listener = new DocumentListener(){

			public void changedUpdate(DocumentEvent e) {
				saveMatches(model);
				
			}

			public void insertUpdate(DocumentEvent e) {
				saveMatches(model);
				
			}

			public void removeUpdate(DocumentEvent e) {
				saveMatches(model);
				
			}
			

			
		};
		this.setLayout(new BorderLayout());
		groupId = new JTextField();
		matches = new JLabel();

		description = new JTextField();
		matchPercent = new JTextField();
		matchPercent.setColumns(3);
		filterCriteria = new JTextField();
		active = new JCheckBox();
		active.setSelected(true);

		
		lastUpdateDate = new JLabel();
		lastUpdateUser = new JLabel();
		lastUpdateOSUser= new JLabel();
		matchPercent.getDocument().addDocumentListener(listener);
		groupId.getDocument().addDocumentListener(listener);
		description.getDocument().addDocumentListener(listener);
		filterCriteria.getDocument().addDocumentListener(listener);
	}

	public PlMatchGroup getModel() {
		return model;
	}

	public void setModel(PlMatchGroup model) {
		this.model = model;
		if(model != null) {
			matchCriteriaTable.setModel(new MatchCriteriaTableModel(model));
			loadMatches(model);
			int translateColumn = MatchCriteriaColumn.getIndex(MatchCriteriaColumn.TRANSLATE_GROUP_NAME);
			matchCriteriaTable.getColumnModel().getColumn(translateColumn).setCellEditor(new DefaultCellEditor(new JComboBox(new TranslationComboBoxModel())));
			
		}
	}

	private void loadMatches(PlMatchGroup model) {
		loading = true;
		groupId.setText(model.getId().getGroupId());
		matches.setText(model.getId().getMatchId());

		description.setText(model.getDescription());
		
		matchPercent.setText(model.getMatchPercent().toString());

		filterCriteria.setText(model.getFilterCriteria());

		active.setSelected(!model.isActiveInd());
		Date updated = model.getLastUpdateDate();
		
		lastUpdateDate.setText(updated == null? "N/A" :updated.toString());
		lastUpdateUser.setText(model.getLastUpdateUser() == null? "N/A" :model.getLastUpdateUser());
		lastUpdateOSUser.setText(model.getLastUpdateOsUser()== null?"N/A":model.getLastUpdateOsUser());
		loading=false;
	}
	
	private boolean validateForm(){
		Boolean valid = true;
		if (groupId.getText().equals("")){
			groupId.setBackground(Color.red);
			valid=false;
		} else {
			groupId.setBackground(textBackground);
		}
		if (model.getPlMatch() != null) {
			for( PlMatchGroup g : model.getPlMatch().getPlMatchGroups()) {
				if (g.getId().getGroupId() == groupId.getText() && g != model){
					groupId.setBackground(Color.red);
					valid=false;
				}
			}
		}
		try {
			Short d = Short.parseShort(matchPercent.getText());
			
			if (d >= 0 && d <= 100){
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
		if ( validateForm() && !loading){
			// load the new model
			PlMatchGroup saveGroup;
			if ( !groupId.getText().equals(model.getId().getGroupId())) {
	
				saveGroup = new PlMatchGroup(new PlMatchGroupId(model.getId().getMatchId(),groupId.getText()),model.getPlMatch(),model);
			} else {
				saveGroup = model;
			}
			saveGroup.setDescription(description.getText());
			saveGroup.setMatchPercent(Short.parseShort(matchPercent.getText()));
			saveGroup.setFilterCriteria(filterCriteria.getText());
			saveGroup.setActiveInd(!active.isSelected());
	
			try {
				System.out.println("Saving "+model);
				
				Transaction tx = HibernateUtil.primarySession().beginTransaction();
				if (!model.equals(saveGroup)) {
					HibernateUtil.primarySession().delete(model);
					HibernateUtil.primarySession().persist(saveGroup);
				} else {
					HibernateUtil.primarySession().flush();
				}
				tx.commit();
			
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return validateForm();
	}

	private JPanel getGroupEditPanel(){
		if (groupEditPanel == null){
			FormLayout formLayout = new FormLayout("3dlu, pref, 5dlu, fill:pref:grow, 10dlu, pref,5dlu,pref,3dlu");
			PanelBuilder pb = new PanelBuilder(formLayout);
			pb.appendRelatedComponentsGapRow();
			pb.appendRow("pref");
			CellConstraints cc = new CellConstraints();
			CellConstraints cl= new CellConstraints();
			pb.add(new JLabel("Match"), cl.xy(2,2),matches, cc.xy(4,2));
			pb.add(new JLabel("Active"), cl.xy(6,2),active, cc.xy(8,2));
			pb.appendRelatedComponentsGapRow();
			pb.appendRow("pref");
			pb.add(new JLabel("Match Group"), cl.xy(2,4), groupId, cc.xy(4,4));
			pb.add(new JLabel("Match Percent"), cl.xy(6,4),matchPercent, cc.xy(8,4));
			pb.appendRelatedComponentsGapRow();
			pb.appendRow("pref");
			pb.add(new JLabel("Description"), cl.xy(2,6),description , cc.xyw(4,6,5));
			pb.appendRelatedComponentsGapRow();
			pb.appendRow("pref");
			pb.add(new JLabel("Filter Criteria"), cl.xy(2,8),filterCriteria, cc.xyw(4,8,5));

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
			matchCriteriaTable.putClientProperty("terminateEditOnFocusLost", true);
		
		}
		return matchCriteriaTable;
	}

	public boolean applyChanges() {
		return saveMatches(model);
	}

	public void discardChanges() {

	}

	public JComponent getPanel() {
		return this;
	}
	private class EditMatchCriteriaAction extends AbstractAction {
		private EditMatchCriteriaAction(String name) {
			super(name);
		}

		public EditMatchCriteriaAction(PlMatchCriteria criteria, Window w) {
			
			this("Edit Match Criterion");
			window = w;
			this.criteria = criteria;
		}

		private PlMatchCriteria criteria;
		private Window window;
	

		public void actionPerformed(ActionEvent e) {
			JDialog d = ArchitectPanelBuilder.createArchitectPanelDialog(new PlMatchCriteriaPanel(criteria), window, "Edit Match Criterion", "Save Match Criterion");
			d.setVisible(true);
			
		}
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
