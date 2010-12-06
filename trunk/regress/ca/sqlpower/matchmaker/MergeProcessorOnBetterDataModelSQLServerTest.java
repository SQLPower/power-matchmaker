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

import java.sql.SQLException;

import ca.sqlpower.sql.JDBCDataSource;

/**
 * This is a class to test the merge processor on a sql server test server.
 */
public class MergeProcessorOnBetterDataModelSQLServerTest extends AbstractMergeProcessorOnBetterDataModelTest {
	
	static boolean alreadyRan = false;
	
	@Override
	protected void setUp() throws Exception {
		if (alreadyRan) return;
		super.setUp();
		alreadyRan = true;
	}
	
	protected String getFullTableName() {
		return "MM_TEST.MM_TEST.BETTER_MERGE_TEST";
	}

	protected void createTables() throws Exception {
		
		// Drop the old tables
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
		sql = "CREATE TABLE " + getFullTableName() + " (" +
			"\n ID NUMERIC NOT NULL PRIMARY KEY," +
			"\n COL_STRING VARCHAR(20) NULL," +
			"\n COL_DATE DATETIME NULL," +
			"\n COL_NUMBER NUMERIC NULL)";
		execSQL(con,sql);
		
		// Creates the child table
		sql = "CREATE TABLE " + getFullTableName() + "_CHILD (" +
			"\n ID NUMERIC NOT NULL," +
			"\n PARENT_ID NUMERIC NOT NULL references " + getFullTableName() + "(ID)," +
			"\n COL_STRING VARCHAR(20) NULL," +
			"\n COL_DATE DATETIME NULL," +
			"\n COL_NUMBER NUMERIC NULL," + 
			"\n CONSTRAINT PARENT_ID_UNIQ UNIQUE (PARENT_ID)," +
			"\n CONSTRAINT BETTER_CHILD_PK PRIMARY KEY (ID))";
		execSQL(con,sql);
		
		// Creates the grand child table
		sql = "CREATE TABLE " + getFullTableName() + "_GCHILD (" +
			"\n ID NUMERIC NOT NULL PRIMARY KEY," +
			"\n GPARENT_ID NUMERIC NOT NULL," +
			"\n COL_STRING VARCHAR(20) NULL," +
			"\n COL_DATE DATETIME NULL," +
			"\n COL_NUMBER NUMERIC NULL," +
			"\n CONSTRAINT BETTER_CHILD_FK FOREIGN KEY (GPARENT_ID) references " + getFullTableName() + "_CHILD(PARENT_ID))";
		execSQL(con,sql);
		
		sourceTable = db.getTableByName("MM_TEST", "MM_TEST", "BETTER_MERGE_TEST");
		childTable = db.getTableByName("MM_TEST", "MM_TEST", "BETTER_MERGE_TEST_CHILD");
		grandChildTable = db.getTableByName("MM_TEST", "MM_TEST", "BETTER_MERGE_TEST_GCHILD");
        project.setSourceTable(sourceTable);
        project.setSourceTableIndex(sourceTable.getPrimaryKeyIndex());
		project.setResultTableName("BETTER_MERGE_TEST_RESULT");
		project.setResultTableSchema("MM_TEST");
		project.setResultTableCatalog("MM_TEST");
		project.setResultTableSPDatasource(ds.getName());
	}

	protected JDBCDataSource getDS() {
		return DBTestUtil.getSqlServerDS();
	}
}
