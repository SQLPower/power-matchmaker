package ca.sqlpower.util.log;

import ca.sqlpower.matchmaker.util.log.LogFactory;
import junit.framework.TestCase;

public class LogFactoryTest extends TestCase {

	public void testReadbackLoggerFailsProperly() {
		try {
			LogFactory.getReadbackLogger("No Such File");
			fail("Did not throw exception for non-existent file");
		} catch (IllegalArgumentException e) {
			System.out.println("Caught expected " + e);
		}
	}

}
