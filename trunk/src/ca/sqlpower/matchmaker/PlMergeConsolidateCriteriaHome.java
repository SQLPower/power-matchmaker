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
 * Home object for domain model class PlMergeConsolidateCriteria.
 * @see ca.sqlpower.matchmaker.PlMergeConsolidateCriteria
 * @author Hibernate Tools
 */
public class PlMergeConsolidateCriteriaHome {

    private static final Log log = LogFactory.getLog(PlMergeConsolidateCriteriaHome.class);

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
    
    public void persist(PlMergeConsolidateCriteria transientInstance) {
        log.debug("persisting PlMergeConsolidateCriteria instance");
        try {
            sessionFactory.getCurrentSession().persist(transientInstance);
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
            sessionFactory.getCurrentSession().saveOrUpdate(instance);
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
            sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
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
            sessionFactory.getCurrentSession().delete(persistentInstance);
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
            PlMergeConsolidateCriteria result = (PlMergeConsolidateCriteria) sessionFactory.getCurrentSession()
                    .merge(detachedInstance);
            log.debug("merge successful");
            return result;
        }
        catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }
    
    public PlMergeConsolidateCriteria findById( ca.sqlpower.matchmaker.PlMergeConsolidateCriteriaId id) {
        log.debug("getting PlMergeConsolidateCriteria instance with id: " + id);
        try {
            PlMergeConsolidateCriteria instance = (PlMergeConsolidateCriteria) sessionFactory.getCurrentSession()
                    .get("ca.sqlpower.matchmaker.generated.PlMergeConsolidateCriteria", id);
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
            List results = sessionFactory.getCurrentSession()
                    .createCriteria("ca.sqlpower.matchmaker.generated.PlMergeConsolidateCriteria")
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

