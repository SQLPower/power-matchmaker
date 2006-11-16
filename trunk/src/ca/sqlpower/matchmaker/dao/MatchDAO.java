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
	 * Finds all the matches that don't have a specified folder
	 * The matches will be ordered by name.
	 *
	 * @return A list of matches or an empty list if there are no
	 * 			Matches that are folderless.
	 */
	public List<Match> findAllMatchesWithoutFolders();

	/**
	 * Finds the match having the given name (case sensitive).
	 * @param name The name of the match to look for
	 * @return The match object with the given name, or null if there
	 * is no such match.
	 */
	public Match findByName(String name);

	/**
	 * check to see if there is any match under given name
	 * @param name
	 * @return true if no match found under given name, false otherwise
	 */
	public boolean isThisMatchNameAcceptable(String name);
}
