package ca.sqlpower.matchmaker.swingui;

import java.util.LinkedList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.AbstractMatchMakerObject;
import ca.sqlpower.matchmaker.FolderParent;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerUtils;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;

public class MatchMakerTreeModel implements TreeModel {

	private static final Logger logger = Logger
			.getLogger(MatchMakerTreeModel.class);

	protected static class MMTreeNode<C extends MatchMakerObject> extends
			AbstractMatchMakerObject<MMTreeNode, C> {

		private boolean root;
		public boolean isRoot() {
			return root;
		}

		public void setRoot(boolean root) {
			this.root = root;
		}

		public MMTreeNode(String name, boolean root) {
			setName(name);
			setRoot(root);
		}

		@Override
		public boolean equals(Object obj) {
			return this == obj;
		}

		@Override
		public int hashCode() {
			return System.identityHashCode(this);
		}

		public MMTreeNode duplicate(MatchMakerObject parent, MatchMakerSession session) {
			throw new UnsupportedOperationException("MMTreeNodes cannot be duplicated");
		}

	}

	private final MMTreeNode<FolderParent> root = new MMTreeNode<FolderParent>(
			"root",true);


	private FolderParent current;
	private FolderParent backup;
	
	private TreeModelEventAdapter listener = new TreeModelEventAdapter();

	public MatchMakerTreeModel(FolderParent current, FolderParent backup, MatchMakerSession s) {
		root.setSession(s);
		current.setName("Current Match/Merge Information");
		root.addChild(current);
		this.current = current;
		backup.setName("Backup Match/Merge Information");
		root.addChild(backup);
		this.backup = backup;
		MatchMakerUtils.listenToHierarchy(listener, root);
	}

	public Object getChild(Object parent, int index) {
		return ((MatchMakerObject<?, ?>) parent).getChildren().get(index);
	}

	public int getChildCount(Object parent) {
		return ((MatchMakerObject<?, ?>) parent).getChildren().size();
	}

	public int getIndexOfChild(Object parent, Object child) {
		return ((MatchMakerObject<?, ?>) parent).getChildren().indexOf(child);
	}

	public Object getRoot() {
		return root;
	}

	public boolean isLeaf(Object node) {
		return !((MatchMakerObject<?, ?>) node).allowsChildren();
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
		throw new UnsupportedOperationException("Value for path change "
				+ "unsupported in the match maker tree");
	}

	protected LinkedList<TreeModelListener> treeModelListeners = new LinkedList<TreeModelListener>();

	public void addTreeModelListener(TreeModelListener l) {
		treeModelListeners.add(l);
	}

	public void removeTreeModelListener(TreeModelListener l) {
		treeModelListeners.remove(l);
	}

	protected void fireTreeNodesInserted(TreeModelEvent e) {
		if (logger.isDebugEnabled()){
			logger.debug("Firing treeNodesInserted event " + e + " to "
				+ treeModelListeners.size() + " listeners...");
		}
		for (int i = treeModelListeners.size() - 1; i >= 0; i--) {
			treeModelListeners.get(i).treeNodesInserted(e);
		}
		logger.debug("done");
	}

	protected void fireTreeNodesRemoved(TreeModelEvent e) {
		if (logger.isDebugEnabled()){
			logger.debug("Firing treeNodesRemoved event " + e + " to "
				+ treeModelListeners.size() + " listeners...");
		}
		for (int i = treeModelListeners.size() - 1; i >= 0; i--) {
			treeModelListeners.get(i).treeNodesRemoved(e);
		}
		logger.debug("done");
	}

	protected void fireTreeNodesChanged(TreeModelEvent e) {
		if (logger.isDebugEnabled()){
			logger.debug("Firing treeNodesChanged event " + e + " to "
				+ treeModelListeners.size() + " listeners...");
		}
		for (int i = treeModelListeners.size() - 1; i >= 0; i--) {
			logger.debug("Notifing " + treeModelListeners.get(i));
			treeModelListeners.get(i).treeNodesChanged(e);
		}
		logger.debug("done");
	}

	protected void fireTreeStructureChanged(TreeModelEvent e) {
		if (logger.isDebugEnabled()){
			logger.debug("Firing treeStructureChanged event " + e + " to "
				+ treeModelListeners.size() + " listeners...");
		}
		for (int i = treeModelListeners.size() - 1; i >= 0; i--) {
			treeModelListeners.get(i).treeStructureChanged(e);
		}
		logger.debug("done");
	}

	public void refresh() {
		fireTreeNodesChanged(new TreeModelEvent(root, new TreePath(root)));
	}

	private class TreeModelEventAdapter<T extends MatchMakerObject, C extends MatchMakerObject>
			implements MatchMakerListener<T, C> {
		public void mmPropertyChanged(MatchMakerEvent<T, C> evt) {
			TreePath paths = getPathForNode(evt.getSource());
			TreeModelEvent e = new TreeModelEvent(evt.getSource(), paths);
			fireTreeNodesChanged(e);
		}

		public void mmChildrenInserted(MatchMakerEvent<T, C> evt) {
			TreePath paths = getPathForNode(evt.getSource());
			TreeModelEvent e = new TreeModelEvent(evt.getSource(), paths, evt
					.getChangeIndices(), evt.getChildren().toArray());
			fireTreeNodesInserted(e);
			for ( MatchMakerObject o : evt.getChildren() ) {
				MatchMakerUtils.listenToHierarchy(listener,o);
			}
		}

		public void mmChildrenRemoved(MatchMakerEvent<T, C> evt) {
			TreePath path = getPathForNode(evt.getSource());
            if (logger.isDebugEnabled()) {
                logger.debug("Got MM children removed event!");
                StringBuilder sb = new StringBuilder();
                MatchMakerObject mmo = evt.getSource();
                while (mmo != null) {
                    sb.insert(0, "->" + mmo.getName());
                    mmo = mmo.getParent();
                }
                logger.debug("Parent of removed MMObject: "+sb);
                logger.debug("          removed children: "+evt.getChildren());
                
                sb = new StringBuilder();
                sb.append("{");
                for (int i = 0; i < evt.getChangeIndices().length; i++) {
                    if (i != 0) sb.append(", ");
                    sb.append(evt.getChangeIndices()[i]);
                }
                sb.append("}");
                logger.debug("     removed child indices: "+sb);
                logger.debug("Traceback:", new Exception());
            }
			TreeModelEvent e = new TreeModelEvent(evt.getSource(), path, evt
					.getChangeIndices(), evt.getChildren().toArray());
            logger.debug("About to fire tree model event: "+e);
			fireTreeNodesRemoved(e);
			for ( MatchMakerObject o : evt.getChildren() ) {
				MatchMakerUtils.unlistenToHierarchy(listener,o);
			}
		}

		public void mmStructureChanged(MatchMakerEvent<T, C> evt) {
			TreePath paths = getPathForNode(evt.getSource());
			TreeModelEvent e = new TreeModelEvent(evt.getSource(), paths);
			fireTreeStructureChanged(e);
		}
	}

	private TreePath getPathForNode(MatchMakerObject<?, ?> source) {
		List<MatchMakerObject> path = new LinkedList<MatchMakerObject>();
		while (source != null) {
			path.add(0, source);
			source = source.getParent();
		}
		return new TreePath(path.toArray());
	}
	
	public void addFolderToCurrent(PlFolder folder){
		current.addChild(folder);
	}
}