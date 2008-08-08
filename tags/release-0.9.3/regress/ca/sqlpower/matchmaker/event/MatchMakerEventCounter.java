/*
 * Copyright (c) 2008, SQL Power Group Inc.
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


package ca.sqlpower.matchmaker.event;

import ca.sqlpower.matchmaker.MatchMakerObject;

/**
 *	Get counts of the various match maker event types
 */
public class MatchMakerEventCounter<T extends MatchMakerObject, C extends MatchMakerObject>
	implements MatchMakerListener<T, C> {

	private int childrenInsertedCount;
	private int childrenRemovedCount;
	private int propertyChangedCount;
	private int structureChangedCount;
	private MatchMakerEvent<T, C> lastEvt;

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

	public MatchMakerEvent<T, C> getLastEvt() {
		return lastEvt;
	}

}
