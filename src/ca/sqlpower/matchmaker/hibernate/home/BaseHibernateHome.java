package ca.sqlpower.matchmaker.hibernate.home;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public abstract class BaseHibernateHome<T> {
	private static Logger logger = Logger.getLogger(BaseHibernateHome.class);
	private SessionFactory sessionFactory;

	/** @deprecated you have to use the constructor that takes a session factory. */
	public BaseHibernateHome() {
		throw new UnsupportedOperationException("No-args constructor is not supported");
	}

	public BaseHibernateHome(SessionFactory hibernateSessionFactory) {
		sessionFactory = hibernateSessionFactory;
	}

	public Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}

	public List<T> findAll() {
		logger.info("finding all for "+ getBusinessClass());
        try {
            List results = getCurrentSession()
                    .createCriteria(getBusinessClass())
                    .list();
            logger.debug("find all successful, result size: " + results.size());
            return results;
        }
        catch (RuntimeException re) {
            logger.error("find all failed", re);
            throw re;
        }
	}

	public abstract String getBusinessClass();

	/**
     * run the sql and commit all changes
     */
	public void flush(){
		Transaction tx = getCurrentSession().beginTransaction();

		getCurrentSession().flush();
		tx.commit();

	}
}
