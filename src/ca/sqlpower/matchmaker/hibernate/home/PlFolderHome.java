package ca.sqlpower.matchmaker.hibernate.home;
// Generated 19-Sep-2006 12:08:40 PM by Hibernate Tools 3.2.0.beta7


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.criterion.Example;

import ca.sqlpower.matchmaker.hibernate.PlFolder;

/**
 * Home object for domain model class PlFolder.
 * @see ca.sqlpower.matchmaker.hibernate.PlFolder
 * @author Hibernate Tools
 */
public class PlFolderHome  extends DefaultHome{

    private static final Log log = LogFactory.getLog(PlFolderHome.class);

    
    public void persist(PlFolder transientInstance) {
        log.debug("persisting PlFolder instance");
        try {
           getCurrentSession().persist(transientInstance);
            log.debug("persist successful");
        }
        catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }
    
    public void attachDirty(PlFolder instance) {
        log.debug("attaching dirty PlFolder instance");
        try {
           getCurrentSession().saveOrUpdate(instance);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void attachClean(PlFolder instance) {
        log.debug("attaching clean PlFolder instance");
        try {
           getCurrentSession().lock(instance, LockMode.NONE);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void delete(PlFolder persistentInstance) {
        log.debug("deleting PlFolder instance");
        try {
           getCurrentSession().delete(persistentInstance);
            log.debug("delete successful");
        }
        catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }
    
    public PlFolder merge(PlFolder detachedInstance) {
        log.debug("merging PlFolder instance");
        try {
            PlFolder result = (PlFolder)getCurrentSession()
                    .merge(detachedInstance);
            log.debug("merge successful");
            return result;
        }
        catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    
    public PlFolder findById( java.lang.String id) {
        log.debug("getting PlFolder instance with id: " + id);
        try {
            PlFolder instance = (PlFolder)getCurrentSession()
                    .get("ca.sqlpower.matchmaker.hibernate.PlFolder", id);
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
    
    public List<PlFolder> findMatchMakerFolders(){
    	   log.debug("finding PlFolders with Matches");
           try {
               List<PlFolder> results =getCurrentSession()
                       .createCriteria(getBusinessClass())
                       .list();               
              
               log.debug("find by Match Maker successful, result size: " + results.size());
               return results;
           }
           catch (RuntimeException re) {
               log.error("find by MatchMaker failed", re);
               throw re;
           }
    }
    
    public List findByExample(PlFolder instance) {
        log.debug("finding PlFolder instance by example");
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
		return "ca.sqlpower.matchmaker.hibernate.PlFolder";
	}
}

