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


package ca.sqlpower.matchmaker.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerObject;

/**
 * Support object to handle the notification of matchmakerlisteners about
 * matchMakerEvents.
 *
 * <p>To use this class, create an instance of it inside your class, delegate
 * the addMatchMakerListener() and removeMatchMakerListener() methods to it, then
 * call this instance's fireXXX methods.  They will create the appropriate
 * event object and deliver it to all of previously-registered listeners in
 * turn (the order of event delivery is the reverse of the order the listeners
 * were added in, but don't count on this for correct client code behaviour!).
 *
 * <p>Comment A (implementation note): Listeners have to be able to remove themselves
 * from the listener list in the course of handling an event.  In order for this to work
 * properly, all fireXXX methods iterate backward through the listener list when firing the
 * events.  Using the Java 5 "enhanced for loop" feature for firing events is therefore not
 * appropriate.
 *
 * @version $Id$
 */
public class MatchMakerEventSupport<T extends MatchMakerObject, C extends MatchMakerObject> {

	private final static Logger logger = Logger.getLogger(MatchMakerEventSupport.class);
	/**
	 * The object that this support class is delivering events for.  This will be the source
	 * of all fired events.
	 */
	private T source;

	/**
	 * The listeners who want to know when events are fired.
	 */
	private List<MatchMakerListener<T, C>> listeners = new ArrayList<MatchMakerListener<T, C>>();

	/**
	 * Creates a new MatchMakerEventSupport object which fires events having <tt>source</tt> as
	 * their source.
	 *
	 * @param source The object that will be using this instance to fire the MatchMaker events.
	 */
	public MatchMakerEventSupport(T source) {
		this.source = source;
	}

	/**
	 * Adds the given listener to the listener list.  The listener will continue to receive
	 * MatchMakerEvent notifications every time this object fires one, until it is removed.
	 * If you add the same listener <tt>n</tt> times, it will receive <tt>n</tt> notifications
	 * each time an event is fired.
	 *
	 * @param l The listener to add.  <tt>null</tt> is not allowed.
	 */
	public void addMatchMakerListener(MatchMakerListener<T, C> l) {
		if (l == null) throw new NullPointerException("Null listener is not allowed");
		listeners.add(l);
	}

	/**
	 * Removes the given listener from the list.  The listener, once removed, will no
	 * longer get notified of MatchMakerEvents from this object, unless it was added more
	 * than once.
	 *
	 * @param l The listener to remove.  If the listener wasn't already registered, this
	 * remove operation will do nothing.
	 */
	public void removeMatchMakerListener(MatchMakerListener<T, C> l) {
		listeners.remove(l);
	}

	/**
	 * This method exists to make better unit tests.  Users of the API should
	 * never need it.
	 */
	public List<MatchMakerListener<T, C>> getListeners() {
		return listeners;
	}

	/**
	 * Fires a mmPropertyChanged event to all listeners unless the oldValue and newValue are
	 * the same.
	 *
	 * @param propertyName The JavaBeans name of the property that might have changed.
	 * @param oldValue The value before the property change.  Can be null.
	 * @param newValue The value after the change. Can be null.
	 */
	public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		if (propertyName == null) throw new NullPointerException("Null property name is not allowed");
		if ( (oldValue == null && newValue == null) ||
			 (oldValue != null && oldValue.equals(newValue))) {
			return;
		}
		MatchMakerEvent<T, C> evt = new MatchMakerEvent<T, C>();
		evt.setSource(source);
		evt.setOldValue(oldValue);
		evt.setNewValue(newValue);
		evt.setPropertyName(propertyName);
		evt.setUndoEvent(source.isUndoing());

		// see class-level comment A
		for (int i = listeners.size() - 1; i >= 0; i--) {
            listeners.get(i).mmPropertyChanged(evt);
		}
	}
	
	/**
	 * Fires a mmPropertyChanged event to all listeners unless the oldValue and newValue are
	 * the same. The event includes an index which is used if it was stored in an indexed 
	 * collection. This should only be used for properties in this case, otherwise, use 
	 * {@link #firePropertyChange(String, Object, Object)}.
	 *
	 * @param propertyName The JavaBeans name of the property that might have changed.
	 * @param oldValue The value before the property change.  Can be null.
	 * @param newValue The value after the change. Can be null.
	 * @param changedIndex The index of the property that might have changed.
	 */
	public void firePropertyChange(String propertyName, int changedIndex, Object oldValue, Object newValue) {
		if (propertyName == null) throw new NullPointerException("Null property name is not allowed");
		if ( (oldValue == null && newValue == null) ||
			 (oldValue != null && oldValue.equals(newValue))) {
			return;
		}
		MatchMakerEvent<T, C> evt = new MatchMakerEvent<T, C>();
		evt.setSource(source);
		evt.setOldValue(oldValue);
		evt.setNewValue(newValue);
		evt.setPropertyName(propertyName);
		evt.setUndoEvent(source.isUndoing());
		evt.setChangeIndices(new int[] {changedIndex});

		// see class-level comment A
		for (int i = listeners.size() - 1; i >= 0; i--) {
            listeners.get(i).mmPropertyChanged(evt);
		}
	} 

	/**
	 * Fires a mmChildrenInserted event to all listeners.
	 *
	 * @param childPropertyName The name of the list property that has one or more new entries.
	 * @param insertedIndices The indices of the new list entries
	 * @param insertedChildren The actual items inserted into the list (the position of the objects
	 * in this list correspond with the indices specified in the <tt>insertedIndices</tt> array).
	 * @throws IllegalArgumentException if the <tt>insertedIndices</tt> array and the <tt>insertedChildren</tt>
	 * list differ in length.
	 */
	public void fireChildrenInserted(String childPropertyName, int[] insertedIndices, List<C> insertedChildren) {
		fireChildrenInserted(childPropertyName, insertedIndices, insertedChildren, false);
	}

	/**
	 * Fires a mmChildrenInserted event to all listeners.
	 * 
	 * @param childPropertyName
	 *            The name of the list property that has one or more new
	 *            entries.
	 * @param insertedIndices
	 *            The indices of the new list entries
	 * @param insertedChildren
	 *            The actual items inserted into the list (the position of the
	 *            objects in this list correspond with the indices specified in
	 *            the <tt>insertedIndices</tt> array).
	 * @param isCompound
	 *            True if this childInserted event is part of a compound edit.
	 *            False if it is not.
	 * @throws IllegalArgumentException
	 *             if the <tt>insertedIndices</tt> array and the
	 *             <tt>insertedChildren</tt> list differ in length.
	 */
	public void fireChildrenInserted(String childPropertyName, int[] insertedIndices, List<C> insertedChildren, boolean isCompoundEvent) {
		if (childPropertyName == null) throw new NullPointerException("Null property name is not allowed");
		if (insertedIndices.length != insertedChildren.size()) {
			throw new IllegalArgumentException(
					"insertetdIndices (length="+insertedIndices.length+") must be same " +
					"length as insertedChildren (size="+insertedChildren.size()+")");
		}

		MatchMakerEvent<T, C> evt = new MatchMakerEvent<T, C>();
		evt.setSource(source);
		evt.setChangeIndices(insertedIndices);
		evt.setPropertyName(childPropertyName);
		evt.setChildren(insertedChildren);
		evt.setUndoEvent(source.isUndoing());
		evt.setCompoundEvent(isCompoundEvent);
		if (logger.isDebugEnabled()){
		    logger.debug("Firing children inserted for object "+source);
		}
        
		// see class-level comment A
		for (int i = listeners.size() - 1; i >= 0; i--) {            
            logger.debug("fireChildrenInserted calling listeners");
			listeners.get(i).mmChildrenInserted(evt);
		}
	}

	/**
	 * Fires a mmChildrenRemoved event to all listeners.
	 *
	 * @param childPropertyName The name of the list property that has had one or more entries removed.
	 * @param removedIndices The indices of the removed list entries
	 * @param removedChildren The actual items removed from the list (the position of the objects
	 * in this list correspond with the indices specified in the <tt>insertedIndices</tt> array).
	 * @throws IllegalArgumentException if the <tt>removedIndices</tt> array and the <tt>removedChildren</tt>
	 * list differ in length.
	 */
	public void fireChildrenRemoved(String childPropertyName, int[] removedIndices, List<C> removedChildren) {
		if (childPropertyName == null) throw new NullPointerException("Null property name is not allowed");
		if (removedIndices.length != removedChildren.size()) {
			throw new IllegalArgumentException(
					"removedIndices (length="+removedIndices.length+") must be same " +
					"length as removedChildren (size="+removedChildren.size()+")");
		}

		MatchMakerEvent<T, C> evt = new MatchMakerEvent<T, C>();
		evt.setSource(source);
		evt.setChangeIndices(removedIndices);
		evt.setPropertyName(childPropertyName);
		evt.setChildren(removedChildren);
		evt.setUndoEvent(source.isUndoing());
		
		// see class-level comment A
		for (int i = listeners.size() - 1; i >= 0; i--) {
            logger.debug("fireChildrenRemoved calling listeners");
			listeners.get(i).mmChildrenRemoved(evt);
		}
	}

	/**
	 * fires an mmStructureChanged event to all listeners.
	 */
	public void fireStructureChanged() {
		MatchMakerEvent<T, C> evt = new MatchMakerEvent<T, C>();
		evt.setSource(source);
		evt.setUndoEvent(source.isUndoing());
		
		// see class-level comment A
		for (int i = listeners.size() - 1; i >= 0; i--) {
			if (logger.isDebugEnabled()) {
				logger.debug("fireStructureChanged: source="+source+"; listener="+ listeners.get(i));
			}
			listeners.get(i).mmStructureChanged(evt);
		}
	}

}
