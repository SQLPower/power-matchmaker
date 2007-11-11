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

import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.matchmaker.dao.StubMatchMakerDAO;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.SQLInputStep;
import ca.sqlpower.matchmaker.munge.UpperCaseMungeStep;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SQL;

public abstract class AbstractCleanseEngineImplTest extends TestCase{
    Project project;
    TestingMatchMakerSession session;
    Connection con;
    SQLDatabase db;
    SPDataSource ds;
    SQLTable sourceTable;
    SQLInputStep step;
    CleanseEngineImpl engine;
    final Logger logger = Logger.getLogger("testLogger");
    
	protected void setUp() throws Exception {
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

		project.setType(ProjectMode.CLEANSE);

		//This is different for Oracle and SQL Server
		createTables();
		step = new SQLInputStep();

		MungeSettings settings = new MungeSettings();
		File file = File.createTempFile("cleanseTest", "log");
		settings.setLog(file);
		project.setMungeSettings(settings);
		engine = new CleanseEngineImpl(session, project);
   	}
	
	private void populateTables() throws Exception {
    	String sql;

		//delete everything from source table
		sql = "DELETE FROM " + getFullTableName();
		execSQL(con, sql);
		
		//Populates the source table
		String testString = "abcdef";
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
	}
	
	public void testSimpleCall() throws Exception{	
		populateTables();

		UpperCaseMungeStep ucms = new UpperCaseMungeStep();

		MungeProcess mungep = new MungeProcess();
		mungep.addChild(step);
		mungep.addChild(ucms);
		mungep.setName("test");
		project.addMungeProcess(mungep);
		
		MungeStep mrs = step.getOuputStep();
		mungep.addChild(mrs);
		
		step.open(logger);
        step.rollback();
		step.close();
		mrs.open(logger);
        mrs.rollback();
		mrs.close();
		mrs.connectInput(1, ucms.getChildren().get(0));
		ucms.connectInput(0, step.getChildren().get(1));
		
		engine.call();

		Connection con = project.createSourceTableConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM " + getFullTableName());

		if (!rs.next()) {
			fail("NOTHING IN THE TABLE! :(");
		}


		assertEquals("a".toUpperCase(), rs.getString(2));
		rs.next();
		assertEquals("b".toUpperCase(), rs.getString(2));
		rs.next();
		assertEquals("c".toUpperCase(), rs.getString(2));
		rs.next();
		assertEquals("d".toUpperCase(), rs.getString(2));
		rs.next();
		assertEquals("e".toUpperCase(), rs.getString(2));
		rs.next();
		assertEquals("f".toUpperCase(), rs.getString(2));

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
	
	
	protected abstract void createTables() throws Exception;
	protected abstract SPDataSource getDS();
	protected abstract String getFullTableName();
}
