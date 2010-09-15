/*
 * Copyright (c) 2010, SQL Power Group Inc.
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker;

/**
 * An Event object for conveying information that anything listening to a
 * {@link MatchMakerEngine} may be interested in.
 */
public class EngineEvent {
	private MatchMakerEngine engine;
	
	/**
	 * Creates a new EngineEvent
	 * 
	 * @param engine
	 *            The {@link MatchMakerEngine} that is the source of this event
	 */
	public EngineEvent(MatchMakerEngine engine) {
		this.engine = engine;
	}

	/**
	 * Getter for the {@link MatchMakerEngine}
	 * 
	 * @return The {@link MatchMakerEngine} instance that is the source of this
	 *         event.
	 */
	public MatchMakerEngine getEngine() {
		return engine;
	}
}
