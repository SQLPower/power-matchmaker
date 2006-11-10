package ca.sqlpower.matchmaker;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.hibernate.SessionFactory;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.matchmaker.dao.PlFolderDAO;
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
		PlFolderDAO folderDAO = new PlFolderDAOHibernate(hibernateSessionFactory,getAppUser());
		return folderDAO.findAll();
	}

}