package ca.sqlpower.matchmaker.swingui;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import junit.framework.TestCase;
import prefs.PreferencesFactory;
import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.CoreUserSettings;
import ca.sqlpower.architect.DataSourceCollection;
import ca.sqlpower.architect.DatabaseListChangeListener;
import ca.sqlpower.architect.JDBCClassLoader;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.dao.hibernate.HibernateTestUtil;
import ca.sqlpower.security.PLSecurityException;

public class SwingSessionContextTest extends TestCase {

    private static final String FAKE_ENGINE_LOCATION = "you found the delegate's engine location!";
    SwingSessionContextImpl context;
    
    protected void setUp() throws Exception {
        super.setUp();
        ArchitectSession archSession = new ArchitectSession() {

            public boolean addDriverJar(String fullPath) {
                // TODO Auto-generated method stub
                return false;
            }

            public void clearDriverJarList() {
                // TODO Auto-generated method stub
                
            }

            public List<String> getDriverJarList() {
                // TODO Auto-generated method stub
                return null;
            }

            public JDBCClassLoader getJDBCClassLoader() {
                // TODO Auto-generated method stub
                return null;
            }

            public CoreUserSettings getUserSettings() {
                // TODO Auto-generated method stub
                return null;
            }

            public void removeAllDriverJars() {
                // TODO Auto-generated method stub
                
            }

            public boolean removeDriverJar(String fullPath) {
                // TODO Auto-generated method stub
                return false;
            }

            public void setUserSettings(CoreUserSettings argUserSettings) {
                // TODO Auto-generated method stub
                
            }
            
        };
        final DataSourceCollection dsCollection = new DataSourceCollection() {

            public void addDataSource(ArchitectDataSource dbcs) {
                System.out.println("Stub DSCollection.addDataSource()");
            }

            public void addDatabaseListChangeListener(DatabaseListChangeListener l) {
                System.out.println("Stub DSCollection.addDatabaseListChangeListener()");
            }

            public List<ArchitectDataSource> getConnections() {
                System.out.println("Stub DSCollection.getConnections()");
                List<ArchitectDataSource> connections = new ArrayList<ArchitectDataSource>();
                connections.add(HibernateTestUtil.getOracleDS());
                return connections;
            }

            public ArchitectDataSource getDataSource(String name) {
                System.out.println("Stub DSCollection.getDataSource()");
                return null;
            }

            public void mergeDataSource(ArchitectDataSource dbcs) {
                System.out.println("Stub DSCollection.mergeDataSource()");
            }

            public void read(File location) throws IOException {
                System.out.println("Stub DSCollection.read("+location+")");
            }

            public void removeDataSource(ArchitectDataSource dbcs) {
                System.out.println("Stub DSCollection.removeDataSource()");
            }

            public void removeDatabaseListChangeListener(DatabaseListChangeListener l) {
                System.out.println("Stub DSCollection.removeDatabaseListChangeListener()");
            }

            public void write(File location) throws IOException {
                System.out.println("Stub DSCollection.write("+location+")");
            }

            public void write() throws IOException {
                // TODO Auto-generated method stub          
            }
            
        };
        MatchMakerSessionContext stubContext = new MatchMakerSessionContext() {

            public MatchMakerSession createSession(ArchitectDataSource ds, String username, String password) throws PLSecurityException, SQLException, ArchitectException, IOException {
                System.out.println("Stub MMSContext.createSession()");
                return null;
            }

            public List<ArchitectDataSource> getDataSources() {
                System.out.println("Stub MMSContext.getDataSources()");
                return dsCollection.getConnections();
            }

            public String getMatchEngineLocation() {
                System.out.println("Stub MMSContext.getEngineLocation()");
                return FAKE_ENGINE_LOCATION;
            }

            public DataSourceCollection getPlDotIni() {
                System.out.println("Stub MMSContext.getPlDotIni()");
                return dsCollection;
            }

			public String getEmailEngineLocation() {
				System.out.println("Stub call: .getEmailEngineLocation()");
				return FAKE_ENGINE_LOCATION;
			}
        };
        System.getProperties().setProperty("java.util.prefs.PreferencesFactory", "prefs.PreferencesFactory");
        PreferencesFactory stubPrefsFactory = new PreferencesFactory();
        Preferences memoryPrefs = stubPrefsFactory.userRoot();
        context = new SwingSessionContextImpl(archSession, memoryPrefs, stubContext);
    }
    
    public void testGetLastLogin(){                
        ArchitectDataSource ds = HibernateTestUtil.getOracleDS();
        context.setLastLoginDataSource(ds);        
        ArchitectDataSource actualDS = context.getLastLoginDataSource();
        assertEquals("Did not remember the last login", ds, actualDS);
    }
    
    public void testGetEngineLocation() {
        assertEquals(FAKE_ENGINE_LOCATION, context.getMatchEngineLocation());        
    }
    
    public void testGetEmailEngineLocation() {
        assertEquals(FAKE_ENGINE_LOCATION, context.getEmailEngineLocation());        
    }
    
    public void testGetLastImportExportAccessPath(){
        context.setLastImportExportAccessPath("/thisisatestpath");
        assertEquals ("Last import/export access path is incorrect", "/thisisatestpath",
                            context.getLastImportExportAccessPath());
    }
    
}
