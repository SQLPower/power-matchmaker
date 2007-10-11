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


package ca.sqlpower.matchmaker;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import junit.framework.TestCase;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.matchmaker.ColumnMergeRules.MergeActionType;
import ca.sqlpower.matchmaker.munge.MungeProcess;

/**
 * This is a test for the merge processor. It focuses on testing the
 * functionality of the actions of the merge processor. The match result
 * table data is not extensive but should be sufficient to test the 
 * functionalities. Implementations of different database platforms 
 * should be written to implement this class.
 */
public abstract class AbstractMergeProcessorTest extends TestCase {

    Match match;
    MergeProcessor mpor;
    TestingMatchMakerSession session;
    Connection con;
	
	TableMergeRules tmr = new TableMergeRules();
	ColumnMergeRules cmr_string = new ColumnMergeRules();
	ColumnMergeRules cmr_date = new ColumnMergeRules();
	ColumnMergeRules cmr_number = new ColumnMergeRules();

	
	/**
	 * Subclasses need to implement this method to set the correct
	 * database of the desired platform. The match and session should
	 * also be set accordingly. See {@link MergeProcessorOracleTest#setUp()}
	 * for example.
	 */
   protected abstract void setUp() throws Exception;
    
    /**
     * This is a protected method that sets defaults for the column 
     * merge rules and table merge rule. Subclasses need to implement this
     * method to setup the match source table and match result table on the
     * database. See {@link MergeProcessorOracleTest#populateTables()} for 
     * example. This should be called before each test case. 
     */
    protected void populateTables() throws Exception {
		cmr_string.setActionType(MergeActionType.IGNORE);
		cmr_date.setActionType(MergeActionType.IGNORE);
		cmr_number.setActionType(MergeActionType.IGNORE);
		
		tmr.addChild(cmr_string);
		tmr.addChild(cmr_date);
		tmr.addChild(cmr_number);
		tmr.setDeleteDup(false);

		match.addTableMergeRule(tmr);
		MungeProcess mp = new MungeProcess();
		mp.setName("test");
		match.addMatchRuleSet(mp);
    }
    
    /**
     * This tests on a unmatch, it should do nothing.
     */
    public void testUnmatch() throws Exception {
    	populateTables();
    	
		cmr_string.setActionType(MergeActionType.AUGMENT);
		cmr_date.setActionType(MergeActionType.AUGMENT);
		cmr_number.setActionType(MergeActionType.AUGMENT);
		mpor.call();
		
		con = session.getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs;
    	
    	String sql = "SELECT * FROM " + getFullTableName() + " WHERE ID = 4";
		rs = stmt.executeQuery(sql);
		if (!rs.next()) {
			fail("Merge deleted master record with id = 4");
		}
		assertEquals("No changes should have been made.", "E", rs.getString(2));
		assertEquals("No changes should have been made.", (new Date(1000*60*60*24*4)).toString(), rs.getDate(3).toString());
		assertEquals("No changes should have been made.", 4, rs.getInt(4));    
    }
       
    /**
     * This tests if the duplicates are deleted when the option is set.
     */
    public void testDeleteDup() throws Exception {
    	populateTables();
		
    	tmr.setDeleteDup(true);
    	
		mpor.call();

		Connection con = session.getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs;
		
		String sql = "SELECT * FROM " + getFullTableName() + " WHERE ID = 1 OR ID = 3";
		rs = stmt.executeQuery(sql);
		if (rs.next()) {
			fail("Duplicate records not deleted.");
		}
    }
    
    /**
     * This ensures that the MatchType is set as MERGED for each record in the match pool. 
     */
    public void testMatchTypeMerged() throws Exception {
    	populateTables();
    	mpor.call();
		
    	MatchPool matchPool = new MatchPool(match);
    	matchPool.findAll(new ArrayList<SQLColumn>());
    	
    	for (PotentialMatchRecord pm : matchPool.getPotentialMatches()) {
    		assertFalse("MatchType not set as MERGED for " + pm, pm.isMatch());
    	}
    }
    
    public void testAugment() throws Exception {
    	populateTables();
    	
		cmr_string.setActionType(MergeActionType.AUGMENT);
		cmr_date.setActionType(MergeActionType.AUGMENT);
		cmr_number.setActionType(MergeActionType.AUGMENT);
		
		mpor.call();

		Connection con = session.getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs;
		
		String sql = "SELECT * FROM " + getFullTableName() + " WHERE ID = 6";
		rs = stmt.executeQuery(sql);
		if (!rs.next()) {
			fail("Merge deleted master record with id = 6");
		}
		assertEquals("String not augmented.", "E", rs.getString(2));
		assertEquals("Date not augmented.", (new Date(1000*60*60*24*4)).toString(), rs.getDate(3).toString());
		assertEquals("Number not augmented.", 4, rs.getInt(4));
		
		sql = "SELECT * FROM " + getFullTableName() + " WHERE ID = 2";
		rs = stmt.executeQuery(sql);
		if (!rs.next()) {
			fail("Merge deleted master record with id = 2");
		}
		assertEquals("String should not have been augmented.", "C", rs.getString(2));
		assertEquals("Date should not have been augmented.", (new Date(1000*60*60*24*2)).toString(), rs.getDate(3).toString());
		assertEquals("Number should not have been augmented.", 2, rs.getInt(4));
    }
		
    public void testConcat() throws Exception {
    	populateTables();
    	
		cmr_date.setActionType(MergeActionType.CONCAT);
		
		try {
			mpor.call();
			fail("Concat action should not be allowed on date datatypes.");
		} catch (IllegalStateException e) {
			// Correct exception thrown.
		}
		
    	cmr_string.setActionType(MergeActionType.IGNORE);
		cmr_date.setActionType(MergeActionType.IGNORE);
		cmr_number.setActionType(MergeActionType.CONCAT);
		
		try {
			mpor.call();
			fail("Concat action should not be allowed on number datatypes.");
		} catch (IllegalStateException e) {
			// Correct exception thrown.
		}
		
    	cmr_string.setActionType(MergeActionType.CONCAT);
		cmr_date.setActionType(MergeActionType.IGNORE);
		cmr_number.setActionType(MergeActionType.IGNORE);
		
		mpor.call();

		Connection con = session.getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs;
		
		String sql = "SELECT * FROM " + getFullTableName() + " WHERE ID = 6";
		rs = stmt.executeQuery(sql);
		if (!rs.next()) {
			fail("Merge deleted master record with id = 6");
		}
		assertEquals("String not concatenated.", "EFBA", rs.getString(2));
		assertNull("Date not ignored.", rs.getDate(3));
		assertNull("Number not ignored.", rs.getObject(4));
		
		sql = "SELECT * FROM " + getFullTableName() + " WHERE ID = 2";
		rs = stmt.executeQuery(sql);
		if (!rs.next()) {
			fail("Merge deleted master record with id = 2");
		}
		assertEquals("String not concatenated.", "CD", rs.getString(2));
		assertEquals("Date not ignored.", (new Date(1000*60*60*24*2)).toString(), rs.getDate(3).toString());
		assertEquals("Number not ignored.", 2, rs.getInt(4));
    }
    
    public void testMin() throws Exception {
    	populateTables();
    	
		cmr_string.setActionType(MergeActionType.MIN);
		cmr_date.setActionType(MergeActionType.MIN);
		cmr_number.setActionType(MergeActionType.MIN);
		
		mpor.call();

		Connection con = session.getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs;
		
		String sql = "SELECT * FROM " + getFullTableName() + " WHERE ID = 6";
		rs = stmt.executeQuery(sql);
		if (!rs.next()) {
			fail("Merge deleted master record with id = 6");
		}
		assertEquals("String not minimized.", "A", rs.getString(2));
		assertEquals("Date not minimized.", (new Date(0)).toString(), rs.getDate(3).toString());
		assertEquals("Number not minimized.", 0, rs.getInt(4));
		
		sql = "SELECT * FROM " + getFullTableName() + " WHERE ID = 2";
		rs = stmt.executeQuery(sql);
		if (!rs.next()) {
			fail("Merge deleted master record with id = 2");
		}
		assertEquals("String not minimized.", "C", rs.getString(2));
		assertEquals("Date not minimized.", (new Date(1000*60*60*24*2)).toString(), rs.getDate(3).toString());
		assertEquals("Number not minimized.", 2, rs.getInt(4));
    }
    
    public void testMax() throws Exception {
    	populateTables();
    	
		cmr_string.setActionType(MergeActionType.MAX);
		cmr_date.setActionType(MergeActionType.MAX);
		cmr_number.setActionType(MergeActionType.MAX);
		
		mpor.call();

		Connection con = session.getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs;
		
		String sql = "SELECT * FROM " + getFullTableName() + " WHERE ID = 6";
		rs = stmt.executeQuery(sql);
		if (!rs.next()) {
			fail("Merge deleted master record with id = 6");
		}
		assertEquals("String not maximized.", "F", rs.getString(2));
		assertEquals("Date not maximized.", (new Date(1000*60*60*24*5)).toString(), rs.getDate(3).toString());
		assertEquals("Number not maximized.", 5, rs.getInt(4));
		
		sql = "SELECT * FROM " + getFullTableName() + " WHERE ID = 2";
		rs = stmt.executeQuery(sql);
		if (!rs.next()) {
			fail("Merge deleted master record with id = 2");
		}
		assertEquals("String not maximized.", "D", rs.getString(2));
		assertEquals("Date not maximized.", (new Date(1000*60*60*24*3)).toString(), rs.getDate(3).toString());
		assertEquals("Number not maximized.", 3, rs.getInt(4));
    }
    
    public void testSum() throws Exception {
    	populateTables();
    	
		cmr_date.setActionType(MergeActionType.SUM);
		
		try {
			mpor.call();
			fail("Sum action should not be allowed on date datatypes.");
		} catch (IllegalStateException e) {
			// Correct exception thrown.
		}
		
    	cmr_string.setActionType(MergeActionType.SUM);
		cmr_date.setActionType(MergeActionType.IGNORE);
		cmr_number.setActionType(MergeActionType.IGNORE);
		
		try {
			mpor.call();
			fail("Sum action should not be allowed on string datatypes.");
		} catch (IllegalStateException e) {
			// Correct exception thrown.
		}
		
    	cmr_string.setActionType(MergeActionType.IGNORE);
		cmr_date.setActionType(MergeActionType.IGNORE);
		cmr_number.setActionType(MergeActionType.SUM);
		
		mpor.call();

		Connection con = session.getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs;
		
		String sql = "SELECT * FROM " + getFullTableName() + " WHERE ID = 6";
		rs = stmt.executeQuery(sql);
		if (!rs.next()) {
			fail("Merge deleted master record with id = 6");
		}
		assertNull("String not ignored.", rs.getString(2));
		assertNull("Date not ignored.", rs.getDate(3));
		assertEquals("Number not summed.", 10, rs.getInt(4));
		
		sql = "SELECT * FROM " + getFullTableName() + " WHERE ID = 2";
		rs = stmt.executeQuery(sql);
		if (!rs.next()) {
			fail("Merge deleted master record with id = 2");
		}
		assertEquals("String not ignored.", "C", rs.getString(2));
		assertEquals("Date not ignored.", (new Date(1000*60*60*24*2)).toString(), rs.getDate(3).toString());
		assertEquals("Number not summed.", 5, rs.getInt(4));
    }
    
	protected boolean execSQL(Connection conn, String sql) {
		Statement stmt = null;
		try {
    		stmt = conn.createStatement();
   			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			System.out.println("SQL ERROR:["+sql+"]\n"+e.getMessage());
			return false;
		} finally {
			try {
				if (stmt != null) stmt.close();
			} catch (SQLException ex) {
			}
	    }
		return true;
	}
	
	protected void tearDown() throws Exception {
		String sql = "DROP TABLE " + getFullTableName();
		execSQL(con, sql);
		sql = "DROP TABLE " + getFullTableName() + "_RESULT";
		execSQL(con, sql);
		con.close();
	}
	
	protected abstract String getFullTableName();
}
