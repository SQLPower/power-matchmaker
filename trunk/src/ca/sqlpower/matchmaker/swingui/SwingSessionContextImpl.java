package ca.sqlpower.matchmaker.swingui;

import java.awt.Rectangle;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.dao.hibernate.MatchMakerHibernateSessionContext;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PLSchemaException;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.util.VersionFormatException;


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
     * The database connection manager GUI for this session context (because all sessions
     * share the same set of database connections).
     */
    private final DatabaseConnectionManager dbConnectionManager;

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

        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
        	logger.error("Unable to set native look and feel. Continuing with default.", ex);
        }
        
        dbConnectionManager = new DatabaseConnectionManager(this);
        loginDialog = new LoginDialog(this);
    }


    //////// MatchMakerSessionContext implementation //////////
    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#createSession(ca.sqlpower.sql.SPDataSource, java.lang.String, java.lang.String)
     */
    public MatchMakerSwingSession createSession(
            SPDataSource ds, String username, String password)
    throws PLSecurityException, SQLException, IOException, VersionFormatException, PLSchemaException, ArchitectException {
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
        return new MatchMakerHibernateSessionContext(plDotIni, plDotIniPath);
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

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#getMatchEngineLocation()
     */
    public String getMatchEngineLocation() {
        return context.getMatchEngineLocation();
    }
    
    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.swingui.SwingSessionContext#getEmailEngineLocation()
     */
    public String getEmailEngineLocation() {
        return context.getEmailEngineLocation();
    }
}
