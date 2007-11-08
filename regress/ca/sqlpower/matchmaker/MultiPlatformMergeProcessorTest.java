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
import java.sql.SQLException;

import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.sql.SPDataSource;

/**
 * A test of the merge processor with a SQLServer pl schema
 * and the tables on Oracle.
 */

public class MultiPlatformMergeProcessorTest extends AbstractMergeProcessorTest {

static boolean alreadyRan = false;
	
	@Override
	protected void setUp() throws Exception {
		if (alreadyRan) return;
		super.setUp();
		alreadyRan = true;
	}
	
	protected String getFullTableName() {
		return "MM_TEST.MERGE_TEST";
	}
	
	protected SPDataSource getDS() {
		return DBTestUtil.getSqlServerDS();
	}

	protected void createTables() throws Exception {
		SQLDatabase db = session.getDatabase(DBTestUtil.getOracleDS());
		Connection con = db.getConnection();
		//Drop the old tables
		String sql = "DROP TABLE " + getFullTableName() + "_GCHILD";
		try {
			execSQL(con,sql);
		} catch (SQLException e) {
			// Ignoring if the table did not exist before hand.
			e.printStackTrace();
		}
		
		sql = "DROP TABLE " + getFullTableName() + "_CHILD";
		try {
			execSQL(con,sql);
		} catch (SQLException e) {
			// Ignoring if the table did not exist before hand.
			e.printStackTrace();
		}
		
		sql = "DROP TABLE " + getFullTableName();
		try {
			execSQL(con,sql);
		} catch (SQLException e) {
			// Ignoring if the table did not exist before hand.
			e.printStackTrace();
		}
		
		// Creates the source table
		sql = "CREATE TABLE " + getFullTableName() + " ("+
					"\n ID NUMBER(22,0) NOT NULL PRIMARY KEY," +
					"\n COL_STRING VARCHAR2(20) NULL," +
					"\n COL_DATE DATE NULL," +
					"\n COL_NUMBER NUMBER(22,0) NULL)";
		execSQL(con,sql);
        
        // Creates the child table
		sql = "CREATE TABLE " + getFullTableName() + "_CHILD ("+
					"\n PARENT_ID NUMBER(22,0) NOT NULL references " + getFullTableName() + "(ID)," +
					"\n ID NUMBER(22,0) NOT NULL," +
					"\n COL_STRING VARCHAR2(20) NULL," +
					"\n COL_DATE DATE NULL," +
					"\n COL_NUMBER NUMBER(22,0) NULL)";
		execSQL(con,sql);
		
		sql = "ALTER TABLE " + getFullTableName() + "_CHILD " +
					"\n ADD PRIMARY KEY (PARENT_ID, ID)";
		execSQL(con,sql);
		
		// Creates the grand child table
		sql = "CREATE TABLE " + getFullTableName() + "_GCHILD ("+
					"\n GPARENT_ID NUMBER(22,0) NOT NULL," +
					"\n PARENT_ID NUMBER(22,0) NOT NULL," +
					"\n ID NUMBER(22,0) NOT NULL," +
					"\n COL_STRING VARCHAR2(20) NULL," +
					"\n COL_DATE DATE NULL," +
					"\n COL_NUMBER NUMBER(22,0) NULL)";
		execSQL(con,sql);
		
		sql = "ALTER TABLE " + getFullTableName() + "_GCHILD " +
			"\n ADD PRIMARY KEY (GPARENT_ID, PARENT_ID, ID)";
		execSQL(con,sql);
		
		sql = "ALTER TABLE " + getFullTableName() + "_GCHILD " +
			"\n ADD CONSTRAINT fk_MERGE_TEST_GCHILD FOREIGN KEY (GPARENT_ID, PARENT_ID) REFERENCES " + getFullTableName() + "_CHILD(PARENT_ID, ID)";
		execSQL(con, sql);
		
		sourceTable = db.getTableByName("MERGE_TEST");
		childTable = db.getTableByName("MERGE_TEST_CHILD");
		grandChildTable = db.getTableByName("MERGE_TEST_GCHILD");
		project.setSourceTable(sourceTable);
        project.setSourceTableIndex(sourceTable.getPrimaryKeyIndex());
		project.setResultTableName("MERGE_TEST_RESULT");
		project.setResultTableSchema(ds.getPlSchema());
		project.setResultTableSPDatasource(DBTestUtil.getOracleDS().getName());
		
		try {
			con.close();
		} catch (SQLException e) {
		}
		
	}

}
