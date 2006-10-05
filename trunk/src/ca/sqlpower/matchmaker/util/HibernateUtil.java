package ca.sqlpower.matchmaker.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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

	public static final String primaryLogin = "primary login";
	public static final String auxLogin = "Auxilliary login";

	private static final Map<String,SessionFactory> sessionFactories=new HashMap<String,SessionFactory>();

	private static final Log log = LogFactory.getLog(HibernateUtil.class);


	/**
	 * Get a Hibernate SessionFactory object, without reference to
	 * whether it is created, looked up, etc.
	 * @return
	 */
	public static SessionFactory getSessionFactory() {
		return sessionFactories.get(primaryLogin);
	}

	public static SessionFactory getSessionFactoryImpl(ArchitectDataSource ds,String key) {
		return sessionFactories.get(key);

	}

	public static SessionFactory createSessionFactory(ArchitectDataSource ds,  String key){
		Configuration cfg = new Configuration();
		SessionFactory sessionFactory = null;

		cfg.configure(new File("./hibernate/hibernate.cfg.xml"));
		cfg.setProperty("hibernate.connection.driver_class", ds.getDriverClass());
		cfg.setProperty("hibernate.connection.password", ds.getPass());
        cfg.setProperty("hibernate.connection.url", ds.getUrl());
        cfg.setProperty("hibernate.connection.username",ds.getUser());
        cfg.setProperty("hibernate.default_schema",ds.getPlSchema());
        String plDbType2Dialect = plDbType2Dialect(ds.getPlDbType());
		cfg.setProperty("hibernate.dialect",plDbType2Dialect);
        cfg.setProperty("hibernate.c3p0.min_size","1");
        cfg.setProperty("hibernate.c3p0.max_size","1");
        cfg.setProperty("hibernate.c3p0.timeout","0");
        cfg.setProperty("hibernate.c3p0.max_statements","0");

        try {
			// Create the SessionFactory from hibernate.cfg.xml
			sessionFactory = cfg.buildSessionFactory();
			sessionFactories.put(key, sessionFactory);

			primarySession=sessionFactory.openSession();
		} catch (Throwable ex) {
			log.error("Initial SessionFactory creation failed." + ex);
			sessionFactory = null;
			throw new ExceptionInInitializerError(ex);
		}
		return sessionFactory;
	}

	private static Session primarySession=null;
    public static ThreadLocal session = new ThreadLocal();

    public static Session primarySession() throws HibernateException {
        Session s = (Session) session.get();
        // Open a new Session, if this Thread has none yet
        if (s == null) {

            s = primarySession;
            session.set(s);
        }
        return s;
    }

	public static String plDbType2Dialect(String plType){
		if (plType == null ) throw new IllegalArgumentException("No dialect for a null database");
		String dbString = plType.toLowerCase();

		if( dbString.equals("oracle")) {
			return "org.hibernate.dialect.OracleDialect";
		} else if(dbString.equals( "sql server")){
			return "org.hibernate.dialect.SQLServerDialect";
		} else if(dbString.equals( "db2")){
			return "org.hibernate.dialect.DB2Dialect";
		} else if(dbString.equals( "postgres")){
			return "org.hibernate.dialect.PostgreSQLDialect";
		} else if(dbString.equals( "hsqldb")){
			return "org.hibernate.dialect.HSQLDialect";
		} else if(dbString.equals( "derby")){
			return "org.hibernate.dialect.DerbyDialect";
		} else {
			return "org.hibernate.dialect.DerbyDialect";
		}

	}
}
