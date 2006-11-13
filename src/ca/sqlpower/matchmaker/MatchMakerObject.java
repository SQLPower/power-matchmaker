package ca.sqlpower.matchmaker;

import java.util.List;

import ca.sqlpower.matchmaker.event.MatchMakerListener;

/**
 * The interface for all of the match maker specific business objects
 *
 * @param T The type of this implementation of MatchMakerObject
 * @param C The type of children this implementation of MatchMakerObject contains
 */
public interface MatchMakerObject<T extends MatchMakerObject, C extends MatchMakerObject> extends Auditable {

	/**
	 * Support for adding match maker event listeners
	 */
	void addMatchMakerListener(MatchMakerListener<T, C> l);

	/**
	 * Support for adding match maker event listeners
	 */
	void removeMatchMakerListener(MatchMakerListener<T, C> l);

	/**
	 * get the parent of this object
	 */
	MatchMakerObject getParent();

	/**
	 * Set the parent (ie. the object that holds this one as a child)
	 */
	 void setParent(MatchMakerObject parent);

	 /**
	  * @return true if this MatchMakerObject allows children, false otherwise.
	  */
	 public boolean allowsChildren();

	/**
	 * Returns the object's primary children
	 */
	List<C> getChildren();

	/**
	 * Get the number of children
	 */
	int getChildCount();

	/**
	 * Add a new child to this object
	 */
	void addChild(C child);

	/**
	 * remove a child from this object
	 */
	void removeChild(C child);

	/**
	 * add this object into the session
	 */
	public void setSession(MatchMakerSession matchMakerSession);
}
