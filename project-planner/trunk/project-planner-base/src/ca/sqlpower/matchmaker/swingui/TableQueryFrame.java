/*
 * Copyright (c) 2007, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */


package ca.sqlpower.matchmaker.swingui;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.ConnectionComboBoxModel;
import ca.sqlpower.swingui.MonitorableWorker;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SwingWorkerRegistry;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class TableQueryFrame extends JFrame {

	private static final Logger logger = Logger.getLogger(TableQueryFrame.class);
	public static final String DBCS_DIALOG_TITLE = "New Database Connection";

    private final MatchMakerSwingSession swingSession;

	private JProgressBar progressBar;
	private JPanel buttonPanel;
	private JComboBox dbDropdown;
	private JComboBox tableDropdown;
	private JSpinner rowLimit;
	private JLabel recordsFound;
	private JTextArea sqlStatement;
	private JTable columnInformation;

	private ResultTableModel model;

	private SQLDatabase cachedDatabase;

	public class SqlAreaPopulator implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			SQLTable table = (SQLTable) tableDropdown.getSelectedItem();
			if ( table != null ) {
				StringBuilder sql = new StringBuilder("SELECT * FROM ");
				if (table.getCatalog() != null) {
					sql.append(table.getCatalogName() + ".");
				}
				if (table.getSchema() != null) {
					sql.append(table.getSchemaName() + ".");
				}
				sql.append(table.getName());
				sqlStatement.setText(sql.toString());
				columnInformation.setName(sqlStatement.getText());
			}
		}
	}

	/**
	 * Finds all the children of a database and puts them in the GUI.
	 */
	public class TablePopulator extends MonitorableWorker implements
	ActionListener {

		private SQLDatabase db;
		private boolean started;
		private boolean finished;
		
		public TablePopulator(SwingWorkerRegistry registry) {
			super(registry);
		}
		 
		/**
		 * Checks the datasource selected in the databaseDropdown, and
		 * starts a worker thread to read its contents if it exists.
		 *
		 * <p>
		 * Otherwise, clears out the catalog and schema dropdowns and does
		 * not start a worker thread.
		 */
		public void actionPerformed(ActionEvent e) {
			db = getDatabase();

			tableDropdown.setEnabled(false);
			if (db != null) {
				new Thread(this).start();
			} else {
				tableDropdown.removeAllItems();
			}
		}

		/**
		 * Populates the database <tt>db</tt> which got set up in
		 * actionPerformed().
		 */
		@Override
		public void doStuff() /*throws Exception*/ {

			ProgressWatcher.watchProgress(progressBar, this);
			try {
			    started = true;
			    finished = false;
				db.populate();
			} catch (ArchitectException e) {
				throw new ArchitectRuntimeException(e);
			} finally {
			    finished = true;
			    started = false;
			    tableDropdown.setEnabled(true);
			}
		}

		/**
		 * Does GUI cleanup work on the Swing EDT once the worker is done.
		 *
		 * <p>
		 * This work involves:
		 * <ul>
		 * <li>Check which child type the database has
		 * <li>Populate the catalog and schema boxes accordingly
		 * <li>Enable or disable the catalog and schema boxes accordingly
		 * </ul>
		 */
		@Override
		public void cleanup() {
			setCleanupExceptionMessage("Could not populate database tables!");

			tableDropdown.removeAllItems();
			tableDropdown.setEnabled(false);

			List<SQLTable> tables;
			try {
				tables = db.getTables();
			} catch (ArchitectException e) {
				throw new ArchitectRuntimeException(e);
			}
			for ( SQLTable table : tables ) {
				tableDropdown.addItem(table);
			}
			tableDropdown.setEnabled(true);
		}

		public Integer getJobSize() {
			return null;
		}

		public String getMessage() {
			return "Populating table...";
		}

		public int getProgress() {
			return 0;
		}

		public boolean hasStarted() {
			return started;
		}

		public boolean isFinished() {
			return finished;
		}
	}

	/**
	 * Returns the currently selected database. Only creates a new
	 * SQLDatabase instance if necessary.
	 */
	public synchronized SQLDatabase getDatabase() {
		SPDataSource ds = (SPDataSource) dbDropdown
		.getSelectedItem();
		if (ds == null) {
			cachedDatabase = null;
		} else if (cachedDatabase == null
				|| !cachedDatabase.getDataSource().equals(ds)) {
			cachedDatabase = swingSession.getDatabase(ds);
		}
		return cachedDatabase;
	}

	private Action exportAction = new AbstractAction() {

		public void actionPerformed(ActionEvent e) {
			new JTableExporter(TableQueryFrame.this,columnInformation);
		}};


		/**
		 * Connects to the database returned by {@link #getDatabase()}, then executes the
		 * SQL query string in the {@link #sqlStatement} text area.
		 * <p>
		 * Applies a limit to the number of rows returned by the query based on the current
		 * value of the {@link #rowLimit} spinner.
		 * Does not attempt to execute an empty or whitespace-only query string.
		 */
	private Action executeAction = new AbstractAction("Execute") {

		public void actionPerformed(ActionEvent e) {
			String sql = sqlStatement.getText();
			sql = sql.trim();
			if (sql == null || sql.length() == 0) return;
			SQLDatabase db = getDatabase();
			if (db == null) return;
			Statement stmt = null;
	        ResultSet rs = null;
	        Connection con = null;
	        List<List<Object>> tableData = new ArrayList<List<Object>>();
	        List<String> columnNames = new ArrayList<String>();
	        try {
	            con = db.getConnection();
				stmt = con.createStatement();
	            int limit = (Integer) rowLimit.getValue();
	            if (limit > 0) {
	            	stmt.setMaxRows(limit);
	            }
	            rs = stmt.executeQuery(sql.toString());

	            ResultSetMetaData rsMeta = rs.getMetaData();
	            for ( int i=0; i<rsMeta.getColumnCount(); i++ ) {
	            	columnNames.add(rsMeta.getColumnName(i+1));
	            }
	            while (rs.next()) {
	            	List<Object> row = new ArrayList<Object>();
	            	for (int i = 0; i < columnNames.size(); i++) {
	            		row.add(rs.getObject(i + 1));
	            	}
	            	tableData.add(row);
	            }
	            
	            model.setData(columnNames, tableData);
	            
	            String rows = tableData.size() == 1 ? "row" : "rows";
	            recordsFound.setText(String.valueOf(tableData.size()) + " " + rows + " selected");

			} catch (SQLException ex) {
				SPSUtils.showExceptionDialogNoReport(TableQueryFrame.this,
						"Database Error while processing query", sql, ex);
			} catch (Exception ex) {
				SPSUtils.showExceptionDialogNoReport(TableQueryFrame.this,
						"Could not execute query", ex);
			} finally {
	        	try {
	        		if (rs != null) rs.close();
	        		if (stmt != null) stmt.close();
	        		if (con != null) con.close();
	        	} catch (SQLException ex) {
	        		logger.error("Cleanup after query execution failed! Squishing" +
	        				" this exception to preserve possible original:", ex);
	        	}
	        }
		}
	};

	/**
	 * Disposes this frame.
	 */
	private Action exitAction = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	};

	public JPanel getButtonPanel() {
		return buttonPanel;
	}

	public TableQueryFrame(MatchMakerSwingSession swingSession) {
		super();
        this.swingSession = swingSession;
		setTitle("Display Database Tables");
		getContentPane().add(createPanel());
		setLocationRelativeTo(swingSession.getFrame());
	}

	/**
	 * Creates the panel that makes up this frame's GUI.
	 */
	private JPanel createPanel() {

		tableDropdown = new JComboBox();
		tableDropdown.addActionListener(new SqlAreaPopulator());
		ConnectionComboBoxModel connectionModel =
            new ConnectionComboBoxModel(swingSession.getContext().getPlDotIni());
		dbDropdown = new JComboBox(connectionModel);
		dbDropdown.addActionListener(new TablePopulator(swingSession));


		rowLimit = new JSpinner();
		rowLimit.setValue(new Integer(500));

		model = new ResultTableModel();
		columnInformation = new JTable(model);

		recordsFound = new JLabel("0 row found");
		sqlStatement = new JTextArea();

		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setVisible(false);


		FormLayout layout = new FormLayout(
				"4dlu,fill:min(70dlu;default),4dlu,fill:200dlu:grow, 4dlu,min(50dlu;default),4dlu, fill:60dlu,4dlu,fill:min(40dlu;default),4dlu", // columns
				"10dlu,pref,4dlu, 0dlu,0dlu,pref,10dlu,10dlu,  10dlu,10dlu,40dlu,4dlu,16dlu,  10dlu, fill:120dlu:grow,10dlu"); // rows

		PanelBuilder pb;
		JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
		pb = new PanelBuilder(layout, p);


		CellConstraints cc = new CellConstraints();

		//Dropdown lists
		pb.add(new JLabel("Database Connection"), cc.xy(2,2,"r,c"));
		pb.add(dbDropdown, cc.xyw(4,2,5));
		pb.add(new JLabel("Table"), cc.xy(2,6,"r,c"));
		pb.add(tableDropdown, cc.xyw(4,6,5));

		pb.add(progressBar, cc.xyw(3,8,7));

		pb.add(new JLabel("Sql Command:"), cc.xy(2,10));
		pb.add(new JLabel("Row Limit:"), cc.xy(4,10,"r,c"));
		pb.add(rowLimit, cc.xy(6,10));

		pb.add(recordsFound, cc.xy(8,10));
		pb.add(new JScrollPane(sqlStatement), cc.xyw(2,11,8,"f,f"));

		ButtonBarBuilder bbBuilder = new ButtonBarBuilder();
		JButton exportButton = new JButton(exportAction );
		exportButton.setText("Export Grid");
		bbBuilder.addGridded (exportButton);
		bbBuilder.addRelatedGap();
		bbBuilder.addGlue();

		JButton executeButton = new JButton(executeAction);
		bbBuilder.addGridded (executeButton);
		bbBuilder.addUnrelatedGap();
		bbBuilder.addGlue();

		JButton exitButton = new JButton(exitAction);
		exitButton.setText("Exit");
		bbBuilder.addGridded (exitButton);
		bbBuilder.addRelatedGap();
		bbBuilder.addGlue();

		pb.add(bbBuilder.getPanel(),cc.xyw(2,13,10));

		JScrollPane scrollPane = new JScrollPane(columnInformation);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pb.add(scrollPane, cc.xyw(2,15,10));

		return pb.getPanel();

	}

	/**
	 * Simplistic class for representing SQL results.  I think we actually have a
	 * better table model somewhere for RowSets, but this was already here and I
	 * don't have time to rip it out.
	 */
	private class ResultTableModel extends AbstractTableModel {

		private List<List<Object>> tableData = Collections.emptyList();
		private List<String> tableColumns = Collections.emptyList();

		/**
		 * Replaces the current data set for this table model with the given
		 * one.  Each list in the rows list must have exactly the same length
		 * as the columnNames list.
		 * <p>
		 * This method doesn't copy the given lists, so if you modify them after
		 * calling this method, this table model will malfunction.
		 * 
		 * @param columnNames The names of the columns in the result set.
		 * @param rows The rows of data in the select list.
		 */
		public void setData(List<String> columnNames, List<List<Object>> rows) {
			tableColumns = columnNames;
			tableData = rows;
			fireTableStructureChanged();
		}
		
		public int getRowCount() {
			return tableData.size();
		}

		public int getColumnCount() {
			return tableColumns.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			return tableData.get(rowIndex).get(columnIndex);
		}

		@Override
		public String getColumnName(int column) {
			return tableColumns.get(column);
		}

	}
}
