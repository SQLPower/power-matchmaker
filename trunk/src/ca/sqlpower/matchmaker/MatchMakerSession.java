/*
 * Copyright (c) 2007, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker;

import java.sql.Connection;
import java.util.Date;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.matchmaker.dao.hibernate.MatchMakerHibernateSessionContext;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.util.Version;

/**
 * The MatchMakerSession interface represents one person's login to
 * a PL Schema, and is the main container for session information
 * throughout the MatchMaker business model.
 *
 * <p>To make one of these, use the
 * {@link MatchMakerHibernateSessionContext#createSession(SPDataSource, String, String)}
 * method.
 *
 * <p>This interface makes a strong committment to keeping the MatchMaker
 * metadata in a SQL database.  This might come back to haunt us some day.
 *
 * @version $Id$
 */
public interface MatchMakerSession {

    /**
     * The session context that created this session.
     */
    public MatchMakerSessionContext getContext();

	/**
	 * The database connection to the PL Schema for this session.
	 */
	public SQLDatabase getDatabase();

    /**
     * get the PL Schema Version
     */
    public Version getPLSchemaVersion();
    
	/**
	 * The PL Schema user for this session.  Often but not necessarily
	 * the same as the DB User.
	 */
	public String getAppUser();

	/**
	 * The actual RDBMS user name that we are connected to the database as.
	 */
	public String getDBUser();

	/**
	 * The time this session was created.
	 */
	public Date getSessionStartTime();

    /**
     * Returns the folder that matches with the name
     * @param foldername the name of the folder that is desired
     * @return the folder with that matches with the foldername, returns null if no results are avaiable
     */
    public PlFolder findFolder(String foldername);

    /**
     * Returns the DAO Object for the given business class
     * @param <T> the business class of the DAO Object
     * @return the object that is of the business class
     */
    public <T extends MatchMakerObject> MatchMakerDAO<T> getDAO(Class<T> businessClass);

    /**
     * Get a connection to the current database
     */
    public Connection getConnection();

    /**
	 * check to see if there is any match under given name
	 * @param name
	 * @return true if no match found under given name, false otherwise
	 */
    public boolean isThisMatchNameAcceptable(String name);

    /**
	 * count match entity by given name
	 * @param name
	 * @return number of match entity
	 */
    public long countMatchByName(String name);

    /**
     * find the Match Object by name, search by the DAO
     * @param name the name of the match desired
     * @return match object or null if not found
     */
    public Match getMatchByName(String name);

    /**
     * This method creates an unique name for the match such that it does not
     * conflict with existing match names.  The foromat of the name should be
     * New_Match#
     * @return an unique non-conflicting name in New_Match# form (unless New_Match
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
     * get all of the translations the user can see
     */
    public TranslateGroupParent getTranslations();

    /**
	 * All of the Match folders that the current user can see with backup.
	 */
	public FolderParent getBackupFolderParent();

	/**
	 * All of the Match folders that the current user can see that are active.
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
    public SQLTable findPhysicalTableByName(String catalog, String schema, String tableName) throws ArchitectException;
    
	/**
     * Returns true if the SQL table exists
     * in the session's database; false otherwise.
     */
    public boolean tableExists(String catalog, String schema, String tableName) throws ArchitectException;
	/**
     * Returns true if the SQL table exists
     * in the session's database; false otherwise.
     */
    public boolean tableExists(SQLTable table) throws ArchitectException;
    
    /**
     * return true if the current user of session can select the sql table
     * @param table -- the table that we want to try select on
     * @return true if the user can select table, 
     * false if the user has no select privilege
     */
    public boolean canSelectTable(SQLTable table);
}
