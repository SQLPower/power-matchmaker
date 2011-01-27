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
import java.awt.Component;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectVersion;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLStatement;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.graph.ConnectedComponentFinder;
import ca.sqlpower.graph.GraphModel;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerUtils;
import ca.sqlpower.matchmaker.MatchPool;
import ca.sqlpower.matchmaker.PotentialMatchRecord;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.SourceTableRecord;
import ca.sqlpower.matchmaker.SourceTableRecordDisplayComparator;
import ca.sqlpower.matchmaker.graph.MatchPoolGraphModel;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSUtils.FileExtensionFilter;
import ca.sqlpower.util.ExceptionReport;

import com.jgoodies.forms.builder.ButtonBarBuilder;

public class MMSUtils {
	
	private static final Logger logger = Logger.getLogger(MMSUtils.class);
	
    /**
     * Pops up a dialog box that lets the user inspect and change the given db's
     * connection spec. This is very similar to the showDbcsDialog in the Architect's
     * ASUtils class because it is. Architect has additional tabs for additional data
     * source information (eg Kettle) which is not included in MatchMaker.
     * <p>
     * We considered making some sort of generic API in the library for creating a
     * connection dialog with optional extra tabs, but there's honestly not very
     * much code in this method, and it's hard to justify a whole API for something
     * this lightweight.
     * 
     * @param parentWindow
     *            The window that owns the dialog
     * @param dataSource
     *            the data source to edit (null not allowed)
     * @param onAccept
     *            this runnable will be invoked if the user OKs the dialog and
     *            validation succeeds. If you don't need to do anything in this
     *            situation, just pass in null for this parameter.
     */
    public static JDialog showDbcsDialog(
            final Window parentWindow,
            final JDBCDataSource dataSource,
            final Runnable onAccept) {
        
        final DataEntryPanel dbcsPanel = new MMDataSourcePanel(dataSource);
        
        Callable<Boolean> okCall = new Callable<Boolean>() {
            public Boolean call() {
                if (dbcsPanel.applyChanges()) {
                    if (onAccept != null) {
                    	try {
                    		dataSource.getParentCollection().write();
                    	} catch (Exception ex) {
                    		MMSUtils.showExceptionDialog(parentWindow, "Couldn't save connection information", ex);
                    	}
                        onAccept.run();
                    }
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
        };
    
        Callable<Boolean> cancelCall = new Callable<Boolean>() {
            public Boolean call() {
                dbcsPanel.discardChanges();
                return Boolean.TRUE;
            }
        };
    
        JDialog d = DataEntryPanelBuilder.createDataEntryPanelDialog(
                dbcsPanel, parentWindow,
                "Database Connection: " + dataSource.getDisplayName(),
                DataEntryPanelBuilder.OK_BUTTON_LABEL,
                okCall, cancelCall);
    
        d.pack();
        d.setLocationRelativeTo(parentWindow);
    
        d.setVisible(true);
        return d;
    }
    
    /**
     * Returns an icon that is suitable for use as a frame icon image
     * in the MatchMaker.
     */
    public static ImageIcon getFrameImageIcon() {
        return SPSUtils.createIcon("dqguru_24", "DQguru Logo");
    }

    /**
     * 
     * Displays a dialog box with the given message and exception,
     * allowing the user to examine the stack trace.  The dialog will
     * not have a parent component so it will be displayed on top of 
     * everything.
     * 
     * @deprecated This method will create a dialog, but because it
     * has no parent component, it will stay over everything.
     * 
     * @param message A user visible string that should explain the problem
     * @param t The exception that warranted a dialog
     */
    public static JDialog showExceptionDialogNoReport(String message, Throwable t) {
        JFrame f = new JFrame();
        f.setIconImage(getFrameImageIcon().getImage());
        return SPSUtils.showExceptionDialogNoReport(f, message, t);
    }

    /**
	 * Displays a dialog box with the given message and exception, allowing the
	 * user to examine the stack trace.
	 * <p>
	 * Also attempts to post an anonymous description of the error to a central
	 * reporting server.
	 * 
	 * @param parent
	 *            The parent window to the error window that this method makes
	 * @param message
	 *            A user visible string that should describe the problem
	 * @param t
	 *            The exception that warranted a dialog
	 */
    public static JDialog showExceptionDialog(Component parent, String message, Throwable t) {
    	try {
    		ExceptionReport report = new ExceptionReport(t, ExceptionHandler.DEFAULT_REPORT_URL, ArchitectVersion.APP_VERSION.toString(), "DQguru");
    		logger.debug(report.toString());
    		report.send();
    	} catch (Throwable seriousProblem) {
    		logger.error("Couldn't generate and send exception report!  Note that this is not the primary problem; it's a side effect of trying to report the real problem.", seriousProblem);
    		JOptionPane.showMessageDialog(null, "Error reporting failed: "+seriousProblem.getMessage()+"\nAdditional information is available in the application log.");
    	} finally {
    		return SPSUtils.showExceptionDialogNoReport(parent, message, t);
    	}
    }
    
    /**
	 * Searches the given tree's selection path for a Node of the given type.
	 * Returns the first one encountered, or null if there are no selected
	 * nodes of the given type.
	 */
	public static <T extends Object> T getTreeObject(JTree tree, Class<T> type) {
		TreePath[] paths = tree.getSelectionPaths();
		if (paths == null || paths.length == 0) {
			return null;
		}
		for (int i = 0; i < paths.length; i++) {
			TreePath path = paths[i];
			for (Object o : path.getPath()) {
				if (o.getClass().equals(type)) return (T) o;
			}
		}
		return null;
	}

    /**
	 * Creates and shows a dialog with the generated SQL for the
	 * result table in it. The dialog has buttons with actions 
	 * that can save, execute, or copy the SQL to the clipboard.
	 */
	public static void createResultTable(final Frame frame,
			JDBCDataSource dataSource, final Project project) throws InstantiationException,
			IllegalAccessException, HeadlessException, SQLException,
			SQLObjectException, ClassNotFoundException {

		final DDLGenerator ddlg = DDLUtils.createDDLGenerator(dataSource);

		if (ddlg == null) {
			throw new NullPointerException(
					"Couldn't create DDL Generator for database type "
							+ dataSource.getDriverClass());
		}
		ddlg.setTargetCatalog(project.getResultTableCatalog());
		ddlg.setTargetSchema(project.getResultTableSchema());
		if (project.doesResultTableExist()) {
			int answer = JOptionPane.showConfirmDialog(frame,
					"The result table exists. Do you want to drop and recreate it?",
					"Table exists",
					JOptionPane.YES_NO_OPTION);
			if ( answer != JOptionPane.YES_OPTION ) {
				return;
			}
			ddlg.dropTable(project.getResultTable());
		}
		ddlg.addTable(project.createResultTable());
		ddlg.addIndex((SQLIndex) project.getResultTable().getChildren(SQLIndex.class).get(0));

		final JDialog editor = new JDialog(frame,
				"Create Result Table", true);
		JComponent cp = (JComponent) editor.getContentPane();

		Box statementsBox = Box.createVerticalBox();
		final List<JTextArea> sqlTextFields = new ArrayList<JTextArea>();
		for (DDLStatement sqlStatement : ddlg.getDdlStatements()) {
			final JTextArea sqlTextArea = new JTextArea(sqlStatement.getSQLText());
			statementsBox.add(sqlTextArea);
			sqlTextFields.add(sqlTextArea);
		}

		Action saveAction = new AbstractAction("Save") {
			public void actionPerformed(ActionEvent e) {
				AbstractDocument doc = new DefaultStyledDocument();
				for (JTextArea sqlText : sqlTextFields ) {
					try {
						doc.insertString(doc.getLength(),
								sqlText.getText(),
								null);
						doc.insertString(doc.getLength(),";\n",null);
					} catch (BadLocationException e1) {
						SPSUtils.showExceptionDialogNoReport(frame, "Unexcepted Document Error",e1);
					}
				}
				SPSUtils.saveDocument(frame,
						doc,
						(FileExtensionFilter)SPSUtils.SQL_FILE_FILTER);
			}
		};
		Action copyAction = new AbstractAction("Copy to Clipboard") {
			public void actionPerformed(ActionEvent e) {
				StringBuffer buf = new StringBuffer();
				for (JTextArea sqlText : sqlTextFields ) {
					buf.append(sqlText.getText());
					buf.append(";\n");
				}
				StringSelection selection = new StringSelection(buf.toString());
				Clipboard clipboard = Toolkit.getDefaultToolkit()
				.getSystemClipboard();
				clipboard.setContents(selection, selection);
			}
		};
		Action executeAction = new AbstractAction("Execute") {
			public void actionPerformed(ActionEvent e) {

				Connection con = null;
				Statement stmt = null;
				String sql = null;
				try {
					con = project.createResultTableConnection();
					stmt = con.createStatement();
					int successCount = 0;

					for (JTextArea sqlText : sqlTextFields ) {
						sql = sqlText.getText();
						try {
							stmt.executeUpdate(sql);
							successCount += 1;
						} catch (SQLException ex) {
							int choice = JOptionPane.showOptionDialog(editor,
									"The following SQL statement failed:\n" +
									sql +
									"\nThe error was: " + ex.getMessage() +
									"\n\nDo you want to continue executing the create script?",
									"SQL Error", JOptionPane.YES_NO_OPTION,
									JOptionPane.ERROR_MESSAGE, null,
									new String[] {"Abort", "Continue"}, "Continue" );
							if (choice != 1) {
								break;
							}
						}
					}

					JOptionPane.showMessageDialog(frame,
							"Successfully executed " + successCount + " of " +
							sqlTextFields.size() + " SQL Statements." +
							(successCount == 0 ? "\n\nBetter Luck Next Time." : ""));

					//closes the dialog if all the statement is executed successfully
					//if not, the dialog remains on the screen
					if (successCount == sqlTextFields.size()){
						editor.dispose();
					}
				} catch (SQLException ex) {
					SPSUtils.showExceptionDialogNoReport(editor, "Create Script Failure", ex);
				} finally {
					try {
						if (stmt != null) {
							stmt.close();
						}
					} catch (SQLException ex) {
						logger.warn("Couldn't close statement", ex);
					}
					try {
						if (con != null) {
							con.close();
						}
					} catch (SQLException ex) {
						logger.warn("Couldn't close connection", ex);
					}
				}
			}
		};
		Action cancelAction = new AbstractAction("Close") {
			public void actionPerformed(ActionEvent e) {
				editor.dispose();
			}
		};

		// the gui layout part
		cp.setLayout(new BorderLayout(10,10));
		cp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		cp.add(new JScrollPane(statementsBox), BorderLayout.CENTER);

		ButtonBarBuilder bbb = ButtonBarBuilder.createLeftToRightBuilder();
		bbb.addGridded(new JButton(saveAction));
		bbb.addGridded(new JButton(copyAction));
		bbb.addGridded(new JButton(executeAction));
		bbb.addGridded(new JButton(cancelAction));

		cp.add(bbb.getPanel(), BorderLayout.SOUTH);

		editor.pack();
		editor.setLocationRelativeTo(null);
		editor.setVisible(true);
	}

	/**
	 * This method will load the entire pool of matches into memory, create the
	 * subgraphs based on the potential matches, and store them in a Derby
	 * database on the user's system.
	 * 
	 * @param project
	 * @param logger
	 * @param db
	 * @throws SQLException
	 */
	public static void populateProjectGraphTable(Project project, Logger logger, SQLDatabase db) throws SQLException {
		Connection testConnection;
		// This will store the graphs in a derby table in the .mm folder to
		// allow the validation screen to load up faster.
	    MatchPool pool = new MatchPool(project);
        try {
			pool.findAll(null);
		} catch (SQLObjectException e) {
			throw new SQLObjectRuntimeException(e);
		}
        GraphModel<SourceTableRecord, PotentialMatchRecord> graphModel = new MatchPoolGraphModel(pool);
        ConnectedComponentFinder<SourceTableRecord, PotentialMatchRecord> ccf =
            new ConnectedComponentFinder<SourceTableRecord, PotentialMatchRecord>(new SourceTableRecordDisplayComparator());
        Set<Set<SourceTableRecord>> components = ccf.findConnectedComponents(graphModel);

        testConnection = null;
        PreparedStatement ps = null;
        try {
        	testConnection = db.getConnection();
        	StringBuffer sb = new StringBuffer();
        	sb.append("INSERT INTO ");
        	sb.append(MatchMakerUtils.GRAPH_TABLE_NAME);
        	sb.append(" (").append(MatchMakerUtils.GRAPH_ID_COL_NAME);
        	for (int i = 0; i < project.getSourceTableIndex().getChildCount(); i++) {
        		sb.append(",");
        		sb.append(MatchMakerUtils.PK_KEY_PREFIX).append(i);
        	}
        	sb.append(") VALUES (");
        	for (int i = 0; i < project.getSourceTableIndex().getChildCount(); i++) {
        		sb.append("?,");
        	}
        	sb.append("?)");
        	ps = testConnection.prepareStatement(sb.toString());
        	int i = 0;
        	for (Set<SourceTableRecord> graph : components) {
        		for (SourceTableRecord str : graph) {
        			ps.setObject(1, i);
        			for (int j = 0; j < str.getKeyValues().size(); j++) {
        				ps.setObject(j + 2, str.getKeyValues().get(j));
        			}
        			ps.execute();
        		}
        		i++;
        	}
        } catch (SQLObjectException e) {
        	throw new RuntimeException(e);
		} finally {
        	if (ps != null) {
        		try {
        			ps.close();
        		} catch (Exception e) {
        			logger.error("Error closing statement to graph db.", e);
        		}
        	}
        	if (testConnection != null) {
        		try {
        			testConnection.close();
        		} catch (Exception e) {
        			logger.error("Error closing connection test.", e);
        		}
        	}
        }
	}

	/**
	 * Returns true if there is a table that exists to contain the graph
	 * groupings in the .mm directory.
	 */
	public static boolean checkForGraphTable(MatchMakerSession session, Project project, Logger logger) {
		SQLDatabase db = MatchMakerUtils.createProjectGraphDataSource(session, project);
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			con = db.getConnection();
			stmt = con.createStatement();
			stmt.setFetchSize(1);
			stmt.setMaxRows(1);
			try {
				rs = stmt.executeQuery("SELECT * FROM " + MatchMakerUtils.GRAPH_TABLE_NAME);
				return true;
			} catch (SQLException e) {
				return false;
			}
			// XXX Should use getMetaData().getTables() but it does not return
			// expected results so hacking a bit due to time.
//			rs = con.getMetaData().getTables(null, null, MatchMakerUtils.GRAPH_TABLE_NAME, null);
//			if (rs.next()) {
//				return true;
//			}
		} catch (SQLObjectException e) {
			throw new SQLObjectRuntimeException(e);
		} catch (SQLException e) {
			throw new RuntimeException("There was an error connecting to a local Derby database. " +
        			"Please check your database type information for the type that starts with Derby. " +
        			"Also ensure a correct driver exists.", e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					logger.error(e);
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					logger.error(e);
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					logger.error(e);
				}
			}
		}
	}

	/**
	 * Drops any existing graph table and re-creates it. 
	 */
	public static SQLDatabase setupProjectGraphTable(MatchMakerSession session, Project project, Logger logger) {
		SQLDatabase db = MatchMakerUtils.createProjectGraphDataSource(session, project);
        Connection testConnection = null;
        Statement stmt = null;
        try {
        	testConnection = db.getConnection();
        	stmt = testConnection.createStatement();
        	try {
        		stmt.execute("DROP TABLE " + MatchMakerUtils.GRAPH_TABLE_NAME);
        	} catch (SQLException e) {
        		//squishing as the table may not exist which is fine.
        	}
        	SQLTable graphTable = new SQLTable(db, true);
			graphTable.setName(MatchMakerUtils.GRAPH_TABLE_NAME);
			graphTable.addColumn(new SQLColumn(graphTable, MatchMakerUtils.GRAPH_ID_COL_NAME, Types.BIGINT, 0, 0));
			project.addResultTableColumns(graphTable, project.getSourceTableIndex(), MatchMakerUtils.PK_KEY_PREFIX);
    		final DDLGenerator ddlg = DDLUtils.createDDLGenerator(db.getDataSource());
    		ddlg.addTable(graphTable);
    		List<DDLStatement> ddlStatements = ddlg.getDdlStatements();
    		for (DDLStatement statement : ddlStatements) {
    			stmt.execute(statement.getSQLText());
    		}
        } catch (SQLObjectException e) {
        	throw new SQLObjectRuntimeException(e);
        } catch (Exception e) {
        	//TODO Make this a friendly error message not a runtime exception.
        	throw new RuntimeException("There was an error connecting to a local Derby database. " +
        			"Please check your database type information for the type that starts with Derby. " +
        			"Also ensure a correct driver exists.", e);
        } finally {
        	if (stmt != null) {
        		try {
        			stmt.close();
        		} catch (Exception e) {
        			logger.error("Error closing statement to graph db.", e);
        		}
        	}
        	if (testConnection != null) {
        		try {
        			testConnection.close();
        		} catch (Exception e) {
        			logger.error("Error closing connection test.", e);
        		}
        	}
        }
		return db;
	}

	/**
	 * Returns the largest graph number so we know when we get to the end of the
	 * generated graphs.
	 */
	public static BigInteger findMaxGraphNumber(MatchMakerSession session, Project project) throws SQLException {
		SQLDatabase db = MatchMakerUtils.createProjectGraphDataSource(session, project);
        Connection testConnection = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
        	testConnection = db.getConnection();
        	stmt = testConnection.createStatement();
        	rs = stmt.executeQuery("SELECT MAX(" + MatchMakerUtils.GRAPH_ID_COL_NAME + ") FROM " + MatchMakerUtils.GRAPH_TABLE_NAME);
        	if (rs.next()) {
        		if (rs.getBigDecimal(1) != null) {
        			return rs.getBigDecimal(1).toBigInteger();
        		} else {
        			return BigInteger.ZERO;
        		}
        	} else {
        		return BigInteger.ZERO;
        	}
        } catch (SQLObjectException e) {
        	throw new SQLObjectRuntimeException(e);
		} finally {
			if (rs != null) {
        		try {
        			rs.close();
        		} catch (Exception e) {
        			logger.error("Error closing statement to graph db.", e);
        		}
        	}
        	if (stmt != null) {
        		try {
        			stmt.close();
        		} catch (Exception e) {
        			logger.error("Error closing statement to graph db.", e);
        		}
        	}
        	if (testConnection != null) {
        		try {
        			testConnection.close();
        		} catch (Exception e) {
        			logger.error("Error closing connection test.", e);
        		}
        	}
        }
	}
}
