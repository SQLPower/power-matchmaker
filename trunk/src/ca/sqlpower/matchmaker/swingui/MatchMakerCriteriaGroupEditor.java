package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

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

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;
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
    private JTextField filterCriteria;
    private JCheckBox active;

    private JButton newMatchCriterion;
    private JButton deleteMatchCriterion;
    private JButton copyMatchCriterion;
    private JButton pasteMatchCriterion;
	private JButton saveMatchCriterion;
	private JButton cancelMatchCriterion;

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
			group.setFilter(filterCriteria.getText());
			if ( !match.getMatchCriteriaGroups().contains(group)) {
				match.addMatchCriteriaGroup(group);
			}
			
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
					if ( criteria.getColumn() != null ) {
						criteria.setName(criteria.getColumn().getName());
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
        filterCriteria = new JTextField();
        filterCriteria.setName("Filter");
        active = new JCheckBox();
        active.setSelected(true);

        newMatchCriterion = new JButton(newCriteria);
        newMatchCriterion.setName("New button for "+group.getName());
        deleteMatchCriterion = new JButton(deleteCriteria);
        copyMatchCriterion = new JButton("Copy");
        pasteMatchCriterion = new JButton("Paste");
        saveMatchCriterion = new JButton(save);
        saveMatchCriterion.setName("Save button for "+group.getName());
        cancelMatchCriterion = new JButton("Cancel");
        

		
		// group header
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
		bbb.addGridded(copyMatchCriterion);
		bbb.addUnrelatedGap();
		bbb.addGridded(pasteMatchCriterion);
		bbb.addUnrelatedGap();
		bbb.addGridded(saveMatchCriterion);
		bbb.addUnrelatedGap();
		bbb.addGridded(cancelMatchCriterion);
		bbb.addUnrelatedGap();
		
		// TODO: Add Copy and Paste buttons
		//bbb.addGridded(copyMatchCriterion);
		//bbb.addGridded(pasteMatchCriterion);
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
	 * @param model the new MatchGroup to edit.
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
        filterCriteria.setText(group.getFilter());
        if ( group.getActive() != null ) {
        	active.setSelected(!group.getActive());
        }

        newCriteria.setEnabled(false);
        if ( match.getSourceTable() != null ) {
        	SQLTable sourceTable = match.getSourceTable().getTable();
        	if ( sourceTable != null ) {
        		newCriteria.setEnabled(true);
        		
        		int columnColumn = MatchCriteriaColumn.getIndex(MatchCriteriaColumn.COLUMN);
        		matchCriteriaTable.getColumnModel().getColumn(columnColumn).setCellEditor(
        				new DefaultCellEditor(new JComboBox(
                                new ColumnComboBoxModel(sourceTable,group))));
        	}
        }
        
        
        
        
        Validator v1 = new MatchGroupNameValidator();
        handler.addValidateObject(groupId,v1);
        
        Validator v2 = new MatchGroupPctValidator();
        handler.addValidateObject(matchPercent,v2);
        
        Validator v3 = new AlwaysOKValidator();
        handler.addValidateObject(description,v3);
        handler.addValidateObject(filterCriteria,v3);
        
        Validator v4 = new CriteriaTableValidator(matchCriteriaTable);
        handler.addValidateObject(matchCriteriaTable,v4);

        
        
        
        
        
/*            int translateColumn = MatchCriteriaColumn
                    .getIndex(MatchCriteriaColumn.TRANSLATE_GROUP);
            matchCriteriaTable.getColumnModel().getColumn(translateColumn)
                    .setCellEditor(
                            new DefaultCellEditor(new JComboBox(
                                    new TranslationComboBoxModel(swingSession))));
            int columnColumn = MatchCriteriaColumn
                    .getIndex(MatchCriteriaColumn.COLUMN);
            PlMatch plMatch = model.getPlMatch();
            if (plMatch != null && plMatch.getMatchTable() != null) {
                SQLTable t = swingSession.getDatabase().getTableByName(
                        plMatch.getTableCatalog(), plMatch.getTableOwner(),
                        plMatch.getMatchTable());
                matchCriteriaTable.getColumnModel().getColumn(columnColumn)
                        .setCellEditor(
                                new DefaultCellEditor(new JComboBox(
                                        new ColumnComboBoxModel(t, model))));
            }
        }*/
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
System.out.println("table validator");

			List<String> columnNames = new ArrayList<String>();
			for ( int i=0; i<model.getRowCount(); i++ ) {
				MatchMakerCriteria c = model.getRow(i);
System.out.println("c="+c.getName()+"  column="+(c.getColumn()==null));				
				if ( c.getColumn() == null || 
						c.getColumn().getName() == null || 
						c.getColumn().getName().length() == 0 ) {
System.out.println("returning :"+Status.FAIL.name());
					
					return ValidateResult.createValidateResult(Status.FAIL,
							"column name can not be null"); 
				}
System.out.println("column name=["+c.getColumn().getName()+"]");				
				if (columnNames.contains(c.getColumn().getName())) {
					return ValidateResult.createValidateResult(Status.FAIL,
							"column name can not be duplicated");
				}
				columnNames.add(c.getColumn().getName());
			}
			return ValidateResult.createValidateResult(Status.OK, "");
		}
		
	}
}
