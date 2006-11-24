package ca.sqlpower.matchmaker;

import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.event.MatchMakerEventCounter;

public class MatchTest extends MatchMakerTestCase<Match> {

    Match match;
	private TestingMatchMakerSession session;

    protected void setUp() throws Exception {
        propertiesToIgnoreForEventGeneration.add("matchCriteriaGroups");
        propertiesToIgnoreForEventGeneration.add("sourceTableCatalog");
        propertiesToIgnoreForEventGeneration.add("sourceTableSchema");
        propertiesToIgnoreForEventGeneration.add("sourceTableIndex");
        propertiesToIgnoreForEventGeneration.add("sourceTableName");
        propertiesToIgnoreForEventGeneration.add("resultTableCatalog");
        propertiesToIgnoreForEventGeneration.add("resultTableSchema");
        propertiesToIgnoreForEventGeneration.add("resultTableName");
        propertiesToIgnoreForEventGeneration.add("xrefTableCatalog");
        propertiesToIgnoreForEventGeneration.add("xrefTableSchema");
        propertiesToIgnoreForEventGeneration.add("xrefTableName");
        super.setUp();
        match = new Match();
        session = new TestingMatchMakerSession();
		session.setDatabase(new SQLDatabase());
        match.setSession(session);
    }
    @Override
    protected Match getTarget() {
        return match;
    }


	public void testEqual() {
		Match m1 = new Match();
		Match m2 = new Match();
		assertTrue("Match1 <> match2", (m1 != m2) );
		assertTrue("Match1 equals match2", m1.equals(m2) );
		m1.setName("match1");
		m2.setName("match2");
		assertFalse("Match1 should not equals match2", m1.equals(m2) );
		m1.setName("match");
		m2.setName("match");
		assertTrue("Match1 should equals match2", m1.equals(m2) );
	}

    public void testMatchMakerFolderFiresEventForMatchCriteriaGroups(){
        MatchMakerEventCounter l = new MatchMakerEventCounter();
        match.getMatchCriteriaGroupFolder().addMatchMakerListener(l);
        List<MatchMakerCriteriaGroup> mmoList = new ArrayList<MatchMakerCriteriaGroup>();
        match.setMatchCriteriaGroups(mmoList);
        assertEquals("Wrong number of events fired",1,l.getAllEventCounts());
        assertEquals("Wrong type of event fired",1,l.getStructureChangedCount());
    }
    
    public void testResultTableExistsWhenTrue() throws Exception {
      ArchitectDataSource ds = new ArchitectDataSource();
      ds.setDriverClass("ca.sqlpower.architect.MockJDBCDriver");
      ds.setUrl("jdbc:mock:dbmd.catalogTerm=Catalog&dbmd.schemaTerm=Schema&catalogs=farm&schemas.farm=cow&tables.farm.cow=moo");
      ds.setUser("n/a");
      ds.setPass("n/a");
      final SQLDatabase db = new SQLDatabase(ds);
      session.setDatabase(db);
      SQLTable resultTable = db.getTableByName("farm", "cow", "moo");
      assertNotNull(resultTable);
      match.setResultTable(resultTable);
      assertTrue(match.resultTableExists());
    }
    
    /**
     * Tests that new nonexistant handcrafted tables are nonexistant according
     * to the Match object.
     */
    public void testResultTableExistsWhenFalse() throws Exception {
        ArchitectDataSource ds = new ArchitectDataSource();
        ds.setDriverClass("ca.sqlpower.architect.MockJDBCDriver");
        ds.setUrl("jdbc:mock:dbmd.catalogTerm=Catalog&dbmd.schemaTerm=Schema&catalogs=farm&schemas.farm=cow&tables.farm.cow=moo");
        ds.setUser("n/a");
        ds.setPass("n/a");
        final SQLDatabase db = new SQLDatabase(ds);
        session.setDatabase(db);
        SQLCatalog farmCat = (SQLCatalog) db.getChildByName("farm");
        SQLSchema cowSchem = (SQLSchema) farmCat.getChildByName("cow");
        SQLTable resultTable = new SQLTable(cowSchem, "nonexistant", null, "TABLE", true);
        match.setResultTable(resultTable);
        assertFalse(match.resultTableExists());
      }

    /**
     * Tests that new nonexistant simulated tables that are really in
     * the session's in-memory view of the database are nonexistant according
     * to the Match object.
     */
    public void testResultTableExistsWhenInMemoryButStillFalse() throws Exception {
        ArchitectDataSource ds = new ArchitectDataSource();
        ds.setDriverClass("ca.sqlpower.architect.MockJDBCDriver");
        ds.setUrl("jdbc:mock:dbmd.catalogTerm=Catalog&dbmd.schemaTerm=Schema&catalogs=farm&schemas.farm=cow&tables.farm.cow=moo");
        ds.setUser("n/a");
        ds.setPass("n/a");
        final SQLDatabase db = new SQLDatabase(ds);
        session.setDatabase(db);
        SQLTable resultTable = ArchitectUtils.addSimulatedTable(db, "cat", "sch", "faketab");
        match.setResultTable(resultTable);
        assertFalse(match.resultTableExists());
      }

}
