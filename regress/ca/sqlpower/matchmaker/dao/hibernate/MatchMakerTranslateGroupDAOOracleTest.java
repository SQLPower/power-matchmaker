
package ca.sqlpower.matchmaker.dao.hibernate;

import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.dao.AbstractMatchMakerTranslateGroupDAOTestCase;
import ca.sqlpower.matchmaker.dao.MatchMakerTranslateGroupDAO;


public class MatchMakerTranslateGroupDAOOracleTest extends AbstractMatchMakerTranslateGroupDAOTestCase{
    
    MatchMakerTranslateGroup translateGroup;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        translateGroup = createNewObjectUnderTest();
    }
    
	@Override
	public MatchMakerTranslateGroupDAO getDataAccessObject() throws Exception {
		return new MatchMakerTranslateGroupDAOHibernate(getSession());
	}

    @Override
    public MatchMakerHibernateSession getSession() throws Exception {
        return HibernateTestUtil.getOracleHibernateSession();
    }
    
    @Override
	public void resetSession() throws Exception {
		((TestingMatchMakerHibernateSession) getSession()).resetSession();
	}
}
