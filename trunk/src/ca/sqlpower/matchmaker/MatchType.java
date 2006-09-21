package ca.sqlpower.matchmaker;

public enum MatchType {

	FIND_DUPLICATES("FIND DUPLICATES"),
	BUILD_CROSS_REFERENCE("BUILD CROSS-REFERENCE");

	String name;

	MatchType(String name)  {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
