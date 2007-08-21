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
		
		if (pmrCycle1ToCycle2 == null || pmrCycle2ToCycle3 == null || pmrCycle2ToCycle3 == null) {
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
		assertTrue(pmrCycle3ToCycle1.getMaster() == cycle1);
		assertTrue(pmrCycle3ToCycle1.getDuplicate() == cycle3);
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
}
