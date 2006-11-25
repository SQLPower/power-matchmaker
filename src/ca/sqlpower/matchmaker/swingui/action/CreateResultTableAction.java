package ca.sqlpower.matchmaker.swingui.action;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

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

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLStatement;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;

import com.jgoodies.forms.builder.ButtonBarBuilder;

/**
 * Creates a new Match object and a GUI editor for it, then puts that editor in the split pane.
 */
public final class CreateResultTableAction extends AbstractAction {

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
		
		DDLGenerator ddlg = DDLUtils.createDDLGenerator(
				swingSession.getDatabase().getDataSource());
		if (ddlg == null) {
			JOptionPane.showMessageDialog(swingSession.getFrame(),
					"Couldn't create DDL Generator for database type\n"+
					swingSession.getDatabase().getDataSource().getDriverClass());
			return;
		}
		if (match.resultTableExists()) {
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
		ddlg.addIndex(match.getSourceTableIndex());
		
	    final JDialog editor = new JDialog(swingSession.getFrame(),
	    		"Create Result Table", false );
	    
	    Box statementsBox = Box.createVerticalBox();
	    for (DDLStatement sqlStatement : ddlg.getDdlStatements()) {
	    	statementsBox.add(new JTextArea(sqlStatement.getSQLText()));
	    }
	    
	    Action saveAction = new AbstractAction("Save") {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "not yet");
			}
	    };
	    Action copyAction = new AbstractAction("Copy to Clipboard") {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "not yet");
			}
	    };
	    Action executeAction = new AbstractAction("Execute") {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "not yet");
			}
	    };
	    Action cancelAction = new AbstractAction("Cancel") {
			public void actionPerformed(ActionEvent e) {
				editor.dispose();
			}
	    };
	    
	    // the gui layout part
	    JComponent cp = (JComponent) editor.getContentPane();
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
