package ca.sqlpower.matchmaker.swingui;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.PlDotIni;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.dao.hibernate.MatchMakerHibernateSessionContext;
import ca.sqlpower.security.PLSecurityException;

import com.darwinsys.util.PrefsUtils;

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
     * Creates a new Swing session context, which is a holding place for all the basic
     * settings in the MatchMaker GUI application.
     */
    public SwingSessionContext() throws ArchitectException, IOException {
        architectSession = ArchitectSession.getInstance();
        prefs = PrefsUtils.getUserPrefsNode(architectSession);
        context = createDelegateContext();
    }
    
    /**
     * Creates the delegate context, prompting the user (GUI) for any missing information.
     * @throws IOException 
     */
    private MatchMakerSessionContext createDelegateContext() throws ArchitectException, IOException {
        PlDotIni plDotIni = null;
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
                    "The Architect keeps its list of database connections" +
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
        return new MatchMakerHibernateSessionContext(plDotIni);
    }
    
    private PlDotIni readPlDotIni(String plDotIniPath) {
        if (plDotIniPath == null) {
            return null;
        }
        File pf = new File(plDotIniPath);
        if (!pf.exists() || !pf.canRead())
            return null;
        //
        PlDotIni pld = new PlDotIni();
        try {
            pld.read(pf);
            return pld;
        } catch (IOException e) {
            ASUtils.showExceptionDialog("Could not read " + pf, e);
            return null;
        }
    }

    public MatchMakerSession createSession(
            ArchitectDataSource ds, String username, String password)
    throws PLSecurityException, SQLException, ArchitectException {
        return context.createSession(ds, username, password);
    }

    public List<ArchitectDataSource> getDataSources() {
        return context.getDataSources();
    }
    
    public PlDotIni getPlDotIni() {
        return context.getPlDotIni();
    }
    
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

    public void setLastLoginDataSource(ArchitectDataSource dataSource) {
        prefs.put(SwingUserSettings.LAST_LOGIN_DATA_SOURCE, dataSource.getName());
    }

 
}
