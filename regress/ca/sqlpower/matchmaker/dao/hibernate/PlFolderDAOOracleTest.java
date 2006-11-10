package ca.sqlpower.matchmaker.dao.hibernate;

import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.dao.PlFolderDAO;


public class PlFolderDAOOracleTest extends AbstractDAOTestCase<PlFolder<MatchMakerObject>,PlFolderDAO> {
	int count=0;
	
	@Override
	public PlFolderDAO getDataAccessObject() {
		return new PlFolderDAOHibernate(HibernateTestUtil.getOracleSessionFactory(),session);
	}

	@Override
	public PlFolder<MatchMakerObject> getNewObjectUnderTest() {
		count++;
		PlFolder<MatchMakerObject> plFolder = new PlFolder<MatchMakerObject>();
		plFolder.setSession(this.session);
		plFolder.setFolderName("test "+count);
		return plFolder;
	}

	
	
}
