package ca.sqlpower.matchmaker;


public class MatchMakerSettingsTest extends MatchMakerTestCase {


	MatchMakerSettings mms;
	
	protected void setUp() throws Exception {
		super.setUp();
		mms = new MatchMakerSettings() {};
		mms.setAppUserName("user name");
		
		
	}

	@Override
	protected MatchMakerObject<MatchMakerObject> getTarget() {
		return mms;
	}

}
