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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.i 
 */

package ca.sqlpower.matchmaker;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PLSchemaException;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SchemaVersionFormatException;

public class TestingMatchMakerContext implements MatchMakerSessionContext {
	List<SPDataSource> dataSources;
	String emailEngineLocation;
	String matchEngineLocation;
	DataSourceCollection plDotIni;
	MatchMakerSession session;
	
	public List<SPDataSource> getDataSources() {
		return dataSources;
	}

	public void setDataSources(List<SPDataSource> dataSources) {
		this.dataSources = dataSources;
	}

	public String getEmailEngineLocation() {
		return emailEngineLocation;
	}

	public void setEmailEngineLocation(String emailEngineLocation) {
		this.emailEngineLocation = emailEngineLocation;
	}

	public String getMatchEngineLocation() {
		return matchEngineLocation;
	}

	public void setMatchEngineLocation(String matchEngineLocation) {
		this.matchEngineLocation = matchEngineLocation;
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


}
