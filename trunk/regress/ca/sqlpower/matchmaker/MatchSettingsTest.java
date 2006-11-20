package ca.sqlpower.matchmaker;

import java.util.Date;

public class MatchSettingsTest extends MatchMakerTestCase {

	MatchSettings ms;
	protected void setUp() throws Exception {
		super.setUp();
		ms = new MatchSettings();
	}

	@Override
	protected MatchMakerObject getTarget() {
		return ms;
	}

    public void testSetLastRunDateDefensive() {
        Date myDate = new Date();
        ms.setLastRunDate(myDate);
        assertEquals(myDate, ms.getLastRunDate());
        assertNotSame(myDate, ms.getLastRunDate());
    }
}
