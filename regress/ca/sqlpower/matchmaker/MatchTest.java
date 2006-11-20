package ca.sqlpower.matchmaker;

import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.matchmaker.event.MatchMakerEventCounter;

public class MatchTest extends MatchMakerTestCase<Match> {

    Match match;

    protected void setUp() throws Exception {
        propertiesToIgnoreForEventGeneration.add("matchCriteriaGroups");
        super.setUp();
        match = new Match();
    }
    @Override
    protected Match getTarget() {
        return match;
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
