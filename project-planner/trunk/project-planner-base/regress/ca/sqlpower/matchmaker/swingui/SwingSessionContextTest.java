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

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.prefs.Preferences;

import junit.framework.TestCase;
import prefs.PreferencesFactory;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;

public class SwingSessionContextTest extends TestCase {

    SwingSessionContextImpl context;
    
    protected void setUp() throws Exception {
        super.setUp();
        MatchMakerSessionContext stubContext = new MatchMakerSessionContext() {

            public MatchMakerSession createSession(SPDataSource ds, String username, String password) throws PLSecurityException, SQLException, IOException {
                System.out.println("Stub MMSContext.createSession()");
                return null;
            }

            public MatchMakerSession createDefaultSession() {
                System.out.println("Stub MMSContext.createDefaultSession()");
                return null;
            }

			public String getEmailSmtpHost() {
				System.out.println("Stub call: MMSContext.getEmailHost()");
				return null;
			}

			public void setEmailSmtpHost(String host) {
				System.out.println("Stub call: MMSContext.setEmailHost()");
			}

            public List<SPDataSource> getDataSources() {
                return null;
            }

            public DataSourceCollection getPlDotIni() {
                return null;
            }
        };
        System.getProperties().setProperty("java.util.prefs.PreferencesFactory", "prefs.PreferencesFactory");
        PreferencesFactory stubPrefsFactory = new PreferencesFactory();

        Preferences memoryPrefs = stubPrefsFactory.userRoot();
        context = new SwingSessionContextImpl(memoryPrefs, stubContext);
    }
    
}
