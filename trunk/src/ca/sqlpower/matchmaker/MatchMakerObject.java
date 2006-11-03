package ca.sqlpower.matchmaker;

import java.util.List;

import ca.sqlpower.matchmaker.event.MatchMakerListener;

/**
 * The interface for all of the match maker specific business objects
 *
 */
public interface MatchMakerObject<T extends MatchMakerObject> extends Auditable {
	
	/**
	 * Support for adding match maker event listeners
	 */
	void addMatchMakerListener(MatchMakerListener l);
	
	/**
	 * Support for adding match maker event listeners
	 */
	void removeMatchMakerListener(MatchMakerListener l);
	
	/**
	 * Returns the object's primary children
	 */
	List<T> getChildren();
	
	/**
	 * Get the number of children
	 */
	int getChildCount();
	
	/**
	 * Add a new child to this object
	 */
	void addChild(T child);
	
	/**
	 * remove a child from this object
	 */
	void removeChild(T child);
}
