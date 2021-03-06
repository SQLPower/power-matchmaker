/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */


package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.RowSetModel;
import ca.sqlpower.swingui.table.DateTableCellRenderer;
import ca.sqlpower.swingui.table.IndicatorCellRenderer;
import ca.sqlpower.swingui.table.NumberAndIntegerTableCellRenderer;
import ca.sqlpower.swingui.table.PercentTableCellRenderer;
import ca.sqlpower.swingui.table.TableModelColumnAutofit;

import com.sun.rowset.CachedRowSetImpl;
import com.sun.rowset.JoinRowSetImpl;

/**
 * Displays statistics for the match engine. These values are currently not
 * saved anywhere, making this class non-functional, but the UI can still be
 * built without any trouble.
 */
public class MatchStatisticsPanel extends JPanel {

	private static final Logger logger = Logger.getLogger(MatchStatisticsPanel.class);
	
	private final MatchMakerSession swingSession;
	private Project project;
	private Timestamp startDateTime;

	public MatchStatisticsPanel(MatchMakerSession swingSession, Project project)
		throws SQLException {
		super(new BorderLayout());
		this.project = project;
		this.swingSession = swingSession;
		createUI();
	}

	private void createUI() throws SQLException {

		JTable table = new MatchStatisticTable(null);
		JScrollPane scroller = new JScrollPane(table);

		JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitter.setLeftComponent(scroller);


		JTable tableGroup = null;
		if ( table.getRowCount() > 0 && table.getValueAt(0,0) != null ) {
			table.setRowSelectionInterval(0,0);
			RowSet rsGroup = getMungeProcessStats(table);
			RowSetModel rsmGroup = new RowSetModel(rsGroup);
			tableGroup = new JTable(new MungeProcessStatisticTableModel(rsmGroup));
			setMungeProcessStatisticTableCellRenderer(tableGroup);
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


	private RowSet getProjectStats(Project project) throws SQLException {
    	Connection con = null;
    	Statement stmt = null;
    	ResultSet rs =  null;
    	String lastSql = null;
    	try {
    		// XXX This is not the table that should be used to find statistics
    		con = project.createResultTableConnection();
    		StringBuffer sql = new StringBuffer();
    		sql.append("SELECT TRANS_RUN_NO,START_DATE_TIME,ELAPSED_TIME");
    		sql.append(",RUN_STATUS,NO_OF_REC_READ,NO_OF_REC_ADDED");
    		sql.append(",NO_OF_REC_UPDATED,NO_OF_REC_TOTAL,NO_OF_REC_PROCESSED");
    		sql.append(",RUN_NO,ROLLBACK_SEGMENT_NAME");
    		sql.append(" FROM PL_STATS WHERE OBJECT_TYPE=? ");
    		sql.append(" AND OBJECT_NAME=? ");
    		sql.append(" ORDER BY TRANS_RUN_NO DESC, START_DATE_TIME DESC");
    		lastSql = sql.toString();
    		PreparedStatement pstmt = con.prepareStatement(lastSql);
    		pstmt.setString(1, "PROJECT");
    		pstmt.setString(2, project.getName());
    		rs = pstmt.executeQuery();

    		CachedRowSetImpl crset = new CachedRowSetImpl();
    		crset.setReadOnly(true);
    		crset.populate(rs);
    		return crset;
    	} catch (SQLException e) {
    		logger.error("SQL Exception caused by:\n" + lastSql, e);
    		throw e;
    	} finally {
    		if ( rs != null )
    			rs.close();
    		if ( stmt != null )
    			stmt.close();
    		if (con != null)
    			con.close();
    	}
    }

	private RowSet getMungeProcessStats(Project project, int runNo, int total) throws SQLException {
    	Connection con = null;
    	PreparedStatement pstmt = null;
    	ResultSet rs =  null;
    	try {
    		// XXX This is not the table that should be used to find statistics
    		con = project.createResultTableConnection();

    		if ( total == 0 ) {
    			total = 1;
    		}
    		StringBuffer sql = new StringBuffer();

    		sql.append("SELECT PROCESS_NAME,MATCH_PERCENT FROM MM_MUNGE_PROCESS WHERE PROJECT_OID=? ORDER BY MATCH_PERCENT DESC");
    		pstmt = con.prepareStatement(sql.toString());
    		pstmt.setLong(1, project.getOid());
    		rs = pstmt.executeQuery();
    		CachedRowSetImpl mungeProcessSet = new CachedRowSetImpl();
    		mungeProcessSet.setReadOnly(true);
    		mungeProcessSet.populate(rs);

    		rs.close();
    		pstmt.close();

    		sql = new StringBuffer();
    		sql.append("SELECT OBJECT_NAME, NO_OF_REC_READ");
    		sql.append(",NO_OF_REC_READ/").append(total);
    		sql.append(", NO_OF_REC_ADDED, NO_OF_REC_UPDATED");
    		sql.append(",NO_OF_REC_TOTAL, NO_OF_REC_PROCESSED");
    		sql.append(" FROM PL_STATS WHERE OBJECT_TYPE=? and TRANS_RUN_NO=?");
    		pstmt = con.prepareStatement(sql.toString());
    		pstmt.setString(1, "MUNGE_PROCESS");
    		pstmt.setInt(2, runNo);
    		rs = pstmt.executeQuery();
    		CachedRowSetImpl statsSet = new CachedRowSetImpl();
    		statsSet.setReadOnly(true);
    		statsSet.populate(rs);

    		JoinRowSet jrs = new JoinRowSetImpl();
    		jrs.addRowSet(mungeProcessSet,1);
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

	private RowSet getMungeProcessStats(JTable t) throws SQLException {

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

		return getMungeProcessStats(project,runNo,totalFound);
	}





	private class MatchStatisticTable extends JTable {

		public MatchStatisticTable(RowSetModel rsm) {
			
			for (int i=0;i<=10;i++) {
				addColumn(new TableColumn());
			}

			JTableHeader header = getTableHeader();
			header.getColumnModel().getColumn(0).setHeaderValue("Run #");
			header.getColumnModel().getColumn(1).setHeaderValue("Start Time");
			header.getColumnModel().getColumn(2).setHeaderValue("Elapsed(sec)");
			header.getColumnModel().getColumn(3).setHeaderValue("ValidateResult");
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
		}

	}

	private class MungeProcessStatisticTableModel extends AbstractTableModel {

		private AbstractTableModel model;

		public MungeProcessStatisticTableModel(AbstractTableModel model) {
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
				return "Transformation";
			} else if ( column == 1 ) {
				return "Priority";
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

	private void setMungeProcessStatisticTableCellRenderer(JTable table) {
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

	private class SelectionListener implements ListSelectionListener {
		private JTable projectTable;
		private JTable mungeProcessTable;

        // It is necessary to keep the table since it is not possible
        // to determine the table from the event's source
        SelectionListener(JTable projectTable, JTable mungeProcessTable) {
            this.projectTable = projectTable;
            this.mungeProcessTable = mungeProcessTable;
        }
        public void valueChanged(ListSelectionEvent e) {
        	RowSet rsGroup;
			try {

				Object startDateTime = projectTable.getValueAt(
						projectTable.getSelectedRow(),
						projectTable.convertColumnIndexToView(1));
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

				rsGroup = getMungeProcessStats(projectTable);
				RowSetModel rsmGroup = new RowSetModel(rsGroup);
				mungeProcessTable.setModel(
						new MungeProcessStatisticTableModel(rsmGroup));
				setMungeProcessStatisticTableCellRenderer(mungeProcessTable);
			} catch (SQLException e1) {
				MMSUtils.showExceptionDialog(MatchStatisticsPanel.this, "SQL Error", e1);
			}
        }
    }

	public int deleteAllStatistics() throws SQLException {
    	Connection con = null;
    	PreparedStatement pstmt = null;
    	try {
    		// XXX This is not the table that should be used to find statistics
    		con = project.createResultTableConnection();
    		StringBuffer sql = new StringBuffer();
    		sql.append("DELETE FROM PL_STATS WHERE OBJECT_TYPE=? ");
    		sql.append(" AND OBJECT_NAME=? ");
    		pstmt = con.prepareStatement(sql.toString());
    		pstmt.setString(1, "PROJECT");
    		pstmt.setString(2, project.getName());
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
    		// XXX This is not the table that should be used to find statistics
    		con = project.createResultTableConnection();
    		StringBuffer sql = new StringBuffer();
    		sql.append("DELETE FROM PL_STATS WHERE OBJECT_TYPE=? ");
    		sql.append(" AND OBJECT_NAME=? AND START_DATE_TIME<=?");
    		pstmt = con.prepareStatement(sql.toString());
    		pstmt.setString(1, "PROJECT");
    		pstmt.setString(2, project.getName());
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

	private Timestamp getStartDateTime() {
		return startDateTime;
	}

	private void setStartDateTime(Timestamp startDateTime) {
		if (this.startDateTime != startDateTime) {
			firePropertyChange("this.startDateTime", this.startDateTime,
					startDateTime);
			this.startDateTime = startDateTime;
		}
	}

}
