package ca.sqlpower.matchmaker.dao.hibernate;

import ca.sqlpower.matchmaker.dao.AbstractPlFolderDAOTestCase;
import ca.sqlpower.matchmaker.dao.PlFolderDAO;


public class PlFolderDAOOracleTest extends AbstractPlFolderDAOTestCase {
	@Override
	public PlFolderDAO getDataAccessObject() {
		return new PlFolderDAOHibernate(HibernateTestUtil.getOracleSessionFactory(),session);
	}
}
