package ca.sqlpower.matchmaker.prefs;

import java.util.AbstractList;
import java.util.prefs.Preferences;

/**
 * A class that extends ArrayList to have the backing list
 * stored in Preferences; this replaces the previous JarFileManager
 * and the concomitant JarFileListMaintainer interfaces.
 */
public class JarFileList extends AbstractList<String> {

	protected static final String JAR_FILE_NODE_NAME = "jarfiles";

	protected static final String PREFS_JARFILE_PREFIX = "JDBCJarFile.";

	private final Preferences backingStore;
	private static final int MAX = 99;

	private String[] list = new String[MAX];
	private int size = 0;

	/**
	 * Construct a JarFileList with a given Preferences
	 * @param rootNode This Application's root node, as might
	 * be obtained from PreferencesManager.getRootNode();
	 */
	public JarFileList(Preferences rootNode) {
		backingStore = rootNode.node(JAR_FILE_NODE_NAME);
	}

	@Override
	public String remove(int index) {
		checkIndex(index);
		String old = list[index];
		backingStore.remove(jarFilePrefName(size));
		for (int i = index; i < size; i++) {
			// XXX
			list[i] = list[i + 1];
			String tmp = backingStore.get(jarFilePrefName(i + 1), null);
			if (tmp == null) {
				break;
			}
			backingStore.put(jarFilePrefName(i), tmp);
		}
		size -= 1;
		return old;
	}

	@Override
	public void add(int index, String element) {
		backingStore.put(jarFilePrefName(size), element);
		list[size++] = element;
	}

	@Override
	public String get(int index) {
		checkIndex(index);
		return list[index];
	}

	@Override
	public int size() {
		return size;
	}

	/** Throw an exception if index is out of range */
	private void checkIndex(int index) {
		if (index >= this.size) {
			throw new IndexOutOfBoundsException(""+index);
		}
	}

	/**
	 * Make up a prefs name for a given number
	 */
	private static String jarFilePrefName(int i) {
		return  String.format("%s%02d", PREFS_JARFILE_PREFIX, i);
	}

}
