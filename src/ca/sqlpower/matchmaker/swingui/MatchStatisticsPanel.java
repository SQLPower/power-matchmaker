package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import javax.sql.RowSet;
import javax.sql.rowset.JoinRowSet;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectPanel;
import ca.sqlpower.architect.swingui.table.DateTableCellRenderer;
import ca.sqlpower.architect.swingui.table.IndicatorCellRenderer;
import ca.sqlpower.architect.swingui.table.NumberAndIntegerTableCellRenderer;
import ca.sqlpower.architect.swingui.table.PercentTableCellRenderer;
import ca.sqlpower.architect.swingui.table.TableModelColumnAutofit;
import ca.sqlpower.matchmaker.RowSetModel;
import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.util.HibernateUtil;

import com.sun.rowset.CachedRowSetImpl;
import com.sun.rowset.JoinRowSetImpl;

public class MatchStatisticsPanel extends JPanel implements ArchitectPanel {

	private PlMatch match;

	public MatchStatisticsPanel(PlMatch match) throws SQLException {
		super(new BorderLayout());
		this.match = match;
		createUI();
	}

	private void createUI() throws SQLException {

		RowSet rs = getMatchStats(match);
		RowSetModel rsm = new RowSetModel(rs);

		JTable table = new MatchStatisticTable(rsm);
		JScrollPane scroller = new JScrollPane(table);

		JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitter.setLeftComponent(scroller);

		JTable tableGroup = null;
		if ( table.getRowCount() > 0 && table.getValueAt(0,0) != null ) {
			table.setRowSelectionInterval(0,0);
			RowSet rsGroup = getMatchGroupStats(table);
			RowSetModel rsmGroup = new RowSetModel(rsGroup);
			tableGroup = new MatchGroupStatisticTable(rsmGroup);
		} else {
			tableGroup = new MatchGroupStatisticTable(null);
		}

		JScrollPane scroller2 = new JScrollPane(tableGroup);
		splitter.setRightComponent(scroller2);

		splitter.setDividerLocation(400);
		add(splitter,BorderLayout.CENTER);

		SelectionListener selectionListener = new SelectionListener(table,tableGroup);
		table.getSelectionModel().addListSelectionListener(selectionListener);

		if ( table.getRowCount() > 0 && table.getValueAt(0,0) != null ) {
			table.getSelectionModel().setSelectionInterval(0,0);
		}
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

	public RowSet getMatchGroupStats(PlMatch match, int runNo, int total) throws SQLException {
    	Connection con = null;
    	PreparedStatement pstmt = null;
    	ResultSet rs =  null;
    	try {
    		con = HibernateUtil.primarySession().connection();

    		if ( total == 0 ) {
    			total = 1;
    		}
    		StringBuffer sql = new StringBuffer();

    		sql.append("SELECT GROUP_ID,MATCH_PERCENT FROM PL_MATCH_GROUP WHERE MATCH_ID=? ORDER BY MATCH_PERCENT DESC");
    		pstmt = con.prepareStatement(sql.toString());
    		pstmt.setString(1, match.getMatchId());
    		rs = pstmt.executeQuery();
    		CachedRowSetImpl matchGroupSet = new CachedRowSetImpl();
    		matchGroupSet.setReadOnly(true);
    		matchGroupSet.populate(rs);

    		rs.close();
    		pstmt.close();

    		sql = new StringBuffer();
    		sql.append("SELECT OBJECT_NAME, NO_OF_REC_READ");
    		sql.append(",100*NO_OF_REC_READ/").append(total);
    		sql.append(", NO_OF_REC_ADDED, NO_OF_REC_UPDATED");
    		sql.append(",NO_OF_REC_TOTAL, NO_OF_REC_PROCESSED");
    		sql.append(" FROM PL_STATS WHERE OBJECT_TYPE=? and TRANS_RUN_NO=?");
    		pstmt = con.prepareStatement(sql.toString());
    		pstmt.setString(1, "MATCHGROUP");
    		pstmt.setInt(2, runNo);
    		rs = pstmt.executeQuery();
    		CachedRowSetImpl statsSet = new CachedRowSetImpl();
    		statsSet.setReadOnly(true);
    		statsSet.populate(rs);

    		JoinRowSet jrs = new JoinRowSetImpl();
    		jrs.addRowSet(matchGroupSet,1);
    		jrs.addRowSet(statsSet,1);



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

	public RowSet getMatchGroupStats(JTable t) throws SQLException {

		if ( t.getSelectedRow() == -1 )
			return null;

		int runNo = 0;
		int totalFound = 0;
		BigDecimal bRunNo = (BigDecimal) t.getValueAt(
				t.getSelectedRow(),
				t.convertColumnIndexToView(0));
		BigDecimal bTotalFound = (BigDecimal) t.getValueAt(
				t.getSelectedRow(),
				t.convertColumnIndexToView(4));
		if ( bRunNo != null ) {
			runNo = bRunNo.intValue();
		}
		if ( bTotalFound != null ) {
			totalFound = bTotalFound.intValue();
		}

		return getMatchGroupStats(match,runNo,totalFound);
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


	private class MatchStatisticTable extends JTable {

		public MatchStatisticTable(RowSetModel rsm) {
			super(rsm);

			JTableHeader header = getTableHeader();
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

			TableColumnModel cm = getColumnModel();
	        for (int col = 0; col < cm.getColumnCount(); col++) {
	            TableColumn tc = cm.getColumn(col);
	            if ( col == 3 ) {
	            	tc.setCellRenderer(new IndicatorCellRenderer());
	            } else if ( Date.class.isAssignableFrom(getColumnClass(col)) ) {
	            	tc.setCellRenderer(new DateTableCellRenderer());
	            } else if ( Number.class.isAssignableFrom(getColumnClass(col)) ) {
	            	tc.setCellRenderer(new NumberAndIntegerTableCellRenderer());
	            }
	        }
	        TableModelColumnAutofit columnAutoFit =
	            new TableModelColumnAutofit(rsm, this);
	        columnAutoFit.setTableHeader(header);
	        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		}

	}

	private class MatchGroupStatisticTable extends JTable {

		public MatchGroupStatisticTable(TableModel model) {
			super(model);
			JTableHeader header = getTableHeader();
			if ( header == null )
				System.out.println("header is null");
			else {

			}
			header.getColumnModel().getColumn(0).setHeaderValue("Group");
			header.getColumnModel().getColumn(1).setHeaderValue("Match Percent");
			header.getColumnModel().getColumn(2).setHeaderValue("Total Found");
			header.getColumnModel().getColumn(3).setHeaderValue("% of Total Found");
			header.getColumnModel().getColumn(4).setHeaderValue("Added");
			header.getColumnModel().getColumn(5).setHeaderValue("Updated");
			header.getColumnModel().getColumn(6).setHeaderValue("Total");
			header.getColumnModel().getColumn(7).setHeaderValue("Overall(current)");
		}

		@Override
		public void setModel(TableModel dataModel) {
			super.setModel(dataModel);
			setupHeaderAndCellRenderer();
		}

		private void setupHeaderAndCellRenderer() {
			/*JTableHeader header = getTableHeader();
			header.getColumnModel().getColumn(0).setHeaderValue("Group");
			header.getColumnModel().getColumn(1).setHeaderValue("Match Percent");
			header.getColumnModel().getColumn(2).setHeaderValue("Total Found");
			header.getColumnModel().getColumn(3).setHeaderValue("% of Total Found");
			header.getColumnModel().getColumn(4).setHeaderValue("Added");
			header.getColumnModel().getColumn(5).setHeaderValue("Updated");
			header.getColumnModel().getColumn(6).setHeaderValue("Total");
			header.getColumnModel().getColumn(7).setHeaderValue("Overall(current)");
*/
			TableColumnModel cm = getColumnModel();
	        for (int col = 0; col < cm.getColumnCount(); col++) {
	            TableColumn tc = cm.getColumn(col);
	            if ( col == 3 ) {
	            	tc.setCellRenderer(new PercentTableCellRenderer());
	            } else if ( Date.class.isAssignableFrom(getColumnClass(col)) ) {
	            	tc.setCellRenderer(new DateTableCellRenderer());
	            } else if ( Number.class.isAssignableFrom(getColumnClass(col)) ) {
	            	tc.setCellRenderer(new NumberAndIntegerTableCellRenderer());
	            }
	        }

/*	        if ( getModel() != null ) {
	        	TableModelColumnAutofit columnAutoFit =
	        		new TableModelColumnAutofit(getModel(), this);
	        	columnAutoFit.setTableHeader(header);
	        	setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	        }*/
		}
	}


	public class SelectionListener implements ListSelectionListener {
		private JTable matchTable;
		private JTable matchGroupTable;

        // It is necessary to keep the table since it is not possible
        // to determine the table from the event's source
        SelectionListener(JTable matchTable, JTable matchGroupTable) {
            this.matchTable = matchTable;
            this.matchGroupTable = matchGroupTable;
        }
        public void valueChanged(ListSelectionEvent e) {
        	RowSet rsGroup;
			try {
				rsGroup = getMatchGroupStats(matchTable);
				RowSetModel rsmGroup = new RowSetModel(rsGroup);
				matchGroupTable.setModel(rsmGroup);
			} catch (SQLException e1) {
				ASUtils.showExceptionDialog(MatchStatisticsPanel.this,"SQL Error",e1);
			}
        }
    }



	public boolean applyChanges() {
		System.out.println("apply changes!!!");
		return false;
	}

	public void discardChanges() {
		System.out.println("discard changes!!!");

	}

	public JComponent getPanel() {
		return this;
	}

}
