package ca.sqlpower.matchmaker;

public class MergeSettingsTest extends MatchMakerTestCase {

	MergeSettings ms;

	protected void setUp() throws Exception {
		super.setUp();
		ms = new MergeSettings();
	}

	@Override
	protected MatchMakerObject<MergeSettings, MatchMakerObject> getTarget() {
		return ms;
	}

}
