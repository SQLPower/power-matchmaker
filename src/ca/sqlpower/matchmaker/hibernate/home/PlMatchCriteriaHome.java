package ca.sqlpower.matchmaker.hibernate.home;
// Generated Sep 18, 2006 4:34:45 PM by Hibernate Tools 3.2.0.beta7


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.criterion.Example;

import ca.sqlpower.matchmaker.hibernate.PlMatchCriteria;

/**
 * Home object for domain model class PlMatchCriteria.
 * @see ca.sqlpower.matchmaker.hibernate.PlMatchCriteria
 * @author Hibernate Tools
 */
public class PlMatchCriteriaHome extends DefaultHome{

    private static final Log log = LogFactory.getLog(PlMatchCriteriaHome.class);

    public void persist(PlMatchCriteria transientInstance) {
        log.debug("persisting PlMatchCriteria instance");
        try {
            getCurrentSession().persist(transientInstance);
            log.debug("persist successful");
        }
        catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }
    
    public void attachDirty(PlMatchCriteria instance) {
        log.debug("attaching dirty PlMatchCriteria instance");
        try {
            getCurrentSession().saveOrUpdate(instance);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void attachClean(PlMatchCriteria instance) {
        log.debug("attaching clean PlMatchCriteria instance");
        try {
            getCurrentSession().lock(instance, LockMode.NONE);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void delete(PlMatchCriteria persistentInstance) {
        log.debug("deleting PlMatchCriteria instance");
        try {
            getCurrentSession().delete(persistentInstance);
            log.debug("delete successful");
        }
        catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }
    
    public PlMatchCriteria merge(PlMatchCriteria detachedInstance) {
        log.debug("merging PlMatchCriteria instance");
        try {
            PlMatchCriteria result = (PlMatchCriteria) getCurrentSession()
                    .merge(detachedInstance);
            log.debug("merge successful");
            return result;
        }
        catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }
    
    public PlMatchCriteria findById( ca.sqlpower.matchmaker.hibernate.PlMatchCriteriaId id) {
        log.debug("getting PlMatchCriteria instance with id: " + id);
        try {
            PlMatchCriteria instance = (PlMatchCriteria) getCurrentSession()
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
    
    public List findByExample(PlMatchCriteria instance) {
        log.debug("finding PlMatchCriteria instance by example");
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
		return "ca.sqlpower.matchmaker.hibernate.PlMatchCriteria";
	}
}

