package ca.sqlpower.util.log;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.util.log.Level;
import ca.sqlpower.matchmaker.util.log.Log;
import ca.sqlpower.matchmaker.util.log.LogFactory;

/**
 * Test the basic logging functionality.
 */
public class LogTest extends TestCase {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testSimpleLogging() {
		Log log = LogFactory.getLogger(Level.INFO, "whee");
		log.log(Level.WARNING, "This message should appear");
		log.log(Level.DEBUG, "This message should NOT appear");
        // FIXME remove the log file
	}

}
