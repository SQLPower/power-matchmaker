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
public class PreferencesManagerTest extends TestCase {

	private static final String DRIVER_TWO = "c.d.e.f.g.Driver";
	private static final String DRIVER_ONE = "a.b.c.d.e.Driver";
	private PreferencesManager pm;

	public void setUp() {
		pm = PreferencesManager.getDefaultInstance();
		PreferencesManager pm2 = PreferencesManager.getDefaultInstance();
		assertNotNull(pm);
		assertSame(pm, pm2);
	}

	boolean consumerWasCalled;

	List<String> driverJars = new ArrayList<String>();
	JarFileListMaintainer session = new JarFileListMaintainer() {
		public void addDriverJar(String jarName) {
			driverJars.add(jarName);
		}

		public List<String> getDriverJarList() {
			return driverJars;
		}

		public void removeDriverJar(String jarName) {
			driverJars.remove(jarName);
		}
	};

	PreferencesUser mockListener = new PreferencesUser() {

		public void setPreferencesRootNode(Preferences data) {
			System.out.println("PreferencesManagerTest.push()");
			consumerWasCalled = true;
		}
	};

	public void testMega() throws Exception {
		consumerWasCalled = false;

		pm.addPreferencesListener(mockListener);

		session.addDriverJar(DRIVER_ONE);
		session.addDriverJar(DRIVER_TWO);
		pm.load(session);
		assertTrue(consumerWasCalled);
		assertEquals(DRIVER_ONE, session.getDriverJarList().get(0));
		assertEquals(DRIVER_TWO, session.getDriverJarList().get(1));

		List<String> newDrivers = Arrays.asList(new String[] { "x.y.Driver", "z.z.Driver" });
		driverJars  = newDrivers;
		pm.store(session);

		// Backdoor verify that it updated Preferences correctly
		Preferences p = Preferences.userNodeForPackage(ca.sqlpower.architect.ArchitectSession.class);
		Preferences q = p.node(PreferencesManager.JAR_FILE_NODE_NAME);
		System.out.println("PreferencesManagerTest.testOne(): jarNode " + q);
		for (String n : q.keys()) {
			System.out.println("q child " + n);
		}
		assertEquals(newDrivers.get(1), q.get(
				PreferencesManager.PREFS_JARFILE_PREFIX + "01", null));
	}


}
