package ca.sqlpower.matchmaker.dao.hibernate;

import java.io.File;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.matchmaker.util.HibernateUtil;

public class HibernateTestUtil {

	//	The password for the test database(sqlserver) plautotest: TIhR2Es0 for sqlserver
	//The password for test database(oracle) mm_test: cowmoo

	
	private static final SessionFactory SQLSERVER_SESSION_FACTORY =
        buildHibernateSessionFactory(getSqlServerDS());
    
    private static final SessionFactory ORACLE_SESSION_FACTORY =
        buildHibernateSessionFactory(getOracleDS());;

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
	 * Builds a session factory from a datasource
	 * @param ds an architect datasource with the following fields filled in:
	 * 			driverClass, pass,Url,userName,plSchema and plDbType
	 * @return a new hibernate session
	 */	
	private static SessionFactory buildHibernateSessionFactory(ArchitectDataSource ds) {
		Configuration cfg = new Configuration();
		SessionFactory sessionFactory = null;
		cfg.configure(new File("./hibernate/hibernate.cfg.xml")); // FIXME
		// doesn't
		// work in a
		// JAR
		
		// last-minute configuration overrides for stuff that can only be known
		// at runtime
//		cfg.setProperty("hibernate.connection.driver_class", ds
//				.getDriverClass());
//		cfg.setProperty("hibernate.connection.password", ds.getPass());
//		cfg.setProperty("hibernate.connection.url", ds.getUrl());
//		cfg.setProperty("hibernate.connection.username", ds.getUser());
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
	 * Get a new oracle session factory
	 * 
	 * @return a session to an oracle database
	 */
	public static SessionFactory getOracleSessionFactory(){
		return ORACLE_SESSION_FACTORY;
	}
	
	/**
	 * Get a new sqlserver session factory
	 * 
	 * @return a session to an oracle database
	 */
	public static SessionFactory getSqlServerSessionFactory(){
		return SQLSERVER_SESSION_FACTORY;
	}
}