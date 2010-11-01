/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
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


package ca.sqlpower.matchmaker.swingui;

import java.io.File;
import java.sql.Connection;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.dao.upgrade.UpgradePersisterManager;
import ca.sqlpower.matchmaker.FolderParent;
import ca.sqlpower.matchmaker.MMRootNode;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TranslateGroupParent;
import ca.sqlpower.matchmaker.WarningListener;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.UserDefinedSQLType;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.DefaultUserPrompterFactory;
import ca.sqlpower.util.UserPrompterFactory;
import ca.sqlpower.util.Version;

/**
 * A true stub implementation. Every method does nothing (except log the call at DEBUG level).
 */
public class StubMatchMakerSession implements MatchMakerSession{

    private static final Logger logger = Logger.getLogger(StubMatchMakerSession.class);
    
    public PlFolder findFolder(String foldername) {
        logger.debug("Stub call: StubMatchMakerSession.findFolder()");
        return null;
    }

    public String getAppUser() {
        logger.debug("Stub call: StubMatchMakerSession.getAppUser()");
        return null;
    }

    public MatchMakerSessionContext getContext() {
        logger.debug("Stub call: StubMatchMakerSession.getContext()");
        return null;
    }

    public <T extends MatchMakerObject> MatchMakerDAO<T> getDAO(Class<T> businessClass) {
        logger.debug("Stub call: StubMatchMakerSession.getDAO()");
        return null;
    }

    public String getDBUser() {
        logger.debug("Stub call: StubMatchMakerSession.getDBUser()");
        return null;
    }

    public SQLDatabase getDatabase() {
        logger.debug("Stub call: StubMatchMakerSession.getDatabase()");
        return null;
    }

    public List<PlFolder> getFolders() {
        logger.debug("Stub call: StubMatchMakerSession.getFolders()");
        return null;
    }

    public Date getSessionStartTime() {
        logger.debug("Stub call: StubMatchMakerSession.getSessionStartTime()");
        return null;
    }

    public Connection getConnection(){
        logger.debug("Stub call: StubMatchMakerSession.getConnection()");
        return null;
    }

	public boolean isThisProjectNameAcceptable(String name) {
		logger.debug("Stub call: StubMatchMakerSession.isThisProjectNameAcceptable()");
		return false;
	}

	public Project getProjectByName(String name) {
		logger.debug("Stub call: StubMatchMakerSession.getProjectByName()");
		return null;
	}

    public String createNewUniqueName() {
        logger.debug("Stub call: StubMatchMakerSession.getNewUniqueName()");
        return null;
    }

    public long countProjectByName(String name) {
        logger.debug("Stub call: StubMatchMakerSession.countProjectByName()");
        return 0;
    }

    public void handleWarning(String message) {
        logger.warn("Stub call: StubMatchMakerSession.handleWarning("+message+")");
    }

    public void addWarningListener(WarningListener l) {
        logger.debug("Stub call: StubMatchMakerSession.addWarningListener()");
    }

    public void removeWarningListener(WarningListener l) {
        logger.debug("Stub call: StubMatchMakerSession.removeWarningListener()");
    }
    
	public MMRootNode getRootNode() {
    	return null;
    }

    public TranslateGroupParent getTranslations() {
        logger.debug("Stub call: StubMatchMakerSession.getTranslations()");
        return null;
    }

	public FolderParent getBackupFolderParent() {
		logger.debug("Stub call: StubMatchMakerSession.getBackupFolderParent()");
		return null;
	}

	public FolderParent getCurrentFolderParent() {
		logger.debug("Stub call: StubMatchMakerSession.getCurrentFolderParent()");
		return null;
	}

    public Version getPLSchemaVersion() {
        logger.debug("Stub call: StubMatchMakerSession.getPLSchemaVersion()");
        return null;
    }

	public boolean canSelectTable(SQLTable table) {
		logger.debug("Stub call: StubMatchMakerSession.canSelectTable()");
		return false;
	}

	public SQLTable findPhysicalTableByName(String catalog, String schema, String tableName) {
		logger.debug("Stub call: StubMatchMakerSession.findSQLTableByName()");
		return null;
	}

	public boolean tableExists(String catalog, String schema, String tableName) {
		logger.debug("Stub call: StubMatchMakerSession.tableExists()");
		return false;
	}

	public boolean tableExists(SQLTable table) {
		logger.debug("Stub call: StubMatchMakerSession.tableExists()");
		return false;
	}

	public String getAppUserEmail() {
		return null;
	}

	public boolean tableExists(String spDataSourceName, String catalog,
			String schema, String tableName) throws SQLObjectException {
		logger.debug("Stub call: StubMatchMakerSession.tableExists()");
		return false;
	}

	public SQLTable findPhysicalTableByName(String spDataSourceName,
			String catalog, String schema, String tableName)
			throws SQLObjectException {
		logger.debug("Stub call: StubMatchMakerSession.findPhysicalTableByName()");
		return null;
	}

	public SQLDatabase getDatabase(JDBCDataSource dataSource) {
		logger.debug("Stub call: StubMatchMakerSession.getDatabase()");
		return null;
	}

	public boolean close() {
		logger.debug("Stub call: StubMatchMakerSession.close()");
		return true;
	}

	public void addSessionLifecycleListener(
			SessionLifecycleListener<MatchMakerSession> listener) {
		logger.debug("Stub call: StubMatchMakerSession.addSessionLifecycleListener()");
		
	}

	public void removeSessionLifecycleListener(
			SessionLifecycleListener<MatchMakerSession> listener) {
		logger.debug("Stub call: StubMatchMakerSession.removeSessionLifecycleListener()");
	}

	public void addStatusMessage(String message) {
		logger.debug("Stub call: StubMatchMakerSession.addStatusMessage()");
		
	}

	public void removeStatusMessage() {
		logger.debug("Stub call: StubMatchMakerSession.removeStatusMessage()");
		
	}

	@Override
	public SPObject getWorkspace() {
		return null;
	}

	@Override
	public void runInForeground(Runnable runner) {
	}

	@Override
	public void runInBackground(Runnable runner) {
	}

	@Override
	public boolean isForegroundThread() {
		return false;
	}
	
	public List<UserDefinedSQLType> getSQLTypes()
    {
    	return null;
    }
    
    public UserDefinedSQLType getSQLType(int sqlType)
    {
    	return null;
    }

	@Override
	public UserPrompterFactory createUserPrompterFactory() {
		return new DefaultUserPrompterFactory();
	}

	@Override
	public UpgradePersisterManager getUpgradePersisterManager() {
		logger.debug("Stub call: MatchMakerSession.getUpgradePersisterManager()");
		return null;
	}

	@Override
	public File getSavePoint() {
		return null;
	}

	@Override
	public void setSavePoint(File savePoint) {
		
	}
}
