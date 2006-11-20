package ca.sqlpower.matchmaker.dao.hibernate;

import ca.sqlpower.matchmaker.MatchMakerCriteriaGroup;
import ca.sqlpower.matchmaker.dao.MatchCriteriaGroupDAO;

public class MatchMakerCriteriaGroupDAOHibernate extends AbstractMatchMakerDAOHibernate<MatchMakerCriteriaGroup> implements
		MatchCriteriaGroupDAO {

	public MatchMakerCriteriaGroupDAOHibernate(MatchMakerHibernateSession matchMakerSession) {
		super(matchMakerSession);
	}

	public Class<MatchMakerCriteriaGroup> getBusinessClass() {
		return MatchMakerCriteriaGroup.class;
	}

}
