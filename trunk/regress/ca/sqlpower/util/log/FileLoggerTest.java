package ca.sqlpower.util.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.util.log.FileLogger;
import ca.sqlpower.matchmaker.util.log.Level;
import ca.sqlpower.matchmaker.util.log.Log;
import ca.sqlpower.matchmaker.util.log.LogFactory;

public class FileLoggerTest extends TestCase {


	private File logFile;
	private String logFilePath;
	private Log log;
	
	@Override
	protected void setUp() throws Exception {
		logFile = File.createTempFile("foo", "bar");
		logFilePath = logFile.getAbsolutePath();
		log = LogFactory.getLogger(Level.INFO, logFilePath);
		assertSame("Didn't get object of the correct type for this test",
				FileLogger.class, log.getClass());
	}

	@Override
	protected void tearDown() throws Exception {
		logFile.delete();
	}
	
	public void testWriteToLog() throws Exception {
		final String MESSAGE_TEXT = "Hello World";
		log.log(Level.INFO, MESSAGE_TEXT);
		log.close();

		assertTrue(logFile.canRead());
		BufferedReader is = new BufferedReader(new FileReader(logFile));
		String line = is.readLine();
		assertTrue(line.endsWith(MESSAGE_TEXT));
		is.close();
	}
	
	public void testIsReadableWhenFalse() {
		File testTheTest = new File("/fake/path/that/doesnt/exist");
		assertFalse("Woops, our fake file actually exists!", testTheTest.canRead());
		Log myLog = LogFactory.getLogger(Level.INFO, "/fake/path/that/doesnt/exist");
		assertFalse(myLog.isReadable());
	}

	public void testIsReadableWhenTrue() {
		assertTrue("Woops, our real file doesn't exist!",
				logFile.canRead());
		assertTrue(log.isReadable());
	}

	public void testIsWritableWhenFalse() {
		File testTheTest = new File("/fake/path/that/doesnt/exist");
		assertFalse("Woops, our fake file actually exists!", testTheTest.canWrite());
		Log myLog = LogFactory.getLogger(Level.INFO, "/fake/path/that/doesnt/exist");
		assertFalse(myLog.isWritable());
	}
	
	public void testIsWritableWhenTrue() {
		assertTrue("Woops, our real log file doesn't exist!",
				logFile.canWrite());
		assertTrue(log.isWritable());
	}

}
