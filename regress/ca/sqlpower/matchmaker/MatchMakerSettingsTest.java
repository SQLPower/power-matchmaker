package ca.sqlpower.matchmaker;


public class MatchMakerSettingsTest extends MatchMakerTestCase {

	String appUserName = "User Name";
	MatchMakerSettings mms;

	protected void setUp() throws Exception {
		super.setUp();
		mms = new MatchMakerSettings() {

			public MatchMakerSettings duplicate(MatchMakerObject parent, MatchMakerSession s) {
				return null;
			}};
		MatchMakerSession session = new TestingMatchMakerSession();
		((TestingMatchMakerSession)session).setAppUser(appUserName);
		mms.setSession(session);


	}

	@Override
	protected MatchMakerObject<MatchMakerSettings, MatchMakerObject> getTarget() {
		return mms;
	}

	@Override
	public void testDuplicate() throws Exception {
		// this class is not duplicated only its subclasses are.
	}
}
