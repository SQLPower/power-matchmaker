package ca.sqlpower.matchmaker.swingui;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import ca.sqlpower.architect.swingui.ASUtils;
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

    private JTextField matchId;
    private JComboBox folderList;
    private JTextArea desc;
    private JComboBox type;
    private JComboBox sourceTableOwner;
    private JComboBox sourceTableName;
    private JComboBox uniqueIndex;

    private JTextArea filter;
    private JComboBox resultTableOwner;
    private JTextField resultTableName;
    private JButton viewBuilder;
    private JButton editFilter;
    private JButton createResultTable;

    private JButton saveMatch;
    private JButton exitEditor;
    private JButton matchCriteria;
    private JButton showAuditInfo;
    private JButton runMatch;
    private JButton validationStatus;
    private JButton validateMatch;

	private JLabel matchSourceTableOwnerLabel;
	private JLabel matchSourceTableNameLabel;

	private PlMatch plMatch;
    private PlFolder plFolder;


    public MatchEditor(PlMatch match) throws HeadlessException {
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
            plMatch.setFilter(filter.getText());
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
            
            PlFolder f = (PlFolder)folderList.getSelectedItem();
            Set f2 = new TreeSet<PlFolder>();
            f2.add(f);
            plMatch.setFolders( f2);

            HibernateUtil.primarySession().save(plMatch);
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
			// TODO:
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

    	List types = new ArrayList();
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
        filter = new JTextArea();
        sourceTableName.addItemListener(new TableNameListChangeListener(
                sourceTableName,
                uniqueIndex,
                filter ));
    	resultTableOwner = new JComboBox(new DefaultComboBoxModel(tablePath.toArray()));
    	resultTableName = new JTextField();
    	viewBuilder = new JButton(viewBuilderAction);
    	editFilter = new JButton(editFilterAction);
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
    		PlFolder f = (PlFolder) plMatch.getFolders().toArray()[0];
    		if ( f != null ) {
    			folderList.setSelectedItem(f);
    		}
    		desc.setText(plMatch.getMatchDesc());
    		type.setSelectedItem(plMatch.getMatchType());
            sourceTableOwner.setSelectedItem(plMatch.getTableOwner());
            MySimpleTable t = MatchMakerFrame.getMainInstance().getTable(plMatch.getTableOwner(),plMatch.getMatchTable());
            sourceTableName.setSelectedItem(t);
            
            System.out.println("pk column:"+plMatch.getPkColumn());

            filter.setText(plMatch.getFilter());
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
				"10dlu,12dlu,4dlu,12dlu,4dlu,24dlu,4dlu,12dlu,   16dlu,12dlu,4dlu,12dlu,4dlu,12dlu,4dlu,24dlu,   16dlu,12dlu,4dlu,12dlu,10dlu"); // rows

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
		pb.add(new JLabel("Filter:"), cc.xy(2,16,"r,t"));

		pb.add(sourceTableOwner, cc.xy(4,10));
		pb.add(sourceTableName, cc.xy(4,12));
		pb.add(uniqueIndex, cc.xy(4,14,"f,f"));
		pb.add(new JScrollPane(filter), cc.xy(4,16,"f,f"));

		pb.add(new JLabel("Result Table Owner:"), cc.xy(2,18,"r,c"));
		pb.add(new JLabel("Table Name:"), cc.xy(2,20,"r,c"));
		pb.add(resultTableOwner, cc.xy(4,18));
		pb.add(resultTableName, cc.xy(4,20));


		pb.add(viewBuilder, cc.xy(6,10));
		pb.add(editFilter, cc.xy(6,16));
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

    public static void main(String[] args) {

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
}
