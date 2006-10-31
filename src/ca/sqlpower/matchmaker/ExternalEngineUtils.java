package ca.sqlpower.matchmaker;

import java.io.File;

import ca.sqlpower.architect.CoreUserSettings;
import ca.sqlpower.matchmaker.swingui.MatchMakerFrame;

public class ExternalEngineUtils {

	private static CoreUserSettings us;

	public static String getProgramPath(EnginePath p) {
		if (us == null) {
			us = MatchMakerFrame.getMainInstance().getUserSettings();
		}
		String plDotIni = us.getPlDotIniPath();
		File plDotIniFile = new File(plDotIni);
		File programDir = plDotIniFile.getParentFile();
		File programPath = new File(programDir, p.toString());
		return programPath.toString();
	}
}
