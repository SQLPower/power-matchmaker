package ca.sqlpower.matchmaker.event;

import ca.sqlpower.matchmaker.MatchMakerObject;

/**
 *	Get counts of the various match maker event types 
 */
public class MatchMakerEventCounter implements MatchMakerListener<MatchMakerObject, MatchMakerObject> {

	int childrenInsertedCount;
	int childrenRemovedCount;
	int propertyChangedCount;
	int structureChangedCount;
	MatchMakerEvent lastEvt;
	
	public void mmChildrenInserted(MatchMakerEvent evt) {
		childrenInsertedCount++;
		lastEvt = evt;
	}

	public void mmChildrenRemoved(MatchMakerEvent evt) {
		childrenRemovedCount++;
		lastEvt = evt;
	}

	public void mmPropertyChanged(MatchMakerEvent evt) {
		propertyChangedCount++;
		lastEvt = evt;
	}

	public void mmStructureChanged(MatchMakerEvent evt) {
		structureChangedCount++;
		lastEvt = evt;

	}

	public int getChildrenInsertedCount() {
		return childrenInsertedCount;
	}

	public int getChildrenRemovedCount() {
		return childrenRemovedCount;
	}

	public int getPropertyChangedCount() {
		return propertyChangedCount;
	}

	public int getStructureChangedCount() {
		return structureChangedCount;
	}
	
	public int getAllEventCounts(){
		return structureChangedCount + propertyChangedCount + childrenInsertedCount + childrenRemovedCount;
	}

	public MatchMakerEvent getLastEvt() {
		return lastEvt;
	}

}
