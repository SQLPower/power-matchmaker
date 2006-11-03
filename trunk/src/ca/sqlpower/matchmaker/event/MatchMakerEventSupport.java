package ca.sqlpower.matchmaker.event;

import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.matchmaker.MatchMakerObject;
/**
 * Support object to handle the notification of matchmakerlisteners
 * about matchMakerEvents
 * 
 */
public class MatchMakerEventSupport {
	private MatchMakerObject source;
	private List<MatchMakerListener> listeners = new ArrayList<MatchMakerListener>();
	
	public MatchMakerEventSupport(MatchMakerObject source) {
		this.source = source;
	}
	
	public void addMatchMakerListener(MatchMakerListener l){
		listeners.add(l);
	}
	
	public void removeMatchMakerListener(MatchMakerListener l){
		listeners.remove(l);
	}
	
	public void firePropertyChange(String propertyName,Object oldValue, Object newValue){
		for (int i = listeners.size()-1;i>=0;i--){
			MatchMakerEvent evt = new MatchMakerEvent();
			evt.setSource(source);
			evt.setOldValue(oldValue);
			evt.setNewValue(newValue);
			evt.setPropertyName(propertyName);
			listeners.get(i).mmPropertyChanged(evt);
		}
	}
	
	public void fireChildrenInserted(String childPropertyName, int[] insertedIndices, MatchMakerObject[] insertedChildren){
		for (int i = listeners.size()-1;i>=0;i--){
			MatchMakerEvent evt = new MatchMakerEvent();
			evt.setSource(source);
			evt.setChangeIndices(insertedIndices);
			evt.setPropertyName(childPropertyName);
			evt.setChildren(insertedChildren);
			listeners.get(i).mmChildrenInserted(evt);
		}
	}
	
	public void fireChildrenRemoved(String childPropertyName, int[] removedIndices, MatchMakerObject[] removedChildren){
		for (int i = listeners.size()-1;i>=0;i--){
			MatchMakerEvent evt = new MatchMakerEvent();
			evt.setSource(source);
			evt.setChangeIndices(removedIndices);
			evt.setPropertyName(childPropertyName);
			evt.setChildren(removedChildren);
			listeners.get(i).mmChildrenRemoved(evt);
		}
	}

	public void fireStructureChanged(){
		for (int i = listeners.size()-1;i>=0;i--){
			MatchMakerEvent evt = new MatchMakerEvent();
			evt.setSource(source);
			listeners.get(i).mmStructureChanged(evt);
		}
	}

	
}
