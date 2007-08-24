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
