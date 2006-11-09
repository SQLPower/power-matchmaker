package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ArchitectPanel;
import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;
import ca.sqlpower.matchmaker.hibernate.home.PlMatchGroupHome;
import ca.sqlpower.matchmaker.swingui.action.CopyMatchCriteria;
import ca.sqlpower.matchmaker.swingui.action.DeleteMatchCriteria;
import ca.sqlpower.matchmaker.swingui.action.NewMatchCriteria;
import ca.sqlpower.matchmaker.swingui.action.PasteMatchCriteria;
import ca.sqlpower.matchmaker.util.EditableJTable;
import ca.sqlpower.matchmaker.util.HibernateUtil;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


public class PlMatchGroupPanel extends JPanel implements ArchitectPanel {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(PlMatchGroupPanel.class);
	
    private final MatchMakerSwingSession swingSession;
	private PlMatchGroup model;

    private JSplitPane jSplitPane;
	private JTable matchCriteriaTable;
	private JPanel groupEditPanel;
    private JTextField groupId;
    private JLabel matches;
    private JTextField description;
    private JTextField matchPercent;
    private JTextField filterCriteria;
    private JCheckBox active;
    private JLabel lastUpdateDate;
    private JLabel lastUpdateUser;
    private JLabel lastUpdateOSUser;
    private Color textBackground;

    private JButton newMatchCriterion;
    private JButton deleteMatchCriterion;
    private JButton copyMatchCriterion;
    private JButton pasteMatchCriterion;

	/**
	 * This is the default constructor
	 * @throws ArchitectException 
	 */
	public PlMatchGroupPanel(MatchMakerSwingSession swingSession, PlMatchGroup model) throws ArchitectException {
		super();
        this.swingSession = swingSession;
		PlMatchGroupPanelImpl(model);
	}
	
	public void PlMatchGroupPanelImpl(PlMatchGroup model) throws ArchitectException{
		this.model = model;
		initialize();
	}

	/**
	 * Creates the GUI components and lays them out.
	 */
	private void initialize() throws ArchitectException {
		DocumentListener listener = new DocumentListener() {
        
        	public void changedUpdate(DocumentEvent e) {
        		validateForm();
        	}
        
        	public void insertUpdate(DocumentEvent e) {
        		validateForm();
        	}
        
        	public void removeUpdate(DocumentEvent e) {
        		validateForm();
        	}		
        };
        
        FocusListener fl = new FocusListener() {
        
        	public void focusGained(FocusEvent e) {
                // don't care
        	}
        
        	public void focusLost(FocusEvent e) {
        		saveMatches();
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
        
        newMatchCriterion = new JButton();
        deleteMatchCriterion = new JButton();
        copyMatchCriterion = new JButton();
        pasteMatchCriterion = new JButton();
        lastUpdateDate = new JLabel();
        lastUpdateUser = new JLabel();
        lastUpdateOSUser= new JLabel();
        matchPercent.addFocusListener(fl);
        matchPercent.getDocument().addDocumentListener(listener);
        groupId.addFocusListener(fl);
        groupId.getDocument().addDocumentListener(listener);
        description.addFocusListener(fl);
        description.getDocument().addDocumentListener(listener);
        filterCriteria.addFocusListener(fl);
        filterCriteria.getDocument().addDocumentListener(listener);
		textBackground = description.getBackground();
        
        jSplitPane = new JSplitPane();
        jSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        jSplitPane.setBottomComponent(new JScrollPane(getMatchCriteriaTable()));
        jSplitPane.setTopComponent(createGroupEditPanel());

		add(jSplitPane, BorderLayout.CENTER);
		
	}

    /**
     * Initializes matchCriteriaTable, or returns the existing one if it's already created. 
     */
    private JTable getMatchCriteriaTable() throws ArchitectException {
        if (matchCriteriaTable == null) {
            matchCriteriaTable = new EditableJTable();
            matchCriteriaTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            setModel(model);
            matchCriteriaTable.setDefaultRenderer(Boolean.class,new CheckBoxRenderer());

            FocusListener tfl = new FocusListener(){

                public void focusGained(FocusEvent e) {
                    // don't care
                }

                public void focusLost(FocusEvent e) {
                    Transaction tx = HibernateUtil.primarySession().beginTransaction();
                    HibernateUtil.primarySession().flush();
                    tx.commit();
                }
                
            };
            matchCriteriaTable.addFocusListener(tfl);
        
        }
        return matchCriteriaTable;
    }

    /**
     * Returns the match group this component is editing now.
     */
	public PlMatchGroup getModel() {
		return model;
	}

	/**
	 * Switches this component to edit a different match group.
	 * 
	 * @param model the new MatchGroup to edit.
	 */
	public void setModel(PlMatchGroup model) throws ArchitectException {
        this.model = model;
        if (model != null) {
            matchCriteriaTable.setModel(new MatchCriteriaTableModel(model));
            loadMatches();
            int translateColumn = MatchCriteriaColumn
                    .getIndex(MatchCriteriaColumn.TRANSLATE_GROUP);
            matchCriteriaTable.getColumnModel().getColumn(translateColumn)
                    .setCellEditor(
                            new DefaultCellEditor(new JComboBox(
                                    new TranslationComboBoxModel(swingSession))));
            int columnColumn = MatchCriteriaColumn
                    .getIndex(MatchCriteriaColumn.COLUMN);
            PlMatch plMatch = model.getPlMatch();
            if (plMatch != null && plMatch.getMatchTable() != null) {
                SQLTable t = swingSession.getPlRepositoryDatabase().getTableByName(
                        plMatch.getTableCatalog(), plMatch.getTableOwner(),
                        plMatch.getMatchTable());
                matchCriteriaTable.getColumnModel().getColumn(columnColumn)
                        .setCellEditor(
                                new DefaultCellEditor(new JComboBox(
                                        new ColumnComboBoxModel(t, model))));
            }
        }
    }

	private void loadMatches() throws ArchitectException {
        PlMatch plMatch = model.getPlMatch();
        if (plMatch != null && plMatch.getMatchTable() != null) {
            SQLTable t = swingSession.getPlRepositoryDatabase().getTableByName(
                    plMatch.getTableCatalog(), plMatch.getTableOwner(),
                    plMatch.getMatchTable());
            newMatchCriterion.setAction(new NewMatchCriteria(model, t));
            newMatchCriterion.setToolTipText(null);
            newMatchCriterion.setEnabled(true);
        } else {
            newMatchCriterion.setAction(new AbstractAction("New Criterion") {

                public void actionPerformed(ActionEvent e) {
                    // TODO
                    JOptionPane.showMessageDialog(PlMatchGroupPanel.this, "Not implemented yet");
                }

            });
            newMatchCriterion.setEnabled(false);
            newMatchCriterion.setToolTipText(
                    "You need to have selected a table you can access to add match criteria");
        }
        deleteMatchCriterion.setAction(new DeleteMatchCriteria(model,
                getMatchCriteriaTable()));
        copyMatchCriterion.setAction(new CopyMatchCriteria(model,
                getMatchCriteriaTable().getSelectedRows()));
        pasteMatchCriterion.setAction(new PasteMatchCriteria(model));
        groupId.setText(model.getGroupId());
        description.setText(model.getDescription());
        matchPercent.setText(model.getMatchPercent().toString());
        filterCriteria.setText(model.getFilterCriteria());
        active.setSelected(!model.isActiveInd());

        Date updated = model.getLastUpdateDate();
        lastUpdateDate.setText(updated == null ? "N/A" : updated.toString());
        lastUpdateUser.setText(model.getLastUpdateUser() == null ? "N/A"
                : model.getLastUpdateUser());
        lastUpdateOSUser.setText(model.getLastUpdateOsUser() == null ? "N/A"
                : model.getLastUpdateOsUser());
    }
	
	private boolean validateForm() {
		if (model == null){
			logger.debug("Trying to validate unloaded form");
			return false;
		}
		Boolean valid = true;
		if (groupId.getText().equals("")){
			groupId.setBackground(Color.red);
			valid=false;
		} else {
			groupId.setBackground(textBackground);
		}
		if (model.getPlMatch() != null) {
			for( PlMatchGroup g : model.getPlMatch().getPlMatchGroups()) {
				
				if (g.getGroupId() != null && (g.getGroupId().equals( groupId.getText() )&& g != model)){
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
	private boolean saveMatches() {
		if ( validateForm() && model.getPlMatch() != null && model.getPlMatch().getMatchId() != null ){
            model.setGroupId(groupId.getText());
            // if this hasn't been added yet add it.
            if (!model.getPlMatch().getChildren().contains(model)) 
                model.getPlMatch().addPlMatchGroups(model);
			model.setDescription(description.getText());
			model.setMatchPercent(Short.parseShort(matchPercent.getText()));
			model.setFilterCriteria(filterCriteria.getText());
			model.setActiveInd(!active.isSelected());
	
			try {
			
				PlMatchGroupHome home = new PlMatchGroupHome();
				home.saveOrUpdate(model);
				home.flush();

			} catch (Exception e) {

				e.printStackTrace();
			}
		}
		return validateForm();
	}

	private JPanel createGroupEditPanel() throws ArchitectException{
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
			pb.appendRelatedComponentsGapRow();
			pb.appendRow("pref");
			groupEditPanel = pb.getPanel();
			ButtonBarBuilder bbb = new ButtonBarBuilder();
			
			bbb.addGridded(newMatchCriterion);
			bbb.addUnrelatedGap();
			bbb.addGridded(deleteMatchCriterion);
			bbb.addUnrelatedGap();
			// TODO Add Copy and Paste buttons
			//bbb.addGridded(copyMatchCriterion);
			//bbb.addGridded(pasteMatchCriterion);
			bbb.addGlue();
			pb.add(bbb.getPanel(),cl.xyw(2, 10, 7));
			
		}
		return groupEditPanel;
	}

	public boolean applyChanges() {
		return saveMatches();
	}

	public void discardChanges() {

	}

	public JComponent getPanel() {
		return this;
	}

    /**
     * A renderer for presenting a JCheckBox in a JTable cell.  It makes sure the check box
     * looks right (correct foreground and background colour depending on the cell's selection
     * state) in the table.
     */
	private class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {

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
} 
