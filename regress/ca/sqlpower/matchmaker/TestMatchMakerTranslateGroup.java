package ca.sqlpower.matchmaker;


public class TestMatchMakerTranslateGroup<C extends MatchMakerTranslateWord> 
	extends MatchMakerTestCase<MatchMakerTranslateGroup> {

	final String appUserName = "test_user";
	MatchMakerTranslateGroup<C> target;
	protected void setUp() throws Exception {
		super.setUp();
		target = new MatchMakerTranslateGroup<C>(appUserName);
	}

	@Override
	protected MatchMakerTranslateGroup getTarget() {
		return target;
	}

	private void checkNull() {
		assertNull("The default last_update_user in match object should be null",
				target.getLastUpdateAppUser());
	}

	private void checkAppUserName() {
		assertEquals("The last_update_user should be [" +
				appUserName +"], because user1 has changed this match object",
				appUserName, target.getLastUpdateAppUser());
	}

	public void testSetName() {
		checkNull();
		target.setName("group name");
		checkAppUserName();
	}


}
