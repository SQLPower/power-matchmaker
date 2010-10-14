/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru.
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

package ca.sqlpower.matchmaker.dao.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.dao.ProjectDAO;

public class ProjectDAOXML implements ProjectDAO {
	
	private static final Logger logger = Logger.getLogger(ProjectDAOXML.class);
	
	/**
     * The source of XML data for reading. Will be null if this is a write-only
     * DAO instance.
     */
    private final InputStream in;
    
    /**
     * The session that all MatchMakerObjects read in from the XML file will be
     * associated with. Currently, this is null unless this DAO is set up for
     * reading.
     */
    private final MatchMakerSession session;

    
    public ProjectDAOXML(OutputStream out) {
		throw new UnsupportedOperationException("XML export is currently not functional");
    }

    /**
     * Creates a new read-only Project DAO. The save() method of this new DAO
     * instance will throw UnsupportedOperationException if called.
     * 
     * @param in
     *            The stream to read the XML project description from.
     */
    public ProjectDAOXML(MatchMakerSession session, InputStream in) {
        this.session = session;
        this.in = in;
    }

	@Override
	public long countProjectByName(String name) {
		throw new UnsupportedOperationException("XML export is currently not functional");
	}

	@Override
	public List<Project> findAllProjectsWithoutFolders() {
		throw new UnsupportedOperationException("XML export is currently not functional");
	}

	@Override
	public Project findByName(String name) {
		throw new UnsupportedOperationException("XML export is currently not functional");
	}

	@Override
	public Project findByOid(long oid) {
		throw new UnsupportedOperationException("XML export is currently not functional");
	}

	@Override
	public Set<String> getProjectNamesUsingResultTable(String dataSourceName,
			String catalogName, String schemaName, String tableName) {
		throw new UnsupportedOperationException("XML export is currently not functional");
	}

	@Override
	public boolean isThisProjectNameAcceptable(String name) {
		throw new UnsupportedOperationException("XML export is currently not functional");
	}

	@Override
	public void delete(Project deleteMe) {
		throw new UnsupportedOperationException("XML export is currently not functional");
	}

	@Override
	public List<Project> findAll() {
        if (in == null) {
            throw new UnsupportedOperationException("This is a write-only DAO instance");
        }
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            ProjectSAXHandler saxHandler = new ProjectSAXHandler(session);
            parser.parse(in, saxHandler);
            return saxHandler.getProjects();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

	@Override
	public Class<Project> getBusinessClass() {
		throw new UnsupportedOperationException("XML export is currently not functional");
	}

	@Override
	public void save(Project saveMe) {
		throw new UnsupportedOperationException("XML export is currently not functional");
	}
 
}
