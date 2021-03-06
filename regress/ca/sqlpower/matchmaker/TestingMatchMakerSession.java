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


package ca.sqlpower.matchmaker;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.dao.upgrade.UpgradePersisterManager;
import ca.sqlpower.matchmaker.dao.MatchMakerUpgradePersisterManager;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.UserDefinedSQLType;
import ca.sqlpower.swingui.event.SessionLifecycleEvent;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.DefaultUserPrompterFactory;
import ca.sqlpower.util.UserPrompterFactory;
import ca.sqlpower.util.Version;

public class TestingMatchMakerSession implements MatchMakerSession {
	
	private static Logger logger = Logger.getLogger(TestingMatchMakerSession.class);

	MMRootNode rootNode;
	
	Date date = new Date();
	String appUser = "App User";
	String appUserEmail = "app@sqlpower.ca";
	String dbUser = "DB User";
	SQLDatabase db = new SQLDatabase();
	List<PlFolder> folders;
    TranslateGroupParent translateGroupParent;
	MatchMakerSessionContext context;
	Connection con;
	List<String> warnings = new ArrayList<String>();
	List<SessionLifecycleListener<MatchMakerSession>> lifecycleListener;
	
    /**
     * The map of SQLDatabases to SPDatasources so they can be cached.
     */
    private Map<SPDataSource, SQLDatabase> databases = new HashMap<SPDataSource, SQLDatabase>();
    
	public TestingMatchMakerSession() {
		this(true);
	}
	
	public TestingMatchMakerSession(boolean loadPlDotIni) {
		folders =  new ArrayList<PlFolder>();
        translateGroupParent= new TestingMatchMakerTranslateGroupParent();
        context = new TestingMatchMakerContext(loadPlDotIni);
        lifecycleListener = new ArrayList<SessionLifecycleListener<MatchMakerSession>>();
        rootNode = new MMRootNode();
        rootNode.setSession(this);
	}

	public String getDBUser() {
		return dbUser;
	}

	public SQLDatabase getDatabase() {
		if (databases.get(db.getDataSource()) == null) {
			databases.put(db.getDataSource(),db);
		}
		return db;
	}

	public List<PlFolder> getFolders() {
		return folders;
	}

	public Date getSessionStartTime() {
		return date;
	}

	public void setAppUser(String appUser) {
		this.appUser = appUser;
	}

	public void setSessionStartTime(Date date) {
		this.date = date;
	}

	public void setDatabase(SQLDatabase db) {
		this.db = db;
	}

	public void setDBUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public void setFolders(List<PlFolder> folders) {
		this.folders = folders;
	}

	public MatchMakerSessionContext getContext() {
		return context;
	}

    public PlFolder findFolder(String foldername) {
        for (PlFolder folder : folders){
            if (folder.getName().equals(foldername)) return folder;
        }
        return null;
    }

    public Connection getConnection() {
        return con;
    }

    public void setConnection(Connection con) {
        this.con = con;
    }

    public Project getProjectByName(String name) {
		return null;
	}

    public String createNewUniqueName() {
        return null;
    }

	public boolean isThisProjectNameAcceptable(String name) {
		return false;
	}

    public long countProjectByName(String name) {    
        return 0;
    }

    /**
     * Prints the message to syserr and appends it to the warnings list.
     * 
     * @see #getWarnings()
     */
    public void handleWarning(String message) {
        System.err.println("TestingMatchMakerSession.handleWarning(): got warning: "+message);
        warnings.add(message);
    }

    /**
     * Returns the real warning list.  Feel free to modify it if you want, but your changes
     * will affect the session's real list of warnings.
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * Replaces this session's warning list.  If you set this to null or an unmodifiable
     * list, handleWarning() will stop working.
     */
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    /**
     * Does nothing.
     */
    public void addWarningListener(WarningListener l) {
    }

    /**
     * Does nothing.
     */
    public void removeWarningListener(WarningListener l) {
    }
    
	public MMRootNode getRootNode() {
    	return rootNode;
    }

    public TranslateGroupParent getTranslations() {
    	if (translateGroupParent == null){
    		translateGroupParent = new TranslateGroupParent();
    	}
    	return translateGroupParent;
    }

    public FolderParent getBackupFolderParent() {
    	return null;
    }

    public FolderParent getCurrentFolderParent() {
    	FolderParent current = new FolderParent();
    	for(PlFolder child : folders) {
    		current.addChild(child);
    	}
    	return current;
    }

    public Version getPLSchemaVersion() {
    	throw new UnsupportedOperationException("Called getPLSchmaVersion on mock object");
    }


    public SQLTable findPhysicalTableByName(String spDataSourceName, String catalog, String schema, String tableName) throws SQLObjectException {

        JDBCDataSource ds = null;
    	
    	if (spDataSourceName == null || spDataSourceName.length() == 0) {
    		ds = getDatabase().getDataSource();
    	} else {
	    	for (JDBCDataSource spd : context.getDataSources()) {
	    		if (spd.getName().equals(spDataSourceName)) {
	    			ds = spd;
	    		}
	    	}
	    	if (ds == null) {
	    		throw new IllegalArgumentException("Error: No database connection named " + spDataSourceName + 
	        			" please create a database connection named " + spDataSourceName + " and try again.");
	    	}
    	}
    	
    	SQLDatabase tempDB = null;
    	try {
    		tempDB = new SQLDatabase(ds);
    		SQLTable table = tempDB.getTableByName(
    				catalog,
    				schema,
    				tableName);
    		if (table == null) return null;
    		table.getColumns();
    		table.getImportedKeys();
    		return table;
    	} finally {
    		if (tempDB != null) tempDB.disconnect();
    	}
    }
    
    public SQLTable findPhysicalTableByName(String catalog, String schema, String tableName) throws SQLObjectException {
    	return findPhysicalTableByName(getDatabase().getDataSource().getName(), catalog, schema, tableName);
    }

    public boolean tableExists(String catalog, String schema, 
    		String tableName) throws SQLObjectException {
    	return (findPhysicalTableByName(catalog,schema,tableName) != null);
    }
    
    public boolean tableExists(String spDataSourceName, String catalog, String schema, 
    		String tableName) throws SQLObjectException {
    	return (findPhysicalTableByName(spDataSourceName, catalog,schema,tableName) != null);
    }

    public boolean tableExists(SQLTable table) throws SQLObjectException {
    	if ( table == null ) return false;
    	return tableExists(table.getParentDatabase().getDataSource().getName(),
    			table.getCatalogName(),
    			table.getSchemaName(),
    			table.getName());
    }

    public boolean canSelectTable(SQLTable table) {

    	Connection conn;
		try {
			conn = table.getParentDatabase().getDataSource().createConnection();
		} catch (SQLException e1) {
			logger.debug("Cannot create connection to table: " + table);
			return false;
		}

    	Statement stmt = null;
    	StringBuffer sql = new StringBuffer();
    	try {
    		sql.append("select * from ");
    		sql.append(DDLUtils.toQualifiedName(table));
    		stmt = conn.createStatement();
    		stmt.executeQuery(sql.toString());
	    	try {
	    		stmt.close();
	    		conn.close();
			} catch (SQLException e1) {
				logger.debug("Could not close stamtment or connection");
			}
    		return true;
    	} catch (SQLException e) {
    		logger.debug("sql error: select statement:[" +
    				sql.toString() + "]\n" + e.getMessage() );
    		try {
				stmt.close();
				conn.close();
			} catch (SQLException e1) {
				logger.debug("Could not close stamtment or connection");
			}
    		return false;
    	} 
    }
	
	public SQLDatabase getDatabase(JDBCDataSource dataSource) {
		SQLDatabase db = databases.get(dataSource);
		if (db == null) {
			db = new SQLDatabase(dataSource);
			databases.put(dataSource,db);
		}
		return db;
	}

	public boolean close() {
		if (db.isConnected()) db.disconnect();
		fireSessionClosing();
		return true;
	}

	public void addSessionLifecycleListener(SessionLifecycleListener<MatchMakerSession> listener) {
		lifecycleListener.add(listener);
	}
	
	public void removeSessionLifecycleListener(
			SessionLifecycleListener<MatchMakerSession> listener) {
		lifecycleListener.remove(listener);
	}

	private void fireSessionClosing() {
		SessionLifecycleEvent<MatchMakerSession> evt = 
			new SessionLifecycleEvent<MatchMakerSession>(this);
		final List<SessionLifecycleListener<MatchMakerSession>> listeners = 
			new ArrayList<SessionLifecycleListener<MatchMakerSession>>(lifecycleListener);
		for (SessionLifecycleListener<MatchMakerSession> listener: listeners) {
			listener.sessionClosing(evt);
		}
	}

	public void addStatusMessage(String message) {
		// no-op
		logger.debug("Stub call: TestingMatchMakerSession.addStatusMessage()");
		
	}

	public void removeStatusMessage() {
		// np-op
		logger.debug("Stub call: TestingMatchMakerSession.removeStatusMessage()");
	}
	
	@Override
	public SPObject getWorkspace() {
		return getRootNode();
	}
	
	@Override
    public void runInForeground(Runnable runner) {
        SwingUtilities.invokeLater(runner);
    }

	@Override
    public void runInBackground(Runnable runner) {
        runInBackground(runner, "worker");
    }

	public void runInBackground(final Runnable runner, String name) {
        new Thread(runner, name).start();       
    }

	@Override
	public boolean isForegroundThread() {
        return true;
	}
	
	/** 
     * Gets the basic SQL types from the PL.INI file
     */
    public List<UserDefinedSQLType> getSQLTypes()
    {
    	return Collections.unmodifiableList(this.getContext().getPlDotIni().getSQLTypes());
    }
    
    /** 
     * Gets the basic SQL type from the PL.INI file.
     */
    public UserDefinedSQLType getSQLType(int sqlType)
    {
    	List<UserDefinedSQLType> types = getSQLTypes();
    	for(UserDefinedSQLType s : types) {
    		if(s.getType().equals(sqlType)) {
    			return s;
    		}
    	}
    	throw new IllegalArgumentException(sqlType + " is not a sql datatype.");
    }

	@Override
	public UserPrompterFactory createUserPrompterFactory() {
		return new DefaultUserPrompterFactory();
	}

	@Override
	public UpgradePersisterManager getUpgradePersisterManager() {
		return new MatchMakerUpgradePersisterManager();
	}

	@Override
	public String getAppUserEmail() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: MatchMakerSession.getAppUserEmail()");
		return null;
	}

	@Override
	public String getAppUser() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: MatchMakerSession.getAppUser()");
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
