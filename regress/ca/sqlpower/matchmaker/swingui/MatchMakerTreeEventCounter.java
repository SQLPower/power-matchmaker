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
