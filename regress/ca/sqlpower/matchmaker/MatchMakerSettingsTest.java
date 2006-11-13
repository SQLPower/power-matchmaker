package ca.sqlpower.matchmaker;


public class MatchMakerSettingsTest extends MatchMakerTestCase {

	String appUserName = "User Name";
	MatchMakerSettings mms;

	protected void setUp() throws Exception {
		super.setUp();
		mms = new MatchMakerSettings() {};
		MatchMakerSession session = new TestingMatchMakerSession();
		((TestingMatchMakerSession)session).setAppUser(appUserName);
		mms.setSession(session);


	}

	@Override
	protected MatchMakerObject<MatchMakerSettings, MatchMakerObject> getTarget() {
		return mms;
	}

}
