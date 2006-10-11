package ca.sqlpower.matchmaker.prefs;

import java.util.prefs.Preferences;

/**
 * A PreferencesUser registers with the PreferencesManager at
 * startup and then is called with a Preferences object when the
 * PreferencesManager loads; stores happen "automatically" by writing
 * settings back into this Preferences object.
 * @see java.util.Preferences
 */
public interface PreferencesUser {

	/** Passed from the framework to push properties to your part of the application
	 * at startup and when things change; you need to ask the Map for your properties.
	 * @param props
	 */
	public abstract void setPreferencesRootNode(Preferences data);

}
