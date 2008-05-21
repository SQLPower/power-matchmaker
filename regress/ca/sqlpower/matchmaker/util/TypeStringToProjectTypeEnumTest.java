/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

import java.sql.SQLException;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.testutil.MockJDBCPreparedStatement;
import ca.sqlpower.testutil.MockJDBCResultSet;

public class TypeStringToProjectTypeEnumTest extends TestCase {

	ProjectMode[] allTypes = ProjectMode.values();

	TypeStringToProjectTypeEnum userType;
	private MockJDBCResultSet rs;
	private String[] names;
	String[] data;
	
	protected void setUp() throws Exception {
		super.setUp();
			
		userType = new TypeStringToProjectTypeEnum();
		rs = new MockJDBCResultSet(1);
		rs.setColumnName(1,"match_type");
		names = new String[1];
		names[0]="match_type";
		data = new String[ProjectMode.values().length+1];
		int i = 0;
		for (ProjectMode type: allTypes) {
			data[i] = type.toString();
			i++;     
		}
	}

	public void testDeepCopy() {
		for (ProjectMode type: allTypes) {
			ProjectMode testCopy = (ProjectMode) userType.deepCopy(type);
			assertEquals("Invalid project type",type,testCopy);
				
		}
	}
	
	public void testNullGet() throws SQLException {
		for	(int i = 0;i < data.length; i++){	
			Object[] row = {data[i]};
			rs.addRow(row);
			rs.next();
			ProjectMode get = (ProjectMode)userType.nullSafeGet(rs, names, null);
			if (i < allTypes.length) {
				assertEquals("The project type is not correct", allTypes[i], get);
			} else {
				assertEquals("The result is not correct",null, get);
			}
				
		}
	}
	
	
	public void testNullSafeSetAtFirstIndex() throws SQLException{
		MockJDBCPreparedStatement statements = new MockJDBCPreparedStatement(11);
		for	(int i = 0;i < data.length; i++){	
			if (i < allTypes.length) {
				userType.nullSafeSet(statements, allTypes[i], 1);
			} else {
				userType.nullSafeSet(statements, null, 1);
			}
			Object[] values = statements.getParameters();
			assertEquals("The project string is not correct", data[i], (String)values[0]);
		}
	}
	
	public void testNullSafeSetAtIndexOtherThanFirst() throws SQLException{
	MockJDBCPreparedStatement statements = new MockJDBCPreparedStatement(11);
		for	(int i = 0;i < data.length; i++){	
			if (i < allTypes.length) {
				userType.nullSafeSet(statements, allTypes[i], 6);
			} else {
				userType.nullSafeSet(statements, null, 6);
			}
			Object[] values = statements.getParameters();
			assertEquals("The project string is not correct", data[i], (String)values[5]);
		}		
	}
		
}
