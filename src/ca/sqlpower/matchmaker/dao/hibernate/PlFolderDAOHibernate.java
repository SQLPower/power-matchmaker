package ca.sqlpower.matchmaker.dao.hibernate;

import org.hibernate.SessionFactory;

import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.dao.PlFolderDAO;

public class PlFolderDAOHibernate extends AbstractMatchMakerDAOHibernate<PlFolder>
		implements PlFolderDAO {

	
	public PlFolderDAOHibernate(SessionFactory factory) {
		super(factory);
	}

	
	public PlFolder findByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}


	public Class<PlFolder> getBusinessClass() {
		return PlFolder.class;
	}


}
