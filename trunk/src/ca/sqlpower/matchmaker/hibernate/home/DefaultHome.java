package ca.sqlpower.matchmaker.hibernate.home;

import java.sql.Connection;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import ca.sqlpower.matchmaker.util.HibernateUtil;

public abstract class DefaultHome<t> {
	private static Logger logger = Logger.getLogger(DefaultHome.class);
	private Connection con;
	private SessionFactory sessionFactory;
	
	public DefaultHome(){
		sessionFactory = HibernateUtil.getRepositorySessionFactory();		
	}
	
	public DefaultHome(Connection con) {
		this();
		this.con = con;
	}
	
	 public Connection getCon() {
		return con;
	}

	public void setCon(Connection con) {
		this.con = con;
	}
	
	public Session getCurrentSession() {
		if (con == null) {
			if(logger.isDebugEnabled()) logger.debug("Using the default session from the sessionFactory");
			return HibernateUtil.primarySession();
			
		} 
		return null;
	}
	
	public List<t> findAll() {
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
	
	public void flush(){
		Transaction tx = getCurrentSession().beginTransaction();
		
		getCurrentSession().flush();
		tx.commit();
		
	}
}
