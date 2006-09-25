package ca.sqlpower.matchmaker.hibernate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * Hand-constructed base class for Hibernate-generated objects that
 * need to handle ChangeListeners.
 */
public abstract class DefaultHibernateObject implements Comparable {

	List<ChangeListener> listeners = new ArrayList<ChangeListener>();

	public void addChangeListener(ChangeListener l) {
		listeners.add(l);
	}

	public void removeChangeListener(ChangeListener l) {
		listeners.remove(l);
	}

	protected void fireChangeEvent(ChangeEvent e){

		for (ChangeListener l:listeners){
			l.stateChanged(e);
		}
	}

	public List<DefaultHibernateObject> getChildren(){
		return Collections.EMPTY_LIST;
	}

	public int getChildCount(){
		return 0;
	}

	@Override
	 public abstract int hashCode();
	@Override
	public abstract boolean equals(Object obj);
}
