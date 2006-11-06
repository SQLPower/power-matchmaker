package ca.sqlpower.matchmaker;

public class TestMatch extends MatchMakerTestCase<Match> {

	Match match;
	protected void setUp() throws Exception {
		super.setUp();
		match = new Match("Test User");
	}
	@Override
	protected Match getTarget() {
		return match;
	}

}
