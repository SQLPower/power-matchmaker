package ca.sqlpower.matchmaker;

public class MatchSettingsTest extends MatchMakerTestCase {

	MatchSettings ms;
	protected void setUp() throws Exception {
		super.setUp();
		ms = new MatchSettings("Test User");
	}

	@Override
	protected MatchMakerObject<MatchMakerObject> getTarget() {
		return ms;
	}

}
