/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

import ca.sqlpower.architect.AbstractUserSetting;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;

/**
 * This class is used as a front end to the UserSettings interface
 * to store GUI properties that will be retrieved during startup
 * so that such properties are consistent accross launches.
 */
public class MatchMakerSwingUserSettings extends AbstractUserSetting {
	/**
	 * The horizontal position that the main frame should start at
	 */
	public static final String MAIN_FRAME_X
		= "SwingUserSettings.MAIN_FRAME_X";

	/**
	 * The vertical position that the main frame should start at
	 */
	public static final String MAIN_FRAME_Y
		= "SwingUserSettings.MAIN_FRAME_Y";

	/**
	 * The width that the main frame should start at
	 */
	public static final String MAIN_FRAME_WIDTH
		= "SwingUserSettings.MAIN_FRAME_WIDTH";

	/**
	 * The height that the main frame should start at
	 */
	public static final String MAIN_FRAME_HEIGHT
		= "SwingUserSettings.MAIN_FRAME_HEIGHT";

    public static final String LAST_LOGIN_DATA_SOURCE
            = "last.login.data.source";
    
    public static final String LAST_IMPORT_EXPORT_PATH
            = "last.importexport.path";

    /**
     * The preference key that specifies whether or not auto-login is enabled.
     * The preference is boolean-valued. If the key is missing in the prefs, the
     * default value should be assumed to be true.
     */
    public static final String AUTO_LOGIN_ENABLED = "SwingUserSettings.AUTO_LOGIN_ENABLED";

    /**
     * If auto login is enabled, this is the name of the repository 
     * data source to connect to on startup. If this key is null-valued
     * or missing, the assumed default value should be
     * {@link MatchMakerSessionContext#DEFAULT_REPOSITORY_DATA_SOURCE_NAME}.
     */
    public static final String AUTO_LOGIN_DATA_SOURCE = "SwingUserSettings.AUTO_LOGIN_DATA_SOURCE";

}