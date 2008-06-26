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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.FolderParent;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TranslateGroupParent;
import ca.sqlpower.matchmaker.WarningListener;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.matchmaker.dao.xml.IOHandler;
import ca.sqlpower.matchmaker.dao.xml.MatchMakerXMLSession;
import ca.sqlpower.matchmaker.dao.xml.ProjectDAOXML;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.util.Version;

/**
 * A simple session that does the bare minimum to load a project from xml. The only
 * useful function is to refresh a given project that has been added to the IOHandler.
 */
public class ProjectPlannerServerSession implements MatchMakerSession {

private static final Logger logger = Logger.getLogger(MatchMakerXMLSession.class);
    
    private final MatchMakerSessionContext context;
    private final FolderParent folderParent = new FolderParent(this);
    private final PlFolder<Project> defaultFolder = new PlFolder<Project>();
    private List<WarningListener> warningListeners = new ArrayList<WarningListener>();
    private String dbUser = "SQL Power Person";

    private final IOHandler ioHandler = new ServerIOHandler();
    private final ProjectDAOXML projectDAO = new ProjectDAOXML(this, ioHandler);
    
    public ProjectPlannerServerSession(MatchMakerSessionContext context) {
        this.context = context;
    }
    
    /**
     * Logs the warning to the log4j logger at WARN level as well as telling all the
     * warning listeners about the warning.
     */
    public void handleWarning(String message) {
        logger.warn("handleWarning: received warning message: "+message);
        synchronized (warningListeners) {
            for (int i = warningListeners.size()-1; i >= 0; i--) {
                warningListeners.get(i).handleWarning(message);
            }
        }
    }

    public void addWarningListener(WarningListener l) {
        synchronized (warningListeners) {
            warningListeners.add(l);
        }
    }

    public void removeWarningListener(WarningListener l) {
        synchronized (warningListeners) {
            warningListeners.remove(l);
        }
    }


    public boolean canSelectTable(SQLTable table) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public long countProjectByName(String name) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String createNewUniqueName() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public PlFolder<Project> findFolder(String foldername) {
        for (PlFolder<Project> folder : folderParent.getChildren()) {
        	if (folder.getName().equals(foldername)) {
        		return folder;
        	}
        }
        return null;
    }

    public SQLTable findPhysicalTableByName(String catalog, String schema, String tableName) throws ArchitectException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public SQLTable findPhysicalTableByName(String spDataSourceName, String catalog, String schema, String tableName) throws ArchitectException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String getAppUser() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String getAppUserEmail() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public FolderParent getBackupFolderParent() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Connection getConnection() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public MatchMakerSessionContext getContext() {
        return context;
    }

    public FolderParent getCurrentFolderParent() {
        return folderParent;
    }

    public <T extends MatchMakerObject> MatchMakerDAO<T> getDAO(Class<T> businessClass) {
        if (businessClass == Project.class) {
            return (MatchMakerDAO<T>) projectDAO;
        } else {
            return null;
        }
    }

    public String getDBUser() {
        return dbUser;
    }

    public SQLDatabase getDatabase() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public SQLDatabase getDatabase(SPDataSource dataSource) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public PlFolder<Project> getDefaultPlFolder() {
        return defaultFolder;
    }

    public Version getPLSchemaVersion() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Project getProjectByName(String name) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Date getSessionStartTime() {
    	throw new UnsupportedOperationException("Not implemented");
    }

    public TranslateGroupParent getTranslations() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean isThisProjectNameAcceptable(String name) {
        return true;
    }

    public boolean tableExists(String catalog, String schema, String tableName)
            throws ArchitectException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean tableExists(String spDataSourceName, String catalog,
            String schema, String tableName) throws ArchitectException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean tableExists(SQLTable table) throws ArchitectException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void save(MatchMakerObject mmo) {
    	throw new UnsupportedOperationException("Not implemented");        
    }

	public IOHandler getIoHandler() {
		return ioHandler;
	}
	
	public boolean savePermissions(Project project) {
		throw new UnsupportedOperationException("Not implemented yet");	
	}

	public void loadPermissions(Project project) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public boolean requestQuote(List<Project> projects, String comments) {
		throw new UnsupportedOperationException("Not implemented yet");
	}	
}
