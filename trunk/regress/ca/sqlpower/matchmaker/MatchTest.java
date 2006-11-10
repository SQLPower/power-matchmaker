package ca.sqlpower.matchmaker;

public class MatchTest extends MatchMakerTestCase<Match> {

	Match match;
	protected void setUp() throws Exception {
		super.setUp();
		match = new Match();
	}
	@Override
	protected Match getTarget() {
		return match;
	}

}
