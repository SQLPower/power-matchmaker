package ca.sqlpower.matchmaker.dao.hibernate;

import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.dao.PlFolderDAO;

public class PlFolderDAOHibernate extends AbstractMatchMakerDAOHibernate<PlFolder>
		implements PlFolderDAO {

	
		
	public PlFolderDAOHibernate(MatchMakerHibernateSession matchMakerSession) {
		super(matchMakerSession);
	}


	public Class<PlFolder> getBusinessClass() {
		return PlFolder.class;
	}


}
