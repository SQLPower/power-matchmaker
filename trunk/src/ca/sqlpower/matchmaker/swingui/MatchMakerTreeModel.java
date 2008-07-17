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

package ca.sqlpower.matchmaker.swingui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TranslateGroupParent;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;

/**
 * A tree model implementation that adapts a hierarchy of MatchMakerObjects
 * to the Swing TreeModel interface.  It uses a simulated root node which
 * should normally not be made visible for the user, since it adds no value
 * or meaning to anything.
 */
public class MatchMakerTreeModel implements TreeModel {

	private static final Logger logger = Logger.getLogger(MatchMakerTreeModel.class);

    /**
     * A very simple MatchMakerObject implementation for the tree's root node object.
     * Its children will be FolderParent objects, which are the "Current Project"
     * and "Backup Project" folders, which are in turn parents to the PLFolders
     * (hence, FolderParent).
     */
    private static class MMRootNode <C extends MatchMakerObject> 
    	extends AbstractMatchMakerObject<MMRootNode, C> {

        public MMRootNode() {
            setName("Root Node");
        }

        public boolean isRoot() {
            return true;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        public MMRootNode duplicate(MatchMakerObject parent, MatchMakerSession session) {
            throw new UnsupportedOperationException("MMTreeNodes cannot be duplicated");
        }
    }

    /**
     * All the types of actions associated with each project in the tree.
     */
    public static enum MatchActionType {
        
        /**
         * Shows the "run match" UI.
         */
        RUN_MATCH("Run Match Engine"),
        
        /**
         * Shows the "validate matches" UI.
         */
        VALIDATE_MATCHES("Validate Matches"),
        
        /**
         * Shows the "validation status" UI.
         */
        VALIDATION_STATUS("Validation Status"),
        
        /**
         * Shows the "run merge" UI.
         */
        RUN_MERGE("Run Merge Engine"),
        
        /**
         * Shows information about the parent project such as
         * its ID, folder, description, type and history
         */
        AUDIT_INFO("Audit Information"),
        
        /**
         * Shows the "run cleansing" UI.
         */
        RUN_CLEANSING("Run Cleansing Engine");
        
        private final String name;
        
        private MatchActionType(String name) {
            this.name = name;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    /**
     * A simple MatchMakerObject that holds a single Swing Action.  We create
     * these as extra children for the Project objects in the tree so the entire
     * project workflow is represented in one place, with pretty pictures and
     * everything.
     */
    public class MatchActionNode extends AbstractMatchMakerObject<Project, MatchActionNode> {

        private final MatchActionType matchActionType;
        private final Project project;
        
        public MatchActionNode(MatchActionType matchActionType, Project project) {
            this.matchActionType = matchActionType;
            this.project = project;
            setName(matchActionType.toString());
        }

        public boolean isRoot() {
            return false;
        }
        
        @Override
        public boolean allowsChildren() {
            return false;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        public Project duplicate(MatchMakerObject parent, MatchMakerSession session) {
            throw new UnsupportedOperationException("A MatchActionNode cannot be duplicated");
        }
        
        public MatchActionType getActionType() {
            return matchActionType;
        }
        
        public Project getProject() {
        	return project;
        }
        
        public Project getParent() {
        	return project;
        }
    }
    
    /**
     * The session this tree model belongs to.  For testing, it's acceptable
     * for this to be just a stub, but you will not be able to call
     * {@link MatchActionNode#performAction()} unless this is actually a
     * MatchMakerSwingSession.
     */
    private final MatchMakerSession session;

    /**
     * This tree's root node.
     */
	private final MMRootNode root = new MMRootNode();

    /**
     * The cache that is maintained by {@link #getActionNodes(Project)}. Don't ever access
     * this map directly.  Use that method.
     */
	private Map<Project, List<MatchActionNode>> matchActionCache = new HashMap<Project, List<MatchActionNode>>();
    
	private FolderParent current;
	private FolderParent backup;
	private TranslateGroupParent translate;
	
	private static final MatchActionType[] DE_DUP_ACTIONS = 
		{MatchActionType.RUN_MATCH, MatchActionType.VALIDATE_MATCHES, MatchActionType.VALIDATION_STATUS,
		MatchActionType.RUN_MERGE, MatchActionType.AUDIT_INFO};
	
	private static final MatchActionType[] CLEANSING_ACTIONS = 
		{MatchActionType.RUN_CLEANSING, MatchActionType.AUDIT_INFO};
	
	
	private TreeModelEventAdapter listener = new TreeModelEventAdapter();
	
	private ActionCacheEventAdapter cacheLisener = new ActionCacheEventAdapter();

    /**
     * Creates a new tree model with two children of the root node (the given
     * current and backup FolderParent objects).
     * 
     * @param s
     *            For testing, it's acceptable for this to be just a stub, but
     *            you will not be able to call
     *            {@link MatchActionNode#performAction()} unless this is
     *            actually a MatchMakerSwingSession.
     */
	public MatchMakerTreeModel(FolderParent current, FolderParent backup,
			TranslateGroupParent translate, MatchMakerSession s) {
		session = s;
        root.setSession(s);
        
        this.current = current;
		root.addChild(current);

        this.backup = backup;
		root.addChild(backup);
		
		this.translate = translate;
		root.addChild(translate);
        
		MatchMakerUtils.listenToHierarchy(listener, root);
		MatchMakerUtils.listenToShallowHierarchy(cacheLisener, current);
	}

    /**
     * Returns (and possibly creates) the list of action nodes associated with the given match
     * in this tree.  The responses from this method are cached, so once a list of
     * actions has been returned for a particular project, the same list will be returned
     * for all future requests.
     * 
     * @param project The project the action nodes belong to. (It's their parent in the tree)
     * @return The unique list of action nodes for the given project.
     */
    private List<MatchActionNode> getActionNodes(Project project) {
        List<MatchActionNode> actionNodes = matchActionCache.get(project);
        if (actionNodes == null) {
            actionNodes = new ArrayList<MatchActionNode>();
            if (project.getType() == ProjectMode.FIND_DUPES) {
	            for (MatchActionType type : DE_DUP_ACTIONS) {
	                actionNodes.add(new MatchActionNode(type, project));
	            }
            } else if (project.getType() == ProjectMode.CLEANSE) {
            	for (MatchActionType type : CLEANSING_ACTIONS) {
            		actionNodes.add(new MatchActionNode(type, project));
            	}
            }
            matchActionCache.put(project, actionNodes);
        }
        return actionNodes;
    }
    
	public Object getChild(Object parent, int index) {
        final MatchMakerObject<?, ?> mmoParent = (MatchMakerObject<?, ?>) parent;
        final MatchMakerObject<?, ?> mmoChild;
    
        if (mmoParent instanceof Project) {
            Project project = (Project) mmoParent;
            MatchMakerObject [] visible = new MatchMakerObject [getChildCount(project)];
            int count = 0;
            for (MatchMakerObject child : project.getChildren()) {
            	if (child.isVisible()) {
            		visible[count++] = child;
            	}
            }
            for (MatchActionNode child : getActionNodes(project)) {
            	if (child.isVisible()) {
            		visible[count++] = child;
            	}
            }
            mmoChild = visible[index];
        } else {
        	int real = index;
        	for (int i = 0; i < index; i++) {
        		if (!mmoParent.getChildren().get(i).isVisible()) {
        			real++;
        		}
        	}
            mmoChild = mmoParent.getChildren().get(real);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Child "+index+" of \""+mmoParent.getName()+"\" is "+
                    mmoChild.getClass().getName()+"@"+System.identityHashCode(mmoChild)+
                    " \""+mmoChild.getName()+"\"");
        }
		return (mmoChild);
	}

	public int getChildCount(Object parent) {
		final MatchMakerObject<?, ?> mmo = (MatchMakerObject<?, ?>) parent;
        int count;
        if (mmo instanceof Project) {
            Project project = (Project) mmo;
            List<MatchActionNode> matchActions = getActionNodes(project);
            count = mmo.getChildren().size() + matchActions.size();
        } else {
            count = mmo.getChildren().size();
        }
        
        for (MatchMakerObject child : mmo.getChildren()) {
        	if (!child.isVisible()) {
        		logger.debug("--------------------------------");
        		count--;
        	}
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("Child count of \""+mmo.getName()+"\" is "+count);
        }
        return count;
	}

	public int getIndexOfChild(Object parent, Object child) {
        final MatchMakerObject<?, ?> mmoParent = (MatchMakerObject<?, ?>) parent;
        final MatchMakerObject<?, ?> mmoChild = (MatchMakerObject<?, ?>) child;
        final int index = mmoParent.getChildren().indexOf(mmoChild);

        if (logger.isDebugEnabled()) {
            logger.debug("Index of child \""+mmoChild.getName()+"\" of \""+mmoParent.getName()+"\" is "+index);
        }
        
        return index;
	}

	public Object getRoot() {
		return root;
	}

	public boolean isLeaf(Object node) {
        final MatchMakerObject<?, ?> mmoNode = (MatchMakerObject<?, ?>) node;
        final boolean isLeaf = !mmoNode.allowsChildren();

        if (logger.isDebugEnabled()) {
            logger.debug("Is "+
                    mmoNode.getClass().getName()+"@"+System.identityHashCode(mmoNode)+
                    " \""+mmoNode.getName()+"\" a leaf? " + isLeaf);
        }
        
        return isLeaf;
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
            logger.debug("Got mmPropertyChanged event. property="+
                    evt.getPropertyName()+"; source="+evt.getSource());
			TreePath paths = getPathForNode(evt.getSource());
			TreeModelEvent e = new TreeModelEvent(evt.getSource(), paths);
			fireTreeNodesChanged(e);
		}

		public void mmChildrenInserted(MatchMakerEvent<T, C> evt) {
			TreePath paths = getPathForNode(evt.getSource());
			TreeModelEvent e = new TreeModelEvent(evt.getSource(), paths, evt
					.getChangeIndices(), evt.getChildren().toArray());
			if (logger.isDebugEnabled()) {
                logger.debug("Got MM children inserted event!");
                StringBuilder sb = new StringBuilder();
                MatchMakerObject mmo = evt.getSource();
                while (mmo != null) {
                    sb.insert(0, "->" + mmo.getName());
                    mmo = mmo.getParent();
                }
                logger.debug("Parent of inserted MMObject: "+sb);
                logger.debug("          inserted children: "+evt.getChildren());
                
                sb = new StringBuilder();
                sb.append("{");
                for (int i = 0; i < evt.getChangeIndices().length; i++) {
                    if (i != 0) sb.append(", ");
                    sb.append(evt.getChangeIndices()[i]);
                }
                sb.append("}");
                logger.debug("     inserted child indices: "+sb);
                logger.debug("Traceback:", new Exception());
			}
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
            logger.debug("Got mmObjectChanged event for " + evt.getSource());
			TreePath paths = getPathForNode(evt.getSource());
			TreeModelEvent e = new TreeModelEvent(evt.getSource(), paths);
			fireTreeStructureChanged(e);
		}
	}

	public TreePath getPathForNode(MatchMakerObject<?, ?> source) {
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
	
	private class ActionCacheEventAdapter<T extends MatchMakerObject, C extends MatchMakerObject>
		implements MatchMakerListener<T, C>{

		public void mmChildrenInserted(MatchMakerEvent<T, C> evt) {
			if (evt.getSource() instanceof FolderParent) {
				for (C folder : evt.getChildren()) {
					folder.addMatchMakerListener(this);
				}
			}
		}

		public void mmChildrenRemoved(MatchMakerEvent<T, C> evt) {
			if (evt.getSource() instanceof FolderParent) {
				for (C folder : evt.getChildren()) {
					for (Object project : folder.getChildren()) {
						matchActionCache.remove(project);
					}
					folder.removeMatchMakerListener(this);
				}
			} else if (evt.getSource() instanceof PlFolder) {
				for (C project : evt.getChildren()) {
					matchActionCache.remove(project);
				}
			}
		}

		public void mmPropertyChanged(MatchMakerEvent<T, C> evt) {
		}

		public void mmStructureChanged(MatchMakerEvent<T, C> evt) {
		}
	}
}