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
 * A simple listener interface for {@link MatchMakerEngine}s. Currently,
 * implementing classes should be able to react to the events of a
 * {@link MatchMakerEngine} starting and stopping.
 */
public interface EngineListener {
	/**
	 * A handler method for when a MatchMakerEngine has started.
	 * 
	 * @param e
	 *            An {@link EngineEvent} that has a reference to the
	 *            {@link MatchMakerEngine} that is the source of this event
	 */
	public void engineStarted(EngineEvent e);

	/**
	 * A handler method for when a MatchMakerEngine has stopped (because it has
	 * finished its task or because it was cancelled by the user, or aborted by
	 * an error)
	 * 
	 * @param e
	 *            An {@link EngineEvent} that has a reference to the
	 *            {@link MatchMakerEngine} that is the source of this event
	 */
	public void engineStopped(EngineEvent e);
}
