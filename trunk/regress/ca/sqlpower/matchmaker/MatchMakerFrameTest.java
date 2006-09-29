package ca.sqlpower.matchmaker;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.swingui.MatchMakerFrame;

public class MatchMakerFrameTest extends TestCase {

	private MatchMakerFrame mf;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mf = MatchMakerFrame.getMainInstance();
	}

	public void testMatchMakerFrame() {
		assertNotNull(mf.getArchitectSession());
		assertNotNull(mf.getArchitectSession().getUserSettings());
	}

}
