package ca.sqlpower.matchmaker.hibernate.home;
// Generated Sep 18, 2006 4:34:45 PM by Hibernate Tools 3.2.0.beta7


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.criterion.Example;

import ca.sqlpower.matchmaker.hibernate.PlMatchTranslate;

/**
 * Home object for domain model class PlMatchTranslate.
 * @see ca.sqlpower.matchmaker.hibernate.PlMatchTranslate
 * @author Hibernate Tools
 */
public class PlMatchTranslateHome extends DefaultHome<PlMatchTranslate> {

    private static final Log log = LogFactory.getLog(PlMatchTranslateHome.class);

    public void persist(PlMatchTranslate transientInstance) {
        log.debug("persisting PlMatchTranslate instance");
        try {
            getCurrentSession().persist(transientInstance);
            log.debug("persist successful");
        }
        catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }
    
    public void attachDirty(PlMatchTranslate instance) {
        log.debug("attaching dirty PlMatchTranslate instance");
        try {
            getCurrentSession().saveOrUpdate(instance);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void attachClean(PlMatchTranslate instance) {
        log.debug("attaching clean PlMatchTranslate instance");
        try {
            getCurrentSession().lock(instance, LockMode.NONE);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void delete(PlMatchTranslate persistentInstance) {
        log.debug("deleting PlMatchTranslate instance");
        try {
            getCurrentSession().delete(persistentInstance);
            log.debug("delete successful");
        }
        catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }
    
    public PlMatchTranslate merge(PlMatchTranslate detachedInstance) {
        log.debug("merging PlMatchTranslate instance");
        try {
            PlMatchTranslate result = (PlMatchTranslate) getCurrentSession()
                    .merge(detachedInstance);
            log.debug("merge successful");
            return result;
        }
        catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }
    
    public PlMatchTranslate findById(Long id) {
        log.debug("getting PlMatchTranslate instance with id: " + id);
        try {
            PlMatchTranslate instance = (PlMatchTranslate) getCurrentSession()
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
    
    public List findByExample(PlMatchTranslate instance) {
        log.debug("finding PlMatchTranslate instance by example");
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
		return "ca.sqlpower.matchmaker.hibernate.PlMatchTranslate";
	}
}

