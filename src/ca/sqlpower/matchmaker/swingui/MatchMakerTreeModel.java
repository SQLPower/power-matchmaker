package ca.sqlpower.matchmaker.swingui;

import java.util.LinkedList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.AbstractMatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerObject;
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

	}

	private final MMTreeNode<MMTreeNode> root = new MMTreeNode<MMTreeNode>(
			"root",true);

	private final MMTreeNode<PlFolder> current = new MMTreeNode<PlFolder>(
			"Current Match/Merge Information",false);

	private final MMTreeNode<PlFolder> backup = new MMTreeNode<PlFolder>(
			"Backup Match/Merge Information",false);

	private TreeModelEventAdapter listener = new TreeModelEventAdapter();

	public MatchMakerTreeModel(List<PlFolder> folders) {
		root.addChild(current);
		root.addChild(backup);

		for (PlFolder f : folders) {
			current.addChild(f);
		}
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
		logger.debug("Firing treeNodesInserted event " + e + " to "
				+ treeModelListeners.size() + " listeners...");
		for (int i = treeModelListeners.size() - 1; i >= 0; i--) {
			treeModelListeners.get(i).treeNodesInserted(e);
		}
		logger.debug("done");
	}

	protected void fireTreeNodesRemoved(TreeModelEvent e) {
		logger.debug("Firing treeNodesRemoved event " + e + " to "
				+ treeModelListeners.size() + " listeners...");
		for (int i = treeModelListeners.size() - 1; i >= 0; i--) {
			treeModelListeners.get(i).treeNodesRemoved(e);
		}
		logger.debug("done");
	}

	protected void fireTreeNodesChanged(TreeModelEvent e) {
		logger.debug("Firing treeNodesChanged event " + e + " to "
				+ treeModelListeners.size() + " listeners...");
		for (int i = treeModelListeners.size() - 1; i >= 0; i--) {
			logger.debug("Notifing " + treeModelListeners.get(i));
			treeModelListeners.get(i).treeNodesChanged(e);
		}
		logger.debug("done");
	}

	protected void fireTreeStructureChanged(TreeModelEvent e) {
		logger.debug("Firing treeStructureChanged event " + e + " to "
				+ treeModelListeners.size() + " listeners...");
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
			TreePath paths = getPathForNode(evt.getSource());
			TreeModelEvent e = new TreeModelEvent(evt.getSource(), paths, evt
					.getChangeIndices(), evt.getChildren().toArray());
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
}