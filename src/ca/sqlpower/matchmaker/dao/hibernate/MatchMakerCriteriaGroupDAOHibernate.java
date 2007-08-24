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

import ca.sqlpower.matchmaker.MatchMakerCriteriaGroup;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.dao.MatchCriteriaGroupDAO;

public class MatchMakerCriteriaGroupDAOHibernate extends AbstractMatchMakerDAOHibernate<MatchMakerCriteriaGroup> implements
		MatchCriteriaGroupDAO {

	public MatchMakerCriteriaGroupDAOHibernate(MatchMakerHibernateSession matchMakerSession) {
		super(matchMakerSession);
	}

	public Class<MatchMakerCriteriaGroup> getBusinessClass() {
		return MatchMakerCriteriaGroup.class;
	}
	
	@Override
	public void delete(MatchMakerCriteriaGroup deleteMe) {
		
		MatchMakerObject parent = deleteMe.getParent();
		if (parent != null ){
			parent.removeChild(deleteMe);
		}
		super.delete(deleteMe);
		
	}

}
