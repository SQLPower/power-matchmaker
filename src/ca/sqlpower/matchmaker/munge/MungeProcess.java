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
import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.matchmaker.AbstractMatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.PotentialMatchRecord;
import ca.sqlpower.matchmaker.Project;

/**
 * A set of MungeSteps. The child type is {@link MungeStep}.
 * {@link #matchPriority} can be NULL, and the constructor
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
	 * The default color of a munge process is red
	 */
	public static final Color DEFAULT_COLOR = new Color(255, 0, 0);
	
	/**
	 * Object ID needed by Hibernate 
	 */
	private Long oid;
	
	private String desc;
    
    /**
     * The priority that this munge process will get evaluated relative to other MungeProcess'
     * priority.  A lower number means a higher priority. Will usually be NULL or in the range 
     * of 0-100, but this range is not enforced.
     * 
     */
	private Short matchPriority = 0;
    
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
     * The colour associated with this munge process by the user.  Useful
     * in the GUI.
     */
	private Color colour = DEFAULT_COLOR;

	/**
	 * The MungeStep that is used to accumulate the resulting munged data.
	 * Any class can get the munge results by calling {@link #getResults()},
	 * which will delegate to getting the results from this output step.
	 */
	private MungeResultStep resultStep;
	
	/**
	 * The input steps that are presently here.	 
	 */
	private List<SQLInputStep> inputSteps = new ArrayList<SQLInputStep>();
	
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

	public Short getMatchPriority() {
		return matchPriority;
	}

	public void setMatchPriority(Short matchPriority) {
        Short oldValue = this.matchPriority;
		this.matchPriority = matchPriority;
		getEventSupport().firePropertyChange("matchPriority", oldValue, matchPriority);
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
    
    public void setColour(Color mungeProcessColor) {
        Color oldValue = this.colour;
        this.colour = mungeProcessColor;
        getEventSupport().firePropertyChange("colour", oldValue, mungeProcessColor);
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
		MungeProcess mungeProcess = new MungeProcess();
		mungeProcess.setActive(getActive());
		mungeProcess.setDesc(getDesc()==null?null:new String(getDesc()));
		mungeProcess.setFilter(getFilter()==null?null:new String(getFilter()));
        mungeProcess.setColour(getColour() == null ? null : new Color(getColour().getRGB()));
		mungeProcess.setMatchPriority(getMatchPriority()==null?null:new Short(getMatchPriority()));
		mungeProcess.setName(getName()==null?null:new String(getName()));
		mungeProcess.setSession(s);
		mungeProcess.setVisible(isVisible());
		
		for (MungeStep step : getChildren()) {
            MungeStep newStep = step.duplicate(mungeProcess,s);
			mungeProcess.addChild(newStep);
		}
		
		return mungeProcess;
	}
	
	/**
	 * Get the results of the munging process, as a list of MungeResult.
	 * This list will be empty if the munge process has not been run through
	 * a munge processor.
	 * @throws NullPointerException if the output step has not been set.
	 */
	public List<MungeResult> getResults() {
		if (resultStep == null) {
			throw new NullPointerException("The output step for this process has not been set!");
		}
		return resultStep.getResults();
	}

	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	protected void addImpl(int index, MungeStep child) {
		includeMungeStep(child);
		super.addImpl(index, child);
	}
	
	/**
	 * Updates the result and input steps if nessary.
	 * 
	 * @param child The child to add to the process
	 */
	private void includeMungeStep(MungeStep child) {
		if (child instanceof SQLInputStep) {
			inputSteps.add((SQLInputStep) child);
			if (resultStep != null) {
				resultStep.addInputStep((SQLInputStep) child);
			}
		} else if (child instanceof MungeResultStep) {
			if (resultStep != null && resultStep != child) {
				throw new IllegalStateException("A munge process can only have one munge result step");
			} else if (resultStep == null) {
				this.resultStep = (MungeResultStep) child;
				for (SQLInputStep input : inputSteps) {
					this.resultStep.addInputStep(input);
				}
			}
		}	
	}

	@Override
	public void removeChild(MungeStep child) {
		if (child instanceof MungeResultStep) {
			throw new IllegalStateException("Removal of munge result step not allowed!");
		} else {
			super.removeChild(child);
		}
	}
	
	@Override
	protected void setChildren(List<MungeStep> children) {
		super.setChildren(children);
		for (MungeStep ms : children) {
			includeMungeStep(ms);
		}
	}
}
