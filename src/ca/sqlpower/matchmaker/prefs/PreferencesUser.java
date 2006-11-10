package ca.sqlpower.matchmaker.prefs;

import java.util.prefs.Preferences;

/**
 * The PreferencesUser interface is a listener interface for classes
 * that want to be notified when values for preferences change.
 * 
 * <p>A PreferencesUser registers with the PreferencesManager at
 * startup and then is called with a Preferences object when the
 * PreferencesManager loads; stores happen "automatically" by writing
 * settings back into this Preferences object.
 * 
 * @see java.util.Preferences
 */
public interface PreferencesUser {

	/**
     * The framework calls this method to push properties to your listener at
     * startup and when things change; you need to ask the Map for your
     * properties.
     * 
     * @param props The current Preferences information.
     */
	public abstract void setPreferencesRootNode(Preferences data);

}
