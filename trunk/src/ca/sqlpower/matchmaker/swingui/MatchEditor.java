package ca.sqlpower.matchmaker.swingui;

import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerCriteriaGroup;
import ca.sqlpower.matchmaker.MatchMakerFolder;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;
import ca.sqlpower.matchmaker.swingui.action.CreateResultTableAction;
import ca.sqlpower.matchmaker.util.MatchMakerQFAFactory;
import ca.sqlpower.validation.AlwaysOKValidator;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class MatchEditor implements EditorPane {

	private static final Logger logger = Logger.getLogger(MatchEditor.class);

	private SQLObjectChooser sourceChooser;
	private SQLObjectChooser resultChooser;

	private JPanel panel;

	StatusComponent status = new StatusComponent();
    private JTextField matchId = new JTextField();
    private JComboBox folderComboBox = new JComboBox();
    private JComboBox indexComboBox = new JComboBox();
    private JTextArea desc = new JTextArea();
    private JComboBox matchType = new JComboBox();
    private JTextField resultTableName = new JTextField();
    private JButton viewBuilder;
    private JButton createResultTable;
    private JButton saveMatch;
    private JButton showAuditInfo;
    private JButton runMatch;
    private JButton validationStatus;
    private JButton validateMatch;
    private JButton matchResultVisualizerButton;
    private JButton createIndexButton;
    private FilterComponentsPanel filterPanel;
    private MatchValidation matchValidation;
    private MatchResultVisualizer matchResultVisualizer;
    
    private final MatchMakerSwingSession swingSession;

    /**
     * The match that this editor is editing.  If you want to edit a different match,
     * create a new MatchEditor.
     */
	private final Match match;
	private final PlFolder<Match> folder;
	private FormValidationHandler handler;

	/**
	 * Construct a MatchEditor; for a match that is not new, we create a backup for it,
	 * and give it the name of the old one, when we save it, we will remove
	 * the backup from the folder, and insert the new one.
	 * @param swingSession  -- a MatchMakerSession
	 * @param match			-- a Match Object to be edited
	 * @param folder		-- where the match is
	 * @param newMatch		-- a flag indicates it's a new match or not
	 * @throws HeadlessException
	 * @throws ArchitectException
	 */
    public MatchEditor(MatchMakerSwingSession swingSession, Match match,
    		PlFolder<Match> folder) throws HeadlessException, ArchitectException {
    	super();
        this.swingSession = swingSession;
        if (match == null)
        	throw new NullPointerException("You can't edit a null plmatch");
        this.match = match;
        this.folder = folder;
        handler = new FormValidationHandler(status);
        createResultTableAction =
        	new CreateResultTableAction(swingSession, match);
        buildUI();
        setDefaultSelections();
        handler.addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				refreshActionStatus();
			}
        });
        handler.setValidated(false); // avoid false hits when newly created
    }

    /**
     * Saves the current match (which is referenced in the plMatch member variable of this editor instance).
     * If there is no current plMatch, a new one will be created and its properties will be set just like
     * they would if one had existed.  In either case, this action will then use Hibernate to save the
     * match object back to the database (but it should use the MatchHome interface instead).
     */
	private Action saveAction = new AbstractAction("Save") {
		public void actionPerformed(final ActionEvent e) {
            try {
                boolean ok = doSave();
                if ( ok ) {
                	JOptionPane.showMessageDialog(swingSession.getFrame(),
                			"Match Interface Saved Successfully",
                			"Saved",JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                ASUtils.showExceptionDialog(swingSession.getFrame(),
                		"Match Interface Not Saved", ex, new MatchMakerQFAFactory());
            }
		}
	};

	private Action newMatchGroupAction = new AbstractAction("New Match Group") {
		public void actionPerformed(ActionEvent arg0) {
			MatchMakerCriteriaGroupEditor editor = null;
			try {
				editor = new MatchMakerCriteriaGroupEditor(swingSession,
						match,
						new MatchMakerCriteriaGroup());
			} catch (ArchitectException e) {
				JOptionPane.showMessageDialog(swingSession.getFrame(),
						"Populate Error", "Error", JOptionPane.ERROR_MESSAGE);
			}
			swingSession.setCurrentEditorComponent(editor);
		}
	};

	private Window getParentWindow() {
	    return SwingUtilities.getWindowAncestor(panel);
	}

    /**
     * Returns the parent (owning) frame of this match editor.  If the owner
     * isn't a frame (it might be a dialog or AWT Window) then null is returned.
     * You should always use {@link #getParentWindow()} in preference to
     * this method unless you really really need a JFrame.
     *
     * @return the parent JFrame of this match editor's panel, or null if
     * the owner is not a JFrame.
     */
    private JFrame getParentFrame() {
        Window owner = getParentWindow();
        if (owner instanceof JFrame) return (JFrame) owner;
        else return null;
    }

	private Action showAuditInfoAction = new AbstractAction("Show Audit Info") {
		public void actionPerformed(ActionEvent e) {

			MatchInfoPanel p = new MatchInfoPanel(match);
			JDialog d = ArchitectPanelBuilder.createSingleButtonArchitectPanelDialog(
					p, getParentWindow(),
					"Audit Information", "OK");
			d.pack();
			d.setVisible(true);
		}};

	private Action runMatchAction = new AbstractAction("Run Match") {
		public void actionPerformed(ActionEvent e) {
			RunMatchDialog p = new RunMatchDialog(swingSession, match, getParentFrame());
			p.pack();
			p.setVisible(true);
		}};

	private Action validationStatusAction = new AbstractAction("View Validation ValidateResult") {
		public void actionPerformed(ActionEvent e) {
			MatchValidationStatus p = new MatchValidationStatus(swingSession, match,
                    ArchitectPanelBuilder.makeOwnedDialog(getPanel(),"View Match Validation Status"));
			p.pack();
			p.setVisible(true);
		}};
	private Action validateMatchAction = new AbstractAction("Validate Match") {
		public void actionPerformed(ActionEvent e) {
			try {
                if (getMatchValidation() == null){
                    matchValidation = new MatchValidation(swingSession, match);
                }
                    matchValidation.showGUI();
			} catch (HeadlessException e1) {
				ASUtils.showExceptionDialog(swingSession.getFrame(),
						"Unknown Error",e1, new MatchMakerQFAFactory());
			} catch (SQLException e1) {
				ASUtils.showExceptionDialog(swingSession.getFrame(),
						"Unknown SQL Error",e1, new MatchMakerQFAFactory());
			} catch (ArchitectException e1) {
				ASUtils.showExceptionDialog(swingSession.getFrame(),
						"Unknown Error",e1, new MatchMakerQFAFactory());
			}
		}};
	private Action viewBuilderAction = new AbstractAction("View Builder") {
		public void actionPerformed(ActionEvent e) {
            SQLTable t = (SQLTable)sourceChooser.getTableComboBox().getSelectedItem();
            JDialog d;
			if (t == null) {
				JOptionPane.showMessageDialog(swingSession.getFrame(),
						"No Table selected, can't create view builder",
						"Error",
						JOptionPane.ERROR_MESSAGE);
			} else {
                try {
                    d = new ViewBuilderDialog(swingSession, getParentFrame(), t);
                    d.pack();
                    d.setSize(800, d.getPreferredSize().height);
                    d.setVisible(true);
                } catch (ArchitectException ex) {
                    ASUtils.showExceptionDialog(swingSession.getFrame(),
                    		"Couldn't create view builder", ex, new MatchMakerQFAFactory());
                }
            }
		}};

	private final Action createResultTableAction;
    
	private Action matchResultVisualizerAction = new AbstractAction("Visualize Match Results") {
        public void actionPerformed(ActionEvent e) {
            if (matchResultVisualizer == null) {
                try {
                    matchResultVisualizer = new MatchResultVisualizer(match, swingSession);
                } catch (Exception ex) {
                    ASUtils.showExceptionDialog(panel, "Couldn't create match result visualizer component", ex, null);
                }
            }
            matchResultVisualizer.showDialog();
        }
    };

	private Action createIndexAction = new AbstractAction("Pick Columns"){
		public void actionPerformed(ActionEvent e) {
			if ( match.getSourceTable() == null ) {
				JOptionPane.showMessageDialog(panel,
						"You have to select a source table and save before picking columns" );
				return;
			}
			try {
				new MatchMakerIndexBuilder(match,swingSession);
			} catch (ArchitectException e1) {
				ASUtils.showExceptionDialog(panel, "Unexcepted Error", e1, null);
			}
		}};

	

    private void buildUI() throws ArchitectException {

    	matchId.setName("Match ID");
		sourceChooser = new SQLObjectChooser(swingSession);
        resultChooser = new SQLObjectChooser(swingSession);
        sourceChooser.getTableComboBox().setName("Source Table");
        resultChooser.getCatalogComboBox().setName("Result "+
        		resultChooser.getCatalogTerm().getText());
        resultChooser.getSchemaComboBox().setName("Result "+
        		resultChooser.getSchemaTerm().getText());
        resultTableName.setName("Result Table");

        filterPanel = new FilterComponentsPanel();

        matchType.setModel(new DefaultComboBoxModel(Match.MatchMode.values()));

        sourceChooser.getTableComboBox().addItemListener(new ItemListener(){
        	public void itemStateChanged(ItemEvent e) {
        		filterPanel.getFilterTextArea().setText("");
        	}});

    	viewBuilder = new JButton(viewBuilderAction);
    	createResultTable = new JButton(createResultTableAction);
    	saveMatch = new JButton(saveAction);
    	showAuditInfo = new JButton(showAuditInfoAction);
    	runMatch= new JButton(runMatchAction);
    	validationStatus = new JButton(validationStatusAction);
    	validateMatch = new JButton(validateMatchAction);
        matchResultVisualizerButton = new JButton(matchResultVisualizerAction);
        createIndexButton = new JButton(createIndexAction );
        
    	FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,fill:min(pref;"+new JComboBox().getMinimumSize().width+"px):grow, 4dlu,pref,10dlu, pref,4dlu", // columns
				"10dlu,pref,4dlu,pref,4dlu,pref,4dlu,30dlu,4dlu,pref,   4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref, 4dlu,32dlu,  4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,10dlu"); // rows

		PanelBuilder pb;

		JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
		pb = new PanelBuilder(layout, p);
		CellConstraints cc = new CellConstraints();
		int row = 2;
		pb.add(status, cc.xy(4,row));
		row += 2;
		pb.add(new JLabel("Match ID:"), cc.xy(2,row,"r,c"));
		pb.add(matchId, cc.xy(4,row));
		row += 2;
		pb.add(new JLabel("Folder:"), cc.xy(2,row,"r,c"));
		pb.add(folderComboBox, cc.xy(4,row));
		row += 2;
		pb.add(new JLabel("Description:"), cc.xy(2,row,"r,t"));
		pb.add(new JScrollPane(desc), cc.xy(4,row,"f,f"));
		row += 2;
		pb.add(new JLabel("Type:"), cc.xy(2,row,"r,c"));
		pb.add(matchType, cc.xy(4,row));
		row+=2;
		pb.addTitle("Source Table", cc.xy(2, row));
		row+=2;
		pb.add(viewBuilder, cc.xy(6,row,"f,f"));
		pb.add(sourceChooser.getCatalogTerm(), cc.xy(2,row,"r,c"));
		pb.add(sourceChooser.getCatalogComboBox(), cc.xy(4,row));
		row+=2;
		pb.add(sourceChooser.getSchemaTerm(), cc.xy(2,row,"r,c"));
		pb.add(sourceChooser.getSchemaComboBox(), cc.xy(4,row));
		row+=2;
		pb.add(new JLabel("Table Name:"), cc.xy(2,row,"r,c"));
		pb.add(sourceChooser.getTableComboBox(), cc.xy(4,row));
		row+=2;
		pb.add(new JLabel("Unique Index:"), cc.xy(2,row,"r,t"));
		pb.add(indexComboBox, cc.xy(4,row,"f,f"));
		pb.add(createIndexButton, cc.xy(6,row,"f,f"));
		row+=2;
		pb.add(new JLabel("Filter:"), cc.xy(2,row,"r,t"));
		pb.add(filterPanel, cc.xy(4,row,"f,f"));
		row+=2;
		pb.addTitle("Output Table", cc.xy(2, row));
		row+=2;
		pb.add(resultChooser.getCatalogTerm(), cc.xy(2,row,"r,c"));
		pb.add(resultChooser.getCatalogComboBox(), cc.xy(4,row));
		pb.add(createResultTable, cc.xywh(6,row,1,1));
		row+=2;
		pb.add(resultChooser.getSchemaTerm(), cc.xy(2,row,"r,c"));
		pb.add(resultChooser.getSchemaComboBox(), cc.xy(4,row));
		row+=2;
		pb.add(new JLabel("Table Name:"), cc.xy(2,row,"r,c"));
		pb.add(resultTableName, cc.xy(4,row));

		ButtonStackBuilder bb = new ButtonStackBuilder();
		bb.addGridded(saveMatch);
		bb.addRelatedGap();
		bb.addRelatedGap();
		bb.addGridded(new JButton(newMatchGroupAction));
		bb.addRelatedGap();
		bb.addRelatedGap();
		bb.addGridded(showAuditInfo);
		bb.addRelatedGap();
		bb.addRelatedGap();
		bb.addGridded(runMatch);
		bb.addRelatedGap();
		bb.addRelatedGap();
		bb.addGridded(validationStatus);
        bb.addRelatedGap();
        bb.addRelatedGap();
        bb.addGridded(validateMatch);
        bb.addRelatedGap();
        bb.addRelatedGap();
        bb.addGridded(matchResultVisualizerButton);

		pb.add(bb.getPanel(), cc.xywh(8,4,1,16,"f,f"));
		panel = pb.getPanel();
    }


    private void setDefaultSelections() throws ArchitectException {

    	final List<PlFolder> folders = swingSession.getFolders();
    	final SQLDatabase loginDB = swingSession.getDatabase();
        sourceChooser.getDataSourceComboBox().setSelectedItem(loginDB.getDataSource());
        resultChooser.getDataSourceComboBox().setSelectedItem(loginDB.getDataSource());

        sourceChooser.getCatalogComboBox().setRenderer(new SQLObjectComboBoxCellRenderer());
        sourceChooser.getSchemaComboBox().setRenderer(new SQLObjectComboBoxCellRenderer());
        sourceChooser.getTableComboBox().setRenderer(new SQLObjectComboBoxCellRenderer());
        resultChooser.getCatalogComboBox().setRenderer(new SQLObjectComboBoxCellRenderer());
        resultChooser.getSchemaComboBox().setRenderer(new SQLObjectComboBoxCellRenderer());

        folderComboBox.setModel(new DefaultComboBoxModel(folders.toArray()));
        folderComboBox.setRenderer(new MatchMakerObjectComboBoxCellRenderer());
        if ( match.getParent() != null) {
       		folderComboBox.setSelectedItem(match.getParent());
        } else if ( folder != null ) {
        	folderComboBox.setSelectedItem(folder);
        }

        matchId.setText(match.getName());
        desc.setText(match.getMatchSettings().getDescription());
        matchType.setSelectedItem(match.getType());
        filterPanel.getFilterTextArea().setText(match.getFilter());

        refreshIndexComboBox(match.getSourceTableIndex(),match.getSourceTable());

        // listen to the table change
        sourceChooser.getTableComboBox().addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				refreshIndexComboBox(null,(SQLTable) sourceChooser.getTableComboBox().getSelectedItem());
			}});
        
        // listen to the sourceTableIndex changes
        match.addMatchMakerListener(new MatchMakerListener<Match, MatchMakerFolder>(){

        	// don't care
			public void mmChildrenInserted(MatchMakerEvent<Match, MatchMakerFolder> evt) {}

			//don't care
			public void mmChildrenRemoved(MatchMakerEvent<Match, MatchMakerFolder> evt) {}

			public void mmPropertyChanged(MatchMakerEvent<Match, MatchMakerFolder> evt) {
				if ( evt.getPropertyName().equals("sourceTableIndex")) {
					try {
						refreshIndexComboBox(match.getSourceTableIndex(),(SQLTable) sourceChooser.getTableComboBox().getSelectedItem());
					} catch (ArchitectException e) {
						ASUtils.showExceptionDialog(getPanel(),
								"Unexcepted error when getting unique index of match",
								e, null);
					}
				}
			}
			//don't care
			public void mmStructureChanged(MatchMakerEvent<Match, MatchMakerFolder> evt) {}
			});
        
        Validator v = new MatchNameValidator(swingSession);
        handler.addValidateObject(matchId,v);

        List<Action> actionsToDisable = new ArrayList<Action>();
        actionsToDisable.add(newMatchGroupAction);
        Validator v2 = new MatchSourceTableValidator(actionsToDisable);
        handler.addValidateObject(sourceChooser.getTableComboBox(),v2);

        Validator v2a = new MatchSourceTableIndexValidator();
        handler.addValidateObject(indexComboBox,v2a);

        if ( resultChooser.getCatalogComboBox().isEnabled() ) {
        	Validator v3 = new MatchResultCatalogSchemaValidator("Result "+
        			resultChooser.getCatalogTerm().getText());
        	handler.addValidateObject(resultChooser.getCatalogComboBox(),v3);
        }

        if ( resultChooser.getSchemaComboBox().isEnabled() ) {
        	Validator v4 = new MatchResultCatalogSchemaValidator("Result "+
        			resultChooser.getSchemaTerm().getText());
        	handler.addValidateObject(resultChooser.getSchemaComboBox(),v4);
        }

        Validator v5 = new MatchResultTableNameValidator();
        handler.addValidateObject(resultTableName,v5);

        Validator v6 = new AlwaysOKValidator();
        handler.addValidateObject(desc, v6);
        handler.addValidateObject(filterPanel.getFilterTextArea(), v6);


        if ( match.getSourceTable() != null ) {
        	SQLTable sourceTable = match.getSourceTable();
        	filterPanel.setTable(sourceTable);
        	SQLCatalog cat = sourceTable.getCatalog();
        	SQLSchema sch = sourceTable.getSchema();
        	sourceChooser.getCatalogComboBox().setSelectedItem(cat);
        	sourceChooser.getSchemaComboBox().setSelectedItem(sch);
        	sourceChooser.getTableComboBox().setSelectedItem(sourceTable);
        	SQLIndex pk = match.getSourceTableIndex();
        	indexComboBox.setSelectedItem(pk);
    	}
        if (indexComboBox.getSelectedItem() == null) {
        	createResultTableAction.setEnabled(false);
        }

    	SQLTable resultTable = match.getResultTable();
    	if ( resultTable != null ) {
    		SQLCatalog cat = resultTable.getCatalog();
    		SQLSchema sch = resultTable.getSchema();
    		if ( cat != null ) {
    			resultChooser.getCatalogComboBox().setSelectedItem(cat);
    		}
    		if ( sch != null ) {
    			resultChooser.getSchemaComboBox().setSelectedItem(sch);
    		}
    		resultTableName.setText(match.getResultTable().getName());
    	}

        //This listener is put here to update the SQLTable in FilterPanel so the
        //FilterMakerDialog two dropdown boxes can work properly
        sourceChooser.getTableComboBox().addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                filterPanel.setTable((SQLTable)(sourceChooser.getTableComboBox().getSelectedItem()));
            }
         });
    }

    /**
     * refersh combobox item
     * @param newIndex       the match object, if the custom pick index is not a
     * part of the table, we will add it to the combobox as well
     * @param newTable    the sqlTable contains unique index
     * @throws ArchitectException
     */
	private void refreshIndexComboBox(SQLIndex newIndex, SQLTable newTable) {
		
		try {
			indexComboBox.removeAllItems();
	        if ( newTable != null ) {
	        	for ( SQLIndex index : newTable.getUniqueIndex() ) {
	        		indexComboBox.addItem(index);
	        	}
	        	if ( !newTable.getUniqueIndex().contains(newIndex) &&
	        			newIndex != null ) {
	        		indexComboBox.addItem(newIndex);
	        	}
	        } else if ( newIndex!= null ){
	        	indexComboBox.addItem(newIndex);
	        }
	        indexComboBox.setSelectedItem(newIndex);
		} catch (ArchitectException e1) {
			ASUtils.showExceptionDialog(getPanel(),
					"Unexcepted error when getting index list",
					e1, null);
		}
		
		
	}

	public JPanel getPanel() {
		return panel;
	}



    /**
     * Copies all the values from the GUI components into the PlMatch
     * object this component is editing, then persists it to the database.
     * @throws ArchitectException
     * @return true if save OK
     */
    public boolean doSave() {

    	List<String> fail = handler.getFailResults();
    	List<String> warn = handler.getWarnResults();

    	if ( fail.size() > 0 ) {
    		StringBuffer failMessage = new StringBuffer();
    		for ( String f : fail ) {
    			failMessage.append(f).append("\n");
    		}
    		JOptionPane.showMessageDialog(swingSession.getFrame(),
    				"You have to fix these errors before saving:\n"+failMessage.toString(),
    				"Match error",
    				JOptionPane.ERROR_MESSAGE);
    		return false;
    	} else if ( warn.size() > 0 ) {
    		StringBuffer warnMessage = new StringBuffer();
    		for ( String w : warn ) {
    			warnMessage.append("--").append(w).append("\n");
    		}
    		JOptionPane.showMessageDialog(swingSession.getFrame(),
    				"Warning: match will be saved, but you may not be able to run it, because of these wanings:\n"+warnMessage.toString(),
    				"Match warning",
    				JOptionPane.INFORMATION_MESSAGE);
    	}

    	final String matchName = matchId.getText().trim();
        match.setType((Match.MatchMode)matchType.getSelectedItem());
        match.getMatchSettings().setDescription(desc.getText());

        match.setSourceTable(((SQLTable) sourceChooser.getTableComboBox().getSelectedItem()));
        match.setSourceTableIndex(((SQLIndex) indexComboBox.getSelectedItem()));

        if ((matchName == null || matchName.length() == 0) &&
        		match.getSourceTable() == null ) {
        	JOptionPane.showMessageDialog(getPanel(),
        			"Match Name can not be empty",
        			"Error",
        			JOptionPane.ERROR_MESSAGE);
        	return false;
        }

        String id = matchName;
        if ( id == null || id.length() == 0 ) {
        	StringBuffer s = new StringBuffer();
        	s.append("MATCH_");
        	SQLTable table = match.getSourceTable();
			if ( table != null &&
					table.getCatalogName() != null &&
        			table.getCatalogName().length() > 0 ) {
        		s.append(table.getCatalogName()).append("_");
        	}
			if ( table != null &&
					table.getSchemaName() != null &&
        			table.getSchemaName().length() > 0 ) {
        		s.append(table.getSchemaName()).append("_");
        	}
			if ( table != null ) {
				s.append(table.getName());
			}
        	id = s.toString();
        	matchId.setText(id);
        }

        SQLObject resultTableParent;
        if ( resultChooser.getSchemaComboBox().isEnabled() &&
        		resultChooser.getSchemaComboBox().getSelectedItem() != null ) {
        	resultTableParent =
        		(SQLSchema) resultChooser.getSchemaComboBox().getSelectedItem();
        } else if ( resultChooser.getCatalogComboBox().isEnabled() &&
        		resultChooser.getCatalogComboBox().getSelectedItem() != null ) {
        	resultTableParent =
        		(SQLCatalog) resultChooser.getCatalogComboBox().getSelectedItem();
        } else {
        	resultTableParent = (SQLDatabase) resultChooser.getDb();
        }

        String trimmedResultTableName = resultTableName.getText().trim();
        if ( trimmedResultTableName == null || trimmedResultTableName.length() == 0 ) {
            //matchName (string taken from the match id textfield) is used
            //instead of match.getName() because if the match is new, the
            //matchName has not been saved to the database and therefore would
            //return MM.Null instead
            trimmedResultTableName = "MM_"+matchName;
        	resultTableName.setText(trimmedResultTableName);
        }

        try {
        	match.setResultTable(new SQLTable(resultTableParent,
        			trimmedResultTableName,
        			"MatchMaker result table",
        			"TABLE", true));
        } catch ( ArchitectException e ) {
        	ASUtils.showExceptionDialog(swingSession.getFrame(),"Save error",e, new MatchMakerQFAFactory());
        	return false;
        }

        match.setFilter(filterPanel.getFilterTextArea().getText());

        if ( !matchId.getText().equals(match.getName()) ) {
        	if ( !swingSession.isThisMatchNameAcceptable(matchId.getText()) ) {
        		JOptionPane.showMessageDialog(getPanel(),
        				"Match name \""+matchId.getText()+
        				"\" exist or invalid. The match has not been saved",
        				"Match name invalid",
        				JOptionPane.ERROR_MESSAGE);
        		return false;
        	}
        	match.setName(matchId.getText());
        }

        logger.debug("Saving Match:" + match.getName());

        PlFolder selectedFolder = (PlFolder) folderComboBox.getSelectedItem();
        if (!selectedFolder.getChildren().contains(match)) {
            swingSession.move(match,selectedFolder);
        	swingSession.save(selectedFolder);
        }

        swingSession.save(match);

        handler.setValidated(false);
		return true;

    }

    private void refreshActionStatus() {
    	ValidateResult worst = handler.getWorstValidationStatus();
    	saveAction.setEnabled(true);
		newMatchGroupAction.setEnabled(true);
		runMatchAction.setEnabled(true);
    	if ( worst.getStatus() == Status.FAIL ) {
    		saveAction.setEnabled(false);
    		newMatchGroupAction.setEnabled(false);
    		runMatchAction.setEnabled(false);
    	} else if ( worst.getStatus() == Status.WARN ) {
    		runMatchAction.setEnabled(false);
    	}
    	if (sourceChooser.getTableComboBox().getSelectedItem() == null){
    		newMatchGroupAction.setEnabled(false);
    	}

    	ValidateResult r1 = handler.getResultOf(indexComboBox);
    	ValidateResult r2 = handler.getResultOf(resultTableName);
    	if ( r1 == null || r1.getStatus() != Status.OK ||
    			r2 == null || r2.getStatus() != Status.OK ) {
    		createResultTableAction.setEnabled(false);
    	} else {
    		createResultTableAction.setEnabled(true);
    	}
    }

    private class MatchNameValidator implements Validator {

		private MatchMakerSwingSession session;

		public MatchNameValidator(MatchMakerSwingSession session) {
    		this.session = session;
		}

		public ValidateResult validate(Object contents) {

			String value = (String)contents;
			if ( value == null || value.length() == 0 ) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Match name is required");
			} else if ( !value.equals(match.getName()) &&
						!session.isThisMatchNameAcceptable(value) ) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Match name is invalid or already exists.");
			}
			return ValidateResult.createValidateResult(Status.OK, "");
		}
    }

    private class MatchSourceTableValidator implements Validator {

        List<Action> actionsToDisable;

        public MatchSourceTableValidator(List<Action> actionsToDisable){
            this.actionsToDisable = actionsToDisable;
        }

        public ValidateResult validate(Object contents) {

			SQLTable value = (SQLTable)contents;
			if ( value == null ) {
                enableAction(false);
				return ValidateResult.createValidateResult(Status.WARN,
						"Match source table is required");
			} else {
				try {
					value.populate();
                    enableAction(true);
				} catch (ArchitectException e) {
					return ValidateResult.createValidateResult(Status.WARN,
						"Match source table has error:"+e.getMessage());
				}
			}
			return ValidateResult.createValidateResult(Status.OK, "");
		}

        public void enableAction(boolean enable){
            for (Action a : actionsToDisable){
                a.setEnabled(enable);
            }
        }
    }


    private class MatchSourceTableIndexValidator implements Validator {

    	public ValidateResult validate(Object contents) {
			SQLIndex value = (SQLIndex)contents;
			if ( value == null ) {
				return ValidateResult.createValidateResult(Status.WARN,
						"Match source table index is required");
			}
			return ValidateResult.createValidateResult(Status.OK, "");
		}
    }

    private class MatchResultCatalogSchemaValidator implements Validator {

    	private String componentName;
    	public MatchResultCatalogSchemaValidator(String componentName) {
    		this.componentName = componentName;
		}
		public ValidateResult validate(Object contents) {
			SQLObject value = (SQLObject)contents;
			if ( value == null ) {
				return ValidateResult.createValidateResult(Status.WARN,
						componentName + " is required");
			}
			return ValidateResult.createValidateResult(Status.OK, "");
		}
    }

    private class MatchResultTableNameValidator implements Validator {

		public ValidateResult validate(Object contents) {
			final Pattern sqlIdentifierPattern =
				Pattern.compile("[a-z_][a-z0-9_]*", Pattern.CASE_INSENSITIVE);

			String value = (String)contents;
			if ( value == null || value.length() == 0 ) {
				return ValidateResult.createValidateResult(Status.WARN,
						"Match result table name is required");
			} else if (!sqlIdentifierPattern.matcher(value).matches()) {
				return ValidateResult.createValidateResult(Status.WARN,
						"Result table name is not a valid SQL identifier");
			} else {
				return ValidateResult.createValidateResult(Status.OK, "");
			}
		}
    }

    public MatchValidation getMatchValidation() {
        return matchValidation;
    }

    public void setMatchValidation(MatchValidation matchValidation) {
        this.matchValidation = matchValidation;
    }

	public boolean hasUnsavedChanges() {
		return handler.isValidated();
	}
}