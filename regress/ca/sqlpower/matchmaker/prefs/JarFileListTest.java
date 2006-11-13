package ca.sqlpower.matchmaker.prefs;

import junit.framework.TestCase;

public class JarFileListTest extends TestCase {

	JarFileList list = new JarFileList(PreferencesManager.getRootNode());

	public void testAdd() throws Exception {
		list.add("jar1.jar");
		assertEquals(1, list.size());
		assertEquals("jar1.jar", list.get(0));
		try {
			list.get(1);
			fail("didn't throw");
		} catch (IndexOutOfBoundsException e) {
			//
		}
	}

	/**
	 * Test that remove pushes the elements following the removal
	 * back one level to replace the one that got removed.
	 */
	public void testRemove() throws Exception {
		list.add("jar1.jar");
		list.add("jar2.jar");
		list.add("jar3.jar");
		assertEquals(3, list.size());
		list.remove(1);
		assertEquals(2, list.size());

		/** This is the critical assertion! */
		assertEquals("jar3.jar", list.get(1));

		try {
			list.remove(2);
			fail("didn't throw");
		} catch (IndexOutOfBoundsException e) {
			//
		}
	}

	/** Check obvious boundary conditions */
	public void testRemoveBoundaries() {
		list.add("jar1.jar");
		list.add("jar2.jar");
		list.add("jar3.jar");
		list.add("jar4.jar");

		list.remove(3);

		list.remove(0);
		assertEquals("jar2.jar", list.get(0));
	}

	/** Check that AbstractList correctly forwards to our remove method */
	public void testClear() {
		list.add("jar1.jar");
		list.add("jar2.jar");
		list.add("jar3.jar");
		list.add("jar4.jar");
		list.clear();
		assertEquals(0, list.size());
	}
}
