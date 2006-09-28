package ca.sqlpower.matchmaker.swingui;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.matchmaker.MatchType;
import ca.sqlpower.matchmaker.MySimpleIndex;
import ca.sqlpower.matchmaker.MySimpleTable;
import ca.sqlpower.matchmaker.hibernate.PlFolder;
import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.util.HibernateUtil;

import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class MatchEditor extends JFrame {

	private static final Logger logger = Logger.getLogger(MatchEditor.class);


	private JComboBox sourceCatalogComboBox;
	private JComboBox sourceSchemaComboBox;
	private JComboBox sourceTableComboBox;

	private JLabel sourceCatalogLabel;
	private JLabel sourceSchemaLabel;

    private JTextField matchId;
    private JComboBox folderList;
    private JTextArea desc;
    private JComboBox type;
    private JComboBox sourceTableOwner;
    private JComboBox sourceTableName;
    private JComboBox uniqueIndex;

    private JComboBox resultTableOwner;
    private JTextField resultTableName;
    private JButton viewBuilder;
    private JButton createResultTable;

    private JButton saveMatch;
    private JButton exitEditor;
    private JButton matchCriteria;
    private JButton showAuditInfo;
    private JButton runMatch;
    private JButton validationStatus;
    private JButton validateMatch;

    private FilterComponentsPanel filterPanel;
	private JLabel matchSourceTableOwnerLabel;
	private JLabel matchSourceTableNameLabel;

	private PlMatch plMatch;
    private PlFolder plFolder;


    public MatchEditor(PlMatch match) throws HeadlessException, ArchitectException {
        this(match,null);
    }

    public MatchEditor(PlMatch match, PlFolder folder2) {
        super();
        if ( match == null ) {
            setTitle("Create new match interface");
        } else {
            setTitle("Edit match interface: "+match.getMatchId());
        }
        this.plMatch = match;
        this.plFolder = folder2;
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

	private Action saveAction = new AbstractAction("Save") {
		public void actionPerformed(ActionEvent e) {

            if ( !checkStringNullOrEmpty(matchId.getText(),"Match ID") )
                return;
            if ( !checkObjectNullOrEmpty(sourceTableOwner.getSelectedItem(),"Source Table Owner") )
                return;
            if ( !checkStringNullOrEmpty((String) sourceTableOwner.getSelectedItem(),"Source Table Owner") )
                return;
            if ( !checkObjectNullOrEmpty(sourceTableName.getSelectedItem(),"Source Table Name") )
                return;
            if ( !checkStringNullOrEmpty(((MySimpleTable) sourceTableName.getSelectedItem()).getName(),"Source Table Name") )
                return;



            if ( plMatch == null ) {
                plMatch = new PlMatch(
                        matchId.getText(),
                        type.getSelectedItem().toString() );
                plMatch.setCreateDate(new Date(System.currentTimeMillis()));
            }
            logger.debug("Saving Match:" + plMatch.getMatchId());

            plMatch.setMatchDesc(desc.getText());
            plMatch.setTableOwner((String) sourceTableOwner.getSelectedItem());
            plMatch.setMatchTable(((MySimpleTable) sourceTableName.getSelectedItem()).getName());
            if ( uniqueIndex.getSelectedItem() != null ) {
                plMatch.setPkColumn(((MySimpleIndex)uniqueIndex.getSelectedItem()).getName());
            }
            plMatch.setFilter(filterPanel.getFilterTextArea().getText());
            if ( resultTableOwner.getSelectedItem() == null ) {
                resultTableOwner.setSelectedItem(sourceTableOwner.getSelectedItem());
            }
            if ( resultTableOwner.getSelectedItem() != null ) {
                plMatch.setResultsTableOwner(resultTableOwner.getSelectedItem().toString());
            }

            String trimedValue = null;
            String resultTable = resultTableName.getText();
            if ( resultTable != null ) {
                trimedValue = resultTable.trim();
            }
            if ( resultTable == null ||
                    trimedValue == null ||
                    trimedValue.length() == 0 ) {
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
			HibernateUtil.primarySession().flush();
			tx.commit();

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
			// TODO:
		}};

	private Action validationStatusAction = new AbstractAction("Validation Status") {
		public void actionPerformed(ActionEvent e) {
			// TODO:
		}};
	private Action validateMatchAction = new AbstractAction("Validate Match") {
		public void actionPerformed(ActionEvent e) {
			// TODO:
		}};
	private Action viewBuilderAction = new AbstractAction("View Builder") {
		public void actionPerformed(ActionEvent e) {
			// TODO:
		}};

	private Action editFilterAction = new AbstractAction("Edit") {
		public void actionPerformed(ActionEvent e) {
			// TODO:
		}};

	private Action createResultTableAction = new AbstractAction("Create Table") {
		public void actionPerformed(ActionEvent e) {
			// TODO:
		}};






    private void buildUI() {



        List <String>tablePath = MatchMakerFrame.getMainInstance().getTablePaths();
        List <MySimpleTable>tableName = MatchMakerFrame.getMainInstance().getTables();
    	matchId = new JTextField();
    	
    	
    	folderList = new JComboBox(new FolderComboBoxModel<PlFolder>(MatchMakerFrame.getMainInstance().getFolders()));
    	desc = new JTextArea();

    	List<String> types = new ArrayList<String>();
    	for ( MatchType mt : MatchType.values() ) {
    		types.add(mt.getName());
    	}
    	type = new JComboBox(new DefaultComboBoxModel(types.toArray()));

    	sourceTableOwner = new JComboBox(new DefaultComboBoxModel(tablePath.toArray()));
    	sourceTableName = new JComboBox(new DefaultComboBoxModel(tableName.toArray()));
        sourceTableOwner.addItemListener(
                new TableOwnerListChangeListener(
                        sourceTableOwner,
                        sourceTableName));
        uniqueIndex = new JComboBox();
        filterPanel = new FilterComponentsPanel();
        sourceTableName.addItemListener(new TableNameListChangeListener(
                sourceTableName,
                uniqueIndex,
                filterPanel.getFilterTextArea() ));
    	resultTableOwner = new JComboBox(new DefaultComboBoxModel(tablePath.toArray()));
    	resultTableName = new JTextField();
    	viewBuilder = new JButton(viewBuilderAction);    	
    	createResultTable = new JButton(createResultTableAction);

    	saveMatch = new JButton(saveAction);
    	exitEditor = new JButton(exitAction);
    	matchCriteria = new JButton(editMatchCriteria);
    	showAuditInfo = new JButton(showAuditInfoAction);
    	runMatch= new JButton(runMatchAction);
    	validationStatus = new JButton(validationStatusAction);
    	validateMatch = new JButton(validateMatchAction);

    	matchSourceTableOwnerLabel = new JLabel("Match Table Owner:");
    	matchSourceTableNameLabel = new JLabel("Table:");

    	if ( plMatch != null ) {
    		matchId.setText(plMatch.getMatchId());
    		if ( plMatch.getFolders() != null && plMatch.getFolders().size() > 0 ) {
    			PlFolder f = (PlFolder) plMatch.getFolders().toArray()[0];
	    		if ( f != null ) {
	    			folderList.setSelectedItem(f);
	    		}
    		}
    		desc.setText(plMatch.getMatchDesc());
    		type.setSelectedItem(plMatch.getMatchType());
            sourceTableOwner.setSelectedItem(plMatch.getTableOwner());
            MySimpleTable t = MatchMakerFrame.getMainInstance().getTable(plMatch.getTableOwner(),plMatch.getMatchTable());
            sourceTableName.setSelectedItem(t);

            System.out.println("pk column:"+plMatch.getPkColumn());

            filterPanel.getFilterTextArea().setText(plMatch.getFilter());
            resultTableOwner.setSelectedItem(plMatch.getResultsTableOwner());
            resultTableName.setText(plMatch.getResultsTable());

            if ( t == null ) {
                JOptionPane.showMessageDialog(MatchEditor.this,
                        "Table [" + plMatch.getTableOwner() + "]"+
                        "[" + plMatch.getMatchTable() + "]" +
                        " Has been removed.",
                        "source table not found!",
                        JOptionPane.INFORMATION_MESSAGE );
            } else {
                MySimpleIndex idx = t.getIndexByName(plMatch.getPkColumn());
                if ( idx != null ) {
                    uniqueIndex.setSelectedItem(idx);
                }
            }
    	} else if ( plFolder != null ) {
                folderList.setSelectedItem(plFolder);
        }

        if ( plMatch == null ) {
            sourceTableOwner.setSelectedItem(null);
            sourceTableName.setSelectedItem(null);
            resultTableOwner.setSelectedItem(null);
        }

    	FormLayout layout = new FormLayout(
				"4dlu,fill:min(70dlu;default),4dlu,fill:200dlu:grow, 4dlu,min(60dlu;default),10dlu, 66dlu,4dlu", // columns
				"10dlu,12dlu,4dlu,12dlu,4dlu,24dlu,4dlu,12dlu,   16dlu,12dlu,4dlu,12dlu,4dlu,12dlu,6dlu,pref,   16dlu,12dlu,4dlu,12dlu,10dlu"); // rows

		PanelBuilder pb;

		JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
		pb = new PanelBuilder(layout, p);
		CellConstraints cc = new CellConstraints();

		pb.add(new JLabel("Match ID:"), cc.xy(2,2,"r,c"));
		pb.add(new JLabel("Folder:"), cc.xy(2,4,"r,c"));
		pb.add(new JLabel("Description:"), cc.xy(2,6,"r,t"));
		pb.add(new JLabel("Type:"), cc.xy(2,8,"r,c"));

		pb.add(matchId, cc.xy(4,2));
		pb.add(folderList, cc.xy(4,4));
		pb.add(new JScrollPane(desc), cc.xy(4,6,"f,f"));
		pb.add(type, cc.xy(4,8));

		pb.add(matchSourceTableOwnerLabel, cc.xy(2,10,"r,c"));
		pb.add(matchSourceTableNameLabel, cc.xy(2,12,"r,c"));
		pb.add(new JLabel("Unique Index:"), cc.xy(2,14,"r,t"));
		

		pb.add(sourceTableOwner, cc.xy(4,10));
		pb.add(sourceTableName, cc.xy(4,12));
		pb.add(uniqueIndex, cc.xy(4,14,"f,f"));
		pb.add(filterPanel, cc.xyw(2,16,5,"f,f"));

		pb.add(new JLabel("Result Table Owner:"), cc.xy(2,18,"r,c"));
		pb.add(new JLabel("Table Name:"), cc.xy(2,20,"r,c"));
		pb.add(resultTableOwner, cc.xy(4,18));
		pb.add(resultTableName, cc.xy(4,20));


		pb.add(viewBuilder, cc.xy(6,10));		
		pb.add(createResultTable, cc.xywh(6,18,1,3));

		ButtonStackBuilder bb = new ButtonStackBuilder();
		bb.addGridded(saveMatch);
		bb.addRelatedGap();
		bb.addRelatedGap();
		bb.addGridded(matchCriteria);
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
		pb.add(exitEditor,cc.xywh(8,18,1,2));

		getContentPane().add(pb.getPanel());

    }

    public static void main(String[] args) throws HeadlessException, ArchitectException {

        MatchMakerFrame.getMainInstance();
        final JFrame f = new MatchEditor(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                f.pack();
                f.setVisible(true);
            }
        });
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
}
