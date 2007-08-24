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

package ca.sqlpower.matchmaker.swingui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import prefs.PreferencesFactory;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.DBTestUtil;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.DatabaseListChangeListener;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SPDataSourceType;

public class SwingSessionContextTest extends TestCase {

    private static final String FAKE_ENGINE_LOCATION = "you found the delegate's engine location!";
    SwingSessionContextImpl context;
    
    protected void setUp() throws Exception {
        super.setUp();
        final DataSourceCollection dsCollection = new DataSourceCollection() {

            public void addDataSource(SPDataSource spds) {
                System.out.println("Stub DSCollection.addDataSource("+spds+")");
            }

			public void addDataSourceType(SPDataSourceType spdst) {
                System.out.println("Stub DSCollection.addDataSourceType("+spdst+")");
			}

            public void addDatabaseListChangeListener(DatabaseListChangeListener l) {
                System.out.println("Stub DSCollection.addDatabaseListChangeListener("+l+")");
            }

            public List<SPDataSource> getConnections() {
                System.out.println("Stub DSCollection.getConnections()");
                List<SPDataSource> connections = new ArrayList<SPDataSource>();
                connections.add(DBTestUtil.getOracleDS());
                return connections;
            }

			public List<SPDataSourceType> getDataSourceTypes() {
                System.out.println("Stub DSCollection.getConnections()");
                List<SPDataSourceType> connectionTypes = new ArrayList<SPDataSourceType>();
                connectionTypes.add(DBTestUtil.getOracleDS().getParentType());
                return connectionTypes;
			}

            public SPDataSource getDataSource(String name) {
                System.out.println("Stub DSCollection.getDataSource("+name+")");
                return null;
            }

            public void mergeDataSource(SPDataSource spds) {
                System.out.println("Stub DSCollection.mergeDataSource("+spds+")");
            }

            public void mergeDataSourceType(SPDataSourceType spdst) {
                System.out.println("Stub DSCollection.mergeDataSourceType("+spdst+")");
            }

            public void read(File location) throws IOException {
                System.out.println("Stub DSCollection.read("+location+")");
            }

            public void read(InputStream in) throws IOException {
                System.out.println("Stub DSCollection.read("+in+")");
            }
            
            public void removeDataSource(SPDataSource spds) {
                System.out.println("Stub DSCollection.removeDataSource("+spds+")");
            }
            
            public boolean removeDataSourceType(SPDataSourceType spdst) {
                System.out.println("Stub DSCollection.removeDataSourceType("+spdst+")");
                return false;
            }

            public void removeDatabaseListChangeListener(DatabaseListChangeListener l) {
                System.out.println("Stub DSCollection.removeDatabaseListChangeListener("+l+")");
            }

            public void write(File location) throws IOException {
                System.out.println("Stub DSCollection.write("+location+")");
            }

            public void write() throws IOException {
                System.out.println("Stub DSCollection.write()");
            }

        };
        MatchMakerSessionContext stubContext = new MatchMakerSessionContext() {

            public MatchMakerSession createSession(SPDataSource ds, String username, String password) throws PLSecurityException, SQLException, IOException {
                System.out.println("Stub MMSContext.createSession()");
                return null;
            }

            public List<SPDataSource> getDataSources() {
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
        context = new SwingSessionContextImpl(memoryPrefs, stubContext);
    }
    
    public void testGetLastLogin(){                
        SPDataSource ds = DBTestUtil.getOracleDS();
        context.setLastLoginDataSource(ds);        
        SPDataSource actualDS = context.getLastLoginDataSource();
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
