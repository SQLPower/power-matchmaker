package ca.sqlpower.matchmaker.event;

import ca.sqlpower.matchmaker.MatchMakerEngine;
import ca.sqlpower.matchmaker.MatchMakerObject;
/**
 * This event communicates steps in the process of running the engine
 *
 */
public class EngineEvent {

	public enum EngineEventType {
		ENGINE_START,ENGINE_END;
	}
	/** The event type */
	private EngineEventType type;
	/** The current match maker object that is being operated on. */
	private MatchMakerObject matchMakerObjectBeingProcessed;
	/** The engine that caused the event */
	private MatchMakerEngine source;
	
	public EngineEvent(MatchMakerEngine source, EngineEventType type, MatchMakerObject matchMakerObjectBeingProcessed) {
		this.source = source;
		this.matchMakerObjectBeingProcessed = matchMakerObjectBeingProcessed;
		this.type = type;
	}

	public MatchMakerObject getMatchMakerObjectBeingProcessed() {
		return matchMakerObjectBeingProcessed;
	}

	public void setMatchMakerObjectBeingProcessed(MatchMakerObject runningMatch) {
		this.matchMakerObjectBeingProcessed = runningMatch;
	}

	public MatchMakerEngine getSource() {
		return source;
	}

	public void setSource(MatchMakerEngine source) {
		this.source = source;
	}

	public EngineEventType getType() {
		return type;
	}

	public void setType(EngineEventType type) {
		this.type = type;
	}

}
