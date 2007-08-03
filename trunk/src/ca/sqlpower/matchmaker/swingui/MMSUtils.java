package ca.sqlpower.matchmaker.swingui;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.ArchitectVersion;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.SPDataSourcePanel;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.util.ExceptionReport;

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
            Window parentWindow,
            SPDataSource dataSource,
            final Runnable onAccept) {
        
        final DataEntryPanel dbcsPanel = new SPDataSourcePanel(dataSource);
        
        Action okAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (dbcsPanel.applyChanges()) {
                    if (onAccept != null) {
                        onAccept.run();
                    }
                }
            }
        };
    
        Action cancelAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dbcsPanel.discardChanges();
            }
        };
    
        JDialog d = DataEntryPanelBuilder.createDataEntryPanelDialog(
                dbcsPanel, parentWindow,
                "Database Connection: " + dataSource.getDisplayName(),
                DataEntryPanelBuilder.OK_BUTTON_LABEL,
                okAction, cancelAction);
    
        d.pack();
        d.setLocationRelativeTo(parentWindow);
    
        d.setVisible(true);
        return d;
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
    public static void showExceptionDialog(Component parent, String message, Throwable t) {
    	try {
    		ExceptionReport report = new ExceptionReport(t, ExceptionHandler.DEFAULT_REPORT_URL, ArchitectVersion.APP_VERSION, ArchitectUtils.getAppUptime(), "MatchMaker");
    		logger.debug(report.toString());
    		report.send();
    	} catch (Throwable seriousProblem) {
    		logger.error("Couldn't generate and send exception report!  Note that this is not the primary problem; it's a side effect of trying to report the real problem.", seriousProblem);
    		JOptionPane.showMessageDialog(null, "Error reporting failed: "+seriousProblem.getMessage()+"\nAdditional information is available in the application log.");
    	} finally {
    		SPSUtils.showExceptionDialogNoReport(parent, message, t);
    	}
    }
}
