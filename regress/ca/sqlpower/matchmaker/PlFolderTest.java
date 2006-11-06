package ca.sqlpower.matchmaker;


public class PlFolderTest extends MatchMakerTestCase<PlFolder> {

	private PlFolder plFolder;
	
	protected void setUp() throws Exception {
		super.setUp();
		plFolder = new PlFolder<Match>("THE_USER");
	}

	@Override
	protected PlFolder getTarget() {
		return plFolder;
	}
	
// TODO this test can't be implemented until we fix the appUserName stuff in AbstractMatchMakerObject.
//	public void testConstructor() {
//		assertEquals("THE_USER", plFolder.getAppUserName());
//	}

}
