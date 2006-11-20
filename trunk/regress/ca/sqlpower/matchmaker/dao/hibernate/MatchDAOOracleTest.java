
package ca.sqlpower.matchmaker.dao.hibernate;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.dao.AbstractPlMatchDAOTestCase;
import ca.sqlpower.matchmaker.dao.MatchDAO;


public class MatchDAOOracleTest extends AbstractPlMatchDAOTestCase {
    
    private Match match;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        match = createNewObjectUnderTest();
    }
    
	@Override
	public MatchDAO getDataAccessObject() throws Exception {
		return new MatchDAOHibernate(getSession());
	}

    @Override
    public MatchMakerHibernateSession getSession() throws Exception {
        return HibernateTestUtil.getOracleHibernateSession();
    }

    public void testMatchSettingsNotNullByDefault() {
        assertNotNull(match.getMatchSettings());
    }

    public void testMergeSettingsNotNullByDefault() {
        assertNotNull(match.getMergeSettings());
    }
}
