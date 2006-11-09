package ca.sqlpower.matchmaker.dao.hibernate;

import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.dao.PlFolderDAO;


public class PlFolderDAOOracleTest extends AbstractDAOTestCase<PlFolder<MatchMakerObject>,PlFolderDAO> {
	int count=0;
	
	@Override
	public PlFolderDAO getDataAccessObject() {
		return new PlFolderDAOHibernate(HibernateTestUtil.getOracleSessionFactory());
	}

	@Override
	public PlFolder<MatchMakerObject> getNewObjectUnderTest() {
		count++;
		PlFolder<MatchMakerObject> plFolder = new PlFolder<MatchMakerObject>("TestUser");
		plFolder.setFolderName("test "+count);
		return plFolder;
	}

	
	
}
