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

package ca.sqlpower.matchmaker.swingui;

import java.awt.Rectangle;
import java.awt.Window;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.matchmaker.MatchMakerConfigurationException;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PLSchemaException;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.util.VersionFormatException;

public interface SwingSessionContext extends MatchMakerSessionContext {

    //////// MatchMakerSessionContext implementation //////////
    public MatchMakerSwingSession createSession(SPDataSource ds,
            String username, String password) throws PLSecurityException,
            SQLException, IOException, PLSchemaException, VersionFormatException,
            ArchitectException, MatchMakerConfigurationException;

    public List<SPDataSource> getDataSources();

    public DataSourceCollection getPlDotIni();

    public String getLastImportExportAccessPath();

    public void setLastImportExportAccessPath(String lastExportAccessPath);

    /**
     * Returns the previous location for the MatchMaker frame, or some reasonable default
     * if the previous bounds are unknown.
     */
    public Rectangle getFrameBounds();

    /**
     * Stores (persistently) the current position of the main frame, so it can appear
     * in the same location next time you call {@link #getFrameBounds()}.
     */
    public void setFrameBounds(Rectangle bounds);

    /**
     * Remembers the name of the given data source, so you can get it back next
     * time you call getLastLoginDataSource, even if the application is quit and
     * restarted in the interim.
     * 
     * @param dataSource The data source to remember.
     */
    public void setLastLoginDataSource(SPDataSource dataSource);

    /**
     * Returns the last data source passed to setLastLoginDataSource, even if
     * that happened since the application was last restarted.
     */
    public SPDataSource getLastLoginDataSource();

    /**
     * Shows the database connection manager dialog, attaching it to the given parent
     * window.  There will only ever be one created no matter how many times you call
     * this method.
     */
    public void showDatabaseConnectionManager(Window owner);

    /**
     * Shows the login dialog.
     * 
     * @param selectedDataSource The data source that should be selected in the dialog.
     * If null, the dialog's selected data source will remain unchanged.
     */
    public void showLoginDialog(SPDataSource selectedDataSource);

}