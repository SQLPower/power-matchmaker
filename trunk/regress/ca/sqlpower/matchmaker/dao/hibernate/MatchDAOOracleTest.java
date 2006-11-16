
package ca.sqlpower.matchmaker.dao.hibernate;

import java.sql.Connection;

import ca.sqlpower.matchmaker.TestingMatchMakerSession;
import ca.sqlpower.matchmaker.dao.AbstractPlMatchDAOTestCase;
import ca.sqlpower.matchmaker.dao.MatchDAO;


public class MatchDAOOracleTest extends AbstractPlMatchDAOTestCase {
    
    
    @Override
    protected void setUp() throws Exception {
        Connection connection = HibernateTestUtil.getOracleSessionFactory().openSession().connection();
        if (connection == null) throw new NullPointerException("Connection shouldn't be null");
        ((TestingMatchMakerSession) session).setConnection(connection);
        super.setUp();
    }
    
	@Override
	public MatchDAO getDataAccessObject() {
		return new MatchDAOHibernate(HibernateTestUtil.getOracleSessionFactory(),session);
	}
}
