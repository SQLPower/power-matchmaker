/*
 * Copyright (c) 2007, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */


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
