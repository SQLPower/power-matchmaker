package ca.sqlpower.util.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.util.log.Level;
import ca.sqlpower.matchmaker.util.log.Log;
import ca.sqlpower.matchmaker.util.log.LogFactory;

public class FileLoggerTest extends TestCase {

	final static String MESSAGE_TEXT = "Hello World";

	public void testOne() throws Exception {
		final File f = File.createTempFile("foo", "bar");
		f.deleteOnExit();
		final String fixedFileName = f.getAbsolutePath();
		final Log log =
			LogFactory.getLogger(Level.INFO, fixedFileName);
		log.log(Level.INFO, MESSAGE_TEXT);
		log.close();

		assertTrue(f.exists() && f.canRead());
		BufferedReader is = new BufferedReader(new FileReader(f));
		String line = is.readLine();
		assertTrue(line.endsWith(MESSAGE_TEXT));
		is.close();
	}
}
