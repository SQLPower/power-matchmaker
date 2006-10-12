package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import javax.sql.RowSet;
import javax.sql.rowset.JoinRowSet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.table.DateTableCellRenderer;
import ca.sqlpower.architect.swingui.table.IndicatorCellRenderer;
import ca.sqlpower.architect.swingui.table.NumberAndIntegerTableCellRenderer;
import ca.sqlpower.architect.swingui.table.PercentTableCellRenderer;
import ca.sqlpower.architect.swingui.table.TableModelColumnAutofit;
import ca.sqlpower.matchmaker.RowSetModel;
import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.util.HibernateUtil;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.sun.rowset.CachedRowSetImpl;
import com.sun.rowset.JoinRowSetImpl;

public class MatchStatisticsPanel extends JPanel {

	private PlMatch match;
	private Timestamp startDateTime;

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
			tableGroup = new JTable(new MatchGroupStatisticTableModel(rsmGroup));
			setMatchGroupStatisticTableCellRenderer(tableGroup);
		} else {
			tableGroup = new JTable();
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
    		sql.append(",NO_OF_REC_READ/").append(total);
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
		final PlMatch match = MatchMakerFrame.getMainInstance().getMatchByName("DEMO_MATCH_PEOPLE_MATCH_FIRST");

		final MatchStatisticsPanel panel = new MatchStatisticsPanel(match);




		JPanel p = new JPanel(new BorderLayout());
		JButton deleteAllButton = new JButton(new AbstractAction("Delete All"){
			public void actionPerformed(ActionEvent e) {
				try {
					panel.deleteAllStatistics();
				} catch (SQLException e1) {
					ASUtils.showExceptionDialog(MatchMakerFrame.getMainInstance(),
							"Could not delete match statistic information", e1);
				}
			}});
		JButton deleteBackwardButton = new JButton(new AbstractAction("Delete Backward"){
			public void actionPerformed(ActionEvent e) {
				try {
					panel.deleteBackwardStatistics();
				} catch (SQLException e1) {
					ASUtils.showExceptionDialog(MatchMakerFrame.getMainInstance(),
							"Could not delete match statistic information", e1);
				}
			}});

		Action closeAction = new AbstractAction("Close"){
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}};
		closeAction.putValue(Action.NAME, "Close");
		JButton closeButton = new JButton(closeAction);

		ButtonBarBuilder bbb = new ButtonBarBuilder();
		bbb.addRelatedGap();
		bbb.addGridded(deleteAllButton);
		bbb.addRelatedGap();
		bbb.addGridded(deleteBackwardButton);
		bbb.addGlue();
		bbb.addGridded(closeButton);
		bbb.addRelatedGap();
		p.add(bbb.getPanel(),BorderLayout.SOUTH);
		p.add(panel,BorderLayout.CENTER);






		f.getContentPane().add(p);
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

	private class MatchGroupStatisticTableModel extends AbstractTableModel {

		private AbstractTableModel model;

		public MatchGroupStatisticTableModel(AbstractTableModel model) {
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
				return "Total Found";
			} else if ( column == 3 ) {
				return "% of Total Found";
			} else if ( column == 4 ) {
				return "Added";
			} else if ( column == 5 ) {
				return "Updated";
			} else if ( column == 6 ) {
				return "Total";
			} else if ( column == 7 ) {
				return "Overall(current)";
			} else {
				return "unknown column";
			}
		}
	}

	public void setMatchGroupStatisticTableCellRenderer(JTable table) {
		TableColumnModel cm = table.getColumnModel();
		for (int col = 0; col < cm.getColumnCount(); col++) {
			TableColumn tc = cm.getColumn(col);
			if ( col == 3 ) {
				tc.setCellRenderer(new PercentTableCellRenderer());
			} else if ( Date.class.isAssignableFrom(table.getColumnClass(col)) ) {
				tc.setCellRenderer(new DateTableCellRenderer());
			} else if ( Number.class.isAssignableFrom(table.getColumnClass(col)) ) {
				tc.setCellRenderer(new NumberAndIntegerTableCellRenderer());
			}
		}
		if ( table.getModel() != null ) {
			TableModelColumnAutofit columnAutoFit =
				new TableModelColumnAutofit(table.getModel(), table);
			columnAutoFit.setTableHeader(table.getTableHeader());
			table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
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

				Object startDateTime = matchTable.getValueAt(
						matchTable.getSelectedRow(),
						matchTable.convertColumnIndexToView(1));
				if ( startDateTime == null ) {
					MatchStatisticsPanel.this.setStartDateTime(null);
				} else if ( startDateTime instanceof Timestamp ) {
					MatchStatisticsPanel.this.setStartDateTime((Timestamp)startDateTime);
				} else if ( startDateTime instanceof java.sql.Date ) {
					MatchStatisticsPanel.this.setStartDateTime(new Timestamp( ((java.sql.Date)startDateTime).getTime()));
				} else if ( startDateTime instanceof Date ) {
					MatchStatisticsPanel.this.setStartDateTime(new Timestamp( ((Date)startDateTime).getTime()));
				} else {
					MatchStatisticsPanel.this.setStartDateTime(null);
				}

				rsGroup = getMatchGroupStats(matchTable);
				RowSetModel rsmGroup = new RowSetModel(rsGroup);
				matchGroupTable.setModel(
						new MatchGroupStatisticTableModel(rsmGroup));
				setMatchGroupStatisticTableCellRenderer(matchGroupTable);
			} catch (SQLException e1) {
				ASUtils.showExceptionDialog(MatchStatisticsPanel.this,"SQL Error",e1);
			}
        }
    }

	public int deleteAllStatistics() throws SQLException {
    	Connection con = null;
    	PreparedStatement pstmt = null;
    	try {
    		con = HibernateUtil.primarySession().connection();
    		StringBuffer sql = new StringBuffer();
    		sql.append("DELETE FROM PL_STATS WHERE OBJECT_TYPE=? ");
    		sql.append(" AND OBJECT_NAME=? ");
    		pstmt = con.prepareStatement(sql.toString());
    		pstmt.setString(1, "MATCH");
    		pstmt.setString(2, match.getMatchId());
    		int rc = pstmt.executeUpdate();
    		con.commit();

    		removeAll();
    		createUI();
    		validate();
    		repaint();
    		return rc;
    	} finally {
    		if ( pstmt != null )
    			pstmt.close();
    		if (con != null)
    			con.close();
    	}
    }

	public int deleteBackwardStatistics() throws SQLException {
    	Connection con = null;
    	PreparedStatement pstmt = null;
    	try {
    		con = HibernateUtil.primarySession().connection();
    		StringBuffer sql = new StringBuffer();
    		sql.append("DELETE FROM PL_STATS WHERE OBJECT_TYPE=? ");
    		sql.append(" AND OBJECT_NAME=? AND START_DATE_TIME<=?");
    		pstmt = con.prepareStatement(sql.toString());
    		pstmt.setString(1, "MATCH");
    		pstmt.setString(2, match.getMatchId());
    		pstmt.setTimestamp(3, getStartDateTime());
    		int rc = pstmt.executeUpdate();
    		con.commit();

    		removeAll();
    		createUI();
    		validate();
    		repaint();
    		return rc;
    	} finally {
    		if ( pstmt != null )
    			pstmt.close();
    		if (con != null)
    			con.close();
    	}
    }

	public Timestamp getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(Timestamp startDateTime) {
		if (this.startDateTime != startDateTime) {
			firePropertyChange("this.startDateTime", this.startDateTime,
					startDateTime);
			this.startDateTime = startDateTime;
		}
	}

}
