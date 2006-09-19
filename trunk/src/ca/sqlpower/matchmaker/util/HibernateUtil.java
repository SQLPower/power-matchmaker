package ca.sqlpower.matchmaker.util;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.impl.SessionFactoryImpl;

import ca.sqlpower.matchmaker.hibernate.home.DefParamHome;

/**
 * Some Utils to encapsulate common Hibernate operations.
 */
public class HibernateUtil {

	private static final SessionFactory sessionFactory;

	private static final Log log = LogFactory.getLog(HibernateUtil.class);

	static {
		try {
			// Create the SessionFactory from hibernate.cfg.xml
			sessionFactory = new Configuration().configure(new File("./hibernate/hibernate.cfg.xml"))
					.buildSessionFactory();
		} catch (Throwable ex) {
			log.error("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	/**
	 * Get a Hibernate SessionFactory object, without reference to
	 * whether it is created, looked up, etc.
	 * @return
	 */
	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
}
