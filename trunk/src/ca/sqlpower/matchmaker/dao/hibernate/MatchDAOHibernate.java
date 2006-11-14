package ca.sqlpower.matchmaker.dao.hibernate;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.dao.MatchDAO;

public class MatchDAOHibernate extends AbstractMatchMakerDAOHibernate<Match> implements
		MatchDAO {

	public MatchDAOHibernate(SessionFactory sessionFactory, MatchMakerSession matchMakerSession) {
		super(sessionFactory, matchMakerSession);
	}

	public List<Match> findAllMatchesWithoutFolders() {
	        try {
	            List<Match> results = getCurrentSession()
	                    .createCriteria(getBusinessClass()).add(Expression.isNull("folder"))
	                    .list();	            
	            return results;
	        }
	        catch (RuntimeException re) {
	            throw re;
	        }
	    } 

	public Class<Match> getBusinessClass() {
		return Match.class;
	}
}
