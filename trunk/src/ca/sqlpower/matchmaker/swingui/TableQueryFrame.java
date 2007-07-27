package ca.sqlpower.matchmaker.swingui;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.swingui.ListerProgressBarUpdater;
import ca.sqlpower.architect.swingui.Populator;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.ConnectionComboBoxModel;
import ca.sqlpower.swingui.SPSUtils;

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
	private JTextField rowLimit;
	private JLabel recordsFound;
	private JTextArea sqlStatement;
	private JTable columnInformation;

	private List<List> tableData;
	private List<String> tableColumn;
	private ResultTableModel model;
	private DDLGenerator ddlg = null;

	private SQLDatabase cachedDatabase;

	public class SqlAreaPopulator implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			SQLTable table = (SQLTable) tableDropdown.getSelectedItem();
			if ( table != null ) {
				try {
					ddlg = (DDLGenerator) DDLUtils.createDDLGenerator(
							table.getParentDatabase().getDataSource());
				} catch (InstantiationException e2) {
					logger.debug("Error getting GenericDDLGenerator", e2);
				} catch (IllegalAccessException e2) {
					logger.debug("Error getting GenericDDLGenerator", e2);
				}
				ddlg.selectTable(table,null,null);
				sqlStatement.setText(ddlg.getDdlStatements().get(0).getSQLText());
				columnInformation.setName(sqlStatement.getText());
			}
		}
	}

	/**
	 * Finds all the children of a database and puts them in the GUI.
	 */
	public class TablePopulator extends Populator implements
	ActionListener {

		private SQLDatabase db;

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

			if (db != null) {
				progressMonitor = db.getProgressMonitor();
				new Thread(this).start();
			} else {
				tableDropdown.removeAllItems();
				tableDropdown.setEnabled(false);
			}
		}

		/**
		 * Populates the database <tt>db</tt> which got set up in
		 * actionPerformed().
		 */
		@Override
		public void doStuff() /*throws Exception*/ {

			ListerProgressBarUpdater progressBarUpdater =
				new ListerProgressBarUpdater(progressBar, this);
			new javax.swing.Timer(100, progressBarUpdater).start();
			db.populate();
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

			List<SQLTable> tables = (List <SQLTable>)db.getTables();
			for ( SQLTable table : tables ) {
				tableDropdown.addItem(table);
			}
			tableDropdown.setEnabled(true);
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
			cachedDatabase = new SQLDatabase(ds);
		}
		return cachedDatabase;
	}

	private Action exportAction = new AbstractAction() {

		public void actionPerformed(ActionEvent e) {
			new JTableExporter(TableQueryFrame.this,columnInformation);
		}};


	private Action executeAction = new AbstractAction() {

		public void actionPerformed(ActionEvent e) {
			String sql = sqlStatement.getText();
			sql = sql.trim();
			if ( sql == null || sql.length() == 0 )
				return;
			SQLDatabase db = getDatabase();
			if ( db == null )
				return;
			Statement stmt = null;
	        ResultSet rs = null;
	        Connection connection = null;
	        tableData = new ArrayList<List>();
	        tableColumn = new ArrayList<String>();
	        try {
	            connection = db.getConnection();
				stmt = connection.createStatement();
	            int limit = 0;
	            if ( rowLimit.getText() != null && rowLimit.getText().length() > 0 ) {
	            	try {
	            		limit = Integer.valueOf(rowLimit.getText());
	            	} catch (Exception e1) {
	            	}
				}
	            stmt.setMaxRows(limit);
	            rs = stmt.executeQuery(sql.toString());

	            ResultSetMetaData rsMeta = rs.getMetaData();
	            for ( int i=0; i<rsMeta.getColumnCount(); i++ ) {
	            	tableColumn.add(rsMeta.getColumnName(i+1));
	            }
	            while (rs.next()) {
	            	List<Object> row = new ArrayList<Object>();
	            	for ( int i=0; i<tableColumn.size(); i++ ) {
	            		row.add(rs.getObject(i+1));
	            	}
	            	tableData.add(row);
	            }
	            model.fireTableStructureChanged();
	            recordsFound.setText(String.valueOf(tableData.size())+
	            		" row(s) found");

			} catch (SQLException e1 ) {
				SPSUtils.showExceptionDialogNoReport(TableQueryFrame.this,
						"Database Error while processing query", sql, e1);
			} finally {
	        	try {
	        		if (rs != null)
	        			rs.close();
	        		if (stmt != null)
	        			stmt.close();
	        		if ( connection != null )
	        			connection.close();
	        	} catch (SQLException e2) {
	        		e2.printStackTrace();
	        	}

	        }

		}};

	private Action exitAction = new AbstractAction() {

		public void actionPerformed(ActionEvent e) {
			setVisible(false);
		}};

	public JPanel getButtonPanel() {
		return buttonPanel;
	}

	public TableQueryFrame(MatchMakerSwingSession swingSession) {
		super();
        this.swingSession = swingSession;
		setTitle("Display Database Tables");
		tableData = new ArrayList<List>();
        tableColumn = new ArrayList<String>();
		getContentPane().add(createPanel());
	}

	private JPanel createPanel() {

		tableDropdown = new JComboBox();
		tableDropdown.addActionListener(new SqlAreaPopulator());
		ConnectionComboBoxModel connectionModel =
            new ConnectionComboBoxModel(swingSession.getContext().getPlDotIni());
		dbDropdown = new JComboBox(connectionModel);
		dbDropdown.addActionListener(new TablePopulator());


		rowLimit = new JTextField(5);
		rowLimit.setText("500");

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
		executeButton.setText("Execute");
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

	private class ResultTableModel extends AbstractTableModel {

		public int getRowCount() {
			return tableData.size();
		}

		public int getColumnCount() {
			return tableColumn.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			return tableData.get(rowIndex).get(columnIndex);
		}

		@Override
		public String getColumnName(int column) {
			return tableColumn.get(column);
		}

	}
}
