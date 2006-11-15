package ca.sqlpower.matchmaker.dao.hibernate;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
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
public abstract class AbstractMatchMakerDAOHibernate<T extends MatchMakerObject> implements
		MatchMakerDAO<T> {
	MatchMakerSession matchMakerSession;
	SessionFactory sessionFactory;
	

	public AbstractMatchMakerDAOHibernate(SessionFactory sessionFactory,MatchMakerSession matchMakerSession) {
		this.matchMakerSession = matchMakerSession;
		this.sessionFactory = sessionFactory;
	}

	public void delete(T deleteMe) {
		Session s = getCurrentSession();
		Transaction t = s.beginTransaction();
		try {
			s.delete(deleteMe);
			s.flush();
			t.commit();
		} catch (RuntimeException re) {
			t.rollback();
			throw re;
		} finally {
			s.close();
		}
	}

	protected Session getCurrentSession() {
		return sessionFactory.openSession();
	}

	public List<T> findAll() {
		Session s = getCurrentSession();
		try {
			List<T> results = s.createCriteria(getBusinessClass()).list();
			for (T item: results){
				item.setSession(matchMakerSession);
			}
			return results;
		} catch (RuntimeException re) {
			throw re;
		} finally {
			s.close();
		}
	}

	public void save(T saveMe) {
		Session s = getCurrentSession();
		Transaction t = s.beginTransaction();
		try {
			s.saveOrUpdate(saveMe);
			s.flush();
			t.commit();
		} catch (RuntimeException re) {
			t.rollback();
			throw re;
		} finally {
			s.close();
		}
	}

}
