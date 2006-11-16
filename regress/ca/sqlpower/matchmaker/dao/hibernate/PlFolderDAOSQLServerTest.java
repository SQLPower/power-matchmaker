package ca.sqlpower.matchmaker.dao.hibernate;

import ca.sqlpower.matchmaker.dao.AbstractPlFolderDAOTestCase;
import ca.sqlpower.matchmaker.dao.PlFolderDAO;


public class PlFolderDAOSQLServerTest extends AbstractPlFolderDAOTestCase {
    @Override
    protected void setUp() throws Exception {
//        Connection connection = HibernateTestUtil.getSqlServerSessionFactory().openSession().connection();
//        if (connection == null) throw new NullPointerException("Connection shouldn't be null");
//        ((TestingMatchMakerSession) session).setConnection(connection);
//        super.setUp();
    }
    
	@Override
	public PlFolderDAO getDataAccessObject() {
		return new PlFolderDAOHibernate(HibernateTestUtil.getSqlServerSessionFactory(),session);
	}

}
