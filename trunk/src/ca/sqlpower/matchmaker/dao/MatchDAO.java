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

	/**
	 * count match entity by given name
	 * @param name
	 * @return number of match entity by given name
	 */
	public long countMatchByName(String name);
	

	
}
