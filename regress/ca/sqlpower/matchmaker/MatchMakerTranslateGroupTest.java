package ca.sqlpower.matchmaker;


public class MatchMakerTranslateGroupTest 
	extends MatchMakerTestCase<MatchMakerTranslateGroup> {

	final String appUserName = "test_user";
	MatchMakerTranslateGroup target;
	protected void setUp() throws Exception {
		super.setUp();
		target = new MatchMakerTranslateGroup();
		MatchMakerSession session = new TestingMatchMakerSession();
		((TestingMatchMakerSession)session).setAppUser(appUserName);
		target.setSession(session);
	}

	@Override
	protected MatchMakerTranslateGroup getTarget() {
		return target;
	}
}
