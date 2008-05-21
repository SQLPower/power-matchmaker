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
import ca.sqlpower.testutil.MockJDBCPreparedStatement;
import ca.sqlpower.testutil.MockJDBCResultSet;

public class StringsToViewSpecTest extends TestCase {

    /**
     * The object under test.
     */
    private StringsToViewSpec userType;
    
    /**
     * An array of example objects that the tests will attempt to
     * convert to strings.
     */
    private ViewSpec[] testViewSpecs;

    /**
     * A fake result set that simulates what Hibernate would pass to
     * the userType in real life.  It is not pre-populated with rows
     * of data by setUp, but the column names are set up.
     */
	private MockJDBCResultSet rs;
    
    /**
     * The column names of rs which hibernate would give to the user type.
     */
	private String[] names;
    
    /**
     * The data that tests can feed into the result set rs.
     */
    private String[][] data;
	
	protected void setUp() throws Exception {
		super.setUp();
			
		userType = new StringsToViewSpec();
		
		rs = new MockJDBCResultSet(3);
		names = new String[3];
		rs.setColumnName(1,"select");
		names[0]="select";
		rs.setColumnName(2,"from");
		names[1]="from";
		rs.setColumnName(3,"where");
		names[2]="where";
		testViewSpecs = new ViewSpec[3];
		testViewSpecs[0] = new ViewSpec("Select *","from","where");
		testViewSpecs[1] = new ViewSpec("Select 1","from 1","where 1");
		testViewSpecs[2] = new ViewSpec("Select 2","from 2","where 2");
		
		data = new String[testViewSpecs.length+1][3];
		for (int i=0; i< testViewSpecs.length; i++) {
			data[i][0] = testViewSpecs[i].getSelect();
			data[i][1] = testViewSpecs[i].getFrom();
			data[i][2] = testViewSpecs[i].getWhere();     
		}
        // note, the data array has one extra entry which is left null (for testing null safety of nullSafeGet)
	}

	public void testDeepCopy() {
		for (ViewSpec query: testViewSpecs) {
			ViewSpec testCopy = (ViewSpec) userType.deepCopy(query);
			assertEquals("Invalid query",query,testCopy);
				
		}
	}
	
	public void testNullGet() throws SQLException {
        rs.addRow(new Object[] {null, null, null});
        rs.next();
        ViewSpec spec = (ViewSpec) userType.nullSafeGet(rs, names, null);
	    assertNull("Spec should have been null because all data was null", spec);
	}
	
    public void testNonNullGet() throws SQLException {
        rs.addRow(new Object[] {"select clause", "from clause", "where clause"});
        rs.next();
        ViewSpec spec = (ViewSpec) userType.nullSafeGet(rs, names, null);
        assertEquals("select clause", spec.getSelect());
        assertEquals("from clause", spec.getFrom());
        assertEquals("where clause", spec.getWhere());
    }
	
	public void testNullSafeSetAtFirstIndex() throws SQLException{
		MockJDBCPreparedStatement statements = new MockJDBCPreparedStatement(11);
		for	(int i = 0;i < data.length; i++){	
			if (i < testViewSpecs.length) {
				userType.nullSafeSet(statements, testViewSpecs[i], 1);
			} else {
				userType.nullSafeSet(statements, null, 1);
			}
			Object[] values = statements.getParameters();
			assertEquals("The select string is not correct", data[i][0], (String)values[0]);
			assertEquals("The from string is not correct", data[i][1], (String)values[1]);
			assertEquals("The where string is not correct", data[i][2], (String)values[2]);
		}
	}
	
	public void testNullSafeSetAtIndexOtherThanFirst() throws SQLException{
	MockJDBCPreparedStatement statements = new MockJDBCPreparedStatement(11);
		for	(int i = 0;i < data.length; i++){	
			if (i < testViewSpecs.length) {
				userType.nullSafeSet(statements, testViewSpecs[i], 6);
			} else {
				userType.nullSafeSet(statements, null, 6);
			}
			Object[] values = statements.getParameters();
			assertEquals("The select string is not correct for dataset "+i, data[i][0], (String)values[5]);
			assertEquals("The from string is not correct for dataset "+i, data[i][1], (String)values[6]);
			assertEquals("The where string is not correct for dataset "+i, data[i][2], (String)values[7]);
		}		
	}
		
}
