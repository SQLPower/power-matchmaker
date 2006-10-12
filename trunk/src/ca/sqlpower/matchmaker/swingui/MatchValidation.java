package ca.sqlpower.matchmaker.swingui;

import java.awt.HeadlessException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.sql.RowSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.util.HibernateUtil;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.rowset.CachedRowSetImpl;

public class MatchValidation extends JFrame {

	private static final Logger logger = Logger.getLogger(MatchValidation.class);

	private PlMatch match;
	private RowSet fullResult;
	private Set<String> allMatchGroup;
	private Set<String> allMatchPct;
	private Set<BigDecimal> allMatchPctThreshold;
	private Set<String> allMatchStatus;

	public MatchValidation(PlMatch match) throws HeadlessException, SQLException {
		super("Validate Matches: ["+match.getMatchId()+"]");
		this.match = match;
		setup();
		buildUI();
	}

	private RowSet getMatchResult(PlMatch match) throws SQLException {
    	Connection con = null;
    	PreparedStatement pstmt = null;
    	ResultSet rs =  null;
    	try {
    		con = HibernateUtil.primarySession().connection();
    		StringBuffer sql = new StringBuffer();
    		sql.append("SELECT * FROM ");
    		sql.append(DDLUtils.toQualifiedName(
    				match.getResultsTableCatalog(),
    				match.getResultsTableOwner(),
    				match.getResultsTable()));
    		pstmt = con.prepareStatement(sql.toString());
    		rs = pstmt.executeQuery();
    		CachedRowSetImpl crset = new CachedRowSetImpl();
    		crset.populate(rs);
    		return crset;
    	} finally {
    		if ( rs != null )
    			rs.close();
    		if ( pstmt != null )
    			pstmt.close();
    		if (con != null)
    			con.close();
    	}
    }

	private void setup() throws SQLException {
		fullResult = getMatchResult(match);
		allMatchGroup = new HashSet<String>();
		allMatchPct = new HashSet<String>();
		allMatchPctThreshold = new HashSet<BigDecimal>();
		allMatchStatus = new HashSet<String>();

		allMatchGroup.add("All");
		allMatchPct.add("All");
		allMatchStatus.add("All");
		allMatchStatus.add("unmatched");
		allMatchStatus.add("AUTO-MATCH");
		allMatchStatus.add("MATCH");
		allMatchStatus.add("NOMATCH");
		allMatchStatus.add("MERGED");


		while ( fullResult.next() ) {
			allMatchGroup.add(fullResult.getString("GROUP_ID"));
			allMatchPct.add(fullResult.getBigDecimal("MATCH_PERCENT").toPlainString());
			allMatchPctThreshold.add(fullResult.getBigDecimal("MATCH_PERCENT"));
			allMatchStatus.add(fullResult.getString("MATCH_STATUS"));
		}

	}

	private void buildUI() {

		// right hand panel
		FormLayout layout = new FormLayout(
				"4dlu,fill:80dlu:grow,4dlu,fill:80dlu, 4dlu",
				//1    2               3    4           5
		"4dlu,12dlu,4dlu,12dlu,4dlu,12dlu,4dlu,12dlu,4dlu,12dlu,4dlu,12dlu,4dlu,12dlu,4dlu,12dlu,4dlu,12dlu,4dlu,12dlu,4dlu,12dlu,4dlu,12dlu");
		//		 1    2     3    4     5    6     7    8     9    10    11   12    13   13    15   16    17   18    19   20    21   22    23   24
		JPanel rightPanel = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
		PanelBuilder pb = new PanelBuilder(layout, rightPanel );
		CellConstraints cc = new CellConstraints();

		pb.add(new JLabel("Auto-Match Threshold:"), cc.xy(2,2,"r,c"));
		pb.add(new JComboBox(new DefaultComboBoxModel(allMatchPctThreshold.toArray())),
				cc.xy(2,4,"r,c"));
		pb.add(new JButton("Apply"), cc.xy(2,6,"r,c"));
		pb.add(new JButton("Reset"), cc.xy(4,6,"r,c"));

		pb.add(new JLabel("How To Search:"), cc.xy(2,8,"r,c"));







		layout = new FormLayout(
                "4dlu,fill:500dlu:grow,4dlu,fill:200dlu, 4dlu",
               //1    2                3    4            5
                "4dlu,12dlu,4dlu,fill:400dlu:grow,4dlu,12dlu,4dlu, 200dlu,4dlu, 12dlu,4dlu");
        //		 1    2     3    4     5    6     7    8     9     10     11    12    13
        JPanel topPanel = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
        pb = new PanelBuilder(layout, topPanel);
        cc = new CellConstraints();






        pb.add(rightPanel, cc.xywh(4,2,1,11,"f,f"));
        getContentPane().add(pb.getPanel());
	}
}
