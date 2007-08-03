package ca.sqlpower.matchmaker.dao.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.matchmaker.DBTestUtil;
import ca.sqlpower.matchmaker.util.HibernateUtil;

public class HibernateTestUtil {

	private static TestingMatchMakerHibernateSession TESTING_MATCH_MAKER_SQL_SERVER_HIBERNATE_SESSION = new TestingMatchMakerHibernateSession(DBTestUtil.getSqlServerDS());

    private static TestingMatchMakerHibernateSession TESTING_MATCH_MAKER_ORACLE_HIBERNATE_SESSION = new TestingMatchMakerHibernateSession(DBTestUtil.getOracleDS());

    /**
	 * Builds a session factory for the given data source, but this session factory will
     * not be able to connect to the database on its own.  All calls to openSession() will
     * have to include a Connection argument.
     * 
	 * @param ds an architect datasource with at least the following fields filled in:
	 * 			plSchema and plDbType
	 * @return a new hibernate session
	 */	
	static SessionFactory buildHibernateSessionFactory(SPDataSource ds) {
		Configuration cfg = new Configuration();
		SessionFactory sessionFactory = null;
        cfg.configure(ClassLoader.getSystemResource("ca/sqlpower/matchmaker/dao/hibernate/hibernate.cfg.xml"));
         
		// last-minute configuration overrides for stuff that can only be known
		// at runtime
		cfg.setProperty("hibernate.default_schema", ds.getPlSchema());
		cfg.setProperty("hibernate.show_sql", "true");
		cfg.setProperty("hibernate.jdbc.batch_size", "0");
		cfg.setProperty("hibernate.dialect", HibernateUtil.guessHibernateDialect(ds.getParentType()));
		
		// Create the SessionFactory from hibernate.cfg.xml
		sessionFactory = cfg.buildSessionFactory();
		return sessionFactory;
	}
    
	/**
	 * Get the singleton oracle hibernate match maker session
	 * <p>
     * WARNING: this method replaces the SQLDatabase on the session.  Therefore
     * it is not thread safe.
     * 
	 * @return a session to an oracle database
	 */
	public static TestingMatchMakerHibernateSession getOracleHibernateSession() throws Exception {
        TESTING_MATCH_MAKER_ORACLE_HIBERNATE_SESSION.setDatabase(new SQLDatabase());
		return TESTING_MATCH_MAKER_ORACLE_HIBERNATE_SESSION;
	}
	
	/**
	 * Get the singleton sqlserver hibernate match maker session
     * <p>
     * WARNING: this method replaces the SQLDatabase on the session.  Therefore
     * it is not thread safe.
	 * 
	 * @return a session to an sql server database
	 * @throws Exception 
	 */	public static TestingMatchMakerHibernateSession getSqlServerHibernateSession() throws Exception{
	     TESTING_MATCH_MAKER_SQL_SERVER_HIBERNATE_SESSION.setDatabase(new SQLDatabase());
	     return TESTING_MATCH_MAKER_SQL_SERVER_HIBERNATE_SESSION;
	}
}