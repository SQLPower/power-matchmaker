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
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.AbstractSPListener;

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
    private static class MMRootNode  
    	extends AbstractMatchMakerObject {

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
    public static enum ProjectActionType {
        
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
        RUN_CLEANSING("Run Cleansing Engine"),
        
        /**
         * Shows the "Run Address Correction" UI.
         */
        RUN_ADDRESS_CORRECTION("Run Address Correction Engine"),
        
        /**
         * Shows the "Validate Addresses" UI.
         */
        VALIDATE_ADDRESSES("Validate Addresses"),
        
        /**
         * Shows the "Commit Validated Addresses" UI.
         */
        COMMIT_VALIDATED_ADDRESSES("Commit Validated Addresses");
        
        private final String name;
        
        private ProjectActionType(String name) {
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
    public class ProjectActionNode extends AbstractMatchMakerObject {

        private final ProjectActionType projectActionType;
        private final Project project;
        
        public ProjectActionNode(ProjectActionType projectActionType, Project project) {
            this.projectActionType = projectActionType;
            this.project = project;
            setName(projectActionType.toString());
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
            throw new UnsupportedOperationException("A ProjectActionNode cannot be duplicated");
        }
        
        public ProjectActionType getActionType() {
            return projectActionType;
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
     * {@link ProjectActionNode#performAction()} unless this is actually a
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
	private Map<Project, List<ProjectActionNode>> projectActionCache = new HashMap<Project, List<ProjectActionNode>>();
    
	private FolderParent current;
	private FolderParent backup;
	private TranslateGroupParent translate;
	
	private static final ProjectActionType[] DE_DUP_ACTIONS = 
		{ProjectActionType.RUN_MATCH, ProjectActionType.VALIDATE_MATCHES, ProjectActionType.VALIDATION_STATUS,
		ProjectActionType.RUN_MERGE, ProjectActionType.AUDIT_INFO};
	
	private static final ProjectActionType[] CLEANSING_ACTIONS = 
		{ProjectActionType.RUN_CLEANSING, ProjectActionType.AUDIT_INFO};
	
	private static final ProjectActionType[] ADDRESSS_CORRECTION_ACTIONS = 
		{ ProjectActionType.RUN_ADDRESS_CORRECTION, ProjectActionType.VALIDATE_ADDRESSES, ProjectActionType.COMMIT_VALIDATED_ADDRESSES };
	
	private TreeModelEventAdapter listener = new TreeModelEventAdapter();
	
	private ActionCacheEventAdapter cacheLisener = new ActionCacheEventAdapter();

    /**
     * Creates a new tree model with two children of the root node (the given
     * current and backup FolderParent objects).
     * 
     * @param s
     *            For testing, it's acceptable for this to be just a stub, but
     *            you will not be able to call
     *            {@link ProjectActionNode#performAction()} unless this is
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
    private List<ProjectActionNode> getActionNodes(Project project) {
        List<ProjectActionNode> actionNodes = projectActionCache.get(project);
        if (actionNodes == null) {
            actionNodes = new ArrayList<ProjectActionNode>();
            if (project.getType() == ProjectMode.FIND_DUPES) {
	            for (ProjectActionType type : DE_DUP_ACTIONS) {
	                actionNodes.add(new ProjectActionNode(type, project));
	            }
            } else if (project.getType() == ProjectMode.CLEANSE) {
            	for (ProjectActionType type : CLEANSING_ACTIONS) {
            		actionNodes.add(new ProjectActionNode(type, project));
            	}
            } else if (project.getType() == ProjectMode.ADDRESS_CORRECTION) {
            	for (ProjectActionType type: ADDRESSS_CORRECTION_ACTIONS) {
            		actionNodes.add(new ProjectActionNode(type, project));
            	}
            }
            projectActionCache.put(project, actionNodes);
        }
        return actionNodes;
    }
    
	public Object getChild(Object parent, int index) {
        final MatchMakerObject mmoParent = (MatchMakerObject) parent;
        final MatchMakerObject mmoChild;
    
        if (mmoParent instanceof Project) {
            Project project = (Project) mmoParent;
            MatchMakerObject [] visible = new MatchMakerObject [getChildCount(project)];
            int count = 0;
            for (MatchMakerObject child : project.getChildren()) {
            	if (child.isVisible()) {
            		visible[count++] = child;
            	}
            }
            for (ProjectActionNode child : getActionNodes(project)) {
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
		final MatchMakerObject mmo = (MatchMakerObject) parent;
        int count;
        if (mmo instanceof Project) {
            Project project = (Project) mmo;
            List<ProjectActionNode> projectActions = getActionNodes(project);
            count = mmo.getChildren().size() + projectActions.size();
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
        final MatchMakerObject mmoParent = (MatchMakerObject) parent;
        final MatchMakerObject mmoChild = (MatchMakerObject) child;
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
        final MatchMakerObject mmoNode = (MatchMakerObject) node;
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

	private class TreeModelEventAdapter
			implements AbstractSPListener {
        
		public void PropertyChanged(MatchMakerEvent evt) {
            logger.debug("Got PropertyChanged event. property="+
                    evt.getPropertyName()+"; source="+evt.getSource());
			TreePath paths = getPathForNode(evt.getSource());
			TreeModelEvent e = new TreeModelEvent(evt.getSource(), paths);
			fireTreeNodesChanged(e);
		}

		public void ChildrenInserted(MatchMakerEvent evt) {
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

		public void ChildrenRemoved(MatchMakerEvent evt) {
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

		public void StructureChanged(MatchMakerEvent evt) {
            logger.debug("Got mmObjectChanged event for " + evt.getSource());
			TreePath paths = getPathForNode(evt.getSource());
			TreeModelEvent e = new TreeModelEvent(evt.getSource(), paths);
			fireTreeStructureChanged(e);
		}
	}

	public TreePath getPathForNode(MatchMakerObject source) {
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
	
	private class ActionCacheEventAdapter
		implements AbstractSPListener{

		public void childAdded(SPChildEvent evt) {
			if (evt.getSource() instanceof FolderParent) {
				for (C folder : evt.getChild()) {
					folder.addMatchMakerListener(this);
				}
			}
		}

		public void childRemoved(SPChildEvent evt) {
			if (evt.getSource() instanceof FolderParent) {
				for (C folder : evt.getChildren()) {
					for (Object project : folder.getChildren()) {
						projectActionCache.remove(project);
					}
					folder.removeMatchMakerListener(this);
				}
			} else if (evt.getSource() instanceof PlFolder) {
				for (C project : evt.getChildren()) {
					projectActionCache.remove(project);
				}
			}
		}

		public void mmPropertyChanged(MatchMakerEvent<T, C> evt) {
		}

		public void mmStructureChanged(MatchMakerEvent<T, C> evt) {
		}
	}
}