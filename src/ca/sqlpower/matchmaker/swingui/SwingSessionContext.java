package ca.sqlpower.matchmaker.swingui;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.DataSourceCollection;
import ca.sqlpower.architect.PlDotIni;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.dao.hibernate.MatchMakerHibernateSessionContext;
import ca.sqlpower.security.PLSecurityException;

import com.darwinsys.swingui.UtilGUI;


public class SwingSessionContext implements MatchMakerSessionContext {

    private static final Logger logger = Logger.getLogger(SwingSessionContext.class);

    /**
     * The underlying context that will deal with Hibernate for us. 
     */
    private final MatchMakerSessionContext context;

    /**
     * We'd rather not have one of these, but it's got something to do with prefs.
     */
    private final ArchitectSession architectSession;

    /**
     * The prefs node that we use for persisting all the basic user settings that are
     * the same for all MatchMaker sessions.
     */
    private final Preferences prefs;

    /**
     * A frame that is not visible which can own application-wide dialogs made by this
     * class.  Note that any dialogs with this frame as their parent should not be modal
     * unless they are also always-on-top, since this frame itself isn't visible.
     */
    private final JFrame fakeParentFrame;

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
    public SwingSessionContext(ArchitectSession architectSession, Preferences prefsRootNode) throws ArchitectException, IOException {
        this(architectSession, prefsRootNode, createDelegateContext(prefsRootNode));
    }
    
    /**
     * Creates a new Swing session context, which is a holding place for all the basic
     * settings in the MatchMaker GUI application.  This implementation uses the delegate
     * context given as an argument.  It is intended for facilitating proper unit tests, and
     * you will most likely prefer using the other constructor in real life.
     */
    public SwingSessionContext(
            ArchitectSession architectSession,
            Preferences prefsRootNode,
            MatchMakerSessionContext delegateContext) throws ArchitectException, IOException {
        this.architectSession = architectSession;
        this.prefs = prefsRootNode;
        this.context = delegateContext;
        
        fakeParentFrame = new JFrame("Never Visible");
        fakeParentFrame.setIconImage(new ImageIcon(getClass().getResource("/icons/matchmaker_24.png")).getImage());
        dbConnectionManager = new DatabaseConnectionManager(fakeParentFrame, this);       
        loginDialog = new LoginDialog(this);
    }


    //////// MaatchMakerSessionContext implementation //////////
    public MatchMakerSwingSession createSession(
            ArchitectDataSource ds, String username, String password)
    throws PLSecurityException, SQLException, ArchitectException, IOException {
        return new MatchMakerSwingSession(this, context.createSession(ds, username, password));
    }

    public List<ArchitectDataSource> getDataSources() {
        return context.getDataSources();
    }

    public DataSourceCollection getPlDotIni() {
        return context.getPlDotIni();
    }


    //////// Persistent Prefs Support /////////

    public String getLastImportExportAccessPath() {
        return prefs.get(SwingUserSettings.LAST_IMPORT_EXPORT_PATH, null);
    }

    public void setLastImportExportAccessPath(String lastExportAccessPath) {
        prefs.put(SwingUserSettings.LAST_IMPORT_EXPORT_PATH, lastExportAccessPath);
    }

    /**
     * Returns the previous location for the MatchMaker frame, or some reasonable default
     * if the previous bounds are unknown.
     */
    public Rectangle getFrameBounds() {
        Rectangle bounds = new Rectangle();
        bounds.x = prefs.getInt(SwingUserSettings.MAIN_FRAME_X, 100);
        bounds.y = prefs.getInt(SwingUserSettings.MAIN_FRAME_Y, 100);
        bounds.width = prefs.getInt(SwingUserSettings.MAIN_FRAME_WIDTH, 600);
        bounds.height = prefs.getInt(SwingUserSettings.MAIN_FRAME_HEIGHT, 440);
        return bounds;
    }

    /**
     * Stores (persistently) the current position of the main frame, so it can appear
     * in the same location next time you call {@link #getFrameBounds()}.
     */
    public void setFrameBounds(Rectangle bounds) {
        prefs.putInt(SwingUserSettings.MAIN_FRAME_X, bounds.x);
        prefs.putInt(SwingUserSettings.MAIN_FRAME_Y, bounds.y);
        prefs.putInt(SwingUserSettings.MAIN_FRAME_WIDTH, bounds.width);
        prefs.putInt(SwingUserSettings.MAIN_FRAME_HEIGHT, bounds.height);
    }

    /**
     * Remembers the name of the given data source, so you can get it back next
     * time you call getLastLoginDataSource, even if the application is quit and
     * restarted in the interim.
     * 
     * @param dataSource The data source to remember.
     */
    public void setLastLoginDataSource(ArchitectDataSource dataSource) {
        prefs.put(SwingUserSettings.LAST_LOGIN_DATA_SOURCE, dataSource.getName());
        System.out.println("Right after set: "+prefs.get(SwingUserSettings.LAST_LOGIN_DATA_SOURCE, null));
    }

    /**
     * Returns the last data source passed to setLastLoginDataSource, even if
     * that happened since the application was last restarted.
     */
    public ArchitectDataSource getLastLoginDataSource() {
        String lastDSName = prefs.get(SwingUserSettings.LAST_LOGIN_DATA_SOURCE, null);
        System.out.println("lastDSName:" + lastDSName);
        if (lastDSName == null) return null;
        for (ArchitectDataSource ds : getDataSources()) {
            if (ds.getName().equals(lastDSName)) return ds;
        }        
        return null;
    }

    ///////// Global GUI Stuff //////////

    /**
     * Shows the database connection manager.  There will only ever be one created
     * no matter how many times you call this method.
     */
    public void showDatabaseConnectionManager() {
        dbConnectionManager.showDialog();
    }

    /**
     * Shows the login dialog.
     * 
     * @param selectedDataSource The data source that should be selected in the dialog.
     * If null, the dialog's selected data source will remain unchanged.
     */
    public void showLoginDialog(ArchitectDataSource selectedDataSource) {
        loginDialog.setDbSource(selectedDataSource);
        loginDialog.pack();
        UtilGUI.centre(loginDialog);
        loginDialog.setVisible(true);
        loginDialog.requestFocus();
    }


    ///////// Private implementation details ///////////

    /**
     * Creates the delegate context, prompting the user (GUI) for any missing information.
     * @throws IOException 
     */
    private static MatchMakerSessionContext createDelegateContext(Preferences prefs) throws ArchitectException, IOException {
        DataSourceCollection plDotIni = null;
        String plDotIniPath = prefs.get(ArchitectSession.PREFS_PL_INI_PATH, null);
        while ((plDotIni = readPlDotIni(plDotIniPath)) == null) {
            logger.debug("readPlDotIni returns null, trying again...");
            String message;
            String[] options = new String[] {"Browse", "Create"};
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
                throw new ArchitectException("Can't start without a pl.ini file");
            } else if (choice == 0) {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(ASUtils.INI_FILE_FILTER);
                fc.setDialogTitle("Locate your PL.INI file");
                int fcChoice = fc.showOpenDialog(null);       // blocking wait
                if (fcChoice == JFileChooser.APPROVE_OPTION) {
                    plDotIniPath = fc.getSelectedFile().getAbsolutePath();
                } else {
                    plDotIniPath = null;
                }
            } else if (choice == 1) {
                plDotIniPath = System.getProperty("user.home", "pl.ini");
            } else {
                throw new ArchitectException(
                "Unexpected return from JOptionPane.showOptionDialog to get pl.ini");
            }
        }
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
            ASUtils.showExceptionDialog("Could not read " + pf, e);
            return null;
        }
    }

    /**
     * Gets the path of where the engine is located base on CoreUserSettings
     * @return a string of the path of where the engine is located, if not found, returns null 
     */
    public String getEngineLocation() {
        return context.getEngineLocation();
    }
}
