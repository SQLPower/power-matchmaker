package ca.sqlpower.matchmaker.hibernate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * Hand-constructed base class for Hibernate-generated objects that
 * need to handle ChangeListeners.
 */
public abstract class DefaultHibernateObject implements Comparable {

	List<ChangeListener> listeners = new ArrayList<ChangeListener>();
	Set<ChangeListener> hierachialListeners = new HashSet<ChangeListener>();

	public void addChangeListener(ChangeListener l) {
		if (!listeners.contains(l)){
			listeners.add(l);
		}
	}

	public void removeChangeListener(ChangeListener l) {
		listeners.remove(l);
	}

	protected void fireChangeEvent(ChangeEvent e){
		for (int i = listeners.size(); i>=0;i--){
			listeners.get(i).stateChanged(e);
		}
	}

	public List<DefaultHibernateObject> getChildren(){
		return Collections.EMPTY_LIST;
	}
	
	public void addAllHierachialChangeListener(List<ChangeListener> listeners){
		for (ChangeListener l: listeners){
			addHierachialChangeListener(l);
		}
	}
	
	public void addHierachialChangeListener(ChangeListener l){
		if (!listeners.contains(l)){
			listeners.add(l);
		}
		hierachialListeners.add(l);
		for (DefaultHibernateObject obj: getChildren()){
			obj.addHierachialChangeListener(l);
		}
	}
	
	public void removeAllHierachialChangeListener(List<ChangeListener> listeners){
		
		for (ChangeListener l: listeners){
			removeHierachialChangeListener(l);
		}
	}
	
	public List<ChangeListener> getHierachialChangeListeners(){
		return Collections.unmodifiableList(new ArrayList<ChangeListener>(hierachialListeners));
	}
	
	public void removeHierachialChangeListener(ChangeListener l){
		hierachialListeners.remove(l);
		listeners.remove(l);
		for (DefaultHibernateObject obj: getChildren()){
			obj.removeHierachialChangeListener(l);
		}
	}
	
	public int getChildCount(){
		return 0;
	}

	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object obj);
}
