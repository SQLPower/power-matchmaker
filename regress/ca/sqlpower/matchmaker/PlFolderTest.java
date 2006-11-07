package ca.sqlpower.matchmaker;


public class PlFolderTest extends MatchMakerTestCase<PlFolder> {

	private PlFolder plFolder;
	final String appUserName = "THE_USER";

	protected void setUp() throws Exception {
		super.setUp();
		plFolder = new PlFolder<Match>(appUserName);
	}

	@Override
	protected PlFolder getTarget() {
		return plFolder;
	}

	public void testPropertyChangesName() {
		assertNull("The default last_update_user in match object should be null",
				plFolder.getLastUpdateAppUser());
		plFolder.setFolderName("xxx");
		assertEquals("The last_update_user should be [" +
				appUserName +"], because user1 has changed this match object",
				appUserName, plFolder.getLastUpdateAppUser());
	}

	public void testPropertyChangesDesc() {
		assertNull("The default last_update_user in match object should be null",
				plFolder.getLastUpdateAppUser());
		plFolder.setFolderDesc("xxx");
		assertEquals("The last_update_user should be [" +
				appUserName +"], because user1 has changed this match object",
				appUserName, plFolder.getLastUpdateAppUser());
	}

	public void testPropertyChangesStatus() {
		assertNull("The default last_update_user in match object should be null",
				plFolder.getLastUpdateAppUser());
		plFolder.setFolderStatus("xxx");
		assertEquals("The last_update_user should be [" +
				appUserName +"], because user1 has changed this match object",
				appUserName, plFolder.getLastUpdateAppUser());
	}
	public void testPropertyChangesBackupNo() {
		assertNull("The default last_update_user in match object should be null",
				plFolder.getLastUpdateAppUser());
		plFolder.setLastBackupNo(new Long(1L));
		assertEquals("The last_update_user should be [" +
				appUserName +"], because user1 has changed this match object",
				appUserName, plFolder.getLastUpdateAppUser());
	}

}
