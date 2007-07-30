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