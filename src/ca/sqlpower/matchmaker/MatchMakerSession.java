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

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import ca.sqlpower.dao.upgrade.UpgradePersisterManager;
import ca.sqlpower.matchmaker.dao.hibernate.MatchMakerSessionContextImpl;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLDatabaseMapping;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.UserDefinedSQLType;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.RunnableDispatcher;
import ca.sqlpower.util.UserPrompterFactory;
import ca.sqlpower.util.WorkspaceContainer;

/**
 * The MatchMakerSession interface represents one person's login to
 * a PL Schema, and is the main container for session information
 * throughout the MatchMaker business model.
 *
 * <p>To make one of these, use the
 * {@link MatchMakerSessionContextImpl#createSession(SPDataSource, String, String)}
 * method.
 *
 * <p>This interface makes a strong committment to keeping the MatchMaker
 * metadata in a SQL database.  This might come back to haunt us some day.
 *
 * @version $Id$
 */
public interface MatchMakerSession extends SQLDatabaseMapping, WorkspaceContainer, RunnableDispatcher {

    /**
     * The session context that created this session.
     */
    public MatchMakerSessionContext getContext();

	/**
	 * The database connection to the PL Schema for this session.
	 */
	public SQLDatabase getDatabase();

	/**
	 * The PL Schema user for this session.  Often but not necessarily
	 * the same as the DB User.
	 */
	public String getAppUser();
	
	/** 
	 * The email of the PL Schema user for this session.
	 */
	public String getAppUserEmail();

	/**
	 * The actual RDBMS user name that we are connected to the database as.
	 */
	public String getDBUser();

	/**
	 * The time this session was created.
	 */
	public Date getSessionStartTime();
	
	/**
	 * The prompter factory for this session. The returned factory will communicate with
	 * the user through the proper method depending on what kind of session is running.
	 * @return the factory
	 */
	public UserPrompterFactory createUserPrompterFactory();

    /**
     * Returns the folder that matches with the name
     * @param foldername the name of the folder that is desired
     * @return the folder with that matches with the foldername, returns null if no results are avaiable
     */
    public PlFolder findFolder(String foldername);

    /**
     * Get a connection to the current database
     */
    public Connection getConnection();

    /**
	 * check to see if there is any project under given name
	 * @param name
	 * @return true if no project found under given name, false otherwise
	 */
    public boolean isThisProjectNameAcceptable(String name);

    /**
	 * count project entity by given name
	 * @param name
	 * @return number of project entity
	 */
    public long countProjectByName(String name);

    /**
     * find the Project Object by name, search by the DAO
     * @param name the name of the project desired
     * @return project object or null if not found
     */
    public Project getProjectByName(String name);

    /**
     * This method creates an unique name for the project such that it does not
     * conflict with existing project names.  The foromat of the name should be
     * New_Project#
     * @return an unique non-conflicting name in New_Project# form (unless New_Project
     *          is already an acceptable name)
     */
    public String createNewUniqueName();
    
    /**
     * Tells the session implementation about a warning message that should be
     * displayed to the user as soon as possible.  Warnings are different from
     * exceptions: they do not prevent completion of the current task.  They are
     * simply assumptions and workarounds in the business logic that the user should
     * be aware of when they happen.
     * <p>
     * A call to this method may invoke some implementation-specific handling mechanism,
     * but should also always result in all subscribed WarningListeners being notified
     * with the message as well.
     * 
     * @param message The message to display to the user.
     */
    public void handleWarning(String message);
    
    /**
     * Subscribes the given listener to the incessant stream of warning messages
     * generated as a result of bad metadata.
     */
    public void addWarningListener(WarningListener l);
    
    /**
     * Removes the given listener to the incessant stream of warning messages
     * generated as a result of bad metadata.
     * 
     * @param l The listener to remove. If it was not actually listening, calling
     * this method has no effect.
     */
    public void removeWarningListener(WarningListener l);

	/**
	 * Retrieves the root node of all MatchMakerObject objects
	 */
    public MMRootNode getRootNode();
    
    /**
     * get all of the translations the user can see
     */
    public TranslateGroupParent getTranslations();

    /**
	 * All of the project that the current user can see with backup.
	 */
	public FolderParent getBackupFolderParent();

	/**
	 * All of the projects that the current user can see that are active.
	 */
	public FolderParent getCurrentFolderParent();
	
	
	/**
     * find the sql table that exists in the session's database 
     * (i.e. not just in memory)
     * @param catalog	catalog of the table
     * @param schema	schema of the table
     * @param tableName name of the table
     * @return SQLTable if found or null if not
     * session's database
     */
    public SQLTable findPhysicalTableByName(String catalog, String schema, String tableName) throws SQLObjectException;
    
    /**
     * find the sql table that exists in the given spDataSource
     * (i.e. not just in memory), throws an error if the dsName
     * is not empty and it is not found.
     * 
     * @param spDataSourceName the name of the data source to get the db from
     * @param catalog	catalog of the table
     * @param schema	schema of the table
     * @param tableName name of the table
     * @return SQLTable if found or null if not
     * session's database
     */
    public SQLTable findPhysicalTableByName(String spDataSourceName, String catalog, String schema, String tableName) throws SQLObjectException;

    
	/**
     * Returns true if the SQL table exists
     * in the session's database; false otherwise.
     */
    public boolean tableExists(String catalog, String schema, String tableName) throws SQLObjectException;
    
    
    /**
     * Returns true if the SQLTable exists in the database 
     * of the given spDataSource, throws an error if the dsName is not empty 
     * and not found.
     */
    public boolean tableExists(String spDataSourceName, String catalog, String schema, String tableName) throws SQLObjectException;
    
	/**
     * Returns true if the SQL table exists
     * in the session's database; false otherwise.
     */
    public boolean tableExists(SQLTable table) throws SQLObjectException;
    
    /**
     * return true if the current user of session can select the sql table
     * @param table -- the table that we want to try select on
     * @return true if the user can select table, 
     * false if the user has no select privilege
     */
    public boolean canSelectTable(SQLTable table);
    
    /**
     * Returns the database associated with the given datasource. If 
     * one already exists it will be returned, if it does not one will
     * be created and remembered for next time.
     */
    public SQLDatabase getDatabase(JDBCDataSource dataSource);

	/**
	 * Call this method to close the session's opened resources.
	 * @return true if successful close, false if failed or cancelled
	 */
	public boolean close();
	
	/**
     * Subscribes the given listener to the session closing events.
     */
	public void addSessionLifecycleListener(SessionLifecycleListener<MatchMakerSession> listener);
	
    /**
     * Removes the given listener from the session closing events
     * 
     * @param l The listener to remove. If it was not actually listening, calling
     * this method has no effect.
     */
	public void removeSessionLifecycleListener(SessionLifecycleListener<MatchMakerSession> listener);
	
	/**
	 * Adds a message onto the status label on the bottom frame of the Match Maker
	 * 
	 */
	public void addStatusMessage(String message);
	
	/**
	 * Removes the message from the status label on the bottom frame of the Match Maker
	 * 
	 */
	public void removeStatusMessage();
	
	 /** 
     * Gets the basic SQL types from the PL.INI file
     */
    public List<UserDefinedSQLType> getSQLTypes();
    
    /** 
     * Gets the basic SQL type from the PL.INI file.
     */
    public UserDefinedSQLType getSQLType(int sqlType);
    
    /**
     * Gets the upgrade version manager for this session.
     */
    public UpgradePersisterManager getUpgradePersisterManager();
}

