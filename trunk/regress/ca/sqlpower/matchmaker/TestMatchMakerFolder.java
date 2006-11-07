package ca.sqlpower.matchmaker;



public class TestMatchMakerFolder<C extends MatchMakerObject> extends MatchMakerTestCase<MatchMakerFolder> {

	private MatchMakerFolder<C> folder;
	final String appUserName = "THE_USER";

	protected void setUp() throws Exception {
		super.setUp();
		folder = new MatchMakerFolder<C>(appUserName);
	}

	@Override
	protected MatchMakerFolder<C> getTarget() {
		return folder;
	}

	public void testPropertyChangesName() {
		assertNull("The default last_update_user in match object should be null",
				folder.getLastUpdateAppUser());
		folder.setFolderName("xxx");
		assertEquals("The last_update_user should be [" +
				appUserName +"], because user1 has changed this match object",
				appUserName, folder.getLastUpdateAppUser());
	}

	public void testPropertyChangesDesc() {
		assertNull("The default last_update_user in match object should be null",
				folder.getLastUpdateAppUser());
		folder.setFolderDesc("xxx");
		assertEquals("The last_update_user should be [" +
				appUserName +"], because user1 has changed this match object",
				appUserName, folder.getLastUpdateAppUser());
	}

}
