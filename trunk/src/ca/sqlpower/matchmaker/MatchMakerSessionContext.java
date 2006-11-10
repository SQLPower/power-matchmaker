package ca.sqlpower.matchmaker;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.PlDotIni;
import ca.sqlpower.security.PLSecurityException;

public interface MatchMakerSessionContext {

    public List<ArchitectDataSource> getDataSources();

    public MatchMakerSession createSession(ArchitectDataSource ds,
            String username, String password) throws PLSecurityException,
            SQLException, ArchitectException, IOException;

    /**
     * Returns the PlDotIni object that manages this context's list of data sources.
     * 
     * <p>We would much rather make the PlDotIni concept an interface (maybe called
     * DataSourceCollection) and implement that on this session context interface.
     * Such implementations could delegate to PlDotIni and the databases.xml stuff,
     * as well as a JNDI implementation.
     */
    public PlDotIni getPlDotIni();
}