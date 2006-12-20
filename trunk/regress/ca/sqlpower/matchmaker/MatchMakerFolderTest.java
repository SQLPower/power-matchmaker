package ca.sqlpower.matchmaker;



public class MatchMakerFolderTest<C extends MatchMakerObject> extends MatchMakerTestCase<MatchMakerFolder> {

	private MatchMakerFolder<C> folder;
	final String appUserName = "THE_USER";

	protected void setUp() throws Exception {
		super.setUp();
		folder = new MatchMakerFolder<C>();
		MatchMakerSession session = new TestingMatchMakerSession();
		((TestingMatchMakerSession)session).setAppUser(appUserName);
		folder.setSession(session);
	}

	@Override
	protected MatchMakerFolder<C> getTarget() {
		return folder;
	}

	/*
	 * Duplicate should not be run on this class
	 */
	@Override
	public void testDuplicate() throws Exception {
	}
}
