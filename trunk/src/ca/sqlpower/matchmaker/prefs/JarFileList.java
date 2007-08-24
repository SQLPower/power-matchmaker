/*
 * Copyright (c) 2007, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.i 
 */

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
