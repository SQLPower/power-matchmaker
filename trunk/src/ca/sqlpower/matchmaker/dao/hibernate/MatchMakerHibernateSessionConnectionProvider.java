package ca.sqlpower.matchmaker.dao.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.connection.ConnectionProvider;

import ca.sqlpower.matchmaker.MatchMakerSession;

/**
 * Provides connections to Hibernate by asking the session for connections.
 *
 */
public class MatchMakerHibernateSessionConnectionProvider
    implements ConnectionProvider {
    
    /**
     * The property name of the Session ID that we will use to associate this connection
     * provider with a particular MatchMakerSession instance.
     */
    // FIXME make package private and move session impl to this package
    public static final String PROP_SESSION_ID = "ca.sqlpower.matchmaker.dao.hibernate.SESSION_ID";

    private MatchMakerSession session;
    
    public void close() throws HibernateException {
        // does nothing
    }

    /**
     * Releases the connection that was obtained from getConnection().
     */
    public void closeConnection(Connection conn) throws SQLException {
        conn.close();
    }

    public void configure(Properties props) throws HibernateException {
        String mmSessionId = props.getProperty(PROP_SESSION_ID);
        if (mmSessionId == null) {
            throw new IllegalStateException("You have to provide the MatchMakerSession ID in the "+PROP_SESSION_ID+" property.");
        }
        session = MatchMakerHibernateSessionImpl.getSpecificInstance(mmSessionId);
        if (session == null) {
            throw new IllegalStateException("Couldn't find session with ID \""+mmSessionId+"\"");
        }
    }

    /**
     * Requests a connection from the matchmaker session.  (this is expected to
     * come from DBCP via SQLDatabase).
     */
    public Connection getConnection() throws SQLException {
        return session.getConnection();
    }

    public boolean supportsAggressiveRelease() {
        return false;
    }
}
