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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerConfigurationException;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.swingui.SwingSessionContextImpl;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.JDBCDataSourceType;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SpecificDataSourceCollection;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.event.SessionLifecycleEvent;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.UnknownFreqCodeException;

/**
 * Basic configuration object for a MatchMaker launch.  If you want to create MatchMakerSession
 * objects which are backed by Hibernate DAOs, create and configure one of these contexts, then
 * ask it to make sessions for you!
 * 
 * <p>There's another version of this which is also tied into the Swing GUI.  In a GUI app,
 * that's the one you'll want: {@link SwingSessionContextImpl}.
 * 
 * @see ca.sqlpower.matchmaker.swingui.SwingSessionContextImpl
 */
public class MatchMakerHibernateSessionContext implements MatchMakerSessionContext {

	private static final Logger logger = Logger.getLogger(MatchMakerHibernateSessionContext.class);

    /**
     * The list of database connections that this session context knows about.  This
     * implementation uses the <blink><marquee>AWESOME</marquee></blink> pl.ini file
     */
    private final DataSourceCollection<JDBCDataSource> plDotIni;
    
    /**
     * The prefs node that we use for persisting all the basic user settings that are
     * the same for all MatchMaker sessions.
     */
    private final Preferences prefs;
    
    /**
     * All live sessions that exist in (and were created by) this context.  Sessions
     * will be removed from this list when they fire their sessionClosing lifecycle
     * event.
     */
    private final Collection<MatchMakerSession> sessions;
    
    /**
     * Creates a new session context that uses the Hibernate DAO's to interact with the PL Schema.
     * 
     * @param plIni The data source collection that this context will use.
     */
    public MatchMakerHibernateSessionContext(Preferences prefs, DataSourceCollection<JDBCDataSource> plIni) {
        logger.debug("Creating new session context");
        this.sessions = new LinkedList<MatchMakerSession>();
        this.plDotIni = new SpecificDataSourceCollection<JDBCDataSource>(plIni, JDBCDataSource.class);
        this.prefs = prefs;
    }

    /**
     * Makes sure the built-in HSQLDB database type is set up correctly in the
     * given data source collection.
     */
    private void ensureHSQLDBIsSetup() {
        List<JDBCDataSourceType> types = plDotIni.getDataSourceTypes();
        JDBCDataSourceType hsql = null;
        for (JDBCDataSourceType dst : types) {
            if ("HSQLDB".equals(dst.getName())) {
                hsql = dst;
                break;
            }
        }
        if (hsql == null) {
            logger.error("HSQLDB connection type is missing! Built-in repository is unlikely to work properly!");
            return;
        }
        if (hsql.getJdbcJarList().isEmpty()) {
            hsql.addJdbcJar("builtin:hsqldb-1.8.0.9.jar");
        }
        List<String> jdbcJarList = hsql.getJdbcJarList();
        for (String jar : jdbcJarList) {
            File jarFile = new File(jar);
            if (!jarFile.exists()) {
                logger.error("HSQLDB Driver File missing: " + jarFile);
            }
        }
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.MatchMakerSessionContext#getDataSources()
     */
    public List<JDBCDataSource> getDataSources() {
        return plDotIni.getConnections();
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.matchmaker.MatchMakerSessionContext#createSession(ca.sqlpower.sql.SPDataSource, java.lang.String, java.lang.String)
     */
    public MatchMakerSession createSession(JDBCDataSource ds, String username,
			String password) throws PLSecurityException, SQLException,
			SQLObjectException, MatchMakerConfigurationException,
			RepositoryVersionException {

        // We create a copy of the data source and change the userID and
		// password
        //and use that for the login attempt.  We do not want to change the
        //default userID and password for the connection in here.
        JDBCDataSource tempDbSource = new JDBCDataSource(ds);
        tempDbSource.setUser(username);
        tempDbSource.setPass(password);

        try {
            MatchMakerSession session = new MatchMakerHibernateSessionImpl(this, tempDbSource);
            sessions.add(session);
            session.addSessionLifecycleListener(sessionLifecycleListener);
            return session;
        } catch (UnknownFreqCodeException ex) {
            throw new RuntimeException("This user doesn't have a valid default Dashboard date frequency, so you can't log in?!", ex);
        }
    }
    
    public MatchMakerSession createDefaultSession() throws RepositoryException {
        ensureHSQLDBIsSetup();
        JDBCDataSource ds = makeDefaultDataSource();
        
        // this throws an exception if there is a non-recoverable schema problem
        RepositoryUtil.createOrUpdateRepositorySchema(ds);
        
        try {
            return createSession(ds, ds.getUser(), ds.getPass());
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Couldn't create session. See nested exception for details.", ex);
        }
    }
    
    /**
     * Removes the closed session from the list, and terminates the VM
     * if there are no more sessions.
     */
    private SessionLifecycleListener<MatchMakerSession> sessionLifecycleListener =
        new SessionLifecycleListener<MatchMakerSession>() {
        public void sessionClosing(SessionLifecycleEvent<MatchMakerSession> e) {
            getSessions().remove(e.getSource());
            e.getSource().removeSessionLifecycleListener(this);
            if (getSessions().isEmpty()) {
                System.exit(0);
            }
        }
    };

    /**
     * Finds the default repository schema entry in this context's data source
     * collection, or if it's not found, creates a new repository data source
     * and adds it to the collection.
     */
    private JDBCDataSource makeDefaultDataSource() {
        JDBCDataSource ds = getPlDotIni().getDataSource(DEFAULT_REPOSITORY_DATA_SOURCE_NAME);
        if (ds == null) {
            ds = new JDBCDataSource(getPlDotIni());
            ds.setName(DEFAULT_REPOSITORY_DATA_SOURCE_NAME);
            ds.setPlSchema("public");
            ds.setUser("sa");
            ds.setPass("");
            ds.setUrl("jdbc:hsqldb:file:"+System.getProperty("user.home")+"/.mm/hsql_repository;shutdown=true");

            // find HSQLDB parent type
            JDBCDataSourceType hsqldbType = null;
            for (JDBCDataSourceType type : getPlDotIni().getDataSourceTypes()) {
                if ("HSQLDB".equals(type.getName())) {
                    hsqldbType = type;
                    break;
                }
            }
            if (hsqldbType == null) {
                throw new RuntimeException("HSQLDB Database type is missing in pl.ini");
            }
            ds.setParentType(hsqldbType);
            
            getPlDotIni().addDataSource(ds);
        } else if (ds.getName().equalsIgnoreCase(DEFAULT_REPOSITORY_DATA_SOURCE_NAME)) {
        	if (!ds.getUrl().contains("shutdown=true")) {
        		getPlDotIni().removeDataSource(ds);
        		ds.setUrl(ds.getUrl() + ";shutdown=true");
        		getPlDotIni().addDataSource(ds);
        	}
        }
        return ds;
    }

    public DataSourceCollection<JDBCDataSource> getPlDotIni() {
        return plDotIni;
    }

    /**
     * Returns the smtp host address 
     */
	public String getEmailSmtpHost() {
		return prefs.get(EMAIL_HOST_PREFS, "");
	}

	/**
	 * Sets the smtp host address in the preferences
	 */
	public void setEmailSmtpHost(String host) {
		prefs.put(EMAIL_HOST_PREFS, host);
	}
	
    public Collection<MatchMakerSession> getSessions() {
        return sessions;
    }

	public SessionLifecycleListener<MatchMakerSession> getSessionLifecycleListener() {
		return sessionLifecycleListener;
	}

	public void closeAll() {
		List<MatchMakerSession> doomedSessions =
			new ArrayList<MatchMakerSession>(getSessions());
		// reverse the list so the oldest sessions close first
		// also so the swing sessions get closed before the hibernate (a MUST)
		Collections.reverse(doomedSessions);
		for (MatchMakerSession s : doomedSessions) {
			if (!((MatchMakerSession) s).close()) return;
		}
	}

	public void ensureDefaultRepositoryDefined() {
	    SPDataSource defaultRepository = null;
		
	    for (SPDataSource ds : getDataSources()) {
	        if (ds.getName().equals(DEFAULT_REPOSITORY_DATA_SOURCE_NAME)) {
	        	defaultRepository = ds;
	        }
	    }
	    
	    if (defaultRepository == null) {
	    	defaultRepository = makeDefaultDataSource();
	    }
	}
	
    public void setAddressCorrectionDataPath(String path) {
    	prefs.put(ADDRESS_CORRECTION_DATA_PATH, path);
    }
    
    public String getAddressCorrectionDataPath() {
    	return prefs.get(ADDRESS_CORRECTION_DATA_PATH, "");
    }
    
    public void addPreferenceChangeListener(PreferenceChangeListener l) {
    	prefs.addPreferenceChangeListener(l);
    }
    
    public void removePreferenceChangeListener(PreferenceChangeListener l) {
    	prefs.removePreferenceChangeListener(l);
    }

}

