package ca.sqlpower.matchmaker.dao.hibernate;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.dao.MatchMakerTranslateGroupDAO;

public class MatchMakerTranslateGroupDAOHibernate extends AbstractMatchMakerDAOHibernate<MatchMakerTranslateGroup> implements MatchMakerTranslateGroupDAO {
    static final Logger logger = Logger.getLogger(MatchMakerTranslateGroupDAOHibernate.class);
    
    public MatchMakerTranslateGroupDAOHibernate(MatchMakerHibernateSession matchMakerSession) {
		super(matchMakerSession);
	}

    @Override
    public void save(MatchMakerTranslateGroup saveMe) {
    	saveMe.syncChildrenSeqNo();
    	super.save(saveMe);
    }

	public Class<MatchMakerTranslateGroup> getBusinessClass() {
		return MatchMakerTranslateGroup.class;
	}

	public MatchMakerTranslateGroup findByName(String name) {
		Session session = getHibernateSession();
		Query query = session.createQuery("from MatchMakerTranslateGroup translateGroup where translateGroup.name = :name");
		query.setParameter("name", name);
		List matches = query.list();
		if (matches.size() == 0) {
			return null;
		} else if (matches.size() == 1) {
            MatchMakerTranslateGroup translateGroup = (MatchMakerTranslateGroup) matches.get(0);
			translateGroup.setSession(getMatchMakerSession());
			return translateGroup;
		} else {
			throw new IllegalStateException("More than one Translate Group with name \""+name+"\"");
		}
	}

}
