package ca.sqlpower.matchmaker.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import ca.sqlpower.architect.ArchitectDataSource;


/**
 * Some Utils to encapsulate common Hibernate operations.
 */
public class HibernateUtil {

	/**
	 * Commons logger for this class.
	 */
	private static final Logger log = Logger.getLogger(HibernateUtil.class);

	/**
	 * The key under which the PL repository database session is stored in the session map.
	 */
	private static final String KEY_REPOSITORY_SESSION_FACTORY =
		"ca.sqlpower.matchmaker.util.HibernateUtil.KEY_REPOSITORY_SESSION_FACTORY";

	private static final Map<String,SessionFactory> sessionFactories = new HashMap<String,SessionFactory>();

	/**
	 * The Hibernate session related to all work in the metadata repository (pl schema).
	 */
	private static Session repositorySession;


	/**
	 * Get a Hibernate SessionFactory object, without reference to
	 * whether it is created, looked up, etc.
	 * @return
	 */
	public static SessionFactory getRepositorySessionFactory() {
		return sessionFactories.get(KEY_REPOSITORY_SESSION_FACTORY);
	}

	public static SessionFactory createRepositorySessionFactory(
			ArchitectDataSource ds)
			throws HibernateException {
		repositorySession = createSessionFactory(ds, KEY_REPOSITORY_SESSION_FACTORY).openSession();
		return sessionFactories.get(KEY_REPOSITORY_SESSION_FACTORY);
	}

	/**
	 * Creates a Hibernate session factory with our configuration defaults
	 * from hibernate.cfg.xml and stores it in a static map in this class using
	 * the given key.
	 *
	 * <p>If you are trying to create a session factory for the metadata (PL Schema)
	 * repository, use {@link #createRepositorySessionFactory(ArchitectDataSource)}
	 * instead.
	 *
	 * @param ds
	 * @param key
	 * @return The session factory just created.  The return value is never null.
	 * @throws HibernateException if there is a problem creating the session factory
	 */
	public static SessionFactory createSessionFactory(
			ArchitectDataSource ds, String key)
			throws HibernateException {
		Configuration cfg = new Configuration();
		SessionFactory sessionFactory = null;

		cfg.configure(new File("./hibernate/hibernate.cfg.xml")); // FIXME doesn't work in a JAR

		// last-minute configuration overrides for stuff that can only be known at runtime
		cfg.setProperty("hibernate.connection.driver_class", ds.getDriverClass());
		cfg.setProperty("hibernate.connection.password", ds.getPass());
        cfg.setProperty("hibernate.connection.url", ds.getUrl());
        cfg.setProperty("hibernate.connection.username",ds.getUser());
        cfg.setProperty("hibernate.default_schema",ds.getPlSchema());
        String plDbType2Dialect = plDbType2Dialect(ds.getPlDbType());
        if(log.isDebugEnabled()) {
        	cfg.setProperty("hibernate.show_sql","true");
        	cfg.setProperty("hibernate.jdbc.batch_size","0");
        }
		cfg.setProperty("hibernate.dialect",plDbType2Dialect);
        cfg.setProperty("hibernate.c3p0.min_size","1");
        cfg.setProperty("hibernate.c3p0.max_size","1");
        cfg.setProperty("hibernate.c3p0.timeout","0");
        cfg.setProperty("hibernate.c3p0.max_statements","0");

        // Create the SessionFactory from hibernate.cfg.xml
        sessionFactory = cfg.buildSessionFactory();
        sessionFactories.put(key, sessionFactory);

		return sessionFactory;
	}

    public static ThreadLocal session = new ThreadLocal();

    /**
     * Returns the repository session (the one that relates to the PL Schema).  Since there
     * is one session, and sessions aren't threadsafe, you can only call this method on
     * the Swing Event Dispatch Thread.
     */
    public static Session primarySession() {
    	if (!SwingUtilities.isEventDispatchThread()) {
    		throw new RuntimeException("You can only interact with the repository session from the Swing EDT");
    	}
    	if (repositorySession == null) {
    		throw new IllegalStateException(
    				"The repository session has not yet been initialised by a successful " +
    				"call to HibernateUtil.createRepositorySessionFactory().  Have a nice day.");
    	}
        return repositorySession;
    }

	public static String plDbType2Dialect(String plType){
		if (plType == null ) throw new IllegalArgumentException("No dialect for a null database");
		String dbString = plType.toLowerCase();

		if( dbString.equals("oracle")) {
			return "org.hibernate.dialect.OracleDialect";
		} else if (dbString.equals("sql server")) {
			return "org.hibernate.dialect.SQLServerDialect";
		} else if (dbString.equals("db2")) {
			return "org.hibernate.dialect.DB2Dialect";
		} else if (dbString.equals("postgres")) {
			return "org.hibernate.dialect.PostgreSQLDialect";
		} else if (dbString.equals("hsqldb")) {
			return "org.hibernate.dialect.HSQLDialect";
		} else if (dbString.equals("derby")) {
			return "org.hibernate.dialect.DerbyDialect";
		} else {
			return "org.hibernate.dialect.DerbyDialect";
		}

	}
}
