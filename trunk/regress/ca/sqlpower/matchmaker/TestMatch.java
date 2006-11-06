package ca.sqlpower.matchmaker;

public class TestMatch extends MatchMakerTestCase<MatchMakerFolder> {

	Match match;
	protected void setUp() throws Exception {
		super.setUp();
		match = new Match("Test User");
	}
	@Override
	protected MatchMakerObject<MatchMakerFolder> getTarget() {
		return match;
	}



}
