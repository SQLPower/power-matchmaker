package ca.sqlpower.matchmaker;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.hibernate.SessionFactory;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.matchmaker.dao.PlFolderDAO;
import ca.sqlpower.matchmaker.dao.hibernate.MatchDAOHibernate;
import ca.sqlpower.matchmaker.dao.hibernate.PlFolderDAOHibernate;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.security.PLSecurityManager;
import ca.sqlpower.security.PLUser;
import ca.sqlpower.util.UnknownFreqCodeException;

/**
 * An implementation of MatchMakerSession that uses Hibernate to
 * look up and store the business objects.
 */
public class MatchMakerSessionImpl implements MatchMakerSession {

    private final MatchMakerSessionContext context;
    private final SessionFactory hibernateSessionFactory;
	private final SQLDatabase database;
	private PLSecurityManager sm;
	private PLUser appUser;
	private String dbUser;
	private Date sessionStartTime;

	public MatchMakerSessionImpl(
            MatchMakerSessionContext context,
			ArchitectDataSource ds,
			SessionFactory hibernateSessionFactory)
		throws PLSecurityException, UnknownFreqCodeException,
				SQLException, ArchitectException {
        this.context = context;
		database = new SQLDatabase(ds);
		dbUser = ds.getUser();
		sm = new PLSecurityManager(database.getConnection(),
				 					dbUser.toUpperCase(),
				 					ds.getPass(),
                                    false);  // since this is a database login, we don't require correct app-level password
		appUser = sm.getPrincipal();
		sessionStartTime = new Date();
		this.hibernateSessionFactory = hibernateSessionFactory;
	}

    public MatchMakerSessionContext getContext() {
        return context;
    }

	public SQLDatabase getDatabase() {
		return database;
	}

	public String getAppUser() {
		return appUser.getUserId();
	}

	public String getDBUser() {
		return dbUser;
	}

	public Date getSessionStartTime() {
		return sessionStartTime;
	}

	public List<PlFolder> getFolders() {
		PlFolderDAO folderDAO = (PlFolderDAO)getDAO(PlFolder.class);
		return folderDAO.findAll();
	}

    public PlFolder findFolder(String foldername) {
        for (PlFolder folder : getFolders()){
            if (folder.getName().equals(foldername)) return folder;
        }
        return null;
    }

    public <T extends MatchMakerObject> MatchMakerDAO<T> getDAO(Class<T> businessClass) {
        if (businessClass == PlFolder.class) {
            return (MatchMakerDAO<T>) new PlFolderDAOHibernate(hibernateSessionFactory,this);
        } else if (businessClass == Match.class) {
            return (MatchMakerDAO<T>) new MatchDAOHibernate(hibernateSessionFactory,this);
        } else {
            throw new IllegalArgumentException("I don't know how to create a DAO for "+businessClass.getName());
        }
    }

    public Match getMatchByName(String name) {
    	List <Match> matches = getDAO(Match.class).findAll();
    	for ( Match m : matches ) {
    		if ( m.getName().equals(name) )
    			return m;
    	}
    	return null;
    }

	public boolean isThisMatchNameAcceptable(Match match, String name) {
		List <Match> matches = getDAO(Match.class).findAll();
    	for ( Match m : matches ) {
    		if ( !m.equals(match) && m.getName().equals(name) )
    			return false;
    	}
    	return true;
	}

    public String createNewUniqueName() {
        String name = "New Match";
        if (getMatchByName(name) == null) {
            return name;
        } else{
            int num=1;
            //Iterates until it finds a name that does not conflict with
            //existing match names
            while(getMatchByName(name+num) != null) {
                num++;
                name = "New Match" + num;
            }
            return name;
        }

    }

}