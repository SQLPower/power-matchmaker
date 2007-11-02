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

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLStatement;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.ColumnMergeRules.MergeActionType;
import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;
import ca.sqlpower.matchmaker.TableMergeRules.ChildMergeActionType;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.matchmaker.dao.StubMatchMakerDAO;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SQL;

/**
 * This is a test for the merge processor. It focuses on testing the
 * functionality of the actions of the merge processor. The match result
 * table data is not extensive but should be sufficient to test the 
 * functionalities. Implementations of different database platforms 
 * should be written to implement this class.
 */
public abstract class AbstractMergeProcessorTest extends TestCase {
	
	private final Logger logger = Logger.getLogger("testLogger");
	
	static Project project;
	static MergeProcessor mpor;
	static TestingMatchMakerSession session;
	static Connection con;
	static SQLDatabase db;
	static SPDataSource ds;
	static SQLTable sourceTable;
	static SQLTable childTable;
	static SQLTable grandChildTable;
    
    static TableMergeRules tmr;
    static ColumnMergeRules cmr_id;
    static ColumnMergeRules cmr_string;
    static ColumnMergeRules cmr_date;
    static ColumnMergeRules cmr_number;
	
    static TableMergeRules ctmr;
    static ColumnMergeRules ccmr_parent_id;
    static ColumnMergeRules ccmr_id;
	static ColumnMergeRules ccmr_string;
	static ColumnMergeRules ccmr_date;
	static ColumnMergeRules ccmr_number;
	
	static TableMergeRules cctmr;
	static ColumnMergeRules cccmr_gparent_id;
	static ColumnMergeRules cccmr_parent_id;
	static ColumnMergeRules cccmr_id;
	static ColumnMergeRules cccmr_string;
	static ColumnMergeRules cccmr_date;
	static ColumnMergeRules cccmr_number;
	
	/**
	 * Subclasses need to implement this method to set the correct
	 * database of the desired platform. The project and session should
	 * also be set accordingly. See {@link MergeProcessorOracleTest#setUp()}
	 * for example.
	 */
	protected void setUp() throws Exception {
		
		String sql;
		project = new Project();

		ds = getDS();
		db = new SQLDatabase(ds);
		session = new TestingMatchMakerSession() {

			@Override
			public Connection getConnection() {
				try {
					return db.getConnection();
				} catch (ArchitectException e) {
					throw new ArchitectRuntimeException(e);
				}
			}

			@Override
			public <T extends MatchMakerObject> MatchMakerDAO<T> getDAO(Class<T> businessClass) {
				return new StubMatchMakerDAO<T>(businessClass);
			}
		};
		session.setDatabase(db);

		project.setSession(session);
		con = db.getConnection();
		session.setConnection(con);

		MungeProcess mp = new MungeProcess();
		mp.setName("test");
		project.addMungeProcess(mp);

		//This is different for Oracle and SQL Server
		createTables();

		// Creates the result Table
        DDLGenerator ddlg = null;
    	try {
    		ddlg = DDLUtils.createDDLGenerator(ds);
    	} catch (ClassNotFoundException e) {
    		fail("DDLUtils.createDDLGenerator(SPDataSource) threw a ClassNotFoundException");
    	}
    	assertNotNull("DDLGenerator error", ddlg);
		ddlg.setTargetSchema(ds.getPlSchema());

		if (Project.doesResultTableExist(session, project)) {
			ddlg.dropTable(project.getResultTable());
		}
		ddlg.addTable(project.createResultTable());
		ddlg.addIndex((SQLIndex) project.getResultTable().getIndicesFolder().getChild(0));
		
	    for (DDLStatement sqlStatement : ddlg.getDdlStatements()) {
	    	sql = sqlStatement.getSQLText();
	    	execSQL(con,sql);
	    }

	    tmr = new TableMergeRules();
	    cmr_id = new ColumnMergeRules();
	    cmr_string = new ColumnMergeRules();
	    cmr_date = new ColumnMergeRules();
	    cmr_number = new ColumnMergeRules();
		
	    ctmr = new TableMergeRules();
	    ccmr_parent_id = new ColumnMergeRules();
	    ccmr_id = new ColumnMergeRules();
		ccmr_string = new ColumnMergeRules();
		ccmr_date = new ColumnMergeRules();
		ccmr_number = new ColumnMergeRules();
		
		cctmr = new TableMergeRules();
		cccmr_gparent_id = new ColumnMergeRules();
		cccmr_parent_id = new ColumnMergeRules();
		cccmr_id = new ColumnMergeRules();
		cccmr_string = new ColumnMergeRules();
		cccmr_date = new ColumnMergeRules();
		cccmr_number = new ColumnMergeRules();
		
		//set the column and tabler merge rules
	    tmr.setTable(sourceTable);
	    tmr.addChild(cmr_id);
		tmr.addChild(cmr_string);
		tmr.addChild(cmr_date);
		tmr.addChild(cmr_number);
		
		cmr_id.setColumn(sourceTable.getColumnByName("ID"));
		cmr_string.setColumn(sourceTable.getColumnByName("COL_STRING"));   	
		cmr_date.setColumn(sourceTable.getColumnByName("COL_DATE"));
		cmr_number.setColumn(sourceTable.getColumnByName("COL_NUMBER"));
		
		ctmr.addChild(ccmr_parent_id);
		ctmr.addChild(ccmr_id);
		ctmr.addChild(ccmr_string);
		ctmr.addChild(ccmr_date);
		ctmr.addChild(ccmr_number);
		ctmr.setTable(childTable);
		ctmr.setParentTable(sourceTable);
		ctmr.setChildMergeAction(ChildMergeActionType.DELETE_ALL_DUP_CHILD);
		
		ccmr_parent_id.setInPrimaryKey(true);
		ccmr_parent_id.setColumn(childTable.getColumnByName("PARENT_ID"));
		ccmr_parent_id.setImportedKeyColumn(sourceTable.getColumnByName("ID"));
		ccmr_id.setInPrimaryKey(true);
		ccmr_id.setColumn(childTable.getColumnByName("ID"));
		ccmr_string.setColumn(sourceTable.getColumnByName("COL_STRING"));   	
		ccmr_date.setColumn(sourceTable.getColumnByName("COL_DATE"));
		ccmr_number.setColumn(sourceTable.getColumnByName("COL_NUMBER"));
		
		cctmr.addChild(cccmr_gparent_id);
		cctmr.addChild(cccmr_parent_id);
		cctmr.addChild(cccmr_id);
		cctmr.addChild(cccmr_string);
		cctmr.addChild(cccmr_date);
		cctmr.addChild(cccmr_number);
		cctmr.setTable(grandChildTable);
		cctmr.setParentTable(childTable);
		cctmr.setChildMergeAction(ChildMergeActionType.DELETE_ALL_DUP_CHILD);
		
		cccmr_gparent_id.setInPrimaryKey(true);
		cccmr_gparent_id.setColumn(grandChildTable.getColumnByName("GPARENT_ID"));
		cccmr_gparent_id.setImportedKeyColumn(childTable.getColumnByName("PARENT_ID"));
		cccmr_parent_id.setInPrimaryKey(true);
		cccmr_parent_id.setColumn(grandChildTable.getColumnByName("PARENT_ID"));
		cccmr_parent_id.setImportedKeyColumn(childTable.getColumnByName("ID"));
		cccmr_id.setInPrimaryKey(true);
		cccmr_id.setColumn(childTable.getColumnByName("ID"));
		cccmr_string.setColumn(sourceTable.getColumnByName("COL_STRING"));   	
		cccmr_date.setColumn(sourceTable.getColumnByName("COL_DATE"));
		cccmr_number.setColumn(sourceTable.getColumnByName("COL_NUMBER"));
		
		project.addTableMergeRule(tmr);
		project.addTableMergeRule(ctmr);
		project.addTableMergeRule(cctmr);
   	}

	/**
	 * Creates the tables we need
	 */
	protected abstract void createTables() throws Exception;
   
    /**
     * This is a protected method that sets defaults for the column 
     * merge rules and table merge rule. Subclasses need to implement this
     * method to setup the match source table and match result table on the
     * database. See {@link MergeProcessorOracleTest#populateTables()} for 
     * example. This should be called before each test case. 
     */
    private void populateTables() throws Exception {
    	String sql;

    	clearTables();
    	
		//Populates the source table
		String testString = "ABCDEF";
		for (int i = 0; i < 6; i++) {
			sql = "INSERT INTO " + getFullTableName() + " VALUES(" +
				i + ", " +
				SQL.quote(testString.charAt(i)) + ", " +
				SQL.escapeDateTime(con, new Date((long) i*1000*60*60*24)) + ", " +
				i + ")";
			execSQL(con,sql);
		}
        sql = "INSERT INTO " + getFullTableName() + " (ID) VALUES(6)";
        execSQL(con,sql);
        
		//6 is the master of 4, which is the master of 5, 
		//which is the master of 1, which is the master of 0. 
		//2 is the master of 3.
	    sql = "INSERT INTO " + getFullTableName() + "_RESULT " +
	    	"(DUP_CANDIDATE_10, DUP_CANDIDATE_20, MATCH_PERCENT, MATCH_STATUS, DUP1_MASTER_IND, GROUP_ID)" +
	    	"VALUES " + 
	    	"(6,4,10,'AUTO_MATCH','Y', 'test')";
	    execSQL(con,sql);
	    sql = "INSERT INTO " + getFullTableName() + "_RESULT " +
	    	"(DUP_CANDIDATE_10, DUP_CANDIDATE_20, MATCH_PERCENT, MATCH_STATUS, DUP1_MASTER_IND, GROUP_ID)" +
	    	"VALUES " + 
	    "(0,1,10,'AUTO_MATCH','N', 'test')";
	    execSQL(con,sql);
	    sql = "INSERT INTO " + getFullTableName() + "_RESULT " +
	    	"(DUP_CANDIDATE_10, DUP_CANDIDATE_20, MATCH_PERCENT, MATCH_STATUS, DUP1_MASTER_IND, GROUP_ID)" +
	    	"VALUES " + 
	    	"(1,5,10,'AUTO_MATCH','N', 'test')";
	    execSQL(con,sql);
	    sql = "INSERT INTO " + getFullTableName() + "_RESULT " +
	    	"(DUP_CANDIDATE_10, DUP_CANDIDATE_20, MATCH_PERCENT, MATCH_STATUS, DUP1_MASTER_IND, GROUP_ID)" +
	    	"VALUES " + 
	    	"(2,3,10,'MATCH','Y', 'test')";
	    execSQL(con,sql);
	    sql = "INSERT INTO " + getFullTableName() + "_RESULT " +
	    	"(DUP_CANDIDATE_10, DUP_CANDIDATE_20, MATCH_PERCENT, MATCH_STATUS, DUP1_MASTER_IND, GROUP_ID)" +
	    	"VALUES " + 
	    	"(6,3,10,'UNMATCH','', 'test')";
	    execSQL(con,sql);
	    sql = "INSERT INTO " + getFullTableName() + "_RESULT " +
	    	"(DUP_CANDIDATE_10, DUP_CANDIDATE_20, MATCH_PERCENT, MATCH_STATUS, DUP1_MASTER_IND, GROUP_ID)" +
	    	"VALUES " + 
	    	"(5,4,10,'MATCH','N', 'test')";
	    execSQL(con,sql);
	    
	    mpor = new MergeProcessor(project, session, logger);
	    
	    // sets the default action type
	    cmr_id.setActionType(MergeActionType.USE_MASTER_VALUE);
	    cmr_string.setActionType(MergeActionType.USE_MASTER_VALUE);
	    cmr_date.setActionType(MergeActionType.USE_MASTER_VALUE);
	    cmr_number.setActionType(MergeActionType.USE_MASTER_VALUE);
	}
    
    private void populateChildTable() throws Exception {
    	String sql;
		String testString = "ABCDEF";
				
		//populates the child table
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < i; j++) {
				sql = "INSERT INTO " + getFullTableName() + "_CHILD VALUES(" +
					i + ", " +
					j + ", " +
					SQL.quote(testString.charAt(j)) + ", " +
					SQL.escapeDateTime(con, new Date((long) j*1000*60*60*24)) + ", " +
					(i+j) + ")";
				execSQL(con,sql);
			}
		}
		
		//sets the default action type
		ctmr.setChildMergeAction(ChildMergeActionType.DELETE_ALL_DUP_CHILD);
	}
    
    private void populateChildTableForUpdate() throws Exception {
    	String sql;
		String testString = "ABCDEF";
				
		//populates the child table
		for (int i = 0; i < 6; i++) {
			sql = "INSERT INTO " + getFullTableName() + "_CHILD VALUES(" +
				i + ", " +
				i + ", " +
				SQL.quote(testString.charAt(i)) + ", " +
				SQL.escapeDateTime(con, new Date((long) i*1000*60*60*24)) + ", " +
				(i+i) + ")";
			execSQL(con,sql);
		}
		
		//sets the default action type
		ctmr.setChildMergeAction(ChildMergeActionType.DELETE_ALL_DUP_CHILD);

	}
    
    private void populateGrandChildTableForUpdate() throws Exception {
    	String sql;
		String testString = "ABCDEF";
				
		//populates the child table
		for (int i = 0; i < 6; i++) {
			for (int k = 0; k < i; k++) {
				sql = "INSERT INTO " + getFullTableName() + "_GCHILD VALUES(" +
				i + ", " +
				i + ", " +
				k + ", " +
				SQL.quote(testString.charAt(k)) + ", " +
				SQL.escapeDateTime(con, new Date((long) k*1000*60*60*24)) + ", " +
				(i+i+k) + ")";
				execSQL(con,sql);
			}
		}
		
		//sets the default action type
		cctmr.setChildMergeAction(ChildMergeActionType.DELETE_ALL_DUP_CHILD);
	}
    
    private void populateGrandChildTable() throws Exception {
    	String sql;
		String testString = "ABCDEF";
		
		//populates the child table
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < i; j++) {
				for (int k = 0; k < j; k++) {
					sql = "INSERT INTO " + getFullTableName() + "_GCHILD VALUES(" +
						i + ", " +
						j + ", " +
						k + ", " +
						SQL.quote(testString.charAt(k)) + ", " +
						SQL.escapeDateTime(con, new Date((long) k*1000*60*60*24)) + ", " +
						(i+j+k) + ")";
					execSQL(con,sql);
				}
			}
		}
		
		//sets the default action type
		cctmr.setChildMergeAction(ChildMergeActionType.DELETE_ALL_DUP_CHILD);
	}
    
    public void clearTables() throws Exception{
    	String sql;
    	
    	//delete everything from grand child table
    	sql = "DELETE FROM " + getFullTableName() + "_GCHILD";
    	execSQL(con, sql);
    	
    	//delete everything from child table
		sql = "DELETE FROM " + getFullTableName() + "_CHILD";
		execSQL(con, sql);
		
		//delete everything from source table
		sql = "DELETE FROM " + getFullTableName();
		execSQL(con, sql);
		
		// delete everything from result table
		sql = "DELETE FROM " + getFullTableName() + "_RESULT";
		execSQL(con, sql);
    }
    
    /**
     * This tests on a unmatch, it should do nothing.
     */
    public void testUnmatch() throws Exception {
    	populateTables();
    	
		mpor.call();
		
		Statement stmt = con.createStatement();
		ResultSet rs;
    	
    	String sql = "SELECT * FROM " + getFullTableName() + " WHERE ID = 6";
		rs = stmt.executeQuery(sql);
		if (!rs.next()) {
			fail("Merge deleted master record with id = 6");
		}
		assertEquals("Record should not be changed on unmatch: col_string was changed.", null, rs.getString(2));
		assertEquals("Record should not be changed on unmatch: col_date was changed.", null, rs.getDate(3));
		assertEquals("Record should not be changed on unmatch: col_number was changed.", 0, rs.getInt(4));    
    }
       
    /**
     * This tests if the duplicates are deleted when the option is set.
     */
    public void testDeleteDup() throws Exception {
    	populateTables();
		
    	
		mpor.call();

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
		
    	MatchPool matchPool = new MatchPool(project);
    	matchPool.findAll(new ArrayList<SQLColumn>());
    	for (PotentialMatchRecord pm : matchPool.getPotentialMatches()) {
    		assertFalse("MatchType not set as MERGED for " + pm, pm.isMatch());
    		assertFalse("Match result with status 'UNMATCH' for duplicate not deleted after merge for: " + pm, pm.getMatchStatus() == MatchType.UNMATCH);
    	}
    }
    
    public void testAugment() throws Exception {
    	populateTables();
    	
		cmr_string.setActionType(MergeActionType.AUGMENT);
		cmr_date.setActionType(MergeActionType.AUGMENT);
		cmr_number.setActionType(MergeActionType.AUGMENT);
		
		mpor.call();

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
		
    	cmr_string.setActionType(MergeActionType.USE_MASTER_VALUE);
		cmr_date.setActionType(MergeActionType.USE_MASTER_VALUE);
		cmr_number.setActionType(MergeActionType.CONCAT);
		
		try {
			mpor.call();
			fail("Concat action should not be allowed on number datatypes.");
		} catch (IllegalStateException e) {
			// Correct exception thrown.
		}
		
    	cmr_string.setActionType(MergeActionType.CONCAT);
		cmr_date.setActionType(MergeActionType.USE_MASTER_VALUE);
		cmr_number.setActionType(MergeActionType.USE_MASTER_VALUE);
		
		mpor.call();

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
		cmr_date.setActionType(MergeActionType.USE_MASTER_VALUE);
		cmr_number.setActionType(MergeActionType.USE_MASTER_VALUE);
		
		try {
			mpor.call();
			fail("Sum action should not be allowed on string datatypes.");
		} catch (IllegalStateException e) {
			// Correct exception thrown.
		}
		
    	cmr_string.setActionType(MergeActionType.USE_MASTER_VALUE);
		cmr_date.setActionType(MergeActionType.USE_MASTER_VALUE);
		cmr_number.setActionType(MergeActionType.SUM);
		
		mpor.call();

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
    
    public void testDeleteDupChild() throws Exception{
    	
    	populateTables();
    	populateChildTable();
    	populateGrandChildTable();
	
		mpor.call();
		
		Statement stmt = con.createStatement();
		ResultSet rs;
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ");
		sql.append(getFullTableName() + "_CHILD");
		sql.append("\n WHERE PARENT_ID = 1 OR PARENT_ID = 0 ");
		sql.append("OR PARENT_ID = 4 OR PARENT_ID = 3 OR PARENT_ID = 5");
		
		rs = stmt.executeQuery(sql.toString());
		assertFalse("Duplicate child records not deleted.", rs.next());

		sql = new StringBuilder();
		sql.append("SELECT * FROM ");
		sql.append(getFullTableName() + "_CHILD");
		sql.append("\n WHERE PARENT_ID = 2 OR PARENT_ID = 6");
		
		rs = stmt.executeQuery(sql.toString());
		for (int i = 0; i < 2; i++) {
			assertTrue("Non-duplicate child records deleted.", rs.next());
		}
		assertFalse("Extra non-duplicate child records found.", rs.next());
		
		sql = new StringBuilder();
		sql.append("SELECT * FROM ");
		sql.append(getFullTableName() + "_GCHILD");
		sql.append("\n WHERE GPARENT_ID = 1 OR GPARENT_ID = 0 ");
		sql.append("OR GPARENT_ID = 4 OR GPARENT_ID = 3 OR GPARENT_ID = 5");
		
		rs = stmt.executeQuery(sql.toString());
		assertFalse("Duplicate grandchild records not deleted.", rs.next());
		
		
    }
    
    public void testUpdateFailOnConflict() throws Exception{

    	populateTables();
    	populateChildTable();
    	
    	ctmr.setChildMergeAction(ChildMergeActionType.UPDATE_FAIL_ON_CONFLICT);
		try {
			mpor.call();
			fail("Merge processor should not have worked!");
		} catch (IllegalStateException e) {
		}
		
		populateTables();
    	populateChildTableForUpdate();
    	populateGrandChildTableForUpdate();
    	
    	ctmr.setChildMergeAction(ChildMergeActionType.UPDATE_FAIL_ON_CONFLICT);
    	cctmr.setChildMergeAction(ChildMergeActionType.UPDATE_FAIL_ON_CONFLICT);
    	mpor.call();
    	
		Statement stmt = con.createStatement();
		ResultSet rs;
		
    	StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ");
		sql.append(getFullTableName() + "_CHILD");
		sql.append("\n WHERE PARENT_ID = 1 OR PARENT_ID = 0 ");
		sql.append("OR PARENT_ID = 4 OR PARENT_ID = 3 OR PARENT_ID = 5");
		
		rs = stmt.executeQuery(sql.toString());
		assertFalse("Duplicate child records not deleted.", rs.next());

		sql = new StringBuilder();
		sql.append("SELECT * FROM ");
		sql.append(getFullTableName() + "_CHILD");
		sql.append("\n WHERE PARENT_ID = 2");
		
		rs = stmt.executeQuery(sql.toString());
		for (int i = 0; i < 2; i++) {
			assertTrue("Non-duplicate child records deleted.", rs.next());
		}
		assertFalse("Extra non-duplicate child records found.", rs.next());
		
		sql = new StringBuilder();
		sql.append("SELECT * FROM ");
		sql.append(getFullTableName() + "_CHILD");
		sql.append("\n WHERE PARENT_ID = 6");
		
		rs = stmt.executeQuery(sql.toString());
		for (int i = 0; i < 4; i++) {
			assertTrue("Non-duplicate child records deleted.", rs.next());
		}
		assertFalse("Extra non-duplicate child records found.", rs.next());
		
		sql = new StringBuilder();
		sql.append("SELECT * FROM ");
		sql.append(getFullTableName() + "_GCHILD");
		sql.append("\n WHERE GPARENT_ID = 1 OR GPARENT_ID = 0 ");
		sql.append("OR GPARENT_ID = 4 OR GPARENT_ID = 3 OR GPARENT_ID = 5");
		
		rs = stmt.executeQuery(sql.toString());
		assertFalse("Duplicate grandchild records not merged.", rs.next());
    }
    
    public void testUpdateDeleteOnConflict() throws Exception{

    	populateTables();
    	populateChildTable();
    	populateGrandChildTable();
    	
    	ctmr.setChildMergeAction(ChildMergeActionType.UPDATE_DELETE_ON_CONFLICT);
    	cctmr.setChildMergeAction(ChildMergeActionType.UPDATE_DELETE_ON_CONFLICT);
	
		mpor.call();
    	
		Statement stmt = con.createStatement();
		ResultSet rs;
		
    	StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ");
		sql.append(getFullTableName() + "_CHILD");
		sql.append("\n WHERE PARENT_ID = 1 OR PARENT_ID = 0 ");
		sql.append("OR PARENT_ID = 4 OR PARENT_ID = 3 OR PARENT_ID = 5");
		
		rs = stmt.executeQuery(sql.toString());
		assertFalse("Duplicate child records not deleted.", rs.next());

		sql = new StringBuilder();
		sql.append("SELECT * FROM ");
		sql.append(getFullTableName() + "_CHILD");
		sql.append("\n WHERE PARENT_ID = 2");
		
		rs = stmt.executeQuery(sql.toString());
		for (int i = 0; i < 3; i++) {
			assertTrue("Non-duplicate child records deleted.", rs.next());
		}
		assertFalse("Extra non-duplicate child records found.", rs.next());
		
		sql = new StringBuilder();
		sql.append("SELECT * FROM ");
		sql.append(getFullTableName() + "_CHILD");
		sql.append("\n WHERE PARENT_ID = 6");
		
		rs = stmt.executeQuery(sql.toString());
		for (int i = 0; i < 5; i++) {
			assertTrue("Non-duplicate child records deleted.", rs.next());
		}
		assertFalse("Extra non-duplicate child records found.", rs.next());
		
		sql = new StringBuilder();
		sql.append("SELECT * FROM ");
		sql.append(getFullTableName() + "_GCHILD");
		sql.append("\n WHERE GPARENT_ID = 1 OR GPARENT_ID = 0 ");
		sql.append("OR GPARENT_ID = 4 OR GPARENT_ID = 3 OR GPARENT_ID = 5");
		
		rs = stmt.executeQuery(sql.toString());
		assertFalse("Duplicate grandchild records not merged.", rs.next());
    }
    
    public void testUpdateUsingSQL() throws Exception{

    	populateTables();
    	populateChildTable();
    	populateGrandChildTable();
    	
    	ctmr.setChildMergeAction(ChildMergeActionType.UPDATE_USING_SQL);
    	ccmr_id.setUpdateStatement("(SELECT MAX(ID)+1 FROM " + getFullTableName() + "_CHILD)");
    	cctmr.setChildMergeAction(ChildMergeActionType.UPDATE_USING_SQL);
    	cccmr_id.setUpdateStatement("(SELECT MAX(ID)+1 FROM " + getFullTableName() + "_GCHILD)");
    	
		mpor.call();
    	
		Statement stmt = con.createStatement();
		ResultSet rs;
		
    	StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ");
		sql.append(getFullTableName() + "_CHILD");
		sql.append("\n WHERE PARENT_ID = 1 OR PARENT_ID = 0 ");
		sql.append("OR PARENT_ID = 4 OR PARENT_ID = 3 OR PARENT_ID = 5");
		
		rs = stmt.executeQuery(sql.toString());
		assertFalse("Duplicate child records not deleted.", rs.next());

		sql = new StringBuilder();
		sql.append("SELECT * FROM ");
		sql.append(getFullTableName() + "_CHILD");
		sql.append("\n WHERE PARENT_ID = 2");
		
		rs = stmt.executeQuery(sql.toString());
		for (int i = 0; i < 2+3; i++) {
			assertTrue("Non-duplicate child records deleted.", rs.next());
		}
		assertFalse("Extra non-duplicate child records found.", rs.next());
		
		sql = new StringBuilder();
		sql.append("SELECT * FROM ");
		sql.append(getFullTableName() + "_CHILD");
		sql.append("\n WHERE PARENT_ID = 6");
		
		rs = stmt.executeQuery(sql.toString());
		for (int i = 0; i < 1+5+4; i++) {
			assertTrue("Non-duplicate child records deleted.", rs.next());
		}
		assertFalse("Extra non-duplicate child records found.", rs.next());
		
		sql = new StringBuilder();
		sql.append("SELECT * FROM ");
		sql.append(getFullTableName() + "_GCHILD");
		sql.append("\n WHERE GPARENT_ID = 1 OR GPARENT_ID = 0 ");
		sql.append("OR GPARENT_ID = 4 OR GPARENT_ID = 3 OR GPARENT_ID = 5");
		
		rs = stmt.executeQuery(sql.toString());
		assertFalse("Duplicate grandchild records not merged.", rs.next());
    }

    public void testMergeOnConflict() throws Exception{

    	populateTables();
    	populateChildTable();
    	populateGrandChildTable();
    	
    	ctmr.setChildMergeAction(ChildMergeActionType.MERGE_ON_CONFLICT);
    	ccmr_string.setActionType(MergeActionType.CONCAT);
    	ccmr_date.setActionType(MergeActionType.MAX);
    	ccmr_number.setActionType(MergeActionType.SUM);
    	
    	cctmr.setChildMergeAction(ChildMergeActionType.MERGE_ON_CONFLICT);
    	cccmr_string.setActionType(MergeActionType.CONCAT);
    	cccmr_date.setActionType(MergeActionType.MAX);
    	cccmr_number.setActionType(MergeActionType.SUM);
    	
		mpor.call();
    	
		Statement stmt = con.createStatement();
		ResultSet rs;
		
    	StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ");
		sql.append(getFullTableName() + "_CHILD");
		sql.append("\n WHERE PARENT_ID = 1 OR PARENT_ID = 0 ");
		sql.append("OR PARENT_ID = 4 OR PARENT_ID = 3 OR PARENT_ID = 5");
		
		rs = stmt.executeQuery(sql.toString());
		assertFalse("Duplicate child records not deleted.", rs.next());

		sql = new StringBuilder();
		sql.append("SELECT * FROM ");
		sql.append(getFullTableName() + "_CHILD");
		sql.append("\n WHERE PARENT_ID = 2");
		
		rs = stmt.executeQuery(sql.toString());
		for (int i = 0; i < 3; i++) {
			assertTrue("Non-duplicate child records deleted.", rs.next());
		}
		assertFalse("Extra non-duplicate child records found.", rs.next());
		
		sql = new StringBuilder();
		sql.append("SELECT * FROM ");
		sql.append(getFullTableName() + "_CHILD");
		sql.append("\n WHERE PARENT_ID = 6");
		
		rs = stmt.executeQuery(sql.toString());
		for (int i = 0; i < 5; i++) {
			assertTrue("Non-duplicate child records deleted.", rs.next());
		}
		assertFalse("Extra non-duplicate child records found.", rs.next());
		
		sql = new StringBuilder();
		sql.append("SELECT * FROM ");
		sql.append(getFullTableName() + "_GCHILD");
		sql.append("\n WHERE GPARENT_ID = 1 OR GPARENT_ID = 0 ");
		sql.append("OR GPARENT_ID = 4 OR GPARENT_ID = 3 OR GPARENT_ID = 5");
		
		rs = stmt.executeQuery(sql.toString());
		assertFalse("Duplicate grandchild records not merged.", rs.next());
    }
    
	protected boolean execSQL(Connection conn, String sql) throws SQLException {
		Statement stmt = null;
		stmt = conn.createStatement();
		stmt.executeUpdate(sql);
		if (stmt != null) stmt.close();

		return true;
	}
	
	protected abstract String getFullTableName();
	
	protected abstract SPDataSource getDS();
}
