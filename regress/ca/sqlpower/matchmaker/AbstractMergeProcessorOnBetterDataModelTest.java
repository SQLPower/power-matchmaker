/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLStatement;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.ColumnMergeRules.MergeActionType;
import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;
import ca.sqlpower.matchmaker.TableMergeRules.ChildMergeActionType;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.matchmaker.dao.StubMatchMakerDAO;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.SQL;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.sqlobject.SQLTable;

/**
 * Like AbstractMergeProcessorTest, this is a test for the merge processor.
 * The main difference is that it conducts the tests on a 'better' data model
 * which uses surrogate keys instead of natural identifiers as the primary keys,
 * and all the foreign keys are not part of the child tables' primary keys. 
 * 
 * It focuses on testing the
 * functionality of the actions of the merge processor. The match result
 * table data is not extensive but should be sufficient to test the 
 * functionalities. Implementations of different database platforms 
 * should be written to implement this class.
 */
public abstract class AbstractMergeProcessorOnBetterDataModelTest extends TestCase {
	
	private final Logger logger = Logger.getLogger("testLogger");
	
	static Project project;
	static MergeProcessor mpor;
	static TestingMatchMakerSession session;
	static Connection rCon;
	static Connection sCon;
	static Connection con;
	static SQLDatabase db;
	static JDBCDataSource ds;
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
				} catch (SQLObjectException e) {
					throw new SQLObjectRuntimeException(e);
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
		
		rCon = project.createResultTableConnection();
		sCon = project.createSourceTableConnection();
		
		// Creates the result Table
        DDLGenerator ddlg = null;
    	try {
    		ddlg = DDLUtils.createDDLGenerator(project.getResultTable().getParentDatabase().getDataSource());
    	} catch (ClassNotFoundException e) {
    		fail("DDLUtils.createDDLGenerator(SPDataSource) threw a ClassNotFoundException");
    	}
    	assertNotNull("DDLGenerator error", ddlg);
		ddlg.setTargetSchema(ds.getPlSchema());

		if (project.doesResultTableExist()) {
			ddlg.dropTable(project.getResultTable());
		}
		ddlg.addTable(project.createResultTable());
		ddlg.addIndex((SQLIndex) project.getResultTable().getIndicesFolder().getChild(0));
		
	    for (DDLStatement sqlStatement : ddlg.getDdlStatements()) {
	    	sql = sqlStatement.getSQLText();
	    	execSQL(rCon,sql);
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
		cccmr_id = new ColumnMergeRules();
		cccmr_string = new ColumnMergeRules();
		cccmr_date = new ColumnMergeRules();
		cccmr_number = new ColumnMergeRules();
		
		//set the column and tabler merge rules
	    tmr.setTable(sourceTable);
	    tmr.setTableIndex(sourceTable.getPrimaryKeyIndex());
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
		ctmr.setTableIndex(childTable.getPrimaryKeyIndex());
		ctmr.setParentMergeRule(tmr);
		ctmr.setChildMergeAction(ChildMergeActionType.DELETE_ALL_DUP_CHILD);
		
		ccmr_parent_id.setColumn(childTable.getColumnByName("PARENT_ID"));
		ccmr_parent_id.setImportedKeyColumn(childTable.getColumnByName("ID"));
		ccmr_id.setInPrimaryKey(true);
		ccmr_id.setColumn(childTable.getColumnByName("ID"));
		ccmr_string.setColumn(childTable.getColumnByName("COL_STRING"));   	
		ccmr_date.setColumn(childTable.getColumnByName("COL_DATE"));
		ccmr_number.setColumn(childTable.getColumnByName("COL_NUMBER"));
		
		cctmr.addChild(cccmr_gparent_id);
		cctmr.addChild(cccmr_id);
		cctmr.addChild(cccmr_string);
		cctmr.addChild(cccmr_date);
		cctmr.addChild(cccmr_number);
		cctmr.setTable(grandChildTable);
		cctmr.setTableIndex(grandChildTable.getPrimaryKeyIndex());
		cctmr.setParentMergeRule(ctmr);
		cctmr.setChildMergeAction(ChildMergeActionType.DELETE_ALL_DUP_CHILD);
		
		cccmr_gparent_id.setColumn(grandChildTable.getColumnByName("GPARENT_ID"));
		cccmr_gparent_id.setImportedKeyColumn(childTable.getColumnByName("PARENT_ID"));
		cccmr_id.setInPrimaryKey(true);
		cccmr_id.setColumn(childTable.getColumnByName("ID"));
		cccmr_string.setColumn(childTable.getColumnByName("COL_STRING"));   	
		cccmr_date.setColumn(childTable.getColumnByName("COL_DATE"));
		cccmr_number.setColumn(childTable.getColumnByName("COL_NUMBER"));
		
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
				SQL.escapeDateTime(sCon, new Date((long) i*1000*60*60*24)) + ", " +
				i + ")";
			execSQL(sCon,sql);
		}
        sql = "INSERT INTO " + getFullTableName() + " (ID) VALUES(6)";
        execSQL(sCon,sql);
        
		//6 is the master of 4, which is the master of 5, 
		//which is the master of 1, which is the master of 0. 
		//2 is the master of 3.
	    sql = "INSERT INTO " + getFullTableName() + "_RESULT " +
	    	"(DUP_CANDIDATE_10, DUP_CANDIDATE_20, MATCH_PERCENT, MATCH_STATUS, DUP1_MASTER_IND, GROUP_ID)" +
	    	"VALUES " + 
	    	"(6,4,10,'AUTO_MATCH','Y', 'test')";
	    execSQL(sCon,sql);
	    sql = "INSERT INTO " + getFullTableName() + "_RESULT " +
	    	"(DUP_CANDIDATE_10, DUP_CANDIDATE_20, MATCH_PERCENT, MATCH_STATUS, DUP1_MASTER_IND, GROUP_ID)" +
	    	"VALUES " + 
	    "(0,1,10,'AUTO_MATCH','N', 'test')";
	    execSQL(sCon,sql);
	    sql = "INSERT INTO " + getFullTableName() + "_RESULT " +
	    	"(DUP_CANDIDATE_10, DUP_CANDIDATE_20, MATCH_PERCENT, MATCH_STATUS, DUP1_MASTER_IND, GROUP_ID)" +
	    	"VALUES " + 
	    	"(1,5,10,'AUTO_MATCH','N', 'test')";
	    execSQL(sCon,sql);
	    sql = "INSERT INTO " + getFullTableName() + "_RESULT " +
	    	"(DUP_CANDIDATE_10, DUP_CANDIDATE_20, MATCH_PERCENT, MATCH_STATUS, DUP1_MASTER_IND, GROUP_ID)" +
	    	"VALUES " + 
	    	"(2,3,10,'MATCH','Y', 'test')";
	    execSQL(sCon,sql);
	    sql = "INSERT INTO " + getFullTableName() + "_RESULT " +
	    	"(DUP_CANDIDATE_10, DUP_CANDIDATE_20, MATCH_PERCENT, MATCH_STATUS, DUP1_MASTER_IND, GROUP_ID)" +
	    	"VALUES " + 
	    	"(6,3,10,'UNMATCH','', 'test')";
	    execSQL(sCon,sql);
	    sql = "INSERT INTO " + getFullTableName() + "_RESULT " +
	    	"(DUP_CANDIDATE_10, DUP_CANDIDATE_20, MATCH_PERCENT, MATCH_STATUS, DUP1_MASTER_IND, GROUP_ID)" +
	    	"VALUES " + 
	    	"(5,4,10,'MATCH','N', 'test')";
	    execSQL(sCon,sql);
	    
	    logger.setLevel(Level.INFO);
	    
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
			sql = "INSERT INTO " + getFullTableName() + "_CHILD VALUES(" +
			i + ", " +
			i + ", " +
			SQL.quote(testString.charAt(i)) + ", " +
			SQL.escapeDateTime(sCon, new Date((long) i*1000*60*60*24)) + ", " +
			i + ")";
			execSQL(sCon,sql);
		}
		
		//sets the default action type
		ctmr.setChildMergeAction(ChildMergeActionType.DELETE_ALL_DUP_CHILD);
	}
    
    private void populateGrandChildTable() throws Exception {
    	String sql;
		String testString = "ABCDEF";
		
		//populates the child table
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 6; j++) {
				sql = "INSERT INTO " + getFullTableName() + "_GCHILD VALUES(" +
				(i*6+j) + ", " +
				j + ", " +
				SQL.quote(testString.charAt(j)) + ", " +
				SQL.escapeDateTime(sCon, new Date((long) j*1000*60*60*24)) + ", " +
				j + ")";
				execSQL(sCon,sql);
			}
		}
		
		//sets the default action type
		cctmr.setChildMergeAction(ChildMergeActionType.DELETE_ALL_DUP_CHILD);
	}
    
    public void clearTables() throws Exception{
    	String sql;
    	
    	//delete everything from grand child table
    	sql = "DELETE FROM " + getFullTableName() + "_GCHILD";
    	execSQL(sCon, sql);
    	
    	//delete everything from child table
		sql = "DELETE FROM " + getFullTableName() + "_CHILD";
		execSQL(sCon, sql);
		
		//delete everything from source table
		sql = "DELETE FROM " + getFullTableName();
		execSQL(sCon, sql);
		
		// delete everything from result table
		sql = "DELETE FROM " + getFullTableName() + "_RESULT";
		execSQL(sCon, sql);
    }
    
    /**
     * This tests on a unmatch, it should do nothing.
     */
    public void testUnmatch() throws Exception {
    	populateTables();
    	
		runProcessor();
		
		Statement stmt = sCon.createStatement();
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
		
		runProcessor();

		Statement stmt = sCon.createStatement();
		ResultSet rs;
		
		String sql = "SELECT * FROM " + getFullTableName() + " WHERE ID = 1 OR ID = 3";
		rs = stmt.executeQuery(sql);
		if (rs.next()) {
			fail("Duplicate records not deleted.");
		}
		
		sql = "SELECT * FROM " + getFullTableName() + "_RESULT WHERE MATCH_STATUS != 'MERGED'";
		rs = stmt.executeQuery(sql);
		if (rs.next()) {
			fail("Result table not cleared.");
		}
    }
    
    /**
     * This ensures that the MatchType is set as MERGED for each record in the match pool. 
     */
    public void testMatchTypeMerged() throws Exception {
    	
    	populateTables();
    	
    	runProcessor();
		
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
		
		runProcessor();

		Statement stmt = sCon.createStatement();
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
			runProcessor();
			fail("Concat action should not be allowed on date datatypes.");
		} catch (IllegalStateException e) {
			// Correct exception thrown.
		}
		
    	cmr_string.setActionType(MergeActionType.USE_MASTER_VALUE);
		cmr_date.setActionType(MergeActionType.USE_MASTER_VALUE);
		cmr_number.setActionType(MergeActionType.CONCAT);
		
		try {
			runProcessor();
			fail("Concat action should not be allowed on number datatypes.");
		} catch (IllegalStateException e) {
			// Correct exception thrown.
		}
		
    	cmr_string.setActionType(MergeActionType.CONCAT);
		cmr_date.setActionType(MergeActionType.USE_MASTER_VALUE);
		cmr_number.setActionType(MergeActionType.USE_MASTER_VALUE);
		
		runProcessor();

		Statement stmt = sCon.createStatement();
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
		
		runProcessor();

		Statement stmt = sCon.createStatement();
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
		
		runProcessor();

		Statement stmt = sCon.createStatement();
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
			runProcessor();
			fail("Sum action should not be allowed on date datatypes.");
		} catch (IllegalStateException e) {
			// Correct exception thrown.
		}
		
    	cmr_string.setActionType(MergeActionType.SUM);
		cmr_date.setActionType(MergeActionType.USE_MASTER_VALUE);
		cmr_number.setActionType(MergeActionType.USE_MASTER_VALUE);
		
		try {
			runProcessor();
			fail("Sum action should not be allowed on string datatypes.");
		} catch (IllegalStateException e) {
			// Correct exception thrown.
		}
		
    	cmr_string.setActionType(MergeActionType.USE_MASTER_VALUE);
		cmr_date.setActionType(MergeActionType.USE_MASTER_VALUE);
		cmr_number.setActionType(MergeActionType.SUM);
		
		runProcessor();

		Statement stmt = sCon.createStatement();
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
    	
		runProcessor();
		
		Statement stmt = sCon.createStatement();
		ResultSet rs;
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) FROM ");
		sql.append(getFullTableName() + "_CHILD");
		sql.append("\n WHERE PARENT_ID = 1 OR PARENT_ID = 0 ");
		sql.append("OR PARENT_ID = 4 OR PARENT_ID = 3 OR PARENT_ID = 5");
		
		rs = stmt.executeQuery(sql.toString());
		rs.next();
		assertEquals("Duplicate child records not deleted.", 0, rs.getInt(1));

		sql = new StringBuilder();
		sql.append("SELECT COUNT(*) FROM ");
		sql.append(getFullTableName() + "_CHILD");
		sql.append("\n WHERE PARENT_ID = 2");
		
		rs = stmt.executeQuery(sql.toString());
		rs.next();
		assertEquals("Non-duplicate child records missing.", 1, rs.getInt(1));
		
		sql = new StringBuilder();
		sql.append("SELECT COUNT(*) FROM ");
		sql.append(getFullTableName() + "_GCHILD");
		sql.append("\n WHERE GPARENT_ID = 1 OR GPARENT_ID = 0 ");
		sql.append("OR GPARENT_ID = 4 OR GPARENT_ID = 3 OR GPARENT_ID = 5");
		
		rs = stmt.executeQuery(sql.toString());
		rs.next();
		assertEquals("Duplicate grandchild records not deleted.", 0, rs.getInt(1));
		
		sql = new StringBuilder();
		sql.append("SELECT COUNT(*) FROM ");
		sql.append(getFullTableName() + "_GCHILD");
		sql.append("\n WHERE GPARENT_ID = 2");
		
		rs = stmt.executeQuery(sql.toString());
		rs.next();
		assertEquals("Non-duplicate grandchild records missing.", 6, rs.getInt(1));
		
    }
    
    public void testUpdateFailOnConflict() throws Exception{
    	populateTables();
    	populateChildTable();
    	populateGrandChildTable();
    	
    	ctmr.setChildMergeAction(ChildMergeActionType.UPDATE_FAIL_ON_CONFLICT);
    	cctmr.setChildMergeAction(ChildMergeActionType.UPDATE_FAIL_ON_CONFLICT);
    	
    	try {
    		runProcessor();
    		// The merge engine does not support merging in a data model where the key the child table imports
    		// from the parent is NOT part of its primary key, and it exports that same imported key into
    		// a grandchild table. If the platform does not support deferrable constraints, then it is
    		// impossible to update the child table records without violating foreign key constraints in 
    		// the grandchild table, so we would have to create a new child record with a new PK, which we
    		// don't support and currently don't have time to implement properly yet. When we finally do,
    		// this test case should change.
    		fail("Merging should have failed because this type of data model structure is not supported.");
    	} catch (UnsupportedOperationException e) {
    		// correct behavior
    	}
    }
    
    /**
	 * 
	 * This test checks for MatchMaker Bug #1544 As of r1855 this test is
	 * failing because the business model does not recognize that the
	 * constraints have been dropped and mistakes it for the data model
	 * structure that we do not support (see {@link #testUpdateFailOnConflict()});
	 * 
	 */ 
    public void testUpdateFailOnConflictOnChildTableWithNoPK() throws Exception {
    	Statement stmt = sCon.createStatement();
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("ALTER TABLE ");
    	sql.append(getFullTableName()).append("_GCHILD");
    	sql.append("\n DROP CONSTRAINT BETTER_CHILD_FK");
    	stmt.executeUpdate(sql.toString());
		
		sql = new StringBuilder();
		sql.append("ALTER TABLE ");
		sql.append(getFullTableName()).append("_CHILD");
		sql.append("\n DROP CONSTRAINT BETTER_CHILD_PK");
		stmt.executeUpdate(sql.toString());
		
		populateTables();
    	populateChildTable();
    	populateGrandChildTable();
    	
		ccmr_id.setInPrimaryKey(false);
    	
    	ctmr.setChildMergeAction(ChildMergeActionType.UPDATE_FAIL_ON_CONFLICT);
    	cctmr.setChildMergeAction(ChildMergeActionType.UPDATE_FAIL_ON_CONFLICT);
    	try {
    		runProcessor();
    	} finally {
    		// Should not have thrown an exception by this point. With the old bug this is testing for,
    		// It would throw an SQLException regarding the missing WHERE clause.

    		// Post clean-up to place back the PK constraints in the tables and column merge rules
    		sql = new StringBuilder();
    		sql.append("ALTER TABLE ");
    		sql.append(getFullTableName()).append("_CHILD");
    		sql.append("\n ADD CONSTRAINT BETTER_CHILD_PK PRIMARY KEY (ID)");
    		stmt.executeUpdate(sql.toString());

    		sql = new StringBuilder();
    		sql.append("ALTER TABLE ");
    		sql.append(getFullTableName()).append("_GCHILD");
    		sql.append("\n ADD CONSTRAINT BETTER_CHILD_FK FOREIGN KEY (GPARENT_ID) REFERENCES ");
    		sql.append(getFullTableName()).append("_CHILD(PARENT_ID)");
    		stmt.executeUpdate(sql.toString());

    		ccmr_id.setInPrimaryKey(true);
    	}
    	
		stmt = sCon.createStatement();
		ResultSet rs;
		
		sql = new StringBuilder();
		sql.append("SELECT COUNT(*) FROM ");
		sql.append(getFullTableName() + "_CHILD");
		sql.append("\n WHERE PARENT_ID = 1 OR PARENT_ID = 0 ");
		sql.append("OR PARENT_ID = 4 OR PARENT_ID = 3 OR PARENT_ID = 5");
		
		rs = stmt.executeQuery(sql.toString());
		rs.next();
		assertEquals("Duplicate child records not deleted.", 0, rs.getInt(1));

		sql = new StringBuilder();
		sql.append("SELECT COUNT(*) FROM ");
		sql.append(getFullTableName() + "_CHILD");
		sql.append("\n WHERE PARENT_ID = 2");
		
		rs = stmt.executeQuery(sql.toString());
		rs.next();
		assertEquals("Non-duplicate child records missing.", 1, rs.getInt(1));
    }
    
    public void testUpdateDeleteOnConflict() throws Exception{

    	populateTables();
    	populateChildTable();
    	populateGrandChildTable();
    	
    	ctmr.setChildMergeAction(ChildMergeActionType.UPDATE_DELETE_ON_CONFLICT);
    	cctmr.setChildMergeAction(ChildMergeActionType.UPDATE_DELETE_ON_CONFLICT);
	
		runProcessor();
    	
		Statement stmt = sCon.createStatement();
		ResultSet rs;
		
    	StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) FROM ");
		sql.append(getFullTableName() + "_CHILD");
		sql.append("\n WHERE PARENT_ID = 1 OR PARENT_ID = 0 ");
		sql.append("OR PARENT_ID = 4 OR PARENT_ID = 3 OR PARENT_ID = 5");
		
		rs = stmt.executeQuery(sql.toString());
		rs.next();
		
		assertEquals("Duplicate child records not deleted.", 0, rs.getInt(1));

		// problem with exported key not being recognized is blocking me from
		// confirming the accuracy of the rest of this test case, fix if needed please
		sql = new StringBuilder();
		sql.append("SELECT COUNT(*) FROM ");
		sql.append(getFullTableName() + "_CHILD");
		sql.append("\n WHERE PARENT_ID = 2");
		
		rs = stmt.executeQuery(sql.toString());
		rs.next();
		assertEquals("Non-duplicate child records missing.", 1, rs.getInt(1));
		
		sql = new StringBuilder();
		sql.append("SELECT COUNT(*) FROM ");
		sql.append(getFullTableName() + "_CHILD");
		sql.append("\n WHERE PARENT_ID = 6");
		
		rs = stmt.executeQuery(sql.toString());
		rs.next();
		assertEquals("Non-duplicate child records missing.", 0, rs.getInt(1));
		
		sql = new StringBuilder();
		sql.append("SELECT COUNT(*) FROM ");
		sql.append(getFullTableName() + "_GCHILD");
		sql.append("\n WHERE GPARENT_ID = 1 OR GPARENT_ID = 0 ");
		sql.append("OR GPARENT_ID = 4 OR GPARENT_ID = 3 OR GPARENT_ID = 5");
		
		rs = stmt.executeQuery(sql.toString());
		rs.next();
		assertEquals("Grandchild duplicate records not merged.", 0, rs.getInt(1));
		
		sql = new StringBuilder();
		sql.append("SELECT COUNT(*) FROM ");
		sql.append(getFullTableName() + "_GCHILD");
		sql.append("\n WHERE GPARENT_ID = 2 OR GPARENT_ID = 6");
		
		rs = stmt.executeQuery(sql.toString());
		rs.next();
		assertEquals("Non-duplicate grandchild records missing.", 6, rs.getInt(1));
    }
    
    public void testMergeOnConflict() throws Exception {
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
    	
    	try {
    		runProcessor();
    		// The merge engine does not support merging in a data model where the key the child table imports
    		// from the parent is NOT part of its primary key, and it exports that same imported key into
    		// a grandchild table. If the platform does not support deferrable constraints, then it is
    		// impossible to update the child table records without violating foreign key constraints in 
    		// the grandchild table, so we would have to create a new child record with a new PK, which we
    		// don't support and currently don't have time to implement properly yet. When we finally do,
    		// this test case should change.
    		fail("Merging should have failed because this type of data model structure is not supported.");
    	} catch (UnsupportedOperationException e) {
    		// correct behavior
    	}
    }
    
	protected boolean execSQL(Connection conn, String sql) throws SQLException {
		Statement stmt = null;
		stmt = conn.createStatement();
		stmt.executeUpdate(sql);
		if (stmt != null) stmt.close();

		return true;
	}
	
	protected abstract String getFullTableName();
	
	protected abstract JDBCDataSource getDS();
	
	private void runProcessor() throws Exception {
		con.setAutoCommit(false);
		try {
			mpor = new MergeProcessor(project, con, logger);
			mpor.call();
			con.commit();
		} catch (Exception e) {
			con.rollback();
			throw e;
		} finally {
//			con.close();
		}
	}
}
