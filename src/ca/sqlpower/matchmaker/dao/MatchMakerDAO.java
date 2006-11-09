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
