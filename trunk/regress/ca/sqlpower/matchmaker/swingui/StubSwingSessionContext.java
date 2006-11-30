package ca.sqlpower.matchmaker.swingui;

import java.awt.Rectangle;
import java.awt.Window;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.DataSourceCollection;
import ca.sqlpower.security.PLSecurityException;

/**
 * A stub of SwingSessionContext.  None of the methods return useful values, but they all
 * log calls on at the debug level.  Override methods you want in your test cases.
 */
public class StubSwingSessionContext implements SwingSessionContext {

    Logger logger = Logger.getLogger(StubSwingSessionContext.class);

    public MatchMakerSwingSession createSession(ArchitectDataSource ds, String username, String password) throws PLSecurityException, SQLException, ArchitectException, IOException {
        logger.debug("Stub call: StubSwingSessionContext.createSession()");
        return null;
    }

    public List<ArchitectDataSource> getDataSources() {
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

    public ArchitectDataSource getLastLoginDataSource() {
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

    public void setLastLoginDataSource(ArchitectDataSource dataSource) {
        logger.debug("Stub call: StubSwingSessionContext.setLastLoginDataSource()");
    }

    public void showDatabaseConnectionManager(Window owner) {
        logger.debug("Stub call: StubSwingSessionContext.showDatabaseConnectionManager()");
    }

    public void showLoginDialog(ArchitectDataSource selectedDataSource) {
        logger.debug("Stub call: StubSwingSessionContext.showLoginDialog()");
    }

	public String getEmailEngineLocation() {
		logger.debug("Stub call: StubSwingSessionContext.getEmailEngineLocation()");
		return null;
	}
}