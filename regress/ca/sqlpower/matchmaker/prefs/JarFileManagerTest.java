package ca.sqlpower.matchmaker.prefs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import junit.framework.TestCase;

/**
 * Test the PreferencesManager by providing Mock instances of both
 * the PreferencesUser and JarFileListMaintainer interfaces.
 */
public class JarFileManagerTest extends TestCase {

	private static final String DRIVER_ONE = "one.DriverJar";
	private static final String DRIVER_TWO = "two.DriverJar";
	private static final String DRIVER_THREE = "three.DriverJar";
	private static final String DRIVER_FOUR = "four.DriverJar";

	/** The object we are testing */
	private PreferencesManager prefsManager;

	public void setUp() throws Exception {
		prefsManager = PreferencesManager.getDefaultInstance();
		PreferencesManager pm2 = PreferencesManager.getDefaultInstance();
		assertNotNull(prefsManager);
		assertSame(prefsManager, pm2);
	}

	List<String> mockMaintainerDriverJars = new ArrayList<String>();

	/**
	 * This Mock represents the GUI code that lets the user
	 * add and remove Jar files from their personal list.
	 */
	JarFileListMaintainer mockMaintainer = new JarFileListMaintainer() {
		public void addDriverJar(String jarName) {
			mockMaintainerDriverJars.add(jarName);
		}

		public List<String> getDriverJarList() {
			return mockMaintainerDriverJars;
		}

		public void removeDriverJar(String jarName) {
			mockMaintainerDriverJars.remove(jarName);
		}

		public void removeAllDriverJars() {
			mockMaintainerDriverJars.clear();
		}
	};

	/**
	 * Try an end-to-end run of jar file stuff.
	 * @throws Exception
	 */
	public void testMega() throws Exception {

		assertEquals(0, mockMaintainer.getDriverJarList().size());

		mockMaintainer.addDriverJar(DRIVER_ONE);
		mockMaintainer.addDriverJar(DRIVER_TWO);

		List<String> newDrivers =
			Arrays.asList(new String[] { DRIVER_THREE, DRIVER_FOUR });

		mockMaintainerDriverJars.addAll(newDrivers);

		Preferences q = PreferencesManager.getRootNode().node(JarFileManager.JAR_FILE_NODE_NAME);
		System.out.println("PreferencesManagerTest.testOne(): jarNode " + q);
		for (String n : q.keys()) {
			System.out.println("q child " + n + " " + q.get(n, null));
		}
		assertEquals(DRIVER_TWO, q.get(
				JarFileManager.PREFS_JARFILE_PREFIX + "01", null));
	}
}
