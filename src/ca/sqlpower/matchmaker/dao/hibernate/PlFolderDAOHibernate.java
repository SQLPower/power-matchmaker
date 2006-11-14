package ca.sqlpower.matchmaker.dao.hibernate;

import org.hibernate.SessionFactory;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.dao.PlFolderDAO;

public class PlFolderDAOHibernate extends AbstractMatchMakerDAOHibernate<PlFolder>
		implements PlFolderDAO {

	
		
	public PlFolderDAOHibernate(SessionFactory sessionFactory, MatchMakerSession matchMakerSession) {
		super(sessionFactory,matchMakerSession);
	}


	public Class<PlFolder> getBusinessClass() {
		return PlFolder.class;
	}


}
