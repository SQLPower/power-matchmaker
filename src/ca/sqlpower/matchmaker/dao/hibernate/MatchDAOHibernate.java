/*
 * Copyright (c) 2007, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.i 
 */

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
