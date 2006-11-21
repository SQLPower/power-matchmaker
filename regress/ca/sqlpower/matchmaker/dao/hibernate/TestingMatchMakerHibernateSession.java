package ca.sqlpower.matchmaker.dao.hibernate;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;

public class TestingMatchMakerHibernateSession implements MatchMakerHibernateSession {

    private static final Logger logger = Logger.getLogger(TestingMatchMakerHibernateSession.class);
    
    /**
     * The connections we've made in this test run.
     */
    private static final Map<ArchitectDataSource, Connection> connections =
        new HashMap<ArchitectDataSource, Connection>();
    
    private final ArchitectDataSource dataSource;
    private final SessionFactory hibernateSessionFactory;
    private final TestingConnection con;
    
    public TestingMatchMakerHibernateSession(ArchitectDataSource dataSource) throws RuntimeException {
        super();
        try {
            this.dataSource = dataSource;
            this.hibernateSessionFactory = HibernateTestUtil.buildHibernateSessionFactory(this.dataSource);
            if (connections.get(dataSource) == null) {
                System.out.println("*** Connecting to Database: "+dataSource);
                Driver driver = (Driver) Class.forName(dataSource.getDriverClass()).newInstance();
                if (!driver.acceptsURL(dataSource.getUrl())) {
                    throw new SQLException("Couldn't connect to database:\n"
                            +"JDBC Driver "+dataSource.getDriverClass()+"\n"
                            +"does not accept the URL "+dataSource.getUrl());
                }
                Properties connectionProps = new Properties();
                connectionProps.setProperty("user", dataSource.getUser());
                connectionProps.setProperty("password", dataSource.getPass());
                Connection mycon = driver.connect(dataSource.getUrl(), connectionProps);
                if (mycon == null) {
                    throw new SQLException("Couldn't connect to datasource " + dataSource +
                            " (driver returned null connection)");
                }
                connections.put(dataSource, mycon);
            }
            con = new TestingConnection(connections.get(dataSource));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Session openSession() {
        return hibernateSessionFactory.openSession(getConnection());
    }

    /**
     * Enables or disables the connection associated with this session.  This is useful for
     * testing that Hibernate is correctly configured to eagerly fetch the data we expect it to.
     * 
     * @param disabled
     */
    public void setConnectionDisabled(boolean disabled) {
        con.setDisabled(disabled);
    }
    
    public Connection getConnection() {
        return con;
    }


    ///////// Unimplemented MatchMakerHibernateSession methods are below this line //////////
    
    public String createNewUniqueName() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: TestingMatchMakerHibernateSession.createNewUniqueName()");
        return null;
    }

    public PlFolder findFolder(String foldername) {
        // TODO Auto-generated method stub
        logger.debug("Stub call: TestingMatchMakerHibernateSession.findFolder()");
        return null;
    }

    public String getAppUser() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: TestingMatchMakerHibernateSession.getAppUser()");
        return null;
    }

    public MatchMakerSessionContext getContext() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: TestingMatchMakerHibernateSession.getContext()");
        return null;
    }

    public <T extends MatchMakerObject> MatchMakerDAO<T> getDAO(Class<T> businessClass) {
        // TODO Auto-generated method stub
        logger.debug("Stub call: TestingMatchMakerHibernateSession.getDAO()");
        return null;
    }

    public String getDBUser() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: TestingMatchMakerHibernateSession.getDBUser()");
        return null;
    }

    public SQLDatabase getDatabase() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: TestingMatchMakerHibernateSession.getDatabase()");
        return null;
    }

    public List<PlFolder> getFolders() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: TestingMatchMakerHibernateSession.getFolders()");
        return null;
    }

    public Match getMatchByName(String name) {
        // TODO Auto-generated method stub
        logger.debug("Stub call: TestingMatchMakerHibernateSession.getMatchByName()");
        return null;
    }

    public Date getSessionStartTime() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: TestingMatchMakerHibernateSession.getSessionStartTime()");
        return null;
    }

    public boolean isThisMatchNameAcceptable(Match match, String name) {
        // TODO Auto-generated method stub
        logger.debug("Stub call: TestingMatchMakerHibernateSession.isThisMatchNameAcceptable()");
        return false;
    }

    public boolean isThisMatchNameAcceptable(String name) {
        // TODO Auto-generated method stub
        logger.debug("Stub call: TestingMatchMakerHibernateSession.isThisMatchNameAcceptable()");
        return false;
    }

    public long countMatchByName(String name) {
        // TODO Auto-generated method stub
        logger.debug("Stub call: TestingMatchMakerHibernateSession.countMatchByName()");
        return 0;
    }

}
