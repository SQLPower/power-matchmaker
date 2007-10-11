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

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLStatement;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.matchmaker.dao.StubMatchMakerDAO;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SQL;

/**
 * This is a class to test the merge processor on an oracle test server.
 */
public class MergeProcessorOracleTest extends AbstractMergeProcessorTest {
	
	@Override
	protected void setUp() throws Exception {
		match = new Match();
		
        SPDataSource ds = DBTestUtil.getOracleDS();
        SQLDatabase db = new SQLDatabase(ds);
        session = new TestingMatchMakerSession() {
			SPDataSource ds = DBTestUtil.getOracleDS();
			SQLDatabase db = new SQLDatabase(ds);
			
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
		
		match.setSession(session);
        session.setConnection(db.getConnection());
	}
	
	@Override
	protected void populateTables() throws Exception {
		super.populateTables();
		
    	SQLDatabase db = session.getDatabase();
    	SPDataSource ds = session.getDatabase().getDataSource();
    	con = db.getConnection();
		String sql = "DROP TABLE " + getFullTableName();
		execSQL(con,sql);
		
		//Creates the source table
		sql = "CREATE TABLE " + getFullTableName() + " ("+
					"\n ID NUMBER(22,0) NOT NULL PRIMARY KEY," +
					"\n COL_STRING VARCHAR2(20) NULL," +
					"\n COL_DATE DATE NULL," +
					"\n COL_NUMBER NUMBER(22,0) NULL)";
		execSQL(con,sql);
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
        match.setSourceTable(db.getTableByName("MERGE_TEST"));
        match.setSourceTableIndex(db.getTableByName("MERGE_TEST").getPrimaryKeyIndex());

        //Creates the result Table
        DDLGenerator ddlg = null;
    	try {
    		ddlg = DDLUtils.createDDLGenerator(ds);
    	} catch (ClassNotFoundException e) {
    		fail("DDLUtils.createDDLGenerator(SPDataSource) threw a ClassNotFoundException");
    	}
    	assertNotNull("DDLGenerator error", ddlg);
		ddlg.setTargetSchema(ds.getPlSchema());
		match.setResultTableName("MERGE_TEST_RESULT");
		match.setResultTableSchema(ds.getPlSchema());
		
		if (Match.doesResultTableExist(session, match)) {
			ddlg.dropTable(match.getResultTable());
		}
		ddlg.addTable(match.createResultTable());
		ddlg.addIndex((SQLIndex) match.getResultTable().getIndicesFolder().getChild(0));
		
	    for (DDLStatement sqlStatement : ddlg.getDdlStatements()) {
	    	sql = sqlStatement.getSQLText();
	    	execSQL(con,sql);
	    }
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
	    	"(4,3,10,'UNMATCH','', 'test')";
	    execSQL(con,sql);
	    sql = "INSERT INTO " + getFullTableName() + "_RESULT " +
	    	"(DUP_CANDIDATE_10, DUP_CANDIDATE_20, MATCH_PERCENT, MATCH_STATUS, DUP1_MASTER_IND, GROUP_ID)" +
	    	"VALUES " + 
	    	"(5,4,10,'MATCH','N', 'test')";
	    execSQL(con,sql);
	    
	    cmr_string.setColumn(match.getSourceTable().getColumnByName("COL_STRING"));   	
		cmr_date.setColumn(match.getSourceTable().getColumnByName("COL_DATE"));
		cmr_number.setColumn(match.getSourceTable().getColumnByName("COL_NUMBER"));
		
		tmr.setTable(match.getSourceTable());
		
		mpor = new MergeProcessor(match, session);
	}

	@Override
	protected String getFullTableName() {
		return "MM_TEST.MERGE_TEST";
	}
}
