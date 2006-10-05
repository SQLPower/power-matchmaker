package ca.sqlpower.matchmaker.hibernate.home;
// Generated Sep 18, 2006 4:34:45 PM by Hibernate Tools 3.2.0.beta7


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.criterion.Example;

import ca.sqlpower.matchmaker.hibernate.PlMatchCriterion;

/**
 * Home object for domain model class PlMatchCriterion.
 * @see ca.sqlpower.matchmaker.hibernate.PlMatchCriterion
 * @author Hibernate Tools
 */
public class PlMatchCriteriaHome extends DefaultHome<PlMatchCriterion>{

    private static final Log log = LogFactory.getLog(PlMatchCriteriaHome.class);

    public void persist(PlMatchCriterion transientInstance) {
        log.debug("persisting PlMatchCriterion instance");
        try {
            getCurrentSession().persist(transientInstance);
            log.debug("persist successful");
        }
        catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }
    
    public void attachDirty(PlMatchCriterion instance) {
        log.debug("attaching dirty PlMatchCriterion instance");
        try {
            getCurrentSession().saveOrUpdate(instance);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void attachClean(PlMatchCriterion instance) {
        log.debug("attaching clean PlMatchCriterion instance");
        try {
            getCurrentSession().lock(instance, LockMode.NONE);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void delete(PlMatchCriterion persistentInstance) {
        log.debug("deleting PlMatchCriterion instance");
        try {
            getCurrentSession().delete(persistentInstance);
            log.debug("delete successful");
        }
        catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }
    
    public PlMatchCriterion merge(PlMatchCriterion detachedInstance) {
        log.debug("merging PlMatchCriterion instance");
        try {
            PlMatchCriterion result = (PlMatchCriterion) getCurrentSession()
                    .merge(detachedInstance);
            log.debug("merge successful");
            return result;
        }
        catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }
    
    public PlMatchCriterion findById( ca.sqlpower.matchmaker.hibernate.PlMatchCriterionId id) {
        log.debug("getting PlMatchCriterion instance with id: " + id);
        try {
            PlMatchCriterion instance = (PlMatchCriterion) getCurrentSession()
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
    
    public List findByExample(PlMatchCriterion instance) {
        log.debug("finding PlMatchCriterion instance by example");
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
		return "ca.sqlpower.matchmaker.hibernate.PlMatchCriterion";
	}
}

