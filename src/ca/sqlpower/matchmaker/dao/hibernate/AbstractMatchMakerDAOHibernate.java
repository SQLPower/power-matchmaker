package ca.sqlpower.matchmaker.dao.hibernate;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import ca.sqlpower.matchmaker.dao.MatchMakerDAO;

/**
 * Abstract data access class that implements a basic findAll, save and delete.
 * 
 * save and delete get a new session, perform their function, then flush and close
 * the session.
 * 
 * findAll only gets a new session and closes it.  It dosn't flush the session.
 * 
 * @param <T> The type of object the DAO accesses
 */
public abstract class AbstractMatchMakerDAOHibernate<T> implements
		MatchMakerDAO<T> {
	SessionFactory sessionFactory;

	public AbstractMatchMakerDAOHibernate(SessionFactory sessionFactory) {
		super();
		this.sessionFactory = sessionFactory;
	}

	public void delete(T deleteMe) {
		try {
			Session s = getCurrentSession();
			s.delete(deleteMe);
			s.flush();
			s.close();
		} catch (RuntimeException re) {
			throw re;
		}
	}

	protected Session getCurrentSession() {
		return sessionFactory.openSession();

	}

	public List<T> findAll() {
		try {
			Session s = getCurrentSession();
			List results = s.createCriteria(getBusinessClass()).list();
			s.close();
			return results;
		} catch (RuntimeException re) {
			throw re;
		}
	}

	public void save(T saveMe) {
		try {
			Session s = getCurrentSession();
			s.saveOrUpdate(saveMe);
			s.flush();
			s.close();
		} catch (RuntimeException re) {
			throw re;
		}
	}

}
