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

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.dao.ProjectDAO;

public class ProjectDAOHibernate extends AbstractMatchMakerDAOHibernate<Project> implements
		ProjectDAO {
    static final Logger logger = Logger.getLogger(ProjectDAOHibernate.class);
    
    public ProjectDAOHibernate(MatchMakerHibernateSession matchMakerSession) {
		super(matchMakerSession);
	}

	public List<Project> findAllProjectsWithoutFolders() {
	        try {
	            List<Project> results = getHibernateSession()
	                    .createCriteria(getBusinessClass()).add(Expression.isNull("folder"))
	                    .list();
	            for ( Project m : results ) {
	            	m.setSession(getMatchMakerSession());
	            }
	            return results;
	        }
	        catch (RuntimeException re) {
	            throw re;
	        }
	    }

	public Class<Project> getBusinessClass() {
		return Project.class;
	}

	public Project findByName(String name) {
		Session session = getHibernateSession();
		Query query = session.createQuery("from Project m where m.name = :name");
		query.setParameter("name", name);
		List projects = query.list();
		if (projects.size() == 0) {
			return null;
		} else if (projects.size() == 1) {
			Project project = (Project) projects.get(0);
			project.setSession(getMatchMakerSession());
			return project;
		} else {
			throw new IllegalStateException("More than one project with name \""+name+"\"");
		}
	}

	public boolean isThisProjectNameAcceptable(String name) {
		Long count = countProjectByName(name);
		return (count == 0);
	}

	public long countProjectByName(String name) {
		Session session = getHibernateSession();
		Query query = session.createQuery("select count(*) from Project m where m.name = :name");
		query.setParameter("name", name, Hibernate.STRING);
		Long count = (Long)query.uniqueResult();
		return count;
	}
	
	@Override
	public void save(Project saveMe) {
		if (saveMe.getParent() == null) {
			throw new RuntimeException("The project parent folder is null");
		}
		super.save(saveMe);
		
		// Saving the project clears out the columns of the result table
		// if the table was created the first time. This block of code
		// finds and sets the result table from the physical database 
		// that would have all the columns.
		try {
			MatchMakerSession session = saveMe.getSession();
			SQLTable resultTable = session.findPhysicalTableByName(saveMe.getResultTableCatalog(),
					saveMe.getResultTableSchema(), saveMe.getResultTableName());
			saveMe.setResultTable(resultTable);
		} catch (ArchitectException e) {
			throw new RuntimeException(e);
		}
	}
}
