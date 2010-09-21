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

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;
import ca.sqlpower.matchmaker.munge.InputDescriptor;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeResultStep;
import ca.sqlpower.matchmaker.munge.SQLInputStep;
import ca.sqlpower.matchmaker.util.MMTestUtils;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.SQL;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLSchema;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLIndex.AscendDescend;
import ca.sqlpower.sqlobject.SQLIndex.Column;

public class MatchEngineImplTest extends TestCase {

	private MatchEngineImpl engine;
	private Connection con;
	private SQLDatabase db;
	private SQLTable resultTable;
	private Project project;
	private TestingMatchMakerSession session;
	private SQLTable sourceTable;
    final private Logger logger = Logger.getLogger("testLogger");
	
	protected void setUp() throws Exception {
	    JDBCDataSource dataSource = DBTestUtil.getHSQLDBInMemoryDS();
		db = new SQLDatabase(dataSource);
		con = db.getConnection();
		MMTestUtils.createResultTable(con);
		MMTestUtils.createSourceTable(con);
		
		SQLSchema plSchema = db.getSchemaByName("pl");

		sourceTable = db.getTableByName(null, "pl", "source_table");
		SQLIndex sourceTableIndex = new SQLIndex("SOURCE_PK", true, null, null, null);
		sourceTableIndex.addChild(new Column(sourceTable.getColumn(0), AscendDescend.UNSPECIFIED));
		sourceTable.addIndex(sourceTableIndex);
		plSchema.addChild(sourceTable);

		Statement stmt = con.createStatement();
		stmt.executeUpdate("INSERT into pl.source_table VALUES ("
				+ SQL.quote("1") + ", 'foo1', 'bar1')");
		stmt.executeUpdate("INSERT into pl.source_table VALUES ("
				+ SQL.quote("2") + ", 'foo2', 'bar2')");
		stmt.executeUpdate("INSERT into pl.source_table VALUES ("
				+ SQL.quote("3") + ", 'foo3', 'bar3')");
		stmt.executeUpdate("INSERT into pl.source_table VALUES ("
				+ SQL.quote("4") + ", 'foo1', 'bar1')");
		stmt.close();
		
		resultTable = db.getTableByName(null, "pl", "match_results");
		session = new TestingMatchMakerSession() {
			@Override
			public Connection getConnection() {
				try {
					return db.getConnection();
				} catch (Exception ex) {
					throw new RuntimeException();
				}
			}
		};
		session.setDatabase(db);
		
		project = new Project();
		project.setSession(session);
		project.setSourceTable(sourceTable);
		project.setSourceTableIndex(sourceTableIndex);
		project.setResultTable(resultTable);
		MungeSettings settings = new MungeSettings();
		File file = File.createTempFile("matchTest", "log");
		settings.setLog(file);
		project.setMungeSettings(settings);
		
		MungeProcess mungeProcessOne = new MungeProcess();
		mungeProcessOne.setName("Munge_Process_One");
		SQLInputStep inputStep = new SQLInputStep();
		mungeProcessOne.addChild(inputStep);
		
		project.addChild(mungeProcessOne);

		MungeResultStep outputStep = inputStep.getOutputStep();
		mungeProcessOne.addChild(outputStep);

		inputStep.refresh(logger);

		outputStep.addInput(new InputDescriptor("result2", Object.class));
		outputStep.connectInput(0, inputStep.getOutputByName("FOO"));
		outputStep.connectInput(1, inputStep.getOutputByName("BAR"));
		engine = new MatchEngineImpl(session, project);
	}

	public void tearDown() throws Exception {
		MMTestUtils.dropResultTable(con);
		MMTestUtils.dropSourceTable(con);
		con.close();
	}
	
	public void testCall() throws Exception {
		engine.call();

		MatchPool pool = new MatchPool(project);
		pool.findAll(null);
		assertEquals(2, pool.getSourceTableRecords().size());
		assertEquals(1, pool.getPotentialMatches().size());
		for (SourceTableRecord s: pool.getSourceTableRecords()) {
			assertTrue(s.getKeyValues().get(0).equals("1") ||
					s.getKeyValues().get(0).equals("4"));
		}
		
		for (PotentialMatchRecord p: pool.getPotentialMatches()) {
			assertTrue(p.getOriginalLhs().getKeyValues().get(0).equals("1") ||
					p.getOriginalLhs().getKeyValues().get(0).equals("4"));
			assertTrue(p.getOriginalRhs().getKeyValues().get(0).equals("1") ||
					p.getOriginalRhs().getKeyValues().get(0).equals("4"));
			assertEquals(null, p.getMaster());
			assertEquals(MatchType.UNMATCH, p.getMatchStatus());
		}
	}
	
	public void testCallOnMultipleMungeProcesses() throws Exception {		
		MungeProcess mungeProcessTwo = new MungeProcess();
		mungeProcessTwo.setName("Munge_Process_Two");
		SQLInputStep inputStep = new SQLInputStep();
		mungeProcessTwo.addChild(inputStep);
		
		project.addChild(mungeProcessTwo);
		
		MungeResultStep outputStep = inputStep.getOutputStep();
		mungeProcessTwo.addChild(outputStep);

		inputStep.refresh(logger);

		outputStep.connectInput(0, inputStep.getOutputByName("FOO"));
		
		engine.call();
		
		MatchPool pool = new MatchPool(project);
		pool.findAll(null);
		assertEquals(2, pool.getSourceTableRecords().size());
		assertEquals(1, pool.getPotentialMatches().size());
	}
}
