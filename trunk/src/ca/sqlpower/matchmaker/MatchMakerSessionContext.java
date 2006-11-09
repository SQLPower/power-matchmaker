package ca.sqlpower.matchmaker;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.SessionFactory;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.PlDotIni;
import ca.sqlpower.matchmaker.util.HibernateUtil;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.util.UnknownFreqCodeException;

public class MatchMakerSessionContext {

	private PlDotIni plDotIni;
	private Map<ArchitectDataSource, SessionFactory> hibernateSessionFactories =
		new HashMap<ArchitectDataSource, SessionFactory>();

	public MatchMakerSessionContext() {
		super();
	}

	public List<ArchitectDataSource> getDataSources() {
		return plDotIni.getConnections();
	}

	public MatchMakerSession createSession(
			ArchitectDataSource ds, String username, String password) throws PLSecurityException, SQLException, ArchitectException {

		// We create a copy of the data source and change the userID and password
        //and use that instead for the loginWasSuccessful.  We do not want to change the
        //default userID and password for the connection in here.
        ArchitectDataSource tempDbSource = new ArchitectDataSource(ds);
        tempDbSource.setUser(username);
        tempDbSource.setPass(password);

        try {
        	return new MatchMakerSessionImpl(tempDbSource, getHibernateSessionFactory(tempDbSource));
        } catch (UnknownFreqCodeException ex) {
        	throw new RuntimeException("This user doesn't have a valid default Dashboard date frequency, so you can't log in?!", ex);
        }
	}

	/**
	 * Creates or retrieves a Hibernate SessionFactory object for the
	 * given database.  Never creates two SessionFactory objects for
	 * the same jdbcurl+user+password combination.
	 *
	 * @param ds The connection specification for the session factory you want.
	 * @return A Hibernate SessionFactory for the given data source.
	 */
	private synchronized SessionFactory getHibernateSessionFactory(ArchitectDataSource ds) {
		SessionFactory factory = hibernateSessionFactories.get(ds);
		if (factory == null) {
			factory = HibernateUtil.createRepositorySessionFactory(ds);
			hibernateSessionFactories.put(ds, factory);
		}
		return factory;
	}
}
