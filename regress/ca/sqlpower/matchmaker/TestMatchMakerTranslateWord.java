package ca.sqlpower.matchmaker;

public class TestMatchMakerTranslateWord<C extends MatchMakerTranslateWord>
	extends MatchMakerTestCase<MatchMakerTranslateWord> {

	final String appUserName = "test_user";
	MatchMakerTranslateWord<C> target;
	private C child;


	@Override
	protected void setUp() throws Exception {
		super.setUp();
		target = new MatchMakerTranslateWord<C>(appUserName);
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

	public void testSetFrom() {
		checkNull();
		target.setFrom("x");
		checkAppUserName();
	}

	public void testSetTo() {
		checkNull();
		target.setTo("x");
		checkAppUserName();
	}

	public void testAddChild() {
		try {
			target.addChild(child);
			fail("Translate word does not allow child!");
		} catch ( IllegalStateException e ) {

		}
	}


}
