package ca.sqlpower.matchmaker.swingui;

import java.awt.Rectangle;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.DataSourceCollection;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.sql.PLSchemaException;
import ca.sqlpower.sql.SchemaVersionFormatException;

public interface SwingSessionContext extends MatchMakerSessionContext {

    //////// MaatchMakerSessionContext implementation //////////
    public MatchMakerSwingSession createSession(ArchitectDataSource ds,
            String username, String password) throws PLSecurityException,
            SQLException, ArchitectException, IOException, PLSchemaException, SchemaVersionFormatException;

    public List<ArchitectDataSource> getDataSources();

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
    public void setLastLoginDataSource(ArchitectDataSource dataSource);

    /**
     * Returns the last data source passed to setLastLoginDataSource, even if
     * that happened since the application was last restarted.
     */
    public ArchitectDataSource getLastLoginDataSource();

    /**
     * Shows the database connection manager.  There will only ever be one created
     * no matter how many times you call this method.
     */
    public void showDatabaseConnectionManager();

    /**
     * Shows the login dialog.
     * 
     * @param selectedDataSource The data source that should be selected in the dialog.
     * If null, the dialog's selected data source will remain unchanged.
     */
    public void showLoginDialog(ArchitectDataSource selectedDataSource);

    /**
     * Gets the path of where the engine is located base on CoreUserSettings
     * @return a string of the path of where the engine is located, if not found, returns null 
     */
    public String getEngineLocation();

}