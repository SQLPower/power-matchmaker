package ca.sqlpower.matchmaker.hibernate.home;
// Generated Sep 18, 2006 4:34:45 PM by Hibernate Tools 3.2.0.beta7


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.criterion.Example;

import ca.sqlpower.matchmaker.hibernate.PlMergeConsolidateCriteria;

/**
 * Home object for domain model class PlMergeConsolidateCriteria.
 * @see ca.sqlpower.matchmaker.hibernate.PlMergeConsolidateCriteria
 * @author Hibernate Tools
 */
public class PlMergeConsolidateCriteriaHome extends DefaultHome {

    private static final Log log = LogFactory.getLog(PlMergeConsolidateCriteriaHome.class);

    public void persist(PlMergeConsolidateCriteria transientInstance) {
        log.debug("persisting PlMergeConsolidateCriteria instance");
        try {
            getCurrentSession().persist(transientInstance);
            log.debug("persist successful");
        }
        catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }
    
    public void attachDirty(PlMergeConsolidateCriteria instance) {
        log.debug("attaching dirty PlMergeConsolidateCriteria instance");
        try {
            getCurrentSession().saveOrUpdate(instance);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void attachClean(PlMergeConsolidateCriteria instance) {
        log.debug("attaching clean PlMergeConsolidateCriteria instance");
        try {
            getCurrentSession().lock(instance, LockMode.NONE);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void delete(PlMergeConsolidateCriteria persistentInstance) {
        log.debug("deleting PlMergeConsolidateCriteria instance");
        try {
            getCurrentSession().delete(persistentInstance);
            log.debug("delete successful");
        }
        catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }
    
    public PlMergeConsolidateCriteria merge(PlMergeConsolidateCriteria detachedInstance) {
        log.debug("merging PlMergeConsolidateCriteria instance");
        try {
            PlMergeConsolidateCriteria result = (PlMergeConsolidateCriteria) getCurrentSession()
                    .merge(detachedInstance);
            log.debug("merge successful");
            return result;
        }
        catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }
    
    public PlMergeConsolidateCriteria findById( ca.sqlpower.matchmaker.hibernate.PlMergeConsolidateCriteriaId id) {
        log.debug("getting PlMergeConsolidateCriteria instance with id: " + id);
        try {
            PlMergeConsolidateCriteria instance = (PlMergeConsolidateCriteria) getCurrentSession()
                    .get("ca.sqlpower.matchmaker.hibernate.PlMergeConsolidateCriteria", id);
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
    
    public List findByExample(PlMergeConsolidateCriteria instance) {
        log.debug("finding PlMergeConsolidateCriteria instance by example");
        try {
            List results = getCurrentSession()
                    .createCriteria("ca.sqlpower.matchmaker.hibernate.PlMergeConsolidateCriteria")
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

