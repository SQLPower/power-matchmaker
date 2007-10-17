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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLIndex.IndexType;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.Match.MatchMode;
import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.matchmaker.dao.StubMatchMakerDAO;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.swingui.StubMatchMakerSession;
import ca.sqlpower.matchmaker.util.MMTestUtils;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SQL;

public class MatchPoolTest extends TestCase {

	/**
	 * The object under test.
	 */
	private MatchPool pool;

	private Connection con;
	private SQLDatabase db;
	private SQLTable sourceTable;
	private SQLTable resultTable;
	private Match match;

	private MungeProcess groupOne;

	@Override
	protected void setUp() throws Exception {
		SPDataSource dataSource = DBTestUtil.getHSQLDBInMemoryDS();
		db = new SQLDatabase(dataSource);
		con = db.getConnection();

		MMTestUtils.createResultTable(con);
		MMTestUtils.createSourceTable(con);

		SQLSchema plSchema = db.getSchemaByName("pl");

		resultTable = db.getTableByName(null, "pl", "match_results");
		sourceTable = db.getTableByName(null, "pl", "source_table");
		
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
			
			@Override
			public <T extends MatchMakerObject> MatchMakerDAO<T> getDAO(Class<T> businessClass) {
		        return new StubMatchMakerDAO<T>(businessClass);
		    }
		};

		match = new Match();
		match.setType(MatchMode.FIND_DUPES);
		match.setSession(session);
		match.setResultTable(resultTable);
		match.setSourceTable(sourceTable);
		match.setSourceTableIndex(sourceTableIndex);

		groupOne = new MungeProcess();
		groupOne.setName("Group_One");
		match.addMatchRuleSet(groupOne);

		MungeProcess groupTwo = new MungeProcess();
		groupTwo.setName("Group_Two");
		match.addMatchRuleSet(groupTwo);
		
		pool = MMTestUtils.createTestingPool(session, match, groupOne, groupTwo);
	}

	@Override
	protected void tearDown() throws Exception {
		MMTestUtils.dropResultTable(con);
		MMTestUtils.dropSourceTable(con);
		con.close();
	}

	/**
	 * Inserts a match record described by the parameters .
	 */
	private static void insertResultTableRecord(Connection con,
			String originalLhsKey, String originalRhsKey, int matchPercent,
			String groupName) throws SQLException {
		Statement stmt = con.createStatement();
		stmt.executeUpdate("INSERT into pl.match_results VALUES ("
				+ SQL.quote(originalLhsKey) + "," + SQL.quote(originalRhsKey) + "," + SQL.quote(originalLhsKey)
				+ "," + SQL.quote(originalRhsKey) + "," + "null," + "null," + "null,"
				+ "null," + matchPercent + "," + SQL.quote(groupName) + ","
				+ "{ts '2006-11-30 17:01:06.0'}," + "null,"
				+ "{ts '2006-11-30 17:01:06.0'}," + "null," + "null)");
		stmt.close();

	}
	
	private static void insertSourceTableRecord(Connection con, String originalKey) throws SQLException {
		Statement stmt = con.createStatement();
		stmt.executeUpdate("INSERT into pl.source_table VALUES ("
				+ SQL.quote(originalKey) + ", 'foo', 'bar')");
		stmt.close();
	}

	/**
	 * Tests that findAll() does find all potential match records (graph edges)
	 * properly.
	 */
	public void testFindAllPotentialMatches() throws Exception {
		MatchPool pool = new MatchPool(match);
		insertResultTableRecord(con, "1", "2", 15, "Group_One");
		insertSourceTableRecord(con, "1");
		insertSourceTableRecord(con, "2");
		pool.findAll(new ArrayList<SQLColumn>());
		Set<PotentialMatchRecord> matches = pool.getPotentialMatches();
		assertEquals(1, matches.size());
		for (PotentialMatchRecord pmr : matches) {
			assertNotNull(pmr);
			assertNotNull(pmr.getOriginalLhs());
			assertNotNull(pmr.getOriginalLhs().getKeyValues());
			assertEquals(1, pmr.getOriginalLhs().getKeyValues().size());
			assertEquals("Group_One", pmr.getRuleSet().getName());
		}
		int originalMatchCount = matches.size();
		
		// Now we test if subsequent calls to findAll adds duplicate PotentialMatchRecords
		pool.findAll(new ArrayList<SQLColumn>());
		assertEquals("Number of PotentialMatchRecords should be the same", originalMatchCount, matches.size());
	}

	/**
	 * Tests that findAll() does find all source table records (graph nodes)
	 * properly.
	 */
	public void testFindSourceTableRecords() throws Exception {
		MatchPool pool = new MatchPool(match);
		insertResultTableRecord(con, "1", "2", 15, "Group_One");
		insertResultTableRecord(con, "1", "3", 15, "Group_One");
		insertSourceTableRecord(con, "1");
		insertSourceTableRecord(con, "2");
		insertSourceTableRecord(con, "3");
		pool.findAll(new ArrayList<SQLColumn>());
		Collection<SourceTableRecord> nodes = pool.getSourceTableRecords();
		assertEquals(3, nodes.size());

		boolean foundOne = false;
		boolean foundTwo = false;
		boolean foundThree = false;
		for (SourceTableRecord str : nodes) {
			if (str.getKeyValues().get(0).equals("1"))
				foundOne = true;
			if (str.getKeyValues().get(0).equals("2"))
				foundTwo = true;
			if (str.getKeyValues().get(0).equals("3"))
				foundThree = true;
		}

		assertTrue(foundOne);
		assertTrue(foundTwo);
		assertTrue(foundThree);
	}

	/** Tests that findAll() hooks up inbound and outbound matches properly. */
	public void testFindAllEdgeHookup() throws Exception {
		MatchPool pool = new MatchPool(match);
		insertResultTableRecord(con, "1", "2", 15, "Group_One");
		insertResultTableRecord(con, "1", "3", 15, "Group_One");
		insertSourceTableRecord(con, "1");
		insertSourceTableRecord(con, "2");
		insertSourceTableRecord(con, "3");
		pool.findAll(new ArrayList<SQLColumn>());
		Collection<SourceTableRecord> nodes = pool.getSourceTableRecords();
		assertEquals(3, nodes.size());

		// FIXME need to be able to retrieve a particular PMR by key values
	}

	/**
	 * Sets the master of a node in a graph when no masters have been set.
	 * <p>
	 * See the image for
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MungeProcess)}
	 * for details on the graph.
	 */
	public void testSetMasterWithNoMasters() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("a2");
		SourceTableRecord a2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("a1");
		SourceTableRecord a1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("a3");
		SourceTableRecord a3 = pool.getSourceTableRecord(keyList);

		pool.defineMaster(a1, a2);

		PotentialMatchRecord pmrA1ToA2 = null;
		PotentialMatchRecord pmrA2ToA3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == a1
					&& potentialMatch.getOriginalRhs() == a2) {
				pmrA1ToA2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a2
					&& potentialMatch.getOriginalRhs() == a1) {
				pmrA1ToA2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a3
					&& potentialMatch.getOriginalRhs() == a2) {
				pmrA2ToA3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a2
					&& potentialMatch.getOriginalRhs() == a3) {
				pmrA2ToA3 = potentialMatch;
			}

			if (pmrA1ToA2 != null && pmrA2ToA3 != null)
				break;
		}

		if (pmrA1ToA2 == null || pmrA2ToA3 == null) {
			fail("An edge no longer exists after we defined a1 as the master.");
		}

		assertTrue(pmrA1ToA2.getMaster() == a1);
		assertTrue(pmrA1ToA2.getDuplicate() == a2);
		assertTrue(pmrA2ToA3.getMaster() == null);
	}

	/**
	 * Sets the master everything to be a1.
	 * <p>
	 * See the image for
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MungeProcess)}
	 * for details on the graph.
	 */
	public void testSetMasterOfAllWithNoMasters() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("a2");
		SourceTableRecord a2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("a1");
		SourceTableRecord a1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("a3");
		SourceTableRecord a3 = pool.getSourceTableRecord(keyList);

		pool.defineMasterOfAll(a1);

		PotentialMatchRecord pmrA1ToA2 = null;
		PotentialMatchRecord pmrA2ToA3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == a1
					&& potentialMatch.getOriginalRhs() == a2) {
				pmrA1ToA2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a2
					&& potentialMatch.getOriginalRhs() == a1) {
				pmrA1ToA2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a3
					&& potentialMatch.getOriginalRhs() == a2) {
				pmrA2ToA3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a2
					&& potentialMatch.getOriginalRhs() == a3) {
				pmrA2ToA3 = potentialMatch;
			}

			if (pmrA1ToA2 != null && pmrA2ToA3 != null)
				break;
		}

		if (pmrA1ToA2 == null || pmrA2ToA3 == null) {
			fail("An edge no longer exists after we defined a1 as the master of all");
		}

		assertTrue(pmrA1ToA2.getMaster() == a1);
		assertTrue(pmrA1ToA2.getDuplicate() == a2);
		assertTrue(pmrA2ToA3.getMaster() == null);
		assertTrue(pmrA2ToA3.getDuplicate() == null);
		assertTrue(pmrA2ToA3.getMatchStatus() == MatchType.UNMATCH);
	}

	/**
	 * Reverse the master/duplicate relationship
	 * <p>
	 * See the image for
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MungeProcess)}
	 * for details on the graph.
	 */
	public void testSetMasterToDuplicate() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("c2");
		SourceTableRecord c2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("c1");
		SourceTableRecord c1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("c3");
		SourceTableRecord c3 = pool.getSourceTableRecord(keyList);

		pool.defineMaster(c1, c2);

		PotentialMatchRecord pmrC1ToC2 = null;
		PotentialMatchRecord pmrC2ToC3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == c1
					&& potentialMatch.getOriginalRhs() == c2) {
				pmrC1ToC2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == c2
					&& potentialMatch.getOriginalRhs() == c1) {
				pmrC1ToC2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == c3
					&& potentialMatch.getOriginalRhs() == c2) {
				pmrC2ToC3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == c2
					&& potentialMatch.getOriginalRhs() == c3) {
				pmrC2ToC3 = potentialMatch;
			}

			if (pmrC1ToC2 != null && pmrC2ToC3 != null)
				break;
		}

		if (pmrC1ToC2 == null || pmrC2ToC3 == null) {
			fail("An edge no longer exists after we defined c1 as the master of c2");
		}

		assertTrue(pmrC1ToC2.getMaster() == c1);
		assertTrue(pmrC1ToC2.getDuplicate() == c2);
		assertTrue(pmrC2ToC3.getMaster() == null);
	}

	/**
	 * Sets the master of a node that has a duplicate which is also a duplicate
	 * of the new master. This should not be a normal case.
	 * <p>
	 * See the image for
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MungeProcess)}
	 * for details on the graph.
	 */
	public void testSetMasterWithSameDuplicate() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("f1");
		SourceTableRecord f1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("f3");
		SourceTableRecord f3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("f2");
		SourceTableRecord f2 = pool.getSourceTableRecord(keyList);

		pool.defineMaster(f3, f1);

		PotentialMatchRecord pmrF1ToF2 = null;
		PotentialMatchRecord pmrF2ToF3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == f1
					&& potentialMatch.getOriginalRhs() == f2) {
				pmrF1ToF2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == f2
					&& potentialMatch.getOriginalRhs() == f1) {
				pmrF1ToF2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == f3
					&& potentialMatch.getOriginalRhs() == f2) {
				pmrF2ToF3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == f2
					&& potentialMatch.getOriginalRhs() == f3) {
				pmrF2ToF3 = potentialMatch;
			}

			if (pmrF1ToF2 != null && pmrF2ToF3 != null)
				break;
		}

		if (pmrF1ToF2 == null || pmrF2ToF3 == null) {
			fail("An edge no longer exists after we defined f3 as the master of f1.");
		}

		assertTrue(pmrF1ToF2.getMaster() == f2);
		assertTrue(pmrF1ToF2.getDuplicate() == f1);
		assertTrue(pmrF2ToF3.getMaster() == f3);
		assertTrue(pmrF2ToF3.getDuplicate() == f2);
	}

	/**
	 * Sets the master of a node to be one of it's current duplicates that also
	 * has another different master. This should not be a normal case.
	 * <p>
	 * See the image for
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MungeProcess)}
	 * for details on the graph.
	 */
	public void testSetMasterToCurrentDuplicate() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("f1");
		SourceTableRecord f1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("f3");
		SourceTableRecord f3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("f2");
		SourceTableRecord f2 = pool.getSourceTableRecord(keyList);

		pool.defineMaster(f2, f1);

		PotentialMatchRecord pmrF1ToF2 = null;
		PotentialMatchRecord pmrF2ToF3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == f1
					&& potentialMatch.getOriginalRhs() == f2) {
				pmrF1ToF2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == f2
					&& potentialMatch.getOriginalRhs() == f1) {
				pmrF1ToF2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == f3
					&& potentialMatch.getOriginalRhs() == f2) {
				pmrF2ToF3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == f2
					&& potentialMatch.getOriginalRhs() == f3) {
				pmrF2ToF3 = potentialMatch;
			}

			if (pmrF1ToF2 != null && pmrF2ToF3 != null)
				break;
		}

		if (pmrF1ToF2 == null || pmrF2ToF3 == null) {
			fail("An edge no longer exists after we defined f3 as the master of f1.");
		}

		assertTrue(pmrF1ToF2.getMaster() == f2);
		assertTrue(pmrF1ToF2.getDuplicate() == f1);
		assertTrue(pmrF2ToF3.getMaster() == f3);
		assertTrue(pmrF2ToF3.getDuplicate() == f2);
	}

	/**
	 * Sets the master of a node with no master, but masters exist in the graph.
	 * <p>
	 * See the image for
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MungeProcess)}
	 * for details on the graph.
	 */
	public void testSetMasterWithMaster() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("b2");
		SourceTableRecord b2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("b3");
		SourceTableRecord b3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("b1");
		SourceTableRecord b1 = pool.getSourceTableRecord(keyList);

		pool.defineMaster(b2, b3);

		PotentialMatchRecord pmrB1ToB2 = null;
		PotentialMatchRecord pmrB2ToB3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == b1
					&& potentialMatch.getOriginalRhs() == b2) {
				pmrB1ToB2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == b2
					&& potentialMatch.getOriginalRhs() == b1) {
				pmrB1ToB2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == b3
					&& potentialMatch.getOriginalRhs() == b2) {
				pmrB2ToB3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == b2
					&& potentialMatch.getOriginalRhs() == b3) {
				pmrB2ToB3 = potentialMatch;
			}

			if (pmrB1ToB2 != null && pmrB2ToB3 != null)
				break;
		}

		if (pmrB1ToB2 == null || pmrB2ToB3 == null) {
			fail("An edge no longer exists after we defined b2 as the master of b3.");
		}

		assertTrue(pmrB1ToB2.getMaster() == b1);
		assertTrue(pmrB1ToB2.getDuplicate() == b2);
		assertTrue(pmrB2ToB3.getMaster() == b2);
		assertTrue(pmrB2ToB3.getDuplicate() == b3);
	}

	/**
	 * Sets the master of a node where the master is a master of another node
	 * and the duplicate node is a duplicate of a different node.
	 * <p>
	 * See the image for
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MungeProcess)}
	 * for details on the graph.
	 */
	public void testSetMasterDupIsAMasterNewMasterHasMaster() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("g2");
		SourceTableRecord g2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("g3");
		SourceTableRecord g3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("g1");
		SourceTableRecord g1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("g4");
		SourceTableRecord g4 = pool.getSourceTableRecord(keyList);

		pool.defineMaster(g2, g3);

		PotentialMatchRecord pmrG1ToG2 = null;
		PotentialMatchRecord pmrG2ToG3 = null;
		PotentialMatchRecord pmrG3ToG4 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == g1
					&& potentialMatch.getOriginalRhs() == g2) {
				pmrG1ToG2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == g2
					&& potentialMatch.getOriginalRhs() == g1) {
				pmrG1ToG2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == g3
					&& potentialMatch.getOriginalRhs() == g2) {
				pmrG2ToG3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == g2
					&& potentialMatch.getOriginalRhs() == g3) {
				pmrG2ToG3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == g3
					&& potentialMatch.getOriginalRhs() == g4) {
				pmrG3ToG4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == g4
					&& potentialMatch.getOriginalRhs() == g3) {
				pmrG3ToG4 = potentialMatch;
			}

			if (pmrG1ToG2 != null && pmrG2ToG3 != null && pmrG3ToG4 != null)
				break;
		}

		if (pmrG1ToG2 == null || pmrG2ToG3 == null || pmrG3ToG4 == null) {
			fail("An edge no longer exists after we defined g2 as the master of g3.");
		}

		assertTrue(pmrG1ToG2.getMaster() == g2);
		assertTrue(pmrG1ToG2.getDuplicate() == g1);
		assertTrue(pmrG2ToG3.getMaster() == g2);
		assertTrue(pmrG2ToG3.getDuplicate() == g3);
		assertTrue(pmrG3ToG4.getMaster() == g3);
		assertTrue(pmrG3ToG4.getDuplicate() == g4);
	}

	/**
	 * Sets the master of a node to a node that is a duplicate of another node.
	 * The current duplicate being set to have a master is also defined as a
	 * duplicate of another node.
	 * <p>
	 * See the image for
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MungeProcess)}
	 * for details on the graph.
	 */
	public void testSetMasterMasterHasMasterDupHasMaster() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("h2");
		SourceTableRecord h2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("h3");
		SourceTableRecord h3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("h1");
		SourceTableRecord h1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("h4");
		SourceTableRecord h4 = pool.getSourceTableRecord(keyList);

		pool.defineMaster(h2, h3);

		PotentialMatchRecord pmrH1ToH2 = null;
		PotentialMatchRecord pmrH2ToH3 = null;
		PotentialMatchRecord pmrH3ToH4 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == h1
					&& potentialMatch.getOriginalRhs() == h2) {
				pmrH1ToH2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == h2
					&& potentialMatch.getOriginalRhs() == h1) {
				pmrH1ToH2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == h3
					&& potentialMatch.getOriginalRhs() == h2) {
				pmrH2ToH3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == h2
					&& potentialMatch.getOriginalRhs() == h3) {
				pmrH2ToH3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == h3
					&& potentialMatch.getOriginalRhs() == h4) {
				pmrH3ToH4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == h4
					&& potentialMatch.getOriginalRhs() == h3) {
				pmrH3ToH4 = potentialMatch;
			}

			if (pmrH1ToH2 != null && pmrH2ToH3 != null && pmrH3ToH4 != null)
				break;
		}

		if (pmrH1ToH2 == null || pmrH2ToH3 == null || pmrH3ToH4 == null) {
			fail("An edge no longer exists after we defined h2 as the master of h3.");
		}

		assertTrue(pmrH1ToH2.getMaster() == h1);
		assertTrue(pmrH1ToH2.getDuplicate() == h2);
		assertTrue(pmrH2ToH3.getMaster() == h2);
		assertTrue(pmrH2ToH3.getDuplicate() == h3);
		assertTrue(pmrH3ToH4.getMaster() == h3);
		assertTrue(pmrH3ToH4.getDuplicate() == h4);
	}

	/**
	 * Sets the master of a node to a node that is a duplicate of another node.
	 * The current duplicate being set to have a master is also defined as a
	 * duplicate of another node. The way the master is defined in this case
	 * should be the same way a duplicate button is implemented in the UI.
	 * <p>
	 * See the image for
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MungeProcess)}
	 * for details on the graph.
	 */
	public void testSetDuplicateMasterHasMasterDupHasMaster() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("h2");
		SourceTableRecord h2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("h3");
		SourceTableRecord h3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("h1");
		SourceTableRecord h1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("h4");
		SourceTableRecord h4 = pool.getSourceTableRecord(keyList);

		// Setting a duplicate is the same thing as setting a master but with
		// reversed parameters.
		pool.defineMaster(h3, h2);

		PotentialMatchRecord pmrH1ToH2 = null;
		PotentialMatchRecord pmrH2ToH3 = null;
		PotentialMatchRecord pmrH3ToH4 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == h1
					&& potentialMatch.getOriginalRhs() == h2) {
				pmrH1ToH2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == h2
					&& potentialMatch.getOriginalRhs() == h1) {
				pmrH1ToH2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == h3
					&& potentialMatch.getOriginalRhs() == h2) {
				pmrH2ToH3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == h2
					&& potentialMatch.getOriginalRhs() == h3) {
				pmrH2ToH3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == h3
					&& potentialMatch.getOriginalRhs() == h4) {
				pmrH3ToH4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == h4
					&& potentialMatch.getOriginalRhs() == h3) {
				pmrH3ToH4 = potentialMatch;
			}

			if (pmrH1ToH2 != null && pmrH2ToH3 != null && pmrH3ToH4 != null)
				break;
		}

		if (pmrH1ToH2 == null || pmrH2ToH3 == null || pmrH3ToH4 == null) {
			fail("An edge no longer exists after we defined h3 as the master of h2.");
		}

		assertTrue(pmrH1ToH2.getMaster() == h2);
		assertTrue(pmrH1ToH2.getDuplicate() == h1);
		assertTrue(pmrH2ToH3.getMaster() == h3);
		assertTrue(pmrH2ToH3.getDuplicate() == h2);
		assertTrue(pmrH3ToH4.getMaster() == h4);
		assertTrue(pmrH3ToH4.getDuplicate() == h3);
	}

	/**
	 * Sets the master of a node to a node that is not connected by
	 * master/duplicate edges.
	 * <p>
	 * See the image for
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MungeProcess)}
	 * for details on the graph.
	 */
	public void testSetDuplicate() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("g2");
		SourceTableRecord g2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("g3");
		SourceTableRecord g3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("g1");
		SourceTableRecord g1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("g4");
		SourceTableRecord g4 = pool.getSourceTableRecord(keyList);

		// Setting a duplicate is the same thing as setting a master but with
		// reversed parameters.
		pool.defineMaster(g1, g3);

		PotentialMatchRecord pmrG1ToG2 = null;
		PotentialMatchRecord pmrG2ToG3 = null;
		PotentialMatchRecord pmrG3ToG4 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == g1
					&& potentialMatch.getOriginalRhs() == g2) {
				pmrG1ToG2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == g2
					&& potentialMatch.getOriginalRhs() == g1) {
				pmrG1ToG2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == g3
					&& potentialMatch.getOriginalRhs() == g2) {
				pmrG2ToG3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == g2
					&& potentialMatch.getOriginalRhs() == g3) {
				pmrG2ToG3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == g3
					&& potentialMatch.getOriginalRhs() == g4) {
				pmrG3ToG4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == g4
					&& potentialMatch.getOriginalRhs() == g3) {
				pmrG3ToG4 = potentialMatch;
			}

			if (pmrG1ToG2 != null && pmrG2ToG3 != null && pmrG3ToG4 != null)
				break;
		}

		if (pmrG1ToG2 == null || pmrG2ToG3 == null || pmrG3ToG4 == null) {
			fail("An edge no longer exists after we defined g1 as the master of g3.");
		}

		assertTrue(pmrG1ToG2.getMaster() == g2);
		assertTrue(pmrG1ToG2.getDuplicate() == g1);
		assertTrue(pmrG2ToG3.getMaster() == g2);
		assertTrue(pmrG2ToG3.getDuplicate() == g3);
		assertTrue(pmrG3ToG4.getMaster() == g3);
		assertTrue(pmrG3ToG4.getDuplicate() == g4);
	}

	/**
	 * Sets the master of a node to a master when a cycle is involved. This is
	 * an unusual case and not something that we will normally come across. Any
	 * paths that are not used to identify a master/duplicate relation will be
	 * set to undefined. In this test we cannot know which node was selected as
	 * the ultimate master in case the algorithm was changed to select a better
	 * ultimate master.
	 * <p>
	 * See the image for
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MungeProcess)}
	 * for details on the graph.
	 */
	public void testSetMasterInACycle() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("cycle2");
		SourceTableRecord cycle2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("cycle3");
		SourceTableRecord cycle3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("cycle1");
		SourceTableRecord cycle1 = pool.getSourceTableRecord(keyList);

		pool.defineMaster(cycle3, cycle2);

		PotentialMatchRecord pmrCycle1ToCycle2 = null;
		PotentialMatchRecord pmrCycle2ToCycle3 = null;
		PotentialMatchRecord pmrCycle3ToCycle1 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == cycle1
					&& potentialMatch.getOriginalRhs() == cycle2) {
				pmrCycle1ToCycle2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle2
					&& potentialMatch.getOriginalRhs() == cycle1) {
				pmrCycle1ToCycle2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle3
					&& potentialMatch.getOriginalRhs() == cycle2) {
				pmrCycle2ToCycle3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle2
					&& potentialMatch.getOriginalRhs() == cycle3) {
				pmrCycle2ToCycle3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle3
					&& potentialMatch.getOriginalRhs() == cycle1) {
				pmrCycle3ToCycle1 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle1
					&& potentialMatch.getOriginalRhs() == cycle3) {
				pmrCycle3ToCycle1 = potentialMatch;
			}

			if (pmrCycle1ToCycle2 != null && pmrCycle2ToCycle3 != null
					&& pmrCycle3ToCycle1 != null)
				break;
		}

		if (pmrCycle1ToCycle2 == null || pmrCycle2ToCycle3 == null
				|| pmrCycle3ToCycle1 == null) {
			fail("An edge no longer exists after we defined cycle3 as the master of cycle2.");
		}

		if (pmrCycle1ToCycle2.getMaster() == cycle1) {
			assertTrue(pmrCycle1ToCycle2.getDuplicate() == cycle2);
			assertTrue(pmrCycle2ToCycle3.getMaster() == null);
			assertTrue(pmrCycle2ToCycle3.getDuplicate() == null);
			assertTrue(pmrCycle3ToCycle1.getMaster() == cycle1);
			assertTrue(pmrCycle3ToCycle1.getDuplicate() == cycle3);
		} else if (pmrCycle1ToCycle2.getMaster() == cycle2) {
			assertTrue(pmrCycle1ToCycle2.getDuplicate() == cycle1);
			assertTrue(pmrCycle2ToCycle3.getMaster() == cycle2);
			assertTrue(pmrCycle2ToCycle3.getDuplicate() == cycle3);
			assertTrue(pmrCycle3ToCycle1.getMaster() == null);
			assertTrue(pmrCycle3ToCycle1.getDuplicate() == null);
		} else {
			// master of this loop was set to cycle3
			assertTrue(pmrCycle1ToCycle2.getDuplicate() == null);
			assertTrue(pmrCycle2ToCycle3.getMaster() == cycle3);
			assertTrue(pmrCycle2ToCycle3.getDuplicate() == cycle2);
			assertTrue(pmrCycle3ToCycle1.getMaster() == cycle3);
			assertTrue(pmrCycle3ToCycle1.getDuplicate() == cycle1);
		}
	}

	/**
	 * Sets the master of a node to a master when a cycle is involved. This is
	 * an unusual case and not something that we will normally come across. Any
	 * paths that are not used to identify a master/duplicate relation will be
	 * set to undefined. In this test we cannot know which node was selected as
	 * the ultimate master in case the algorithm was changed to select a better
	 * ultimate master. This is the case for setting a master when the master is
	 * in a cycle.
	 * <p>
	 * See the image for
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MungeProcess)}
	 * for details on the graph.
	 */
	public void testSetMasterWithMasterInACycle() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("j1");
		SourceTableRecord j1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("j2");
		SourceTableRecord j2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("j3");
		SourceTableRecord j3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("j4");
		SourceTableRecord j4 = pool.getSourceTableRecord(keyList);

		pool.defineMaster(j2, j1);

		PotentialMatchRecord pmrJ1ToJ2 = null;
		PotentialMatchRecord pmrJ2ToJ3 = null;
		PotentialMatchRecord pmrJ3ToJ4 = null;
		PotentialMatchRecord pmrJ4ToJ2 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == j2
					&& potentialMatch.getOriginalRhs() == j1) {
				pmrJ1ToJ2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == j1
					&& potentialMatch.getOriginalRhs() == j2) {
				pmrJ1ToJ2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == j2
					&& potentialMatch.getOriginalRhs() == j3) {
				pmrJ2ToJ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == j3
					&& potentialMatch.getOriginalRhs() == j2) {
				pmrJ2ToJ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == j4
					&& potentialMatch.getOriginalRhs() == j3) {
				pmrJ3ToJ4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == j3
					&& potentialMatch.getOriginalRhs() == j4) {
				pmrJ3ToJ4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == j2
					&& potentialMatch.getOriginalRhs() == j4) {
				pmrJ4ToJ2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == j4
					&& potentialMatch.getOriginalRhs() == j2) {
				pmrJ4ToJ2 = potentialMatch;
			}

			if (pmrJ1ToJ2 != null && pmrJ2ToJ3 != null && pmrJ3ToJ4 != null
					&& pmrJ4ToJ2 != null)
				break;
		}

		if (pmrJ1ToJ2 == null || pmrJ2ToJ3 == null || pmrJ3ToJ4 == null
				|| pmrJ4ToJ2 == null) {
			fail("An edge no longer exists after we defined j2 to be the master of j1.");
		}

		assertTrue(pmrJ1ToJ2.getMaster() == j2);
		assertTrue(pmrJ1ToJ2.getDuplicate() == j1);
		if (pmrJ2ToJ3.getMaster() == j2) {
			assertTrue(pmrJ2ToJ3.getDuplicate() == j3);
			assertTrue(pmrJ3ToJ4.getMaster() == null);
			assertTrue(pmrJ3ToJ4.getDuplicate() == null);
			assertTrue(pmrJ4ToJ2.getMaster() == j2);
			assertTrue(pmrJ4ToJ2.getDuplicate() == j4);
		} else if (pmrJ2ToJ3.getMaster() == j3) {
			assertTrue(pmrJ2ToJ3.getDuplicate() == j2);
			assertTrue(pmrJ3ToJ4.getMaster() == j3);
			assertTrue(pmrJ3ToJ4.getDuplicate() == j4);
			assertTrue(pmrJ4ToJ2.getMaster() == null);
			assertTrue(pmrJ4ToJ2.getDuplicate() == null);
		} else {
			// master of this loop was set to j4
			assertTrue(pmrJ2ToJ3.getMaster() == null);
			assertTrue(pmrJ2ToJ3.getDuplicate() == null);
			assertTrue(pmrJ3ToJ4.getMaster() == j4);
			assertTrue(pmrJ3ToJ4.getDuplicate() == j3);
			assertTrue(pmrJ4ToJ2.getMaster() == j4);
			assertTrue(pmrJ4ToJ2.getDuplicate() == j2);
		}
	}

	/**
	 * Sets the master of a node to a master when a cycle is involved. This is
	 * an unusual case and not something that we will normally come across. Any
	 * paths that are not used to identify a master/duplicate relation will be
	 * set to undefined. In this test we cannot know which node was selected as
	 * the ultimate master in case the algorithm was changed to select a better
	 * ultimate master. This is the case for setting a master when the ultimate
	 * master is in a cycle.
	 * <p>
	 * See the image for
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MungeProcess)}
	 * for details on the graph.
	 */
	public void testSetMasterWithUltimateMasterInACycle() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("k1");
		SourceTableRecord k1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("k2");
		SourceTableRecord k2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("k3");
		SourceTableRecord k3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("k4");
		SourceTableRecord k4 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("k5");
		SourceTableRecord k5 = pool.getSourceTableRecord(keyList);

		pool.defineMaster(k2, k1);

		PotentialMatchRecord pmrK1ToK2 = null;
		PotentialMatchRecord pmrK2ToK3 = null;
		PotentialMatchRecord pmrK3ToK4 = null;
		PotentialMatchRecord pmrK4ToK5 = null;
		PotentialMatchRecord pmrK5ToK3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == k2
					&& potentialMatch.getOriginalRhs() == k1) {
				pmrK1ToK2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == k1
					&& potentialMatch.getOriginalRhs() == k2) {
				pmrK1ToK2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == k2
					&& potentialMatch.getOriginalRhs() == k3) {
				pmrK2ToK3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == k3
					&& potentialMatch.getOriginalRhs() == k2) {
				pmrK2ToK3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == k4
					&& potentialMatch.getOriginalRhs() == k3) {
				pmrK3ToK4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == k3
					&& potentialMatch.getOriginalRhs() == k4) {
				pmrK3ToK4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == k5
					&& potentialMatch.getOriginalRhs() == k4) {
				pmrK4ToK5 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == k4
					&& potentialMatch.getOriginalRhs() == k5) {
				pmrK4ToK5 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == k5
					&& potentialMatch.getOriginalRhs() == k3) {
				pmrK5ToK3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == k3
					&& potentialMatch.getOriginalRhs() == k5) {
				pmrK5ToK3 = potentialMatch;
			}

			if (pmrK1ToK2 != null && pmrK2ToK3 != null && pmrK3ToK4 != null
					&& pmrK4ToK5 != null && pmrK5ToK3 != null)
				break;
		}

		if (pmrK1ToK2 == null || pmrK2ToK3 == null || pmrK3ToK4 == null
				|| pmrK4ToK5 == null || pmrK5ToK3 == null) {
			fail("An edge no longer exists after we defined k2 to be the master of k1.");
		}

		assertTrue(pmrK1ToK2.getMaster() == k2);
		assertTrue(pmrK1ToK2.getDuplicate() == k1);
		assertTrue(pmrK2ToK3.getMaster() == k3);
		assertTrue(pmrK2ToK3.getDuplicate() == k2);
		if (pmrK3ToK4.getMaster() == k3) {
			assertTrue(pmrK3ToK4.getDuplicate() == k4);
			assertTrue(pmrK4ToK5.getMaster() == null);
			assertTrue(pmrK4ToK5.getDuplicate() == null);
			assertTrue(pmrK5ToK3.getMaster() == k3);
			assertTrue(pmrK5ToK3.getDuplicate() == k5);
		} else if (pmrK3ToK4.getMaster() == k4) {
			assertTrue(pmrK3ToK4.getDuplicate() == k3);
			assertTrue(pmrK4ToK5.getMaster() == k4);
			assertTrue(pmrK4ToK5.getDuplicate() == k5);
			assertTrue(pmrK5ToK3.getMaster() == null);
			assertTrue(pmrK5ToK3.getDuplicate() == null);
		} else {
			// master of this loop was set to k5
			assertTrue(pmrK3ToK4.getMaster() == null);
			assertTrue(pmrK3ToK4.getDuplicate() == null);
			assertTrue(pmrK4ToK5.getMaster() == k5);
			assertTrue(pmrK4ToK5.getDuplicate() == k4);
			assertTrue(pmrK5ToK3.getMaster() == k5);
			assertTrue(pmrK5ToK3.getDuplicate() == k3);
		}
	}

	/**
	 * Sets the master of a node to a master that has two master. When this
	 * matching has been completed each node should only have one master.
	 * <p>
	 * See the image for
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MungeProcess)}
	 * for details on the graph.
	 */
	public void testSetMasterWhereMasterHasTwoMasters() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("l1");
		SourceTableRecord l1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("l2");
		SourceTableRecord l2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("l3");
		SourceTableRecord l3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("l4");
		SourceTableRecord l4 = pool.getSourceTableRecord(keyList);

		pool.defineMaster(l2, l1);

		PotentialMatchRecord pmrL1ToL2 = null;
		PotentialMatchRecord pmrL2ToL3 = null;
		PotentialMatchRecord pmrL2ToL4 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == l2
					&& potentialMatch.getOriginalRhs() == l1) {
				pmrL1ToL2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == l1
					&& potentialMatch.getOriginalRhs() == l2) {
				pmrL1ToL2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == l2
					&& potentialMatch.getOriginalRhs() == l3) {
				pmrL2ToL3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == l3
					&& potentialMatch.getOriginalRhs() == l2) {
				pmrL2ToL3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == l4
					&& potentialMatch.getOriginalRhs() == l2) {
				pmrL2ToL4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == l2
					&& potentialMatch.getOriginalRhs() == l4) {
				pmrL2ToL4 = potentialMatch;
			}

			if (pmrL1ToL2 != null && pmrL2ToL3 != null && pmrL2ToL4 != null)
				break;
		}

		if (pmrL1ToL2 == null || pmrL2ToL3 == null || pmrL2ToL4 == null) {
			fail("An edge no longer exists after we defined l2 to be the master of l1.");
		}

		assertTrue(pmrL1ToL2.getMaster() == l2);
		assertTrue(pmrL1ToL2.getDuplicate() == l1);
		if (pmrL2ToL3.getMaster() == l3) {
			assertTrue(pmrL2ToL3.getDuplicate() == l2);
			assertTrue(pmrL2ToL4.getMaster() == l2);
			assertTrue(pmrL2ToL4.getDuplicate() == l4);
		} else {
			// master was set to l4
			assertTrue(pmrL2ToL3.getMaster() == l2);
			assertTrue(pmrL2ToL3.getDuplicate() == l3);
			assertTrue(pmrL2ToL4.getMaster() == l4);
			assertTrue(pmrL2ToL4.getDuplicate() == l2);
		}
	}

	/**
	 * Sets the master of a node to a master when a cycle is involved. This is
	 * an unusual case and not something that we will normally come across. Any
	 * paths that are not used to identify a master/duplicate relation will be
	 * set to undefined. In this test we cannot know which node was selected as
	 * the ultimate master in case the algorithm was changed to select a better
	 * ultimate master. This is the case for setting a master when the ultimate
	 * master is on the other side of a cycle.
	 * <p>
	 * See the image for
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MungeProcess)}
	 * for details on the graph.
	 */
	public void testSetMasterWithUltimateMasterOutsideCycle() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("m1");
		SourceTableRecord m1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("m2");
		SourceTableRecord m2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("m3");
		SourceTableRecord m3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("m4");
		SourceTableRecord m4 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("m5");
		SourceTableRecord m5 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("m6");
		SourceTableRecord m6 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("m7");
		SourceTableRecord m7 = pool.getSourceTableRecord(keyList);

		pool.defineMaster(m2, m1);

		PotentialMatchRecord pmrM1ToM2 = null;
		PotentialMatchRecord pmrM2ToM3 = null;
		PotentialMatchRecord pmrM3ToM5 = null;
		PotentialMatchRecord pmrM5ToM6 = null;
		PotentialMatchRecord pmrM6ToM7 = null;
		PotentialMatchRecord pmrM6ToM4 = null;
		PotentialMatchRecord pmrM4ToM3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == m2
					&& potentialMatch.getOriginalRhs() == m1) {
				pmrM1ToM2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == m1
					&& potentialMatch.getOriginalRhs() == m2) {
				pmrM1ToM2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == m2
					&& potentialMatch.getOriginalRhs() == m3) {
				pmrM2ToM3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == m3
					&& potentialMatch.getOriginalRhs() == m2) {
				pmrM2ToM3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == m5
					&& potentialMatch.getOriginalRhs() == m3) {
				pmrM3ToM5 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == m3
					&& potentialMatch.getOriginalRhs() == m5) {
				pmrM3ToM5 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == m5
					&& potentialMatch.getOriginalRhs() == m6) {
				pmrM5ToM6 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == m6
					&& potentialMatch.getOriginalRhs() == m5) {
				pmrM5ToM6 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == m6
					&& potentialMatch.getOriginalRhs() == m7) {
				pmrM6ToM7 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == m7
					&& potentialMatch.getOriginalRhs() == m6) {
				pmrM6ToM7 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == m6
					&& potentialMatch.getOriginalRhs() == m4) {
				pmrM6ToM4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == m4
					&& potentialMatch.getOriginalRhs() == m6) {
				pmrM6ToM4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == m4
					&& potentialMatch.getOriginalRhs() == m3) {
				pmrM4ToM3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == m3
					&& potentialMatch.getOriginalRhs() == m4) {
				pmrM4ToM3 = potentialMatch;
			}

			if (pmrM1ToM2 != null && pmrM2ToM3 != null && pmrM3ToM5 != null
					&& pmrM5ToM6 != null && pmrM6ToM7 != null
					&& pmrM6ToM4 != null && pmrM4ToM3 != null)
				break;
		}

		if (pmrM1ToM2 == null || pmrM2ToM3 == null || pmrM3ToM5 == null
				|| pmrM5ToM6 == null || pmrM6ToM7 == null || pmrM6ToM4 == null
				|| pmrM4ToM3 == null) {
			fail("An edge no longer exists after we defined m2 to be the master of m1.");
		}

		assertTrue(pmrM1ToM2.getMaster() == m2);
		assertTrue(pmrM1ToM2.getDuplicate() == m1);
		assertTrue(pmrM2ToM3.getMaster() == m3);
		assertTrue(pmrM2ToM3.getDuplicate() == m2);
		if (pmrM3ToM5.getMaster() == m3 && pmrM4ToM3.getMaster() == m3) {
			assertTrue(pmrM6ToM7.getMaster() == m6);
			if (pmrM5ToM6.getMaster() == m5) {
				assertTrue(pmrM6ToM4.getMaster() == null);
			} else {
				assertTrue(pmrM6ToM4.getMaster() == m4);
				assertTrue(pmrM5ToM6.getMaster() == m5);
			}
		} else if (pmrM4ToM3.getMaster() == m4 && pmrM6ToM4.getMaster() == m4) {
			assertTrue(pmrM6ToM7.getMaster() == m6);
			if (pmrM3ToM5.getMaster() == m3) {
				assertTrue(pmrM5ToM6.getMaster() == null);
			} else {
				assertTrue(pmrM5ToM6.getMaster() == m6);
				assertTrue(pmrM3ToM5.getMaster() == null);
			}

		} else if (pmrM5ToM6.getMaster() == m6 && pmrM6ToM4.getMaster() == m6
				&& pmrM6ToM7.getMaster() == m6) {
			if (pmrM3ToM5.getMaster() == m5) {
				assertTrue(pmrM4ToM3.getMaster() == null);
			} else {
				assertTrue(pmrM4ToM3.getMaster() == m4);
				assertTrue(pmrM3ToM5.getMaster() == null);
			}
		} else if (pmrM3ToM5.getMaster() == m5 && pmrM5ToM6.getMaster() == m5) {
			assertTrue(pmrM6ToM7.getMaster() == m6);
			if (pmrM4ToM3.getMaster() == m3) {
				assertTrue(pmrM6ToM4.getMaster() == null);
			} else {
				assertTrue(pmrM6ToM4.getMaster() == m6);
				assertTrue(pmrM4ToM3.getMaster() == null);
			}
		} else if (pmrM6ToM7.getMaster() == m7) {
			assertTrue(pmrM5ToM6.getMaster() == m6);
			assertTrue(pmrM6ToM4.getMaster() == m6);
			if (pmrM3ToM5.getMaster() == m5) {
				assertTrue(pmrM4ToM3.getMaster() == null);
			} else {
				assertTrue(pmrM4ToM3.getMaster() == m4);
				assertTrue(pmrM3ToM5.getMaster() == null);
			}
		} else {
			fail("We don't know what happened to the master, but we know it's wrong!");
		}
	}

	/**
	 * Sets the master of a node to a master that will create a cycle in the
	 * graph. The end result should have no cycles.
	 * <p>
	 * See the image for
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MungeProcess)}
	 * for details on the graph.
	 */
	public void testSetMasterToCreateACycle() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("i2");
		SourceTableRecord i2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("i3");
		SourceTableRecord i3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("i1");
		SourceTableRecord i1 = pool.getSourceTableRecord(keyList);

		pool.defineMaster(i1, i3);

		PotentialMatchRecord pmrI1ToI2 = null;
		PotentialMatchRecord pmrI2ToI3 = null;
		PotentialMatchRecord pmrI3ToI1 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == i1
					&& potentialMatch.getOriginalRhs() == i2) {
				pmrI1ToI2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == i2
					&& potentialMatch.getOriginalRhs() == i1) {
				pmrI1ToI2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == i3
					&& potentialMatch.getOriginalRhs() == i2) {
				pmrI2ToI3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == i2
					&& potentialMatch.getOriginalRhs() == i3) {
				pmrI2ToI3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == i3
					&& potentialMatch.getOriginalRhs() == i1) {
				pmrI3ToI1 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == i1
					&& potentialMatch.getOriginalRhs() == i3) {
				pmrI3ToI1 = potentialMatch;
			}

			if (pmrI1ToI2 != null && pmrI2ToI3 != null && pmrI3ToI1 != null)
				break;
		}

		if (pmrI1ToI2 == null || pmrI2ToI3 == null || pmrI3ToI1 == null) {
			fail("An edge no longer exists after we defined i1 as the master of i3.");
		}

		assertTrue(pmrI3ToI1.getMaster() == i1);
		assertTrue(pmrI3ToI1.getDuplicate() == i3);
		if (pmrI1ToI2.getMaster() == i1) {
			assertTrue(pmrI1ToI2.getDuplicate() == i2);
			assertTrue(pmrI2ToI3.getMaster() == null);
			assertTrue(pmrI2ToI3.getDuplicate() == null);
			assertTrue(pmrI2ToI3.getMatchStatus() == MatchType.UNMATCH);
		} else {
			// pmrI2ToI3's master is i3
			assertTrue(pmrI2ToI3.getMaster() == i2);
			assertTrue(pmrI2ToI3.getDuplicate() == i3);
			assertTrue(pmrI1ToI2.getMaster() == null);
			assertTrue(pmrI1ToI2.getDuplicate() == null);
			assertTrue(pmrI1ToI2.getMatchStatus() == MatchType.UNMATCH);
		}
	}

	/**
	 * Sets the master of a node in a graph when we need to make a synthetic
	 * edge
	 * <p>
	 * See the image for
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MungeProcess)}
	 * for details on the graph.
	 */
	public void testSetMasterCreatingASyntheticEdge() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("a2");
		SourceTableRecord a2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("a1");
		SourceTableRecord a1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("a3");
		SourceTableRecord a3 = pool.getSourceTableRecord(keyList);

		pool.defineMaster(a3, a1);

		PotentialMatchRecord pmrA1ToA2 = null;
		PotentialMatchRecord pmrA2ToA3 = null;
		PotentialMatchRecord pmrA1ToA3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == a1
					&& potentialMatch.getOriginalRhs() == a2) {
				pmrA1ToA2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a2
					&& potentialMatch.getOriginalRhs() == a1) {
				pmrA1ToA2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a3
					&& potentialMatch.getOriginalRhs() == a2) {
				pmrA2ToA3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a2
					&& potentialMatch.getOriginalRhs() == a3) {
				pmrA2ToA3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a3
					&& potentialMatch.getOriginalRhs() == a1) {
				pmrA1ToA3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a1
					&& potentialMatch.getOriginalRhs() == a3) {
				pmrA1ToA3 = potentialMatch;
			}

			if (pmrA1ToA2 != null && pmrA2ToA3 != null && pmrA1ToA3 != null)
				break;
		}

		if (pmrA1ToA2 == null || pmrA2ToA3 == null || pmrA1ToA3 == null) {
			fail("An edge no longer exists after we defined a3 as the master of a1.");
		}

		assertTrue(pmrA1ToA2.getMaster() == null);
		assertTrue(pmrA1ToA2.getDuplicate() == null);
		assertTrue(pmrA2ToA3.getMaster() == null);
		assertTrue(pmrA1ToA3.getMaster() == a3);
		assertTrue(pmrA1ToA3.getDuplicate() == a1);
		assertTrue(pmrA1ToA3.isSynthetic());
	}

	/**
	 * This test removes a defined connection between two nodes.
	 */
	public void testDefiningNoMatchFromMatched() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("b2");
		SourceTableRecord b2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("b3");
		SourceTableRecord b3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("b1");
		SourceTableRecord b1 = pool.getSourceTableRecord(keyList);

		pool.defineNoMatch(b2, b1);

		PotentialMatchRecord pmrB1ToB2 = null;
		PotentialMatchRecord pmrB2ToB3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == b1
					&& potentialMatch.getOriginalRhs() == b2) {
				pmrB1ToB2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == b2
					&& potentialMatch.getOriginalRhs() == b1) {
				pmrB1ToB2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == b3
					&& potentialMatch.getOriginalRhs() == b2) {
				pmrB2ToB3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == b2
					&& potentialMatch.getOriginalRhs() == b3) {
				pmrB2ToB3 = potentialMatch;
			}

			if (pmrB1ToB2 != null && pmrB2ToB3 != null)
				break;
		}

		if (pmrB1ToB2 == null || pmrB2ToB3 == null) {
			fail("An edge no longer exists after we defined no match between b2 and b1.");
		}

		assertTrue(pmrB1ToB2.getMaster() == null);
		assertTrue(pmrB1ToB2.getDuplicate() == null);
		assertTrue(pmrB2ToB3.getMaster() == null);
		assertTrue(pmrB2ToB3.getDuplicate() == null);
		assertTrue(pmrB1ToB2.getMatchStatus() == MatchType.NOMATCH);
	}

	/**
	 * Testing the defineNoMatchToAny method. The method should remove both the
	 * match defining the source table record as a master and the match defining
	 * it as a duplicate.
	 */
	public void testDefineNoMatchOfAny() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("cycle2");
		SourceTableRecord cycle2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("cycle3");
		SourceTableRecord cycle3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("cycle1");
		SourceTableRecord cycle1 = pool.getSourceTableRecord(keyList);

		pool.defineNoMatchOfAny(cycle2);

		PotentialMatchRecord pmrCycle1ToCycle2 = null;
		PotentialMatchRecord pmrCycle2ToCycle3 = null;
		PotentialMatchRecord pmrCycle3ToCycle1 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == cycle1
					&& potentialMatch.getOriginalRhs() == cycle2) {
				pmrCycle1ToCycle2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle2
					&& potentialMatch.getOriginalRhs() == cycle1) {
				pmrCycle1ToCycle2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle3
					&& potentialMatch.getOriginalRhs() == cycle2) {
				pmrCycle2ToCycle3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle2
					&& potentialMatch.getOriginalRhs() == cycle3) {
				pmrCycle2ToCycle3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle3
					&& potentialMatch.getOriginalRhs() == cycle1) {
				pmrCycle3ToCycle1 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle1
					&& potentialMatch.getOriginalRhs() == cycle3) {
				pmrCycle3ToCycle1 = potentialMatch;
			}

			if (pmrCycle1ToCycle2 != null && pmrCycle2ToCycle3 != null
					&& pmrCycle3ToCycle1 != null)
				break;
		}

		if (pmrCycle1ToCycle2 == null || pmrCycle2ToCycle3 == null
				|| pmrCycle3ToCycle1 == null) {
			fail("An edge no longer exists after we defined cycle2 to not match any other connected nodes.");
		}

		assertTrue(pmrCycle1ToCycle2.getMaster() == null);
		assertTrue(pmrCycle1ToCycle2.getDuplicate() == null);
		assertTrue(pmrCycle1ToCycle2.getMatchStatus() == MatchType.NOMATCH);
		assertTrue(pmrCycle2ToCycle3.getMaster() == null);
		assertTrue(pmrCycle2ToCycle3.getDuplicate() == null);
		assertTrue(pmrCycle2ToCycle3.getMatchStatus() == MatchType.NOMATCH);
		assertTrue(pmrCycle3ToCycle1.getMaster() == cycle1);
		assertTrue(pmrCycle3ToCycle1.getDuplicate() == cycle3);
	}

	/**
	 * Sets the potential match record between two source table records that was
	 * unmatched to be no match.
	 */
	public void testDefiningNoMatchFromUnmatched() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("b2");
		SourceTableRecord b2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("b3");
		SourceTableRecord b3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("b1");
		SourceTableRecord b1 = pool.getSourceTableRecord(keyList);

		pool.defineNoMatch(b2, b3);

		PotentialMatchRecord pmrB1ToB2 = null;
		PotentialMatchRecord pmrB2ToB3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == b1
					&& potentialMatch.getOriginalRhs() == b2) {
				pmrB1ToB2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == b2
					&& potentialMatch.getOriginalRhs() == b1) {
				pmrB1ToB2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == b3
					&& potentialMatch.getOriginalRhs() == b2) {
				pmrB2ToB3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == b2
					&& potentialMatch.getOriginalRhs() == b3) {
				pmrB2ToB3 = potentialMatch;
			}

			if (pmrB1ToB2 != null && pmrB2ToB3 != null)
				break;
		}

		if (pmrB1ToB2 == null || pmrB2ToB3 == null) {
			fail("An edge no longer exists after we defined no match between b2 and b3.");
		}

		assertTrue(pmrB1ToB2.getMaster() == b1);
		assertTrue(pmrB1ToB2.getDuplicate() == b2);
		assertTrue(pmrB2ToB3.getMaster() == null);
		assertTrue(pmrB2ToB3.getDuplicate() == null);
		assertTrue(pmrB2ToB3.getMatchStatus() == MatchType.NOMATCH);
	}

	/**
	 * This test defines two nodes that were not directly connected before to be
	 * labeled as having no match between them.
	 */
	public void testDefiningNoMatchCreatingSynthetic() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("a2");
		SourceTableRecord a2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("a1");
		SourceTableRecord a1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("a3");
		SourceTableRecord a3 = pool.getSourceTableRecord(keyList);

		pool.defineNoMatch(a3, a1);

		PotentialMatchRecord pmrA1ToA2 = null;
		PotentialMatchRecord pmrA2ToA3 = null;
		PotentialMatchRecord pmrA1ToA3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == a1
					&& potentialMatch.getOriginalRhs() == a2) {
				pmrA1ToA2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a2
					&& potentialMatch.getOriginalRhs() == a1) {
				pmrA1ToA2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a3
					&& potentialMatch.getOriginalRhs() == a2) {
				pmrA2ToA3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a2
					&& potentialMatch.getOriginalRhs() == a3) {
				pmrA2ToA3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a3
					&& potentialMatch.getOriginalRhs() == a1) {
				pmrA1ToA3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a1
					&& potentialMatch.getOriginalRhs() == a3) {
				pmrA1ToA3 = potentialMatch;
			}

			if (pmrA1ToA2 != null && pmrA2ToA3 != null && pmrA1ToA3 != null)
				break;
		}

		if (pmrA1ToA2 == null || pmrA2ToA3 == null || pmrA1ToA3 == null) {
			fail("An edge no longer exists after we defined no match between a1 and a3.");
		}

		assertTrue(pmrA1ToA2.getMaster() == null);
		assertTrue(pmrA1ToA2.getDuplicate() == null);
		assertTrue(pmrA2ToA3.getMaster() == null);
		assertTrue(pmrA1ToA3.getMaster() == null);
		assertTrue(pmrA1ToA3.getDuplicate() == null);
		assertTrue(pmrA1ToA3.getMatchStatus() == MatchType.NOMATCH);
		assertTrue(pmrA1ToA3.isSynthetic());
	}

	/**
	 * Testing the defineUnmatchAll method. The method should remove both the
	 * match defining the source table record as a master and the match defining
	 * it as a duplicate and set the edges to be unmatched.
	 */
	public void testDefineUnmatchAll() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("cycle2");
		SourceTableRecord cycle2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("cycle3");
		SourceTableRecord cycle3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("cycle1");
		SourceTableRecord cycle1 = pool.getSourceTableRecord(keyList);

		pool.defineUnmatchAll(cycle2);

		PotentialMatchRecord pmrCycle1ToCycle2 = null;
		PotentialMatchRecord pmrCycle2ToCycle3 = null;
		PotentialMatchRecord pmrCycle3ToCycle1 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == cycle1
					&& potentialMatch.getOriginalRhs() == cycle2) {
				pmrCycle1ToCycle2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle2
					&& potentialMatch.getOriginalRhs() == cycle1) {
				pmrCycle1ToCycle2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle3
					&& potentialMatch.getOriginalRhs() == cycle2) {
				pmrCycle2ToCycle3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle2
					&& potentialMatch.getOriginalRhs() == cycle3) {
				pmrCycle2ToCycle3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle3
					&& potentialMatch.getOriginalRhs() == cycle1) {
				pmrCycle3ToCycle1 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle1
					&& potentialMatch.getOriginalRhs() == cycle3) {
				pmrCycle3ToCycle1 = potentialMatch;
			}

			if (pmrCycle1ToCycle2 != null && pmrCycle2ToCycle3 != null
					&& pmrCycle3ToCycle1 != null)
				break;
		}

		if (pmrCycle1ToCycle2 == null || pmrCycle2ToCycle3 == null
				|| pmrCycle3ToCycle1 == null) {
			fail("An edge no longer exists after we unmatched cycle2 from cycle1 and cycle3.");
		}

		assertTrue(pmrCycle1ToCycle2.getMaster() == null);
		assertTrue(pmrCycle1ToCycle2.getDuplicate() == null);
		assertTrue(pmrCycle1ToCycle2.getMatchStatus() == MatchType.UNMATCH);
		assertTrue(pmrCycle2ToCycle3.getMaster() == null);
		assertTrue(pmrCycle2ToCycle3.getDuplicate() == null);
		assertTrue(pmrCycle2ToCycle3.getMatchStatus() == MatchType.UNMATCH);
		if (pmrCycle3ToCycle1.getMaster() == cycle1) {
			assertTrue(pmrCycle3ToCycle1.getDuplicate() == cycle3);
		} else {
			assertTrue(pmrCycle3ToCycle1.getMaster() == cycle3);
			assertTrue(pmrCycle3ToCycle1.getDuplicate() == cycle1);
		}
	}

	/**
	 * Sets the potential match record between two source table records that was
	 * matched to be unmatched.
	 */
	public void testDefiningUnmatchedForMatched() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("b2");
		SourceTableRecord b2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("b3");
		SourceTableRecord b3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("b1");
		SourceTableRecord b1 = pool.getSourceTableRecord(keyList);

		pool.defineUnmatched(b2, b1);

		PotentialMatchRecord pmrB1ToB2 = null;
		PotentialMatchRecord pmrB2ToB3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == b1
					&& potentialMatch.getOriginalRhs() == b2) {
				pmrB1ToB2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == b2
					&& potentialMatch.getOriginalRhs() == b1) {
				pmrB1ToB2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == b3
					&& potentialMatch.getOriginalRhs() == b2) {
				pmrB2ToB3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == b2
					&& potentialMatch.getOriginalRhs() == b3) {
				pmrB2ToB3 = potentialMatch;
			}

			if (pmrB1ToB2 != null && pmrB2ToB3 != null)
				break;
		}

		if (pmrB1ToB2 == null || pmrB2ToB3 == null) {
			fail("An edge no longer exists after we unmatched b1 and b2.");
		}

		assertTrue(pmrB1ToB2.getMaster() == null);
		assertTrue(pmrB1ToB2.getDuplicate() == null);
		assertTrue(pmrB2ToB3.getMaster() == null);
		assertTrue(pmrB2ToB3.getDuplicate() == null);
		assertTrue(pmrB1ToB2.getMatchStatus() == MatchType.UNMATCH);
	}

	/**
	 * This test sets a potential match record that was defined as a match to be
	 * a no match.
	 */
	public void testNoMatchToMaster() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("e2");
		SourceTableRecord e2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("e3");
		SourceTableRecord e3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("e1");
		SourceTableRecord e1 = pool.getSourceTableRecord(keyList);

		pool.defineNoMatch(e2, e1);

		PotentialMatchRecord pmrE1ToE2 = null;
		PotentialMatchRecord pmrE2ToE3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == e1
					&& potentialMatch.getOriginalRhs() == e2) {
				pmrE1ToE2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == e2
					&& potentialMatch.getOriginalRhs() == e1) {
				pmrE1ToE2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == e3
					&& potentialMatch.getOriginalRhs() == e2) {
				pmrE2ToE3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == e2
					&& potentialMatch.getOriginalRhs() == e3) {
				pmrE2ToE3 = potentialMatch;
			}

			if (pmrE1ToE2 != null && pmrE2ToE3 != null)
				break;
		}

		if (pmrE1ToE2 == null || pmrE2ToE3 == null) {
			fail("An edge no longer exists after we defined no match between e1 and e2.");
		}

		assertTrue(pmrE1ToE2.getMaster() == null);
		assertTrue(pmrE1ToE2.getDuplicate() == null);
		assertTrue(pmrE2ToE3.getMaster() == e2);
		assertTrue(pmrE2ToE3.getDuplicate() == e3);
		assertTrue(pmrE1ToE2.getMatchStatus() == MatchType.NOMATCH);
	}

	/**
	 * This test sets a potential match record that was defined as a no match to
	 * be a match.
	 */
	public void testMatchReplacingNoMatch() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("o2");
		SourceTableRecord o2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("o3");
		SourceTableRecord o3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("o1");
		SourceTableRecord o1 = pool.getSourceTableRecord(keyList);

		pool.defineMaster(o2, o1);

		PotentialMatchRecord pmrO1ToO3 = null;
		PotentialMatchRecord pmrO2ToO3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == o1
					&& potentialMatch.getOriginalRhs() == o3) {
				pmrO1ToO3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == o3
					&& potentialMatch.getOriginalRhs() == o1) {
				pmrO1ToO3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == o3
					&& potentialMatch.getOriginalRhs() == o2) {
				pmrO2ToO3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == o2
					&& potentialMatch.getOriginalRhs() == o3) {
				pmrO2ToO3 = potentialMatch;
			}

			if (pmrO1ToO3 != null && pmrO2ToO3 != null)
				break;
		}

		if (pmrO1ToO3 == null || pmrO2ToO3 == null) {
			fail("An edge no longer exists after we defined a match between o1 and o2.");
		}

		assertTrue(pmrO1ToO3.getMaster() == o3);
		assertTrue(pmrO1ToO3.getDuplicate() == o1);
		assertTrue(pmrO2ToO3.getMaster() == o3);
		assertTrue(pmrO2ToO3.getDuplicate() == o2);
		assertTrue(pmrO1ToO3.isMatch());
	}

	/**
	 * This test sets a potential match record that was defined as a no match to
	 * be a match. This test is similar to the previous test of replacing a no
	 * match with a match but it is more complex.
	 */
	public void testMatchReplacingNoMatchComplex() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("p2");
		SourceTableRecord p2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("p3");
		SourceTableRecord p3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("p1");
		SourceTableRecord p1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("p4");
		SourceTableRecord p4 = pool.getSourceTableRecord(keyList);

		pool.defineMaster(p4, p1);

		PotentialMatchRecord pmrP1ToP2 = null;
		PotentialMatchRecord pmrP1ToP3 = null;
		PotentialMatchRecord pmrP1ToP4 = null;
		PotentialMatchRecord pmrP2ToP3 = null;
		PotentialMatchRecord pmrP4ToP3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == p1
					&& potentialMatch.getOriginalRhs() == p3) {
				pmrP1ToP3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p3
					&& potentialMatch.getOriginalRhs() == p1) {
				pmrP1ToP3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p3
					&& potentialMatch.getOriginalRhs() == p2) {
				pmrP2ToP3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p2
					&& potentialMatch.getOriginalRhs() == p3) {
				pmrP2ToP3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p1
					&& potentialMatch.getOriginalRhs() == p2) {
				pmrP1ToP2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p2
					&& potentialMatch.getOriginalRhs() == p1) {
				pmrP1ToP2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p1
					&& potentialMatch.getOriginalRhs() == p4) {
				pmrP1ToP4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p4
					&& potentialMatch.getOriginalRhs() == p1) {
				pmrP1ToP4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p3
					&& potentialMatch.getOriginalRhs() == p4) {
				pmrP4ToP3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p4
					&& potentialMatch.getOriginalRhs() == p3) {
				pmrP4ToP3 = potentialMatch;
			}

			if (pmrP1ToP3 != null && pmrP2ToP3 != null && pmrP1ToP2 != null
					&& pmrP1ToP4 != null && pmrP4ToP3 != null)
				break;
		}

		if (pmrP1ToP3 == null || pmrP2ToP3 == null || pmrP1ToP2 == null
				|| pmrP1ToP4 == null || pmrP4ToP3 == null) {
			fail("An edge no longer exists after we defined a match between p1 and p4.");
		}

		// Not all branches of this test have been exercised. If this starts
		// failing, think hard
		// about whether the test makes sense.
		if (pmrP2ToP3.getMaster() == p3) {
			assertTrue(pmrP2ToP3.getDuplicate() == p2);
			if (pmrP4ToP3.getMaster() == p3) {
				assertTrue(pmrP4ToP3.getDuplicate() == p4);
				if (pmrP1ToP2.getMaster() == p2) {
					assertTrue(pmrP1ToP2.getDuplicate() == p1);
					assertTrue(pmrP1ToP3.getMaster() == null);
					assertTrue(pmrP1ToP3.getDuplicate() == null);
					assertTrue(pmrP1ToP3.getMatchStatus() == MatchType.UNMATCH);
					assertTrue(pmrP1ToP4.getMaster() == null);
					assertTrue(pmrP1ToP4.getDuplicate() == null);
					assertTrue(pmrP1ToP4.getMatchStatus() == MatchType.UNMATCH);
				} else if (pmrP1ToP3.getMaster() == p3) {
					assertTrue(pmrP1ToP3.getDuplicate() == p1);
					assertTrue(pmrP1ToP2.getMaster() == null);
					assertTrue(pmrP1ToP2.getDuplicate() == null);
					assertTrue(pmrP1ToP2.getMatchStatus() == MatchType.UNMATCH);
					assertTrue(pmrP1ToP4.getMaster() == null);
					assertTrue(pmrP1ToP4.getDuplicate() == null);
					assertTrue(pmrP1ToP4.getMatchStatus() == MatchType.UNMATCH);
				} else {
					// pmrP1ToP4's master must be p4
					assertTrue(pmrP1ToP4.getMaster() == p4);
					assertTrue(pmrP1ToP4.getDuplicate() == p1);
					assertTrue(pmrP1ToP3.getMaster() == null);
					assertTrue(pmrP1ToP3.getDuplicate() == null);
					assertTrue(pmrP1ToP3.getMatchStatus() == MatchType.UNMATCH);
					assertTrue(pmrP1ToP2.getMaster() == null);
					assertTrue(pmrP1ToP2.getDuplicate() == null);
					assertTrue(pmrP1ToP2.getMatchStatus() == MatchType.UNMATCH);
				}
			} else {
				// pmrP1ToP4's master must be p1
				assertTrue(pmrP1ToP4.getMaster() == p1);
				assertTrue(pmrP1ToP4.getDuplicate() == p4);
				assertTrue(pmrP4ToP3.getMaster() == null);
				assertTrue(pmrP4ToP3.getDuplicate() == null);
				assertTrue(pmrP4ToP3.getMatchStatus() == MatchType.UNMATCH);
				if (pmrP1ToP2.getMaster() == p2) {
					assertTrue(pmrP1ToP2.getDuplicate() == p1);
					assertTrue(pmrP1ToP3.getMaster() == null);
					assertTrue(pmrP1ToP3.getDuplicate() == null);
					assertTrue(pmrP1ToP3.getMatchStatus() == MatchType.UNMATCH);
				} else {
					// pmrP1ToP3's master must be p3
					assertTrue(pmrP1ToP3.getMaster() == p3);
					assertTrue(pmrP1ToP3.getDuplicate() == p1);
					assertTrue(pmrP1ToP2.getMaster() == null);
					assertTrue(pmrP1ToP2.getDuplicate() == null);
					assertTrue(pmrP1ToP2.getMatchStatus() == MatchType.UNMATCH);
				}
			}
		} else {
			// pmrP1ToP2's master must be p1
			assertTrue(pmrP1ToP2.getMaster() == p1);
			assertTrue(pmrP1ToP2.getDuplicate() == p2);
			assertTrue(pmrP2ToP3.getMaster() == null);
			assertTrue(pmrP2ToP3.getDuplicate() == null);
			assertTrue(pmrP2ToP3.getMatchStatus() == MatchType.UNMATCH);
			if (pmrP4ToP3.getMaster() == p3) {
				assertTrue(pmrP4ToP3.getDuplicate() == p4);
				if (pmrP1ToP3.getMaster() == p3) {
					assertTrue(pmrP1ToP3.getDuplicate() == p1);
					assertTrue(pmrP1ToP4.getMaster() == null);
					assertTrue(pmrP1ToP4.getDuplicate() == null);
					assertTrue(pmrP1ToP4.getMatchStatus() == MatchType.UNMATCH);
				} else {
					// pmrP1ToP4's master must be p4
					assertTrue(pmrP1ToP4.getMaster() == p4);
					assertTrue(pmrP1ToP4.getDuplicate() == p1);
					assertTrue(pmrP1ToP3.getMaster() == null);
					assertTrue(pmrP1ToP3.getDuplicate() == null);
					assertTrue(pmrP1ToP3.getMatchStatus() == MatchType.UNMATCH);
				}
			} else {
				// pmrP1ToP4's master must be p1
				assertTrue(pmrP1ToP4.getMaster() == p1);
				assertTrue(pmrP1ToP4.getDuplicate() == p4);
				assertTrue(pmrP4ToP3.getMaster() == null);
				assertTrue(pmrP4ToP3.getDuplicate() == null);
				assertTrue(pmrP4ToP3.getMatchStatus() == MatchType.UNMATCH);
				assertTrue(pmrP1ToP3.getMaster() == p3);
				assertTrue(pmrP1ToP3.getDuplicate() == p1);
			}
		}
	}

	/**
	 * This test confirms that if a node is not a match of a different node
	 * setting one of the nodes to be a master of a different node will not
	 * affect the relationship.
	 */
	public void testMatchDoesNotModifyUnrelatedNoMatches() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("q2");
		SourceTableRecord q2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("q3");
		SourceTableRecord q3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("q1");
		SourceTableRecord q1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("q4");
		SourceTableRecord q4 = pool.getSourceTableRecord(keyList);

		pool.defineMaster(q3, q4);

		PotentialMatchRecord pmrQ1ToQ2 = null;
		PotentialMatchRecord pmrQ1ToQ3 = null;
		PotentialMatchRecord pmrQ1ToQ4 = null;
		PotentialMatchRecord pmrQ2ToQ3 = null;
		PotentialMatchRecord pmrQ4ToQ3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == q1
					&& potentialMatch.getOriginalRhs() == q3) {
				pmrQ1ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q3
					&& potentialMatch.getOriginalRhs() == q1) {
				pmrQ1ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q3
					&& potentialMatch.getOriginalRhs() == q2) {
				pmrQ2ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q2
					&& potentialMatch.getOriginalRhs() == q3) {
				pmrQ2ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q1
					&& potentialMatch.getOriginalRhs() == q2) {
				pmrQ1ToQ2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q2
					&& potentialMatch.getOriginalRhs() == q1) {
				pmrQ1ToQ2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q1
					&& potentialMatch.getOriginalRhs() == q4) {
				pmrQ1ToQ4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q4
					&& potentialMatch.getOriginalRhs() == q1) {
				pmrQ1ToQ4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q3
					&& potentialMatch.getOriginalRhs() == q4) {
				pmrQ4ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q4
					&& potentialMatch.getOriginalRhs() == q3) {
				pmrQ4ToQ3 = potentialMatch;
			}

			if (pmrQ1ToQ3 != null && pmrQ2ToQ3 != null && pmrQ1ToQ2 != null
					&& pmrQ1ToQ4 != null && pmrQ4ToQ3 != null)
				break;
		}

		if (pmrQ1ToQ3 == null || pmrQ2ToQ3 == null || pmrQ1ToQ2 == null
				|| pmrQ1ToQ4 == null || pmrQ4ToQ3 == null) {
			fail("An edge no longer exists after we defined a match between q3 and q4.");
		}

		assertTrue(pmrQ1ToQ2.getMaster() == null);
		assertTrue(pmrQ1ToQ2.getDuplicate() == null);
		assertTrue(pmrQ1ToQ2.getMatchStatus() == MatchType.NOMATCH);
		assertTrue(pmrQ1ToQ3.getMaster() == null);
		assertTrue(pmrQ1ToQ3.getDuplicate() == null);
		assertTrue(pmrQ1ToQ3.getMatchStatus() == MatchType.NOMATCH);
		assertTrue(pmrQ1ToQ4.getMaster() == null);
		assertTrue(pmrQ1ToQ4.getDuplicate() == null);
		assertTrue(pmrQ1ToQ4.getMatchStatus() == MatchType.UNMATCH);
		assertTrue(pmrQ2ToQ3.getMaster() == q3);
		assertTrue(pmrQ2ToQ3.getDuplicate() == q2);
		assertTrue(pmrQ4ToQ3.getMaster() == q3);
		assertTrue(pmrQ4ToQ3.getDuplicate() == q4);
	}

	/**
	 * This test checks that if we define a no match within a cycle then the
	 * cycle will be broken.
	 */
	public void testNoMatchRemovesCycles() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("r2");
		SourceTableRecord r2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("r3");
		SourceTableRecord r3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("r1");
		SourceTableRecord r1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("r4");
		SourceTableRecord r4 = pool.getSourceTableRecord(keyList);

		pool.defineNoMatch(r1, r3);

		PotentialMatchRecord pmrR1ToR2 = null;
		PotentialMatchRecord pmrR1ToR3 = null;
		PotentialMatchRecord pmrR1ToR4 = null;
		PotentialMatchRecord pmrR2ToR3 = null;
		PotentialMatchRecord pmrR4ToR3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == r1
					&& potentialMatch.getOriginalRhs() == r3) {
				pmrR1ToR3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r3
					&& potentialMatch.getOriginalRhs() == r1) {
				pmrR1ToR3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r3
					&& potentialMatch.getOriginalRhs() == r2) {
				pmrR2ToR3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r2
					&& potentialMatch.getOriginalRhs() == r3) {
				pmrR2ToR3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r1
					&& potentialMatch.getOriginalRhs() == r2) {
				pmrR1ToR2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r2
					&& potentialMatch.getOriginalRhs() == r1) {
				pmrR1ToR2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r1
					&& potentialMatch.getOriginalRhs() == r4) {
				pmrR1ToR4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r4
					&& potentialMatch.getOriginalRhs() == r1) {
				pmrR1ToR4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r3
					&& potentialMatch.getOriginalRhs() == r4) {
				pmrR4ToR3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r4
					&& potentialMatch.getOriginalRhs() == r3) {
				pmrR4ToR3 = potentialMatch;
			}

			if (pmrR1ToR3 != null && pmrR2ToR3 != null && pmrR1ToR2 != null
					&& pmrR1ToR4 != null && pmrR4ToR3 != null)
				break;
		}

		if (pmrR1ToR3 == null || pmrR2ToR3 == null || pmrR1ToR2 == null
				|| pmrR1ToR4 == null || pmrR4ToR3 == null) {
			fail("An edge no longer exists after we defined a match between p1 and p4.");
		}

		if (pmrR1ToR2.getMaster() == r2) {
			assertTrue(pmrR1ToR2.getDuplicate() == r1);
			assertTrue(pmrR1ToR4.getMaster() == r1);
			assertTrue(pmrR1ToR4.getDuplicate() == r4);
		} else if (pmrR1ToR2.getMaster() == r1) {
			assertTrue(pmrR1ToR2.getDuplicate() == r2);
			assertTrue(pmrR1ToR4.getMaster() == r1);
			assertTrue(pmrR1ToR4.getDuplicate() == r4);
		} else {
			// r4 is the ultimate master
			assertTrue(pmrR1ToR2.getMaster() == r1);
			assertTrue(pmrR1ToR2.getDuplicate() == r2);
			assertTrue(pmrR1ToR4.getMaster() == r4);
			assertTrue(pmrR1ToR4.getDuplicate() == r1);
		}

		assertTrue(pmrR1ToR3.getMaster() == null);
		assertTrue(pmrR1ToR3.getDuplicate() == null);
		assertTrue(pmrR1ToR3.getMatchStatus() == MatchType.NOMATCH);
		assertTrue(pmrR2ToR3.getMaster() == null);
		assertTrue(pmrR2ToR3.getDuplicate() == null);
		assertTrue(pmrR2ToR3.getMatchStatus() == MatchType.UNMATCH);
		assertTrue(pmrR4ToR3.getMaster() == null);
		assertTrue(pmrR4ToR3.getDuplicate() == null);
		assertTrue(pmrR4ToR3.getMatchStatus() == MatchType.UNMATCH);
	}

	/**
	 * This test checks that if we unmatch nodes within a cycle then the cycle
	 * will be broken.
	 */
	public void testUnmatchRemovesCycles() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("r2");
		SourceTableRecord r2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("r3");
		SourceTableRecord r3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("r1");
		SourceTableRecord r1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("r4");
		SourceTableRecord r4 = pool.getSourceTableRecord(keyList);

		pool.defineUnmatched(r1, r3);

		PotentialMatchRecord pmrR1ToR2 = null;
		PotentialMatchRecord pmrR1ToR4 = null;
		PotentialMatchRecord pmrR2ToR3 = null;
		PotentialMatchRecord pmrR4ToR3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == r3
					&& potentialMatch.getOriginalRhs() == r2) {
				pmrR2ToR3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r2
					&& potentialMatch.getOriginalRhs() == r3) {
				pmrR2ToR3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r1
					&& potentialMatch.getOriginalRhs() == r2) {
				pmrR1ToR2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r2
					&& potentialMatch.getOriginalRhs() == r1) {
				pmrR1ToR2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r1
					&& potentialMatch.getOriginalRhs() == r4) {
				pmrR1ToR4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r4
					&& potentialMatch.getOriginalRhs() == r1) {
				pmrR1ToR4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r3
					&& potentialMatch.getOriginalRhs() == r4) {
				pmrR4ToR3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r4
					&& potentialMatch.getOriginalRhs() == r3) {
				pmrR4ToR3 = potentialMatch;
			}

			if (pmrR2ToR3 != null && pmrR1ToR2 != null && pmrR1ToR4 != null
					&& pmrR4ToR3 != null)
				break;
		}

		if (pmrR2ToR3 == null || pmrR1ToR2 == null || pmrR1ToR4 == null
				|| pmrR4ToR3 == null) {
			fail("An edge no longer exists after we defined a match between p1 and p4.");
		}

		if (pmrR1ToR2.getMaster() == r2) {
			assertTrue(pmrR1ToR2.getDuplicate() == r1);
			assertTrue(pmrR1ToR4.getMaster() == r1);
			assertTrue(pmrR1ToR4.getDuplicate() == r4);
		} else if (pmrR1ToR2.getMaster() == r1 && pmrR1ToR4.getMaster() == r1) {
			assertTrue(pmrR1ToR2.getDuplicate() == r2);
			assertTrue(pmrR1ToR4.getDuplicate() == r4);
		} else {
			// r4 is the ultimate master
			assertTrue(pmrR1ToR2.getMaster() == r1);
			assertTrue(pmrR1ToR2.getDuplicate() == r2);
			assertTrue(pmrR1ToR4.getMaster() == r4);
			assertTrue(pmrR1ToR4.getDuplicate() == r1);
		}

		assertTrue(pmrR2ToR3.getMaster() == null);
		assertTrue(pmrR2ToR3.getDuplicate() == null);
		assertTrue(pmrR2ToR3.getMatchStatus() == MatchType.UNMATCH);
		assertTrue(pmrR4ToR3.getMaster() == null);
		assertTrue(pmrR4ToR3.getDuplicate() == null);
		assertTrue(pmrR4ToR3.getMatchStatus() == MatchType.UNMATCH);
	}

	/**
	 * This test checks that an ultimate master will be removed from the decided
	 * edges if it is set to be not matched to one of its duplicates.
	 */
	public void testNoMatchToUltimateMaster() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("n2");
		SourceTableRecord n2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("n3");
		SourceTableRecord n3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("n1");
		SourceTableRecord n1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("n4");
		SourceTableRecord n4 = pool.getSourceTableRecord(keyList);

		pool.defineNoMatch(n2, n4);

		PotentialMatchRecord pmrN1ToN2 = null;
		PotentialMatchRecord pmrN2ToN3 = null;
		PotentialMatchRecord pmrN4ToN3 = null;
		PotentialMatchRecord pmrN2ToN4 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == n3
					&& potentialMatch.getOriginalRhs() == n2) {
				pmrN2ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2
					&& potentialMatch.getOriginalRhs() == n3) {
				pmrN2ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n1
					&& potentialMatch.getOriginalRhs() == n2) {
				pmrN1ToN2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2
					&& potentialMatch.getOriginalRhs() == n1) {
				pmrN1ToN2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n3
					&& potentialMatch.getOriginalRhs() == n4) {
				pmrN4ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n4
					&& potentialMatch.getOriginalRhs() == n3) {
				pmrN4ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n4
					&& potentialMatch.getOriginalRhs() == n2) {
				pmrN2ToN4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2
					&& potentialMatch.getOriginalRhs() == n4) {
				pmrN2ToN4 = potentialMatch;
			}

			if (pmrN2ToN3 != null && pmrN1ToN2 != null && pmrN4ToN3 != null
					&& pmrN2ToN4 != null)
				break;
		}

		if (pmrN2ToN3 == null || pmrN1ToN2 == null || pmrN4ToN3 == null
				|| pmrN2ToN4 == null) {
			fail("An edge no longer exists after we defined no match between n2 and n4.");
		}

		if (pmrN1ToN2.getMaster() == n1) {
			assertTrue(pmrN1ToN2.getDuplicate() == n2);
			assertTrue(pmrN2ToN3.getMaster() == n2);
			assertTrue(pmrN2ToN3.getDuplicate() == n3);
		} else if (pmrN1ToN2.getMaster() == n2 && pmrN2ToN3.getMaster() == n2) {
			assertTrue(pmrN1ToN2.getDuplicate() == n1);
			assertTrue(pmrN2ToN3.getDuplicate() == n3);
		} else {
			// n3 is the ultimate master
			assertTrue(pmrN1ToN2.getMaster() == n2);
			assertTrue(pmrN1ToN2.getDuplicate() == n1);
			assertTrue(pmrN2ToN3.getMaster() == n3);
			assertTrue(pmrN2ToN3.getDuplicate() == n2);
		}
		assertTrue(pmrN4ToN3.getMaster() == null);
		assertTrue(pmrN4ToN3.getDuplicate() == null);
		assertTrue(pmrN4ToN3.getMatchStatus() == MatchType.UNMATCH);
		assertTrue(pmrN2ToN4.getMaster() == null);
		assertTrue(pmrN2ToN4.getDuplicate() == null);
		assertTrue(pmrN2ToN4.getMatchStatus() == MatchType.NOMATCH);
	}

	/**
	 * This test checks that a non master node can be removed from its ultimate
	 * master if it is set to not be a match to another duplicate of the
	 * ultimate master.
	 */
	public void testNoMatchToOtherDuplicate() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("n2");
		SourceTableRecord n2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("n3");
		SourceTableRecord n3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("n1");
		SourceTableRecord n1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("n4");
		SourceTableRecord n4 = pool.getSourceTableRecord(keyList);

		pool.defineNoMatch(n2, n3);

		PotentialMatchRecord pmrN1ToN2 = null;
		PotentialMatchRecord pmrN2ToN3 = null;
		PotentialMatchRecord pmrN4ToN3 = null;
		PotentialMatchRecord pmrN1ToN4 = null;
		PotentialMatchRecord pmrN2ToN4 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == n3
					&& potentialMatch.getOriginalRhs() == n2) {
				pmrN2ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2
					&& potentialMatch.getOriginalRhs() == n3) {
				pmrN2ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n1
					&& potentialMatch.getOriginalRhs() == n2) {
				pmrN1ToN2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2
					&& potentialMatch.getOriginalRhs() == n1) {
				pmrN1ToN2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n3
					&& potentialMatch.getOriginalRhs() == n4) {
				pmrN4ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n4
					&& potentialMatch.getOriginalRhs() == n3) {
				pmrN4ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n1
					&& potentialMatch.getOriginalRhs() == n4) {
				pmrN1ToN4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n4
					&& potentialMatch.getOriginalRhs() == n1) {
				pmrN1ToN4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2
					&& potentialMatch.getOriginalRhs() == n4) {
				pmrN2ToN4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n4
					&& potentialMatch.getOriginalRhs() == n2) {
				pmrN2ToN4 = potentialMatch;
			}

			if (pmrN2ToN3 != null && pmrN1ToN2 != null && pmrN4ToN3 != null
					&& (pmrN1ToN4 != null || pmrN2ToN4 != null))
				break;
		}

		if (pmrN2ToN3 == null || pmrN1ToN2 == null || pmrN4ToN3 == null
				|| (pmrN1ToN4 == null && pmrN2ToN4 == null)) {
			fail("An edge no longer exists after we defined no match between n2 and n3.");
		}

		if (pmrN1ToN4 != null && pmrN1ToN4 != null) {
			fail("We created two additional edges when only one was required");
		}

		if (pmrN1ToN4 != null) {
			assertTrue(pmrN1ToN4.getMaster() != n4);
			assertTrue(pmrN1ToN4.getDuplicate() == n1);
			assertTrue(pmrN1ToN2.getMaster() == n1);
			assertTrue(pmrN1ToN2.getDuplicate() == n2);
		} else {
			// The synthetic edge was created through n2 to n4
			assertTrue(pmrN2ToN4.getMaster() == n4);
			assertTrue(pmrN2ToN4.getDuplicate() == n2);
			assertTrue(pmrN1ToN2.getMaster() == n2);
			assertTrue(pmrN1ToN2.getDuplicate() == n1);
		}

		assertTrue(pmrN2ToN3.getMaster() == null);
		assertTrue(pmrN2ToN3.getDuplicate() == null);
		assertTrue(pmrN2ToN3.getMatchStatus() == MatchType.NOMATCH);
		assertTrue(pmrN4ToN3.getMaster() == null);
		assertTrue(pmrN4ToN3.getDuplicate() == null);
		assertTrue(pmrN4ToN3.getMatchStatus() == MatchType.UNMATCH);
	}

	/**
	 * This test checks that an ultimate master will be removed from the decided
	 * edges if it is set to be unmatched to one of its duplicates.
	 */
	public void testUnmatchToUltimateMaster() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("n2");
		SourceTableRecord n2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("n3");
		SourceTableRecord n3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("n1");
		SourceTableRecord n1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("n4");
		SourceTableRecord n4 = pool.getSourceTableRecord(keyList);

		pool.defineUnmatched(n2, n4);

		PotentialMatchRecord pmrN1ToN2 = null;
		PotentialMatchRecord pmrN2ToN3 = null;
		PotentialMatchRecord pmrN4ToN3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == n3
					&& potentialMatch.getOriginalRhs() == n2) {
				pmrN2ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2
					&& potentialMatch.getOriginalRhs() == n3) {
				pmrN2ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n1
					&& potentialMatch.getOriginalRhs() == n2) {
				pmrN1ToN2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2
					&& potentialMatch.getOriginalRhs() == n1) {
				pmrN1ToN2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n3
					&& potentialMatch.getOriginalRhs() == n4) {
				pmrN4ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n4
					&& potentialMatch.getOriginalRhs() == n3) {
				pmrN4ToN3 = potentialMatch;
			}

			if (pmrN2ToN3 != null && pmrN1ToN2 != null && pmrN4ToN3 != null)
				break;
		}

		if (pmrN2ToN3 == null || pmrN1ToN2 == null || pmrN4ToN3 == null) {
			fail("An edge no longer exists after we unmatch n2 and n4.");
		}

		if (pmrN1ToN2.getMaster() == n1) {
			assertTrue(pmrN1ToN2.getDuplicate() == n2);
			assertTrue(pmrN2ToN3.getMaster() == n2);
			assertTrue(pmrN2ToN3.getDuplicate() == n3);
		} else if (pmrN1ToN2.getMaster() == n2 && pmrN2ToN3.getMaster() == n2) {
			assertTrue(pmrN1ToN2.getDuplicate() == n1);
			assertTrue(pmrN2ToN3.getDuplicate() == n3);
		} else {
			// n3 is the ultimate master
			assertTrue(pmrN1ToN2.getMaster() == n2);
			assertTrue(pmrN1ToN2.getDuplicate() == n1);
			assertTrue(pmrN2ToN3.getMaster() == n3);
			assertTrue(pmrN2ToN3.getDuplicate() == n2);
		}
		assertTrue(pmrN4ToN3.getMaster() == null);
		assertTrue(pmrN4ToN3.getDuplicate() == null);
		assertTrue(pmrN4ToN3.getMatchStatus() == MatchType.UNMATCH);
	}

	/**
	 * This test checks that a non master node can be removed from its ultimate
	 * master if it is set to be unmatched to another duplicate of the ultimate
	 * master.
	 */
	public void testUnmatchToOtherDuplicate() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("n2");
		SourceTableRecord n2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("n3");
		SourceTableRecord n3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("n1");
		SourceTableRecord n1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("n4");
		SourceTableRecord n4 = pool.getSourceTableRecord(keyList);

		pool.defineUnmatched(n2, n3);

		PotentialMatchRecord pmrN1ToN2 = null;
		PotentialMatchRecord pmrN2ToN3 = null;
		PotentialMatchRecord pmrN4ToN3 = null;
		PotentialMatchRecord pmrN1ToN4 = null;
		PotentialMatchRecord pmrN2ToN4 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == n3
					&& potentialMatch.getOriginalRhs() == n2) {
				pmrN2ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2
					&& potentialMatch.getOriginalRhs() == n3) {
				pmrN2ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n1
					&& potentialMatch.getOriginalRhs() == n2) {
				pmrN1ToN2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2
					&& potentialMatch.getOriginalRhs() == n1) {
				pmrN1ToN2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n3
					&& potentialMatch.getOriginalRhs() == n4) {
				pmrN4ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n4
					&& potentialMatch.getOriginalRhs() == n3) {
				pmrN4ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n1
					&& potentialMatch.getOriginalRhs() == n4) {
				pmrN1ToN4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n4
					&& potentialMatch.getOriginalRhs() == n1) {
				pmrN1ToN4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2
					&& potentialMatch.getOriginalRhs() == n4) {
				pmrN2ToN4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n4
					&& potentialMatch.getOriginalRhs() == n2) {
				pmrN2ToN4 = potentialMatch;
			}

			if (pmrN2ToN3 != null && pmrN1ToN2 != null && pmrN4ToN3 != null
					&& (pmrN1ToN4 != null || pmrN2ToN4 != null))
				break;
		}

		if (pmrN2ToN3 == null || pmrN1ToN2 == null || pmrN4ToN3 == null
				|| (pmrN1ToN4 == null && pmrN2ToN4 == null)) {
			fail("An edge no longer exists after we unmatch n2 and n3.");
		}

		if (pmrN1ToN4 != null && pmrN2ToN4 != null) {
			fail("We created two additional edges when only one was required");
		}

		if (pmrN1ToN4 != null) {
			assertTrue(pmrN1ToN4.getMaster() == n4);
			assertTrue(pmrN1ToN4.getDuplicate() == n1);
			assertTrue(pmrN1ToN2.getMaster() == n1);
			assertTrue(pmrN1ToN2.getDuplicate() == n2);
		} else {
			// The synthetic edge was created through n2 to n4
			assertTrue(pmrN2ToN4.getMaster() == n4);
			assertTrue(pmrN2ToN4.getDuplicate() == n2);
			assertTrue(pmrN1ToN2.getMaster() == n2);
			assertTrue(pmrN1ToN2.getDuplicate() == n1);
		}

		assertTrue(pmrN2ToN3.getMaster() == null);
		assertTrue(pmrN2ToN3.getDuplicate() == null);
		assertTrue(pmrN2ToN3.getMatchStatus() == MatchType.UNMATCH);
		assertTrue(pmrN4ToN3.getMaster() == null);
		assertTrue(pmrN4ToN3.getDuplicate() == null);
		assertTrue(pmrN4ToN3.getMatchStatus() == MatchType.UNMATCH);
	}

	/**
	 * This test confirms that master all takes only unmatched edges and leaves
	 * no match edges alone
	 */
	public void testMatchAllDoesntTakeNoMatches() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("q2");
		SourceTableRecord q2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("q3");
		SourceTableRecord q3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("q1");
		SourceTableRecord q1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("q4");
		SourceTableRecord q4 = pool.getSourceTableRecord(keyList);

		pool.defineMasterOfAll(q3);

		PotentialMatchRecord pmrQ1ToQ2 = null;
		PotentialMatchRecord pmrQ1ToQ3 = null;
		PotentialMatchRecord pmrQ1ToQ4 = null;
		PotentialMatchRecord pmrQ2ToQ3 = null;
		PotentialMatchRecord pmrQ4ToQ3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == q1
					&& potentialMatch.getOriginalRhs() == q3) {
				pmrQ1ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q3
					&& potentialMatch.getOriginalRhs() == q1) {
				pmrQ1ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q3
					&& potentialMatch.getOriginalRhs() == q2) {
				pmrQ2ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q2
					&& potentialMatch.getOriginalRhs() == q3) {
				pmrQ2ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q1
					&& potentialMatch.getOriginalRhs() == q2) {
				pmrQ1ToQ2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q2
					&& potentialMatch.getOriginalRhs() == q1) {
				pmrQ1ToQ2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q1
					&& potentialMatch.getOriginalRhs() == q4) {
				pmrQ1ToQ4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q4
					&& potentialMatch.getOriginalRhs() == q1) {
				pmrQ1ToQ4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q3
					&& potentialMatch.getOriginalRhs() == q4) {
				pmrQ4ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q4
					&& potentialMatch.getOriginalRhs() == q3) {
				pmrQ4ToQ3 = potentialMatch;
			}

			if (pmrQ1ToQ3 != null && pmrQ2ToQ3 != null && pmrQ1ToQ2 != null
					&& pmrQ1ToQ4 != null && pmrQ4ToQ3 != null)
				break;
		}

		if (pmrQ1ToQ3 == null || pmrQ2ToQ3 == null || pmrQ1ToQ2 == null
				|| pmrQ1ToQ4 == null || pmrQ4ToQ3 == null) {
			fail("An edge no longer exists after we defined q3 to be the master of all.");
		}

		assertTrue(pmrQ1ToQ2.getMaster() == null);
		assertTrue(pmrQ1ToQ2.getDuplicate() == null);
		assertTrue(pmrQ1ToQ2.getMatchStatus() == MatchType.NOMATCH);
		assertTrue(pmrQ1ToQ3.getMaster() == null);
		assertTrue(pmrQ1ToQ3.getDuplicate() == null);
		assertTrue(pmrQ1ToQ3.getMatchStatus() == MatchType.NOMATCH);
		assertTrue(pmrQ1ToQ4.getMaster() == null);
		assertTrue(pmrQ1ToQ4.getDuplicate() == null);
		assertTrue(pmrQ1ToQ4.getMatchStatus() == MatchType.UNMATCH);
		assertTrue(pmrQ2ToQ3.getMaster() == q3);
		assertTrue(pmrQ2ToQ3.getDuplicate() == q2);
		assertTrue(pmrQ4ToQ3.getMaster() == q3);
		assertTrue(pmrQ4ToQ3.getDuplicate() == q4);
	}

	/**
	 * This test confirms that no match all will change any type of edge
	 */
	public void testNoMatchModifiesAllEdges() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("q2");
		SourceTableRecord q2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("q3");
		SourceTableRecord q3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("q1");
		SourceTableRecord q1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("q4");
		SourceTableRecord q4 = pool.getSourceTableRecord(keyList);

		pool.defineNoMatchOfAny(q3);

		PotentialMatchRecord pmrQ1ToQ2 = null;
		PotentialMatchRecord pmrQ1ToQ3 = null;
		PotentialMatchRecord pmrQ1ToQ4 = null;
		PotentialMatchRecord pmrQ2ToQ3 = null;
		PotentialMatchRecord pmrQ4ToQ3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == q1
					&& potentialMatch.getOriginalRhs() == q3) {
				pmrQ1ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q3
					&& potentialMatch.getOriginalRhs() == q1) {
				pmrQ1ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q3
					&& potentialMatch.getOriginalRhs() == q2) {
				pmrQ2ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q2
					&& potentialMatch.getOriginalRhs() == q3) {
				pmrQ2ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q1
					&& potentialMatch.getOriginalRhs() == q2) {
				pmrQ1ToQ2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q2
					&& potentialMatch.getOriginalRhs() == q1) {
				pmrQ1ToQ2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q1
					&& potentialMatch.getOriginalRhs() == q4) {
				pmrQ1ToQ4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q4
					&& potentialMatch.getOriginalRhs() == q1) {
				pmrQ1ToQ4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q3
					&& potentialMatch.getOriginalRhs() == q4) {
				pmrQ4ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q4
					&& potentialMatch.getOriginalRhs() == q3) {
				pmrQ4ToQ3 = potentialMatch;
			}

			if (pmrQ1ToQ3 != null && pmrQ2ToQ3 != null && pmrQ1ToQ2 != null
					&& pmrQ1ToQ4 != null && pmrQ4ToQ3 != null)
				break;
		}

		if (pmrQ1ToQ3 == null || pmrQ2ToQ3 == null || pmrQ1ToQ2 == null
				|| pmrQ1ToQ4 == null || pmrQ4ToQ3 == null) {
			fail("An edge no longer exists after we defined q3 to not be the match of any undefined edges.");
		}

		assertTrue(pmrQ1ToQ2.getMaster() == null);
		assertTrue(pmrQ1ToQ2.getDuplicate() == null);
		assertTrue(pmrQ1ToQ2.getMatchStatus() == MatchType.NOMATCH);
		assertTrue(pmrQ1ToQ3.getMaster() == null);
		assertTrue(pmrQ1ToQ3.getDuplicate() == null);
		assertTrue(pmrQ1ToQ3.getMatchStatus() == MatchType.NOMATCH);
		assertTrue(pmrQ1ToQ4.getMaster() == null);
		assertTrue(pmrQ1ToQ4.getDuplicate() == null);
		assertTrue(pmrQ1ToQ4.getMatchStatus() == MatchType.UNMATCH);
		assertTrue(pmrQ2ToQ3.getMaster() == null);
		assertTrue(pmrQ2ToQ3.getDuplicate() == null);
		assertTrue(pmrQ2ToQ3.getMatchStatus() == MatchType.NOMATCH);
		assertTrue(pmrQ4ToQ3.getMaster() == null);
		assertTrue(pmrQ4ToQ3.getDuplicate() == null);
		assertTrue(pmrQ4ToQ3.getMatchStatus() == MatchType.NOMATCH);
	}

	/**
	 * This test checks that the unmatch all will remove the node from a chain
	 * of matches but keep the rest of the matches together.
	 */
	public void testUnmatchToAll() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("n2");
		SourceTableRecord n2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("n3");
		SourceTableRecord n3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("n1");
		SourceTableRecord n1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("n4");
		SourceTableRecord n4 = pool.getSourceTableRecord(keyList);

		pool.defineUnmatchAll(n3);

		PotentialMatchRecord pmrN1ToN2 = null;
		PotentialMatchRecord pmrN2ToN3 = null;
		PotentialMatchRecord pmrN4ToN3 = null;
		PotentialMatchRecord pmrN1ToN4 = null;
		PotentialMatchRecord pmrN2ToN4 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == n3
					&& potentialMatch.getOriginalRhs() == n2) {
				pmrN2ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2
					&& potentialMatch.getOriginalRhs() == n3) {
				pmrN2ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n1
					&& potentialMatch.getOriginalRhs() == n2) {
				pmrN1ToN2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2
					&& potentialMatch.getOriginalRhs() == n1) {
				pmrN1ToN2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n3
					&& potentialMatch.getOriginalRhs() == n4) {
				pmrN4ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n4
					&& potentialMatch.getOriginalRhs() == n3) {
				pmrN4ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n1
					&& potentialMatch.getOriginalRhs() == n4) {
				pmrN1ToN4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n4
					&& potentialMatch.getOriginalRhs() == n1) {
				pmrN1ToN4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2
					&& potentialMatch.getOriginalRhs() == n4) {
				pmrN2ToN4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n4
					&& potentialMatch.getOriginalRhs() == n2) {
				pmrN2ToN4 = potentialMatch;
			}

			if (pmrN2ToN3 != null && pmrN1ToN2 != null && pmrN4ToN3 != null
					&& (pmrN1ToN4 != null || pmrN2ToN4 != null))
				break;
		}

		if (pmrN2ToN3 == null || pmrN1ToN2 == null || pmrN4ToN3 == null
				|| (pmrN1ToN4 == null && pmrN2ToN4 == null)) {
			fail("An edge no longer exists after we unmatched n3 from everything.");
		}

		if (pmrN1ToN4 != null && pmrN2ToN4 != null) {
			fail("We created two additional edges when only one was required");
		}

		if (pmrN1ToN4 != null) {
			assertTrue(pmrN1ToN4.getMaster() == n4);
			assertTrue(pmrN1ToN4.getDuplicate() == n1);
			assertTrue(pmrN1ToN2.getMaster() == n1);
			assertTrue(pmrN1ToN2.getDuplicate() == n2);
		} else {
			// The synthetic edge was created through n2 to n4
			assertTrue(pmrN2ToN4.getMaster() == n4);
			assertTrue(pmrN2ToN4.getDuplicate() == n2);
			assertTrue(pmrN1ToN2.getMaster() == n2);
			assertTrue(pmrN1ToN2.getDuplicate() == n1);
		}

		assertTrue(pmrN2ToN3.getMaster() == null);
		assertTrue(pmrN2ToN3.getDuplicate() == null);
		assertTrue(pmrN2ToN3.getMatchStatus() == MatchType.UNMATCH);
		assertTrue(pmrN4ToN3.getMaster() == null);
		assertTrue(pmrN4ToN3.getDuplicate() == null);
		assertTrue(pmrN4ToN3.getMatchStatus() == MatchType.UNMATCH);
	}

	/**
	 * This test unmatches no match and match edges and keeps duplicates, that
	 * were dependent on the removed node, together.
	 */
	public void testUnmatchAllRemovesMatchAndNoMatch() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("p2");
		SourceTableRecord p2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("p3");
		SourceTableRecord p3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("p1");
		SourceTableRecord p1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("p4");
		SourceTableRecord p4 = pool.getSourceTableRecord(keyList);

		pool.defineUnmatchAll(p3);

		PotentialMatchRecord pmrP1ToP2 = null;
		PotentialMatchRecord pmrP1ToP3 = null;
		PotentialMatchRecord pmrP1ToP4 = null;
		PotentialMatchRecord pmrP2ToP3 = null;
		PotentialMatchRecord pmrP2ToP4 = null;
		PotentialMatchRecord pmrP4ToP3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == p1
					&& potentialMatch.getOriginalRhs() == p3) {
				pmrP1ToP3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p3
					&& potentialMatch.getOriginalRhs() == p1) {
				pmrP1ToP3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p3
					&& potentialMatch.getOriginalRhs() == p2) {
				pmrP2ToP3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p2
					&& potentialMatch.getOriginalRhs() == p3) {
				pmrP2ToP3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p1
					&& potentialMatch.getOriginalRhs() == p2) {
				pmrP1ToP2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p2
					&& potentialMatch.getOriginalRhs() == p1) {
				pmrP1ToP2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p1
					&& potentialMatch.getOriginalRhs() == p4) {
				pmrP1ToP4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p4
					&& potentialMatch.getOriginalRhs() == p1) {
				pmrP1ToP4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p3
					&& potentialMatch.getOriginalRhs() == p4) {
				pmrP4ToP3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p4
					&& potentialMatch.getOriginalRhs() == p3) {
				pmrP4ToP3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p2
					&& potentialMatch.getOriginalRhs() == p4) {
				pmrP2ToP4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p4
					&& potentialMatch.getOriginalRhs() == p2) {
				pmrP2ToP4 = potentialMatch;
			}

			if (pmrP1ToP3 != null && pmrP2ToP3 != null && pmrP2ToP4 != null
					&& pmrP1ToP2 != null && pmrP1ToP4 != null
					&& pmrP4ToP3 != null)
				break;
		}

		if (pmrP1ToP3 == null || pmrP2ToP3 == null || pmrP2ToP4 == null
				|| pmrP1ToP2 == null || pmrP1ToP4 == null || pmrP4ToP3 == null) {
			fail("An edge no longer exists after we unmatched p3 from everything.");
		}

		assertTrue(pmrP1ToP2.getMaster() == null);
		assertTrue(pmrP1ToP2.getDuplicate() == null);
		assertTrue(pmrP1ToP2.getMatchStatus() == MatchType.NOMATCH);
		assertTrue(pmrP1ToP3.getMaster() == null);
		assertTrue(pmrP1ToP3.getDuplicate() == null);
		assertTrue(pmrP1ToP3.getMatchStatus() == MatchType.UNMATCH);
		assertTrue(pmrP1ToP4.getMaster() == null);
		assertTrue(pmrP1ToP4.getDuplicate() == null);
		assertTrue(pmrP1ToP4.getMatchStatus() == MatchType.UNMATCH);
		assertTrue(pmrP2ToP3.getMaster() == null);
		assertTrue(pmrP2ToP3.getDuplicate() == null);
		assertTrue(pmrP2ToP3.getMatchStatus() == MatchType.UNMATCH);
		assertTrue(pmrP4ToP3.getMaster() == null);
		assertTrue(pmrP4ToP3.getDuplicate() == null);
		assertTrue(pmrP4ToP3.getMatchStatus() == MatchType.UNMATCH);
	}

	/**
	 * This test confirms that master all takes as many unmatched and matched
	 * edges as possible but does not take an unmatched node if it is a no match
	 * to an already taken node.
	 */
	public void testMatchAllDoesntTakeNoMatchesFromNewMatches()
			throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("s2");
		SourceTableRecord s2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("s3");
		SourceTableRecord s3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("s1");
		SourceTableRecord s1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("s4");
		SourceTableRecord s4 = pool.getSourceTableRecord(keyList);

		pool.defineMasterOfAll(s3);

		PotentialMatchRecord pmrS1ToS2 = null;
		PotentialMatchRecord pmrS1ToS3 = null;
		PotentialMatchRecord pmrS1ToS4 = null;
		PotentialMatchRecord pmrS2ToS3 = null;
		PotentialMatchRecord pmrS4ToS3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == s1
					&& potentialMatch.getOriginalRhs() == s3) {
				pmrS1ToS3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == s3
					&& potentialMatch.getOriginalRhs() == s1) {
				pmrS1ToS3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == s3
					&& potentialMatch.getOriginalRhs() == s2) {
				pmrS2ToS3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == s2
					&& potentialMatch.getOriginalRhs() == s3) {
				pmrS2ToS3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == s1
					&& potentialMatch.getOriginalRhs() == s2) {
				pmrS1ToS2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == s2
					&& potentialMatch.getOriginalRhs() == s1) {
				pmrS1ToS2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == s1
					&& potentialMatch.getOriginalRhs() == s4) {
				pmrS1ToS4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == s4
					&& potentialMatch.getOriginalRhs() == s1) {
				pmrS1ToS4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == s3
					&& potentialMatch.getOriginalRhs() == s4) {
				pmrS4ToS3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == s4
					&& potentialMatch.getOriginalRhs() == s3) {
				pmrS4ToS3 = potentialMatch;
			}

			if (pmrS1ToS3 != null && pmrS2ToS3 != null && pmrS1ToS2 != null
					&& pmrS1ToS4 != null && pmrS4ToS3 != null)
				break;
		}

		if (pmrS1ToS3 == null || pmrS2ToS3 == null || pmrS1ToS2 == null
				|| pmrS1ToS4 == null || pmrS4ToS3 == null) {
			fail("An edge no longer exists after we defined q3 to be the master of all.");
		}

		assertTrue(pmrS1ToS2.getMaster() == null);
		assertTrue(pmrS1ToS2.getDuplicate() == null);
		assertTrue(pmrS1ToS2.getMatchStatus() == MatchType.NOMATCH);
		assertTrue(pmrS1ToS4.getMaster() == null);
		assertTrue(pmrS1ToS4.getDuplicate() == null);
		assertTrue(pmrS1ToS4.getMatchStatus() == MatchType.UNMATCH);
		assertTrue(pmrS4ToS3.getMaster() == s3);
		assertTrue(pmrS4ToS3.getDuplicate() == s4);
		if (pmrS2ToS3.getMaster() == s3) {
			assertTrue(pmrS1ToS3.getMaster() == null);
			assertTrue(pmrS1ToS3.getDuplicate() == null);
			assertTrue(pmrS1ToS3.getMatchStatus() == MatchType.UNMATCH);
			assertTrue(pmrS2ToS3.getDuplicate() == s2);
		} else {
			assertTrue(pmrS1ToS3.getMaster() == s3);
			assertTrue(pmrS1ToS3.getDuplicate() == s1);
			assertTrue(pmrS2ToS3.getMaster() == null);
			assertTrue(pmrS2ToS3.getDuplicate() == null);
			assertTrue(pmrS2ToS3.getMatchStatus() == MatchType.UNMATCH);
		}
	}

	/**
	 * This test confirms that if a node is not a match of a different node
	 * setting one of the nodes to be the master of all nodes will not affect
	 * the relationship.
	 */
	public void testMatchAllDoesNotModifyUnrelatedNoMatches() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("q2");
		SourceTableRecord q2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("q3");
		SourceTableRecord q3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("q1");
		SourceTableRecord q1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("q4");
		SourceTableRecord q4 = pool.getSourceTableRecord(keyList);

		pool.defineMasterOfAll(q4);

		PotentialMatchRecord pmrQ1ToQ2 = null;
		PotentialMatchRecord pmrQ1ToQ3 = null;
		PotentialMatchRecord pmrQ1ToQ4 = null;
		PotentialMatchRecord pmrQ2ToQ3 = null;
		PotentialMatchRecord pmrQ4ToQ3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == q1
					&& potentialMatch.getOriginalRhs() == q3) {
				pmrQ1ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q3
					&& potentialMatch.getOriginalRhs() == q1) {
				pmrQ1ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q3
					&& potentialMatch.getOriginalRhs() == q2) {
				pmrQ2ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q2
					&& potentialMatch.getOriginalRhs() == q3) {
				pmrQ2ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q1
					&& potentialMatch.getOriginalRhs() == q2) {
				pmrQ1ToQ2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q2
					&& potentialMatch.getOriginalRhs() == q1) {
				pmrQ1ToQ2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q1
					&& potentialMatch.getOriginalRhs() == q4) {
				pmrQ1ToQ4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q4
					&& potentialMatch.getOriginalRhs() == q1) {
				pmrQ1ToQ4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q3
					&& potentialMatch.getOriginalRhs() == q4) {
				pmrQ4ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q4
					&& potentialMatch.getOriginalRhs() == q3) {
				pmrQ4ToQ3 = potentialMatch;
			}

			if (pmrQ1ToQ3 != null && pmrQ2ToQ3 != null && pmrQ1ToQ2 != null
					&& pmrQ1ToQ4 != null && pmrQ4ToQ3 != null)
				break;
		}

		if (pmrQ1ToQ3 == null || pmrQ2ToQ3 == null || pmrQ1ToQ2 == null
				|| pmrQ1ToQ4 == null || pmrQ4ToQ3 == null) {
			fail("An edge no longer exists after we defined a match between q3 and q4.");
		}

		if (pmrQ4ToQ3.getMaster() == q4) {
			assertTrue(pmrQ1ToQ2.getMaster() == null);
			assertTrue(pmrQ1ToQ2.getDuplicate() == null);
			assertTrue(pmrQ1ToQ2.getMatchStatus() == MatchType.NOMATCH);
			assertTrue(pmrQ1ToQ3.getMaster() == null);
			assertTrue(pmrQ1ToQ3.getDuplicate() == null);
			assertTrue(pmrQ1ToQ3.getMatchStatus() == MatchType.NOMATCH);
			assertTrue(pmrQ1ToQ4.getMaster() == null);
			assertTrue(pmrQ1ToQ4.getDuplicate() == null);
			assertTrue(pmrQ1ToQ4.getMatchStatus() == MatchType.UNMATCH);
			assertTrue(pmrQ2ToQ3.getMaster() == q3);
			assertTrue(pmrQ2ToQ3.getDuplicate() == q2);
			assertTrue(pmrQ4ToQ3.getDuplicate() == q3);
		} else {
			// q1 was matched to q4
			assertTrue(pmrQ1ToQ2.getMaster() == null);
			assertTrue(pmrQ1ToQ2.getDuplicate() == null);
			assertTrue(pmrQ1ToQ2.getMatchStatus() == MatchType.NOMATCH);
			assertTrue(pmrQ1ToQ3.getMaster() == null);
			assertTrue(pmrQ1ToQ3.getDuplicate() == null);
			assertTrue(pmrQ1ToQ3.getMatchStatus() == MatchType.NOMATCH);
			assertTrue(pmrQ1ToQ4.getMaster() == q4);
			assertTrue(pmrQ1ToQ4.getDuplicate() == q1);
			assertTrue(pmrQ2ToQ3.getMaster() == q3);
			assertTrue(pmrQ2ToQ3.getDuplicate() == q2);
			assertTrue(pmrQ4ToQ3.getMaster() == null);
			assertTrue(pmrQ4ToQ3.getDuplicate() == null);
			assertTrue(pmrQ4ToQ3.getMatchStatus() == MatchType.UNMATCH);
		}
	}

	/**
	 * This test confirms that the match all method does not match nodes that
	 * are duplicates of nodes that are defined to not be a match to the node
	 * being set to the master of all nodes.
	 */
	public void testMatchAllDoesNotModifyNodesWhoseParentIsNotAMatch()
			throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("t2");
		SourceTableRecord t2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("t3");
		SourceTableRecord t3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("t1");
		SourceTableRecord t1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("t4");
		SourceTableRecord t4 = pool.getSourceTableRecord(keyList);

		pool.defineMasterOfAll(t3);

		PotentialMatchRecord pmrT1ToT2 = null;
		PotentialMatchRecord pmrT1ToT3 = null;
		PotentialMatchRecord pmrT2ToT3 = null;
		PotentialMatchRecord pmrT4ToT3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == t1
					&& potentialMatch.getOriginalRhs() == t3) {
				pmrT1ToT3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == t3
					&& potentialMatch.getOriginalRhs() == t1) {
				pmrT1ToT3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == t3
					&& potentialMatch.getOriginalRhs() == t2) {
				pmrT2ToT3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == t2
					&& potentialMatch.getOriginalRhs() == t3) {
				pmrT2ToT3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == t1
					&& potentialMatch.getOriginalRhs() == t2) {
				pmrT1ToT2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == t2
					&& potentialMatch.getOriginalRhs() == t1) {
				pmrT1ToT2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == t3
					&& potentialMatch.getOriginalRhs() == t4) {
				pmrT4ToT3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == t4
					&& potentialMatch.getOriginalRhs() == t3) {
				pmrT4ToT3 = potentialMatch;
			}

			if (pmrT1ToT3 != null && pmrT2ToT3 != null && pmrT1ToT2 != null
					&& pmrT4ToT3 != null)
				break;
		}

		if (pmrT1ToT3 == null || pmrT2ToT3 == null || pmrT1ToT2 == null
				|| pmrT4ToT3 == null) {
			fail("An edge no longer exists after we defined t3 to be the master of all.");
		}

		assertTrue(pmrT1ToT2.getMaster() == t2);
		assertTrue(pmrT1ToT2.getDuplicate() == t1);
		assertTrue(pmrT1ToT3.getMaster() == null);
		assertTrue(pmrT1ToT3.getDuplicate() == null);
		assertTrue(pmrT1ToT3.getMatchStatus() == MatchType.UNMATCH);
		assertTrue(pmrT2ToT3.getMaster() == null);
		assertTrue(pmrT2ToT3.getDuplicate() == null);
		assertTrue(pmrT2ToT3.getMatchStatus() == MatchType.NOMATCH);
		assertTrue(pmrT4ToT3.getMaster() == t3);
		assertTrue(pmrT4ToT3.getDuplicate() == t4);
	}

	/**
	 * This test confirms that match all does not take two nodes that are both
	 * undecided but are defined to be different records (ie: no match between
	 * them)
	 */
	public void testMatchAllDoesNotModifyNoMatchesSimpleCase() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("u2");
		SourceTableRecord u2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("u3");
		SourceTableRecord u3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("u1");
		SourceTableRecord u1 = pool.getSourceTableRecord(keyList);

		pool.defineMasterOfAll(u2);

		PotentialMatchRecord pmrU1ToU2 = null;
		PotentialMatchRecord pmrU1ToU3 = null;
		PotentialMatchRecord pmrU2ToU3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == u1
					&& potentialMatch.getOriginalRhs() == u3) {
				pmrU1ToU3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == u3
					&& potentialMatch.getOriginalRhs() == u1) {
				pmrU1ToU3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == u3
					&& potentialMatch.getOriginalRhs() == u2) {
				pmrU2ToU3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == u2
					&& potentialMatch.getOriginalRhs() == u3) {
				pmrU2ToU3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == u1
					&& potentialMatch.getOriginalRhs() == u2) {
				pmrU1ToU2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == u2
					&& potentialMatch.getOriginalRhs() == u1) {
				pmrU1ToU2 = potentialMatch;
			}

			if (pmrU1ToU3 != null && pmrU2ToU3 != null && pmrU1ToU2 != null)
				break;
		}

		if (pmrU1ToU3 == null || pmrU2ToU3 == null || pmrU1ToU2 == null) {
			fail("An edge no longer exists after we defined u2 to be the master of all.");
		}

		assertTrue(pmrU1ToU3.getMaster() == null);
		assertTrue(pmrU1ToU3.getDuplicate() == null);
		assertTrue(pmrU1ToU3.getMatchStatus() == MatchType.NOMATCH);
		if (pmrU1ToU2.getMaster() == u2) {
			assertTrue(pmrU1ToU2.getDuplicate() == u1);
			assertTrue(pmrU2ToU3.getMaster() == null);
			assertTrue(pmrU2ToU3.getDuplicate() == null);
			assertTrue(pmrU2ToU3.getMatchStatus() == MatchType.UNMATCH);
		} else {
			// u3 was matched to u2 instead of u1
			assertTrue(pmrU1ToU2.getMaster() == null);
			assertTrue(pmrU1ToU2.getDuplicate() == null);
			assertTrue(pmrU1ToU2.getMatchStatus() == MatchType.UNMATCH);
			assertTrue(pmrU2ToU3.getMaster() == u2);
			assertTrue(pmrU2ToU3.getDuplicate() == u3);
		}
	}

	/**
	 * This test confirms that match all does not take two nodes that are both
	 * undecided but are defined to be different records (ie: no match between
	 * them). This is similar to the above case but it is more complex.
	 */
	public void testMatchAllDoesNotModifyNoMatchesComplexCase()
			throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("v2");
		SourceTableRecord v2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("v3");
		SourceTableRecord v3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("v1");
		SourceTableRecord v1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("v4");
		SourceTableRecord v4 = pool.getSourceTableRecord(keyList);

		pool.defineMasterOfAll(v3);

		PotentialMatchRecord pmrV1ToV2 = null;
		PotentialMatchRecord pmrV1ToV3 = null;
		PotentialMatchRecord pmrV1ToV4 = null;
		PotentialMatchRecord pmrV2ToV3 = null;
		PotentialMatchRecord pmrV4ToV3 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == v1
					&& potentialMatch.getOriginalRhs() == v3) {
				pmrV1ToV3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == v3
					&& potentialMatch.getOriginalRhs() == v1) {
				pmrV1ToV3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == v3
					&& potentialMatch.getOriginalRhs() == v2) {
				pmrV2ToV3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == v2
					&& potentialMatch.getOriginalRhs() == v3) {
				pmrV2ToV3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == v1
					&& potentialMatch.getOriginalRhs() == v2) {
				pmrV1ToV2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == v2
					&& potentialMatch.getOriginalRhs() == v1) {
				pmrV1ToV2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == v1
					&& potentialMatch.getOriginalRhs() == v4) {
				pmrV1ToV4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == v4
					&& potentialMatch.getOriginalRhs() == v1) {
				pmrV1ToV4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == v3
					&& potentialMatch.getOriginalRhs() == v4) {
				pmrV4ToV3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == v4
					&& potentialMatch.getOriginalRhs() == v3) {
				pmrV4ToV3 = potentialMatch;
			}

			if (pmrV1ToV3 != null && pmrV2ToV3 != null && pmrV1ToV2 != null
					&& pmrV1ToV4 != null && pmrV4ToV3 != null)
				break;
		}

		if (pmrV1ToV3 == null || pmrV2ToV3 == null || pmrV1ToV2 == null
				|| pmrV1ToV4 == null || pmrV4ToV3 == null) {
			fail("An edge no longer exists after we defined v3 to be the master of all.");
		}

		assertTrue(pmrV1ToV2.getMaster() == null);
		assertTrue(pmrV1ToV2.getDuplicate() == null);
		assertTrue(pmrV1ToV2.getMatchStatus() == MatchType.NOMATCH);
		if (pmrV2ToV3.getMaster() == v3) {
			assertTrue(pmrV2ToV3.getDuplicate() == v2);
			assertTrue(pmrV4ToV3.getMaster() == v3);
			assertTrue(pmrV4ToV3.getDuplicate() == v4);
			assertTrue(pmrV1ToV3.getMaster() == null);
			assertTrue(pmrV1ToV3.getDuplicate() == null);
			assertTrue(pmrV1ToV3.getMatchStatus() == MatchType.UNMATCH);
			assertTrue(pmrV1ToV4.getMaster() == null);
			assertTrue(pmrV1ToV4.getDuplicate() == null);
			assertTrue(pmrV1ToV4.getMatchStatus() == MatchType.UNMATCH);
		} else {
			// v2 was not selected to have v3 as it's ultimate master.
			assertTrue(pmrV2ToV3.getMaster() == null);
			assertTrue(pmrV2ToV3.getDuplicate() == null);
			assertTrue(pmrV2ToV3.getMatchStatus() == MatchType.UNMATCH);
			if (pmrV1ToV4.getMaster() == v1) {
				assertTrue(pmrV1ToV4.getDuplicate() == v4);
				assertTrue(pmrV4ToV3.getMaster() == null);
				assertTrue(pmrV4ToV3.getDuplicate() == null);
				assertTrue(pmrV4ToV3.getMatchStatus() == MatchType.UNMATCH);
				assertTrue(pmrV1ToV3.getMaster() == v3);
				assertTrue(pmrV1ToV3.getDuplicate() == v1);
			} else if (pmrV1ToV4.getMaster() == v4) {
				assertTrue(pmrV1ToV4.getDuplicate() == v1);
				assertTrue(pmrV1ToV3.getMaster() == null);
				assertTrue(pmrV1ToV3.getDuplicate() == null);
				assertTrue(pmrV1ToV3.getMatchStatus() == MatchType.UNMATCH);
				assertTrue(pmrV4ToV3.getMaster() == v3);
				assertTrue(pmrV4ToV3.getDuplicate() == v4);
			} else {
				assertTrue(pmrV4ToV3.getMaster() == v3);
				assertTrue(pmrV4ToV3.getDuplicate() == v4);
				assertTrue(pmrV1ToV4.getMaster() == null);
				assertTrue(pmrV1ToV4.getDuplicate() == null);
				assertTrue(pmrV1ToV4.getMatchStatus() == MatchType.UNMATCH);
				assertTrue(pmrV1ToV3.getMaster() == v3);
				assertTrue(pmrV1ToV3.getDuplicate() == v1);
			}
		}
	}

	/**
	 * Check defining master of all on a node will set the new duplicate node's
	 * old master to be a duplicate of the new master.
	 */
	public void testMasterOfAllTakesNewDuplicateDuplicates() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("h2");
		SourceTableRecord h2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("h3");
		SourceTableRecord h3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("h1");
		SourceTableRecord h1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("h4");
		SourceTableRecord h4 = pool.getSourceTableRecord(keyList);

		pool.defineMasterOfAll(h3);

		PotentialMatchRecord pmrH1ToH2 = null;
		PotentialMatchRecord pmrH2ToH3 = null;
		PotentialMatchRecord pmrH3ToH4 = null;
		for (PotentialMatchRecord potentialMatch : pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == h1
					&& potentialMatch.getOriginalRhs() == h2) {
				pmrH1ToH2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == h2
					&& potentialMatch.getOriginalRhs() == h1) {
				pmrH1ToH2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == h3
					&& potentialMatch.getOriginalRhs() == h2) {
				pmrH2ToH3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == h2
					&& potentialMatch.getOriginalRhs() == h3) {
				pmrH2ToH3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == h3
					&& potentialMatch.getOriginalRhs() == h4) {
				pmrH3ToH4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == h4
					&& potentialMatch.getOriginalRhs() == h3) {
				pmrH3ToH4 = potentialMatch;
			}

			if (pmrH1ToH2 != null && pmrH2ToH3 != null && pmrH3ToH4 != null)
				break;
		}

		if (pmrH1ToH2 == null || pmrH2ToH3 == null || pmrH2ToH3 == null) {
			fail("An edge no longer exists after we defined h2 as the master of h3.");
		}

		assertTrue(pmrH1ToH2.getMaster() == h2);
		assertTrue(pmrH1ToH2.getDuplicate() == h1);
		assertTrue(pmrH2ToH3.getMaster() == h3);
		assertTrue(pmrH2ToH3.getDuplicate() == h2);
		assertTrue(pmrH3ToH4.getMaster() == h3);
		assertTrue(pmrH3ToH4.getDuplicate() == h4);
	}

	/**
	 * This test confirms the reset pool method sets all edges to be unmatched.
	 */
	public void testResetPool() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("p2");
		SourceTableRecord p2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("p3");
		SourceTableRecord p3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("p1");
		SourceTableRecord p1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("p4");
		SourceTableRecord p4 = pool.getSourceTableRecord(keyList);

		pool.resetPool();

		PotentialMatchRecord pmrP1ToP2 = pool.getPotentialMatchFromOriginals(
				p1, p2);
		PotentialMatchRecord pmrP1ToP3 = pool.getPotentialMatchFromOriginals(
				p1, p3);
		PotentialMatchRecord pmrP1ToP4 = pool.getPotentialMatchFromOriginals(
				p1, p4);
		PotentialMatchRecord pmrP2ToP3 = pool.getPotentialMatchFromOriginals(
				p3, p2);
		PotentialMatchRecord pmrP4ToP3 = pool.getPotentialMatchFromOriginals(
				p4, p3);

		assertNotNull(pmrP1ToP3);
		assertNotNull(pmrP2ToP3);
		assertNotNull(pmrP1ToP2);
		assertNotNull(pmrP1ToP4);
		assertNotNull(pmrP4ToP3);

		assertTrue(pmrP1ToP2.getMaster() == null);
		assertTrue(pmrP1ToP2.getDuplicate() == null);
		assertSame(pmrP1ToP2.getMatchStatus(), MatchType.UNMATCH);
		assertTrue(pmrP1ToP3.getMaster() == null);
		assertTrue(pmrP1ToP3.getDuplicate() == null);
		assertTrue(pmrP1ToP3.getMatchStatus() == MatchType.UNMATCH);
		assertTrue(pmrP1ToP4.getMaster() == null);
		assertTrue(pmrP1ToP4.getDuplicate() == null);
		assertTrue(pmrP1ToP4.getMatchStatus() == MatchType.UNMATCH);
		assertTrue(pmrP2ToP3.getMaster() == null);
		assertTrue(pmrP2ToP3.getDuplicate() == null);
		assertTrue(pmrP2ToP3.getMatchStatus() == MatchType.UNMATCH);
		assertTrue(pmrP4ToP3.getMaster() == null);
		assertTrue(pmrP4ToP3.getDuplicate() == null);
		assertTrue(pmrP4ToP3.getMatchStatus() == MatchType.UNMATCH);
	}

	/**
	 * Sets the master of a node to a node that is not connected by
	 * master/duplicate edges. This will result in a new "synthetic" edge being
	 * created. We then reset the pool to confirm that the synthetic edge has
	 * been removed.
	 * <p>
	 * See the image for
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MungeProcess)}
	 * for details on the graph.
	 */
	public void testResetPoolRemovesSynthetics() throws Exception {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("a2");
		SourceTableRecord a2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("a1");
		SourceTableRecord a1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("a3");
		SourceTableRecord a3 = pool.getSourceTableRecord(keyList);

		pool.defineMaster(a3, a1);

		PotentialMatchRecord pmrA1ToA2 = pool.getPotentialMatchFromOriginals(
				a1, a2);
		PotentialMatchRecord pmrA2ToA3 = pool.getPotentialMatchFromOriginals(
				a3, a2);
		PotentialMatchRecord pmrA1ToA3 = pool.getPotentialMatchFromOriginals(
				a1, a3);

		if (pmrA1ToA2 == null || pmrA2ToA3 == null || pmrA1ToA3 == null) {
			fail("An edge no longer exists after we defined a3 as the master of a1.");
		}

		assertTrue(pmrA1ToA3.getMaster() == a3);
		assertTrue(pmrA1ToA3.getDuplicate() == a1);
		assertTrue(pmrA1ToA3.isSynthetic());

		pool.resetPool();

		assertTrue(pmrA1ToA2.getMatchStatus() == MatchType.UNMATCH);
		assertTrue(pmrA2ToA3.getMatchStatus() == MatchType.UNMATCH);
		assertFalse(pool.getPotentialMatches().contains(pmrA1ToA3));
	}

	/**
	 * This test defines two records to be a match and checks that it is stored
	 * in the database.
	 */
	public void testStoreUpdatesOnMatch() throws Exception {
		pool.store();
		
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("a2");
		SourceTableRecord a2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("a1");
		SourceTableRecord a1 = pool.getSourceTableRecord(keyList);
		PotentialMatchRecord pmrA1ToA2 = pool.getPotentialMatchFromOriginals(
				a1, a2);
		pmrA1ToA2.setMaster(a1);

		pool.store();

		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT MATCH_STATUS FROM "
				+ DDLUtils.toQualifiedName(resultTable)
				+ " WHERE DUP_CANDIDATE_10='a1' AND DUP_CANDIDATE_20='a2'");
		rs.next();
		assertEquals("MATCH", rs.getString(1));
	}

	/**
	 * This test defines two records to not be a match and checks that it is
	 * stored in the database.
	 */
	public void testStoreUpdatesOnNoMatch() throws Exception {
		pool.store();
		
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("a2");
		SourceTableRecord a2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("a1");
		SourceTableRecord a1 = pool.getSourceTableRecord(keyList);
		PotentialMatchRecord pmrA1ToA2 = pool.getPotentialMatchFromOriginals(
				a1, a2);
		pmrA1ToA2.setMatchStatus(MatchType.NOMATCH);

		pool.store();

		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT MATCH_STATUS FROM "
				+ DDLUtils.toQualifiedName(resultTable)
				+ " WHERE DUP_CANDIDATE_10='a1' AND DUP_CANDIDATE_20='a2'");
		rs.next();
		assertEquals("NO_MATCH", rs.getString(1));
	}
	
	/**
	 * This test defines two records to be unmatched and checks that it is
	 * stored in the database.
	 */
	public void testStoreUpdatesOnUnmatch() throws Exception {
		pool.store();
		
		SourceTableRecord b1 = pool.getSourceTableRecord(Collections.singletonList("b1"));
		SourceTableRecord b2 = pool.getSourceTableRecord(Collections.singletonList("b2"));
		PotentialMatchRecord b1b2 = pool.getPotentialMatchFromOriginals(
				b1, b2);
		b1b2.setMatchStatus(MatchType.UNMATCH);

		pool.store();

		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT MATCH_STATUS FROM "
				+ DDLUtils.toQualifiedName(resultTable)
				+ " WHERE DUP_CANDIDATE_10='b1' AND DUP_CANDIDATE_20='b2'");
		rs.next();
		assertEquals("UNMATCH", rs.getString(1));
	}
	
	/**
	 * This test is to make sure that the store method doesn't throw
	 * an exception if the MatchPool contains no SourceTableRecords.
	 */
	public void testStoreOnEmptyMatchPool() throws Exception {
		MatchPool emptyPool = new MatchPool(match);
		try {
			emptyPool.store();
		} catch (ArrayIndexOutOfBoundsException e) {
			// Expecting no exception to get thrown
			fail("store() threw ArrayIndexOutOfBoundsException on an empty MatchPool");
		}
	}
	
	/**
	 * This test removes potential matches from the pool to check that they will also be
	 * removed from the database.
	 */
	public void testStoreDropsRemovedRecords() throws Exception {
		pool.store();
		
		SourceTableRecord a1 = pool.getSourceTableRecord(Collections.singletonList("a1"));
		SourceTableRecord a2 = pool.getSourceTableRecord(Collections.singletonList("a2"));
		SourceTableRecord a3 = pool.getSourceTableRecord(Collections.singletonList("a3"));
		PotentialMatchRecord a1a2 = pool.getPotentialMatchFromOriginals(
				a1, a2);
		PotentialMatchRecord a2a3 = pool.getPotentialMatchFromOriginals(
				a2, a3);
		pool.removePotentialMatch(a1a2);
		pool.removePotentialMatch(a2a3);

		pool.store();

		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT MATCH_STATUS FROM "
				+ DDLUtils.toQualifiedName(resultTable)
				+ " WHERE DUP_CANDIDATE_10='a1' AND DUP_CANDIDATE_20='a2'");
		assertFalse(rs.next());
		
		rs = stmt.executeQuery("SELECT MATCH_STATUS FROM "
				+ DDLUtils.toQualifiedName(resultTable)
				+ " WHERE DUP_CANDIDATE_10='a2' AND DUP_CANDIDATE_20='a3'");
		assertFalse(rs.next());
	}
	
	/**
	 * This test defines two records to be unmatched and checks that a new
	 * potential record is stored in the database.
	 */
	public void testStoreNewRecordMatched() throws Exception {
		SourceTableRecord a1 = pool.getSourceTableRecord(Collections.singletonList("a1"));
		SourceTableRecord a3 = pool.getSourceTableRecord(Collections.singletonList("a3"));
		PotentialMatchRecord a1a3 = new PotentialMatchRecord(groupOne, MatchType.UNMATCH, 
				a1, a3, false);
		a1a3.setMatchStatus(MatchType.MATCH);
		pool.addPotentialMatch(a1a3);

		pool.store();

		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT MATCH_STATUS FROM "
				+ DDLUtils.toQualifiedName(resultTable)
				+ " WHERE DUP_CANDIDATE_10='a1' AND DUP_CANDIDATE_20='a3'");
		rs.next();
		assertEquals("MATCH", rs.getString(1));
	}
    
	//======================== Auto-Match Tests ==========================
    
    /**
     * Tests the basic case for auto-match; that all possible matches
     * are made, no synthetic edges are created and that the graph is
     * left in a legal state.
	 * <p>
	 * See graph 'a' in the image for
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MungeProcess)}
	 * for details on the graph.
     * @throws ArchitectException 
     * @throws SQLException 
     */
	public void testAutoMatchBasic() throws SQLException, ArchitectException {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("a1");
		SourceTableRecord a1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("a2");
		SourceTableRecord a2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("a3");
		SourceTableRecord a3 = pool.getSourceTableRecord(keyList);
		
		pool.doAutoMatch("Group_One");
		
		PotentialMatchRecord pmrA1ToA2 = pool.getPotentialMatchFromOriginals(a1, a2);
		PotentialMatchRecord pmrA1ToA3 = pool.getPotentialMatchFromOriginals(a1, a3);
		PotentialMatchRecord pmrA2ToA3 = pool.getPotentialMatchFromOriginals(a2, a3);
		
		assertNull(pmrA1ToA3);
		assertTrue(pmrA1ToA2.getMatchStatus() == MatchType.AUTOMATCH);
		assertTrue(pmrA2ToA3.getMatchStatus() == MatchType.AUTOMATCH);
		
		//Checks to see if a2 has two masters
		assertFalse(pmrA1ToA2.getMaster() == a1 && pmrA2ToA3.getMaster() == a3);
	}
	
	/**
	 * Tests that NoMatch edges are left alone by the auto-match.
	 * <p>
	 * See graph 'o' in the image for
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MungeProcess)}
	 * for details on the graph.
	 * @throws ArchitectException 
	 * @throws SQLException 
     */
	public void testAutoMatchNoMatchLeftAlone() throws SQLException, ArchitectException {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("o1");
		SourceTableRecord o1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("o2");
		SourceTableRecord o2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("o3");
		SourceTableRecord o3 = pool.getSourceTableRecord(keyList);
		
		pool.doAutoMatch("Group_One");
		
		PotentialMatchRecord pmrO1ToO2 = pool.getPotentialMatchFromOriginals(o1, o2);
		PotentialMatchRecord pmrO1ToO3 = pool.getPotentialMatchFromOriginals(o1, o3);
		PotentialMatchRecord pmrO2ToO3 = pool.getPotentialMatchFromOriginals(o2, o3);
		
		assertNull(pmrO1ToO2);
		assertTrue(pmrO1ToO3.getMatchStatus() == MatchType.NOMATCH);
		assertTrue(pmrO2ToO3.getMatchStatus() == MatchType.AUTOMATCH);		
	}
	
	/**
	 * Tests to make sure that NoMatch edges are left alone and used to 
	 * prevent illegal states in the graph.
	 * <p>
	 * See graph 'u' in the image for
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MungeProcess)}
	 * for details on the graph.
	 * @throws ArchitectException 
	 * @throws SQLException 
	 */
	public void testAutoMatchRespected() throws SQLException, ArchitectException {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("u1");
		SourceTableRecord u1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("u2");
		SourceTableRecord u2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("u3");
		SourceTableRecord u3 = pool.getSourceTableRecord(keyList);
		
		pool.doAutoMatch("Group_One");
		
		PotentialMatchRecord pmrU1ToU2 = pool.getPotentialMatchFromOriginals(u1, u2);
		PotentialMatchRecord pmrU1ToU3 = pool.getPotentialMatchFromOriginals(u1, u3);
		PotentialMatchRecord pmrU2ToU3 = pool.getPotentialMatchFromOriginals(u2, u3);
		
		assertTrue(pmrU1ToU3.getMatchStatus() == MatchType.NOMATCH);
		
		// Makes sure that one of the edges is decided, but not both
		// direction does not matter
		assertTrue(pmrU1ToU2.getMatchStatus() == MatchType.AUTOMATCH ^
				pmrU2ToU3.getMatchStatus() == MatchType.AUTOMATCH);
	}
	
	/**
	 * This test makes sure that when auto-match is run on a cycle, the
	 * result is a legal graph.
	 * <p>
	 * See graph 'cycle' in the image for
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MungeProcess)}
	 * for details on the graph.
	 * @throws ArchitectException 
	 * @throws SQLException 
	 */
	public void testAutoMatchOnCycle() throws SQLException, ArchitectException {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("cycle1");
		SourceTableRecord cycle1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("cycle2");
		SourceTableRecord cycle2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("cycle3");
		SourceTableRecord cycle3 = pool.getSourceTableRecord(keyList);
		
		pool.doAutoMatch("Group_One");
		
		PotentialMatchRecord pmrCyc1ToCyc2 = pool.getPotentialMatchFromOriginals(cycle1, cycle2);
		PotentialMatchRecord pmrCyc1ToCyc3 = pool.getPotentialMatchFromOriginals(cycle1, cycle3);
		PotentialMatchRecord pmrCyc2ToCyc3 = pool.getPotentialMatchFromOriginals(cycle2, cycle3);
		
		//make sure no nodes have 2 parents
		assertFalse(pmrCyc1ToCyc2.getMaster() == cycle2 
				&& pmrCyc1ToCyc3.getMaster() == cycle3);
		assertFalse(pmrCyc1ToCyc2.getMaster() == cycle1 
				&& pmrCyc2ToCyc3.getMaster() == cycle3);
		assertFalse(pmrCyc1ToCyc3.getMaster() == cycle1 
				&& pmrCyc2ToCyc3.getMaster() == cycle2);
		
		//make sure there is no cycle
		assertFalse(pmrCyc1ToCyc2.getMaster() == cycle2
					&& pmrCyc2ToCyc3.getMaster() == cycle3
					&& pmrCyc1ToCyc3.getMaster() == cycle1);
		assertFalse(pmrCyc1ToCyc2.getMaster() == cycle1
				&& pmrCyc2ToCyc3.getMaster() == cycle2
				&& pmrCyc1ToCyc3.getMaster() == cycle3);
	}
	
	/**
	 * This test makes sure that auto-match does not fail and leaves the
	 * graph in a legal state if it is given a graph that has a node with
	 * two masters.
	 * <p>
	 * See graph 'f' in the image for
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MungeProcess)}
	 * for details on the graph.
	 * @throws ArchitectException 
	 * @throws SQLException 
	 */
	public void testAutoMatchOnTwoMasters() throws SQLException, ArchitectException {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("f1");
		SourceTableRecord f1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("f2");
		SourceTableRecord f2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("f3");
		SourceTableRecord f3 = pool.getSourceTableRecord(keyList);
		
		pool.doAutoMatch("Group_One");
		
		PotentialMatchRecord pmrF1ToF2 = pool.getPotentialMatchFromOriginals(f1, f2);
		PotentialMatchRecord pmrF1ToF3 = pool.getPotentialMatchFromOriginals(f1, f3);
		PotentialMatchRecord pmrF2ToF3 = pool.getPotentialMatchFromOriginals(f2, f3);
		
		assertNull(pmrF1ToF3);
		assertTrue(pmrF1ToF2.getMatchStatus() == MatchType.AUTOMATCH);
		assertTrue(pmrF2ToF3.getMatchStatus() == MatchType.AUTOMATCH);
		assertFalse(pmrF1ToF2.getMaster() == f1 && pmrF2ToF3.getMaster() == f3);
	}
	
	/**
	 * This tests whether or not auto-match only matches on the supplied
	 * rule set.
	 * <p>
	 * See graph 'w' in the image for
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MungeProcess)}
	 * for details on the graph.
	 * @throws ArchitectException 
	 * @throws SQLException 
	 */
	public void testAutoMatchBasicTwoRules() throws SQLException, ArchitectException {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("w1");
		SourceTableRecord w1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("w2");
		SourceTableRecord w2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("w3");
		SourceTableRecord w3 = pool.getSourceTableRecord(keyList);
		
		pool.doAutoMatch("Group_One");
		
		PotentialMatchRecord pmrW1ToW2 = pool.getPotentialMatchFromOriginals(w1, w2);
		PotentialMatchRecord pmrW1ToW3 = pool.getPotentialMatchFromOriginals(w1, w3);
		PotentialMatchRecord pmrW2ToW3 = pool.getPotentialMatchFromOriginals(w2, w3);
		
		assertNull(pmrW1ToW3);
		assertTrue(pmrW1ToW2.getMatchStatus() == MatchType.UNMATCH);
		assertTrue(pmrW2ToW3.getMatchStatus() == MatchType.AUTOMATCH);
	}
	
	/**
	 * This method tests whether or not the auto-match function can
	 * make matches on all subgraphs that have edges in the provided
	 * rule set. 
	 * <p>
	 * See graphs 'x' and 'y' in the image for
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MungeProcess)}
	 * for details on the graph.
	 * @throws ArchitectException 
	 * @throws SQLException 
	 */
	public void testAutoMatchPropogation() throws SQLException, ArchitectException {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("x1");
		SourceTableRecord x1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("x2");
		SourceTableRecord x2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("x3");
		SourceTableRecord x3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("x4");
		SourceTableRecord x4 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("y1");
		SourceTableRecord y1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("y2");
		SourceTableRecord y2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("y3");
		SourceTableRecord y3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("y4");
		SourceTableRecord y4 = pool.getSourceTableRecord(keyList);

		pool.doAutoMatch("Group_One");
		
		PotentialMatchRecord pmrX1ToX2 = pool.getPotentialMatchFromOriginals(x1, x2);
		PotentialMatchRecord pmrX1ToX3 = pool.getPotentialMatchFromOriginals(x1, x3);
		PotentialMatchRecord pmrX1ToX4 = pool.getPotentialMatchFromOriginals(x1, x4);
		PotentialMatchRecord pmrX2ToX3 = pool.getPotentialMatchFromOriginals(x2, x3);
		PotentialMatchRecord pmrX2ToX4 = pool.getPotentialMatchFromOriginals(x2, x4);
		PotentialMatchRecord pmrX3ToX4 = pool.getPotentialMatchFromOriginals(x3, x4);
		
		PotentialMatchRecord pmrY1ToY2 = pool.getPotentialMatchFromOriginals(y1, y2);
		PotentialMatchRecord pmrY1ToY3 = pool.getPotentialMatchFromOriginals(y1, y3);
		PotentialMatchRecord pmrY1ToY4 = pool.getPotentialMatchFromOriginals(y1, y4);
		PotentialMatchRecord pmrY2ToY3 = pool.getPotentialMatchFromOriginals(y2, y3);
		PotentialMatchRecord pmrY2ToY4 = pool.getPotentialMatchFromOriginals(y2, y4);
		PotentialMatchRecord pmrY3ToY4 = pool.getPotentialMatchFromOriginals(y3, y4);
		
		assertNull(pmrX1ToX3);
		assertNull(pmrX1ToX4);
		assertNull(pmrX2ToX4);
		
		assertTrue(pmrX1ToX2.getMatchStatus() == MatchType.AUTOMATCH);
		assertTrue(pmrX2ToX3.getMatchStatus() == MatchType.UNMATCH);
		assertTrue(pmrX3ToX4.getMatchStatus() == MatchType.AUTOMATCH);
		
		assertNull(pmrY1ToY3);
		assertNull(pmrY1ToY4);
		assertNull(pmrY2ToY4);
		
		assertTrue(pmrY1ToY2.getMatchStatus() == MatchType.AUTOMATCH);
		assertTrue(pmrY2ToY3.getMatchStatus() == MatchType.NOMATCH);
		assertTrue(pmrY3ToY4.getMatchStatus() == MatchType.AUTOMATCH);
	}
	
	/**
	 * This test makes sure that auto-match is 'aware' of edges that need to
	 * change and are in other rule sets so that the end result is a legal
	 * state.
	 * <p>
	 * See graph 'z' in the image for
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MungeProcess)}
	 * for details on the graph.
	 * @throws ArchitectException 
	 * @throws SQLException 
	 */
	public void testAutoMatchPreservesLegalState() throws SQLException, ArchitectException {
		List<Object> keyList = new ArrayList<Object>();
		keyList.add("z1");
		SourceTableRecord z1 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("z2");
		SourceTableRecord z2 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("z3");
		SourceTableRecord z3 = pool.getSourceTableRecord(keyList);
		keyList.clear();
		keyList.add("z4");
		SourceTableRecord z4 = pool.getSourceTableRecord(keyList);
		
		pool.doAutoMatch("Group_One");
		
		PotentialMatchRecord pmrZ1ToZ2 = pool.getPotentialMatchFromOriginals(z1, z2);
		PotentialMatchRecord pmrZ1ToZ3 = pool.getPotentialMatchFromOriginals(z1, z3);
		PotentialMatchRecord pmrZ1ToZ4 = pool.getPotentialMatchFromOriginals(z1, z4);
		PotentialMatchRecord pmrZ2ToZ3 = pool.getPotentialMatchFromOriginals(z2, z3);
		PotentialMatchRecord pmrZ2ToZ4 = pool.getPotentialMatchFromOriginals(z2, z4);
		PotentialMatchRecord pmrZ3ToZ4 = pool.getPotentialMatchFromOriginals(z3, z4);
	
		assertNull(pmrZ1ToZ3);
		assertNull(pmrZ1ToZ4);
		assertNull(pmrZ2ToZ4);
		
		assertTrue(pmrZ1ToZ2.getMatchStatus() == MatchType.AUTOMATCH);
		assertTrue(pmrZ2ToZ3.getMatchStatus() == MatchType.AUTOMATCH);
		assertTrue(pmrZ3ToZ4.getMatchStatus() == MatchType.AUTOMATCH);
		
		assertFalse(pmrZ1ToZ2.getMaster() == z1
				&& pmrZ2ToZ3.getMaster() == z3);
		assertFalse(pmrZ2ToZ3.getMaster() == z2
				&& pmrZ3ToZ4.getMaster() == z4);
	}
}
