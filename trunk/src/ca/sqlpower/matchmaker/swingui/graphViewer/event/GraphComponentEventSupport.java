package ca.sqlpower.matchmaker.swingui.graphViewer.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.swingui.graphViewer.Diedge;
import ca.sqlpower.matchmaker.swingui.graphViewer.GraphComponent;

/**
 * Support object to handle the notification of graphComponentListeners about
 * graphComponentEvents.
 *
 * <p>To use this class, create an instance of it inside your class, delegate
 * the addGraphComponentListener() and removeGraphComponentListener() methods to it, then
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
public class GraphComponentEventSupport {

	private final static Logger logger = Logger.getLogger(GraphComponentEventSupport.class);
	/**
	 * The object that this support class is delivering events for.  This will be the source
	 * of all fired events.
	 */
	private GraphComponent source;

	/**
	 * The listeners who want to know when events are fired.
	 */
	private List<GraphComponentListener> listeners = new ArrayList<GraphComponentListener>();

	/**
	 * Creates a new GraphComponentEventSupport object which fires events having <tt>source</tt> as
	 * their source.
	 *
	 * @param source The object that will be using this instance to fire the GraphComponent events.
	 */
	public GraphComponentEventSupport(GraphComponent source) {
		this.source = source;
	}

	/**
	 * Adds the given listener to the listener list.  The listener will continue to receive
	 * GraphComponentEvent notifications every time this object fires one, until it is removed.
	 * If you add the same listener <tt>n</tt> times, it will receive <tt>n</tt> notifications
	 * each time an event is fired.
	 *
	 * @param l The listener to add.  <tt>null</tt> is not allowed.
	 */
	public void addGraphComponentListener(GraphComponentListener l) {
		if (l == null) throw new NullPointerException("Null listener is not allowed");
		listeners.add(l);
	}

	/**
	 * Removes the given listener from the list.  The listener, once removed, will no
	 * longer get notified of GraphComponentEvents from this object, unless it was added more
	 * than once.
	 *
	 * @param l The listener to remove.  If the listener wasn't already registered, this
	 * remove operation will do nothing.
	 */
	public void removeGraphComponentListener(GraphComponentListener l) {
		listeners.remove(l);
	}

	/**
	 * This method exists to make better unit tests.  Users of the API should
	 * never need it.
	 */
	public List<GraphComponentListener> getListeners() {
		return listeners;
	}

	/**
	 * Fires a gcPropertyChanged event to all listeners unless the oldValue and newValue are
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
		GraphComponentEvent evt = new GraphComponentEvent();
		evt.setSource(source);
		evt.setOldValue(oldValue);
		evt.setNewValue(newValue);
		evt.setPropertyName(propertyName);

		// see class-level comment A
		for (int i = listeners.size() - 1; i >= 0; i--) {
			listeners.get(i).gcPropertyChanged(evt);
		}
	}

	public void fireEdgeDirectionSwap(){
		GraphComponentEvent evt = new GraphComponentEvent();
		evt.setSource(source);
		// see class-level comment A
		for (int i = listeners.size() - 1; i >= 0; i--) {
			listeners.get(i).gcEdgeDirectionSwap(evt);
		}
	}
	
	public void fireEdgeCut(){
		GraphComponentEvent evt = new GraphComponentEvent();
		evt.setSource(source);
		// see class-level comment A
		for (int i = listeners.size() - 1; i >= 0; i--) {
			listeners.get(i).gcEdgeCut(evt);
		}
	}
	
	public void fireNewEdgeAddedToNode(Diedge edge){
		GraphComponentEvent evt = new GraphComponentEvent();
		evt.setSource(source);
		evt.setEdge(edge);
		// see class-level comment A
		for (int i = listeners.size() - 1; i >= 0; i--) {
			listeners.get(i).gcNewEdgeAddedToNode(evt);
		}
	}
}
