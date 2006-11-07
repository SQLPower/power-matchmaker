package ca.sqlpower.matchmaker.event;

import ca.sqlpower.matchmaker.MatchMakerObject;

public interface MatchMakerListener<T extends MatchMakerObject,C extends MatchMakerObject> {
	
	void mmPropertyChanged(MatchMakerEvent<T,C> evt);
	void mmChildrenInserted(MatchMakerEvent<T,C> evt);
	void mmChildrenRemoved(MatchMakerEvent<T,C> evt);
	void mmStructureChanged(MatchMakerEvent<T,C> evt);

}
