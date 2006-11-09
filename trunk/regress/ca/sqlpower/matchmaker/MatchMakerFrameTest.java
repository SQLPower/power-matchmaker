package ca.sqlpower.matchmaker;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.swingui.MatchMakerMain;

public class MatchMakerFrameTest extends TestCase {

	private MatchMakerMain mf;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mf = MatchMakerMain.getMainInstance();
	}

	public void testMatchMakerFrame() {
		assertNotNull(mf.getArchitectSession());
		assertNotNull(mf.getArchitectSession().getUserSettings());
	}

}
