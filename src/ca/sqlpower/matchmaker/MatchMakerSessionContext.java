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

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.prefs.PreferenceChangeListener;

import ca.sqlpower.matchmaker.dao.hibernate.RepositoryVersionException;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.event.SessionLifecycleListener;

/**
 * A MatchMakerSessionContext holds global (well, systemwide) configuration and
 * preferences information, and when properly configured, is used for creating
 * MatchMaker sessions, which are a basic requirement for using the rest of the
 * MatchMaker API. If you were looking for the starting point, you've found it!
 *
 * <p>The normal implementation of this interface is
 * {@link ca.sqlpower.matchmaker.dao.hibernate.MatchMakerHibernateSessionContext},
 * so you probably want to create and configure one of those to get started.
 *
 * @version $Id$
 */
public interface MatchMakerSessionContext {

    /**
     * The name of the data source that the local default repository is
     * in. Technically, this is only a concern when using the MatchMaker
     * with a repository provider (DAO layer) that uses data sources,
     * but several different session context implementations need to know
     * this shared piece of information, and those implementations definitely
     * shouldn't know about each other.
     */
    public static final String DEFAULT_REPOSITORY_DATA_SOURCE_NAME = "DQguru Default Repository";

    /**
     * Name of the preferences parameter for email host address
     */
    public static final String EMAIL_HOST_PREFS = "email.host";
    
    /**
     * Name of the preferences parameter for the PL.INI path
     */
    public static final String PREFS_PL_INI_PATH = "PL.INI.PATH";
    
    /**
	 * The preference key that specifies the path to the directory containing
	 * Address Correction Data provided by SQL Power Group Inc.
	 */
	public static final String ADDRESS_CORRECTION_DATA_PATH = "MatchMakerSessionContext.ADDRESS_CORRECTION_DATA_PATH";

    
    public List<JDBCDataSource> getDataSources();

    /**
     * Creates a MatchMaker session object, which entails logging into a database
     * with a current PL Schema.  A MatchMaker session is the core object of the
     * MatchMaker application, so you will want to call this method early in your
     * usage of the MatchMaker API.
     *
     * @param ds The data source that contains the PL Schema you want to connect to.
     * @param username The database user name to connect as.
     * @param password The database password for the given username.
     * @return A new MatchMaker session which is connected to the given database.
     * @throws PLSecurityException If there is no PL_USER entry for your database user, or
     * that user doesn't have permission to use the MatchMaker.
     * @throws SQLException If there are general database errors (can't connect, database
     * permission denied, the PL Schema is missing, etc).
     * @throws RepositoryVersionException If the repository version is incorrect, invalid, or missing.
     * @throws SQLObjectException If some SQLObject operations fail
     * @throws MatchMakerConfigurationException  If there is a user-fixable configuration problem.
     */
    public MatchMakerSession createSession(JDBCDataSource ds, String username,
			String password) throws PLSecurityException, SQLException,
			SQLObjectException, MatchMakerConfigurationException, RepositoryVersionException;

    /**
     * Creates a session using some default repository data source. If the
     * default repository data source doesn't exist, it will be created
     * automatically.
     */
    public MatchMakerSession createDefaultSession();
    
    /**
     * Returns the PlDotIni object that manages this context's list of data sources.
     *
     * <p>We would much rather make the PlDotIni concept an interface (maybe called
     * DataSourceCollection) and implement that on this session context interface.
     * Such implementations could delegate to PlDotIni and the databases.xml stuff,
     * as well as a JNDI implementation.
     */
    public DataSourceCollection<JDBCDataSource> getPlDotIni();
    
    /**
     * Returns the email smtp host address set in the preferences
     */
    public String getEmailSmtpHost();
    
    /**
     * Sets the email smtp host address in the preferences 
     */
    public void setEmailSmtpHost(String host);
    
    /**
     * Returns a linked list containing all the sessions from this context. 
     */
    public Collection<MatchMakerSession> getSessions();
    
    /**
     * Returns the listener that deals with closing sessions for this context.
     */
    public SessionLifecycleListener<MatchMakerSession> getSessionLifecycleListener();
    
    /**
     * Closes all sessions and terminates the VM.  This is the typical "exit"
     * action for a project.
     */
    public void closeAll();
    
    /**
	 * Checks whether or not the default repository is defined in the user's
	 * data sources, and if it's not, then add an entry for it.
	 */
    public void ensureDefaultRepositoryDefined();
    
    /**
     * Sets the path of the directory containing the Address Correction Data.
     */
    public void setAddressCorrectionDataPath(String path);
    
    /**
     * Returns the path of the directory containing the Address Correction Data.
     */
    public String getAddressCorrectionDataPath();
    
    /**
     * Adds a listener to the preferences so different parts of the app
     * can update when a preference changes.
     */
    public void addPreferenceChangeListener(PreferenceChangeListener l);
    
    /**
     * Removes a listener to the preferences.
     */
    public void removePreferenceChangeListener(PreferenceChangeListener l);
}