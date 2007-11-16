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

import ca.sqlpower.architect.AbstractUserSetting;

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
}