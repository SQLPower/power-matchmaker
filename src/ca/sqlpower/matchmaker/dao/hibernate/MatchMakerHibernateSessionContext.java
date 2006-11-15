package ca.sqlpower.matchmaker.dao.hibernate;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.SessionFactory;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.DataSourceCollection;
import ca.sqlpower.matchmaker.EnginePath;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.MatchMakerSessionImpl;
import ca.sqlpower.matchmaker.util.HibernateUtil;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.util.UnknownFreqCodeException;

/**
 * Basic configuration object for a MatchMaker launch.  If you want to create MatchMakerSession
 * objects which are backed by Hibernate DAOs, create and configure one of these contexts, then
 * ask it to make sessions for you!
 * 
 * <p>There's another version of this which is also tied into the Swing GUI.  In a GUI app,
 * that's the one you'll want: {@link SwingSessionContextImpl}.
 * 
 * @see ca.sqlpower.matchmaker.swingui.SwingSessionContextImpl
 */
public class MatchMakerHibernateSessionContext implements MatchMakerSessionContext {

    private final DataSourceCollection plDotIni;
    private final String plIniPath;
    private final Map<ArchitectDataSource, SessionFactory> hibernateSessionFactories =
        new HashMap<ArchitectDataSource, SessionFactory>();

    public MatchMakerHibernateSessionContext(DataSourceCollection plIni, String plIniPath) throws IOException {
        this.plDotIni = plIni;
        this.plIniPath = plIniPath;
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.MatchMakerSessionContext#getDataSources()
     */
    public List<ArchitectDataSource> getDataSources() {
        return plDotIni.getConnections();
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.MatchMakerSessionContext#createSession(ca.sqlpower.architect.ArchitectDataSource, java.lang.String, java.lang.String)
     */
    public MatchMakerSession createSession(
            ArchitectDataSource ds, String username, String password)
    throws PLSecurityException, SQLException, ArchitectException {

        // We create a copy of the data source and change the userID and password
        //and use that instead for the loginWasSuccessful.  We do not want to change the
        //default userID and password for the connection in here.
        ArchitectDataSource tempDbSource = new ArchitectDataSource(ds);
        tempDbSource.setUser(username);
        tempDbSource.setPass(password);

        try {
            return new MatchMakerSessionImpl(this, tempDbSource, getHibernateSessionFactory(tempDbSource));
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

    public DataSourceCollection getPlDotIni() {
        return plDotIni;
    }

    public String getEngineLocation() {
        EnginePath p = EnginePath.MATCHMAKER;
        String plDotIni = plIniPath;
        if (plDotIni == null) {
            return null;
        }
        File plDotIniFile = new File(plDotIni);
        File programDir = plDotIniFile.getParentFile();
        File programPath = new File(programDir, p.getProgName());
        return programPath.toString();
    }
}
