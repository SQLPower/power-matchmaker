
package ca.sqlpower.matchmaker.dao.hibernate;

import ca.sqlpower.matchmaker.dao.AbstractPlMatchDAOTestCase;
import ca.sqlpower.matchmaker.dao.MatchDAO;


public class MatchDAOOracleTest extends AbstractPlMatchDAOTestCase {
    
       
	@Override
	public MatchDAO getDataAccessObject() throws Exception {
		return new MatchDAOHibernate(getSession());
	}

    @Override
    public MatchMakerHibernateSession getSession() throws Exception {
        return HibernateTestUtil.getOracleHibernateSession();
    }
}
