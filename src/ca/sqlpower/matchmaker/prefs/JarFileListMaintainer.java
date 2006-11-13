package ca.sqlpower.matchmaker.prefs;

import java.util.List;

/**
 * A JarFileListMaintainer is a UI that lets the user add
 * or move/remove driver jars, e.g., the ArchitectSession; the interface
 * was extracted to let the PreferencesManager not have to know about
 * particular classes such as the ArchitectSession.
 * <p>
 * A JarFileListMaintainer will likely use the JarFileManager,
 * by passing itself as the callback argument to load()/store().
 */
public interface JarFileListMaintainer {

	public void addDriverJar(String jarName);

	public void removeDriverJar(String jarName);

	public List<String> getDriverJarList();

	public void removeAllDriverJars();
}
