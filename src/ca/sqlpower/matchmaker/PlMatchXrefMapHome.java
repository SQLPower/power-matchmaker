package ca.sqlpower.matchmaker;
// Generated Sep 18, 2006 4:34:45 PM by Hibernate Tools 3.2.0.beta7


import java.util.List;
import javax.naming.InitialContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;

/**
 * Home object for domain model class PlMatchXrefMap.
 * @see ca.sqlpower.matchmaker.PlMatchXrefMap
 * @author Hibernate Tools
 */
public class PlMatchXrefMapHome {

    private static final Log log = LogFactory.getLog(PlMatchXrefMapHome.class);

    private final SessionFactory sessionFactory = getSessionFactory();
    
    protected SessionFactory getSessionFactory() {
        try {
            return (SessionFactory) new InitialContext().lookup("SessionFactory");
        }
        catch (Exception e) {
            log.error("Could not locate SessionFactory in JNDI", e);
            throw new IllegalStateException("Could not locate SessionFactory in JNDI");
        }
    }
    
    public void persist(PlMatchXrefMap transientInstance) {
        log.debug("persisting PlMatchXrefMap instance");
        try {
            sessionFactory.getCurrentSession().persist(transientInstance);
            log.debug("persist successful");
        }
        catch (RuntimeException re) {
            log.error("persist failed", re);
            throw re;
        }
    }
    
    public void attachDirty(PlMatchXrefMap instance) {
        log.debug("attaching dirty PlMatchXrefMap instance");
        try {
            sessionFactory.getCurrentSession().saveOrUpdate(instance);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void attachClean(PlMatchXrefMap instance) {
        log.debug("attaching clean PlMatchXrefMap instance");
        try {
            sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    
    public void delete(PlMatchXrefMap persistentInstance) {
        log.debug("deleting PlMatchXrefMap instance");
        try {
            sessionFactory.getCurrentSession().delete(persistentInstance);
            log.debug("delete successful");
        }
        catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }
    
    public PlMatchXrefMap merge(PlMatchXrefMap detachedInstance) {
        log.debug("merging PlMatchXrefMap instance");
        try {
            PlMatchXrefMap result = (PlMatchXrefMap) sessionFactory.getCurrentSession()
                    .merge(detachedInstance);
            log.debug("merge successful");
            return result;
        }
        catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }
    
    public PlMatchXrefMap findById( ca.sqlpower.matchmaker.PlMatchXrefMapId id) {
        log.debug("getting PlMatchXrefMap instance with id: " + id);
        try {
            PlMatchXrefMap instance = (PlMatchXrefMap) sessionFactory.getCurrentSession()
                    .get("ca.sqlpower.matchmaker.generated.PlMatchXrefMap", id);
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
    
    public List findByExample(PlMatchXrefMap instance) {
        log.debug("finding PlMatchXrefMap instance by example");
        try {
            List results = sessionFactory.getCurrentSession()
                    .createCriteria("ca.sqlpower.matchmaker.generated.PlMatchXrefMap")
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

