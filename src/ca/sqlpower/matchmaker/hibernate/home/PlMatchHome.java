package ca.sqlpower.matchmaker.hibernate.home;
// Generated Sep 18, 2006 4:34:45 PM by Hibernate Tools 3.2.0.beta7


import java.sql.Connection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;

import ca.sqlpower.matchmaker.hibernate.PlMatch;

/**
 * Home object for domain model class PlMatch.
 * @see ca.sqlpower.matchmaker.hibernate.PlMatch
 * @author Hibernate Tools
 */
public class PlMatchHome extends DefaultHome {

    private static final Log log = LogFactory.getLog(PlMatchHome.class);

    
    public PlMatchHome(Connection con) {
    	this.setCon(con);
	}

	public void persist(PlMatch transientInstance) {
        log.debug("persisting PlMatch instance");
        try {
            getCurrentSession().persist(transientInstance);
            log.debug("persist successful");
        }
        catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }
    
    public void attachDirty(PlMatch instance) {
        log.debug("attaching dirty PlMatch instance");
        try {
            getCurrentSession().saveOrUpdate(instance);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void attachClean(PlMatch instance) {
        log.debug("attaching clean PlMatch instance");
        try {
            getCurrentSession().lock(instance, LockMode.NONE);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void delete(PlMatch persistentInstance) {
        log.debug("deleting PlMatch instance");
        try {
            getCurrentSession().delete(persistentInstance);
            log.debug("delete successful");
        }
        catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }
    
    public PlMatch merge(PlMatch detachedInstance) {
        log.debug("merging PlMatch instance");
        try {
            PlMatch result = (PlMatch) getCurrentSession()
                    .merge(detachedInstance);
            log.debug("merge successful");
            return result;
        }
        catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }
    
    public PlMatch findById( java.lang.String id) {
        log.debug("getting PlMatch instance with id: " + id);
        try {
            PlMatch instance = (PlMatch) getCurrentSession()
                    .get("ca.sqlpower.matchmaker.hibernate.PlMatch", id);
            if (instance==null) {
                log.debug("get successful, no instance found");
            }
            else {
                log.debug("get successful, instance found");
            }
            return instance;
        }
        catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
    
    public List findByExample(PlMatch instance) {
        log.debug("finding PlMatch instance by example");
        try {
            List results = getCurrentSession()
                    .createCriteria("ca.sqlpower.matchmaker.hibernate.PlMatch")
                    .add(Example.create(instance))
            .list();
            log.debug("find by example successful, result size: " + results.size());
            return results;
        }
        catch (RuntimeException re) {
            log.error("find by example failed", re);
            throw re;
        }
    }

	public List<PlMatch> findAll() {
		log.debug("finding all PlMatch");
        try {
            List results = getCurrentSession()
                    .createCriteria("ca.sqlpower.matchmaker.hibernate.PlMatch")
                    .list();
            log.debug("find all successful, result size: " + results.size());
            return results;
        }
        catch (RuntimeException re) {
            log.error("find all failed", re);
            throw re;
        }
	} 
}

