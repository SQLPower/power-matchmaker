/*
 * Copyright (c) 2007, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.i 
 */

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
