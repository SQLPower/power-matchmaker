package ca.sqlpower.matchmaker.prefs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.util.NamesToSQLTable;

/**
 * Test the PreferencesManager by providing Mock instances of both
 * the PreferencesUser and JarFileListMaintainer interfaces.
 */
public class PreferencesManagerTest extends TestCase {

	private static final String DRIVER_ONE = "one.DriverJar";
	private static final String DRIVER_TWO = "two.DriverJar";
	private static final String DRIVER_THREE = "three.DriverJar";
	private static final String DRIVER_FOUR = "four.DriverJar";

	/** The object we are testing */
	private PreferencesManager prefsManager;

	/**
	 * Set up an alternate prefs node, since this test is in the
	 * same package as that of the code it's testing, but we
	 * don't want the test to erase the developer's driverlist!
	 * So, pick a victim in some other package...
	 */
	final static Preferences actualPrefsNode = Preferences.userNodeForPackage(NamesToSQLTable.class);
	/** Backdoor: tell PrefsManager not to mess up the
	 * developer's actual Preferences; use our own for testing.
	 * Only needs to be done once, not in setUp or constructor.
	 */
	static {
		PreferencesManager.setPreferences(actualPrefsNode);
	}

	public void setUp() throws Exception {
		prefsManager = PreferencesManager.getDefaultInstance();
		PreferencesManager pm2 = PreferencesManager.getDefaultInstance();
		assertNotNull(prefsManager);
		assertSame(prefsManager, pm2);
		actualPrefsNode.clear(); // start each test "tabula rasa"
		actualPrefsNode.flush();
	}

	boolean prefsUserWasNotified;

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
	 * This Mock represents a class that needs access to Prefs;
	 */
	PreferencesUser mockPrefsUser = new PreferencesUser() {

		public void setPreferencesRootNode(Preferences data) {
			System.out.println("PreferencesManagerTest.push()");
			prefsUserWasNotified = true;
		}
	};

	/**
	 * Try an end-to-end run of testing.
	 * @throws Exception
	 */
	public void testMega() throws Exception {
		prefsUserWasNotified = false;

		prefsManager.addPreferencesListener(mockPrefsUser);
		// This should not notifiy the PrefsUser, yet.
		assertFalse(prefsUserWasNotified);

		// Load from (hopefully-empty) prefs node.
		// This should call the mockPrefsUser's setPreferencesRootNode()
		prefsManager.load(mockMaintainer);
		assertTrue(prefsUserWasNotified);
		assertEquals(0, mockMaintainer.getDriverJarList().size());

		mockMaintainer.addDriverJar(DRIVER_ONE);
		mockMaintainer.addDriverJar(DRIVER_TWO);

		List<String> newDrivers =
			Arrays.asList(new String[] { DRIVER_THREE, DRIVER_FOUR });

		mockMaintainerDriverJars.addAll(newDrivers);
		prefsManager.store(mockMaintainer);

		Preferences q = actualPrefsNode.node(PreferencesManager.JAR_FILE_NODE_NAME);
		System.out.println("PreferencesManagerTest.testOne(): jarNode " + q);
		for (String n : q.keys()) {
			System.out.println("q child " + n + " " + q.get(n, null));
		}
		assertEquals(DRIVER_TWO, q.get(
				PreferencesManager.PREFS_JARFILE_PREFIX + "01", null));
	}
}
