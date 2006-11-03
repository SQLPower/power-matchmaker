package ca.sqlpower.matchmaker.event;

import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.matchmaker.MatchMakerObject;
/**
 * Support object to handle the notification of matchmakerlisteners
 * about matchMakerEvents
 * 
 */
public class MatchMakerEventSupport<T extends MatchMakerObject<C>,C extends MatchMakerObject> {
	private T source;
	private List<MatchMakerListener<T,C>> listeners = new ArrayList<MatchMakerListener<T,C>>();
	
	public MatchMakerEventSupport(T source) {
		this.source = source;
	}
	
	public void addMatchMakerListener(MatchMakerListener<T,C> l){
		listeners.add(l);
	}
	
	public void removeMatchMakerListener(MatchMakerListener<T,C> l){
		listeners.remove(l);
	}
	
	public void firePropertyChange(String propertyName,Object oldValue, Object newValue){
		for (int i = listeners.size()-1;i>=0;i--){
			MatchMakerEvent<T,C> evt = new MatchMakerEvent<T,C>();
			evt.setSource(source);
			evt.setOldValue(oldValue);
			evt.setNewValue(newValue);
			evt.setPropertyName(propertyName);
			listeners.get(i).mmPropertyChanged(evt);
		}
	}
	
	public void fireChildrenInserted(String childPropertyName, int[] insertedIndices, List<C> insertedChildren){
		for (int i = listeners.size()-1;i>=0;i--){
			MatchMakerEvent<T,C> evt = new MatchMakerEvent<T,C>();
			evt.setSource(source);
			evt.setChangeIndices(insertedIndices);
			evt.setPropertyName(childPropertyName);
			evt.setChildren(insertedChildren);
			listeners.get(i).mmChildrenInserted(evt);
		}
	}
	
	public void fireChildrenRemoved(String childPropertyName, int[] removedIndices, List<C> removedChildren){
		for (int i = listeners.size()-1;i>=0;i--){
			MatchMakerEvent<T,C> evt = new MatchMakerEvent<T,C>();
			evt.setSource(source);
			evt.setChangeIndices(removedIndices);
			evt.setPropertyName(childPropertyName);
			evt.setChildren(removedChildren);
			listeners.get(i).mmChildrenRemoved(evt);
		}
	}

	public void fireStructureChanged(){
		for (int i = listeners.size()-1;i>=0;i--){
			MatchMakerEvent<T,C> evt = new MatchMakerEvent<T,C>();
			evt.setSource(source);
			listeners.get(i).mmStructureChanged(evt);
		}
	}

	
}
