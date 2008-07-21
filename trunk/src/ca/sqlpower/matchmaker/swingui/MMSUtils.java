/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

import java.awt.Component;
import java.awt.Window;
import java.util.concurrent.Callable;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectVersion;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
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
            final Window parentWindow,
            final SPDataSource dataSource,
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
        return SPSUtils.createIcon("matchmaker_24", "MatchMaker Logo");
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
    		ExceptionReport report = new ExceptionReport(t, ExceptionHandler.DEFAULT_REPORT_URL, ArchitectVersion.APP_VERSION.toString(), "MatchMaker");
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

}
