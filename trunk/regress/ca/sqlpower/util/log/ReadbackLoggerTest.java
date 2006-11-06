package ca.sqlpower.util.log;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import ca.sqlpower.matchmaker.util.log.Log;
import ca.sqlpower.matchmaker.util.log.LogFactory;
import junit.framework.TestCase;

public class ReadbackLoggerTest extends TestCase {

	public void testReadbackLoggerReadsAllLines() throws Exception {
		File f = File.createTempFile("goo", "gar");
		f.deleteOnExit();
		PrintWriter p = new PrintWriter(new FileWriter(f));
		int[] testMessages = new int[] { 0, 1, 2 };
		for (int i : testMessages) {
			p.println("Line " + i);
		}
		p.close();

		Log r = LogFactory.getReadbackLogger(f.getAbsolutePath());
		List<String> log = r.readAsList();
		assertEquals(log.size(), testMessages.length);
		assertEquals("Line 0", log.get(0));
		r.close();
	}
}
