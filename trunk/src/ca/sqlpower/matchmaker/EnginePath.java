package ca.sqlpower.matchmaker;

/**
 * A program to be looked up by ExternalProgramUtils
 */
public enum EnginePath {

	POWERLOADER("ploader_NOT_RIGHT_YET"),
	MATCHMAKER("Match_ODBC");

	private String progName;

	private EnginePath(String path) {
		this.progName = path;
	}

	public String getProgName() {
		return progName;
	}
}