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
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PLSchemaException;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SchemaVersionFormatException;

public class TestingMatchMakerContext implements MatchMakerSessionContext {
	List<SPDataSource> dataSources = new ArrayList<SPDataSource>();
	DataSourceCollection plDotIni;
	MatchMakerSession session;
	
 	/**
 	 * The Preferences node that we will use in this test. We want to keep
 	 * this separate from the regular MatchMaker Preferences to ensure the test
 	 * suite doesn't interfere with the user's preferences.
 	 */
 	Preferences prefs = Preferences.userNodeForPackage(TestingMatchMakerContext.class).node("test");
	
	public TestingMatchMakerContext() {
	}
	
	public List<SPDataSource> getDataSources() {
		return dataSources;
	}

	public void setDataSou2rces(List<SPDataSource> dataSources) {
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
	}

	public MatchMakerSession createSession(SPDataSource ds,
			String username, String password) throws PLSecurityException,
			SQLException, IOException,
			SchemaVersionFormatException, PLSchemaException {
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
}
