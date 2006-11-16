package ca.sqlpower.matchmaker.dao.hibernate;

import ca.sqlpower.matchmaker.dao.AbstractPlFolderDAOTestCase;
import ca.sqlpower.matchmaker.dao.PlFolderDAO;


public class PlFolderDAOOracleTest extends AbstractPlFolderDAOTestCase {
   
	@Override
	public PlFolderDAO getDataAccessObject() throws Exception {
		return new PlFolderDAOHibernate(getSession());
	}
       
    @Override
    public MatchMakerHibernateSession getSession() throws Exception {
        return HibernateTestUtil.getOracleHibernateSession();
    }
}
