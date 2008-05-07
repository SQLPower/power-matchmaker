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

package ca.sqlpower.matchmaker.server;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.dao.xml.IOHandler;
import ca.sqlpower.matchmaker.dao.xml.ProjectDAOXML;

/**
 * A simple IOHandler that serves to return an input stream to a project.
 * The projects must be added beforehand with corresponding xml.
 */
public class ServerIOHandler implements IOHandler {
	
    private ProjectDAOXML dao;
    
    private Map<Long, String> projects = new HashMap<Long, String>();

	public OutputStream createOutputStream(Project project) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public List<Project> createProjectList() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public void delete(Project project) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public InputStream getInputStream(Project project) {
		Long id = project.getOid();
		String xml = projects.get(id);
		if (xml == null) {
			throw new RuntimeException("Project (id:" + id + ") not found");
		}
		return new ByteArrayInputStream(xml.getBytes());
	}

	public void setDAO(ProjectDAOXML dao) {
		this.dao = dao;
	}

	public void addProject(Long id, String xml) {
		projects.put(id, xml);
	}
	
	public void savePermissions(long projectId, String string) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public JSONObject loadPermissions(long projectId) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
