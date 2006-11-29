package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
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
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerCriteria;
import ca.sqlpower.matchmaker.MatchMakerCriteriaGroup;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;
import ca.sqlpower.matchmaker.util.EditableJTable;
import ca.sqlpower.validation.AlwaysOKValidator;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


public class MatchMakerCriteriaGroupEditor {

	private static final Logger logger = Logger.getLogger(MatchMakerCriteriaGroupEditor.class);

    private final MatchMakerSwingSession swingSession;
    private MatchMakerCriteriaGroup group;
    private Match match;
    
    private JPanel panel;
    
    StatusComponent status = new StatusComponent();
    private FormValidationHandler handler;
    
    private JSplitPane jSplitPane;
	private JTable matchCriteriaTable;
	private MatchCriteriaTableModel matchCriteriaTableModel;
	private JPanel groupEditPanel;
    private JTextField groupId;
    private JLabel matches;
    private JTextField description;
    private JTextField matchPercent;
    private FilterComponentsPanel filterPanel;
    private JCheckBox active;

    private JButton newMatchCriterion;
    private JButton deleteMatchCriterion;
	private JButton saveMatchCriterion;

	/**
	 * This is the default constructor
	 * the validation will be triggered before the end of this constructor
	 * @throws ArchitectException
	 */
	public MatchMakerCriteriaGroupEditor(MatchMakerSwingSession swingSession,
			Match match,
			MatchMakerCriteriaGroup group) throws ArchitectException {
		super();
        this.swingSession = swingSession;
        this.match = match;
		this.group = group;
		
		handler = new FormValidationHandler(status);
		matchCriteriaTableModel = new MatchCriteriaTableModel(group);
		matchCriteriaTable = new EditableJTable(matchCriteriaTableModel);
		buildUI();
		setDefaultSelection(group,match);
		handler.addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				refreshActionStatus();
			}
        });
		save.putValue("mm_name", "save action for "+group.getName()+"@"+System.identityHashCode(group));
		/**
		 * for trigger the form validation
		 */
		matchCriteriaTableModel.fireTableChanged(new TableModelEvent(matchCriteriaTableModel));;
	}

	private class MatchGroupNameValidator implements Validator {
		public ValidateResult validate(Object contents) {
			String value = (String)contents;
			if ( value == null || value.length() == 0 ) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Match group name is required");
			} else if ( !value.equals(group.getName()) &&
					match.getMatchCriteriaGroupByName(groupId.getText()) != null ) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Match group name is invalid or already exists.");
			}
			return ValidateResult.createValidateResult(Status.OK, "");
		}
    }
	
	private class MatchGroupPctValidator implements Validator {
		public ValidateResult validate(Object contents) {
			String value = (String)contents;
			if ( value == null || value.length() == 0 ) {
				return ValidateResult.createValidateResult(Status.WARN,
						"Match group percentage is required");
			} else {
				int pct = -1;
				try {
					pct = Integer.parseInt(value);
				} catch ( NumberFormatException e ) {
					return ValidateResult.createValidateResult(Status.FAIL,
						"Match group percentage is invalid.");
				}
				if ( pct > 100 || pct < 0 ) {
					return ValidateResult.createValidateResult(Status.WARN,
							"Match group percentage range is invalid.");
				}
			}
			return ValidateResult.createValidateResult(Status.OK, "");
		}
    }

	private void refreshActionStatus() {
		ValidateResult worst = handler.getWorstValidationStatus();
		save.setEnabled(true);
		logger.debug("Setting "+save.getValue("mm_name")+" enabled to "+true);
		newCriteria.setEnabled(true);
		if ( worst.getStatus() == Status.FAIL ) {
			save.setEnabled(false);
			logger.debug("Setting "+save.getValue("mm_name")+" enabled to "+false);
			newCriteria.setEnabled(false);
		}
	}
	 
	private Action save = new AbstractAction("Save") {
		public void actionPerformed(ActionEvent e) {
			
			List<String> fail = handler.getFailResults();
	    	List<String> warn = handler.getWarnResults();

	    	if ( fail.size() > 0 ) {
	    		StringBuffer failMessage = new StringBuffer();
	    		for ( String f : fail ) {
	    			failMessage.append(f).append("\n");
	    		}
	    		JOptionPane.showMessageDialog(swingSession.getFrame(),
	    				"You have to fix these errors before saving:\n"+
	    				failMessage.toString(),
	    				"Match group error",
	    				JOptionPane.ERROR_MESSAGE);
	    		return;
	    	} else if ( warn.size() > 0 ) {
	    		StringBuffer warnMessage = new StringBuffer();
	    		for ( String w : warn ) {
	    			warnMessage.append(w).append("\n");
	    		}
	    		JOptionPane.showMessageDialog(swingSession.getFrame(),
	    				"Warning: match group will be saved, " +
	    				"but you may not be able to run it, because of these wanings:\n"+
	    				warnMessage.toString(),
	    				"Match warning",
	    				JOptionPane.INFORMATION_MESSAGE);
	    	}
	    	
			if ( !groupId.getText().equals(group.getName()) ) {
	        	if ( match.getMatchCriteriaGroupByName(groupId.getText()) != null ) {
	        		JOptionPane.showMessageDialog(getPanel(),
	        				"Match group name \""+groupId.getText()+
	        				"\" exist or invalid. The match group can not be saved",
	        				"Match group name invalid",
	        				JOptionPane.ERROR_MESSAGE);
	        		return;
	        	}
	        	group.setName(groupId.getText());
	        }
			if (matchPercent.getText().trim().length() > 0){
				int pct = Integer.parseInt(matchPercent.getText());
				group.setMatchPercent((short)pct);
			}
			group.setDesc(description.getText());
			group.setFilter(filterPanel.getFilterTextArea().getText());
			if ( !match.getMatchCriteriaGroups().contains(group)) {
				match.addMatchCriteriaGroup(group);
			}
			group.setActive(active.isSelected());
			MatchMakerDAO<Match> dao = swingSession.getDAO(Match.class);
	        dao.save(match);
		}
	};
	
	private Action newCriteria = new AbstractAction("New Criteria") {
		public void actionPerformed(ActionEvent arg0) {
			MatchMakerCriteria criteria = new MatchMakerCriteria();
			String newUniqueName = group.createNewUniqueName();
			criteria.setName(newUniqueName);
			group.addChild(criteria);
			criteria.addMatchMakerListener(new MatchMakerListener() {

				public void mmChildrenInserted(MatchMakerEvent evt) {
				}

				public void mmChildrenRemoved(MatchMakerEvent evt) {
				}

				public void mmPropertyChanged(MatchMakerEvent evt) {
					MatchMakerCriteria criteria = (MatchMakerCriteria) evt.getSource();
                    try {
                        if ( criteria.getColumn() != null ) {
                            criteria.setName(criteria.getColumn().getName());
                        }
                    } catch (ArchitectException ex) {
                        ASUtils.showExceptionDialog("Couldn't determine criteria for column", ex);
                    }
				}

				public void mmStructureChanged(MatchMakerEvent evt) {
				}});

		}
	};
	
	private Action deleteCriteria = new AbstractAction("Delete") {
		public void actionPerformed(ActionEvent e) {
			int selectedRow = matchCriteriaTable.getSelectedRow();
			if ( selectedRow == -1 ) return;
			MatchMakerCriteria c = matchCriteriaTableModel.getRow(selectedRow);
			group.removeChild(c);
		}
	};
	
	/**
	 * Creates the GUI components and lays them out.
	 */
	private void buildUI() {
        groupId = new JTextField();
        groupId.setName("Group ID");
        matches = new JLabel();

        description = new JTextField();
        matchPercent = new JTextField();
        matchPercent.setColumns(3);
        matchPercent.setName("Percent");
        filterPanel = new FilterComponentsPanel();
        filterPanel.getFilterTextArea().setName("Filter");
        active = new JCheckBox();
        active.setSelected(true);

        newMatchCriterion = new JButton(newCriteria);
        newMatchCriterion.setName("New button for "+group.getName());
        deleteMatchCriterion = new JButton(deleteCriteria);
        saveMatchCriterion = new JButton(save);
        saveMatchCriterion.setName("Save button for "+group.getName());
		
		// group header
		FormLayout formLayout = new FormLayout("3dlu, pref, 5dlu, fill:pref:grow, 10dlu, pref,5dlu,pref,3dlu");
		PanelBuilder pb = new PanelBuilder(formLayout);
		pb.setDefaultDialogBorder();
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
		pb.add(new JLabel("Filter Criteria"), cl.xy(2,8),filterPanel, cc.xyw(4,8,5));
		pb.appendRelatedComponentsGapRow();
		pb.appendRow("pref");
		groupEditPanel = pb.getPanel();
		ButtonBarBuilder bbb = new ButtonBarBuilder();

		bbb.addGridded(newMatchCriterion);
		bbb.addUnrelatedGap();
		bbb.addGridded(deleteMatchCriterion);
		bbb.addUnrelatedGap();
		bbb.addGridded(saveMatchCriterion);
		bbb.addUnrelatedGap();
		bbb.addGlue();
		pb.add(bbb.getPanel(),cl.xyw(2, 10, 7));

		
		
		// group detail (match criteria)
		matchCriteriaTable.setName("Match Criteria Editor for "+group.getName());
		matchCriteriaTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		matchCriteriaTable.setDefaultRenderer(Boolean.class,new CheckBoxRenderer());

		jSplitPane = new JSplitPane();
        jSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        jSplitPane.setBottomComponent(new JScrollPane(matchCriteriaTable));
        jSplitPane.setTopComponent(groupEditPanel);
        
        panel = new JPanel();        
        panel.setLayout(new BorderLayout());
        panel.add(jSplitPane, BorderLayout.CENTER);
        panel.add(status, BorderLayout.NORTH);
	}



	/**
	 * Switches this component to edit a different match group.
	 *
	 * @param criteria the new MatchGroup to edit.
	 * @throws ArchitectException 
	 */
	private void setDefaultSelection(MatchMakerCriteriaGroup group,
			Match match ) throws ArchitectException {
		
		matches.setText(match.getName());
		groupId.setText(group.getName());
        description.setText(group.getDesc());
        
        if ( group.getMatchPercent() != null ) {
        	matchPercent.setText(group.getMatchPercent().toString());
        }
        filterPanel.getFilterTextArea().setText(group.getFilter());
        if ( group.getActive() != null ) {
        	active.setSelected(group.getActive());
        }

        SQLTable sourceTable;
        newCriteria.setEnabled(false);
        if ( match.getSourceTable() != null ) {
        	sourceTable = match.getSourceTable();
        	if ( sourceTable != null ) {
        		newCriteria.setEnabled(true);

                filterPanel.setTable(sourceTable);
                
        		int columnColumn = MatchCriteriaColumn.getIndex(MatchCriteriaColumn.COLUMN);
        		matchCriteriaTable.getColumnModel().getColumn(columnColumn).setCellEditor(
        				new DefaultCellEditor(new JComboBox(
        						new ColumnComboBoxModel(sourceTable,group))));
        		
        		int colIndex = MatchCriteriaColumn.getIndex(MatchCriteriaColumn.TRANSLATE_GROUP);
            	TableColumn col = matchCriteriaTable.getColumnModel().getColumn(colIndex);
            	final JComboBox translateComboBox = new JComboBox(
				            					new TranslationComboBoxModel(swingSession.getTranslations()));
				col.setCellEditor(new DefaultCellEditor(translateComboBox));
				translateComboBox.setRenderer(new MatchMakerObjectComboBoxCellRenderer());
        	}
        }

        Validator v1 = new MatchGroupNameValidator();
        handler.addValidateObject(groupId,v1);
        
        Validator v2 = new MatchGroupPctValidator();
        handler.addValidateObject(matchPercent,v2);
        
        Validator v3 = new AlwaysOKValidator();
        handler.addValidateObject(description,v3);
        handler.addValidateObject(filterPanel.getFilterTextArea(),v3);
        
        Validator v4 = new CriteriaTableValidator(matchCriteriaTable);
        handler.addValidateObject(matchCriteriaTable,v4);
        
        
        //These three fields are not really needed as the table cells automatically
        //reject if the user enters something that is not a number.  These 
        //validators act as an insurance in case the invalid text does get 
        //bypassed.  
           
        Validator v5 = new NumberValidatorAllowingNull(matchCriteriaTable,
        		MatchCriteriaColumn.FIRST_N_CHAR);
        handler.addValidateObject(matchCriteriaTable,v5);
        
           
        Validator v6 = new NumberValidatorAllowingNull(matchCriteriaTable, 
        		MatchCriteriaColumn.FIRST_N_CHARS_BY_WORD);
        handler.addValidateObject(matchCriteriaTable,v6);
                
        Validator v7 = new NumberValidatorAllowingNull(matchCriteriaTable,
        		MatchCriteriaColumn.MIN_WORDS_IN_COMMON);
        handler.addValidateObject(matchCriteriaTable,v7);
        
    }



	public JComponent getPanel() {
		return panel;
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
	
	private class CriteriaTableValidator implements Validator {

		private MatchCriteriaTableModel model;
		public CriteriaTableValidator(JTable table) {
			this.model = (MatchCriteriaTableModel) table.getModel();
		}
		public ValidateResult validate(Object contents) {

			List<String> columnNames = new ArrayList<String>();
			for ( int i=0; i<model.getRowCount(); i++ ) {
                try {
                    MatchMakerCriteria c = model.getRow(i);
                    if ( c.getColumn() == null || 
                            c.getColumn().getName() == null || 
                            c.getColumn().getName().length() == 0 ) {
                        return ValidateResult.createValidateResult(Status.FAIL,
                        "column name can not be null"); 
                    }
                    if (columnNames.contains(c.getColumn().getName())) {
                        return ValidateResult.createValidateResult(Status.FAIL,
                        "column name can not be duplicated");
                    }
                    columnNames.add(c.getColumn().getName());
                } catch (ArchitectException ex) {
                    ASUtils.showExceptionDialog("Couldn't determine criteria for column", ex);
                }
			}
			return ValidateResult.createValidateResult(Status.OK, "");
		}
		
	}
	
	/**
	 * Unlike the RegEx validator, this validator allows the content
	 * to be null as well
	 */
	private class NumberValidatorAllowingNull implements Validator{
		
		private JTable table;
		private MatchCriteriaColumn translate_group_name;
		
		public NumberValidatorAllowingNull(JTable table, 
				MatchCriteriaColumn translate_group_name){
			this.table = table;
			this.translate_group_name = translate_group_name;			
		}
		
		public ValidateResult validate(Object contents) {
			Pattern pattern = Pattern.compile("\\d+");
			int colIndex = ((MatchCriteriaTableModel)table.getModel()).
							getIndexOfClass(translate_group_name);
			
			//If it does not exist, the columns have not been setup yet
			//therefore it is ok
			if (colIndex == -1){
				return ValidateResult.createValidateResult(Status.OK, "");
			}
			//Iterates through the rows to ensure that each column
			//in that row are either empty or valid number inputs
			for (int i = 0; i < table.getRowCount(); i++){
				if (table.getValueAt(i, colIndex) instanceof Long ||
						table.getValueAt(i, colIndex) instanceof Integer) continue;
				
				String value = (String) table.getValueAt(i, colIndex);
				if ( value == null || value.trim().length()==0) continue;
				if (pattern.matcher(value).matches()){
					continue;
				} else {
					String className =table.getModel().getColumnClass(colIndex)
											.toString();
					return ValidateResult.createValidateResult(Status.FAIL, 
							className + "must be in number form");
				}			
			}
			return ValidateResult.createValidateResult(Status.OK, "");
		}
	}
}