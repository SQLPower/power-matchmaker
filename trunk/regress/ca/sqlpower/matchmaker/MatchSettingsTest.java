package ca.sqlpower.matchmaker;

public class MatchSettingsTest extends MatchMakerTestCase {

	MatchSettings ms;
	protected void setUp() throws Exception {
		super.setUp();
		ms = new MatchSettings();
	}

	@Override
	protected MatchMakerObject<MatchSettings, MatchMakerObject> getTarget() {
		return ms;
	}

}
