package ca.sqlpower.matchmaker.dao;

import ca.sqlpower.matchmaker.PlFolder;
/**
 * The Data access interface for PlFolders objects
 *
 * Remember to program to this interface rather than an implemenation
 */
public interface PlFolderDAO extends MatchMakerDAO<PlFolder> {
	
	/** 
	 * Find the PlFolder that has the PlFolder name name 
	 * 
	 * @param name The name of the PlFolder we want to find
	 * @return the PlFolder with the id name or null if there
	 * 				is no match with that name.
	 */
	public PlFolder findByName(String name);

}
