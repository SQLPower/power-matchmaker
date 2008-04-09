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


package ca.sqlpower.sql;

import java.sql.Connection;
import java.sql.SQLException;

import ca.sqlpower.matchmaker.MatchMakerSession;

public class TestingDefParamsObject extends DefaultParameters {
	
	String emailServerName = null;
	String emailReturnAddress = null;

	public TestingDefParamsObject(Connection arg0) throws SQLException, PLSchemaException {
		super(arg0);
	}

	public TestingDefParamsObject(MatchMakerSession session) throws SQLException, PLSchemaException {
		super(session.getConnection());
	}

	@Override
	public String get(String arg0) {
		return super.get(arg0);
	}

	public void setEmailReturnAddress(String emailReturnAddress) {
		this.emailReturnAddress = emailReturnAddress;
	}

	public void setEmailServerName(String emailServerName) {
		this.emailServerName = emailServerName;
	}

	@Override
	public String getEmailReturnAddress() {
		return emailReturnAddress;
	}

	@Override
	public String getEmailServerName() {
		return emailServerName;
	}

}
