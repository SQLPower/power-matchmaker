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
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.event.SessionLifecycleListener;

public class TestingMatchMakerContext implements MatchMakerSessionContext {
	List<JDBCDataSource> dataSources = new ArrayList<JDBCDataSource>();
	DataSourceCollection plDotIni;
	MatchMakerSession session;
	Collection<MatchMakerSession> sessions = new LinkedList<MatchMakerSession>();
	
	/**
	 * The Preferences node for the TestingMatchMakerContext. We want to keep
	 * this separate from the regular MatchMaker Preferences to ensure the test
	 * suite doesn't interfere with the user's preferences.
	 */
	Preferences prefs = Preferences.userNodeForPackage(TestingMatchMakerContext.class).node("test");
	
	public TestingMatchMakerContext() {
		this(true);
	}
	
	public TestingMatchMakerContext(boolean loadPlDotIni) {
		plDotIni = new PlDotIni();
		sessions.add(session);
		
		if (loadPlDotIni) {
			dataSources.add(DBTestUtil.getHSQLDBInMemoryDS());
			dataSources.add(DBTestUtil.getOracleDS());
			dataSources.add(DBTestUtil.getSqlServerDS());
			try {
				plDotIni.read(new File("testbed/pl.regression.ini"));
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage());
			}
		}
	}
	
	public List<JDBCDataSource> getDataSources() {
		return dataSources;
	}

	public void setDataSou2rces(List<JDBCDataSource> dataSources) {
		this.dataSources = dataSources;
	}

	public DataSourceCollection getPlDotIni() {
		return plDotIni;
	}

	public void setPlDotIni(DataSourceCollection plDotIni) {
		this.plDotIni = plDotIni;
	}

	public MatchMakerSession getSession() {
		return session;
	}

	public void setSession(MatchMakerSession session) {
		this.session = session;
		sessions.clear();
		sessions.add(session);
	}

	public MatchMakerSession createSession(JDBCDataSource ds, String username,
			String password) throws PLSecurityException, SQLException {
		return session;
	}

    public MatchMakerSession createDefaultSession() {
        return session;
    }

	public String getEmailSmtpHost() {
		return prefs.get(EMAIL_HOST_PREFS, "");
	}

	public void setEmailSmtpHost(String host) {
		prefs.put(EMAIL_HOST_PREFS, host);
	}

	public Collection<MatchMakerSession> getSessions() {
		return sessions;
	}

	public SessionLifecycleListener<MatchMakerSession> getSessionLifecycleListener() {
		return null;
	}

	public void closeAll() {
		session.close();
	}
	
	public void ensureDefaultRepositoryDefined() {
		// Do nothing
	}

	public String getAddressCorrectionDataPath() {
		return null;
	}

	public void setAddressCorrectionDataPath(String path) {
		// Do nothing
	}

	public void addPreferenceChangeListener(PreferenceChangeListener l) {
		prefs.addPreferenceChangeListener(l);
	}

	public void removePreferenceChangeListener(PreferenceChangeListener l) {
		prefs.removePreferenceChangeListener(l);
	}

	@Override
	public MatchMakerSession createSession() throws PLSecurityException,
			SQLException, SQLObjectException, MatchMakerConfigurationException {
		return null;
	}
}
