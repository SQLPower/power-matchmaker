package ca.sqlpower.matchmaker;



public class MatchMakerCriteriaGroupTest extends MatchMakerTestCase<MatchMakerCriteriaGroup> {

	MatchMakerCriteriaGroup target;
	final String appUserName = "test user";

	protected void setUp() throws Exception {
		super.setUp();
		target = new MatchMakerCriteriaGroup<MatchmakerCriteria>(appUserName);
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
		target.setMatchPercent(new Long(100L));
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


}
