package ca.sqlpower.matchmaker.prefs;

import java.util.List;

/**
 * A JarFileListMaintainer deals with the user to let them add
 * or move/remove driver jars, e.g., the ArchitectSession; the interface
 * was extracted to let the PreferencesManager not have to know about
 * particular classes such as the ArchitectSession.
 */
public interface JarFileListMaintainer {

	public void addDriverJar(String jarName);

	public void removeDriverJar(String jarName);

	public List<String> getDriverJarList();
}
