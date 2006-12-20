package ca.sqlpower.matchmaker;

public class MatchMakerTranslateWordTest
	extends MatchMakerTestCase<MatchMakerTranslateWord> {

	final String appUserName = "test_user";
	MatchMakerTranslateWord target;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		target = new MatchMakerTranslateWord();
		MatchMakerSession session = new TestingMatchMakerSession();
		((TestingMatchMakerSession)session).setAppUser(appUserName);
		target.setSession(session);
	}

	@Override
	protected MatchMakerTranslateWord getTarget() {
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

    public void testAssertDoesNotAllowChildren(){
        assertFalse(target.allowsChildren());
    }

	public void testAddChild() {
		try {
			target.addChild(new TestingAbstractMatchMakerObject());
			fail("Translate word does not allow child!");
		} catch ( IllegalStateException e ) {
			// what we excepted
		}
	}


}
