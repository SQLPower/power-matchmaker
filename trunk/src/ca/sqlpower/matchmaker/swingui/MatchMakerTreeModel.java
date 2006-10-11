package ca.sqlpower.matchmaker.swingui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import ca.sqlpower.matchmaker.hibernate.DefaultHibernateObject;
import ca.sqlpower.matchmaker.hibernate.PlFolder;
import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.hibernate.PlMatchCriterion;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;

public class MatchMakerTreeModel implements TreeModel, PropertyChangeListener {

	public static final String root="All Match/Merge Information";
	public static final String current="Current Match/Merge Information";
	public static final String allCurrent="All Current Match/Merge Information";
	public static final String backup="Backup Match/Merge Information";
	public List<PlFolder> folders = new ArrayList<PlFolder>();
	public List<PlMatch>  matches = new ArrayList<PlMatch>();

	public MatchMakerTreeModel(List<PlFolder> folders, List<PlMatch> matches) {
		this();
		this.folders = folders;
		this.matches = matches;

		for (PlMatch m: matches){
			m.addHierachialChangeListener(this);
		}


	}



	public MatchMakerTreeModel() {
		super();
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
			if (node instanceof PlMatchGroup){
				return true;
			}
			return ((DefaultHibernateObject)node).getChildCount()==0;
		}
		return true;
	}


	public void valueForPathChanged(TreePath path, Object newValue) {
		throw new UnsupportedOperationException("Value for path change unsupported in the match maker tree");

	}

	protected LinkedList<TreeModelListener> treeModelListeners = new LinkedList<TreeModelListener>();

	public void addTreeModelListener(TreeModelListener l) {
		treeModelListeners.add(l);
	}

	public void removeTreeModelListener(TreeModelListener l) {
		treeModelListeners.remove(l);
	}

	protected void fireTreeNodesInserted(TreeModelEvent e) {
		for (int i= treeModelListeners.size()-1; i >=0; i--){
			treeModelListeners.get(i).treeNodesInserted(e);
		}
	}

	protected void fireTreeNodesRemoved(TreeModelEvent e) {
		for (int i= treeModelListeners.size()-1; i >=0; i--){
			treeModelListeners.get(i).treeNodesRemoved(e);
		}
	}

	protected void fireTreeNodesChanged(TreeModelEvent e) {
		for (int i= treeModelListeners.size()-1; i >=0; i--){
			treeModelListeners.get(i).treeNodesChanged(e);
		}

	}

	protected void fireTreeStructureChanged(TreeModelEvent e) {
		for (int i= treeModelListeners.size()-1; i >=0; i--){
			treeModelListeners.get(i).treeStructureChanged(e);
		}

	}

	public void refresh() {
		fireTreeNodesChanged(new TreeModelEvent(root,new TreePath(root)));
	}

	private List<TreePath> getPathFromPlMatch(PlMatch match) {
		ArrayList<TreePath> paths = new ArrayList<TreePath>();
		Object[] allPrefix = {root,current,allCurrent};
		TreePath path = new TreePath(allPrefix);
		path = path.pathByAddingChild(match);
		paths.add(path);
		for (PlFolder folder: folders){
			if (folder.getChildren().contains(match)){
				TreePath p = new TreePath(root);
				p=p.pathByAddingChild(current);
				p=p.pathByAddingChild(folder);
				p=p.pathByAddingChild(match);
				paths.add(p);
			}
		}
		return paths;
	}



	public void propertyChange(PropertyChangeEvent evt) {
		List<TreePath> paths =new ArrayList<TreePath>();
		if (evt.getSource() instanceof PlMatch){
			paths = getPathFromPlMatch((PlMatch)evt.getSource());
		} else if (evt.getSource()  instanceof PlMatchGroup) {
			PlMatchGroup group = (PlMatchGroup) evt.getSource();
			List<TreePath> matchpaths = getPathFromPlMatch(group.getPlMatch());
			for (TreePath p: matchpaths){
				paths.add(p.pathByAddingChild(group));
			}
		}else if (evt.getSource() instanceof PlMatchCriterion) {
			return;
		} else {

			throw new IllegalArgumentException("Invalid class "+evt.getSource().getClass());
		}

		for (TreePath p: paths){
			// XXX Extract these strings as Constants!
			if (evt.getPropertyName().equals("plMatchGroups") || evt.getPropertyName().equals("plMatchCriterias") || evt.getPropertyName().equals("plMatchs")){
				List<? extends DefaultHibernateObject> oldList = new ArrayList<DefaultHibernateObject>((Set)evt.getOldValue());
				List<? extends DefaultHibernateObject> newList = new ArrayList<DefaultHibernateObject>((Set)evt.getNewValue());

			if (evt.getPropertyName().equals("plMatchGroups")) {
				Collections.sort((List<PlMatchGroup>)oldList);
				Collections.sort((List<PlMatchGroup>)newList);
			} else if (evt.getPropertyName().equals("plMatchCriterias")) {
				Collections.sort((List<PlMatchCriterion>)oldList);
				Collections.sort((List<PlMatchCriterion>)newList);
			} else if (evt.getPropertyName().equals("plMatchs")) {
				Collections.sort((List<PlMatch>)oldList);
				Collections.sort((List<PlMatch>)newList);
			}

				if (((Set)evt.getOldValue()).size() < ((Set)evt.getNewValue()).size()){
					List<DefaultHibernateObject> deltaList = new ArrayList<DefaultHibernateObject>(newList);
					deltaList.removeAll(oldList);
					int[] indices = new int[deltaList.size()];
					int i =0;
					for (DefaultHibernateObject dho: deltaList){
						indices[i] = newList.indexOf(dho);
						i++;
					}

					fireTreeNodesInserted(new TreeModelEvent(evt.getSource(),p,indices,deltaList.toArray()));

				} else if (((Set)evt.getOldValue()).size() > ((Set)evt.getNewValue()).size()) {
					List<DefaultHibernateObject> deltaList = new ArrayList<DefaultHibernateObject>(oldList);
					deltaList.removeAll(newList);
					int[] indices = new int[deltaList.size()];
					int i =0;
					for (DefaultHibernateObject dho: deltaList){
						indices[i] = oldList.indexOf(dho);
						i++;
					}

					fireTreeNodesRemoved(new TreeModelEvent(evt.getSource(),p,indices,deltaList.toArray()));
				} else {
					throw new IllegalArgumentException("You have added or removed 0 items.");
				}
			} else {
				fireTreeNodesChanged(new TreeModelEvent(evt.getSource(),p));
			}
		}
	}



}
