package ca.sqlpower.matchmaker.prefs;

import java.util.Iterator;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

/**
 * Do a little dance for handling a list of
 * JDBC Driver jar file names.
 * <p>
 * XXX Refactor into and implementation of JarFileListManager
 * so that user code can just and/remove entries anytime.
 * <p>
 * XXX make methods non-static, then extend PropertyChangeSupport
 */
public class JarFileManager {

	private static final int MAX_DRIVER_JAR_FILE_NAMES = 99;

	protected static final String JAR_FILE_NODE_NAME = "jarfiles";

	protected static final String PREFS_JARFILE_PREFIX = "JDBCJarFile.";

	private static final Logger logger = Logger.getLogger(JarFileManager.class);

	// -------------------- Loading the preferences --------------------------

	public static void load(JarFileListMaintainer manager) {
		logger.debug("loading UserSettings from java.util.prefs.");

		Preferences jarNode = PreferencesManager.getRootNode().node(JAR_FILE_NODE_NAME);
		manager.removeAllDriverJars();
		logger.debug("PreferencesManager.load(): jarNode " + jarNode);
		for (int i = 0; i <= MAX_DRIVER_JAR_FILE_NAMES; i++) {
			String jarName = jarNode.get(jarFilePrefName(i), null);
			logger.debug("read Jar File entry: " + jarName);
			if (jarName == null) {
				break;
			}

			logger.debug("Adding JarName: " + jarName);
			manager.addDriverJar(jarName);
		}

	}

	// -------------------- "WRITING THE FILE" --------------------------

	public void store(JarFileListMaintainer manager) {

		logger.debug("Saving prefs to java.util.prefs");
		Preferences prefs = PreferencesManager.getRootNode();
		// Delete and re-create jar file sub-node
		try {
			prefs.node(JAR_FILE_NODE_NAME).removeNode();
			prefs.flush();
			if (prefs.nodeExists(JAR_FILE_NODE_NAME)) {
				System.err.println("Warning: Jar Node Still Exists!!");
			}
		} catch (BackingStoreException e) {
			// Do nothing, this is OK
			logger.warn("Error: BackingStoreException while removing or testing previous Jar Node!!");
		}

		Preferences jarNode = prefs.node(JAR_FILE_NODE_NAME);	// (re)-create
		List<String> driverJarList = manager.getDriverJarList();
		System.out.println("PreferencesManager.store(): size=" + driverJarList.size());
		Iterator<String> it = driverJarList.iterator();
		for (int i = 0; it.hasNext() && i <= MAX_DRIVER_JAR_FILE_NAMES; i++) {
			String name = it.next();
			logger.debug("Putting JAR " + i + " " + name);
			jarNode.put(jarFilePrefName(i), name);
		}

		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			logger.warn("Unable to flush Java preferences", e);
		}
	}

	/**
	 * Make up a prefs name for a given number
	 */
	private static String jarFilePrefName(int i) {
		return  String.format("%s%02d", PREFS_JARFILE_PREFIX, i);
	}

}
