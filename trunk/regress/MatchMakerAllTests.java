import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;

import junit.framework.Test;

import com.gargoylesoftware.base.testing.RecursiveTestSuite;
import com.gargoylesoftware.base.testing.TestFilter;

/**
 * Use the RecursiveTestSuite to run all tests whose name
 * ends in "Test" (enforced by the suite itself) that are
 * not Abstract (enforced by the Filter).
 */
public class MatchMakerAllTests {

	/**
	 *  TRY to load system prefs factory before anybody else uses prefs.
	 */
	static {
		System.getProperties().setProperty(
			"java.util.prefs.PreferencesFactory", "prefs.PreferencesFactory");
		System.err.println("Warning: Changed PreferencesFactory to in-memory version;");
		System.err.println("this means you MUST exit Eclipse after running, before");
		System.err.println("any changes you make in preferences will be saved.");
	}
	public static Test suite() throws IOException {
		// Point this at the top-level of the output folder
		File file = new File("bin");

		TestFilter filt = new TestFilter() {
			public boolean accept(Class aClass) {
				int modifiers = aClass.getModifiers();
				return !Modifier.isAbstract(modifiers);
			}
		};
		return new RecursiveTestSuite(file, filt);	
	}
	
}
