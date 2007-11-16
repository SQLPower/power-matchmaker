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

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PLSchemaException;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.util.Version;
import ca.sqlpower.util.VersionFormatException;

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
     * The version of this MatchMaker front end.
     */
    public static final Version APP_VERSION = new Version(0, 9, 0);

    /**
     * Name of the preferences parameter for email host address
     */
    public static final String EMAIL_HOST_PREFS = "email.host";
    
    public List<SPDataSource> getDataSources();

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
     * @throws IOException If any bootstrap init files are missing or unreadable.
     * @throws PLSchemaException If the version of the schema is not up to date or is missing
     * @throws VersionFormatException If the format of the version number is not in the form x.y.z
     * @throws ArchitectException If some SQLObject operations fail
     * @throws MatchMakerConfigurationException  If there is a user-fixable configuration problem.
     */
    public MatchMakerSession createSession(SPDataSource ds,
            String username, String password) throws PLSecurityException,
            SQLException, IOException, PLSchemaException, VersionFormatException,
            ArchitectException, MatchMakerConfigurationException;

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
    public DataSourceCollection getPlDotIni();
    
    /**
     * Returns the email smtp host address set in the preferences
     */
    public String getEmailSmtpHost();
    
    /**
     * Sets the email smtp host address in the preferences 
     */
    public void setEmailSmtpHost(String host);
}