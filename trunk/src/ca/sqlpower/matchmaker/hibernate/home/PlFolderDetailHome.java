package ca.sqlpower.matchmaker.hibernate.home;
// Generated 19-Sep-2006 12:08:40 PM by Hibernate Tools 3.2.0.beta7


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.criterion.Example;

import ca.sqlpower.matchmaker.hibernate.PlFolderDetail;

/**
 * Home object for domain model class PlFolderDetail.
 * @see ca.sqlpower.matchmaker.hibernate.PlFolderDetail
 * @author Hibernate Tools
 */
public class PlFolderDetailHome extends DefaultHome{

    private static final Log log = LogFactory.getLog(PlFolderDetailHome.class);

    
    
    public void persist(PlFolderDetail transientInstance) {
        log.debug("persisting PlFolderDetail instance");
        try {
           getCurrentSession().persist(transientInstance);
            log.debug("persist successful");
        }
        catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }
    
    public void attachDirty(PlFolderDetail instance) {
        log.debug("attaching dirty PlFolderDetail instance");
        try {
           getCurrentSession().saveOrUpdate(instance);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void attachClean(PlFolderDetail instance) {
        log.debug("attaching clean PlFolderDetail instance");
        try {
           getCurrentSession().lock(instance, LockMode.NONE);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void delete(PlFolderDetail persistentInstance) {
        log.debug("deleting PlFolderDetail instance");
        try {
           getCurrentSession().delete(persistentInstance);
            log.debug("delete successful");
        }
        catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }
    
    public PlFolderDetail merge(PlFolderDetail detachedInstance) {
        log.debug("merging PlFolderDetail instance");
        try {
            PlFolderDetail result = (PlFolderDetail)getCurrentSession()
                    .merge(detachedInstance);
            log.debug("merge successful");
            return result;
        }
        catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }
    
    public PlFolderDetail findById( ca.sqlpower.matchmaker.hibernate.PlFolderDetailId id) {
        log.debug("getting PlFolderDetail instance with id: " + id);
        try {
            PlFolderDetail instance = (PlFolderDetail)getCurrentSession()
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
    
    public List findByExample(PlFolderDetail instance) {
        log.debug("finding PlFolderDetail instance by example");
        try {
            List results =getCurrentSession()
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
		return "ca.sqlpower.matchmaker.hibernate.PlFolderDetail";
	}
}

