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

package ca.sqlpower.matchmaker.prefs;

import java.util.Date;
import java.util.prefs.Preferences;
import ca.sqlpower.matchmaker.prefs.PreferencesManager;

import junit.framework.TestCase;

/**
 * Test the PreferencesManager.
 */
public class PreferencesManagerTest extends TestCase {

	/** The object we are testing */
	private PreferencesManager prefsManager;

	public void setUp() throws Exception {
		prefsManager = PreferencesManager.getDefaultInstance();
		PreferencesManager pm2 = PreferencesManager.getDefaultInstance();
		assertNotNull(prefsManager);
		assertSame(prefsManager, pm2);
	}

	/**
	 * Try an end-to-end run of testing.
	 * @throws Exception
	 */
	public void testMega() throws Exception {

		Preferences p = PreferencesManager.getRootNode();

		String date = new Date().toString();
		String key = "fred0000";
		p.put(key, date);

		assertEquals(date, p.get(key, null));
	}
}
