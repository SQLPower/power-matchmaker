/*
 * Copyright (c) 2008, SQL Power Group Inc.
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
/**
 * This is the generic interface that all DAOs in the match maker should use
 * It provides basic access to the data store.
 * 
 * @param <T> The type of the object that is to be returned
 */
public interface MatchMakerDAO<T> {
	/**
	 * The class that this DAO accesses
	 * 
	 * @return the class of the objects that are returned
	 */
	public Class<T> getBusinessClass();
	
	/**
	 * Find all objects of type T in the database
	 * 
	 * @return A list of all the objects of type T in the data store
	 */
	public List<T> findAll();

	/**
	 * Make sure this object is no longer in the data store
	 * 
	 * @param deleteMe The object to be deleted
	 */
	public void delete(T deleteMe);
	
	/**
	 * Save the object saveMe back to the data store, adding it if necessary
	 * 
	 * @param saveMe The object to be saved
	 */
	public void save(T saveMe);

}
