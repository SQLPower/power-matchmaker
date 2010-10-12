/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerConfigurationException;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.dao.hibernate.MatchMakerHibernateSession;
import ca.sqlpower.matchmaker.dao.hibernate.MatchMakerHibernateSessionContext;
import ca.sqlpower.matchmaker.dao.hibernate.RepositoryVersionException;
import ca.sqlpower.matchmaker.dao.xml.ProjectDAOXML;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sqlobject.SQLObjectException;

/**
 * This class contains a utility method for importing all of the projects
 * in a Hibernate repository into an existing project.
 */
public class ImportHibernateUtil {
	
	private static final Logger logger = Logger.getLogger(ImportHibernateUtil.class);
	
	public static void exportHibernateProjects(
			MatchMakerHibernateSessionContext context, JDBCDataSource ds, File exportDirectory) 
			throws RepositoryVersionException, PLSecurityException, SQLException, SQLObjectException, 
			MatchMakerConfigurationException, FileNotFoundException {
    	MatchMakerHibernateSession session = context.createSession(ds, ds.getUser(), ds.getPass());
    	for (PlFolder folder : session.getCurrentFolderParent().getChildren()) {
    		for (Object folderChild : folder.getChildren()) {
    			if (folderChild instanceof Project) {
    				Project project = (Project) folderChild;
    				//For some reason the hibernate session is set but not the
    				//core session.
    				project.setSession(session);
    				FileOutputStream out = null;
    				try {
    					File exportFile = new File(exportDirectory, project.getName() + ".xml");
    					int i = 0;
    					while (exportFile.exists()) {
    						i++;
    						exportFile = new File(exportDirectory, project.getName() + " " + i + ".xml");
    					}
						out = new FileOutputStream(exportFile);
    					ProjectDAOXML xmldao = new ProjectDAOXML(out);
    					xmldao.save(project);
    				} finally {
    					if (out != null) {
    						try {
    							out.close();
    						} catch (IOException e) {
    							logger.error("Exception when closing output stream. " +
    									"Not rethrowing this exception to prevent masking the root cause.", e);
    						}
    					}
    				}
    			} else {
    				throw new IllegalStateException("The folder " + folderChild + 
    						" does not contain projects, it contains " + folderChild.getClass() + 
    						" which is unrecognized.");
    			}
    		}
    	}
	}

}
