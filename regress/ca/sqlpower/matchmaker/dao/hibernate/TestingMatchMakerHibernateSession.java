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


package ca.sqlpower.matchmaker.dao.hibernate;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import ca.sqlpower.matchmaker.DBTestUtil;
import ca.sqlpower.matchmaker.FolderParent;
import ca.sqlpower.matchmaker.MMRootNode;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TestingMatchMakerContext;
import ca.sqlpower.matchmaker.TranslateGroupParent;
import ca.sqlpower.matchmaker.WarningListener;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.swingui.event.SessionLifecycleEvent;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.Version;

public class TestingMatchMakerHibernateSession implements MatchMakerHibernateSession {

    private static final Logger logger = Logger.getLogger(TestingMatchMakerHibernateSession.class);
        
    private final JDBCDataSource dataSource;
    private final SessionFactory hibernateSessionFactory;
    private TestingMatchMakerContext context = new TestingMatchMakerContext();
    private final TestingConnection con;
    private SQLDatabase db;
    private List<String> warnings = new ArrayList<String>();
    private TranslateGroupParent tgp = new TranslateGroupParent(this);
	private List<SessionLifecycleListener<MatchMakerSession>> lifecycleListener;

	private Session hSession;
    
    /**
     * The map of SQLDatabases to SPDatasources so they can be cached.
     */
    private Map<SPDataSource, SQLDatabase> databases = new HashMap<SPDataSource, SQLDatabase>();
	
    /**
     * Creates a new session that is really connected to a datasource.  
     * This session does not create a SQLDatabase
     * 
     * @param dataSource an architect data source describing the connection
     * @throws RuntimeException
     */
    public TestingMatchMakerHibernateSession(JDBCDataSource dataSource) throws RuntimeException {
        super();
        try {
            this.dataSource = dataSource;
            this.hibernateSessionFactory = HibernateTestUtil.buildHibernateSessionFactory(this.dataSource);
            this.con = DBTestUtil.connectToDatabase(this.dataSource);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        lifecycleListener = new ArrayList<SessionLifecycleListener<MatchMakerSession>>();
        resetSession();
    }
    
    /**
     * Provides a disconnected SQLDatabase, but is not really connected to a datasource
     */
    public TestingMatchMakerHibernateSession() {
        db = new SQLDatabase();
        con = null;
        dataSource = null;
        hibernateSessionFactory = null;
    }

    public Session openSession() {
        return hSession;
    }

    public void resetSession() {
    	hSession = hibernateSessionFactory.openSession(getConnection());
    }
    /**
     * Enables or disables the connection associated with this session.  This is useful for
     * testing that Hibernate is correctly configured to eagerly fetch the data we expect it to.
     * 
     * @param disabled
     */
    public void setConnectionDisabled(boolean disabled) {
        con.setDisabled(disabled);
    }
    
    public Connection getConnection() {
        return con;
    }


    ///////// Unimplemented MatchMakerHibernateSession methods are below this line //////////
    
    public String createNewUniqueName() {
        logger.debug("Stub call: TestingMatchMakerHibernateSession.createNewUniqueName()");
        return null;
    }

    public PlFolder findFolder(String foldername) {
        logger.debug("Stub call: TestingMatchMakerHibernateSession.findFolder()");
        return null;
    }

    public String getAppUser() {
        logger.debug("Stub call: TestingMatchMakerHibernateSession.getAppUser()");
        return null;
    }

    public MatchMakerSessionContext getContext() {
        return context;
    }

    public <T extends MatchMakerObject> MatchMakerDAO<T> getDAO(Class<T> businessClass) {
        logger.debug("Stub call: TestingMatchMakerHibernateSession.getDAO()");
        return null;
    }

    public String getDBUser() {
        logger.debug("Stub call: TestingMatchMakerHibernateSession.getDBUser()");
        return null;
    }

	public SQLDatabase getDatabase() {
		if (databases.get(db.getDataSource()) == null) {
			databases.put(db.getDataSource(),db);
		}
		return db;
	}
    
    public void setDatabase(SQLDatabase db) {
        this.db = db;
    }

    public List<PlFolder> getFolders() {
        logger.debug("Stub call: TestingMatchMakerHibernateSession.getFolders()");
        return null;
    }

    public Project getProjectByName(String name) {
        logger.debug("Stub call: TestingMatchMakerHibernateSession.getProjectByName()");
        return null;
    }

    public Date getSessionStartTime() {
        logger.debug("Stub call: TestingMatchMakerHibernateSession.getSessionStartTime()");
        return null;
    }

    public boolean isThisProjectNameAcceptable(Project project, String name) {
        logger.debug("Stub call: TestingMatchMakerHibernateSession.isThisProjectNameAcceptable()");
        return false;
    }

    public boolean isThisProjectNameAcceptable(String name) {
        logger.debug("Stub call: TestingMatchMakerHibernateSession.isThisProjectNameAcceptable()");
        return false;
    }

    public long countProjectByName(String name) {
        logger.debug("Stub call: TestingMatchMakerHibernateSession.countProjectByName()");
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

    public void addWarningListener(WarningListener l) {
        logger.debug("Stub call: TestingMatchMakerHibernateSession.addWarningListener()");
    }

    public void removeWarningListener(WarningListener l) {
        logger.debug("Stub call: TestingMatchMakerHibernateSession.removeWarningListener()");
    }

    public TranslateGroupParent getTranslations() {
        return tgp;
    }

	public void setContext(TestingMatchMakerContext context) {
		this.context = context;
	}

	public FolderParent getBackupFolderParent() {
		logger.debug("Stub call: TestingMatchMakerHibernateSession.getBackupFolderParent()");
		return null;
	}

	public FolderParent getCurrentFolderParent() {
		logger.debug("Stub call: TestingMatchMakerHibernateSession.getCurrentFolderParent()");
		return null;
	}
    
    public Version getPLSchemaVersion() {
        throw new UnsupportedOperationException("Called getPLSchmaVersion on mock object");
    }

	public boolean canSelectTable(SQLTable table) {
		logger.debug("Stub call: TestingMatchMakerHibernateSession.canSelectTable()");
		return false;
	}

	 public SQLTable findPhysicalTableByName(String spDataSourceName, String catalog, String schema, String tableName) throws SQLObjectException {
	    	logger.debug("Session.findSQLTableByName:" + spDataSourceName + " " + 
	    			catalog + "." + schema + "." + tableName);
	    	
	    	if (tableName == null || tableName.length() == 0) return null;
	    	
	    	if (spDataSourceName == null || spDataSourceName.length() == 0) {
	    		return findPhysicalTableByName(catalog, schema, tableName);
	    	}
	    	
	    	JDBCDataSource ds = null;
	    	SQLDatabase tempDB = null;
	    	if (context == null || context.getDataSources() == null) {
	    		tempDB = getDatabase();
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
		    	tempDB = new SQLDatabase(ds);
	    	}
	    	
	    	try {
	    		SQLTable table = tempDB.getTableByName(
	    				catalog,
	    				schema,
	    				tableName);
	    		table.getColumns();
	    		table.getImportedKeys();
	    		return table;
	    	} finally {
	    		if (tempDB != null) tempDB.disconnect();
	    	}
	    }
	    
	 	//Right now the other findPhicialTableByName uses this method, don't call it from here using an empty data source.
	    public SQLTable findPhysicalTableByName(String catalog, String schema, String tableName) throws SQLObjectException {
	    	if (tableName == null || tableName.length() == 0) return null;
	    	SQLDatabase tempDB = new SQLDatabase(dataSource);
	    	try {
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

	public String getAppUserEmail() {
		logger.debug("Stub call: TestingMatchMakerHibernateSession.getAppUserEmail()");
		return null;
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
		if (hSession.isConnected()) hSession.close();
		if (!hibernateSessionFactory.isClosed()) hibernateSessionFactory.close();
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
		logger.debug("Stub call: TestingMatchMakerHibernateSession.addStatusMessage()");
		
	}

	public void removeStatusMessage() {
		// no-op
		logger.debug("Stub call: TestingMatchMakerHibernateSession.removeStatusMessage()");
		
	}

	@Override
	public MMRootNode getRootNode() {
		logger.debug("Stub call: MatchMakerSession.getRootNode()");
		return null;
	}
}
