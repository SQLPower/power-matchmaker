package ca.sqlpower.matchmaker.hibernate.home;
// Generated Sep 18, 2006 4:34:45 PM by Hibernate Tools 3.2.0.beta7


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.criterion.Example;

import ca.sqlpower.matchmaker.hibernate.PlMatchWordCount;

/**
 * Home object for domain model class PlMatchWordCount.
 * @see ca.sqlpower.matchmaker.hibernate.PlMatchWordCount
 * @author Hibernate Tools
 */
public class PlMatchWordCountHome extends DefaultHome{

    private static final Log log = LogFactory.getLog(PlMatchWordCountHome.class);

    public void persist(PlMatchWordCount transientInstance) {
        log.debug("persisting PlMatchWordCount instance");
        try {
            getCurrentSession().persist(transientInstance);
            log.debug("persist successful");
        }
        catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }
    
    public void attachDirty(PlMatchWordCount instance) {
        log.debug("attaching dirty PlMatchWordCount instance");
        try {
            getCurrentSession().saveOrUpdate(instance);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void attachClean(PlMatchWordCount instance) {
        log.debug("attaching clean PlMatchWordCount instance");
        try {
            getCurrentSession().lock(instance, LockMode.NONE);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void delete(PlMatchWordCount persistentInstance) {
        log.debug("deleting PlMatchWordCount instance");
        try {
            getCurrentSession().delete(persistentInstance);
            log.debug("delete successful");
        }
        catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }
    
    public PlMatchWordCount merge(PlMatchWordCount detachedInstance) {
        log.debug("merging PlMatchWordCount instance");
        try {
            PlMatchWordCount result = (PlMatchWordCount) getCurrentSession()
                    .merge(detachedInstance);
            log.debug("merge successful");
            return result;
        }
        catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }
    
    public PlMatchWordCount findById( ca.sqlpower.matchmaker.hibernate.PlMatchWordCountId id) {
        log.debug("getting PlMatchWordCount instance with id: " + id);
        try {
            PlMatchWordCount instance = (PlMatchWordCount) getCurrentSession()
                    .get("ca.sqlpower.matchmaker.hibernate.PlMatchWordCount", id);
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
    
    public List findByExample(PlMatchWordCount instance) {
        log.debug("finding PlMatchWordCount instance by example");
        try {
            List results = getCurrentSession()
                    .createCriteria("ca.sqlpower.matchmaker.hibernate.PlMatchWordCount")
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
}

