package ca.sqlpower.matchmaker;


public class MatchMakerCriteriaTest extends MatchMakerTestCase<MatchMakerCriteria> {

	private MatchMakerCriteria target;
	final String appUserName = "Test User";
	private MatchMakerObject child;

	protected void setUp() throws Exception {
		super.setUp();
		target = new MatchMakerCriteria();
		MatchMakerCriteriaGroup g = new MatchMakerCriteriaGroup();
		g.addChild(target);
		Match match = new Match();
		match.setSession(new TestingMatchMakerSession());
		match.addMatchCriteriaGroup(g);
		match.setSourceTableCatalog("cat");
		match.setSourceTableName("name");
		match.setSourceTableSchema("schema");
		MatchMakerSession session = new TestingMatchMakerSession();
		((TestingMatchMakerSession)session).setAppUser(appUserName);
		target.setSession(session);
        propertiesToIgnoreForEventGeneration.add("columnName");
        // ignored due to inter-related behavior
        propertiesToIgnoreForDuplication.add("column");
        propertiesToIgnoreForDuplication.add("columnName");
        propertiesToIgnoreForDuplication.add("name");
        // removed as this should be the same object
        propertiesToIgnoreForDuplication.add("translateGroup");
        
        propertiesThatDifferOnSetAndGet.add("name");
        propertiesThatDifferOnSetAndGet.add("parent");
        
        propertiesThatHaveSideEffects.add("column");
	}

	@Override
	protected MatchMakerCriteria getTarget() {
		return target;
	}
    
    /**
     * Tests that getColumnName() and setColumnName() work together
     * in the absence of calls to getColumn() and setColumn().
     */
    public void testGetAndSetColumnName() throws Exception {
        target.setColumnName("testcow");
        assertEquals("testcow", target.getColumnName());
    }
	
	public void testAddChild() {
		try {
			target.addChild(child);
			fail("MatchMakerCriteria class does not allow child!");
		} catch ( IllegalStateException e ) {
			// this is what we excepted
		}
	}

    public void testAssertDoesNotAllowChildren(){
        assertFalse(target.allowsChildren());
    }
}