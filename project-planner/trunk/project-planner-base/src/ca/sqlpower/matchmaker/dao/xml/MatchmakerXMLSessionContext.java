/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.dao.xml;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.matchmaker.MatchMakerConfigurationException;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PLSchemaException;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.util.VersionFormatException;

public class MatchmakerXMLSessionContext implements MatchMakerSessionContext {

    public MatchMakerXMLSession createDefaultSession() {
        return new MatchMakerXMLSession(this);
    }

    public MatchMakerSession createSession(SPDataSource ds, String username, String password)
    throws PLSecurityException, SQLException,
            IOException, PLSchemaException, VersionFormatException,
            ArchitectException, MatchMakerConfigurationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<SPDataSource> getDataSources() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String getEmailSmtpHost() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public DataSourceCollection getPlDotIni() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void setEmailSmtpHost(String host) {
        throw new UnsupportedOperationException("Not implemented");
    }

}
