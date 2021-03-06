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

package ca.sqlpower.matchmaker.swingui;

import java.awt.Rectangle;
import java.awt.Window;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.prefs.PreferenceChangeListener;

import org.apache.log4j.Logger;

import ca.sqlpower.enterprise.client.ProjectLocation;
import ca.sqlpower.enterprise.client.SPServerInfo;
import ca.sqlpower.matchmaker.MatchMakerConfigurationException;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.enterprise.MatchMakerClientSideSession;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.swingui.munge.AbstractMungeComponent;
import ca.sqlpower.matchmaker.swingui.munge.StepDescription;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.validation.swingui.FormValidationHandler;

/**
 * A stub of SwingSessionContext.  None of the methods return useful values, but they all
 * log calls on at the debug level.  Override methods you want in your test cases.
 */
public class StubSwingSessionContext implements SwingSessionContext {

    Logger logger = Logger.getLogger(StubSwingSessionContext.class);

    public MatchMakerSwingSession createSession(JDBCDataSource ds, String username, String password) throws PLSecurityException, SQLException {
        logger.debug("Stub call: StubSwingSessionContext.createSession()");
        return null;
    }

    public List<JDBCDataSource> getDataSources() {
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

	public Map<Class, StepDescription> getStepMap() {
		logger.debug("Stub call: StubSwingSessionContext.getStepList()");
		return null;
	}

	public String getEmailSmtpHost() {
		logger.debug("Stub call: StubSwingSessionContext.getEmailSmtpHost()");
		return null;
	}

	public void setEmailSmtpHost(String smtpHost) {
		logger.debug("Stub call: StubSwingSessionContext.setEmailSmtpHost()");
	}

    public boolean isAutoLoginEnabled() {
        logger.debug("Stub call: StubSwingSessionContext.isAutoLoginEnabled()");
        return false;
    }

    public void setAutoLoginEnabled(boolean enabled) {
        logger.debug("Stub call: StubSwingSessionContext.setAutoLoginEnabled()");
    }

    public void launchDefaultSession() {
        logger.debug("Stub call: StubSwingSessionContext.launchDefaultSession()");
    }

    public MatchMakerSwingSession createDefaultSession() {
        logger.debug("Stub call: StubSwingSessionContext.createDefaultSession()");
        return null;
    }

    public SPDataSource getAutoLoginDataSource() {
        logger.debug("Stub call: StubSwingSessionContext.getAutoLoginDataSource()");
        return null;
    }

    public void setAutoLoginDataSource(SPDataSource selectedItem) {
        logger.debug("Stub call: StubSwingSessionContext.setAutoLoginDataSource()");
    }

	public Collection<MatchMakerSession> getSessions() {
		logger.debug("Stub call: StubSwingSessionContext.getSessions()");
		return null;
	}

	public void closeAll() {
		logger.debug("Stub call: StubSwingSessionContext.closeAll()");
	}

	public SessionLifecycleListener<MatchMakerSession> getSessionLifecycleListener() {
		logger.debug("Stub call: StubSwingSessionContext.getSessionLifecycleListener()");
		return null;
	}
	
	public void ensureDefaultRepositoryDefined() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: StubSwingSessionContext.ensureDefaultRepositoryDefined()");
	}
	
	public void setAddressCorrectionDataPath(String path) {
		logger.debug("Stub call: StubSwingSessionContext.setAddressCorrectionDataPath()");
		
	}
	
	public String getAddressCorrectionDataPath() {
		logger.debug("Stub call: StubSwingSessionContext.getAddressCorrectionDataPath()");
		return null;
	}

	public void addPreferenceChangeListener(PreferenceChangeListener l) {
		logger.debug("Stub call: MatchMakerSessionContext.addPreferenceChangeListener()");
		
	}

	public void removePreferenceChangeListener(PreferenceChangeListener l) {
		logger.debug("Stub call: MatchMakerSessionContext.removePreferenceChangeListener()");
		
	}
	
	@Override
	public MatchMakerClientSideSession createSecuritySession(
			SPServerInfo serverInfo) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: StubSwingSessionContext.createSecuritySession()");
		return null;
	}
	
	@Override
	public MatchMakerSwingSession createServerSession(
			ProjectLocation projectLocation) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: StubSwingSessionContext.createServerSession()");
		return null;
	}

	@Override
	public MatchMakerSwingSession createSession() throws PLSecurityException,
			SQLException, SQLObjectException, MatchMakerConfigurationException {
		// TODO Auto-generated method stub
		logger.debug("Stub call: SwingSessionContext.createSession()");
		return null;
	}

	@Override
	public MungeStep getMungeStep(Class<? extends MungeStep> create) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: SwingSessionContext.getMungeStep()");
		return null;
	}
}