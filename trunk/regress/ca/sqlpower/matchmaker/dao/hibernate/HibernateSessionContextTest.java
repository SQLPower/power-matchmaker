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

package ca.sqlpower.matchmaker.dao.hibernate;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.DBTestUtil;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SpecificDataSourceCollection;

public class HibernateSessionContextTest extends TestCase {

    /**
     * The session context we're testing.
     */
	private MatchMakerSessionContext ctx;
    
    /**
     * The sole data source the setUp() method puts in the session context.  Provided
     * here for convenience.  You could get the same data source with ctx.getDataSources().get(0).
     */
	private JDBCDataSource ds;
    
	/**
	 * The Preferences node that we will use in this test. We want to keep
	 * this separate from the regular MatchMaker Preferences to ensure the test
	 * suite doesn't interfere with the user's preferences.
	 */
	Preferences prefs = Preferences.userNodeForPackage(MatchMakerSessionContextImpl.class).node("test");
	
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DataSourceCollection<JDBCDataSource> ini = new SpecificDataSourceCollection<JDBCDataSource>(new PlDotIni(), JDBCDataSource.class);
        ds = DBTestUtil.getOracleDS();
        ini.addDataSource(ds);
        ctx = new MatchMakerSessionContextImpl(prefs, ini);
    }
    
    public void testGetDataSources() {
        assertNotNull(ctx.getDataSources());
    }
    
	public void testCreateSession() throws Exception {
		MatchMakerSession session = ctx.createSession();
		assertNotNull(session);
	}
    
    /**
     * The context might have to alter the username and password in the data source
     * we give it, but we don't want this to permanently alter the settings in pl.ini.
     * This test insures the data source we give it remains untouched.
     */
    public void testDataSourceNotModifiedByLogin() {
        Map<String, String> originalProps = new HashMap<String, String>(ds.getPropertiesMap());
        try {
            ctx.createSession();
        } catch (Exception ex) {
            // the username/password is wrong, so the login will fail. That's not what we're testing
        }
        assertEquals("DataSource should not have been modified", originalProps, ds.getPropertiesMap());
    }
    
    public void testGetHibernateSessionFactory() throws Exception{
        MatchMakerSession mmSession = ctx.createSession();
        //we're testing this since the purpose of the getHibernateSessionFactory is to
        //initialize the PlFolder
        assertNotNull(mmSession.getCurrentFolderParent().getChildren());
    }
}