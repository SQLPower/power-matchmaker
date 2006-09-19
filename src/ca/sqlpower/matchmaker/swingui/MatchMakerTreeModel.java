package ca.sqlpower.matchmaker.swingui;

import java.util.LinkedList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import ca.sqlpower.matchmaker.hibernate.PlFolder;
import ca.sqlpower.matchmaker.hibernate.home.PlFolderHome;

public class MatchMakerTreeModel implements TreeModel {
	
	public static final String root="All Match/Merge Information";
	public List<PlFolder> folders;
	
	public MatchMakerTreeModel() {
		PlFolderHome folderHome = new PlFolderHome();
	}
	
	public Object getChild(Object parent, int index) {
		if(parent == root ){
			return folders.get(index);
		} else {
			return null;
		}
		
	}

	public int getChildCount(Object parent) {
		if(parent == root){
			return folders.size();
		}
		return 0;
	}

	public int getIndexOfChild(Object parent, Object child) {
		if (parent==root){
			return folders.indexOf(child);
		} else {
			return -1;
		}
		
	}

	public Object getRoot() {
		return root;
	}

	public boolean isLeaf(Object node) {
		if(node instanceof PlFolder || root == node){
			return false;
		}
		return true;
	}


	public void valueForPathChanged(TreePath path, Object newValue) {
		// TODO Auto-generated method stub

	}
	
	protected LinkedList treeModelListeners;

	public void addTreeModelListener(TreeModelListener l) {		
		treeModelListeners.add(l);
	}

	public void removeTreeModelListener(TreeModelListener l) {
		treeModelListeners.remove(l);
	}

	protected void fireTreeNodesInserted(TreeModelEvent e) {

	}
	
	protected void fireTreeNodesRemoved(TreeModelEvent e) {

	}

	protected void fireTreeNodesChanged(TreeModelEvent e) {

		
	}

	protected void fireTreeStructureChanged(TreeModelEvent e) {

		
	}

}
