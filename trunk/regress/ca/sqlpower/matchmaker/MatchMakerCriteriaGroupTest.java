package ca.sqlpower.matchmaker;

import ca.sqlpower.matchmaker.event.MatchMakerEventCounter;



public class MatchMakerCriteriaGroupTest extends MatchMakerTestCase<MatchMakerCriteriaGroup> {
    
	MatchMakerCriteriaGroup target;
	final String appUserName = "test user";

    public MatchMakerCriteriaGroupTest() {
        super();
        propertiesToIgnoreForEventGeneration.add("parentMatch");
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

	/*
	 * Test method for 'ca.sqlpower.matchmaker.MatchMakerCriteriaGroup.setDesc(String)'
	 */
	public void testSetDesc() {
		assertNull("The default last_update_user in match object should be null",
				target.getLastUpdateAppUser());
		target.setDesc("xxx");
		assertEquals("The last_update_user should be [" +
				appUserName +"], because user1 has changed this match object",
				appUserName, target.getLastUpdateAppUser());
	}

	/*
	 * Test method for 'ca.sqlpower.matchmaker.MatchMakerCriteriaGroup.setMatchPercent(long)'
	 */
	public void testSetMatchPercent() {
		assertNull("The default last_update_user in match object should be null",
				target.getLastUpdateAppUser());
		target.setMatchPercent(new Short("100"));
		assertEquals("The last_update_user should be [" +
				appUserName +"], because user1 has changed this match object",
				appUserName, target.getLastUpdateAppUser());
	}

	/*
	 * Test method for 'ca.sqlpower.matchmaker.MatchMakerCriteriaGroup.setName(String)'
	 */
	public void testSetName() {
		assertNull("The default last_update_user in match object should be null",
				target.getLastUpdateAppUser());
		target.setName("xxx");
		assertEquals("The last_update_user should be [" +
				appUserName +"], because user1 has changed this match object",
				appUserName, target.getLastUpdateAppUser());
	}

	/*
	 * Test method for 'ca.sqlpower.matchmaker.MatchMakerCriteriaGroup.setActive(boolean)'
	 */
	public void testSetActive() {
		assertNull("The default last_update_user in match object should be null",
				target.getLastUpdateAppUser());
		target.setActive(true);
		assertEquals("The last_update_user should be [" +
				appUserName +"], because user1 has changed this match object",
				appUserName, target.getLastUpdateAppUser());
	}

	/*
	 * Test method for 'ca.sqlpower.matchmaker.MatchMakerCriteriaGroup.setFilter(String)'
	 */
	public void testSetFilter() {
		assertNull("The default last_update_user in match object should be null",
				target.getLastUpdateAppUser());
		target.setFilter("xxx");
		assertEquals("The last_update_user should be [" +
				appUserName +"], because user1 has changed this match object",
				appUserName, target.getLastUpdateAppUser());
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
