package ca.sqlpower.matchmaker;

public class TestMergeSettings extends MatchMakerTestCase {

	MergeSettings ms;
	
	protected void setUp() throws Exception {
		super.setUp();
		ms = new MergeSettings("Test user");
	}

	@Override
	protected MatchMakerObject<MatchMakerObject> getTarget() {
		
		return ms;
	}

}
