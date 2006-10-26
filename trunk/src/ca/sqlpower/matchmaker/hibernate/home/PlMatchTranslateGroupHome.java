package ca.sqlpower.matchmaker.hibernate.home;
// Generated Oct 23, 2006 1:15:07 PM by Hibernate Tools 3.2.0.beta7


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Example;

import ca.sqlpower.matchmaker.hibernate.PlMatchTranslateGroup;

/**
 * Home object for domain model class PlMatchTranslateGroup.
 * @see ca.sqlpower.matchmaker.hibernate.PlMatchTranslateGroup
 * @author Hibernate Tools
 */
public class PlMatchTranslateGroupHome extends DefaultHome<PlMatchTranslateGroup> {

    private static final String CLASS_NAME = "ca.sqlpower.matchmaker.hibernate.PlMatchTranslateGroup";

	private static final Log log = LogFactory.getLog(PlMatchTranslateGroupHome.class);

        
    public void saveOrUpdate(PlMatchTranslateGroup instance) {
        log.debug("attaching dirty PlMatchTranslateGroup instance");
        try {
            getCurrentSession().saveOrUpdate(instance);
            log.debug("attach successful");
        }
        catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }
    

    
    public void delete(PlMatchTranslateGroup persistentInstance) {
        log.debug("deleting PlMatchTranslateGroup instance");
        try {
            getCurrentSession().delete(persistentInstance);
            log.debug("delete successful");
        }
        catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }

    
    public PlMatchTranslateGroup findById( java.math.BigDecimal id) {
        log.debug("getting PlMatchTranslateGroup instance with id: " + id);
        try {
            PlMatchTranslateGroup instance = (PlMatchTranslateGroup) getCurrentSession()
                    .get(CLASS_NAME, id);
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
    
    public List findByExample(PlMatchTranslateGroup instance) {
        log.debug("finding PlMatchTranslateGroup instance by example");
        try {
            List results = getCurrentSession()
                    .createCriteria(CLASS_NAME)
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
		return CLASS_NAME;
	} 
}

