package ca.sqlpower.matchmaker.swingui;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import javax.sql.RowSet;
import javax.sql.rowset.JoinRowSet;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.swingui.table.DateTableCellRenderer;
import ca.sqlpower.architect.swingui.table.NumberAndIntegerTableCellRenderer;
import ca.sqlpower.architect.swingui.table.TableModelColumnAutofit;
import ca.sqlpower.matchmaker.RowSetModel;
import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.util.HibernateUtil;

import com.sun.rowset.CachedRowSetImpl;
import com.sun.rowset.JoinRowSetImpl;

public class MatchStatisticsPanel extends JPanel {

	private PlMatch match;

	public MatchStatisticsPanel(PlMatch match) throws SQLException {
		super();
		this.match = match;
		createUI();
	}

	private void createUI() throws SQLException {

		RowSet rs = getMatchStats(match);
		RowSetModel rsm = new RowSetModel(rs);

		JTable table = new JTable(rsm);
		JTableHeader header = table.getTableHeader();
		header.getColumnModel().getColumn(0).setHeaderValue("Run #");
		header.getColumnModel().getColumn(1).setHeaderValue("Start Time");
		header.getColumnModel().getColumn(2).setHeaderValue("Elapsed(sec)");
		header.getColumnModel().getColumn(3).setHeaderValue("Status");
		header.getColumnModel().getColumn(4).setHeaderValue("Total Found");
		header.getColumnModel().getColumn(5).setHeaderValue("Added");
		header.getColumnModel().getColumn(6).setHeaderValue("Updated");
		header.getColumnModel().getColumn(7).setHeaderValue("Total");
		header.getColumnModel().getColumn(8).setHeaderValue("Overall(current)");
		header.getColumnModel().getColumn(9).setHeaderValue("Job Run #");
		header.getColumnModel().getColumn(10).setHeaderValue("Rollback Seg");

		TableColumnModel cm = table.getColumnModel();
        for (int col = 0; col < cm.getColumnCount(); col++) {
            TableColumn tc = cm.getColumn(col);
            if ( Date.class.isAssignableFrom(table.getColumnClass(col)) ) {
            	tc.setCellRenderer(new DateTableCellRenderer());
            } else if ( Number.class.isAssignableFrom(table.getColumnClass(col)) ) {
            	tc.setCellRenderer(new NumberAndIntegerTableCellRenderer());
            }
        }
        TableModelColumnAutofit columnAutoFit =
            new TableModelColumnAutofit(rsm, table);
        columnAutoFit.setTableHeader(header);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);


		JScrollPane scroller = new JScrollPane(table);

		JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitter.setDividerLocation(.5);
		splitter.setLeftComponent(scroller);

		int latestRunNo = 0;
		if ( table.getRowCount() > 0 ) {
			latestRunNo = ((BigDecimal) table.getValueAt(0,0)).intValue();
		}





		RowSet rsGroup = getMatchGroupStats(match,latestRunNo);
		RowSetModel rsmGroup = new RowSetModel(rsGroup);
		JTable tableGroup = new JTable(rsmGroup);
		header = tableGroup.getTableHeader();

		header.getColumnModel().getColumn(0).setHeaderValue("Group");
		header.getColumnModel().getColumn(1).setHeaderValue("Match Percent");
		header.getColumnModel().getColumn(2).setHeaderValue("Added");
		header.getColumnModel().getColumn(3).setHeaderValue("Update");
		header.getColumnModel().getColumn(4).setHeaderValue("Total");
		header.getColumnModel().getColumn(5).setHeaderValue("Overall");
		header.getColumnModel().getColumn(6).setHeaderValue("Processed");

		cm = tableGroup.getColumnModel();
		for (int col = 0; col < cm.getColumnCount(); col++) {
			TableColumn tc = cm.getColumn(col);
			if ( Date.class.isAssignableFrom(tableGroup.getColumnClass(col)) ) {
				tc.setCellRenderer(new DateTableCellRenderer());
			} else if ( Number.class.isAssignableFrom(tableGroup.getColumnClass(col)) ) {
				tc.setCellRenderer(new NumberAndIntegerTableCellRenderer());
			}
		}
		TableModelColumnAutofit columnAutoFitGroup =
			new TableModelColumnAutofit(rsmGroup, tableGroup);
		columnAutoFitGroup.setTableHeader(header);
		tableGroup.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JScrollPane scroller2 = new JScrollPane(tableGroup);
		splitter.setRightComponent(scroller2);

		add(splitter);

		MatchStatisticsMouseListener mouseListener = new MatchStatisticsMouseListener(table,tableGroup);
		table.addMouseListener(mouseListener);

	}

	public RowSet getMatchStats(PlMatch match) throws SQLException {
    	Connection con = null;
    	Statement stmt = null;
    	ResultSet rs =  null;
    	try {
    		con = HibernateUtil.primarySession().connection();
    		StringBuffer sql = new StringBuffer();
    		sql.append("SELECT TRANS_RUN_NO,START_DATE_TIME,ELAPSED_TIME");
    		sql.append(",RUN_STATUS,NO_OF_REC_READ,NO_OF_REC_ADDED");
    		sql.append(",NO_OF_REC_UPDATED,NO_OF_REC_TOTAL,NO_OF_REC_PROCESSED");
    		sql.append(",RUN_NO,ROLLBACK_SEGMENT_NAME");
    		sql.append(" FROM PL_STATS WHERE OBJECT_TYPE=? ");
    		sql.append(" AND OBJECT_NAME=? ");
    		sql.append(" ORDER BY TRANS_RUN_NO DESC, START_DATE_TIME DESC");
    		PreparedStatement pstmt = con.prepareStatement(sql.toString());
    		pstmt.setString(1, "MATCH");
    		pstmt.setString(2, match.getMatchId());
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

	public RowSet getMatchGroupStats(PlMatch match, int runNo) throws SQLException {
    	Connection con = null;
    	PreparedStatement pstmt = null;
    	ResultSet rs =  null;
    	try {
    		con = HibernateUtil.primarySession().connection();

    		StringBuffer sql = new StringBuffer();
    		sql.append("SELECT OBJECT_NAME,NO_OF_REC_ADDED, NO_OF_REC_UPDATED");
    		sql.append(",NO_OF_REC_TOTAL, NO_OF_REC_READ,NO_OF_REC_PROCESSED");
    		sql.append(" FROM PL_STATS WHERE OBJECT_TYPE=? and TRANS_RUN_NO=?");
    		pstmt = con.prepareStatement(sql.toString());
    		pstmt.setString(1, "MATCHGROUP");
    		pstmt.setInt(2, runNo);
    		rs = pstmt.executeQuery();
    		CachedRowSetImpl crset = new CachedRowSetImpl();
    		crset.setReadOnly(true);
    		crset.populate(rs);
    		crset.setMatchColumn(1);

    		rs.close();
    		pstmt.close();

    		sql = new StringBuffer();
    		sql.append("SELECT GROUP_ID,MATCH_PERCENT FROM PL_MATCH_GROUP WHERE MATCH_ID=?");
    		pstmt = con.prepareStatement(sql.toString());
    		pstmt.setString(1, match.getMatchId());
    		rs = pstmt.executeQuery();
    		CachedRowSetImpl crset2 = new CachedRowSetImpl();
    		crset2.setReadOnly(true);
    		crset2.populate(rs);
    		crset2.setMatchColumn(1);

    		JoinRowSet jrs = new JoinRowSetImpl();
    		jrs.addRowSet(crset2);
    		jrs.addRowSet(crset);

    		ResultSetMetaData m = jrs.getMetaData();
    		for ( int i=0; i<m.getColumnCount(); i++ ) {
    			System.out.println("#"+i+"  "+m.getColumnName(i+1) );
    		}


    		rs.close();
    		pstmt.close();
    		rs = null;
    		pstmt = null;

    		return jrs;
    	} finally {
    		if ( rs != null )
    			rs.close();
    		if ( pstmt != null )
    			pstmt.close();
    		if (con != null)
    			con.close();
    	}
    }

	private class MatchStatisticsMouseListener implements MouseListener {

		private JTable matchTable;
		private JTable matchGroupTable;

		public MatchStatisticsMouseListener(JTable matchTable, JTable matchGroupTable) {
			this.matchTable = matchTable;
			this.matchGroupTable = matchGroupTable;
		}

		public void mouseClicked(MouseEvent evt) {

			Object obj = evt.getSource();
			if ( obj instanceof JTable ) {
				JTable t = (JTable)obj;
				BigDecimal bRunNo = (BigDecimal) t.getValueAt(
						t.getSelectedRow(),
						t.convertColumnIndexToView(0));

				RowSet rsGroup;
				try {
					rsGroup = getMatchGroupStats(match,bRunNo.intValue());
					RowSetModel rsmGroup = new RowSetModel(rsGroup);
					matchGroupTable.setModel(rsmGroup);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

		public void mousePressed(MouseEvent e) {
			// do nothing
		}

		public void mouseReleased(MouseEvent e) {
			// do nothing
		}

		public void mouseEntered(MouseEvent e) {
			// do nothing
		}

		public void mouseExited(MouseEvent e) {
			// do nothing
		}

	}


	public static void main(String[] args) throws SQLException {

		final JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		MatchMakerFrame.getMainInstance();
		ArchitectDataSource ds = MatchMakerFrame.getMainInstance().getUserSettings().getPlDotIni().getDataSource("ARTHUR_TEST");
		MatchMakerFrame.getMainInstance().newLogin(new SQLDatabase(ds));
		final PlMatch match = MatchMakerFrame.getMainInstance().getMatchByName("MATCH_PT_COMPANY");

		final MatchStatisticsPanel panel = new MatchStatisticsPanel(match);

		f.getContentPane().add(panel);
		f.setPreferredSize(new Dimension(800,600));
		f.setTitle("Match Statistics: "+match.getMatchId());


        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                f.pack();
                f.setVisible(true);
            }
        });

    }


}
