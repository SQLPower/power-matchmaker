package ca.sqlpower.matchmaker;


public class PlFolderTest extends MatchMakerTestCase<PlFolder> {

	private PlFolder plFolder;
	final String appUserName = "THE_USER";

	protected void setUp() throws Exception {
		super.setUp();
		plFolder = new PlFolder<Match>("Test Folder");
		MatchMakerSession session = new TestingMatchMakerSession();
		((TestingMatchMakerSession)session).setAppUser(appUserName);
		plFolder.setSession(session);
	}

	@Override
	protected PlFolder getTarget() {
		return plFolder;
	}
}
