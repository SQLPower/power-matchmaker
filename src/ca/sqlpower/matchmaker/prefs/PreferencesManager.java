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


package ca.sqlpower.matchmaker.prefs;

import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

/**
 * Locate the User Preferences Node for the Architect package, and
 * pass it around to people that want it via load-time injection.
 */
public class PreferencesManager {

	private static final Logger logger = Logger.getLogger(PreferencesManager.class);

	private static final PreferencesManager singleton = new PreferencesManager();

    private final static Preferences prefs =
    	// ArchitectSession is not a copy-and-paste error here:
    	Preferences.userNodeForPackage(ca.sqlpower.architect.ArchitectSession.class);

    private PreferencesManager() {
		// private constructor, is a singleton
		logger.info("Create PreferencesManager singleton");
	}

	public static PreferencesManager getDefaultInstance() {
		return singleton;
	}

	public static Preferences getRootNode() {
		return prefs;
	}

}
