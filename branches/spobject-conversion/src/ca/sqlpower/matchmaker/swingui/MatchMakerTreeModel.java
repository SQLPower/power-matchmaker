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

import java.beans.PropertyChangeEvent;
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

import ca.sqlpower.matchmaker.FolderParent;
import ca.sqlpower.matchmaker.MMRootNode;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.TranslateGroupParent;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.TransactionEvent;

/**
 * A tree model implementation that adapts a hierarchy of MatchMakerObjects
 * to the Swing TreeModel interface.  It uses a simulated root node which
 * should normally not be made visible for the user, since it adds no value
 * or meaning to anything.
 */
public class MatchMakerTreeModel implements TreeModel {

	private static final Logger logger = Logger.getLogger(MatchMakerTreeModel.class);

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
     * The session this tree model belongs to.  For testing, it's acceptable
     * for this to be just a stub, but you will not be able to call
     * {@link ProjectActionNode#performAction()} unless this is actually a
     * MatchMakerSwingSession.
     */
    private final MatchMakerSession session;

    /**
     * This tree's root node.
     */
	private final MMRootNode rootNode;

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
	public MatchMakerTreeModel(MMRootNode rootNode, MatchMakerSession s) {
		session = s;
        this.rootNode = rootNode;
        
        this.current = rootNode.getCurrentFolderParent();

        this.backup = rootNode.getBackupFolderParent();
		
		this.translate = rootNode.getTranslateGroupParent();
        
		SQLPowerUtils.listenToHierarchy(rootNode, listener);
		SQLPowerUtils.listenToShallowHierarchy(cacheLisener, current);
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
            for (MatchMakerObject child : project.getChildren(MatchMakerObject.class)) {
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
        		if (!mmoParent.getChildren(MatchMakerObject.class).get(i).isVisible()) {
        			real++;
        		}
        	}
            mmoChild = mmoParent.getChildren(MatchMakerObject.class).get(real);
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
        
        for (MatchMakerObject child : mmo.getChildren(MatchMakerObject.class)) {
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
		return rootNode;
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
		fireTreeNodesChanged(new TreeModelEvent(rootNode, new TreePath(rootNode)));
	}

	private class TreeModelEventAdapter
			implements SPListener {

		@Override
		public void childAdded(SPChildEvent e) {
			TreePath paths = getPathForNode((MatchMakerObject)e.getSource());
            MatchMakerObject children[] = {(MatchMakerObject) e.getSource()};
            int indices[] = {e.getIndex()};
			TreeModelEvent evt = new TreeModelEvent((MatchMakerObject) e.getSource(), paths,
					indices ,children);
			if (logger.isDebugEnabled()) {
                logger.debug("Got MM children inserted event!");
                StringBuilder sb = new StringBuilder();
                MatchMakerObject mmo = (MatchMakerObject)e.getSource();
                while (mmo != null) {
                    sb.insert(0, "->" + mmo.getName());
                    mmo = (MatchMakerObject)mmo.getParent();
                }
                logger.debug("Parent of inserted MMObject: "+sb);
                logger.debug("          inserted child: "+e.getChild());
                
                sb = new StringBuilder();
                sb.append("{");
                sb.append(e.getIndex());
                sb.append("}");
                logger.debug("     inserted child indices: "+sb);
                logger.debug("Traceback:", new Exception());
			}
			fireTreeNodesInserted(evt);
			SQLPowerUtils.listenToHierarchy(e.getChild(), listener);
		}

		@Override
		public void childRemoved(SPChildEvent e) {
			TreePath path = getPathForNode((MatchMakerObject) e.getSource());
            if (logger.isDebugEnabled()) {
                logger.debug("Got MM children removed event!");
                StringBuilder sb = new StringBuilder();
                MatchMakerObject mmo = (MatchMakerObject) e.getSource();
                while (mmo != null) {
                    sb.insert(0, "->" + mmo.getName());
                    mmo = (MatchMakerObject) mmo.getParent();
                }
                logger.debug("Parent of removed MMObject: "+sb);
                logger.debug("          removed child: "+e.getChild());
                
                sb = new StringBuilder();
                sb.append("{");
                sb.append(e.getIndex());
                sb.append("}");
                logger.debug("     removed child index: "+sb);
                logger.debug("Traceback:", new Exception());
            }
            MatchMakerObject children[] = {(MatchMakerObject) e.getSource()};
            int indices[] = {e.getIndex()};
			TreeModelEvent evt = new TreeModelEvent((MatchMakerObject) e.getSource(), path,
					indices ,children);
            logger.debug("About to fire tree model event: "+e);
			fireTreeNodesRemoved(evt);
			SQLPowerUtils.unlistenToHierarchy(e.getChild(), listener);
		}

		@Override
		public void transactionStarted(TransactionEvent e) {
			// no-op
		}

		@Override
		public void transactionEnded(TransactionEvent e) {
			// no-op
		}

		@Override
		public void transactionRollback(TransactionEvent e) {
			// no-op
		}

		@Override
		public void propertyChanged(PropertyChangeEvent e) {
            logger.debug("Got PropertyChanged event. property="+
                    e.getPropertyName()+"; source="+e.getSource());
			TreePath paths = getPathForNode((MatchMakerObject)e.getSource());
			TreeModelEvent evt = new TreeModelEvent(e.getSource(), paths);
			fireTreeNodesChanged(evt);
			
		}
	}

	public TreePath getPathForNode(MatchMakerObject source) {
		List<MatchMakerObject> path = new LinkedList<MatchMakerObject>();
		while (source != null) {
			path.add(0, source);
			source = (MatchMakerObject)source.getParent();
		}
		return new TreePath(path.toArray());
	}
	
	public void addFolderToCurrent(PlFolder folder){
		current.addChild(folder);
	}
	
	private class ActionCacheEventAdapter implements SPListener{

		public void childAdded(SPChildEvent e) {
			if (e.getSource() instanceof FolderParent) {
				PlFolder folder = (PlFolder)e.getChild();
				folder.addSPListener(this);
				}
		}

		public void childRemoved(SPChildEvent e) {
			if (e.getSource() instanceof FolderParent) {
				PlFolder folder = (PlFolder)e.getChild();
				for (Project project : folder.getChildren(Project.class)) {
					projectActionCache.remove(project);
				}
				folder.removeSPListener(this);
			} else if (e.getSource() instanceof PlFolder) {
				for (Project project : e.getSource().getChildren(Project.class)) {
					projectActionCache.remove(project);
				}
			}
		}

		@Override
		public void propertyChanged(PropertyChangeEvent evt) {
			//no-op
		}

		@Override
		public void transactionStarted(TransactionEvent e) {
			//no-op
		}

		@Override
		public void transactionEnded(TransactionEvent e) {
			//no-op
		}

		@Override
		public void transactionRollback(TransactionEvent e) {
			//no-op
		}
	}
}