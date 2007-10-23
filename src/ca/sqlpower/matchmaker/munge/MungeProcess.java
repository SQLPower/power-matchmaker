/*
 * Copyright (c) 2007, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.munge;

import java.awt.Color;
import java.util.List;

import ca.sqlpower.matchmaker.AbstractMatchMakerObject;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.PotentialMatchRecord;

/**
 * A set of MungeSteps. The child type is {@link MungeStep}.
 * {@link #matchPercent} can be NULL, and the constructor
 * sets it to NULL by default.
 */
public class MungeProcess
	extends AbstractMatchMakerObject<MungeProcess, MungeStep> {
	
	/**
	 * This is the name given to a rule set made by the Match Maker
	 * to identify any new {@link PotentialMatchRecord} that are also created by
	 * the Match Maker when previously unconnected nodes are defined to be
	 * related.
	 */
	public static final String SYNTHETIC_MATCHES = "Synthetic_matches";

	/**
	 * Object ID needed by Hibernate 
	 */
	private Long oid;
	
	private String desc;
    
    /**
     * The user's confidence that this group of match rule produces
     * actual matches.  Will usually be NULL or in the range 0-100, but
     * this range is not enforced.
     */
	private Short matchPercent;
    
    /**
     * A SQL Where fragment that restricts this set of rules to a subset
     * of the whole match table.
     */
	private String filter;
    
    /**
     * Marks whether or not the match engine should process this munge process when
     * running a match process.  True means it will be processed.
     */
	private boolean active = true;
    
    /**
     * The colour associated with this group of rule by the user.  Useful
     * in the GUI.
     */
	private Color colour;

	/**
	 * The MungeStep that is used to accumulate the resulting munged data.
	 * Any class can get the munge results by calling {@link #getResults()},
	 * which will delegate to getting the results from this output step.
	 */
	private MungeResultStep outputStep;
	
	/**
     * Constructor that sets up a default Munge process.
	 */
	public MungeProcess() {
	}

    public Long getOid() {
        return oid;
    }

    public void setOid(Long oid) {
        this.oid = oid;
    }
    
    /**
     * Gets the grandparent of this object in the MatchMaker object tree.  If the parent
     * (a folder) is null, returns null.
     */
    public Project getParentProject() {
        MatchMakerObject parentFolder = getParent();
        if (parentFolder == null) {
            return null;
        } else {
            return (Project) parentFolder.getParent();
        }
    }

    /**
     * Sets the parent of this object to be the rule set folder of the given project object
     *
     * this will fire a <b>parent</b> changed event not a parent match event
     */
    public void setParentProject(Project grandparent) {
        if (grandparent == null) {
            setParent(null);
        } else {
            setParent(grandparent.getMungeProcessesFolder());
        }
    }

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		String oldDesc = this.desc;
		this.desc = desc;
		getEventSupport().firePropertyChange("desc", oldDesc, desc);
	}

	public Short getMatchPercent() {
		return matchPercent;
	}

	public void setMatchPercent(Short matchPercent) {
        Short oldValue = this.matchPercent;
		this.matchPercent = matchPercent;
		getEventSupport().firePropertyChange("matchPercent", oldValue, matchPercent);
	}

	public boolean getActive() {
		return active;
	}

	public void setActive(boolean active) {
		boolean oldValue = this.active;
		this.active = active;
		getEventSupport().firePropertyChange("active", oldValue, active);
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		String oldValue = this.filter;
		this.filter = filter;
		getEventSupport().firePropertyChange("filter", oldValue, filter);
	}

    public Color getColour() {
        return colour;
    }
    
    public void setColour(Color groupColor) {
        Color oldValue = this.colour;
        this.colour = groupColor;
        getEventSupport().firePropertyChange("colour", oldValue, groupColor);
    }
    
	@Override
	public int hashCode() {
        int result = ((getName() == null) ? 0 : getName().hashCode());
        return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof MungeProcess))
			return false;
		final MungeProcess other = (MungeProcess) obj;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		return true;
	}

	/**
	 * duplicate all the properties of the MungeProcess 
	 * and it's children, except oid and parent
     * 
	 * @return new MungeProcess object with the same properties
	 * and children
	 */
	public MungeProcess duplicate(MatchMakerObject parent, MatchMakerSession s){
		MungeProcess group = new MungeProcess();
		group.setActive(getActive());
		group.setDesc(getDesc()==null?null:new String(getDesc()));
		group.setFilter(getFilter()==null?null:new String(getFilter()));
        group.setColour(getColour() == null ? null : new Color(getColour().getRGB()));
		group.setMatchPercent(getMatchPercent()==null?null:new Short(getMatchPercent()));
		group.setName(getName()==null?null:new String(getName()));
		group.setSession(s);
		group.setVisible(isVisible());
		
		for ( MungeStep step : getChildren()) {
            MungeStep newStep = step.duplicate(group,s);
			group.addChild(newStep);
		}
		return group;
	}
	
	/**
	 * Get the results of the munging process, as a list of MungeResult.
	 * This list will be empty if the munge process has not been run through
	 * a munge processor.
	 * @throws NullPointerException if the output step has not been set.
	 */
	public List<MungeResult> getResults() {
		if (outputStep == null) {
			throw new NullPointerException("The output step for this process has not been set!");
		}
		return outputStep.getResults();
	}

	public void setOutputStep(MungeResultStep outputStep) {
		this.outputStep = outputStep;
	}

	@Override
	public String toString() {
		return getName();
	}
}
