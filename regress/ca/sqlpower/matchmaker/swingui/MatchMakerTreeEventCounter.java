/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */


package ca.sqlpower.matchmaker.swingui;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

/**
 *	Get counts of the various match maker event types
 */
public class MatchMakerTreeEventCounter implements TreeModelListener {

	int childrenInsertedCount;
	int childrenRemovedCount;
	int propertyChangedCount;
	int structureChangedCount;
	TreeModelEvent lastEvt;

	public void treeNodesInserted(TreeModelEvent e) {
		childrenInsertedCount++;
		lastEvt = e;
	}

	public void treeNodesRemoved(TreeModelEvent e) {
		childrenRemovedCount++;
		lastEvt = e;
	}

	public void treeNodesChanged(TreeModelEvent evt) {
		propertyChangedCount++;
		lastEvt = evt;
	}

	public void treeStructureChanged(TreeModelEvent e) {
		structureChangedCount++;
		lastEvt = e;

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

	public TreeModelEvent getLastEvt() {
		return lastEvt;
	}
}
