package ca.sqlpower.matchmaker.prefs;

import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

/**
 * Locate the User Preferences Node for the Architect package, and
 * pass it around to people that want it via load-time injection.
 */
public class PreferencesManager {

	private static final Logger logger = Logger.getLogger(PreferencesManager.class);

	private static final PreferencesManager singleton = new PreferencesManager();

    private final static Preferences prefs =
    	// ArchitectSession is not a copy-and-paste error here:
    	Preferences.userNodeForPackage(ca.sqlpower.architect.ArchitectSession.class);

    private PreferencesManager() {
		// private constructor, is a singleton
		logger.info("Create PreferencesManager singleton");
	}

	public static PreferencesManager getDefaultInstance() {
		return singleton;
	}

	public static Preferences getRootNode() {
		return prefs;
	}

}
