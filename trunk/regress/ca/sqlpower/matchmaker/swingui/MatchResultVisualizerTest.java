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

package ca.sqlpower.matchmaker.swingui;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.DBTestUtil;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.PotentialMatchRecord;
import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.SourceTableRecord;
import ca.sqlpower.matchmaker.TestingMatchMakerSession;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.matchmaker.dao.StubMatchMakerDAO;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.swingui.MatchResultVisualizer.SetDuplicateAction;
import ca.sqlpower.matchmaker.swingui.MatchResultVisualizer.SetMasterAction;
import ca.sqlpower.matchmaker.swingui.MatchResultVisualizer.SetNoMatchAction;
import ca.sqlpower.matchmaker.swingui.MatchResultVisualizer.SetUnmatchAction;
import ca.sqlpower.matchmaker.util.MMTestUtils;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLIndex.AscendDescend;
import ca.sqlpower.sqlobject.SQLIndex.Column;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.sqlobject.SQLSchema;
import ca.sqlpower.sqlobject.SQLTable;

public class MatchResultVisualizerTest extends TestCase {
	
	Logger logger = Logger.getLogger(MatchResultVisualizerTest.class);
	TestingMatchMakerSession session;
	Project project;
	MatchResultVisualizer visualizer;
	SourceTableRecord lhs;
	SourceTableRecord rhs;
	PotentialMatchRecord pmr;
	SQLDatabase db;
	Connection con;
	
	protected void setUp() throws Exception {
		super.setUp();
		JDBCDataSource dataSource = DBTestUtil.getHSQLDBInMemoryDS();
		db = new SQLDatabase(dataSource);
		con = db.getConnection();
		
		MMTestUtils.createResultTable(con);
		MMTestUtils.createSourceTable(con);

		SQLSchema plSchema = db.getSchemaByName("pl");

		SQLTable resultTable = db.getTableByName(null, "pl", "match_results");
		final SQLTable sourceTable = db.getTableByName(null, "pl", "source_table");
		
		sourceTable.getColumns();

		SQLIndex sourceTableIndex = new SQLIndex("SOURCE_PK", true, null, null, null);
		sourceTableIndex.addChild(new Column(sourceTable.getColumn(0), AscendDescend.UNSPECIFIED));
		sourceTable.addIndex(sourceTableIndex);

		plSchema.addChild(sourceTable);

		MatchMakerSession session = new StubMatchMakerSession() {
			@Override
			public Connection getConnection() {
				try {
					return db.getConnection();
				} catch (SQLObjectException e) {
					throw new SQLObjectRuntimeException(e);
				}
			}
			
			@Override
			public SQLDatabase getDatabase() {
				return db;
			}
			
			@Override
			public <T extends MatchMakerObject> MatchMakerDAO<T> getDAO(Class<T> businessClass) {
		        return new StubMatchMakerDAO<T>(businessClass);
		    }
			
			@Override
			public boolean tableExists(String spDataSourceName, String catalog,
					String schema, String tableName) throws SQLObjectException {
				return true;
			}
			
			@Override
			public SQLTable findPhysicalTableByName(String spDataSourceName,
					String catalog, String schema, String tableName)
					throws SQLObjectException {
				return sourceTable;
			}
		};

		project = new Project();
		project.setSession(session);
		project.setResultTable(resultTable);
		project.setSourceTable(sourceTable);
		project.setSourceTableIndex(sourceTableIndex);
		
		visualizer = new MatchResultVisualizer(project, null);
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("lhs");
		lhs = new SourceTableRecord(project, keyList);
		keyList.clear();
		keyList.add("rhs");
		rhs = new SourceTableRecord(project, keyList);
		pmr = new PotentialMatchRecord(new MungeProcess(),
										MatchType.UNMATCH,
										lhs,
										rhs,
										false);
		visualizer.getPool().addSourceTableRecord(lhs);
		visualizer.getPool().addSourceTableRecord(rhs);
		visualizer.getPool().addPotentialMatch(pmr);
	}
	
	protected void tearDown() throws Exception {
		MMTestUtils.dropResultTable(con);
		MMTestUtils.dropSourceTable(con);
		con.close();
	}

	/**
	 * This test ensures that the correct list of actions is returned when
	 * looking for the actions needed on the LHS record.
	 */
	public void testLHSGetActions() {
		List<Action> actions = visualizer.getActions(lhs, lhs);
		
		
		boolean setMasterExists = false;
		boolean setNoMatchExists = false;
		boolean setUnmatchExists = false;
		
		for (Action a : actions) {
			if (a instanceof SetMasterAction) {
				assertFalse("There should only be one SetMasterAction", setMasterExists);
				setMasterExists = true;
			} else if (a instanceof SetNoMatchAction) {
				assertFalse("There should only be one SetNoMatchAction", setNoMatchExists);
				setNoMatchExists = true;
			} else if (a instanceof SetUnmatchAction) {
				assertFalse("There should only be one SetUnmatchAction", setUnmatchExists);
				setUnmatchExists = true;
			} else {
				fail("Invalid action type: " + a.getClass().getName());
			}
		}
		
		assertTrue(setMasterExists);
		assertTrue(setNoMatchExists);
		assertTrue(setUnmatchExists);
		assertEquals("There should be 3 actions for this case", 3, actions.size());
	}

	/**
	 * This test ensures that the correct list of actions is returned
	 * when the RHS is unmatched to the LHS.
	 */
	public void testUnmatchedGetActions() {
		List<Action> actions = visualizer.getActions(lhs, rhs);
		
		boolean masterExists = false;
		boolean duplicateExists = false;
		boolean noMatchExists = false;
		for (Action a : actions) {
			if (a instanceof SetMasterAction) {
				assertFalse(masterExists);
				masterExists = true;
			} else if (a instanceof SetDuplicateAction) {
				assertFalse(duplicateExists);
				duplicateExists = true;
			} else if (a instanceof SetNoMatchAction) {
				assertFalse(noMatchExists);
				noMatchExists = true;
			} else {
				fail("Invalid action type: " + a.getClass().getName());
			}
		}
		
		assertTrue(masterExists);
		assertTrue(noMatchExists);
		assertTrue(duplicateExists);
		assertEquals("There should be 3 actions for this case", 3, actions.size());
	}
	
	/**
	 * This test ensures that we receive the correct actions when the RHS
	 * record is the master of the LHS record.
	 */
	public void testMasterGetActions() {
		pmr.setMatchStatus(MatchType.MATCH);
		pmr.setMasterRecord(rhs);
		List<Action> actions = visualizer.getActions(lhs, rhs);
		
		boolean unMatchExists = false;
		boolean noMatchExists = false;
		for (Action a : actions) {
			if (a instanceof SetUnmatchAction) {
				assertFalse(unMatchExists);
				unMatchExists = true;
			} else if (a instanceof SetNoMatchAction) {
				assertFalse(noMatchExists);
				noMatchExists = true;
			} else {
				fail("Invalid action type: " + a.getClass().getName());
			}
		}
		
		assertTrue(unMatchExists);
		assertTrue(noMatchExists);
		assertEquals("There should be 2 actions for this case", 2, actions.size());

	}

	/**
	 * This test ensures that we receive the correct actions when the LHS
	 * record is the master of the RHS record.
	 */
	public void testDuplicateGetActions() {
		pmr.setMatchStatus(MatchType.MATCH);
		pmr.setMasterRecord(lhs);
		List<Action> actions = visualizer.getActions(lhs, rhs);
		
		boolean unMatchExists = false;
		boolean noMatchExists = false;
		for (Action a : actions) {
			if (a instanceof SetUnmatchAction) {
				assertFalse(unMatchExists);
				unMatchExists = true;
			} else if (a instanceof SetNoMatchAction) {
				assertFalse(noMatchExists);
				noMatchExists = true;
			} else {
				fail("Invalid action type: " + a.getClass().getName());
			}
		}
		
		assertTrue(unMatchExists);
		assertTrue(noMatchExists);
		assertEquals("There should be 2 actions for this case", 2, actions.size());
	}
	
	/**
	 * This tests that the correct list of actions are returned when the
	 * RHS is a no-match to the LHS.
	 */
	public void testNomatchGetActions() {
		pmr.setMatchStatus(MatchType.NOMATCH);
		List<Action> actions = visualizer.getActions(lhs, rhs);
		
		boolean masterExists = false;
		boolean duplicateExists = false;
		boolean unMatchExists = false;
		for (Action a : actions) {
			if (a instanceof SetMasterAction) {
				assertFalse(masterExists);
				masterExists = true;
			} else if (a instanceof SetDuplicateAction) {
				assertFalse(duplicateExists);
				duplicateExists = true;
			} else if (a instanceof SetUnmatchAction) {
				assertFalse(unMatchExists);
				unMatchExists = true;
			} else {
				fail("Invalid action type: " + a.getClass().getName());
			}
		}
		
		assertTrue(masterExists);
		assertTrue(duplicateExists);
		assertTrue(unMatchExists);
		assertEquals("There should be 3 actions for this case", 3, actions.size());		
	}
}