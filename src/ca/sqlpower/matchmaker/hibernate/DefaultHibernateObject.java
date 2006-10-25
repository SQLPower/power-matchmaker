package ca.sqlpower.matchmaker.hibernate;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Hand-constructed base class for Hibernate-generated objects that
 * need to handle ChangeListeners.
 */
public abstract class DefaultHibernateObject<C extends DefaultHibernateObject> {

	PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	Set<PropertyChangeListener> hierachicalListeners = new HashSet<PropertyChangeListener>();



	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(propertyName, listener);
	}

	public void fireIndexedPropertyChange(String propertyName, int index, boolean oldValue, boolean newValue) {
		pcs.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
	}

	public void fireIndexedPropertyChange(String propertyName, int index, int oldValue, int newValue) {
		pcs.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
	}

	public void fireIndexedPropertyChange(String propertyName, int index, Object oldValue, Object newValue) {
		pcs.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
	}

	public void firePropertyChange(PropertyChangeEvent evt) {
		pcs.firePropertyChange(evt);
	}

	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
		pcs.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void firePropertyChange(String propertyName, int oldValue, int newValue) {
		pcs.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		pcs.firePropertyChange(propertyName, oldValue, newValue);
	}

	public PropertyChangeListener[] getPropertyChangeListeners() {
		return pcs.getPropertyChangeListeners();
	}

	public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
		return pcs.getPropertyChangeListeners(propertyName);
	}

	public boolean hasListeners(String propertyName) {
		return pcs.hasListeners(propertyName);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(propertyName, listener);
	}



	public List<C> getChildren(){
		List<C> emptyList = Collections.emptyList();
		return emptyList;
	}

	public void addAllHierachialChangeListener(List<PropertyChangeListener> listeners){
		for (PropertyChangeListener l: listeners){
			addHierachialChangeListener(l);
		}
	}

	public void addHierachialChangeListener(PropertyChangeListener l){

		pcs.addPropertyChangeListener(l);

		hierachicalListeners.add(l);
		for (DefaultHibernateObject obj: getChildren()){
			obj.addHierachialChangeListener(l);
		}
	}

	public void removeAllHierachialChangeListener(List<PropertyChangeListener> listeners){

		for (PropertyChangeListener l: listeners){
			removeHierachialChangeListener(l);
		}
	}

	public List<PropertyChangeListener> getHierachialChangeListeners(){
		return Collections.unmodifiableList(new ArrayList<PropertyChangeListener>(hierachicalListeners));
	}

	public void removeHierachialChangeListener(PropertyChangeListener l){
		hierachicalListeners.remove(l);
		pcs.removePropertyChangeListener(l);
		for (DefaultHibernateObject obj: getChildren()){
			obj.removeHierachialChangeListener(l);
		}
	}

	public int getChildCount(){
		return 0;
	}

	/** Canonical hashCode(), made abstract to require subclasses to implement.
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public abstract int hashCode();

	/** Canonical equals(), made abstract to require subclasses to implement.
	 * @see java.lang.Object#equals()
	 */
	@Override
	public abstract boolean equals(Object obj);
}
