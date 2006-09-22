package ca.sqlpower.matchmaker.swingui;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchType;
import ca.sqlpower.matchmaker.hibernate.PlFolder;
import ca.sqlpower.matchmaker.hibernate.PlMatch;

import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class MatchEditor extends JFrame {

	private static final Logger logger = Logger.getLogger(MatchEditor.class);

    private JTextField matchId;
    private JComboBox folder;
    private JTextArea desc;
    private JComboBox type;
    private JComboBox sourceTableOwner;
    private JComboBox sourceTableName;
    private JTable uniqueIndex;

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
	private PlMatch plMatch;

	private JLabel matchSourceTableOwnerLabel;
	private JLabel matchSourceTableNameLabel;


    public MatchEditor(PlMatch match) throws HeadlessException {
        super();

        if ( match == null ) {
        	setTitle("Create new match interface");
        } else {
        	setTitle("Edit match interface: "+match.getMatchId());
        }
        this.plMatch = match;
        buildUI();
    }


    private Action exitAction = new AbstractAction("Exit") {
		public void actionPerformed(ActionEvent e) {
			setVisible(false);
		}};

	private Action saveAction = new AbstractAction("Save") {
		public void actionPerformed(ActionEvent e) {
			logger.debug("Saving Match:" + plMatch.getMatchId());
			// TODO: save it
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
    	matchId = new JTextField();
    	folder = new JComboBox(new FolderComboBoxModel<PlFolder>(MatchMakerFrame.getMainInstance().getFolders()));
    	desc = new JTextArea();

    	List types = new ArrayList();
    	for ( MatchType mt : MatchType.values() ) {
    		types.add(mt.getName());
    	}
    	type = new JComboBox(new DefaultComboBoxModel(types.toArray()));

    	sourceTableOwner = new JComboBox();
    	sourceTableName = new JComboBox();
    	uniqueIndex = new JTable();

    	filter = new JTextArea();
    	resultTableOwner = new JComboBox();
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
    			folder.setSelectedItem(f);
    		}
    		desc.setText(plMatch.getMatchDesc());
    		System.out.println("match type=["+plMatch.getMatchType()+"]");
    		type.setSelectedItem(plMatch.getMatchType());
    	}

    	FormLayout layout = new FormLayout(
				"4dlu,fill:min(70dlu;default),4dlu,fill:200dlu:grow, 4dlu,min(60dlu;default),10dlu, 66dlu,4dlu", // columns
				"10dlu,12dlu,4dlu,12dlu,4dlu,24dlu,4dlu,12dlu,   16dlu,12dlu,4dlu,12dlu,4dlu,24dlu,4dlu,24dlu,   16dlu,12dlu,4dlu,12dlu,10dlu"); // rows

		PanelBuilder pb;

		JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
		pb = new PanelBuilder(layout, p);
		CellConstraints cc = new CellConstraints();

		pb.add(new JLabel("Match ID:"), cc.xy(2,2,"r,c"));
		pb.add(new JLabel("Folder:"), cc.xy(2,4,"r,c"));
		pb.add(new JLabel("Description:"), cc.xy(2,6,"r,c"));
		pb.add(new JLabel("Type:"), cc.xy(2,8,"r,c"));

		pb.add(matchId, cc.xy(4,2));
		pb.add(folder, cc.xy(4,4));
		pb.add(new JScrollPane(desc), cc.xy(4,6,"f,f"));
		pb.add(type, cc.xy(4,8));

		pb.add(matchSourceTableOwnerLabel, cc.xy(2,10,"r,c"));
		pb.add(matchSourceTableNameLabel, cc.xy(2,12,"r,c"));
		pb.add(new JLabel("Unique Index:"), cc.xy(2,14,"r,c"));
		pb.add(new JLabel("Filter:"), cc.xy(2,16,"r,c"));

		pb.add(sourceTableOwner, cc.xy(4,10));
		pb.add(sourceTableName, cc.xy(4,12));
		pb.add(new JScrollPane(uniqueIndex), cc.xy(4,14,"f,f"));
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
