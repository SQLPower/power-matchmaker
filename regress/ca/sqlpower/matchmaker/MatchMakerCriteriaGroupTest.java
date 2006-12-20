package ca.sqlpower.matchmaker;

import ca.sqlpower.matchmaker.event.MatchMakerEventCounter;



public class MatchMakerCriteriaGroupTest extends MatchMakerTestCase<MatchMakerCriteriaGroup> {

	MatchMakerCriteriaGroup target;
	final String appUserName = "test user";

    public MatchMakerCriteriaGroupTest() {
        super();
        propertiesToIgnoreForEventGeneration.add("parentMatch");
        propertiesThatDifferOnSetAndGet.add("parent");
    }
	protected void setUp() throws Exception {
		super.setUp();
		target = new MatchMakerCriteriaGroup();
		MatchMakerSession session = new TestingMatchMakerSession();
		((TestingMatchMakerSession)session).setAppUser(appUserName);
		target.setSession(session);
	}

	@Override
	protected MatchMakerCriteriaGroup getTarget() {
		return target;
	}


    public void testSetParentMatch(){
        Match match = new Match();
        MatchMakerEventCounter listener = new MatchMakerEventCounter();
        MatchMakerCriteriaGroup group = new MatchMakerCriteriaGroup();

        group.addMatchMakerListener(listener);
        group.setParentMatch(match);
        assertEquals("Incorrect number of events fired",1,listener.getAllEventCounts());
        assertEquals("Wrong property fired in the event","parent",listener.getLastEvt().getPropertyName());
    }

}
