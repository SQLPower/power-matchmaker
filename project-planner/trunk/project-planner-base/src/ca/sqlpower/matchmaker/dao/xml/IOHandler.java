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

package ca.sqlpower.matchmaker.dao.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import ca.sqlpower.matchmaker.Project;

public interface IOHandler {
    
    /**
     * Returns a list of sparsely populated project descriptions that the current 
     * user has access to. At least the name and oid fields will be available.
     */
    List<Project> createProjectList();
    
    /**
     * Returns an input stream where a single project definition can be loaded
     * (as a single XML document). The input stream is specific for the given
     * project.
     * 
     * @param project The project to load. It must have a non-null OID.
     */
    InputStream getInputStream(Project project);
    
    /**
     * Returns an output stream where a single project definition can be written
     * (as a single XML document). Don't try to write more than one XML document
     * to the returned stream; call this method again for a new output stream.
     * 
     * @param project The project you intend to save.
     */
    OutputStream createOutputStream(Project project);
    
    /**
     * Tells this IO Handler which DAO it belongs to. Normally this will be called
     * by the DAO itself.
     * 
     * @param dao The new parent DAO.
     */
    public void setDAO(ProjectDAOXML dao);

    /**
     * Performs the deletion of the given project.
     * 
     * @param project The project you intend to delete.
     */
    void delete(Project project);

	void savePermissions(long projectId, String string);
}
