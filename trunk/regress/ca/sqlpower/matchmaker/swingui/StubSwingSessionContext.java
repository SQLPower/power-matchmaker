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
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.swingui.munge.AbstractMungeComponent;
import ca.sqlpower.matchmaker.swingui.munge.StepDescription;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.validation.swingui.FormValidationHandler;

/**
 * A stub of SwingSessionContext.  None of the methods return useful values, but they all
 * log calls on at the debug level.  Override methods you want in your test cases.
 */
public class StubSwingSessionContext implements SwingSessionContext {

    Logger logger = Logger.getLogger(StubSwingSessionContext.class);

    public MatchMakerSwingSession createSession(SPDataSource ds, String username, String password) throws PLSecurityException, SQLException, IOException {
        logger.debug("Stub call: StubSwingSessionContext.createSession()");
        return null;
    }

    public List<SPDataSource> getDataSources() {
        logger.debug("Stub call: StubSwingSessionContext.getDataSources()");
        return null;
    }

    public String getMatchEngineLocation() {
        logger.debug("Stub call: StubSwingSessionContext.getEngineLocation()");
        return null;
    }

    public Rectangle getFrameBounds() {
        logger.debug("Stub call: StubSwingSessionContext.getFrameBounds()");
        return null;
    }

    public String getLastImportExportAccessPath() {
        logger.debug("Stub call: StubSwingSessionContext.getLastImportExportAccessPath()");
        return null;
    }

    public SPDataSource getLastLoginDataSource() {
        logger.debug("Stub call: StubSwingSessionContext.getLastLoginDataSource()");
        return null;
    }

    public DataSourceCollection getPlDotIni() {
        logger.debug("Stub call: StubSwingSessionContext.getPlDotIni()");
        return null;
    }

    public void setFrameBounds(Rectangle bounds) {
        logger.debug("Stub call: StubSwingSessionContext.setFrameBounds()");
    }

    public void setLastImportExportAccessPath(String lastExportAccessPath) {
        logger.debug("Stub call: StubSwingSessionContext.setLastImportExportAccessPath()");
    }

    public void setLastLoginDataSource(SPDataSource dataSource) {
        logger.debug("Stub call: StubSwingSessionContext.setLastLoginDataSource()");
    }

    public void showDatabaseConnectionManager(Window owner) {
        logger.debug("Stub call: StubSwingSessionContext.showDatabaseConnectionManager()");
    }

    public void showLoginDialog(SPDataSource selectedDataSource) {
        logger.debug("Stub call: StubSwingSessionContext.showLoginDialog()");
    }

	public AbstractMungeComponent getMungeComponent(MungeStep ms,
			FormValidationHandler handler, MatchMakerSession session) {
		logger.debug("Stub call: StubSwingSessionContext.getMungeComponent()");
		return null;
	}

	public MungeStep getMungeStep(Class create, MatchMakerSession session) {
		logger.debug("Stub call: StubSwingSessionContext.getMungeStep()");
		return null;
	}

	public Map<Class, StepDescription> getStepMap() {
		logger.debug("Stub call: StubSwingSessionContext.getStepList()");
		return null;
	}

	public String getEmailSmtpHost() {
		logger.debug("Stub call: StubSwingSessionContext.getEmailSmtpHost()");
		return null;
	}

	public String getEmailSmtpLocalhost() {
		logger.debug("Stub call: StubSwingSessionContext.getEmailSmtpLocalhost()");
		return null;
	}

	public void setEmailSmtpHost(String smtpHost) {
		logger.debug("Stub call: StubSwingSessionContext.setEmailSmtpHost()");
	}

	public void setEmailSmtpLocalhost(String smtpLocalHost) {
		logger.debug("Stub call: StubSwingSessionContext.setEmailSmtpLocalhost()");
	}
}