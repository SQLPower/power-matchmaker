package ca.sqlpower.matchmaker.dao.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.matchmaker.util.HibernateUtil;

public class HibernateTestUtil {

	//	The password for the test database(sqlserver) plautotest: TIhR2Es0 for sqlserver
	//The password for test database(oracle) mm_test: cowmoo

	
	private static TestingMatchMakerHibernateSession TESTING_MATCH_MAKER_SQL_SERVER_HIBERNATE_SESSION = new TestingMatchMakerHibernateSession(HibernateTestUtil.getSqlServerDS());

    private static TestingMatchMakerHibernateSession TESTING_MATCH_MAKER_ORACLE_HIBERNATE_SESSION = new TestingMatchMakerHibernateSession(HibernateTestUtil.getOracleDS());

    public static ArchitectDataSource getSqlServerDS() { 
		/*
		 * Setup information for SQL Server
		 */
		final String ssUserName = "plautotest";
		final String ssPassword = "TIhR2Es0";
		final String ssUrl ="jdbc:microsoft:sqlserver://deepthought:1433;SelectMethod=cursor;DatabaseName=plautotest";
		
		ArchitectDataSource sqlServerDS = new ArchitectDataSource();
		sqlServerDS.setDriverClass("com.microsoft.jdbc.sqlserver.SQLServerDriver");
		sqlServerDS.setName("Test SQLServer");
		sqlServerDS.setUser(ssUserName);
		sqlServerDS.setPass(ssPassword);
		sqlServerDS.setPlDbType("sql server");
		sqlServerDS.setPlSchema("plautotest");
		sqlServerDS.setUrl(ssUrl);
		return sqlServerDS;
	}

	public static ArchitectDataSource getOracleDS() { 
		/*
		 * Setup information for Oracle
		 */
		final String oracleUserName = "mm_test";
		final String oraclePassword = "cowmoo";
		final String oracleUrl = "jdbc:oracle:thin:@arthur:1521:test";
		
		ArchitectDataSource oracleDataSource = new ArchitectDataSource();
		oracleDataSource.setDriverClass("oracle.jdbc.driver.OracleDriver");
		oracleDataSource.setName("Test Oracle");
      
		oracleDataSource.setUser(oracleUserName);
		oracleDataSource.setPass(oraclePassword);
		oracleDataSource.setPlDbType("ORACLE");
		oracleDataSource.setPlSchema("mm_test");
		oracleDataSource.setUrl(oracleUrl);
		return oracleDataSource;
	}
    
	/**
	 * Builds a session factory for the given data source, but this session factory will
     * not be able to connect to the database on its own.  All calls to openSession() will
     * have to include a Connection argument.
     * 
	 * @param ds an architect datasource with at least the following fields filled in:
	 * 			plSchema and plDbType
	 * @return a new hibernate session
	 */	
	static SessionFactory buildHibernateSessionFactory(ArchitectDataSource ds) {
		Configuration cfg = new Configuration();
		SessionFactory sessionFactory = null;
        cfg.configure(ClassLoader.getSystemResource("ca/sqlpower/matchmaker/dao/hibernate/hibernate.cfg.xml"));
         
		// last-minute configuration overrides for stuff that can only be known
		// at runtime
		cfg.setProperty("hibernate.default_schema", ds.getPlSchema());
		cfg.setProperty("hibernate.show_sql", "true");
		cfg.setProperty("hibernate.jdbc.batch_size", "0");
		cfg.setProperty("hibernate.dialect", HibernateUtil.plDbType2Dialect(ds.getPlDbType()));
		
		// Create the SessionFactory from hibernate.cfg.xml
        System.err.println("Creating a new Hibernate SessionFactory!");
		sessionFactory = cfg.buildSessionFactory();
		return sessionFactory;
	}
    
	/**
	 * Get the singleton oracle hibernate match maker session
	 * 
	 * @return a session to an oracle database
	 */
	public static TestingMatchMakerHibernateSession getOracleHibernateSession() throws Exception {
		return TESTING_MATCH_MAKER_ORACLE_HIBERNATE_SESSION;
	}
	
	/**
	 * Get the singleton sqlserver hibernate match maker session
	 * 
	 * @return a session to an sql server database
	 * @throws Exception 
	 */	public static TestingMatchMakerHibernateSession getSqlServerHibernateSession() throws Exception{
		return TESTING_MATCH_MAKER_SQL_SERVER_HIBERNATE_SESSION;
	}
}