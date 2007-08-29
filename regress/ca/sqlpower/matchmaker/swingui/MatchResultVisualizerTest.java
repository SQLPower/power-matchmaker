package ca.sqlpower.matchmaker.swingui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLIndex.IndexType;
import ca.sqlpower.matchmaker.DBTestUtil;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchRuleSet;
import ca.sqlpower.matchmaker.PotentialMatchRecord;
import ca.sqlpower.matchmaker.SourceTableRecord;
import ca.sqlpower.matchmaker.TestingMatchMakerSession;
import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;
import ca.sqlpower.matchmaker.swingui.MatchResultVisualizer.SetDuplicateAction;
import ca.sqlpower.matchmaker.swingui.MatchResultVisualizer.SetMasterAction;
import ca.sqlpower.matchmaker.swingui.MatchResultVisualizer.SetNoMatchAction;
import ca.sqlpower.matchmaker.swingui.MatchResultVisualizer.SetUnmatchAction;
import ca.sqlpower.sql.SPDataSource;

public class MatchResultVisualizerTest extends TestCase {
	
	Logger logger = Logger.getLogger(MatchResultVisualizerTest.class);
	TestingMatchMakerSession session;
	Match match;
	MatchResultVisualizer visualizer;
	SourceTableRecord lhs;
	SourceTableRecord rhs;
	PotentialMatchRecord pmr;
	SQLDatabase db;
	Connection con;
	
	protected void setUp() throws Exception {
		super.setUp();
		SPDataSource dataSource = DBTestUtil.getHSQLDBInMemoryDS();
		db = new SQLDatabase(dataSource);
		con = db.getConnection();
		
		createResultTable(con);

		SQLSchema plSchema = db.getSchemaByName("pl");

		SQLTable resultTable = db.getTableByName(null, "pl", "match_results");

		SQLTable sourceTable = new SQLTable(plSchema, "source_table", null, "TABLE",
				true);
		sourceTable.addColumn(new SQLColumn(sourceTable, "PK1", Types.INTEGER,
				10, 0));
		sourceTable.addColumn(new SQLColumn(sourceTable, "FOO", Types.VARCHAR,
				10, 0));
		sourceTable.addColumn(new SQLColumn(sourceTable, "BAR", Types.VARCHAR,
				10, 0));

		SQLIndex sourceTableIndex = new SQLIndex("SOURCE_PK", true, null,
				IndexType.OTHER, null);
		sourceTableIndex.addChild(sourceTableIndex.new Column(sourceTable
				.getColumn(0), false, false));
		sourceTable.addIndex(sourceTableIndex);

		plSchema.addChild(sourceTable);

		MatchMakerSession session = new StubMatchMakerSession() {
			@Override
			public Connection getConnection() {
				try {
					return db.getConnection();
				} catch (ArchitectException e) {
					throw new ArchitectRuntimeException(e);
				}
			}
		};

		match = new Match();
		match.setSession(session);
		match.setResultTable(resultTable);
		match.setSourceTable(sourceTable);
		match.setSourceTableIndex(sourceTableIndex);
		
		visualizer = new MatchResultVisualizer(match);
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("lhs");
		lhs = new SourceTableRecord(session,
									match, 
									keyList);
		keyList.clear();
		keyList.add("rhs");
		rhs = new SourceTableRecord(session,
									match, 
									keyList);
		pmr = new PotentialMatchRecord(new MatchRuleSet(),
										MatchType.UNMATCH,
										lhs,
										rhs,
										false);
		visualizer.getPool().addSourceTableRecord(lhs);
		visualizer.getPool().addSourceTableRecord(rhs);
		visualizer.getPool().addPotentialMatch(pmr);
	}
	
	protected void tearDown() throws Exception {
		dropResultTable(con);
		con.close();
	}

	private static void dropResultTable(Connection con) throws SQLException {
		Statement stmt = con.createStatement();
		stmt.executeUpdate("DROP TABLE pl.match_results");
		stmt.close();
	}
	
	/**
	 * This test ensures that the correct list of actions is returned when
	 * looking for the actions needed on the LHS record.
	 */
	public void testLHSGetActions() {
		List<Action> actions = visualizer.getActions(lhs, lhs);
		
		assertEquals("There should be 3 actions for this case", 3, actions.size());
		
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
	}
	
	/**
	 * This test ensures that we receive the correct actions when the RHS
	 * record is the master of the LHS record.
	 */
	public void testMasterGetActions() {
		pmr.setMatchStatus(MatchType.MATCH);
		pmr.setMaster(rhs);
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
	}

	/**
	 * This test ensures that we receive the correct actions when the LHS
	 * record is the master of the RHS record.
	 */
	public void testDuplicateGetActions() {
		pmr.setMatchStatus(MatchType.MATCH);
		pmr.setMaster(lhs);
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
		
	}
	
	private static void createResultTable(Connection con) throws SQLException {
		Statement stmt = con.createStatement();
		stmt.executeUpdate("CREATE TABLE pl.match_results ("
				+ "\n DUP_CANDIDATE_10 varchar(50) not null,"
				+ "\n DUP_CANDIDATE_20 varchar(50) not null,"
				+ "\n CURRENT_CANDIDATE_10 varchar(50),"
				+ "\n CURRENT_CANDIDATE_20 varchar(50)," 
				+ "\n DUP_ID0 varchar(50),"
				+ "\n MASTER_ID0 varchar(50),"
				+ "\n CANDIDATE_10_MAPPED varchar(1),"
				+ "\n CANDIDATE_20_MAPPED varchar(1),"
				+ "\n MATCH_PERCENT integer," 
				+ "\n GROUP_ID varchar(60),"
				+ "\n MATCH_DATE timestamp," 
				+ "\n MATCH_STATUS varchar(60),"
				+ "\n MATCH_STATUS_DATE timestamp,"
				+ "\n MATCH_STATUS_USER varchar(60),"
				+ "\n DUP1_MASTER_IND  varchar(1)" + "\n)");
		stmt.close();
	}
}