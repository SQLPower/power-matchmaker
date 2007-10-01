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

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.matchmaker.MatchMakerConfigurationException;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.dao.hibernate.MatchMakerHibernateSessionContext;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PLSchemaException;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.JDefaultButton;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.db.DataSourceDialogFactory;
import ca.sqlpower.swingui.db.DataSourceTypeDialogFactory;
import ca.sqlpower.swingui.db.DataSourceTypeEditor;
import ca.sqlpower.swingui.db.DatabaseConnectionManager;
import ca.sqlpower.util.ExceptionReport;
import ca.sqlpower.util.VersionFormatException;

import com.jgoodies.forms.factories.ButtonBarFactory;


public class SwingSessionContextImpl implements MatchMakerSessionContext, SwingSessionContext {

    private static final Logger logger = Logger.getLogger(SwingSessionContextImpl.class);

    /**
     * The underlying context that will deal with Hibernate for us.
     */
    private final MatchMakerSessionContext context;

    /**
     * The prefs node that we use for persisting all the basic user settings that are
     * the same for all MatchMaker sessions.
     */
    private final Preferences prefs;
    
    /**
     * Action that implements an extra button we put on the database connection manager
     * dialog. It hides that dialog, then shows the login dialog, where the connection
     * that was selected in the connection manager is made into the current selection.
     */
    private final Action loginDatabaseConnectionAction = new AbstractAction("Login") {

		public void actionPerformed(ActionEvent e) {
			SPDataSource dbcs = dbConnectionManager.getSelectedConnection();
			if (dbcs == null) {
				logger.debug("getSelectedConnection returned null");
				return;
			}
			dbConnectionManager.closeDialog();
            showLoginDialog(dbcs);
		}
	};

    /**
     * The database connection manager GUI for this session context (because all sessions
     * share the same set of database connections).
     */
    private final DatabaseConnectionManager dbConnectionManager;

    /**
     * This factory just passes the request through to the {@link MMSUtils#showDbcsDialog(Window, SPDataSource, Runnable)}
     * method.
     */
    private final DataSourceDialogFactory dsDialogFactory = new DataSourceDialogFactory() {

		public JDialog showDialog(Window parentWindow, SPDataSource dataSource,	Runnable onAccept) {
			return MMSUtils.showDbcsDialog(parentWindow, dataSource, onAccept);
		}
    	
    };
    
    /**
     * Implementation of DataSourceTypeDialogFactory that will display a DataSourceTypeEditor dialog
     */
    private final DataSourceTypeDialogFactory dsTypeDialogFactory = new DataSourceTypeDialogFactory() {
        
    	private JDialog d; 
    	private DataSourceTypeEditor editor;
    	
    	public Window showDialog(Window owner) {
        	if (d == null) {
	    		d = SPSUtils.makeOwnedDialog(owner, "JDBC Drivers");
	        	editor = new DataSourceTypeEditor(context.getPlDotIni());
	        	
	        	JPanel cp = new JPanel(new BorderLayout(12,12));
	            cp.add(editor.getPanel(), BorderLayout.CENTER);
	            cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
	        	
	        	JDefaultButton okButton = new JDefaultButton(DataEntryPanelBuilder.OK_BUTTON_LABEL);
	            okButton.addActionListener(new ActionListener() {
	                    public void actionPerformed(ActionEvent evt) {
	                        editor.applyChanges();
	                        d.dispose();
	                    }
	                });
	        
	            Action cancelAction = new AbstractAction() {
	                    public void actionPerformed(ActionEvent evt) {
	                        editor.discardChanges();
	                        d.dispose();
	                    }
	            };
	            cancelAction.putValue(Action.NAME, DataEntryPanelBuilder.CANCEL_BUTTON_LABEL);
	            JButton cancelButton = new JButton(cancelAction);
	    
	            JPanel buttonPanel = ButtonBarFactory.buildOKCancelBar(okButton, cancelButton);
	    
	            SPSUtils.makeJDialogCancellable(d, cancelAction);
	            d.getRootPane().setDefaultButton(okButton);
	            cp.add(buttonPanel, BorderLayout.SOUTH);
	        	
	        	d.setContentPane(cp);
	        	d.pack();
	        	d.setLocationRelativeTo(owner);
        	}
        	d.setVisible(true);
            return d;
        }
    };
    
    /**
     * The login dialog for this app.  The session context will only create one login
     * dialog.
     */
    private final LoginDialog loginDialog;

    /**
     * Creates a new Swing session context, which is a holding place for all the basic
     * settings in the MatchMaker GUI application.  This constructor creates its own delegate
     * session context object based on information in the given prefs node, or failing that,
     * by prompting the user with a GUI.
     */
    public SwingSessionContextImpl(Preferences prefsRootNode) throws IOException {
        this(prefsRootNode, createDelegateContext(prefsRootNode));
    }

    /**
     * Creates a new Swing session context, which is a holding place for all the basic
     * settings in the MatchMaker GUI application.  This implementation uses the delegate
     * context given as an argument.  It is intended for facilitating proper unit tests, and
     * you will most likely prefer using the other constructor in real life.
     */
    public SwingSessionContextImpl(
            Preferences prefsRootNode,
            MatchMakerSessionContext delegateContext) throws IOException {
        this.prefs = prefsRootNode;
        this.context = delegateContext;
        ExceptionReport.init();

        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
        	logger.error("Unable to set native look and feel. Continuing with default.", ex);
        }
        // Set a login action property so that if there is no connection selected 
        // in the dbConnectionManager GUI, the corresponding button will be disabled.
        loginDatabaseConnectionAction.putValue(DatabaseConnectionManager.DISABLE_IF_NO_CONNECTION_SELECTED, Boolean.TRUE);
        
        dbConnectionManager = new DatabaseConnectionManager(getPlDotIni(), dsDialogFactory,dsTypeDialogFactory, Collections.singletonList(loginDatabaseConnectionAction));
        loginDialog = new LoginDialog(this);
    }


    //////// MatchMakerSessionContext implementation //////////
    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#createSession(ca.sqlpower.sql.SPDataSource, java.lang.String, java.lang.String)
     */
    public MatchMakerSwingSession createSession(
            SPDataSource ds, String username, String password)
    throws PLSecurityException, SQLException, IOException, VersionFormatException,
            PLSchemaException, ArchitectException, MatchMakerConfigurationException {
        return new MatchMakerSwingSession(this, context.createSession(ds, username, password));
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#getDataSources()
     */
    public List<SPDataSource> getDataSources() {
        return context.getDataSources();
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#getPlDotIni()
     */
    public DataSourceCollection getPlDotIni() {
        return context.getPlDotIni();
    }


    //////// Persistent Prefs Support /////////

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#getLastImportExportAccessPath()
     */
    public String getLastImportExportAccessPath() {
        return prefs.get(MatchMakerSwingUserSettings.LAST_IMPORT_EXPORT_PATH, null);
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#setLastImportExportAccessPath(java.lang.String)
     */
    public void setLastImportExportAccessPath(String lastExportAccessPath) {
        prefs.put(MatchMakerSwingUserSettings.LAST_IMPORT_EXPORT_PATH, lastExportAccessPath);
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#getFrameBounds()
     */
    public Rectangle getFrameBounds() {
        Rectangle bounds = new Rectangle();
        bounds.x = prefs.getInt(MatchMakerSwingUserSettings.MAIN_FRAME_X, 100);
        bounds.y = prefs.getInt(MatchMakerSwingUserSettings.MAIN_FRAME_Y, 100);
        bounds.width = prefs.getInt(MatchMakerSwingUserSettings.MAIN_FRAME_WIDTH, 600);
        bounds.height = prefs.getInt(MatchMakerSwingUserSettings.MAIN_FRAME_HEIGHT, 440);
        return bounds;
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#setFrameBounds(java.awt.Rectangle)
     */
    public void setFrameBounds(Rectangle bounds) {
        prefs.putInt(MatchMakerSwingUserSettings.MAIN_FRAME_X, bounds.x);
        prefs.putInt(MatchMakerSwingUserSettings.MAIN_FRAME_Y, bounds.y);
        prefs.putInt(MatchMakerSwingUserSettings.MAIN_FRAME_WIDTH, bounds.width);
        prefs.putInt(MatchMakerSwingUserSettings.MAIN_FRAME_HEIGHT, bounds.height);
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#setLastLoginDataSource(ca.sqlpower.sql.SPDataSource)
     */
    public void setLastLoginDataSource(SPDataSource dataSource) {
        prefs.put(MatchMakerSwingUserSettings.LAST_LOGIN_DATA_SOURCE, dataSource.getName());
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#getLastLoginDataSource()
     */
    public SPDataSource getLastLoginDataSource() {
        String lastDSName = prefs.get(MatchMakerSwingUserSettings.LAST_LOGIN_DATA_SOURCE, null);
        if (lastDSName == null) return null;
        for (SPDataSource ds : getDataSources()) {
            if (ds.getName().equals(lastDSName)) return ds;
        }
        return null;
    }

    ///////// Global GUI Stuff //////////

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#showDatabaseConnectionManager()
     */
    public void showDatabaseConnectionManager(Window owner) {
        dbConnectionManager.showDialog(owner);
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#showLoginDialog(ca.sqlpower.sql.SPDataSource)
     */
    public void showLoginDialog(SPDataSource selectedDataSource) {
        loginDialog.showLoginDialog(selectedDataSource);
    }


    ///////// Private implementation details ///////////

    /**
     * Creates the delegate context, prompting the user (GUI) for any missing information.
     * @throws IOException
     */
    private static MatchMakerSessionContext createDelegateContext(Preferences prefs) throws IOException {
        DataSourceCollection plDotIni = null;
        //XXX: We should NOT be using ArchitectSession for this
        String plDotIniPath = prefs.get(ArchitectSession.PREFS_PL_INI_PATH, null);
        while ((plDotIni = readPlDotIni(plDotIniPath)) == null) {
            logger.debug("readPlDotIni returns null, trying again...");
            String message;
            String[] options = new String[] {"Browse", "Create"};
            final int BROWSE = 0; // indices into above array
            final int CREATE = 1;
            if (plDotIniPath == null) {
                message = "location is not set";
            } else if (new File(plDotIniPath).isFile()) {
                message = "file \n\n\""+plDotIniPath+"\"\n\n could not be read";
            } else {
                message = "file \n\n\""+plDotIniPath+"\"\n\n does not exist";
            }
            int choice = JOptionPane.showOptionDialog(null,   // blocking wait
                    "The MatchMaker keeps its list of database connections" +
                    "\nin a file called PL.INI.  Your PL.INI "+message+"." +
                    "\n\nYou can browse for an existing PL.INI file on your system" +
                    "\nor allow the Architect to create a new one in your home directory." +
                    "\n\nHint: If you are a Power*Loader Suite user, you should browse for" +
                    "\nan existing PL.INI in your Power*Loader installation directory.",
                    "Missing PL.INI", 0, JOptionPane.INFORMATION_MESSAGE, null, options, null);

            if (choice == JOptionPane.CLOSED_OPTION) {
                throw new RuntimeException("Can't start without a pl.ini file");
            } else if (choice == BROWSE) {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(SPSUtils.INI_FILE_FILTER);
                fc.setDialogTitle("Locate your PL.INI file");
                int fcChoice = fc.showOpenDialog(null);       // blocking wait
                if (fcChoice == JFileChooser.APPROVE_OPTION) {
                    plDotIniPath = fc.getSelectedFile().getAbsolutePath();
                } else {
                    plDotIniPath = null;
                }
            } else if (choice == CREATE) {
                String userHome = System.getProperty("user.home");
                if (userHome == null) {
                	throw new IllegalStateException("user.home property is null!");
                }
				plDotIniPath = userHome + File.separator + "pl.ini";
				// Create an empty file so the read won't throw an IOE
				if (new File(plDotIniPath).createNewFile()) {
					logger.debug("Created file " + plDotIniPath);
				} else {
					logger.debug("Did NOT create file " + plDotIniPath +
							"; mayhap it already exists?");
				}
            } else {
                throw new RuntimeException(
                "Unexpected return from JOptionPane.showOptionDialog to get pl.ini");
            }
        }
        //XXX: We should NOT be using ArchitectSession for this
        prefs.put(ArchitectSession.PREFS_PL_INI_PATH, plDotIniPath);
        return new MatchMakerHibernateSessionContext(plDotIni);
    }

    private static DataSourceCollection readPlDotIni(String plDotIniPath) {
        if (plDotIniPath == null) {
            return null;
        }
        File pf = new File(plDotIniPath);
        if (!pf.exists() || !pf.canRead()) {
            return null;
        }

        DataSourceCollection pld = new PlDotIni();
        try {
            pld.read(pf);
            return pld;
        } catch (IOException e) {
            SPSUtils.showExceptionDialogNoReport("Could not read " + pf, e);
            return null;
        }
    }
}
