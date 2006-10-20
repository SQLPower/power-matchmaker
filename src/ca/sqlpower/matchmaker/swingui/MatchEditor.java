package ca.sqlpower.matchmaker.swingui;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.PlDotIni;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.matchmaker.MatchType;
import ca.sqlpower.matchmaker.hibernate.PlFolder;
import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.swingui.action.NewMatchGroupAction;
import ca.sqlpower.matchmaker.util.HibernateUtil;

import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class MatchEditor extends JFrame {

	private static final Logger logger = Logger.getLogger(MatchEditor.class);

	private SQLObjectChooser sourceChooser;
	private SQLObjectChooser xrefChooser;
	private SQLObjectChooser resultChooser;

	private JPanel panel;

    private JTextField matchId = new JTextField();
    private JComboBox folderComboBox = new JComboBox();
    private JTextArea desc = new JTextArea();
    private JComboBox type = new JComboBox();

    private JTextField resultTableName = new JTextField();

    private JButton viewBuilder;
    private JButton createResultTable;

    private JButton saveMatch;
    //private JButton exitEditor;
    private JButton showAuditInfo;
    private JButton runMatch;
    private JButton validationStatus;
    private JButton validateMatch;
    private FilterComponentsPanel filterPanel;

	private PlMatch plMatch;
    private PlFolder plFolder;


    public MatchEditor(PlMatch match,JSplitPane splitPane) throws HeadlessException, ArchitectException {
        this(match,null,splitPane);
    }

    public MatchEditor(PlMatch match, PlFolder folder,JSplitPane splitPane) throws ArchitectException {
        super();
        if ( match == null ) {
            setTitle("Create new match interface");
        } else {
            setTitle("Edit match interface: "+match.getMatchId());
        }
        this.plMatch = match;
        this.plFolder = folder;
        this.splitPane = splitPane;
        buildUI();
    }


    private Action exitAction = new AbstractAction("Exit") {
		public void actionPerformed(ActionEvent e) {
			setVisible(false);
			dispose();
		}};


    private boolean checkStringNullOrEmpty (String value, String name) {
        String trimedValue = null;
        if ( value != null ) {
            trimedValue = value.trim();
        }
        if ( value == null || trimedValue == null || trimedValue.length() == 0 ) {
            JOptionPane.showMessageDialog(
                    MatchEditor.this,
                    name + " is required",
                    name + " is required",
                    JOptionPane.ERROR_MESSAGE );
            return false;
        }
        return true;
    }

    private boolean checkObjectNullOrEmpty (Object value, String name) {
        if ( value == null ) {
            JOptionPane.showMessageDialog(
                    MatchEditor.this,
                    name + " is required",
                    name + " is required",
                    JOptionPane.ERROR_MESSAGE );
            return false;
        }
        return true;
    }

    /**
     * Saves the current match (which is referenced in the plMatch member variable of this editor instance).
     * If there is no current plMatch, a new one will be created and its properties will be set just like
     * they would if one had existed.  In either case, this action will then use Hibernate to save the 
     * match object back to the database (but it should use the MatchHome interface instead).
     */
	private Action saveAction = new AbstractAction("Save") {
		public void actionPerformed(ActionEvent e) {

			if ( plMatch == null ) {

				if ( matchId.getText() != null && matchId.getText().length() > 0 ) {
					if ( MatchMakerFrame.getMainInstance().getMatchByName(matchId.getText()) != null ) {
						JOptionPane.showMessageDialog(
			                    MatchEditor.this,
			                    "Match ["+matchId.getText()+"] Exists!",
			                    "Match ["+matchId.getText()+"] Exists!",
			                    JOptionPane.ERROR_MESSAGE );
						return;
					}
				}
				plMatch = new PlMatch();
				plMatch.setCreateDate(new Date());
			}

			if ( !checkObjectNullOrEmpty(
					sourceChooser.getTableComboBox().getSelectedItem(),
					"Source Table") )
				return;
			if ( !checkStringNullOrEmpty(
					((SQLTable) sourceChooser.getTableComboBox().getSelectedItem()).getName(),
					"Source Table Name") )
				return;

			if ( sourceChooser.getCatalogComboBox().isEnabled() ) {
            	if ( !checkObjectNullOrEmpty(
            			sourceChooser.getCatalogComboBox().getSelectedItem(),
            			"Source Catalog" ) )
            		return;
            	if ( !checkStringNullOrEmpty(
            			((SQLCatalog)sourceChooser.getCatalogComboBox().getSelectedItem()).getName(),
            			"Source Catalog Name" ) )
            		return;
            }

            if ( sourceChooser.getSchemaComboBox().isEnabled() ) {
            	if ( !checkObjectNullOrEmpty(
            			sourceChooser.getSchemaComboBox().getSelectedItem(),
            			"Source Schema"))
            		return;
            	if ( !checkStringNullOrEmpty(
            			((SQLSchema)sourceChooser.getSchemaComboBox().getSelectedItem()).getName(),
            			"Source Schema Name"))
            		return;
            }

            if ( sourceChooser.getCatalogComboBox().isEnabled() &&
            		sourceChooser.getCatalogComboBox().getSelectedItem() != null ) {
            	plMatch.setTableCatalog(((SQLCatalog)sourceChooser.getCatalogComboBox().getSelectedItem()).getName());
            }

            if ( sourceChooser.getSchemaComboBox().isEnabled() &&
            		sourceChooser.getSchemaComboBox().getSelectedItem() != null ) {
            	plMatch.setTableOwner(((SQLSchema)sourceChooser.getSchemaComboBox().getSelectedItem()).getName());
            }
            plMatch.setMatchType(type.getSelectedItem().toString());
            
            plMatch.setMatchDesc(desc.getText());
            plMatch.setMatchTable(
            		((SQLTable) sourceChooser.getTableComboBox().
            				getSelectedItem()).getName());

			String id = matchId.getText().trim();
			if ( id == null || id.length() == 0 ) {
				StringBuffer s = new StringBuffer();
				s.append("MATCH_");
				if ( plMatch.getTableCatalog() != null &&
						plMatch.getTableCatalog().length() > 0 ) {
					s.append(plMatch.getTableCatalog()).append("_");
				}
				if ( plMatch.getTableOwner() != null &&
						plMatch.getTableOwner().length() > 0 ) {
					s.append(plMatch.getTableOwner()).append("_");
				}
				s.append(plMatch.getMatchTable());
				id = s.toString();
				if ( MatchMakerFrame.getMainInstance().getMatchByName(id) == null )
					matchId.setText(id);
			}

            if ( !checkStringNullOrEmpty(matchId.getText(),"Match ID") )
                return;

            plMatch.setMatchId(matchId.getText());
            logger.debug("Saving Match:" + plMatch.getMatchId());


            if ( sourceChooser.getUniqueKeyComboBox().getSelectedItem() != null ) {
                plMatch.setPkColumn(
                		((SQLIndex)sourceChooser.getUniqueKeyComboBox().getSelectedItem()).getName());
            }
            plMatch.setFilter(filterPanel.getFilterTextArea().getText());

            if ( resultChooser.getCatalogComboBox().isEnabled() &&
            		resultChooser.getCatalogComboBox().getSelectedItem() == null ) {
            	SQLDatabase db = resultChooser.getDb();
            	try {
					SQLCatalog cat = db.getCatalogByName(
							((SQLCatalog)sourceChooser.getCatalogComboBox().
									getSelectedItem()).getName());
					resultChooser.getCatalogComboBox().setSelectedItem(cat);
				} catch (ArchitectException e1) {
					ASUtils.showExceptionDialogNoReport(MatchEditor.this,
							"Unknown Database error", e1);
				}
            }
            if ( resultChooser.getCatalogComboBox().isEnabled() &&
            		resultChooser.getCatalogComboBox().getSelectedItem() != null ) {
                plMatch.setResultsTableCatalog(
                		((SQLCatalog)resultChooser.getCatalogComboBox().getSelectedItem()).getName());
            }

            if ( resultChooser.getSchemaComboBox().isEnabled() &&
            		resultChooser.getSchemaComboBox().getSelectedItem() == null ) {
            	SQLSchema resultSchema = null;

            	SQLDatabase db = resultChooser.getDb();
            	SQLSchema sourceSchema =
            		(SQLSchema) sourceChooser.getSchemaComboBox().getSelectedItem();
            	SQLCatalog sourceCatalog =
            		(SQLCatalog) sourceChooser.getCatalogComboBox().getSelectedItem();
            	try {
					if ( db.isSchemaContainer() ) {
						resultSchema = db.getSchemaByName(sourceSchema.getName());
					} else {
						SQLCatalog cat = db.getCatalogByName(sourceCatalog.getName());
						resultSchema = cat.getSchemaByName(sourceSchema.getName());
					}
				} catch (ArchitectException e1) {
					ASUtils.showExceptionDialogNoReport(MatchEditor.this,
							"Unknown Database error", e1);
				}
            	resultChooser.getSchemaComboBox().setSelectedItem(resultSchema);
            }

            if ( resultChooser.getSchemaComboBox().isEnabled() &&
            		resultChooser.getSchemaComboBox().getSelectedItem() != null ) {
                plMatch.setResultsTableOwner(
                		((SQLSchema)resultChooser.getSchemaComboBox().getSelectedItem()).getName());
            }

            String trimedValue = null;
            String resultTable = resultTableName.getText();
            if ( resultTable != null ) {
                trimedValue = resultTable.trim();
            }
            if ( trimedValue == null || trimedValue.length() == 0 ) {
                resultTableName.setText("MM_"+plMatch.getMatchId());
            }


            plMatch.setResultsTable(resultTableName.getText());
            plMatch.setLastUpdateDate(new Date(System.currentTimeMillis()));

       //FIXME get around the foldersaving problem
       //     PlFolder f = (PlFolder)folderList.getSelectedItem();
       //     Set<PlFolder> f2 = new TreeSet<PlFolder>();
       //     f2.add(f);
       //     plMatch.setFolders( f2);

            Transaction tx = HibernateUtil.primarySession().beginTransaction();
            HibernateUtil.primarySession().saveOrUpdate(plMatch);
			HibernateUtil.primarySession().flush();
			tx.commit();
			HibernateUtil.primarySession().refresh(plMatch);
			HibernateUtil.primarySession().flush();

            JOptionPane.showMessageDialog(MatchEditor.this,
                    "Match Interface Save Successfully",
                    "Saved",JOptionPane.INFORMATION_MESSAGE);

		}};

	private Action editMatchCriteria = new AbstractAction("Match Criteria") {
		public void actionPerformed(ActionEvent e) {
			logger.debug("Edit Match Criteria for: " + plMatch.getMatchId());
			// TODO:
		}};

	private Action showAuditInfoAction = new AbstractAction("Show Audit Info") {
		public void actionPerformed(ActionEvent e) {

			MatchInfoPanel p = new MatchInfoPanel(plMatch);
			JDialog d = ArchitectPanelBuilder.createSingleButtonArchitectPanelDialog(
					p,MatchEditor.this,
					"Audit Information","OK");
			d.pack();
			d.setVisible(true);
		}};

	private Action runMatchAction = new AbstractAction("Run Match") {
		public void actionPerformed(ActionEvent e) {
			RunMatchPanel p = new RunMatchPanel(plMatch);
			p.pack();
			p.setVisible(true);
		}};

	private Action validationStatusAction = new AbstractAction("View Validation Status") {
		public void actionPerformed(ActionEvent e) {
			MatchValidationStatus p = new MatchValidationStatus(plMatch,MatchEditor.this);
			p.pack();
			p.setVisible(true);
		}};
	private Action validateMatchAction = new AbstractAction("Validate Match") {
		public void actionPerformed(ActionEvent e) {
			try {
				MatchValidation v = new MatchValidation(plMatch);
				v.pack();
				v.setVisible(true);
			} catch (HeadlessException e1) {
				ASUtils.showExceptionDialog(MatchEditor.this,"Unknown Error",e1);
			} catch (SQLException e1) {
				ASUtils.showExceptionDialog(MatchEditor.this,"Unknown SQL Error",e1);
			} catch (ArchitectException e1) {
				ASUtils.showExceptionDialog(MatchEditor.this,"Unknown Error",e1);
			}
		}};
	private Action viewBuilderAction = new AbstractAction("View Builder") {
		public void actionPerformed(ActionEvent e) {
			// TODO:
		}};

	private Action createResultTableAction = new AbstractAction("Create Table") {
		public void actionPerformed(ActionEvent e) {
			// TODO:
		}};

	private JSplitPane splitPane;









    private void buildUI() throws ArchitectException {

    	final MatchMakerFrame mainFrame = MatchMakerFrame.getMainInstance();
		sourceChooser = new SQLObjectChooser(MatchEditor.this,
        		mainFrame.getUserSettings().getConnections());
        xrefChooser = new SQLObjectChooser(MatchEditor.this,
        		mainFrame.getUserSettings().getConnections());
        resultChooser = new SQLObjectChooser(MatchEditor.this,
        		mainFrame.getUserSettings().getConnections());


        final SQLDatabase loginDB = mainFrame.getDatabase();
        ArchitectDataSource ds;
        if ( loginDB != null ) {
        	PlDotIni ini = mainFrame.getUserSettings().getPlDotIni();
        	ds = ini.getDataSource(loginDB.getDataSource().getName());
        	sourceChooser.getDataSourceComboBox().setSelectedItem(ds);
        	resultChooser.getDataSourceComboBox().setSelectedItem(ds);
        	// no connection no folders
        	folderComboBox.setModel(
        			new FolderComboBoxModel<PlFolder>(mainFrame.getFolders()));
        }
        
        filterPanel = new FilterComponentsPanel();
        
        PropertyChangeListener pcl = new PropertyChangeListener(){

			public void propertyChange(PropertyChangeEvent evt) {
				String property = evt.getPropertyName();
				if (property.equals("tableCatalog") || property.equals("tableOwner") ||property.equals("matchTable") ){
					try {
						filterPanel.setTable(loginDB.getTableByName(plMatch.getTableCatalog(), plMatch.getTableOwner(), plMatch.getMatchTable()));
					} catch (ArchitectException e) {
						throw new ArchitectRuntimeException(e);
					}
				}

			}

        };

    	List<String> types = new ArrayList<String>();
    	for ( MatchType mt : MatchType.values() ) {
    		types.add(mt.getName());
    	}
    	type.setModel(new DefaultComboBoxModel(types.toArray()));

        sourceChooser.getTableComboBox().addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				filterPanel.getFilterTextArea().setText("");
			}});

    	viewBuilder = new JButton(viewBuilderAction);
    	createResultTable = new JButton(createResultTableAction);

    	saveMatch = new JButton(saveAction);
    	//exitEditor = new JButton(exitAction);

    	showAuditInfo = new JButton(showAuditInfoAction);
    	runMatch= new JButton(runMatchAction);
    	validationStatus = new JButton(validationStatusAction);
    	validateMatch = new JButton(validateMatchAction);


    	if ( plMatch != null ) {
            if (plMatch.getMatchTable() != null) {
            	SQLTable tableByName = loginDB.getTableByName(plMatch.getTableCatalog(), plMatch.getTableOwner(), plMatch.getMatchTable());
            	if (tableByName != null) {
            		filterPanel.setTable(tableByName);
            	}
            }

    		matchId.setText(plMatch.getMatchId());
    		if ( plMatch.getFolder() != null) {
    			PlFolder f = (PlFolder) plMatch.getFolder();
	    		if ( f != null ) {
	    			folderComboBox.setSelectedItem(f);
	    		}
    		}
    		desc.setText(plMatch.getMatchDesc());
    		type.setSelectedItem(plMatch.getMatchType());

    		SQLTable table = null;
			try {
				SQLDatabase db = sourceChooser.getDb();
				if ( db != null && plMatch.getMatchTable() != null) {
					table = db.getTableByName(
							plMatch.getTableCatalog(),
							plMatch.getTableOwner(),
							plMatch.getMatchTable());
				}
			} catch (ArchitectException e2) {
				ASUtils.showExceptionDialogNoReport(
						MatchEditor.this,
						"Unable to read Source Table"+
						DDLUtils.toQualifiedName(
								plMatch.getTableCatalog(),
								plMatch.getTableOwner(),
								plMatch.getMatchTable())+
						" from Database!", e2 );
			}

    		if ( table == null ) {
    			JOptionPane.showMessageDialog(MatchEditor.this,
    					"Table [" + DDLUtils.toQualifiedName(
    							plMatch.getTableCatalog(),
    		    				plMatch.getTableOwner(),
    		    				plMatch.getMatchTable()) + "]" +
    					" Has been removed.",
    					"Source table not found!",
    					JOptionPane.INFORMATION_MESSAGE );
    		} else {
	    		SQLCatalog cat = table.getCatalog();
	    		SQLSchema sch = table.getSchema();
	    		if ( cat != null ) {
	    			sourceChooser.getCatalogComboBox().setSelectedItem(cat);
	    		}
	    		if ( sch != null ) {
	    			sourceChooser.getSchemaComboBox().setSelectedItem(sch);
	    		}
	    		sourceChooser.getTableComboBox().setSelectedItem(table);

	    		String pkName = plMatch.getPkColumn();
	    		if (  pkName != null && pkName.length() > 0 ) {
	    			SQLIndex pk = null;
					try {
						pk = table.getIndexByName(pkName);
					} catch (ArchitectException e1) {
						ASUtils.showExceptionDialogNoReport(
								MatchEditor.this,
								"Unable to get table unique indices!", e1 );
					}
	    			if ( pk != null ) {
	    				sourceChooser.getUniqueKeyComboBox().setSelectedItem(pk);
	    			}
	    		}
    		}

            filterPanel.getFilterTextArea().setText(plMatch.getFilter());

            SQLTable resultTable = null;
			try {
				SQLDatabase db = resultChooser.getDb();
				if ( db != null && plMatch.getResultsTable() != null) {
					resultTable = db.getTableByName(
							plMatch.getResultsTableCatalog(),
							plMatch.getResultsTableOwner(),
							plMatch.getResultsTable());
				}
			} catch (ArchitectException e1) {
				ASUtils.showExceptionDialogNoReport(
						MatchEditor.this,
						"Unable to read Result Table"+
						DDLUtils.toQualifiedName(
								plMatch.getResultsTableCatalog(),
								plMatch.getResultsTableOwner(),
								plMatch.getResultsTable())+
						" from Database!", e1 );
			}

			if ( resultTable != null ) {
	            SQLCatalog cat = resultTable.getCatalog();
	    		SQLSchema sch = resultTable.getSchema();
	    		if ( cat != null ) {
	    			resultChooser.getCatalogComboBox().setSelectedItem(cat);
	    		}
	    		if ( sch != null ) {
	    			resultChooser.getSchemaComboBox().setSelectedItem(sch);
	    		}
			}
            resultTableName.setText(plMatch.getResultsTable());

    	} else if ( plFolder != null ) {
                folderComboBox.setSelectedItem(plFolder);
        }


    	FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,fill:min(pref;"+new JComboBox().getMinimumSize().width+"px):grow, 4dlu,pref,10dlu, pref,4dlu", // columns
				"10dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,   16dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,   4dlu,pref,  16dlu,pref,4dlu,pref,4dlu,pref,10dlu"); // rows
    	//		 1     2     3    4     5    6     7    8        9     10    11   12    13   14    15   16       17    18     19    20    21   22    23   24    25

		PanelBuilder pb;

		JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
		pb = new PanelBuilder(layout, p);
		CellConstraints cc = new CellConstraints();

		pb.add(new JLabel("Match ID:"), cc.xy(2,2,"r,c"));
		pb.add(new JLabel("Folder:"), cc.xy(2,4,"r,c"));
		pb.add(new JLabel("Description:"), cc.xy(2,6,"r,t"));
		pb.add(new JLabel("Type:"), cc.xy(2,8,"r,c"));

		pb.add(matchId, cc.xy(4,2));
		pb.add(folderComboBox, cc.xy(4,4));
		pb.add(new JScrollPane(desc), cc.xy(4,6,"f,f"));
		pb.add(type, cc.xy(4,8));

		pb.add(sourceChooser.getCatalogTerm(), cc.xy(2,10,"r,c"));
		pb.add(sourceChooser.getSchemaTerm(), cc.xy(2,12,"r,c"));
		pb.add(new JLabel("Table Name:"), cc.xy(2,14,"r,c"));
		pb.add(new JLabel("Unique Index:"), cc.xy(2,16,"r,t"));
		pb.add(new JLabel("Filter:"), cc.xy(2,18,"r,t"));

		pb.add(sourceChooser.getCatalogComboBox(), cc.xy(4,10));
		pb.add(sourceChooser.getSchemaComboBox(), cc.xy(4,12));
		pb.add(sourceChooser.getTableComboBox(), cc.xy(4,14));
		pb.add(sourceChooser.getUniqueKeyComboBox(), cc.xy(4,16,"f,f"));
		pb.add(filterPanel, cc.xyw(4,18,3,"f,f"));

		pb.add(resultChooser.getCatalogTerm(), cc.xy(2,20,"r,c"));
		pb.add(resultChooser.getSchemaTerm(), cc.xy(2,22,"r,c"));
		pb.add(new JLabel("Table Name:"), cc.xy(2,24,"r,c"));

		pb.add(resultChooser.getCatalogComboBox(), cc.xy(4,20));
		pb.add(resultChooser.getSchemaComboBox(), cc.xy(4,22));
		pb.add(resultTableName, cc.xy(4,24));



		pb.add(viewBuilder, cc.xy(6,10,"f,f"));
		pb.add(createResultTable, cc.xywh(6,20,1,3));

		ButtonStackBuilder bb = new ButtonStackBuilder();
		bb.addGridded(saveMatch);
		bb.addRelatedGap();
		bb.addRelatedGap();
		bb.addGridded(new JButton(new NewMatchGroupAction(plMatch,splitPane)));
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



		pb.add(bb.getPanel(), cc.xywh(8,2,1,14,"f,f"));
		//pb.add(exitEditor,cc.xywh(8,18,1,2));
		panel = pb.getPanel();
		getContentPane().add(panel);

    }


    /**
	 * Finds all the children of a catalog and puts them in the GUI.
	 */
	public class CatalogPopulator implements ActionListener {

		private JComboBox catalogComboBox;
		private JComboBox schemaComboBox;
		private JLabel schemaLabel;
		private JLabel catalogLabel;
		private JComboBox tableComboBox;

		public CatalogPopulator(
				JComboBox catalogComboBox,
				JComboBox schemaComboBox,
				JComboBox tableComboBox,
				JLabel catalogLabel,
				JLabel schemaLabel ) {
			this.catalogComboBox = catalogComboBox;
			this.schemaComboBox = schemaComboBox;
			this.tableComboBox = tableComboBox;
			this.catalogLabel = catalogLabel;
			this.schemaLabel = schemaLabel;
		}

		/**
		 * Clears the schema dropdown, and start to
		 * repopulate it (if possible).
		 */
		public void actionPerformed(ActionEvent e) {
			logger.debug("CATALOG POPULATOR IS ABOUT TO START...");
			catalogComboBox.removeAllItems();
			catalogComboBox.setEnabled(false);
			catalogLabel.setText("");

			final SQLDatabase db = MatchMakerFrame.getMainInstance().getDatabase();

			try {
				if (db.isCatalogContainer()) {
					for (SQLObject item : (List<SQLObject>) db.getChildren()) {
						catalogComboBox.addItem(item);
					}
					if ( catalogComboBox.getItemCount() > 0 &&
							catalogComboBox.getSelectedIndex() < 0 ) {
						catalogComboBox.setSelectedIndex(0);
					}
					// check if we need to do schemas
					SQLCatalog cat = (SQLCatalog) catalogComboBox.getSelectedItem();
					if ( cat != null && cat.getNativeTerm() !=null )
						catalogLabel.setText(cat.getNativeTerm());
					if (cat == null) {
						// there are no catalogs (database is completely empty)
						catalogComboBox.setEnabled(false);
					}  else {
						// there are catalogs, but they don't contain schemas
						catalogComboBox.setEnabled(true);
					}
				} else if (db.isSchemaContainer()) {

					catalogComboBox.setEnabled(false);
					schemaComboBox.removeAllItems();
					schemaLabel.setText("");

					for (SQLObject item : (List<SQLObject>) db.getChildren()) {
						schemaComboBox.addItem(item);
					}
					if ( schemaComboBox.getItemCount() > 0 &&
							schemaComboBox.getSelectedIndex() < 0 ) {
						schemaComboBox.setSelectedIndex(0);
					}
					SQLSchema sch = (SQLSchema) schemaComboBox.getSelectedItem();
					if ( sch != null && sch.getNativeTerm() !=null )
						schemaLabel.setText(sch.getNativeTerm());
					if (sch == null) {
						// there are no schema (database is completely empty)
						schemaComboBox.setEnabled(false);
					}  else {
						// there are catalogs, but they don't contain schemas
						schemaComboBox.setEnabled(true);
					}
				} else {
					// database contains tables directly
					catalogComboBox.setEnabled(false);
					schemaComboBox.setEnabled(false);
					tableComboBox.removeAllItems();

					for (SQLObject item : (List<SQLObject>) db.getChildren()) {
						tableComboBox.addItem(item);
					}
				}
			} catch ( ArchitectException e1 ) {
				ASUtils.showExceptionDialog(MatchEditor.this,
						"Database Error", e1);
			}
		}
	}


    /**
	 * Finds all the children of a catalog and puts them in the GUI.
	 */
	public class SchemaPopulator implements ActionListener {

		private JComboBox catalogComboBox;
		private JComboBox schemaComboBox;
		private JLabel schemaLabel;

		public SchemaPopulator(JComboBox catalogComboBox,
				JComboBox schemaComboBox,
				JLabel schemaLabel ) {
			this.catalogComboBox = catalogComboBox;
			this.schemaComboBox = schemaComboBox;
			this.schemaLabel = schemaLabel;
		}

		/**
		 * Clears the schema dropdown, and start to
		 * repopulate it (if possible).
		 */
		public void actionPerformed(ActionEvent e) {
			logger.debug("SCHEMA POPULATOR IS ABOUT TO START...");
			schemaComboBox.removeAllItems();
			schemaComboBox.setEnabled(false);
			schemaLabel.setText("");

			SQLCatalog catToPopulate = (SQLCatalog) catalogComboBox.getSelectedItem();
			if (catToPopulate != null) {
				logger.debug("SCHEMA POPULATOR IS STARTED...");
				try {
					// this might take a while
					catToPopulate.getChildren();
					if ( catToPopulate.isSchemaContainer() ) {
						for (SQLObject item : (List<SQLObject>) catToPopulate
								.getChildren()) {
							schemaComboBox.addItem(item);
						}
						if (schemaComboBox.getItemCount() > 0) {
							schemaComboBox.setEnabled(true);
							if ( ((SQLSchema)(catToPopulate.getChild(0))).getNativeTerm() != null ) {
								schemaLabel.setText(
										((SQLSchema)(catToPopulate.getChild(0))).getNativeTerm());
							}
						}
					}
				} catch (ArchitectException e1) {
					ASUtils.showExceptionDialog(MatchEditor.this,
							"Database Error", e1);
				}

			}
		}
	}


	public JPanel getPanel() {
		return panel;
	}

	public void setPanel(JPanel panel) {
		if (this.panel != panel) {
			this.panel = panel;
			//TODO fire event
		}
	}
}