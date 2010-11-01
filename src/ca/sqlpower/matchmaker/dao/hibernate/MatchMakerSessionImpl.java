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
import ca.sqlpower.matchmaker.FolderParent;
import ca.sqlpower.matchmaker.MMRootNode;
import ca.sqlpower.matchmaker.MatchMakerConfigurationException;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TranslateGroupParent;
import ca.sqlpower.matchmaker.WarningListener;
import ca.sqlpower.matchmaker.dao.MatchMakerUpgradePersisterManager;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.security.PLSecurityManager;
import ca.sqlpower.security.PLUser;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.UserDefinedSQLType;
import ca.sqlpower.swingui.event.SessionLifecycleEvent;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.DefaultUserPrompterFactory;
import ca.sqlpower.util.UserPrompterFactory;

/**
 * An implementation of MatchMakerSession. This is the core implementation
 * that does not deal with UI specifics.
 */
public class MatchMakerSessionImpl implements MatchMakerSession {

    private static final Logger logger = Logger.getLogger(MatchMakerSessionImpl.class);

    /**
     * The map of SQLDatabases to SPDatasources so they can be cached.
     */
    private Map<SPDataSource, SQLDatabase> databases = new HashMap<SPDataSource, SQLDatabase>();

	/**
	 * A list that helps keep track of the created sessions
	 */
	private List<SessionLifecycleListener<MatchMakerSession>> lifecycleListener;

    private final MatchMakerSessionContext context;

	/**
	 * The security manager is used to define a logged in user and their email
	 * address. This will likely change as the security model changes in the
	 * Enterprise Edition.
	 * <p>
	 * XXX Do we need a security manager now that Hibernate is gone and we are
	 * no longer using a repository?
	 */
	private PLSecurityManager sm;
	private PLUser appUser;
	private String dbUser;
	private Date sessionStartTime;
	
	/**
     * The factory that creates user prompters for this session. Defaults to a
     * factory that makes an "always OK" user prompter for headless/embedded use.
     * When this session is being used in a GUI environment, the startup code
     * for the GUI will replace the default factory with one that actually
     * prompts the user.
     */
    private UserPrompterFactory userPrompterFactory = new DefaultUserPrompterFactory();
	
    /**
     * The last place the objects in this session were saved to.
     */
    private File savePoint;
    
    /**
     * This node is the root node of all MatchMakerObjects and everything stems from this.
     * Its children are the FolderParents and the TranslateGroupParents
     */
    private MMRootNode rootNode;
    
    private List<WarningListener> warningListeners = new ArrayList<WarningListener>();
    
    private final UpgradePersisterManager upgradeManager = new MatchMakerUpgradePersisterManager();

    /**
     * XXX this is untestable unless you're connected to a database right now.
     *   It should be given a PLSecurityManager implementation rather than creating one.
     *  
     * @throws SQLObjectException if there was a problem connecting to the database
     * @throws MatchMakerConfigurationException If there are some user settings that are
     * not set up properly. 
     */
	public MatchMakerSessionImpl(MatchMakerSessionContext context) throws PLSecurityException,
			SQLException, SQLObjectException,
			MatchMakerConfigurationException {
		
		rootNode = new MMRootNode();
		rootNode.setSession(this);
        
        lifecycleListener = new ArrayList<SessionLifecycleListener<MatchMakerSession>>();

        this.context = context;

		sessionStartTime = new Date();
	}

    public MatchMakerSessionContext getContext() {
        return context;
    }

	public String getAppUser() {
		return appUser.getUserId();
	}
	
	public String getAppUserEmail() {
		return appUser.getEmailAddress();
	}

	public String getDBUser() {
		return dbUser;
	}

	public Date getSessionStartTime() {
		return sessionStartTime;
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


    public PlFolder findFolder(String foldername) {
        for (SPObject spo : getCurrentFolderParent().getChildren()) {
        	PlFolder folder = (PlFolder) spo;
            if (folder.getName().equals(foldername)) return folder;
        }
        return null;
    }

    public Project getProjectByName(String name) {
    	for (PlFolder folder : rootNode.getCurrentFolderParent().getPlFolders()) {
    		for (Project p : folder.getProjects()) {
    			if (p.getName().equals(name)) {
    				return p;
    			}
    		}
    	}
    	return null;
    }

	public boolean isThisProjectNameAcceptable(String name) {
		return getProjectByName(name) == null;
	}

    public String createNewUniqueName() {
        String name = "New Project";
        if (getProjectByName(name) == null) {
            return name;
        } else{
            int num=1;
            //Iterates until it finds a name that does not conflict with
            //existing project names
            while(getProjectByName(name+num) != null) {
                num++;
                name = "New Project" + num;
            }
            return name;
        }

    }

	public long countProjectByName(String name) {
		long projectCount = 0;
    	for (PlFolder folder : rootNode.getCurrentFolderParent().getPlFolders()) {
    		for (Project p : folder.getProjects()) {
    			if (p.getName().equals(name)) {
    				projectCount++;
    			}
    		}
    	}
		return projectCount;
	}

    public TranslateGroupParent getTranslations() {
        return rootNode.getTranslateGroupParent();
    }

    /**
     * Retrieves all the PL Folders from the database, or returns the
     * list that was previously retrieved by a call to this method.
     */
	public FolderParent getCurrentFolderParent() {
		return rootNode.getCurrentFolderParent();
	}
	
	/**
	 * Retrieves the root node of all MatchMakerObject objects
	 */
    public MMRootNode getRootNode() {
    	return rootNode;
    }

	/**
     * Retrieves all the PL Folders from the database that have backed-up project
     * transactions, or returns the list that was previously retrieved by a call
     * to this method.
     * <p>
     * TODO implement backups
     */
	public FolderParent getBackupFolderParent() {
		return rootNode.getBackupFolderParent();
	}

    /**
     * If you change this method, you must also change the methods in TestingMatchMakerSession and 
     * TestingMatchMakerSession because they are actually the same method......
     */
    public SQLTable findPhysicalTableByName(String spDataSourceName, String catalog, String schema, String tableName) throws SQLObjectException {
    	logger.debug("Session.findSQLTableByName: ds=" + spDataSourceName + ", " + 
    			catalog + "." + schema + "." + tableName);
    	
    	JDBCDataSource ds = null;
    	
    	if (spDataSourceName == null || spDataSourceName.length() == 0) {
    		return null;
    	} else {
    		for (SPDataSource spd : context.getDataSources()) {
    			if (spd.getName().equals(spDataSourceName)) {
    				ds = (JDBCDataSource) spd;
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
    		for(SQLColumn c : table.getColumns()) {
    			c.setType(getSQLType(c.getType()));
    		}
    		
    		table.getImportedKeys();
    		return table;
    	} finally {
    		if (tempDB != null) tempDB.disconnect();
    	}
    }
    
    public boolean tableExists(String spDataSourceName, String catalog, String schema,
    		String tableName) throws SQLObjectException {
        logger.debug("Session.findSQLTableByName: ds=" + spDataSourceName + ", " + 
                catalog + "." + schema + "." + tableName);
        
        JDBCDataSource ds = null;
        
        if (spDataSourceName == null || spDataSourceName.length() == 0) {
            return false;
        } else {
            for (SPDataSource spd : context.getDataSources()) {
                if (spd.getName().equals(spDataSourceName)) {
                    ds = (JDBCDataSource) spd;
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
            return table != null;
        } finally {
            if (tempDB != null) tempDB.disconnect();
        }
    }

    public boolean tableExists(SQLTable table) throws SQLObjectException {
    	if ( table == null ) return false;
    	return tableExists(table.getParentDatabase().getDataSource().getName(),
    			table.getCatalogName(),
    			table.getSchemaName(),
    			table.getName());
    }

    /**
     * this method requires real JDBC connection and create sql statement
     * on the connection.
     */
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
    		
    		conn.setAutoCommit(false);
    		stmt = conn.createStatement();
    		stmt.setMaxRows(1);
    		stmt.setFetchSize(1);
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
	
	/**
	 * Call this method to close the hibernate resources and database connection.
	 */
	public boolean close() {
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
		logger.debug("Stub call: MatchMakerHibernateSessionImpl.addStatusMessage()");
		
	}

	public void removeStatusMessage() {
		// no-op
		logger.debug("Stub call: MatchMakerHibernateSessionImpl.removeStatusMessage()");
		
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
     * Gets the basic SQL type from the PL.INI file by comparing jdbc codes.
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
		return upgradeManager;
	}

	public void setSavePoint(File savePoint) {
			this.savePoint = savePoint;
		
	}

	public File getSavePoint() {
		return savePoint;
	}

}