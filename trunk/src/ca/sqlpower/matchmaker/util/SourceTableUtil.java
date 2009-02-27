/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;

/**
 * A utility class with methods used to fix a project if any changes has been
 * made to its source table.
 */
public class SourceTableUtil {

	private static final Logger logger = Logger.getLogger(SourceTableUtil.class);
	
	/**
	 * Checks the project's source table for changes and attempts to fix the
	 * project accordingly. If the source table cannot be found on the database,
	 * it just shows an error message because it's too hard to fix.
	 * 
	 * @param session
	 *            Used to find the source table on the database and display an
	 *            error message.
	 * @param project
	 *            The project to check and fix.
	 * @throws Exception
	 *             If anything went wrong in the process.
	 */
	public static void checkAndfixProject(MatchMakerSwingSession session, Project project) throws RefreshException {
		SQLTable sourceTable = project.getSourceTable();
		SQLObject sourceTableContainer = sourceTable.getParent();
		SQLDatabase db = sourceTable.getParentDatabase();
		
		project.startCompoundEdit();
		session.setSelectNewChild(false);
		try {
		    db.refresh();

		    if (!sourceTableContainer.getChildren().contains(sourceTable)) {
		        throw new RefreshException(
		                project,
		                "The source table " + sourceTable.getName() + 
		        " is no longer in the database");
		    }


		    // TODO examine current unique index and see if it corresponds with something in the table
		    //   - recreate result table if PK has changed
		    
		    // iterate over the input (and output for cleanse) steps of each munge process, and fix them
			
			
			session.save(project);
		} catch (SQLObjectException ex) {
		    throw new RefreshException(
		            project,
		            "Refresh failed because of a problem accessing the source database",
		            ex);
		} finally {
			project.endCompoundEdit();
			session.setSelectNewChild(true);
		}
	}

}
