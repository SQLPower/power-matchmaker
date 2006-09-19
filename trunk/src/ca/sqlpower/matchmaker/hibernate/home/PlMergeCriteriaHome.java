package ca.sqlpower.matchmaker.hibernate.home;
// Generated Sep 18, 2006 4:34:45 PM by Hibernate Tools 3.2.0.beta7


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;

import ca.sqlpower.matchmaker.hibernate.PlMergeCriteria;
import ca.sqlpower.matchmaker.hibernate.PlMergeCriteriaId;

/**
 * Home object for domain model class PlMergeCriteria.
 * @see ca.sqlpower.matchmaker.hibernate.PlMergeCriteria
 * @author Hibernate Tools
 */
public class PlMergeCriteriaHome extends DefaultHome {

    private static final Log log = LogFactory.getLog(PlMergeCriteriaHome.class);

    public void persist(PlMergeCriteria transientInstance) {
        log.debug("persisting PlMergeCriteria instance");
        try {
            getCurrentSession().persist(transientInstance);
            log.debug("persist successful");
        }
        catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }
    
    public void attachDirty(PlMergeCriteria instance) {
        log.debug("attaching dirty PlMergeCriteria instance");
        try {
            getCurrentSession().saveOrUpdate(instance);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void attachClean(PlMergeCriteria instance) {
        log.debug("attaching clean PlMergeCriteria instance");
        try {
            getCurrentSession().lock(instance, LockMode.NONE);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void delete(PlMergeCriteria persistentInstance) {
        log.debug("deleting PlMergeCriteria instance");
        try {
            getCurrentSession().delete(persistentInstance);
            log.debug("delete successful");
        }
        catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }
    
    public PlMergeCriteria merge(PlMergeCriteria detachedInstance) {
        log.debug("merging PlMergeCriteria instance");
        try {
            PlMergeCriteria result = (PlMergeCriteria) getCurrentSession()
                    .merge(detachedInstance);
            log.debug("merge successful");
            return result;
        }
        catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }
    
    public PlMergeCriteria findById( ca.sqlpower.matchmaker.hibernate.PlMergeCriteriaId id) {
        log.debug("getting PlMergeCriteria instance with id: " + id);
        try {
            PlMergeCriteria instance = (PlMergeCriteria) getCurrentSession()
                    .get("ca.sqlpower.matchmaker.generated.PlMergeCriteria", id);
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
    
    public List findByExample(PlMergeCriteria instance) {
        log.debug("finding PlMergeCriteria instance by example");
        try {
            List results = getCurrentSession()
                    .createCriteria("ca.sqlpower.matchmaker.generated.PlMergeCriteria")
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

