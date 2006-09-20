package ca.sqlpower.matchmaker.swingui;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import ca.sqlpower.matchmaker.hibernate.DefaultHibernateObject;
import ca.sqlpower.matchmaker.hibernate.PlFolder;
import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.hibernate.home.PlFolderHome;
import ca.sqlpower.matchmaker.hibernate.home.PlMatchHome;
import ca.sqlpower.matchmaker.util.HibernateUtil;

public class MatchMakerTreeModel implements TreeModel {
	
	public static final String root="All Match/Merge Information";
	public static final String current="Current Match/Merge Information";
	public static final String allCurrent="All Current Match/Merge Information";
	public static final String backup="Backup Match/Merge Information";
	public List<PlFolder> folders = new ArrayList<PlFolder>();
	public List<PlMatch>  matches = new ArrayList<PlMatch>();
	
	public MatchMakerTreeModel(){
		if (HibernateUtil.getSessionFactory() != null){
			PlMatchHome matchHome = new PlMatchHome();
			matches = matchHome.findAll();

			SortedSet<PlFolder> f = new TreeSet<PlFolder>();
			for(PlMatch m:matches){
				if (m.getFolders().size() > 0){
					f.addAll(m.getFolders());
				}
			}

			folders = new ArrayList<PlFolder>(f);
		}
	}
	

	
	public Object getChild(Object parent, int index) {
		if(parent == root ){
			if(index == 0) {
				return current;
			} else if (index == 1 ){
				return backup;
			}
			throw new IndexOutOfBoundsException("The root only has 2 children");
		} else if (parent == current){
			if (index == 0){
				return allCurrent;
			} else {
				return folders.get(index -1);
			}
		}else if (parent == allCurrent){
			return matches.get(index);
		} else if(parent instanceof DefaultHibernateObject) {
			return ((DefaultHibernateObject) parent).getChildren().get(index);		
		}
		return null;
		
	}

	public int getChildCount(Object parent) {
		if(parent == root){
			return 2;
		} else if(parent==current){
			return folders.size()+1;
		}else if (parent==allCurrent){
			return matches.size();
		} else if(parent instanceof DefaultHibernateObject) {
			return ((DefaultHibernateObject) parent).getChildren().size();		
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
		if(node instanceof PlFolder || root == node || current==node || allCurrent==node || backup == node){
			return false;
		} else if(node instanceof DefaultHibernateObject){
			return ((DefaultHibernateObject)node).getChildCount()==0;
		}
		return true;
	}


	public void valueForPathChanged(TreePath path, Object newValue) {
		// TODO Auto-generated method stub

	}
	
	protected LinkedList treeModelListeners = new LinkedList();

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
