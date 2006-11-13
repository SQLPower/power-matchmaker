package ca.sqlpower.matchmaker.prefs;

import java.util.Date;
import java.util.prefs.Preferences;

import junit.framework.TestCase;

/**
 * Test the PreferencesManager by providing Mock instances of both
 * the PreferencesUser and JarFileListMaintainer interfaces.
 */
public class PreferencesManagerTest extends TestCase {

	/** The object we are testing */
	private PreferencesManager prefsManager;

	public void setUp() throws Exception {
		prefsManager = PreferencesManager.getDefaultInstance();
		PreferencesManager pm2 = PreferencesManager.getDefaultInstance();
		assertNotNull(prefsManager);
		assertSame(prefsManager, pm2);
	}

	/**
	 * Try an end-to-end run of testing.
	 * @throws Exception
	 */
	public void testMega() throws Exception {

		Preferences p = PreferencesManager.getRootNode();

		String date = new Date().toString();
		String key = "fred0000";
		p.put(key, date);

		assertEquals(date, p.get(key, null));
		assertSame(date, p.get(key, null));
	}
}
