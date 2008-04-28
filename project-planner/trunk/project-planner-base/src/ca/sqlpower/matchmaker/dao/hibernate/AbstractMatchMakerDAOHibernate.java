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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.dao.hibernate;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;

/**
 * Abstract data access class that implements a basic findAll, save and delete.
 * 
 * save and delete get a new session, perform their function, then flush and close
 * the session.
 * 
 * findAll only gets a new session and closes it.  It dosn't flush the session.
 * 
 * @param <T> The type of object the DAO accesses
 */
public abstract class AbstractMatchMakerDAOHibernate<T extends MatchMakerObject> implements
		MatchMakerDAO<T> {
    
	MatchMakerHibernateSession matchMakerSession;

	public AbstractMatchMakerDAOHibernate(MatchMakerHibernateSession matchMakerSession) {
		this.matchMakerSession = matchMakerSession;
	}
	
	protected MatchMakerHibernateSession getMatchMakerSession() {
		return matchMakerSession;
	}
	
	protected Session getHibernateSession() {
	    return matchMakerSession.openSession();
	}

	public void delete(T deleteMe) {
		Session s = getHibernateSession();
		Transaction t = s.beginTransaction();
		try {
			s.delete(deleteMe);
			if (deleteMe.getParent() != null) {
				deleteMe.getParent().removeChild(deleteMe);
			}
			s.flush();
			t.commit();
		} catch (RuntimeException re) {
			t.rollback();
			throw re;
		} finally {
		}
	}

	public List<T> findAll() {
		Session s = getHibernateSession();
		try {
			List<T> results = s.createCriteria(getBusinessClass()).list();
			return results;
		} catch (RuntimeException re) {
			throw re;
		} finally {
		}
	}

	public void save(T saveMe) {
		Session s = getHibernateSession();
		Transaction t = s.beginTransaction();
		try {
			s.saveOrUpdate(saveMe);
			s.flush();
			t.commit();
		} catch (RuntimeException re) {
			t.rollback();
			throw re;
		} finally {
		}
	}

}
