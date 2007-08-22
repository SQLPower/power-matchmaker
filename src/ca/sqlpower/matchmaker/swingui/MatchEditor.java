package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLStatement;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerCriteriaGroup;
import ca.sqlpower.matchmaker.MatchMakerFolder;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;
import ca.sqlpower.matchmaker.validation.MatchNameValidator;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSUtils.FileExtensionFilter;
import ca.sqlpower.validation.AlwaysOKValidator;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The MatchEditor is the GUI for editing all aspects of a {@link Match} instance.
 */
public class MatchEditor implements EditorPane {

	private static final Logger logger = Logger.getLogger(MatchEditor.class);

	/**
	 * The collection of combo boxes for choosing the match source table (or view).
	 */
	private SQLObjectChooser sourceChooser;
	
	/**
	 * The collection of combo boxes for choosing the match result table.
	 */
	private SQLObjectChooser resultChooser;

	/**
	 * The panel that holds this editor's GUI.
	 */
	private final JPanel panel;

	StatusComponent status = new StatusComponent();
    private JTextField matchId = new JTextField();
    private JComboBox folderComboBox = new JComboBox();
    private JComboBox indexComboBox = new JComboBox();
    private JTextArea desc = new JTextArea();
    private JComboBox matchType = new JComboBox();
    private JTextField resultTableName = new JTextField();
    private JButton viewBuilder;
    private JButton createResultTable;
    private JButton createIndexButton;
    private JButton saveMatch;
    
    private FilterComponents filterPanel;

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
	 * @param match the Match Object to be edited
	 * @param folder the match's parent folder (XXX isn't this just match.getParent()?)
	 * @param newMatch a flag that indicates if match is a new match or not
	 */
    public MatchEditor(final MatchMakerSwingSession swingSession, Match match, PlFolder<Match> folder) throws ArchitectException {
        this.swingSession = swingSession;
        if (match == null) throw new NullPointerException("You can't edit a null plmatch");
        this.match = match;
        this.folder = folder;
        handler = new FormValidationHandler(status);
        createResultTableAction = new CreateResultTableAction(swingSession, match);
        panel = buildUI();
        setDefaultSelections();
        handler.addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				refreshActionStatus();
			}
        });
        handler.resetHasValidated(); // avoid false hits when newly created

        /**
         * listen to the parent of the match, to handle the match removal
         * from the tree. if this currently editting match was deleted
         * from the tree, we want to close this panel without saving.
         */
        if ( match.getParent() != null ) {
        	final Match match2 = match;
        	final PlFolder<Match> parentFolder = (PlFolder<Match>) match.getParent();
        	final JPanel panel = getPanel();
        	final MatchMakerListener<PlFolder, Match> matchRemovalListener = new MatchMakerListener<PlFolder, Match>(){

        		// we don't care
        		public void mmChildrenInserted(MatchMakerEvent<PlFolder, Match> evt) {}

        		public void mmChildrenRemoved(MatchMakerEvent<PlFolder, Match> evt) {
        			if (!panel.isVisible() || !panel.isDisplayable()) {
        				return;
        			}
        			final List<Match> matches = evt.getChildren();
        			boolean found = false;
        			for ( Match m : matches ) {
        				if ( m == match2 ) found = true;
        			}
        			if ( found ) {
        				logger.debug("This match is deleted from somewhere");
        				logger.debug("we have to close the editor without saving");
        				handler.resetHasValidated();
        				panel.setVisible(false);
        				swingSession.setCurrentEditorComponent(null);
        				parentFolder.removeMatchMakerListener(this);
        			}
        		}
        		// we don't care
        		public void mmPropertyChanged(MatchMakerEvent<PlFolder, Match> evt) {}
        		// we don't care
        		public void mmStructureChanged(MatchMakerEvent<PlFolder, Match> evt) {}
        	};
        	parentFolder.addMatchMakerListener(matchRemovalListener);
        }
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
                MMSUtils.showExceptionDialog(swingSession.getFrame(),
                		"Match Interface Not Saved", ex);
            }
		}
	};

	private Action newMatchGroupAction = new AbstractAction("New Match Group") {
		public void actionPerformed(ActionEvent arg0) {
			MatchMakerCriteriaGroupEditor editor = null;
			editor = new MatchMakerCriteriaGroupEditor(swingSession,
					match,
					new MatchMakerCriteriaGroup());
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
                    SPSUtils.showExceptionDialogNoReport(swingSession.getFrame(),
                    		"Couldn't create view builder", ex);
                }
            }
		}
	};

	private final Action createResultTableAction;

	private Action createIndexAction = new AbstractAction("Pick Columns"){
		public void actionPerformed(ActionEvent e) {
			if ( match.getSourceTable() == null ) {
				JOptionPane.showMessageDialog(panel,
						"You have to select a source table and save before picking columns" );
				return;
			}
			try {
				new MatchMakerIndexBuilder(match,swingSession);
			} catch (Exception ex) {
				SPSUtils.showExceptionDialogNoReport(panel, "An exception occured while picking columns", ex);
			}
		}
	};

    private JPanel buildUI() {

    	matchId.setName("Match ID");
		sourceChooser = new SQLObjectChooser(swingSession);
        resultChooser = new SQLObjectChooser(swingSession);
        sourceChooser.getTableComboBox().setName("Source Table");
        resultChooser.getCatalogComboBox().setName("Result "+
        		resultChooser.getCatalogTerm().getText());
        resultChooser.getSchemaComboBox().setName("Result "+
        		resultChooser.getSchemaTerm().getText());
        resultTableName.setName("Result Table");

        filterPanel = new FilterComponents(swingSession.getFrame());

        matchType.setModel(new DefaultComboBoxModel(Match.MatchMode.values()));

        sourceChooser.getTableComboBox().addItemListener(new ItemListener(){
        	public void itemStateChanged(ItemEvent e) {
        		filterPanel.getFilterTextArea().setText("");
        	}});

    	viewBuilder = new JButton(viewBuilderAction);
    	createResultTable = new JButton(createResultTableAction);
    	saveMatch = new JButton(saveAction);
        createIndexButton = new JButton(createIndexAction );

    	FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,fill:min(pref;"+new JComboBox().getMinimumSize().width+"px):grow, 4dlu,pref,4dlu", // columns
				"10dlu,pref,4dlu,pref,4dlu,pref,4dlu,40dlu,4dlu,pref,   4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref, 4dlu,32dlu,  4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,10dlu"); // rows

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
        desc.setWrapStyleWord(true);
        desc.setLineWrap(true);
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
		pb.add(new JScrollPane(filterPanel.getFilterTextArea()), cc.xy(4,row,"f,f"));
        pb.add(filterPanel.getEditButton(), cc.xy(6,row));
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
		row+=2;
		// We don't want the save button to take up the whole column width
		// so we wrap it in a JPanel with a FlowLayout. If there is a better
		// way, please fix this.
		JPanel savePanel = new JPanel(new FlowLayout());
		savePanel.add(saveMatch);
		pb.add(savePanel, cc.xy(4, row));

		return pb.getPanel();
    }


    private void setDefaultSelections() throws ArchitectException {

    	final List<PlFolder> folders = swingSession.getCurrentFolderParent().getChildren();
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

        Validator v = new MatchNameValidator(swingSession,match);
        handler.addValidateObject(matchId,v);

        Validator v2 = new MatchSourceTableValidator(Collections.singletonList(saveAction));
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
    	}
        if (indexComboBox.getSelectedItem() == null) {
        	createResultTableAction.setEnabled(false);
        }

        refreshIndexComboBox(match.getSourceTableIndex(),match.getSourceTable());

        // listen to the table change
        sourceChooser.getTableComboBox().addItemListener(new ItemListener(){
        	public void itemStateChanged(ItemEvent e) {
        		try {
					refreshIndexComboBox(null,(SQLTable) sourceChooser.getTableComboBox().getSelectedItem());
				} catch (ArchitectException e1) {
					throw new ArchitectRuntimeException(e1);
				}
        	}
        });

        // listen to the sourceTableIndex changes,
        // for update the index combobox selection
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
						throw new ArchitectRuntimeException(e);
					}
        		}
        	}
        	//don't care
        	public void mmStructureChanged(MatchMakerEvent<Match, MatchMakerFolder> evt) {}
        });


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
     */
	private void refreshIndexComboBox(SQLIndex newIndex, SQLTable newTable) throws ArchitectException {

		indexComboBox.removeAllItems();
		if ( newTable != null ) {
			boolean contains = false;
			for ( SQLIndex index : newTable.getUniqueIndices() ) {
				indexComboBox.addItem(index);
				if ( newIndex != null && index.getName().equalsIgnoreCase(newIndex.getName())) {
					contains = true;
				}
			}
			if ( !contains && newIndex != null ) {
				indexComboBox.addItem(newIndex);
				indexComboBox.setSelectedItem(newIndex);
			} else if ( indexComboBox.getItemCount() > 0 ) {
				indexComboBox.setSelectedIndex(0);
			}
		} else if ( newIndex!= null ){
			indexComboBox.addItem(newIndex);
			indexComboBox.setSelectedItem(newIndex);
		}
	}

	public JPanel getPanel() {
		return panel;
	}

    /**
     * Copies all the values from the GUI components into the PlMatch
     * object this component is editing, then persists it to the database.
     * @return true if save OK
     * @throws ArchitectRuntimeException if we cannot set the result table on a match
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

        if(resultChooser.getCatalogComboBox().getSelectedItem() != null) {
        	match.setResultTableCatalog( ((SQLCatalog) resultChooser.getCatalogComboBox().getSelectedItem()).getName());
        }
        if(resultChooser.getSchemaComboBox().getSelectedItem() != null) {
        	match.setResultTableSchema( ((SQLSchema) resultChooser.getSchemaComboBox().getSelectedItem()).getName());
        }
        
        try {
			match.setResultTable(new SQLTable(resultTableParent,
					trimmedResultTableName,
					"MatchMaker result table",
					"TABLE", true));
		} catch (ArchitectException e) {
			throw new ArchitectRuntimeException(e);
		}

        match.setFilter(filterPanel.getFilterTextArea().getText());

        String matchIdText = matchId.getText();
		if (!matchIdText.equals(match.getName())) {
        	if (!swingSession.isThisMatchNameAcceptable(matchIdText)) {
        		JOptionPane.showMessageDialog(getPanel(),
        				"<html>Match name \"" + matchId.getText() +
        					"\" does not exist or is invalid.\n" +
        					"The match has not been saved",
        				"Match name invalid",
        				JOptionPane.ERROR_MESSAGE);
        		return false;
        	}
        	match.setName(matchIdText);
        }

        logger.debug("Saving Match:" + match.getName());

        PlFolder selectedFolder = (PlFolder) folderComboBox.getSelectedItem();
        if (!selectedFolder.getChildren().contains(match)) {
            swingSession.move(match,selectedFolder);
        	swingSession.save(selectedFolder);
        }

        swingSession.save(match);
        handler.resetHasValidated();

        // bring back some buttons like create index...
        refreshActionStatus();
		return true;

    }

    private void refreshActionStatus() {
    	ValidateResult worst = handler.getWorstValidationStatus();
    	saveAction.setEnabled(true);
		newMatchGroupAction.setEnabled(true);

    	if ( worst.getStatus() == Status.FAIL ) {
    		saveAction.setEnabled(false);
    		newMatchGroupAction.setEnabled(false);
    	}
    	if (sourceChooser.getTableComboBox().getSelectedItem() == null){
    		newMatchGroupAction.setEnabled(false);
    		createIndexAction.setEnabled(false);
    	} else {
    		if (sourceChooser.getTableComboBox().getSelectedItem() !=
        		match.getSourceTable() ) {
        		createIndexAction.setEnabled(false);
        	} else {
        		createIndexAction.setEnabled(true);
        	}
    	}

    	ValidateResult r1 = handler.getResultOf(indexComboBox);
    	ValidateResult r2 = handler.getResultOf(resultTableName);
    	ValidateResult r3 = handler.getResultOf(sourceChooser.getTableComboBox());
    	if ( r1 == null || r1.getStatus() != Status.OK ||
    			r2 == null || r2.getStatus() != Status.OK ||
    			r3 == null || r3.getStatus() != Status.OK ||
    			handler.hasPerformedValidation() ) {
    		createResultTableAction.setEnabled(false);
    	} else {
    		createResultTableAction.setEnabled(true);
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
			}
			else {
				try {
					value.populate();
				} catch (ArchitectException e) {
					throw new ArchitectRuntimeException(e);
				}
				enableAction(true);

				final String tableName = resultTableName.getText();
				if (tableName != null && tableName.length() > 0) {
					String catalogName = null;
					String schemaName = null;

					if ( resultChooser.getCatalogComboBox().getSelectedItem() != null) {
						catalogName = ((SQLCatalog) resultChooser.getCatalogComboBox().getSelectedItem()).getName();
					}
					if ( resultChooser.getSchemaComboBox().getSelectedItem() != null) {
						schemaName = ((SQLSchema) resultChooser.getSchemaComboBox().getSelectedItem()).getName();
					}
					SQLTable resultTable;
					try {
						resultTable = swingSession.getDatabase().getTableByName(
								catalogName, schemaName, tableName);
					} catch (ArchitectException e) {
						throw new ArchitectRuntimeException(e);
					}
					if ( value == resultTable ) {
						return ValidateResult.createValidateResult(
								Status.WARN,
								"Match source table has the same name as the result table");
					}
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
        private static final int MAX_CHAR_RESULT_TABLE = 30;

		public ValidateResult validate(Object contents) {
			final Pattern sqlIdentifierPattern =
				Pattern.compile("[a-z_][a-z0-9_]*", Pattern.CASE_INSENSITIVE);

			String value = (String)contents;
			if ( value == null || value.length() == 0 ) {
				return ValidateResult.createValidateResult(Status.WARN,
						"Match result table name is required");
			} else if (value.length() > MAX_CHAR_RESULT_TABLE){
			    return ValidateResult.createValidateResult(Status.FAIL, "The result table" +
                        "cannot be longer than " +  MAX_CHAR_RESULT_TABLE + " characters long");
            } else if (!sqlIdentifierPattern.matcher(value).matches()) {
				return ValidateResult.createValidateResult(Status.WARN,
						"Result table name is not a valid SQL identifier");
			} else if (sourceChooser.getTableComboBox().getSelectedItem() != null ) {
				SQLTable sourceTable = (SQLTable) sourceChooser.getTableComboBox().getSelectedItem();
				String catalogName = null;
				String schemaName = null;
				if ( resultChooser.getCatalogComboBox().getSelectedItem() != null) {
					catalogName = ((SQLCatalog) resultChooser.getCatalogComboBox().getSelectedItem()).getName();
				}
				if ( resultChooser.getSchemaComboBox().getSelectedItem() != null) {
					schemaName = ((SQLSchema) resultChooser.getSchemaComboBox().getSelectedItem()).getName();
				}

				SQLTable resultTable;
				try {
					resultTable = swingSession.getDatabase().getTableByName(
							catalogName, schemaName, value);
				} catch (ArchitectException e) {
					throw new ArchitectRuntimeException(e);
				}
				if ( sourceTable == resultTable ) {
					return ValidateResult.createValidateResult(Status.WARN,
							"Match result table has the same name as the source table");
				}
			}
			return ValidateResult.createValidateResult(Status.OK, "");
		}
    }

	public boolean hasUnsavedChanges() {
		return handler.hasPerformedValidation();
	}


	/**
	 * Creates a new Match object and a GUI editor for it, then puts that editor in the split pane.
	 */
	private final class CreateResultTableAction extends AbstractAction {

	    private final MatchMakerSwingSession swingSession;
		private Match match;

		public CreateResultTableAction(
	            MatchMakerSwingSession swingSession,
	            Match match) {
			super("Create Result Table");
	        this.swingSession = swingSession;
			this.match = match;
		}

		public void actionPerformed(ActionEvent e) {
			if (hasUnsavedChanges()) {
				int choice = JOptionPane.showOptionDialog(
						MatchEditor.this.getPanel(),
						"Your match has unsaved changes", "Unsaved Changes",
						0, 0, null,
						new String[] { "Save", "Cancel" }, "Save");
				logger.debug("choice: "+choice);
				if (choice == 0) {
					boolean success = doSave();
					if (!success) {
						JOptionPane.showMessageDialog(
								MatchEditor.this.getPanel(),
						"Validation Error.  Can't save.");
						return;
					}
				} else if (choice == 1 || choice == -1) {
					return;
				} else {
					throw new IllegalStateException("Unknown choice: "+choice);
				}
			}
			try {
				showSqlGui();
			} catch (Exception ex) {
				SPSUtils.showExceptionDialogNoReport(swingSession.getFrame(), "Couldn't create SQL Preview", ex);
			}
		}

		/**
		 * Creates and shows a dialog with the generated SQL in it.
		 * The dialog has buttons with actions that can save, execute,
		 * or copy the SQL to the clipboard.
		 */
		public void showSqlGui()
			throws InstantiationException, IllegalAccessException,
			HeadlessException, SQLException, ArchitectException, ClassNotFoundException {

			final DDLGenerator ddlg = DDLUtils.createDDLGenerator(
					swingSession.getDatabase().getDataSource());
			if (ddlg == null) {
				JOptionPane.showMessageDialog(swingSession.getFrame(),
						"Couldn't create DDL Generator for database type\n"+
						swingSession.getDatabase().getDataSource().getDriverClass());
				return;
			}
			ddlg.setTargetCatalog(match.getResultTableCatalog());
			ddlg.setTargetSchema(match.getResultTableSchema());
			if (Match.doesResultTableExist(swingSession, match)) {
				int answer = JOptionPane.showConfirmDialog(swingSession.getFrame(),
						"Result table exists, do you want to drop and recreate it?",
						"Table exists",
						JOptionPane.YES_NO_OPTION);
				if ( answer != JOptionPane.YES_OPTION ) {
					return;
				}
				ddlg.dropTable(match.getResultTable());
			}
			ddlg.addTable(match.createResultTable());
			ddlg.addIndex((SQLIndex) match.getResultTable().getIndicesFolder().getChild(0));

		    final JDialog editor = new JDialog(swingSession.getFrame(),
		    		"Create Result Table", false );
		    JComponent cp = (JComponent) editor.getContentPane();

		    Box statementsBox = Box.createVerticalBox();
		    final List<JTextArea> sqlTextFields = new ArrayList<JTextArea>();
		    for (DDLStatement sqlStatement : ddlg.getDdlStatements()) {
		    	final JTextArea sqlTextArea = new JTextArea(sqlStatement.getSQLText());
				statementsBox.add(sqlTextArea);
				sqlTextFields.add(sqlTextArea);
		    }

		    Action saveAction = new AbstractAction("Save") {
				public void actionPerformed(ActionEvent e) {
					AbstractDocument doc = new DefaultStyledDocument();
					for (JTextArea sqlText : sqlTextFields ) {
				    	try {
							doc.insertString(doc.getLength(),
											sqlText.getText(),
											null);
							doc.insertString(doc.getLength(),";\n",null);
						} catch (BadLocationException e1) {
							SPSUtils.showExceptionDialogNoReport(swingSession.getFrame(), "Unexcepted Document Error",e1);
						}
				    }
					SPSUtils.saveDocument(swingSession.getFrame(),
							doc,
							(FileExtensionFilter)SPSUtils.SQL_FILE_FILTER);
				}
		    };
		    Action copyAction = new AbstractAction("Copy to Clipboard") {
				public void actionPerformed(ActionEvent e) {
					StringBuffer buf = new StringBuffer();
					for (JTextArea sqlText : sqlTextFields ) {
						buf.append(sqlText.getText());
						buf.append(";\n");
				    }
					StringSelection selection = new StringSelection(buf.toString());
					Clipboard clipboard = Toolkit.getDefaultToolkit()
							.getSystemClipboard();
					clipboard.setContents(selection, selection);
				}
		    };
		    Action executeAction = new AbstractAction("Execute") {
				public void actionPerformed(ActionEvent e) {

					Connection con = null;
					Statement stmt = null;
					String sql = null;
					try {
						con = swingSession.getConnection();
						stmt = con.createStatement();
						int successCount = 0;

						for (JTextArea sqlText : sqlTextFields ) {
							sql = sqlText.getText();
							try {
								stmt.executeUpdate(sql);
								successCount += 1;
							} catch (SQLException e1) {
								int choice = JOptionPane.showOptionDialog(editor,
										"The following SQL statement failed:\n" +
										sql +
										"\nThe error was: " + e1.getMessage() +
										"\n\nDo you want to continue executing the create script?",
										"SQL Error", JOptionPane.YES_NO_OPTION,
										JOptionPane.ERROR_MESSAGE, null,
										new String[] {"Abort", "Continue"}, "Continue" );
								if (choice != 1) {
									break;
								}
							}
						}

						JOptionPane.showMessageDialog(swingSession.getFrame(),
								"Successfully executed " + successCount + " of " +
								sqlTextFields.size() + " SQL Statements." +
								(successCount == 0 ? "\n\nBetter Luck Next Time." : ""));

	                    //closes the dialog if all the statement is executed successfully
	                    //if not, the dialog remains on the screen
	                    if (successCount == sqlTextFields.size()){
						    editor.dispose();
	                    }
					} catch (SQLException ex) {
						JOptionPane.showMessageDialog(editor,
								"Create Script Failure",
								"Couldn't allocate a Statement:\n" + ex.getMessage(),
								JOptionPane.ERROR_MESSAGE);
					} finally {
                        try {
                            if (stmt != null) {
                                stmt.close();
                            }
                        } catch (SQLException ex) {
                            logger.warn("Couldn't close statement", ex);
                        }
                        try {
                            if (con != null) {
                                con.close();
                            }
                        } catch (SQLException ex) {
                            logger.warn("Couldn't close connection", ex);
                        }
					}
				}
		    };
		    Action cancelAction = new AbstractAction("Close") {
				public void actionPerformed(ActionEvent e) {
					editor.dispose();
				}
		    };

		    // the gui layout part
		    cp.setLayout(new BorderLayout(10,10));
		    cp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		    cp.add(new JScrollPane(statementsBox), BorderLayout.CENTER);

		    ButtonBarBuilder bbb = ButtonBarBuilder.createLeftToRightBuilder();
		    bbb.addGridded(new JButton(saveAction));
		    bbb.addGridded(new JButton(copyAction));
		    bbb.addGridded(new JButton(executeAction));
		    bbb.addGridded(new JButton(cancelAction));

		    cp.add(bbb.getPanel(), BorderLayout.SOUTH);

		    editor.pack();
		    editor.setLocationRelativeTo(null);
		    editor.setVisible(true);
		}
	}
}