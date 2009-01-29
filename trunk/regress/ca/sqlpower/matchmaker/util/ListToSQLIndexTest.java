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

import junit.framework.TestCase;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLIndex.AscendDescend;
import ca.sqlpower.sqlobject.SQLIndex.Column;
import ca.sqlpower.testutil.MockJDBCPreparedStatement;
import ca.sqlpower.testutil.MockJDBCResultSet;

public class ListToSQLIndexTest extends TestCase {

	SQLIndex index;
	SQLIndex.Column c0;
	SQLIndex.Column c1;
	SQLIndex.Column c2;
	SQLIndex.Column c3;
	SQLIndex.Column c4;
	SQLIndex.Column c5;
	SQLIndex.Column c6;
	SQLIndex.Column c7;
	SQLIndex.Column c8;
	SQLIndex.Column c9;
	SQLIndex index2;
	SQLIndex.Column c20;
	SQLIndex.Column c21;
	SQLIndex.Column c22;
	SQLIndex.Column c23;
	SQLIndex.Column c24;
	SQLIndex.Column c25;
	SQLIndex.Column c26;
	SQLIndex.Column c27;
	SQLIndex.Column c28;
	SQLIndex.Column c29;

	ListToSQLIndex userType;
	private MockJDBCResultSet rs;
	private String[] names;
	
	
	protected void setUp() throws Exception {
		super.setUp();
		index = new SQLIndex();
		index.setName("TestIndex");
		index2 = new SQLIndex();
		index2.setName("TestIndex");
		
		c0 = index.new Column("Test0", AscendDescend.UNSPECIFIED);
		c1 = index.new Column("Test1", AscendDescend.UNSPECIFIED);
		c2 = index.new Column("Test2", AscendDescend.UNSPECIFIED);
		c3 = index.new Column("Test3", AscendDescend.UNSPECIFIED);
		c4 = index.new Column("Test4", AscendDescend.UNSPECIFIED);
		c5 = index.new Column("Test5", AscendDescend.UNSPECIFIED);
		c6 = index.new Column("Test6", AscendDescend.UNSPECIFIED);
		c7 = index.new Column("Test7", AscendDescend.UNSPECIFIED);
		c8 = index.new Column("Test8", AscendDescend.UNSPECIFIED);
		c9 = index.new Column("Test9", AscendDescend.UNSPECIFIED);		
		index.addChild(c0);
		index.addChild(c1);
		index.addChild(c2);
		index.addChild(c3);
		index.addChild(c4);
		index.addChild(c5);
		index.addChild(c6);
		index.addChild(c7);
		index.addChild(c8);
		index.addChild(c9);	
		
		c20 = index2.new Column("Test0", AscendDescend.UNSPECIFIED);
		c21 = index2.new Column("Test1", AscendDescend.UNSPECIFIED);
		c22 = index2.new Column("Test2", AscendDescend.UNSPECIFIED);
		c23 = index2.new Column("Test3", AscendDescend.UNSPECIFIED);
		c24 = index2.new Column("Test4", AscendDescend.UNSPECIFIED);
		c25 = index2.new Column("Test5", AscendDescend.UNSPECIFIED);
		c26 = index2.new Column("Test6", AscendDescend.UNSPECIFIED);
		c27 = index2.new Column("Test7", AscendDescend.UNSPECIFIED);
		c28 = index2.new Column("Test8", AscendDescend.UNSPECIFIED);
		c29 = index2.new Column("Test9", AscendDescend.UNSPECIFIED);
		index2.addChild(c20);
		index2.addChild(c21);
		index2.addChild(c22);
		index2.addChild(c23);
		index2.addChild(c24);
		index2.addChild(c25);
		index2.addChild(c26);
		index2.addChild(c27);
		index2.addChild(c28);
		index2.addChild(c29);		
		userType = new ListToSQLIndex();
		
		rs = new MockJDBCResultSet(11);
		rs.setColumnName(1,"pkName");
		names = new String[11];
		names[0]="pkName";
		for (int i=1; i < 11; i++){
			rs.setColumnName(i+1, "index_column_name"+i);
			names[i]="index_column_name"+i;
		}
	}

	public void testDeepCopy() throws Exception {
		SQLIndex testCopy = (SQLIndex) userType.deepCopy(index);
		assertEquals("The test copy should have the same number of children",
						testCopy.getChildCount(), index.getChildCount());
		for (int i=0; i < testCopy.getChildren().size(); i++){			
			if (!(testCopy.getChildren().get(i) instanceof SQLIndex.Column)){
				fail("SQLIndex child cannot be anything else other than SQLColumn");
			}
			SQLIndex.Column testChild = (Column) index.getChild(i);
			SQLIndex.Column copyChild = (Column) testCopy.getChild(i);			
			assertEquals("The two elements should have the same name",
									testChild.getName(), copyChild.getName());
			assertEquals("The two elements should have the same ordering",
					testChild.getAscendingOrDescending(), copyChild.getAscendingOrDescending());
		}		
	}
	
	public void testNullGetWithAllValidColumns() throws Exception {

		String[] data = new String[11];
		data[0] = "pkName";
		for (int i=1; i>11;i++){
			data[i]= "index_column_name"+i;
		}
		
		rs.addRow(data);
		rs.next();
		SQLIndex ind = (SQLIndex)userType.nullSafeGet(rs, names, null);
		assertNotNull("We should not be getting a null value when we don't pass one in",ind);
		assertEquals("The primary key is not correct",  "pkName", ind.getName());
	
		for (int j=1; j>11; j++){
			assertEquals("The child does not have the right name", 
					"index_column_name"+j,ind.getChild(j).getName()); 
		}
	}
	
	public void testNullGetWithNullsInList() throws Exception {
		String[] data = new String[11];
		data[0] = "pkName";
		for (int i=1; i<11;i++){
			data[i]= "index_column_name"+i;
		}
		data[6] = null;
		rs.addRow(data);
		rs.next();
		SQLIndex ind = (SQLIndex)userType.nullSafeGet(rs, names, null);
		assertNotNull("We should not be getting a null value when we don't pass one in",ind);
		assertEquals("The primary key is not correct", ind.getName(), "pkName");
		assertEquals("The children size of the SQLIndex is not correct"+ind.getChildren(), 
				ind.getChildCount(), 9);
		for (int j=1; j>10; j++){
			if (j < 6){
				assertEquals("The child does not have the right name", 
						"index_column_name"+j, ind.getChild(j).getName() );
			}else {
				assertEquals("The child does not have the right name", 
						"index_column_name"+(j+1),ind.getChild(j).getName());
			}
		}
	}
	
	public void testNullSafeSetAtFirstIndex() throws Exception{
		MockJDBCPreparedStatement statements = new MockJDBCPreparedStatement(11);
		userType.nullSafeSet(statements, index, 1);
        statements.checkAllParametersSet();
		Object[] values = statements.getParameters();
		assertEquals("The index has the wrong name","TestIndex", (String)values[0]);
		for (int i=1; i < 11; i++){
			assertEquals("The columns have the wrong name","Test"+(i-1), 
					(String)values[i]);			
		}		
	}
	
	public void testNullSafeSetAtIndexOtherThanFirst() throws Exception{
		MockJDBCPreparedStatement statements = new MockJDBCPreparedStatement(18);
		final int startPosition = 5;
        userType.nullSafeSet(statements, index, startPosition);
        statements.checkParametersSet(startPosition, 11);
		Object[] values = statements.getParameters();
		assertEquals("The index has the wrong name","TestIndex", (String) values[startPosition-1]);
		for (int i=startPosition; i < startPosition+10; i++){
			assertEquals("The columns have the wrong name","Test"+(i-startPosition), 
					(String)values[i]);			
		}		
	}
		
	public void testEquals() throws Exception {
		assertTrue("The two indices are not equal, but should be",userType.equals(index,index2));
		assertTrue("The two indices are not equal, but should be",userType.equals(index,userType.deepCopy(index)));
		String oldName = index2.getName();
		index2.setName("some other name");
		assertFalse("The two indices are equal, but should not be",userType.equals(index,index2));
		index2.setName(oldName);
		index2.addChild(index2.new Column("Test10", AscendDescend.UNSPECIFIED));
		assertFalse("The two indices are equal, but should not be",userType.equals(index,index2));
	}
}
