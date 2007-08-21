package ca.sqlpower.matchmaker.swingui;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.RowSet;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.RowSetModel;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.rowset.CachedRowSetImpl;

/**
 * A dialog box that shows a table of status information about one Match object.
 * If you want to see a status table for a different match, create a new one of these.
 */
public class MatchValidationStatus extends JDialog {

	private static final Logger logger = Logger.getLogger(MatchValidationStatus.class);
	private final Match match;
	private final JTable status = new JTable();
    private final MatchMakerSwingSession swingSession;

	public MatchValidationStatus(MatchMakerSwingSession swingSession, Match match, JFrame frameParent) {
		super(frameParent,"View Match Validation Status");
        this.swingSession = swingSession;
		this.match = match;
		createUI();
	}

    public MatchValidationStatus(MatchMakerSwingSession swingSession, Match match, JDialog dialogParent) {
        super(dialogParent,"View Match Validation Status");
        this.swingSession = swingSession;
        this.match = match;
        createUI();
    }

	public Match getMatch() {
		return match;
	}

	private RowSet getMatchStats(Match match) throws SQLException {
    	Connection con = null;
    	Statement stmt = null;
    	ResultSet rs =  null;

    	try {
    		con = swingSession.getConnection();
    		StringBuffer sql = new StringBuffer();
    		sql.append("SELECT GROUP_ID,MATCH_PERCENT,MATCH_STATUS");
    		sql.append(",COUNT(*)/2");
    		sql.append(" FROM  ");
    		SQLTable resultTable = match.getResultTable();
			sql.append(DDLUtils.toQualifiedName(resultTable.getCatalogName(),
					resultTable.getSchemaName(),
					resultTable.getName()));
    		sql.append(" GROUP BY GROUP_ID,MATCH_PERCENT,MATCH_STATUS");
    		sql.append(" ORDER BY MATCH_PERCENT DESC,MATCH_STATUS");

    		PreparedStatement pstmt = con.prepareStatement(sql.toString());
    		rs = pstmt.executeQuery();
    		CachedRowSetImpl crset = new CachedRowSetImpl();
    		crset.setReadOnly(true);
    		crset.populate(rs);
    		return crset;
    	} finally {
    		if ( rs != null )
    			rs.close();
    		if ( stmt != null )
    			stmt.close();
    		if (con != null)
    			con.close();
    	}
    }
	private class MatchStatsTableModel extends AbstractTableModel {

		private AbstractTableModel model;

		public MatchStatsTableModel(AbstractTableModel model) {
			this.model = model;
		}
		public int getRowCount() {
			return model.getRowCount();
		}

		public int getColumnCount() {
			return model.getColumnCount();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			return model.getValueAt(rowIndex,columnIndex);
		}

		@Override
		public String getColumnName(int column) {
			if ( column == 0 ) {
				return "Group";
			} else if ( column == 1 ) {
				return "Match Percent";
			} else if ( column == 2 ) {
				return "Match Status";
			} else if ( column == 3 ) {
				return "Row Count";
			} else {
				return "unknown column";
			}
		}
	}


	private void createUI() {

		final JComboBox folderComboBox = new JComboBox(swingSession.getCurrentFolderParent().getChildren().toArray());
		folderComboBox.setSelectedItem(match.getParent());
		folderComboBox.setRenderer(new MatchMakerObjectComboBoxCellRenderer());
		List <Match> matches = ((PlFolder)match.getParent()).getChildren();
		DefaultComboBoxModel model = new DefaultComboBoxModel(matches.toArray());
		final JComboBox matchComboBox = new JComboBox(model);
		matchComboBox.setRenderer(new MatchMakerObjectComboBoxCellRenderer());

		folderComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				List<Match> matches = ((PlFolder)match.getParent()).getChildren();
				matchComboBox.removeAllItems();
				for ( Match m : matches ) {
					matchComboBox.addItem(m);
				}
				matchComboBox.setSelectedItem(null);
			}}
		);

		matchComboBox.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				Match selectedMatch = (Match) matchComboBox.getSelectedItem();
				if ( selectedMatch == null ) return;
				RowSetModel rsm = null;
				try {
					rsm = new RowSetModel(getMatchStats(selectedMatch));
					MatchStatsTableModel model = new MatchStatsTableModel(rsm);
					status.setModel(model);
					SQLTable resultTable = selectedMatch.getResultTable();
					status.setName(DDLUtils.toQualifiedName(
							resultTable.getCatalogName(),
							resultTable.getSchemaName(),
							resultTable.getName()));
				} catch (SQLException e1) {
					MMSUtils.showExceptionDialog(MatchValidationStatus.this,
							"Unknown SQL Error", e1);
				}
			}});

		FormLayout layout = new FormLayout(
                "10dlu,fill:pref:grow, 10dlu",
         //		 1    2                  3    4                 5     6     7    8     9     10    11    12   13
                "10dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,10dlu,fill:pref:grow,4dlu,pref,4dlu");
        //		 1     2    3    4    5    6    7    8    9     10             11   12   13
        PanelBuilder pb;
        JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
        pb = new PanelBuilder(layout, p);

        CellConstraints cc = new CellConstraints();

        pb.add(new JLabel("Folder:"), cc.xy(2,2,"l,c"));
		pb.add(folderComboBox, cc.xy(2,4,"l,c"));
        pb.add(new JLabel("Match:"), cc.xy(2,6,"l,c"));
		pb.add(matchComboBox, cc.xy(2,8,"l,c"));

		pb.add(new JScrollPane(status), cc.xy(2,10,"f,f"));

        JButton save = new JButton(new AbstractAction("Save"){
			public void actionPerformed(ActionEvent e) {
				new JTableExporter(MatchValidationStatus.this, status);
			}
		});

        ButtonBarBuilder bb1 = new ButtonBarBuilder();
        bb1.addRelatedGap();
        bb1.addGridded(save);
        bb1.addRelatedGap();
        pb.add(bb1.getPanel(), cc.xy(2,12));

        getContentPane().add(pb.getPanel());
        if ( match != null ) {
        	matchComboBox.setSelectedItem(match);
        }
	}

}
