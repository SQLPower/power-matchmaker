/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */


package ca.sqlpower.matchmaker.dao.hibernate;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.MatchMakerTranslateWord;
import ca.sqlpower.matchmaker.dao.MatchMakerTranslateGroupDAO;

public class MatchMakerTranslateGroupDAOHibernate extends AbstractMatchMakerDAOHibernate<MatchMakerTranslateGroup> implements MatchMakerTranslateGroupDAO {
    static final Logger logger = Logger.getLogger(MatchMakerTranslateGroupDAOHibernate.class);
    
    public MatchMakerTranslateGroupDAOHibernate(MatchMakerHibernateSession matchMakerSession) {
		super(matchMakerSession);
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
			boolean tgChanged = false;
			for (Iterator<MatchMakerTranslateWord> iter = translateGroup.getChildren().iterator(); iter.hasNext();) {
				MatchMakerTranslateWord mmtw = iter.next();
				if (mmtw == null) {
					iter.remove();
					tgChanged = true;
				}
			}
			if (tgChanged) save(translateGroup);
			return translateGroup;
		} else {
			throw new IllegalStateException("More than one Translate Group with name \""+name+"\"");
		}
	}
	
	public MatchMakerTranslateGroup findByOID(long oid) {
		Session session = getHibernateSession();
		Query query = session.createQuery("from MatchMakerTranslateGroup translateGroup where translateGroup.oid = :oid");
		query.setParameter("oid", oid);
		List matches = query.list();
		if (matches.size() == 0) {
			return null;
		} else {
			MatchMakerTranslateGroup translateGroup = (MatchMakerTranslateGroup) matches.get(0);
			translateGroup.setSession(getMatchMakerSession());
			boolean tgChanged = false;
			for (MatchMakerTranslateWord tw : translateGroup.getChildren()) {
				if (tw == null) {
					translateGroup.removeChild(tw);
					tgChanged = true;
				}
			}
			if (tgChanged) save(translateGroup);
			return translateGroup;
		}
	}
	
	@Override
	public List<MatchMakerTranslateGroup> findAll() {
		List<MatchMakerTranslateGroup> results = super.findAll();
		boolean tgChanged;
		for (MatchMakerTranslateGroup tg : results) {
			tgChanged = false;
			for (MatchMakerTranslateWord tw : tg.getChildren()) {
				if (tw == null) {
					tg.removeChild(tw);
					tgChanged = true;
				}
			}
			if (tgChanged) save(tg);
		}
		return results;
	}
}
