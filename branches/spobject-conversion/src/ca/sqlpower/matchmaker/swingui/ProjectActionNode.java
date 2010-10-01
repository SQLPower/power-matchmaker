/* Copyright (c) 2010, SQL Power Group Inc.
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

import java.util.Collections;
import java.util.List;

import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.swingui.MatchMakerTreeModel.ProjectActionType;
import ca.sqlpower.object.SPObject;

/**
     * A simple MatchMakerObject that holds a single Swing Action.  We create
     * these as extra children for the Project objects in the tree so the entire
     * project workflow is represented in one place, with pretty pictures and
     * everything.
     * 
     * This extends AbstractMatchMakerObject for convenience, but this does not
     * need to be persisted since it has nothing to do with the core, only with
     * the UI.
     */
public class ProjectActionNode{

	/**
     * List of allowable child types
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.emptyList();
	
    private final ProjectActionType projectActionType;
    private final Project project;
    String name;
    boolean visible;
    
    public ProjectActionNode(ProjectActionType projectActionType, Project project) {
        this.projectActionType = projectActionType;
        this.project = project;
        setName(projectActionType.toString());
        visible = true;
    }
    
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

	public List<? extends SPObject> getChildren() {
		return Collections.emptyList();
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isVisible() {
		return visible;
	}
}