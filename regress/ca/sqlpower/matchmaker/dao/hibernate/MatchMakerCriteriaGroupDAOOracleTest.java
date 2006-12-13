
package ca.sqlpower.matchmaker.dao.hibernate;

import ca.sqlpower.matchmaker.MatchMakerCriteriaGroup;
import ca.sqlpower.matchmaker.dao.AbstractMatchMakerCriteriaGroupDAOTestCase;
import ca.sqlpower.matchmaker.dao.MatchCriteriaGroupDAO;


public class MatchMakerCriteriaGroupDAOOracleTest extends AbstractMatchMakerCriteriaGroupDAOTestCase {
    
    private MatchMakerCriteriaGroup criteriaGroup;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        criteriaGroup = createNewObjectUnderTest();
    }
    @Override
	public void resetSession() throws Exception {
		((TestingMatchMakerHibernateSession) getSession()).resetSession();
	}
    
	@Override
	public MatchCriteriaGroupDAO getDataAccessObject() throws Exception {
		return new MatchMakerCriteriaGroupDAOHibernate(getSession());
	}

    @Override
    public MatchMakerHibernateSession getSession() throws Exception {
        return HibernateTestUtil.getOracleHibernateSession();
    }
}
