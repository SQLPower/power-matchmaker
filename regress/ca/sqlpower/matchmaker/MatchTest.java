package ca.sqlpower.matchmaker;

import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.matchmaker.event.MatchMakerEventCounter;

public class MatchTest extends MatchMakerTestCase<Match> {

    Match match;

    protected void setUp() throws Exception {
        propertiesToIgnoreForEventGeneration.add("matchCriteriaGroups");
        propertiesToIgnoreForEventGeneration.add("sourceTableCatalog");
        propertiesToIgnoreForEventGeneration.add("sourceTableSchema");
        propertiesToIgnoreForEventGeneration.add("sourceTableIndex");
        propertiesToIgnoreForEventGeneration.add("sourceTableName");
        super.setUp();
        match = new Match();
        TestingMatchMakerSession session = new TestingMatchMakerSession();
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
}
