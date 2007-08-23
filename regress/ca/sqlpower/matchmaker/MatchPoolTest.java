package ca.sqlpower.matchmaker;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
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
import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;
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
    
    @Override
    protected void setUp() throws Exception {
        SPDataSource dataSource = DBTestUtil.getHSQLDBInMemoryDS();
        db = new SQLDatabase(dataSource);
        con = db.getConnection();
        
        createResultTable(con);
        
        SQLSchema plSchema = db.getSchemaByName("pl");
        
        resultTable = db.getTableByName(null, "pl", "match_results");
        
        sourceTable = new SQLTable(plSchema, "source_table", null, "TABLE", true);
        sourceTable.addColumn(new SQLColumn(sourceTable, "PK1", Types.INTEGER, 10, 0));
        sourceTable.addColumn(new SQLColumn(sourceTable, "FOO", Types.VARCHAR, 10, 0));
        sourceTable.addColumn(new SQLColumn(sourceTable, "BAR", Types.VARCHAR, 10, 0));
        
        SQLIndex sourceTableIndex = new SQLIndex("SOURCE_PK", true, null, IndexType.OTHER, null);
        sourceTableIndex.addChild(sourceTableIndex.new Column(sourceTable.getColumn(0), false, false));
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
        
        MatchMakerCriteriaGroup groupOne = new MatchMakerCriteriaGroup();
        groupOne.setName("Group_One");
        match.addMatchCriteriaGroup(groupOne);
        
        pool = MMTestUtils.createTestingPool(session, match, groupOne);
    }
    
    @Override
    protected void tearDown() throws Exception {
        dropResultTable(con);
        con.close();
    }
    
    private static void createResultTable(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        stmt.executeUpdate("CREATE TABLE pl.match_results (" +
                "\n DUP_CANDIDATE_10 integer not null," +
                "\n DUP_CANDIDATE_20 integer not null," +
                "\n CURRENT_CANDIDATE_10 integer," +
                "\n CURRENT_CANDIDATE_20 integer," +
                "\n DUP_ID0 integer," +
                "\n MASTER_ID0 integer," +
                "\n CANDIDATE_10_MAPPED varchar(1)," +
                "\n CANDIDATE_20_MAPPED varchar(1)," +
                "\n MATCH_PERCENT integer," +
                "\n GROUP_ID varchar(60)," +
                "\n MATCH_DATE timestamp," +
                "\n MATCH_STATUS varchar(60)," +
                "\n MATCH_STATUS_DATE timestamp," +
                "\n MATCH_STATUS_USER varchar(60)," +
                "\n DUP1_MASTER_IND  varchar(1)" +
                "\n)");
        stmt.close();
    }
    
    private static void dropResultTable(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        stmt.executeUpdate("DROP TABLE pl.match_results");
        stmt.close();
    }
    
    /**
     * Inserts the pair of match records described by the parameters (one row
     * for LHS-RHS and another for RHS-LHS just like the enging does).
     */
    private static void insertResultTableRecord(
            Connection con, int originalLhsKey, int originalRhsKey,
            int matchPercent, String groupName) throws SQLException {
        Statement stmt = con.createStatement();
        stmt.executeUpdate("INSERT into pl.match_results VALUES (" +
                originalLhsKey+"," +
                originalRhsKey+"," +
                originalLhsKey+","+
                originalRhsKey+","+
                "null,"+
                "null," +
                "null,"+
                "null,"+
                matchPercent+"," +
                SQL.quote(groupName)+","+
                "{ts '2006-11-30 17:01:06.0'},"+
                "null,"+
                "{ts '2006-11-30 17:01:06.0'},"+
                "null," +
                "null)"
                );
        stmt.executeUpdate("INSERT into pl.match_results VALUES (" +
                originalRhsKey+"," +
                originalLhsKey+"," +
                originalRhsKey+","+
                originalLhsKey+","+
                "null,"+
                "null," +
                "null,"+
                "null,"+
                matchPercent+"," +
                SQL.quote(groupName)+","+
                "{ts '2006-11-30 17:01:06.0'},"+
                "null,"+
                "{ts '2006-11-30 17:01:06.0'},"+
                "null," +
                "null)"
                );
        stmt.close();
        
    }
    
    /** Tests that findAll() does find all potential match records (graph edges) properly. */
    public void testFindAllPotentialMatches() throws Exception {
    	MatchPool pool = new MatchPool(match);
        insertResultTableRecord(con, 1, 2, 15, "Group_One");
        pool.findAll();        
        Set<PotentialMatchRecord> matches = pool.getPotentialMatches();
        assertEquals(1, matches.size());
        for (PotentialMatchRecord pmr : matches) {
            assertNotNull(pmr);
            assertNotNull(pmr.getOriginalLhs());
            assertNotNull(pmr.getOriginalLhs().getKeyValues());
            assertEquals(1, pmr.getOriginalLhs().getKeyValues().size());
            assertEquals("Group_One", pmr.getCriteriaGroup().getName());
        }
    }
    
    /** Tests that findAll() does find all source table records (graph nodes) properly. */
    public void testFindSourceTableRecords() throws Exception {
    	MatchPool pool = new MatchPool(match);
        insertResultTableRecord(con, 1, 2, 15, "Group_One");
        insertResultTableRecord(con, 1, 3, 15, "Group_One");
        pool.findAll();
        Collection<SourceTableRecord> nodes = pool.getSourceTableRecords();
        assertEquals(3, nodes.size());
        
        boolean foundOne = false;
        boolean foundTwo = false;
        boolean foundThree = false;
        for (SourceTableRecord str : nodes) {
            if (str.getKeyValues().get(0).equals(1)) foundOne = true;
            if (str.getKeyValues().get(0).equals(2)) foundTwo = true;
            if (str.getKeyValues().get(0).equals(3)) foundThree = true;
        }
        
        assertTrue(foundOne);
        assertTrue(foundTwo);
        assertTrue(foundThree);
    }
    
    /** Tests that findAll() hooks up inbound and outbound matches properly. */
    public void testFindAllEdgeHookup() throws Exception {
    	MatchPool pool = new MatchPool(match);
        insertResultTableRecord(con, 1, 2, 15, "Group_One");
        insertResultTableRecord(con, 1, 3, 15, "Group_One");
        pool.findAll();
        Collection<SourceTableRecord> nodes = pool.getSourceTableRecords();
        assertEquals(3, nodes.size());

        //FIXME need to be able to retrieve a particular PMR by key values
    }
    
    /**
     * Sets the master of a node in a graph when no masters have been set.
     * <p>
     * See the image for {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MatchMakerCriteriaGroup)}
     * for details on the graph.
     */
    public void testSetMasterWithNoMasters() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == a1 && potentialMatch.getOriginalRhs() == a2) {
				pmrA1ToA2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a2 && potentialMatch.getOriginalRhs() == a1) {
				pmrA1ToA2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a3 && potentialMatch.getOriginalRhs() == a2) {
				pmrA2ToA3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a2 && potentialMatch.getOriginalRhs() == a3) {
				pmrA2ToA3 = potentialMatch;
			}
			
			if (pmrA1ToA2 != null && pmrA2ToA3 != null) break;
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
     * See the image for {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MatchMakerCriteriaGroup)}
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == a1 && potentialMatch.getOriginalRhs() == a2) {
				pmrA1ToA2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a2 && potentialMatch.getOriginalRhs() == a1) {
				pmrA1ToA2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a3 && potentialMatch.getOriginalRhs() == a2) {
				pmrA2ToA3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a2 && potentialMatch.getOriginalRhs() == a3) {
				pmrA2ToA3 = potentialMatch;
			}
			
			if (pmrA1ToA2 != null && pmrA2ToA3 != null) break;
		}
		
		if (pmrA1ToA2 == null || pmrA2ToA3 == null) {
			fail("An edge no longer exists after we defined a1 as the master of all");
		}
		
		assertTrue(pmrA1ToA2.getMaster() == a1);
		assertTrue(pmrA1ToA2.getDuplicate() == a2);
		assertTrue(pmrA2ToA3.getMaster() == a2);
		assertTrue(pmrA2ToA3.getDuplicate() == a3);
	}
	
	/**
     * Reverse the master/duplicate relationship
     * <p>
     * See the image for {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MatchMakerCriteriaGroup)}
     * for details on the graph.
     */
	public void testSetMasterToDuplicate() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == c1 && potentialMatch.getOriginalRhs() == c2) {
				pmrC1ToC2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == c2 && potentialMatch.getOriginalRhs() == c1) {
				pmrC1ToC2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == c3 && potentialMatch.getOriginalRhs() == c2) {
				pmrC2ToC3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == c2 && potentialMatch.getOriginalRhs() == c3) {
				pmrC2ToC3 = potentialMatch;
			}
			
			if (pmrC1ToC2 != null && pmrC2ToC3 != null) break;
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
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MatchMakerCriteriaGroup)}
	 * for details on the graph.
	 */
	public void testSetMasterWithSameDuplicate() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == f1 && potentialMatch.getOriginalRhs() == f2) {
				pmrF1ToF2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == f2 && potentialMatch.getOriginalRhs() == f1) {
				pmrF1ToF2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == f3 && potentialMatch.getOriginalRhs() == f2) {
				pmrF2ToF3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == f2 && potentialMatch.getOriginalRhs() == f3) {
				pmrF2ToF3 = potentialMatch;
			}
			
			if (pmrF1ToF2 != null && pmrF2ToF3 != null) break;
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
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MatchMakerCriteriaGroup)}
	 * for details on the graph.
	 */
	public void testSetMasterToCurrentDuplicate() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == f1 && potentialMatch.getOriginalRhs() == f2) {
				pmrF1ToF2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == f2 && potentialMatch.getOriginalRhs() == f1) {
				pmrF1ToF2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == f3 && potentialMatch.getOriginalRhs() == f2) {
				pmrF2ToF3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == f2 && potentialMatch.getOriginalRhs() == f3) {
				pmrF2ToF3 = potentialMatch;
			}
			
			if (pmrF1ToF2 != null && pmrF2ToF3 != null) break;
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
     * See the image for {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MatchMakerCriteriaGroup)}
     * for details on the graph.
     */
	public void testSetMasterWithMaster() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == b1 && potentialMatch.getOriginalRhs() == b2) {
				pmrB1ToB2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == b2 && potentialMatch.getOriginalRhs() == b1) {
				pmrB1ToB2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == b3 && potentialMatch.getOriginalRhs() == b2) {
				pmrB2ToB3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == b2 && potentialMatch.getOriginalRhs() == b3) {
				pmrB2ToB3 = potentialMatch;
			}
			
			if (pmrB1ToB2 != null && pmrB2ToB3 != null) break;
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
     * Sets the master of a node where the master is a master of another node and the duplicate node is
     * a duplicate of a different node.
     * <p>
     * See the image for {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MatchMakerCriteriaGroup)}
     * for details on the graph.
     */
	public void testSetMasterDupIsAMasterNewMasterHasMaster() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == g1 && potentialMatch.getOriginalRhs() == g2) {
				pmrG1ToG2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == g2 && potentialMatch.getOriginalRhs() == g1) {
				pmrG1ToG2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == g3 && potentialMatch.getOriginalRhs() == g2) {
				pmrG2ToG3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == g2 && potentialMatch.getOriginalRhs() == g3) {
				pmrG2ToG3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == g3 && potentialMatch.getOriginalRhs() == g4) {
				pmrG3ToG4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == g4 && potentialMatch.getOriginalRhs() == g3) {
				pmrG3ToG4 = potentialMatch;
			}
			
			if (pmrG1ToG2 != null && pmrG2ToG3 != null && pmrG3ToG4 != null) break;
		}
		
		if (pmrG1ToG2 == null || pmrG2ToG3 == null || pmrG2ToG3 == null) {
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
     * Sets the master of a node to a node that is a duplicate of another node. The current duplicate being set to
     * have a master is also defined as a duplicate of another node.
     * <p>
     * See the image for {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MatchMakerCriteriaGroup)}
     * for details on the graph.
     */
	public void testSetMasterMasterHasMasterDupHasMaster() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == h1 && potentialMatch.getOriginalRhs() == h2) {
				pmrH1ToH2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == h2 && potentialMatch.getOriginalRhs() == h1) {
				pmrH1ToH2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == h3 && potentialMatch.getOriginalRhs() == h2) {
				pmrH2ToH3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == h2 && potentialMatch.getOriginalRhs() == h3) {
				pmrH2ToH3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == h3 && potentialMatch.getOriginalRhs() == h4) {
				pmrH3ToH4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == h4 && potentialMatch.getOriginalRhs() == h3) {
				pmrH3ToH4 = potentialMatch;
			}
			
			if (pmrH1ToH2 != null && pmrH2ToH3 != null && pmrH3ToH4 != null) break;
		}
		
		if (pmrH1ToH2 == null || pmrH2ToH3 == null || pmrH2ToH3 == null) {
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
     * Sets the master of a node to a node that is a duplicate of another node. The current duplicate being set to
     * have a master is also defined as a duplicate of another node. The way the master is defined in this case
     * should be the same way a duplicate button is implemented in the UI.
     * <p>
     * See the image for {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MatchMakerCriteriaGroup)}
     * for details on the graph.
     */
	public void testSetDuplicateMasterHasMasterDupHasMaster() {
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

		//Setting a duplicate is the same thing as setting a master but with reversed parameters.
		pool.defineMaster(h3, h2);
		
		PotentialMatchRecord pmrH1ToH2 = null;
		PotentialMatchRecord pmrH2ToH3 = null;
		PotentialMatchRecord pmrH3ToH4 = null;
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == h1 && potentialMatch.getOriginalRhs() == h2) {
				pmrH1ToH2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == h2 && potentialMatch.getOriginalRhs() == h1) {
				pmrH1ToH2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == h3 && potentialMatch.getOriginalRhs() == h2) {
				pmrH2ToH3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == h2 && potentialMatch.getOriginalRhs() == h3) {
				pmrH2ToH3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == h3 && potentialMatch.getOriginalRhs() == h4) {
				pmrH3ToH4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == h4 && potentialMatch.getOriginalRhs() == h3) {
				pmrH3ToH4 = potentialMatch;
			}
			
			if (pmrH1ToH2 != null && pmrH2ToH3 != null && pmrH3ToH4 != null) break;
		}
		
		if (pmrH1ToH2 == null || pmrH2ToH3 == null || pmrH2ToH3 == null) {
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
     * Sets the master of a node to a node that is not connected by master/duplicate edges. This will result in a 
     * new "synthetic" edge being created.
     * <p>
     * See the image for {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MatchMakerCriteriaGroup)}
     * for details on the graph.
     */
	public void testSetDuplicate() {
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
		
		//Setting a duplicate is the same thing as setting a master but with reversed parameters.
		pool.defineMaster(g1, g3);
		
		PotentialMatchRecord pmrG1ToG2 = null;
		PotentialMatchRecord pmrG2ToG3 = null;
		PotentialMatchRecord pmrG3ToG4 = null;
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == g1 && potentialMatch.getOriginalRhs() == g2) {
				pmrG1ToG2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == g2 && potentialMatch.getOriginalRhs() == g1) {
				pmrG1ToG2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == g3 && potentialMatch.getOriginalRhs() == g2) {
				pmrG2ToG3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == g2 && potentialMatch.getOriginalRhs() == g3) {
				pmrG2ToG3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == g3 && potentialMatch.getOriginalRhs() == g4) {
				pmrG3ToG4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == g4 && potentialMatch.getOriginalRhs() == g3) {
				pmrG3ToG4 = potentialMatch;
			}
			
			if (pmrG1ToG2 != null && pmrG2ToG3 != null && pmrG3ToG4 != null) break;
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
     * Sets the master of a node to a master when a cycle is involved. This is an unusual case and not something
     * that we will normally come across. Any paths that are not used to identify a master/duplicate relation
     * will be set to undefined. In this test we cannot know which node was selected as the ultimate master
     * in case the algorithm was changed to select a better ultimate master.
     * <p>
     * See the image for {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MatchMakerCriteriaGroup)}
     * for details on the graph.
     */
	public void testSetMasterInACycle() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == cycle1 && potentialMatch.getOriginalRhs() == cycle2) {
				pmrCycle1ToCycle2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle2 && potentialMatch.getOriginalRhs() == cycle1) {
				pmrCycle1ToCycle2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle3 && potentialMatch.getOriginalRhs() == cycle2) {
				pmrCycle2ToCycle3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle2 && potentialMatch.getOriginalRhs() == cycle3) {
				pmrCycle2ToCycle3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle3 && potentialMatch.getOriginalRhs() == cycle1) {
				pmrCycle3ToCycle1 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle1 && potentialMatch.getOriginalRhs() == cycle3) {
				pmrCycle3ToCycle1 = potentialMatch;
			}
			
			if (pmrCycle1ToCycle2 != null && pmrCycle2ToCycle3 != null && pmrCycle3ToCycle1 != null) break;
		}
		
		if (pmrCycle1ToCycle2 == null || pmrCycle2ToCycle3 == null || pmrCycle3ToCycle1 == null) {
			fail("An edge no longer exists after we defined cycle3 as the master of cycle2.");
		}
		
		if (pmrCycle1ToCycle2.getMaster() == cycle1) {
			assertTrue(pmrCycle1ToCycle2.getDuplicate() == cycle2);
			assertTrue(pmrCycle2ToCycle3.getMaster() == null);
			assertTrue(pmrCycle2ToCycle3.getDuplicate() == null);
			assertTrue(pmrCycle3ToCycle1.getMaster() == cycle1);
			assertTrue(pmrCycle3ToCycle1.getDuplicate() == cycle3);
		} else if (pmrCycle1ToCycle2.getMaster() == cycle2){
			assertTrue(pmrCycle1ToCycle2.getDuplicate() == cycle1);
			assertTrue(pmrCycle2ToCycle3.getMaster() == cycle2);
			assertTrue(pmrCycle2ToCycle3.getDuplicate() == cycle3);
			assertTrue(pmrCycle3ToCycle1.getMaster() == null);
			assertTrue(pmrCycle3ToCycle1.getDuplicate() == null);
		} else {
			//master of this loop was set to cycle3
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
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MatchMakerCriteriaGroup)}
	 * for details on the graph.
	 */
	public void testSetMasterWithMasterInACycle() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == j2 && potentialMatch.getOriginalRhs() == j1) {
				pmrJ1ToJ2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == j1 && potentialMatch.getOriginalRhs() == j2) {
				pmrJ1ToJ2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == j2 && potentialMatch.getOriginalRhs() == j3) {
				pmrJ2ToJ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == j3 && potentialMatch.getOriginalRhs() == j2) {
				pmrJ2ToJ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == j4 && potentialMatch.getOriginalRhs() == j3) {
				pmrJ3ToJ4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == j3 && potentialMatch.getOriginalRhs() == j4) {
				pmrJ3ToJ4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == j2 && potentialMatch.getOriginalRhs() == j4) {
				pmrJ4ToJ2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == j4 && potentialMatch.getOriginalRhs() == j2) {
				pmrJ4ToJ2 = potentialMatch;
			}
			
			if (pmrJ1ToJ2 != null && pmrJ2ToJ3 != null && pmrJ3ToJ4 != null && pmrJ4ToJ2 != null) break;
		}
		
		if (pmrJ1ToJ2 == null || pmrJ2ToJ3 == null || pmrJ3ToJ4 == null || pmrJ4ToJ2 == null) {
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
			//master of this loop was set to j4
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
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MatchMakerCriteriaGroup)}
	 * for details on the graph.
	 */
	public void testSetMasterWithUltimateMasterInACycle() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == k2 && potentialMatch.getOriginalRhs() == k1) {
				pmrK1ToK2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == k1 && potentialMatch.getOriginalRhs() == k2) {
				pmrK1ToK2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == k2 && potentialMatch.getOriginalRhs() == k3) {
				pmrK2ToK3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == k3 && potentialMatch.getOriginalRhs() == k2) {
				pmrK2ToK3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == k4 && potentialMatch.getOriginalRhs() == k3) {
				pmrK3ToK4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == k3 && potentialMatch.getOriginalRhs() == k4) {
				pmrK3ToK4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == k5 && potentialMatch.getOriginalRhs() == k4) {
				pmrK4ToK5 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == k4 && potentialMatch.getOriginalRhs() == k5) {
				pmrK4ToK5 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == k5 && potentialMatch.getOriginalRhs() == k3) {
				pmrK5ToK3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == k3 && potentialMatch.getOriginalRhs() == k5) {
				pmrK5ToK3 = potentialMatch;
			}
			
			if (pmrK1ToK2 != null && pmrK2ToK3 != null && pmrK3ToK4 != null && pmrK4ToK5 != null && pmrK5ToK3 != null) break;
		}
		
		if (pmrK1ToK2 == null || pmrK2ToK3 == null || pmrK3ToK4 == null || pmrK4ToK5 == null || pmrK5ToK3 == null) {
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
			//master of this loop was set to k5
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
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MatchMakerCriteriaGroup)}
	 * for details on the graph.
	 */
	public void testSetMasterWhereMasterHasTwoMasters() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == l2 && potentialMatch.getOriginalRhs() == l1) {
				pmrL1ToL2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == l1 && potentialMatch.getOriginalRhs() == l2) {
				pmrL1ToL2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == l2 && potentialMatch.getOriginalRhs() == l3) {
				pmrL2ToL3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == l3 && potentialMatch.getOriginalRhs() == l2) {
				pmrL2ToL3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == l4 && potentialMatch.getOriginalRhs() == l2) {
				pmrL2ToL4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == l2 && potentialMatch.getOriginalRhs() == l4) {
				pmrL2ToL4 = potentialMatch;
			}
			
			if (pmrL1ToL2 != null && pmrL2ToL3 != null && pmrL2ToL4 != null) break;
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
			//master was set to l4
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
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MatchMakerCriteriaGroup)}
	 * for details on the graph.
	 */
	public void testSetMasterWithUltimateMasterOutsideCycle() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == m2 && potentialMatch.getOriginalRhs() == m1) {
				pmrM1ToM2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == m1 && potentialMatch.getOriginalRhs() == m2) {
				pmrM1ToM2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == m2 && potentialMatch.getOriginalRhs() == m3) {
				pmrM2ToM3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == m3 && potentialMatch.getOriginalRhs() == m2) {
				pmrM2ToM3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == m5 && potentialMatch.getOriginalRhs() == m3) {
				pmrM3ToM5 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == m3 && potentialMatch.getOriginalRhs() == m5) {
				pmrM3ToM5 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == m5 && potentialMatch.getOriginalRhs() == m6) {
				pmrM5ToM6 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == m6 && potentialMatch.getOriginalRhs() == m5) {
				pmrM5ToM6 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == m6 && potentialMatch.getOriginalRhs() == m7) {
				pmrM6ToM7 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == m7 && potentialMatch.getOriginalRhs() == m6) {
				pmrM6ToM7 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == m6 && potentialMatch.getOriginalRhs() == m4) {
				pmrM6ToM4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == m4 && potentialMatch.getOriginalRhs() == m6) {
				pmrM6ToM4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == m4 && potentialMatch.getOriginalRhs() == m3) {
				pmrM4ToM3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == m3 && potentialMatch.getOriginalRhs() == m4) {
				pmrM4ToM3 = potentialMatch;
			}
			
			if (pmrM1ToM2 != null && pmrM2ToM3 != null && pmrM3ToM5 != null && pmrM5ToM6 != null && pmrM6ToM7 != null && pmrM6ToM4 != null && pmrM4ToM3 != null) break;
		}
		
		if (pmrM1ToM2 == null || pmrM2ToM3 == null || pmrM3ToM5 == null || pmrM5ToM6 == null || pmrM6ToM7 == null || pmrM6ToM4 == null || pmrM4ToM3 == null) {
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
			
		} else if (pmrM5ToM6.getMaster() == m6 && pmrM6ToM4.getMaster() == m6 && pmrM6ToM7.getMaster() == m6) {
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
	 * {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MatchMakerCriteriaGroup)}
	 * for details on the graph.
	 */
	public void testSetMasterToCreateACycle() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == i1 && potentialMatch.getOriginalRhs() == i2) {
				pmrI1ToI2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == i2 && potentialMatch.getOriginalRhs() == i1) {
				pmrI1ToI2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == i3 && potentialMatch.getOriginalRhs() == i2) {
				pmrI2ToI3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == i2 && potentialMatch.getOriginalRhs() == i3) {
				pmrI2ToI3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == i3 && potentialMatch.getOriginalRhs() == i1) {
				pmrI3ToI1 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == i1 && potentialMatch.getOriginalRhs() == i3) {
				pmrI3ToI1 = potentialMatch;
			}
			
			if (pmrI1ToI2 != null && pmrI2ToI3 != null && pmrI3ToI1 != null) break;
		}
		
		if (pmrI1ToI2 == null || pmrI2ToI3 == null || pmrI2ToI3 == null) {
			fail("An edge no longer exists after we defined i1 as the master of i3.");
		}
		
		assertTrue(pmrI1ToI2.getMaster() == i2);
		assertTrue(pmrI1ToI2.getDuplicate() == i1);
		assertTrue(pmrI2ToI3.getMaster() == i2);
		assertTrue(pmrI2ToI3.getDuplicate() == i3);
		assertTrue(pmrI3ToI1.getMaster() == null);
		assertTrue(pmrI3ToI1.getDuplicate() == null);
	}
	
	/**
     * Sets the master of a node in a graph when we need to make a synthetic edge
     * <p>
     * See the image for {@link MMTestUtils#createTestingPool(MatchMakerSession, Match, MatchMakerCriteriaGroup)}
     * for details on the graph.
     */
    public void testSetMasterCreatingASyntheticEdge() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == a1 && potentialMatch.getOriginalRhs() == a2) {
				pmrA1ToA2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a2 && potentialMatch.getOriginalRhs() == a1) {
				pmrA1ToA2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a3 && potentialMatch.getOriginalRhs() == a2) {
				pmrA2ToA3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a2 && potentialMatch.getOriginalRhs() == a3) {
				pmrA2ToA3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a3 && potentialMatch.getOriginalRhs() == a1) {
				pmrA1ToA3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a1 && potentialMatch.getOriginalRhs() == a3) {
				pmrA1ToA3 = potentialMatch;
			}
			
			if (pmrA1ToA2 != null && pmrA2ToA3 != null && pmrA1ToA3 != null) break;
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
    public void testDefiningNoMatchFromMatched() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == b1 && potentialMatch.getOriginalRhs() == b2) {
				pmrB1ToB2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == b2 && potentialMatch.getOriginalRhs() == b1) {
				pmrB1ToB2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == b3 && potentialMatch.getOriginalRhs() == b2) {
				pmrB2ToB3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == b2 && potentialMatch.getOriginalRhs() == b3) {
				pmrB2ToB3 = potentialMatch;
			}
			
			if (pmrB1ToB2 != null && pmrB2ToB3 != null) break;
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
    public void testDefineNoMatchOfAny() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == cycle1 && potentialMatch.getOriginalRhs() == cycle2) {
				pmrCycle1ToCycle2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle2 && potentialMatch.getOriginalRhs() == cycle1) {
				pmrCycle1ToCycle2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle3 && potentialMatch.getOriginalRhs() == cycle2) {
				pmrCycle2ToCycle3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle2 && potentialMatch.getOriginalRhs() == cycle3) {
				pmrCycle2ToCycle3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle3 && potentialMatch.getOriginalRhs() == cycle1) {
				pmrCycle3ToCycle1 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle1 && potentialMatch.getOriginalRhs() == cycle3) {
				pmrCycle3ToCycle1 = potentialMatch;
			}
			
			if (pmrCycle1ToCycle2 != null && pmrCycle2ToCycle3 != null && pmrCycle3ToCycle1 != null) break;
		}
		
		if (pmrCycle1ToCycle2 == null || pmrCycle2ToCycle3 == null || pmrCycle2ToCycle3 == null) {
			fail("An edge no longer exists after we defined cycle2 to not match any other connected nodes.");
		}

		assertTrue(pmrCycle1ToCycle2.getMaster() == cycle2);
		assertTrue(pmrCycle1ToCycle2.getDuplicate() == cycle1);
		assertTrue(pmrCycle2ToCycle3.getMaster() == cycle3);
		assertTrue(pmrCycle2ToCycle3.getDuplicate() == cycle2);
		assertTrue(pmrCycle3ToCycle1.getMaster() == cycle1);
		assertTrue(pmrCycle3ToCycle1.getDuplicate() == cycle3);
    }
    
    /**
	 * Sets the potential match record between two source table records that was
	 * unmatched to be no match.
	 */
    public void testDefiningNoMatchFromUnmatched() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == b1 && potentialMatch.getOriginalRhs() == b2) {
				pmrB1ToB2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == b2 && potentialMatch.getOriginalRhs() == b1) {
				pmrB1ToB2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == b3 && potentialMatch.getOriginalRhs() == b2) {
				pmrB2ToB3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == b2 && potentialMatch.getOriginalRhs() == b3) {
				pmrB2ToB3 = potentialMatch;
			}
			
			if (pmrB1ToB2 != null && pmrB2ToB3 != null) break;
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
    public void testDefiningNoMatchCreatingSynthetic() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == a1 && potentialMatch.getOriginalRhs() == a2) {
				pmrA1ToA2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a2 && potentialMatch.getOriginalRhs() == a1) {
				pmrA1ToA2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a3 && potentialMatch.getOriginalRhs() == a2) {
				pmrA2ToA3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a2 && potentialMatch.getOriginalRhs() == a3) {
				pmrA2ToA3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a3 && potentialMatch.getOriginalRhs() == a1) {
				pmrA1ToA3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == a1 && potentialMatch.getOriginalRhs() == a3) {
				pmrA1ToA3 = potentialMatch;
			}
			
			if (pmrA1ToA2 != null && pmrA2ToA3 != null && pmrA1ToA3 != null) break;
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
    public void testDefineUnmatchAll() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == cycle1 && potentialMatch.getOriginalRhs() == cycle2) {
				pmrCycle1ToCycle2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle2 && potentialMatch.getOriginalRhs() == cycle1) {
				pmrCycle1ToCycle2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle3 && potentialMatch.getOriginalRhs() == cycle2) {
				pmrCycle2ToCycle3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle2 && potentialMatch.getOriginalRhs() == cycle3) {
				pmrCycle2ToCycle3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle3 && potentialMatch.getOriginalRhs() == cycle1) {
				pmrCycle3ToCycle1 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == cycle1 && potentialMatch.getOriginalRhs() == cycle3) {
				pmrCycle3ToCycle1 = potentialMatch;
			}
			
			if (pmrCycle1ToCycle2 != null && pmrCycle2ToCycle3 != null && pmrCycle3ToCycle1 != null) break;
		}
		
		if (pmrCycle1ToCycle2 == null || pmrCycle2ToCycle3 == null || pmrCycle2ToCycle3 == null) {
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
    public void testDefiningUnmatchedForMatched() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == b1 && potentialMatch.getOriginalRhs() == b2) {
				pmrB1ToB2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == b2 && potentialMatch.getOriginalRhs() == b1) {
				pmrB1ToB2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == b3 && potentialMatch.getOriginalRhs() == b2) {
				pmrB2ToB3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == b2 && potentialMatch.getOriginalRhs() == b3) {
				pmrB2ToB3 = potentialMatch;
			}
			
			if (pmrB1ToB2 != null && pmrB2ToB3 != null) break;
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
    public void testNoMatchToMaster() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == e1 && potentialMatch.getOriginalRhs() == e2) {
				pmrE1ToE2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == e2 && potentialMatch.getOriginalRhs() == e1) {
				pmrE1ToE2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == e3 && potentialMatch.getOriginalRhs() == e2) {
				pmrE2ToE3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == e2 && potentialMatch.getOriginalRhs() == e3) {
				pmrE2ToE3 = potentialMatch;
			}
			
			if (pmrE1ToE2 != null && pmrE2ToE3 != null) break;
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
	 * This test sets a potential match record that was defined as a no match
	 * to be a match.
	 */
    public void testMatchReplacingNoMatch() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == o1 && potentialMatch.getOriginalRhs() == o3) {
				pmrO1ToO3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == o3 && potentialMatch.getOriginalRhs() == o1) {
				pmrO1ToO3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == o3 && potentialMatch.getOriginalRhs() == o2) {
				pmrO2ToO3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == o2 && potentialMatch.getOriginalRhs() == o3) {
				pmrO2ToO3 = potentialMatch;
			}
			
			if (pmrO1ToO3 != null && pmrO2ToO3 != null) break;
		}
		
		if (pmrO1ToO3 == null || pmrO2ToO3 == null) {
			fail("An edge no longer exists after we defined a match between o1 and o2.");
		}
		
		assertTrue(pmrO1ToO3.getMaster() == o3);
		assertTrue(pmrO1ToO3.getDuplicate() == o1);
		assertTrue(pmrO2ToO3.getMaster() == o3);
		assertTrue(pmrO2ToO3.getDuplicate() == o2);
		assertTrue(pmrO1ToO3.getMatchStatus() == MatchType.MATCH);
    }
    
    /**
	 * This test sets a potential match record that was defined as a no match
	 * to be a match. This test is similar to the previous test of replacing
	 * a no match with a match but it is more complex.
	 */
    public void testMatchReplacingNoMatchComplex() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == p1 && potentialMatch.getOriginalRhs() == p3) {
				pmrP1ToP3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p3 && potentialMatch.getOriginalRhs() == p1) {
				pmrP1ToP3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p3 && potentialMatch.getOriginalRhs() == p2) {
				pmrP2ToP3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p2 && potentialMatch.getOriginalRhs() == p3) {
				pmrP2ToP3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p1 && potentialMatch.getOriginalRhs() == p2) {
				pmrP1ToP2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p2 && potentialMatch.getOriginalRhs() == p1) {
				pmrP1ToP2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p1 && potentialMatch.getOriginalRhs() == p4) {
				pmrP1ToP4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p4 && potentialMatch.getOriginalRhs() == p1) {
				pmrP1ToP4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p3 && potentialMatch.getOriginalRhs() == p4) {
				pmrP4ToP3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p4 && potentialMatch.getOriginalRhs() == p3) {
				pmrP4ToP3 = potentialMatch;
			}
			
			if (pmrP1ToP3 != null && pmrP2ToP3 != null && pmrP1ToP2 != null && pmrP1ToP4 != null && pmrP4ToP3 != null) break;
		}
		
		if (pmrP1ToP3 == null || pmrP2ToP3 == null || pmrP1ToP2 == null || pmrP1ToP4 == null || pmrP4ToP3 == null) {
			fail("An edge no longer exists after we defined a match between p1 and p4.");
		}
		
		assertTrue(pmrP1ToP2.getMaster() == null);
		assertTrue(pmrP1ToP2.getDuplicate() == null);
		assertTrue(pmrP1ToP3.getMaster() == p3);
		assertTrue(pmrP1ToP3.getDuplicate() == p1);
		assertTrue(pmrP1ToP4.getMaster() == null);
		assertTrue(pmrP1ToP4.getDuplicate() == null);
		assertTrue(pmrP2ToP3.getMaster() == p3);
		assertTrue(pmrP2ToP3.getDuplicate() == p2);
		assertTrue(pmrP4ToP3.getMaster() == p3);
		assertTrue(pmrP4ToP3.getDuplicate() == p4);
    }
    
    /**
	 * This test confirms that if a node is not a match of a different node
	 * setting one of the nodes to be a master of a different node will not 
	 * affect the relationship.
	 */
    public void testMatchDoesNotModifyUnrelatedNoMatches() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == q1 && potentialMatch.getOriginalRhs() == q3) {
				pmrQ1ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q3 && potentialMatch.getOriginalRhs() == q1) {
				pmrQ1ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q3 && potentialMatch.getOriginalRhs() == q2) {
				pmrQ2ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q2 && potentialMatch.getOriginalRhs() == q3) {
				pmrQ2ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q1 && potentialMatch.getOriginalRhs() == q2) {
				pmrQ1ToQ2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q2 && potentialMatch.getOriginalRhs() == q1) {
				pmrQ1ToQ2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q1 && potentialMatch.getOriginalRhs() == q4) {
				pmrQ1ToQ4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q4 && potentialMatch.getOriginalRhs() == q1) {
				pmrQ1ToQ4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q3 && potentialMatch.getOriginalRhs() == q4) {
				pmrQ4ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q4 && potentialMatch.getOriginalRhs() == q3) {
				pmrQ4ToQ3 = potentialMatch;
			}
			
			if (pmrQ1ToQ3 != null && pmrQ2ToQ3 != null && pmrQ1ToQ2 != null && pmrQ1ToQ4 != null && pmrQ4ToQ3 != null) break;
		}
		
		if (pmrQ1ToQ3 == null || pmrQ2ToQ3 == null || pmrQ1ToQ2 == null || pmrQ1ToQ4 == null || pmrQ4ToQ3 == null) {
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
	 * This test checks that if we define a no match within a cycle
	 * then the cycle will be broken.
	 */
    public void testNoMatchRemovesCycles() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == r1 && potentialMatch.getOriginalRhs() == r3) {
				pmrR1ToR3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r3 && potentialMatch.getOriginalRhs() == r1) {
				pmrR1ToR3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r3 && potentialMatch.getOriginalRhs() == r2) {
				pmrR2ToR3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r2 && potentialMatch.getOriginalRhs() == r3) {
				pmrR2ToR3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r1 && potentialMatch.getOriginalRhs() == r2) {
				pmrR1ToR2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r2 && potentialMatch.getOriginalRhs() == r1) {
				pmrR1ToR2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r1 && potentialMatch.getOriginalRhs() == r4) {
				pmrR1ToR4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r4 && potentialMatch.getOriginalRhs() == r1) {
				pmrR1ToR4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r3 && potentialMatch.getOriginalRhs() == r4) {
				pmrR4ToR3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r4 && potentialMatch.getOriginalRhs() == r3) {
				pmrR4ToR3 = potentialMatch;
			}
			
			if (pmrR1ToR3 != null && pmrR2ToR3 != null && pmrR1ToR2 != null && pmrR1ToR4 != null && pmrR4ToR3 != null) break;
		}
		
		if (pmrR1ToR3 == null || pmrR2ToR3 == null || pmrR1ToR2 == null || pmrR1ToR4 == null || pmrR4ToR3 == null) {
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
			//r4 is the ultimate master
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
	 * This test checks that if we unmatch nodes within a cycle
	 * then the cycle will be broken.
	 */
    public void testUnmatchRemovesCycles() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == r3 && potentialMatch.getOriginalRhs() == r2) {
				pmrR2ToR3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r2 && potentialMatch.getOriginalRhs() == r3) {
				pmrR2ToR3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r1 && potentialMatch.getOriginalRhs() == r2) {
				pmrR1ToR2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r2 && potentialMatch.getOriginalRhs() == r1) {
				pmrR1ToR2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r1 && potentialMatch.getOriginalRhs() == r4) {
				pmrR1ToR4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r4 && potentialMatch.getOriginalRhs() == r1) {
				pmrR1ToR4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r3 && potentialMatch.getOriginalRhs() == r4) {
				pmrR4ToR3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == r4 && potentialMatch.getOriginalRhs() == r3) {
				pmrR4ToR3 = potentialMatch;
			}
			
			if (pmrR2ToR3 != null && pmrR1ToR2 != null && pmrR1ToR4 != null && pmrR4ToR3 != null) break;
		}
		
		if (pmrR2ToR3 == null || pmrR1ToR2 == null || pmrR1ToR4 == null || pmrR4ToR3 == null) {
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
			//r4 is the ultimate master
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
    public void testNoMatchToUltimateMaster() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == n3 && potentialMatch.getOriginalRhs() == n2) {
				pmrN2ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2 && potentialMatch.getOriginalRhs() == n3) {
				pmrN2ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n1 && potentialMatch.getOriginalRhs() == n2) {
				pmrN1ToN2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2 && potentialMatch.getOriginalRhs() == n1) {
				pmrN1ToN2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n3 && potentialMatch.getOriginalRhs() == n4) {
				pmrN4ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n4 && potentialMatch.getOriginalRhs() == n3) {
				pmrN4ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n4 && potentialMatch.getOriginalRhs() == n2) {
				pmrN2ToN4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2 && potentialMatch.getOriginalRhs() == n4) {
				pmrN2ToN4 = potentialMatch;
			}
			
			if (pmrN2ToN3 != null && pmrN1ToN2 != null && pmrN4ToN3 != null && pmrN2ToN4 != null) break;
		}
		
		if (pmrN2ToN3 == null || pmrN1ToN2 == null || pmrN4ToN3 == null || pmrN2ToN4 == null) {
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
			//n3 is the ultimate master
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
    public void testNoMatchToOtherDuplicate() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == n3 && potentialMatch.getOriginalRhs() == n2) {
				pmrN2ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2 && potentialMatch.getOriginalRhs() == n3) {
				pmrN2ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n1 && potentialMatch.getOriginalRhs() == n2) {
				pmrN1ToN2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2 && potentialMatch.getOriginalRhs() == n1) {
				pmrN1ToN2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n3 && potentialMatch.getOriginalRhs() == n4) {
				pmrN4ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n4 && potentialMatch.getOriginalRhs() == n3) {
				pmrN4ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n1 && potentialMatch.getOriginalRhs() == n4) {
				pmrN1ToN4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n4 && potentialMatch.getOriginalRhs() == n1) {
				pmrN1ToN4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2 && potentialMatch.getOriginalRhs() == n4) {
				pmrN2ToN4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n4 && potentialMatch.getOriginalRhs() == n2) {
				pmrN2ToN4 = potentialMatch;
			}
			
			if (pmrN2ToN3 != null && pmrN1ToN2 != null && pmrN4ToN3 != null && (pmrN1ToN4 != null || pmrN2ToN4 != null)) break;
		}
		
		if (pmrN2ToN3 == null || pmrN1ToN2 == null || pmrN4ToN3 == null || (pmrN1ToN4 == null && pmrN2ToN4 == null)) {
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
			//The synthetic edge was created through n2 to n4
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
    public void testUnmatchToUltimateMaster() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == n3 && potentialMatch.getOriginalRhs() == n2) {
				pmrN2ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2 && potentialMatch.getOriginalRhs() == n3) {
				pmrN2ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n1 && potentialMatch.getOriginalRhs() == n2) {
				pmrN1ToN2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2 && potentialMatch.getOriginalRhs() == n1) {
				pmrN1ToN2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n3 && potentialMatch.getOriginalRhs() == n4) {
				pmrN4ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n4 && potentialMatch.getOriginalRhs() == n3) {
				pmrN4ToN3 = potentialMatch;
			}
			
			if (pmrN2ToN3 != null && pmrN1ToN2 != null && pmrN4ToN3 != null) break;
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
			//n3 is the ultimate master
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
	 * master if it is set to be unmatched to another duplicate of the
	 * ultimate master.
	 */
    public void testUnmatchToOtherDuplicate() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == n3 && potentialMatch.getOriginalRhs() == n2) {
				pmrN2ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2 && potentialMatch.getOriginalRhs() == n3) {
				pmrN2ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n1 && potentialMatch.getOriginalRhs() == n2) {
				pmrN1ToN2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2 && potentialMatch.getOriginalRhs() == n1) {
				pmrN1ToN2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n3 && potentialMatch.getOriginalRhs() == n4) {
				pmrN4ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n4 && potentialMatch.getOriginalRhs() == n3) {
				pmrN4ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n1 && potentialMatch.getOriginalRhs() == n4) {
				pmrN1ToN4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n4 && potentialMatch.getOriginalRhs() == n1) {
				pmrN1ToN4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2 && potentialMatch.getOriginalRhs() == n4) {
				pmrN2ToN4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n4 && potentialMatch.getOriginalRhs() == n2) {
				pmrN2ToN4 = potentialMatch;
			}
			
			if (pmrN2ToN3 != null && pmrN1ToN2 != null && pmrN4ToN3 != null && (pmrN1ToN4 != null || pmrN2ToN4 != null)) break;
		}
		
		if (pmrN2ToN3 == null || pmrN1ToN2 == null || pmrN4ToN3 == null || (pmrN1ToN4 == null && pmrN2ToN4 == null)) {
			fail("An edge no longer exists after we unmatch n2 and n3.");
		}
		
		if (pmrN1ToN4 != null && pmrN1ToN4 != null) {
			fail("We created two additional edges when only one was required");
		}
		
		if (pmrN1ToN4 != null) {
			assertTrue(pmrN1ToN4.getMaster() == n4);
			assertTrue(pmrN1ToN4.getDuplicate() == n1);
			assertTrue(pmrN1ToN2.getMaster() == n1);
			assertTrue(pmrN1ToN2.getDuplicate() == n2);
		} else {
			//The synthetic edge was created through n2 to n4
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
    public void testMatchAllDoesntTakeNoMatches() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == q1 && potentialMatch.getOriginalRhs() == q3) {
				pmrQ1ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q3 && potentialMatch.getOriginalRhs() == q1) {
				pmrQ1ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q3 && potentialMatch.getOriginalRhs() == q2) {
				pmrQ2ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q2 && potentialMatch.getOriginalRhs() == q3) {
				pmrQ2ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q1 && potentialMatch.getOriginalRhs() == q2) {
				pmrQ1ToQ2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q2 && potentialMatch.getOriginalRhs() == q1) {
				pmrQ1ToQ2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q1 && potentialMatch.getOriginalRhs() == q4) {
				pmrQ1ToQ4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q4 && potentialMatch.getOriginalRhs() == q1) {
				pmrQ1ToQ4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q3 && potentialMatch.getOriginalRhs() == q4) {
				pmrQ4ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q4 && potentialMatch.getOriginalRhs() == q3) {
				pmrQ4ToQ3 = potentialMatch;
			}
			
			if (pmrQ1ToQ3 != null && pmrQ2ToQ3 != null && pmrQ1ToQ2 != null && pmrQ1ToQ4 != null && pmrQ4ToQ3 != null) break;
		}
		
		if (pmrQ1ToQ3 == null || pmrQ2ToQ3 == null || pmrQ1ToQ2 == null || pmrQ1ToQ4 == null || pmrQ4ToQ3 == null) {
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
	 * This test confirms that no match all will not change already matched
	 * edges.
	 */
    public void testNoMatchDoesNotModifyMatchedEdges() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == q1 && potentialMatch.getOriginalRhs() == q3) {
				pmrQ1ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q3 && potentialMatch.getOriginalRhs() == q1) {
				pmrQ1ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q3 && potentialMatch.getOriginalRhs() == q2) {
				pmrQ2ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q2 && potentialMatch.getOriginalRhs() == q3) {
				pmrQ2ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q1 && potentialMatch.getOriginalRhs() == q2) {
				pmrQ1ToQ2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q2 && potentialMatch.getOriginalRhs() == q1) {
				pmrQ1ToQ2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q1 && potentialMatch.getOriginalRhs() == q4) {
				pmrQ1ToQ4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q4 && potentialMatch.getOriginalRhs() == q1) {
				pmrQ1ToQ4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q3 && potentialMatch.getOriginalRhs() == q4) {
				pmrQ4ToQ3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == q4 && potentialMatch.getOriginalRhs() == q3) {
				pmrQ4ToQ3 = potentialMatch;
			}
			
			if (pmrQ1ToQ3 != null && pmrQ2ToQ3 != null && pmrQ1ToQ2 != null && pmrQ1ToQ4 != null && pmrQ4ToQ3 != null) break;
		}
		
		if (pmrQ1ToQ3 == null || pmrQ2ToQ3 == null || pmrQ1ToQ2 == null || pmrQ1ToQ4 == null || pmrQ4ToQ3 == null) {
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
		assertTrue(pmrQ2ToQ3.getMaster() == q3);
		assertTrue(pmrQ2ToQ3.getDuplicate() == q2);
		assertTrue(pmrQ4ToQ3.getMaster() == null);
		assertTrue(pmrQ4ToQ3.getDuplicate() == null);
		assertTrue(pmrQ4ToQ3.getMatchStatus() == MatchType.NOMATCH);
    }
    
    /**
	 * This test checks that the unmatch all will remove the node from
	 * a chain of matches but keep the rest of the matches together.
	 */
    public void testUnmatchToAll() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == n3 && potentialMatch.getOriginalRhs() == n2) {
				pmrN2ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2 && potentialMatch.getOriginalRhs() == n3) {
				pmrN2ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n1 && potentialMatch.getOriginalRhs() == n2) {
				pmrN1ToN2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2 && potentialMatch.getOriginalRhs() == n1) {
				pmrN1ToN2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n3 && potentialMatch.getOriginalRhs() == n4) {
				pmrN4ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n4 && potentialMatch.getOriginalRhs() == n3) {
				pmrN4ToN3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n1 && potentialMatch.getOriginalRhs() == n4) {
				pmrN1ToN4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n4 && potentialMatch.getOriginalRhs() == n1) {
				pmrN1ToN4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n2 && potentialMatch.getOriginalRhs() == n4) {
				pmrN2ToN4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == n4 && potentialMatch.getOriginalRhs() == n2) {
				pmrN2ToN4 = potentialMatch;
			}
			
			if (pmrN2ToN3 != null && pmrN1ToN2 != null && pmrN4ToN3 != null && (pmrN1ToN4 != null || pmrN2ToN4 != null)) break;
		}
		
		if (pmrN2ToN3 == null || pmrN1ToN2 == null || pmrN4ToN3 == null || (pmrN1ToN4 == null && pmrN2ToN4 == null)) {
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
			//The synthetic edge was created through n2 to n4
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
	 * This test unmatches no match and match edges and keeps duplicates,
	 * that were dependent on the removed node, together.
	 */
    public void testUnmatchAllRemovesMatchAndNoMatch() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == p1 && potentialMatch.getOriginalRhs() == p3) {
				pmrP1ToP3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p3 && potentialMatch.getOriginalRhs() == p1) {
				pmrP1ToP3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p3 && potentialMatch.getOriginalRhs() == p2) {
				pmrP2ToP3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p2 && potentialMatch.getOriginalRhs() == p3) {
				pmrP2ToP3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p1 && potentialMatch.getOriginalRhs() == p2) {
				pmrP1ToP2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p2 && potentialMatch.getOriginalRhs() == p1) {
				pmrP1ToP2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p1 && potentialMatch.getOriginalRhs() == p4) {
				pmrP1ToP4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p4 && potentialMatch.getOriginalRhs() == p1) {
				pmrP1ToP4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p3 && potentialMatch.getOriginalRhs() == p4) {
				pmrP4ToP3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p4 && potentialMatch.getOriginalRhs() == p3) {
				pmrP4ToP3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p2 && potentialMatch.getOriginalRhs() == p4) {
				pmrP2ToP4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == p4 && potentialMatch.getOriginalRhs() == p2) {
				pmrP2ToP4 = potentialMatch;
			}
			
			if (pmrP1ToP3 != null && pmrP2ToP3 != null && pmrP2ToP4 != null && pmrP1ToP2 != null && pmrP1ToP4 != null && pmrP4ToP3 != null) break;
		}
		
		if (pmrP1ToP3 == null || pmrP2ToP3 == null || pmrP2ToP4 == null || pmrP1ToP2 == null || pmrP1ToP4 == null || pmrP4ToP3 == null) {
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
    public void testMatchAllDoesntTakeNoMatchesFromNewMatches() {
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
		for (PotentialMatchRecord potentialMatch: pool.getPotentialMatches()) {
			if (potentialMatch.getOriginalLhs() == s1 && potentialMatch.getOriginalRhs() == s3) {
				pmrS1ToS3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == s3 && potentialMatch.getOriginalRhs() == s1) {
				pmrS1ToS3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == s3 && potentialMatch.getOriginalRhs() == s2) {
				pmrS2ToS3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == s2 && potentialMatch.getOriginalRhs() == s3) {
				pmrS2ToS3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == s1 && potentialMatch.getOriginalRhs() == s2) {
				pmrS1ToS2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == s2 && potentialMatch.getOriginalRhs() == s1) {
				pmrS1ToS2 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == s1 && potentialMatch.getOriginalRhs() == s4) {
				pmrS1ToS4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == s4 && potentialMatch.getOriginalRhs() == s1) {
				pmrS1ToS4 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == s3 && potentialMatch.getOriginalRhs() == s4) {
				pmrS4ToS3 = potentialMatch;
			} else if (potentialMatch.getOriginalLhs() == s4 && potentialMatch.getOriginalRhs() == s3) {
				pmrS4ToS3 = potentialMatch;
			}
			
			if (pmrS1ToS3 != null && pmrS2ToS3 != null && pmrS1ToS2 != null && pmrS1ToS4 != null && pmrS4ToS3 != null) break;
		}
		
		if (pmrS1ToS3 == null || pmrS2ToS3 == null || pmrS1ToS2 == null || pmrS1ToS4 == null || pmrS4ToS3 == null) {
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
}
