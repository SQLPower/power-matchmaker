package ca.sqlpower.matchmaker.hibernate.home;
// Generated Sep 18, 2006 4:34:45 PM by Hibernate Tools 3.2.0.beta7


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.criterion.Example;

import ca.sqlpower.matchmaker.hibernate.PlMatchWordCountTemp;

/**
 * Home object for domain model class PlMatchWordCountTemp.
 * @see ca.sqlpower.matchmaker.hibernate.PlMatchWordCountTemp
 * @author Hibernate Tools
 */
public class PlMatchWordCountTempHome extends DefaultHome {

    private static final Log log = LogFactory.getLog(PlMatchWordCountTempHome.class);

    public void persist(PlMatchWordCountTemp transientInstance) {
        log.debug("persisting PlMatchWordCountTemp instance");
        try {
            getCurrentSession().persist(transientInstance);
            log.debug("persist successful");
        }
        catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }
    
    public void attachDirty(PlMatchWordCountTemp instance) {
        log.debug("attaching dirty PlMatchWordCountTemp instance");
        try {
            getCurrentSession().saveOrUpdate(instance);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void attachClean(PlMatchWordCountTemp instance) {
        log.debug("attaching clean PlMatchWordCountTemp instance");
        try {
            getCurrentSession().lock(instance, LockMode.NONE);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void delete(PlMatchWordCountTemp persistentInstance) {
        log.debug("deleting PlMatchWordCountTemp instance");
        try {
            getCurrentSession().delete(persistentInstance);
            log.debug("delete successful");
        }
        catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }
    
    public PlMatchWordCountTemp merge(PlMatchWordCountTemp detachedInstance) {
        log.debug("merging PlMatchWordCountTemp instance");
        try {
            PlMatchWordCountTemp result = (PlMatchWordCountTemp) getCurrentSession()
                    .merge(detachedInstance);
            log.debug("merge successful");
            return result;
        }
        catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }
    
    public PlMatchWordCountTemp findById( ca.sqlpower.matchmaker.hibernate.PlMatchWordCountTempId id) {
        log.debug("getting PlMatchWordCountTemp instance with id: " + id);
        try {
            PlMatchWordCountTemp instance = (PlMatchWordCountTemp) getCurrentSession()
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
    
    public List findByExample(PlMatchWordCountTemp instance) {
        log.debug("finding PlMatchWordCountTemp instance by example");
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
		return PlMatchWordCountTemp.class.toString();
	}
}

