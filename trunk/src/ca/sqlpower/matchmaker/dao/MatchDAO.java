package ca.sqlpower.matchmaker.dao;

import java.util.List;

import ca.sqlpower.matchmaker.Match;

/**
 * The Data access interface for match objects
 *
 * Remember to program to this interface rather than an implemenation
 */

public interface MatchDAO extends MatchMakerDAO<Match> {

	/** 
	 * Find the match that has the match name name 
	 * 
	 * @param name The name of the match we want to find
	 * @return the match with the id name or null if there
	 * 				is no match with that name.
	 */
	public Match findByName(String name);
	
	/**
	 * Finds all the matches that don't have a specified folder
	 * The matches will be ordered by name.
	 * 
	 * @return A list of matches or an empty list if there are no
	 * 			Matches that are folderless.
	 */
	public List<Match> findAllMatchesWithoutFolders();
}
