package ca.sqlpower.matchmaker.prefs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

/**
 * Locate the User Preferences Node for the Architect package, and pass it around to people
 * that want it via load-time injection; also do some little dance for handling a list of
 * JDBC Driver jar file names.
 */
public class PreferencesManager {

	private static final int MAX_DRIVER_JAR_FILE_NAMES = 99;

	protected static final String JAR_FILE_NODE_NAME = "jarfiles";

	protected static final String PREFS_JARFILE_PREFIX = "JDBCJarFile.";

	private static final Logger logger = Logger.getLogger(PreferencesManager.class);

	private static final PreferencesManager singleton;
    private static final Preferences prefs =
    	Preferences.userNodeForPackage(ca.sqlpower.architect.ArchitectSession.class);

    private List<PreferencesUser> listeners = new ArrayList<PreferencesUser>();

	private PreferencesManager() {
		// private constructor, is a singleton
	}

	static {
		singleton = new PreferencesManager();
	}

	public static PreferencesManager getDefaultInstance() {
		return singleton;
	}

	public void addPreferencesListener(PreferencesUser listener) {
		listeners.add(listener);
	}

	public Preferences getRootNode() {
		return prefs;
	}

	// -------------------- Loading the preferences --------------------------

	public void load(JarFileListMaintainer session) {
		logger.debug("loading UserSettings from java.util.prefs.");

		Preferences jarNode = prefs.node(JAR_FILE_NODE_NAME);
		logger.debug("PreferencesManager.load(): jarNode " + jarNode);
		for (int i = 0; i <= MAX_DRIVER_JAR_FILE_NAMES; i++) {
			String jarName = jarNode.get(jarFilePrefName(i), null);
			logger.debug("read Jar File entry: " + jarName);
			if (jarName == null) {
				break;
			}

			logger.debug("Adding JarName: " + jarName);
			session.addDriverJar(jarName);
		}

		for (PreferencesUser listener : listeners) {
			listener.setPreferencesRootNode(prefs);
		}

	}


	// -------------------- "WRITING THE FILE" --------------------------

	public void store(JarFileListMaintainer session) {

		logger.debug("Saving prefs to java.util.prefs");

		// Delete and re-create jar file sub-node
		try {
			prefs.node(JAR_FILE_NODE_NAME).removeNode();
			prefs.flush();
		} catch (BackingStoreException e) {
			// Do nothing, this is OK
		}

		Preferences jarNode = prefs.node(JAR_FILE_NODE_NAME);	// (re)-create
		List<String> driverJarList = session.getDriverJarList();
		Iterator<String> it = driverJarList.iterator();
		for (int i = 0 ; i <= MAX_DRIVER_JAR_FILE_NAMES; i++) {
			if (it.hasNext()) {
				String name = it.next();
				logger.debug("Putting JAR " + i + " " + name);
				jarNode.put(jarFilePrefName(i), name);
			}
		}

		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			throw new RuntimeException("Unable to flush Java preferences", e);
		}
	}

	/**
	 * Make up a prefs name for a given number
	 */
	private String jarFilePrefName(int i) {
		return  String.format("%s%02d", PREFS_JARFILE_PREFIX, i);
	}

}
