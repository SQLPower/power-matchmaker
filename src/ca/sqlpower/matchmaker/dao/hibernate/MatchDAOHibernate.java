package ca.sqlpower.matchmaker.dao.hibernate;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.dao.MatchDAO;

public class MatchDAOHibernate extends AbstractMatchMakerDAOHibernate<Match> implements
		MatchDAO {
    static final Logger logger = Logger.getLogger(MatchDAOHibernate.class);
    
    public MatchDAOHibernate(MatchMakerHibernateSession matchMakerSession) {
		super(matchMakerSession);
	}

	public List<Match> findAllMatchesWithoutFolders() {
	        try {
	            List<Match> results = getHibernateSession()
	                    .createCriteria(getBusinessClass()).add(Expression.isNull("folder"))
	                    .list();
	            for ( Match m : results ) {
	            	m.setSession(getMatchMakerSession());
	            }
	            return results;
	        }
	        catch (RuntimeException re) {
	            throw re;
	        }
	    }

	public Class<Match> getBusinessClass() {
		return Match.class;
	}

	public Match findByName(String name) {
		Session session = getHibernateSession();
		Query query = session.createQuery("from Match m where m.name = :name");
		query.setParameter("name", name);
		List matches = query.list();
		if (matches.size() == 0) {
			return null;
		} else if (matches.size() == 1) {
			Match match = (Match) matches.get(0);
			match.setSession(getMatchMakerSession());
			return match;
		} else {
			throw new IllegalStateException("More than one match with name \""+name+"\"");
		}
	}

	public boolean isThisMatchNameAcceptable(String name) {
		Long count = countMatchByName(name);
		return (count == 0);
	}

	public long countMatchByName(String name) {
		Session session = getHibernateSession();
		Query query = session.createQuery("select count(*) from Match m where m.name = :name");
		query.setParameter("name", name, Hibernate.STRING);
		Long count = (Long)query.uniqueResult();
		return count;
	}
	
	@Override
	public void save(Match saveMe) {
		if (saveMe.getParent() == null) {
			throw new RuntimeException("The match parent folder is null");
		}
		super.save(saveMe);
	}
}
