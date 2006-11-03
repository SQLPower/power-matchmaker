package ca.sqlpower.matchmaker.event;

public interface MatchMakerListener {
	
	void mmPropertyChanged(MatchMakerEvent evt);
	void mmChildrenInserted(MatchMakerEvent evt);
	void mmChildrenRemoved(MatchMakerEvent evt);
	void mmStructureChanged(MatchMakerEvent evt);

}
