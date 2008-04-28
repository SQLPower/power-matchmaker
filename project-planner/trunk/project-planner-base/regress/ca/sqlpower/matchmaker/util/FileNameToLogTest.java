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


package ca.sqlpower.matchmaker.util;

import java.io.File;
import java.sql.SQLException;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.testutil.MockJDBCPreparedStatement;
import ca.sqlpower.testutil.MockJDBCResultSet;


public class FileNameToLogTest extends TestCase {

	File[] allLogs;

	FileNameToLog userType;
	private MockJDBCResultSet rs;
	private String[] names;
	String[] data;

	protected void setUp() throws Exception {
		super.setUp();

		userType = new FileNameToLog();

		rs = new MockJDBCResultSet(1);
		rs.setColumnName(1,"file_name");
		names = new String[1];
		names[0]="file_name";
		data = new String[ProjectMode.values().length+1];
		allLogs = new File[3];

		for (int i = 0; i<3;i++) {
			data[i] = "File"+i;
			allLogs[i] = new File(data[i]);
		}
	}

	public void testDeepCopy() {

		File testCopy = (File) userType.deepCopy(allLogs[0]);
		assertEquals("Invalid log",allLogs[0],testCopy);
		testCopy = (File) userType.deepCopy(allLogs[1]);
		assertEquals("Invalid log",allLogs[1],testCopy);
		testCopy = (File) userType.deepCopy(allLogs[2]);
		assertEquals("Invalid log",allLogs[2],testCopy);

	}

	public void testNullGet() throws SQLException {
		for	(int i = 0;i < data.length; i++){
			Object[] row = {data[i]};
			rs.addRow(row);
			rs.next();
			File get = (File)userType.nullSafeGet(rs, names, null);
			if (i < allLogs.length) {
				assertEquals("The log type is not correct", allLogs[i], get);
			} else {
				assertEquals("The result is not correct",null, get);
			}

		}
	}


	public void testNullSafeSetAtFirstIndex() throws SQLException{
		MockJDBCPreparedStatement statements = new MockJDBCPreparedStatement(11);
		for	(int i = 0;i < data.length; i++){
			if (i < allLogs.length) {
				userType.nullSafeSet(statements, allLogs[i], 1);
			} else {
				userType.nullSafeSet(statements, null, 1);
			}
			Object[] values = statements.getParameters();
			assertEquals("The Log is not correct", data[i], (String)values[0]);
		}
	}

	public void testNullSafeSetAtIndexOtherThanFirst() throws SQLException{
	MockJDBCPreparedStatement statements = new MockJDBCPreparedStatement(11);
		for	(int i = 0;i < data.length; i++){
			if (i < allLogs.length) {
				userType.nullSafeSet(statements, allLogs[i], 6);
			} else {
				userType.nullSafeSet(statements, null, 6);
			}
			Object[] values = statements.getParameters();
			assertEquals("The match string is not correct", data[i], (String)values[5]);
		}
	}

}
