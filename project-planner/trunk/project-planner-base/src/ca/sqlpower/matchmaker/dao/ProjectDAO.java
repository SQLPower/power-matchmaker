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

package ca.sqlpower.matchmaker.dao;

import java.util.List;

import ca.sqlpower.matchmaker.Project;

/**
 * The Data access interface for project objects
 *
 * Remember to program to this interface rather than an implemenation
 */

public interface ProjectDAO extends MatchMakerDAO<Project> {


	/**
	 * Finds all the proejcts that don't have a specified folder
	 * The projects will be ordered by name.
	 *
	 * @return A list of projects or an empty list if there are no
	 * 			Projects that are folderless.
	 */
	public List<Project> findAllProjectsWithoutFolders();

	/**
	 * Finds the project having the given name (case sensitive).
	 * @param name The name of the project to look for
	 * @return The project object with the given name, or null if there
	 * is no such project.
	 */
	public Project findByName(String name);

	/**
	 * check to see if there is any project under given name
	 * @param name
	 * @return true if no project found under given name, false otherwise
	 */
	public boolean isThisProjectNameAcceptable(String name);

	/**
	 * count project entity by given name
	 * @param name
	 * @return number of project entity by given name
	 */
	public long countProjectByName(String name);

    /**
     * Sets all the properties and children of the given Project to the values
     * stored in the persistent object store.
     * 
     * @param project The project instance to refresh. Its oid must be non-null.
     */
    public void refresh(Project project);
	
}
