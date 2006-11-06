package ca.sqlpower.matchmaker;


public class TestMatchMakerSettings extends MatchMakerTestCase {


	MatchMakerSettings mms;
	
	protected void setUp() throws Exception {
		super.setUp();
		mms = new MatchMakerSettings("User") {};
		
		
	}

	@Override
	protected MatchMakerObject<MatchMakerObject> getTarget() {
		return mms;
	}

}
