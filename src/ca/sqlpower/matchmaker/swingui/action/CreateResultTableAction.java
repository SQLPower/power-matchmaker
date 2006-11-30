package ca.sqlpower.matchmaker.swingui.action;


import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLStatement;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.SaveDocument;
import ca.sqlpower.architect.swingui.ASUtils.FileExtensionFilter;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;

import com.jgoodies.forms.builder.ButtonBarBuilder;

/**
 * Creates a new Match object and a GUI editor for it, then puts that editor in the split pane.
 */
public final class CreateResultTableAction extends AbstractAction {

	private static final Logger logger = Logger.getLogger(CreateResultTableAction.class);
    private final MatchMakerSwingSession swingSession;
	private Match match;

	public CreateResultTableAction(
            MatchMakerSwingSession swingSession,
            Match match) {
		super("Create Result Table");
        this.swingSession = swingSession;
		this.match = match;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			showSqlGui();
		} catch (Exception ex) {
			ASUtils.showExceptionDialog("Couldn't create SQL Preview", ex);
		}
	}
	
	/**
	 * Creates and shows a dialog with the generated SQL in it.
	 * The dialog has buttons with actions that can save, execute,
	 * or copy the SQL to the clipboard.
	 */
	public void showSqlGui() 
		throws InstantiationException, IllegalAccessException, 
		HeadlessException, ArchitectException, SQLException {
		
		final DDLGenerator ddlg = DDLUtils.createDDLGenerator(
				swingSession.getDatabase().getDataSource());
		if (ddlg == null) {
			JOptionPane.showMessageDialog(swingSession.getFrame(),
					"Couldn't create DDL Generator for database type\n"+
					swingSession.getDatabase().getDataSource().getDriverClass());
			return;
		}
		if (Match.doesResultTableExist(swingSession, match)) {
			int answer = JOptionPane.showConfirmDialog(swingSession.getFrame(),
					"Result table exists, do you want to drop and recreate it?",
					"Table exists", 
					JOptionPane.YES_NO_OPTION);
			if ( answer != JOptionPane.YES_OPTION ) {
				return;
			}
			ddlg.dropTable(match.getResultTable());
		}
		ddlg.addTable(match.createResultTable());
		ddlg.addIndex((SQLIndex) match.getResultTable().getIndicesFolder().getChild(0));
		
	    final JDialog editor = new JDialog(swingSession.getFrame(),
	    		"Create Result Table", false );
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
						ASUtils.showExceptionDialog("Unexcepted Document Error",e1);
					}
			    }
				new SaveDocument(swingSession.getFrame(),
						doc,
						(FileExtensionFilter)ASUtils.SQL_FILE_FILTER);
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
					con = swingSession.getConnection();
					stmt = con.createStatement();
					int successCount = 0;
					
					for (JTextArea sqlText : sqlTextFields ) {
						sql = sqlText.getText();
						try {
							stmt.executeUpdate(sql);
							successCount += 1;
						} catch (SQLException e1) {
							int choice = JOptionPane.showOptionDialog(editor,
									"The following SQL statement failed:\n" +
									sql +
									"\nThe error was: " + e1.getMessage() + 
									"\n\nDo you want to continue executing the create script?",
									"SQL Error", JOptionPane.YES_NO_OPTION,
									JOptionPane.ERROR_MESSAGE, null,
									new String[] {"Abort", "Continue"}, "Continue" );
							if (choice != 1) {
								break;
							}
						}
					}
					
					JOptionPane.showMessageDialog(swingSession.getFrame(),
							"Successfully executed " + successCount + " of " +
							sqlTextFields.size() + " SQL Statements." +
							(successCount == 0 ? "\n\nBetter Luck Next Time." : ""));
				} catch (SQLException ex) {
					JOptionPane.showMessageDialog(editor,
							"Create Script Failure",
							"Couldn't allocate a Statement:\n" + ex.getMessage(),
							JOptionPane.ERROR_MESSAGE);
				} finally {
					try {
						if (stmt != null) stmt.close();
					} catch (SQLException ex) {
						logger.error("SQLException while closing statement", ex);
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
}
