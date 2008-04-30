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

/**
 * 
 */
package ca.sqlpower.matchmaker.dao.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Project;

public final class LocalFileIOHandler implements IOHandler {
    
    /**
     * just temporary for testing. real thing uses client/server communication.
     */
    private final File projectFile = new File(System.getProperty("user.home"), "mm-planner-file.xml");
    
    private static final Logger logger = Logger.getLogger(LocalFileIOHandler.class);

    private ProjectDAOXML dao;

    public InputStream getInputStream(Project project) {
        FileInputStream in;
        try {
            if (!projectFile.exists()) {
                throw new IllegalStateException("Couldn't update project info (file not found)");
            } else {
               in = new FileInputStream(projectFile);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        return in;
    }

    public OutputStream createOutputStream(Project p) {
        try {
            return new FileOutputStream(projectFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Project> createProjectList() {
        return new ArrayList<Project>();
    }

    public void setDAO(ProjectDAOXML dao) {
        this.dao = dao;
    }
}