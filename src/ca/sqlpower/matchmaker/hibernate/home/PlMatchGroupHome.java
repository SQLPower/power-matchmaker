package ca.sqlpower.matchmaker.hibernate.home;
// Generated Sep 18, 2006 4:34:45 PM by Hibernate Tools 3.2.0.beta7


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.criterion.Example;

import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;

/**
 * Home object for domain model class PlMatchGroup.
 * @see ca.sqlpower.matchmaker.hibernate.PlMatchGroup
 * @author Hibernate Tools
 */
public class PlMatchGroupHome extends DefaultHome<PlMatchGroupHome>{

    private static final Log log = LogFactory.getLog(PlMatchGroupHome.class);
    
    public void persist(PlMatchGroup transientInstance) {
        log.debug("persisting PlMatchGroup instance");
        try {
            getCurrentSession().persist(transientInstance);
            log.debug("persist successful");
        }
        catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }
    
    public void attachDirty(PlMatchGroup instance) {
        log.debug("attaching dirty PlMatchGroup instance");
        try {
            getCurrentSession().saveOrUpdate(instance);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void attachClean(PlMatchGroup instance) {
        log.debug("attaching clean PlMatchGroup instance");
        try {
            getCurrentSession().lock(instance, LockMode.NONE);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void delete(PlMatchGroup persistentInstance) {
        log.debug("deleting PlMatchGroup instance");
        try {
            getCurrentSession().delete(persistentInstance);
            log.debug("delete successful");
        }
        catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }
    
    public PlMatchGroup merge(PlMatchGroup detachedInstance) {
        log.debug("merging PlMatchGroup instance");
        try {
            PlMatchGroup result = (PlMatchGroup) getCurrentSession()
                    .merge(detachedInstance);
            log.debug("merge successful");
            return result;
        }
        catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }
    
    public PlMatchGroup findById( ca.sqlpower.matchmaker.hibernate.PlMatchGroupId id) {
        log.debug("getting PlMatchGroup instance with id: " + id);
        try {
            PlMatchGroup instance = (PlMatchGroup) getCurrentSession()
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
    
    public List findByExample(PlMatchGroup instance) {
        log.debug("finding PlMatchGroup instance by example");
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
		return "ca.sqlpower.matchmaker.hibernate.PlMatchGroup";
	}
}

