package ca.sqlpower.matchmaker;

import ca.sqlpower.architect.SQLTable;

public class Match extends AbstractMatchMakerObject<MatchMakerFolder> {

	public enum MatchType {
		FIND_DUPES("Find Duplicates"), 
		BUILD_XREF("Build Cross-Reference");
		
		String displayName;
		
		private MatchType(String displayName) {
			this.displayName=displayName;
		}
		
		@Override
		public String toString() {
			return displayName;
		}
	}

	/** 
	 * The id of this object.  It must be unique across all match objects
	 */
	String name;
	
	/**
	 * The type of match
	 */
	MatchType type;
	
	/**
	 * A little not on this match object
	 */
	String description;
	
	/**
	 * The table where the engine stores the results of a match
	 */
	SQLTable resultTable;
	
	
	
	
	
	public Match(String appUserName) {
		super(appUserName);
		
	}
	
	

}
