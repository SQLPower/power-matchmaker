package ca.sqlpower.matchmaker;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
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
import ca.sqlpower.matchmaker.swingui.StubMatchMakerSession;
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
        
        pool = new MatchPool(match);
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
        insertResultTableRecord(con, 1, 2, 15, "Group_One");
        insertResultTableRecord(con, 1, 3, 15, "Group_One");
        pool.findAll();
        Collection<SourceTableRecord> nodes = pool.getSourceTableRecords();
        assertEquals(3, nodes.size());

        //FIXME need to be able to retrieve a particular PMR by key values
    }

}
