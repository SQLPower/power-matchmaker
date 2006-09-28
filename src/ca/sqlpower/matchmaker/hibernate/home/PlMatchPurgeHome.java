package ca.sqlpower.matchmaker.hibernate.home;
// Generated Sep 18, 2006 4:34:45 PM by Hibernate Tools 3.2.0.beta7


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.criterion.Example;

import ca.sqlpower.matchmaker.hibernate.PlMatchPurge;

/**
 * Home object for domain model class PlMatchPurge.
 * @see ca.sqlpower.matchmaker.hibernate.PlMatchPurge
 * @author Hibernate Tools
 */
public class PlMatchPurgeHome extends DefaultHome<PlMatchPurge> {

    private static final Log log = LogFactory.getLog(PlMatchPurgeHome.class);

    public void persist(PlMatchPurge transientInstance) {
        log.debug("persisting PlMatchPurge instance");
        try {
            getCurrentSession().persist(transientInstance);
            log.debug("persist successful");
        }
        catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }
    
    public void attachDirty(PlMatchPurge instance) {
        log.debug("attaching dirty PlMatchPurge instance");
        try {
            getCurrentSession().saveOrUpdate(instance);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void attachClean(PlMatchPurge instance) {
        log.debug("attaching clean PlMatchPurge instance");
        try {
            getCurrentSession().lock(instance, LockMode.NONE);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void delete(PlMatchPurge persistentInstance) {
        log.debug("deleting PlMatchPurge instance");
        try {
            getCurrentSession().delete(persistentInstance);
            log.debug("delete successful");
        }
        catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }
    
    public PlMatchPurge merge(PlMatchPurge detachedInstance) {
        log.debug("merging PlMatchPurge instance");
        try {
            PlMatchPurge result = (PlMatchPurge) getCurrentSession()
                    .merge(detachedInstance);
            log.debug("merge successful");
            return result;
        }
        catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }
    
    public PlMatchPurge findById( java.lang.String id) {
        log.debug("getting PlMatchPurge instance with id: " + id);
        try {
            PlMatchPurge instance = (PlMatchPurge) getCurrentSession()
                    .get(getBusinessClass(), id);
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
    
    public List findByExample(PlMatchPurge instance) {
        log.debug("finding PlMatchPurge instance by example");
        try {
            List results = getCurrentSession()
                    .createCriteria(getBusinessClass())
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
    
    @Override
	public String getBusinessClass() {
		return "ca.sqlpower.matchmaker.hibernate.PlMatchPurge";
	}
}

