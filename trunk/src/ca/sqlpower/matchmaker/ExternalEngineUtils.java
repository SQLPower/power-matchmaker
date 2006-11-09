package ca.sqlpower.matchmaker;

import java.io.File;

import ca.sqlpower.architect.CoreUserSettings;
// import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;

public class ExternalEngineUtils {

	private static CoreUserSettings us;

	public static String getProgramPath(EnginePath p) {
        if (p != null) return null; // XXX bypass code for now
		if (us == null) {
			// us = MatchMakerSwingSession.getMainInstance().getUserSettings();
		}
		String plDotIni = us.getPlDotIniPath();
		File plDotIniFile = new File(plDotIni);
		File programDir = plDotIniFile.getParentFile();
		File programPath = new File(programDir, p.getProgName());
		return programPath.toString();
	}
}
