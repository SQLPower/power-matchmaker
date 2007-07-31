package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerCriteria;
import ca.sqlpower.matchmaker.MatchMakerCriteriaGroup;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;
import ca.sqlpower.matchmaker.util.EditableJTable;
import ca.sqlpower.swingui.SPSUtils;
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

public class MatchMakerCriteriaGroupEditor implements EditorPane {

	private static final Logger logger = Logger.getLogger(MatchMakerCriteriaGroupEditor.class);

    private final MatchMakerSwingSession swingSession;
    private MatchMakerCriteriaGroup group;
    private Match match;

    private JPanel panel;

    StatusComponent status = new StatusComponent();
    private FormValidationHandler handler;

    private JSplitPane jSplitPane;
	private final JTable matchCriteriaTable;
	private MatchCriteriaTableModel matchCriteriaTableModel;
	private JPanel groupEditPanel;
    private JTextField groupId;
    private JLabel matches;
    private JTextArea description;
    private JTextField matchPercent;
    private FilterComponents filterPanel;
    private JCheckBox active;
    private JComboBox colourPicker;

    private JButton newMatchCriterion;
    private JButton deleteMatchCriterion;
	private JButton saveMatchCriterion;

	/**
	 * This is the default constructor
	 * the validation will be triggered before the end of this constructor
	 */
	public MatchMakerCriteriaGroupEditor(MatchMakerSwingSession swingSession,
			Match match,
			MatchMakerCriteriaGroup group) {
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
        
		/* Now trigger form validation so the validation status starts up correctly,
         * then reset the validation handler so we don't think there are unsaved changes
         * to start with.
		 */
		matchCriteriaTableModel.fireTableChanged(new TableModelEvent(matchCriteriaTableModel));
        handler.resetHasValidated();
	}

	private class MatchGroupNameValidator implements Validator {
		private static final int MAX_GROUP_NAME_CHAR = 30;
        public ValidateResult validate(Object contents) {
			String value = (String)contents;
			if ( value == null || value.length() == 0 ) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Match group name is required");
			} else if ( !value.equals(group.getName()) &&
					match.getMatchCriteriaGroupByName(groupId.getText()) != null ) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Match group name is invalid or already exists.");
			} else if (value.length() > MAX_GROUP_NAME_CHAR){
			    return ValidateResult.createValidateResult(Status.FAIL, 
                        "Match group name cannot be more than " + MAX_GROUP_NAME_CHAR + " characters long");
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
		public void actionPerformed(final ActionEvent e) {
		    doSave();
		}
	};

	private Action newCriteria = new AbstractAction("New Criteria") {
		public void actionPerformed(ActionEvent arg0) {
			MatchMakerCriteria criteria = new MatchMakerCriteria();
			group.addChild(criteria);
			criteria.addMatchMakerListener(new MatchMakerListener<MatchMakerCriteria,MatchMakerObject>() {

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
	        
	        /* Note, this is a temporary workaround.  Deleting a criteria set will have
	         * the side effect of saving all other sets in the group.
	         * 
	         * We'd prefer to be able to do one of the following (in order of preference):
	         * 
	         * 1. Make it so when you delete a criteria row and don't save, the
	         *    row reappears next time you come to this group editor
	         * 2. Make the delete permanently and immediately delete the one criteria set
	         *    we just removed, but not save the group or any other criteria
	         *    (this would require having a working MatchMakerCriteriaDAO, which we don't)
	         */
            
            //For now we catch the exception as an additional workaround, we add the child back 
            //and give the user a message
	        try {
	            group.removeChild(c);
	            swingSession.save(match);
	        } catch (Exception ex){
	            group.addChild(c);     
                SPSUtils.showExceptionDialogNoReport("Delete operation failed!", ex);
	        }

	    }
	};

	/**
	 * Creates the GUI components and lays them out.
	 */
	private void buildUI() {
        groupId = new JTextField();
        groupId.setName("Group ID");
        matches = new JLabel();

        description = new JTextArea();
        matchPercent = new JTextField();
        matchPercent.setColumns(3);
        matchPercent.setName("Percent");
        filterPanel = new FilterComponents(swingSession.getFrame());
        filterPanel.getFilterTextArea().setName("Filter");
        active = new JCheckBox();
        active.setSelected(true);
        colourPicker = new JComboBox(ColorScheme.BREWER_SET19);
        colourPicker.setRenderer(new ColorCellRenderer());
        
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
		pb.appendRow("40dlu");
		pb.add(new JLabel("Description"), cl.xy(2,6),new JScrollPane(description) , cc.xy(4,6,"f,f"));
        pb.add(new JLabel("Colour"), cl.xy(6,6), colourPicker, cc.xy(8,6));
		pb.appendRelatedComponentsGapRow();
		pb.appendRow("40dlu");
		pb.add(new JLabel("Filter Criteria"), cl.xy(2,8));
        pb.add(new JScrollPane(filterPanel.getFilterTextArea()), cc.xy(4,8, "f,f"));
        pb.add(filterPanel.getEditButton(), cc.xy(6,8));
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
	 */
	private void setDefaultSelection(MatchMakerCriteriaGroup group,
			Match match ) {

		matches.setText(match.getName());
		groupId.setText(group.getName());
        description.setText(group.getDesc());

        if ( group.getMatchPercent() != null ) {
        	matchPercent.setText(group.getMatchPercent().toString());
        }
        filterPanel.getFilterTextArea().setText(group.getFilter());
       	active.setSelected(group.getActive());

        colourPicker.setSelectedItem(group.getColour());
        
        SQLTable sourceTable;
        newCriteria.setEnabled(false);
        if ( match.getSourceTable() != null ) {
        	sourceTable = match.getSourceTable();
        	if ( sourceTable != null ) {
        	    logger.debug("sourceTable isn't null.  Creating filterPanel, column chooser, and translation chooser.");

                newCriteria.setEnabled(true);

                filterPanel.setTable(sourceTable);

        		setupColumnEditors(group, sourceTable);
        	} else {
                logger.debug("sourceTable is null.  not setting up editors.");
            }
        }

        Validator v1 = new MatchGroupNameValidator();
        handler.addValidateObject(groupId,v1);

        Validator v2 = new MatchGroupPctValidator();
        handler.addValidateObject(matchPercent,v2);

        Validator v3 = new AlwaysOKValidator();
        handler.addValidateObject(description,v3);
        handler.addValidateObject(filterPanel.getFilterTextArea(),v3);
        handler.addValidateObject(colourPicker, v3);
        handler.addValidateObject(active, v3);
        
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


	/**
     * Sets up all the renderers and listeners that the match criteria table needs
     * in order to display the GUI properly (ie a ColumnComboBoxModel for Translate
     * Words dropdown)
     * 
     * @param group the MatchCriteriaGroup the editors will be setting up for
     * @param sourceTable the source table of the match
	 */
    private void setupColumnEditors(MatchMakerCriteriaGroup group, SQLTable sourceTable) {
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
        
        matchCriteriaTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }



	public JComponent getPanel() {
		return panel;
	}

	private class CriteriaTableValidator implements Validator {

		private MatchCriteriaTableModel model;
		public CriteriaTableValidator(JTable table) {
			this.model = (MatchCriteriaTableModel) table.getModel();
		}
		public ValidateResult validate(Object contents) {

			List<String> columnNames = new ArrayList<String>();
			for ( int i=0; i<model.getRowCount(); i++ ) {
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

		Pattern pattern = Pattern.compile("\\d+");
		public ValidateResult validate(Object contents) {
			int colIndex = ((MatchCriteriaTableModel) table.getModel()).
							getIndexOfClass(translate_group_name);

			//If it does not exist, the columns have not been setup yet
			//therefore it is ok
			if (colIndex == -1) {
				return ValidateResult.createValidateResult(Status.OK, "");
			}
			//Iterates through the rows to ensure that each column
			//in that row are either empty or valid number inputs
			for (int i = 0; i < table.getRowCount(); i++){
				if (table.getValueAt(i, colIndex) instanceof Long ||
						table.getValueAt(i, colIndex) instanceof Integer)
					continue;

				String value = (String) table.getValueAt(i, colIndex);
				if (value == null || value.trim().length()==0)
					continue;
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

    /**
     * Renders a rectangle of colour in a list cell.  The colour is determined
     * by the list item value, which must be of type java.awt.Color.
     */
    private class ColorCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, "", index, isSelected, cellHasFocus);
            setBackground((Color) value);
            setOpaque(true);
            setPreferredSize(new Dimension(50, 50));
            return this;
        }
    }
    
	public boolean doSave() {
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
            return false;
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
                return false;
            }
            group.setName(groupId.getText());
        }
        

        
        //If the field is empty, we need to store it as null in order for
        //it to load back appropiately.  There is no need to worry about invalid cases 
        //here since the Validation handles them and won't allow the user to save
        //if there's a failure.
        if (matchPercent.getText().trim().length() == 0) {
            group.setMatchPercent(null);
        } else {
            int pct = Integer.parseInt(matchPercent.getText());
            group.setMatchPercent((short)pct);
        }
        group.setDesc(description.getText());
        group.setFilter(filterPanel.getFilterTextArea().getText());
        if ( !match.getMatchCriteriaGroups().contains(group)) {
            match.addMatchCriteriaGroup(group);
        }
        
        group.setActive(active.isSelected());
        
        group.setColour((Color) colourPicker.getSelectedItem());
        
        MatchMakerDAO<Match> dao = swingSession.getDAO(Match.class);
        dao.save(match);        

        //This code is called since saving the match, under some circumstances, fires structure change
        //and that causes all the renderers and cell editors to be set to null.  Therefore it is 
        //required to re-hook up all the editors and renderers as a workaround.
        setupColumnEditors(group, match.getSourceTable());

        handler.resetHasValidated();
		return true;
	}

	public boolean hasUnsavedChanges() {
		return handler.hasPerformedValidation();
	}
}