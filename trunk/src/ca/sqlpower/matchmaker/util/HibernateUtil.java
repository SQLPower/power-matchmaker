package ca.sqlpower.matchmaker.util;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import ca.sqlpower.architect.ArchitectDataSource;

/**
 * Some Utils to encapsulate common Hibernate operations.
 */
public class HibernateUtil {

	private static SessionFactory sessionFactory=null;

	private static final Log log = LogFactory.getLog(HibernateUtil.class);


	/**
	 * Get a Hibernate SessionFactory object, without reference to
	 * whether it is created, looked up, etc.
	 * @return
	 */
	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	public static SessionFactory getSessionFactory(ArchitectDataSource ds) {
		Configuration cfg = new Configuration();
		
		cfg.configure(new File("./hibernate/hibernate.cfg.xml"));
		cfg.setProperty("hibernate.connection.driver_class", ds.getDriverClass());
		cfg.setProperty("hibernate.connection.password", ds.getPass());
        cfg.setProperty("hibernate.connection.url", ds.getUrl());
        cfg.setProperty("hibernate.connection.username",ds.getUser());
        cfg.setProperty("hibernate.default_schema",ds.getPlSchema());
        cfg.setProperty("hibernate.dialect",plDbType2Dialect(ds.getPlDbType()));
        cfg.setProperty("hibernate.c3p0.min_size","5");
        cfg.setProperty("hibernate.c3p0.max_size","20");
        cfg.setProperty("hibernate.c3p0.timeout","1800");
        cfg.setProperty("hibernate.c3p0.max_statements","50");
        
        try {
			// Create the SessionFactory from hibernate.cfg.xml
			sessionFactory = cfg.buildSessionFactory();
		} catch (Throwable ex) {
			log.error("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
		return sessionFactory;

	}
	
    public static final ThreadLocal session = new ThreadLocal();

    public static Session currentSession() throws HibernateException {
        Session s = (Session) session.get();
        // Open a new Session, if this Thread has none yet
        if (s == null) {
            s = sessionFactory.openSession();
            session.set(s);
        }
        return s;
    }

    public static void closeSession() throws HibernateException {
        Session s = (Session) session.get();
        session.set(null);
        if (s != null)
            s.close();
    }
	
	public static String plDbType2Dialect(String plType){
		if (plType == null ) throw new IllegalArgumentException("No dialect for a null database");
		String dbString = plType.toLowerCase();
		if( dbString=="oracle") {
			return "org.hibernate.dialect.OracleDialect";
		} else if(dbString == "sql server"){
			return "org.hibernate.dialect.SQLServerDialect";
		} else if(dbString == "db2"){
			return "org.hibernate.dialect.DB2Dialect";
		} else if(dbString == "postgres"){
			return "org.hibernate.dialect.PostgreSQLDialect";
		} else if(dbString == "hsqldb"){
			return "org.hibernate.dialect.HSQLDialect";
		} else if(dbString == "derby"){
			return "org.hibernate.dialect.DerbyDialect";
		} else {
			return "org.hibernate.dialect.DerbyDialect";
		}
		
	}
}
