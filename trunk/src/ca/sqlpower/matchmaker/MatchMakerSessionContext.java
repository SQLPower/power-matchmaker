package ca.sqlpower.matchmaker;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.DataSourceCollection;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.sql.PLSchemaException;
import ca.sqlpower.sql.SchemaVersionFormatException;

/**
 * A MatchMakerSessionContext holds global (well, systemwide) configuration and
 * preferences information, and when properly configured, is used for creating
 * MatchMaker sessions, which are a basic requirement for using the rest of the
 * MatchMaker API. If you were looking for the starting point, you've found it!
 * 
 * <p>The normal implementation of this interface is 
 * {@link ca.sqlpower.matchmaker.dao.hibernate.MatchMakerHibernateSessionContext},
 * so you probably want to create and configure one of those to get started.
 * 
 * @version $Id$
 */
public interface MatchMakerSessionContext {

    public List<ArchitectDataSource> getDataSources();

    /**
     * Creates a MatchMaker session object, which entails logging into a database
     * with a current PL Schema.  A MatchMaker session is the core object of the
     * MatchMaker application, so you will want to call this method early in your
     * usage of the MatchMaker API.
     * 
     * @param ds The data source that contains the PL Schema you want to connect to.
     * @param username The database user name to connect as.
     * @param password The database password for the given username.
     * @return A new MatchMaker session which is connected to the given database.
     * @throws PLSecurityException If there is no PL_USER entry for your database user, or
     * that user doesn't have permission to use the MatchMaker.
     * @throws SQLException If there are general database errors (can't connect, database
     * permission denied, the PL Schema is missing, etc).
     * @throws ArchitectException Because of cut and paste.
     * @throws IOException If any bootstrap init files are missing or unreadable.
     * @throws PLSchemaException If the version of the schema is not up to date or is missing
     * @throws SchemaVersionFormatException If the format of the version number is not in the form x.y.z
     */
    public MatchMakerSession createSession(ArchitectDataSource ds,
            String username, String password) throws PLSecurityException,
            SQLException, ArchitectException, IOException, SchemaVersionFormatException, PLSchemaException;

    /**
     * Returns the PlDotIni object that manages this context's list of data sources.
     * 
     * <p>We would much rather make the PlDotIni concept an interface (maybe called
     * DataSourceCollection) and implement that on this session context interface.
     * Such implementations could delegate to PlDotIni and the databases.xml stuff,
     * as well as a JNDI implementation.
     */
    public DataSourceCollection getPlDotIni();
    
    /**
     * The location of the matchmaker engine
     * @return the path to the engine
     */
    public String getMatchEngineLocation();
    
    /**
     * The location of the email sender engine
     * @return the path to the engine
     */
    public String getEmailEngineLocation();
}