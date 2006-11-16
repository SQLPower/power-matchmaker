package ca.sqlpower.matchmaker.dao.hibernate;

import ca.sqlpower.matchmaker.dao.AbstractPlMatchDAOTestCase;
import ca.sqlpower.matchmaker.dao.MatchDAO;


public class MatchDAOSQLServerTest extends AbstractPlMatchDAOTestCase {
    
    @Override
    protected void setUp() throws Exception {
//        Connection connection = HibernateTestUtil.getSqlServerSessionFactory().openSession().connection();
//        if (connection == null) throw new NullPointerException("Connection shouldn't be null");
//        ((TestingMatchMakerSession) session).setConnection(connection);
//        super.setUp();
    }
    
	@Override
	public MatchDAO getDataAccessObject() {
		return new MatchDAOHibernate(HibernateTestUtil.getSqlServerSessionFactory(),session);
	}
}
