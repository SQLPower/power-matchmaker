package ca.sqlpower.matchmaker.dao.hibernate;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.matchmaker.EnginePath;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.swingui.SwingSessionContextImpl;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PLSchemaException;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.util.UnknownFreqCodeException;
import ca.sqlpower.util.VersionFormatException;

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

    private static final Logger logger = Logger.getLogger(MatchMakerHibernateSessionContext.class);

    /**
     * The list of database connections that this session context knows about.  This
     * implementation uses the <blink><marquee>AWESOME</marquee></blink> pl.ini file
     * format for storing its connection infos.
     */
    private final DataSourceCollection plDotIni;
    
    /**
     * The location of the PL.INI file that populated the {@link #plDotIni} collection.
     */
    private final String plIniPath;
    
    /**
     * Creates a new session context that uses the Hibernate DAO's to interact with the PL Schema.
     * 
     * @param plIni The data source collection that this context will use.
     * @param plIniPath The file system location of the pl.ini file that plIni came from.
     */
    public MatchMakerHibernateSessionContext(DataSourceCollection plIni, String plIniPath) {
        this.plDotIni = plIni;
        this.plIniPath = plIniPath;
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.MatchMakerSessionContext#getDataSources()
     */
    public List<SPDataSource> getDataSources() {
        return plDotIni.getConnections();
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.MatchMakerSessionContext#createSession(ca.sqlpower.sql.SPDataSource, java.lang.String, java.lang.String)
     */
    public MatchMakerSession createSession(
            SPDataSource ds, String username, String password)
    throws PLSecurityException, SQLException, 
            VersionFormatException, PLSchemaException, ArchitectException {

        // We create a copy of the data source and change the userID and password
        //and use that for the login attempt.  We do not want to change the
        //default userID and password for the connection in here.
        SPDataSource tempDbSource = new SPDataSource(ds);
        tempDbSource.setUser(username);
        tempDbSource.setPass(password);

        try {
            return new MatchMakerHibernateSessionImpl(this, tempDbSource);
        } catch (UnknownFreqCodeException ex) {
            throw new RuntimeException("This user doesn't have a valid default Dashboard date frequency, so you can't log in?!", ex);
        }
    }

    public DataSourceCollection getPlDotIni() {
        return plDotIni;
    }

    public String getMatchEngineLocation() {
        EnginePath p = EnginePath.MATCHMAKER;
        if (plIniPath == null) {
            return null;
        }
        File plDotIniFile = new File(plIniPath);
        File programDir = plDotIniFile.getParentFile();
        File programPath = new File(programDir, p.getProgName());
        return programPath.toString();
    }
    
    public String getEmailEngineLocation() {
        EnginePath p = EnginePath.EMAILNOTIFICATION;
        if (plIniPath == null) {
            return null;
        }
        File plDotIniFile = new File(plIniPath);
        File programDir = plDotIniFile.getParentFile();
        File programPath = new File(programDir, p.getProgName());
        return programPath.toString();
    }
}
