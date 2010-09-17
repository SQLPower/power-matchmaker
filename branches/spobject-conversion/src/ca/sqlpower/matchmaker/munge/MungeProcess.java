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

package ca.sqlpower.matchmaker.munge;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.matchmaker.AbstractMatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.PotentialMatchRecord;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.swingui.ColorScheme;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.object.SPObject;

/**
 * A set of MungeSteps. The child type is {@link MungeStep}.
 * {@link #matchPriority} can be NULL, and the constructor
 * sets it to NULL by default.
 */
public class MungeProcess extends AbstractMatchMakerObject {
	
	/**
	 * This the list that tells us the allowable child types and their order in getting children.
	 */
	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(AddressCorrectionMungeStep.class, MungeResultStep.class,
						SQLInputStep.class)));
	
	protected List<AddressCorrectionMungeStep> addressCorrectionMungeSteps = new ArrayList<AddressCorrectionMungeStep>();
	protected List<MungeResultStep> mungeResultSteps = new ArrayList<MungeResultStep>();
	protected List<SQLInputStep> sqlInputSteps = new ArrayList<SQLInputStep>();
	
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
	public static final Color DEFAULT_COLOR = ColorScheme.BREWER_SET19.get(0);
	
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
	private Integer matchPriority = 0;
    
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
	 * Marks whether or not the match validator should show nodes and edges from
	 * this munge process.  True means it will be shown
	 */
	private boolean validate = true;
	
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
    public void setParentProject(Project parent) {
        if (parent == null) {
            setParent(null);
        } else {
            setParent(parent);
        }
    }

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		String oldDesc = this.desc;
		this.desc = desc;
		firePropertyChange("desc", oldDesc, desc);
	}

	public Integer getMatchPriority() {
		return matchPriority;
	}

	public void setMatchPriority(Integer matchPriority) {
        Integer oldValue = this.matchPriority;
		this.matchPriority = matchPriority;
		firePropertyChange("matchPriority", oldValue, matchPriority);
	}

	public boolean getActive() {
		return active;
	}

	public void setActive(boolean active) {
		boolean oldValue = this.active;
		this.active = active;
		firePropertyChange("active", oldValue, active);
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		String oldValue = this.filter;
		this.filter = filter;
		firePropertyChange("filter", oldValue, filter);
	}
	
	/**
	 * Indicates whether this munge process will be displayed on the Match Validation screen
	 */
	public boolean isValidate() {
		return validate;
	}

	/**
	 * Sets whether this munge process will be displayed on the Match Validation screen
	 */
	public void setValidate(boolean validate) {
		boolean oldValue = this.validate;
		this.validate = validate;
		firePropertyChange("validate", oldValue, validate);
	}

    public Color getColour() {
        return colour;
    }
    
    public void setColour(Color mungeProcessColor) {
        Color oldValue = this.colour;
        this.colour = mungeProcessColor;
        firePropertyChange("colour", oldValue, mungeProcessColor);
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
		mungeProcess.setValidate(isValidate());
		mungeProcess.setDesc(getDesc()==null?null:new String(getDesc()));
		mungeProcess.setFilter(getFilter()==null?null:new String(getFilter()));
        mungeProcess.setColour(getColour() == null ? null : new Color(getColour().getRGB()));
		mungeProcess.setMatchPriority(getMatchPriority()==null?null:new Integer(getMatchPriority()));
		mungeProcess.setName(getName()==null?null:new String(getName()));
		mungeProcess.setSession(s);
		mungeProcess.setVisible(isVisible());
		
		for (SPObject spo : getChildren()) {
			MungeStep step = (MungeStep) spo;
            MungeStep newStep = (MungeStep) step.duplicate(mungeProcess,s);
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

	public void addChild(SPObject spo) {
		int size = -1;
		if(spo instanceof AddressCorrectionMungeStep) {
			size = addressCorrectionMungeSteps.size();
		} else if (spo instanceof MungeResultStep) {
			size = mungeResultSteps.size();
		} else {
			size = sqlInputSteps.size();
		}
		addChild(spo, size);
	}
	
	@Override
	protected void addChildImpl(SPObject spo, int index) {
		if(spo instanceof AddressCorrectionMungeStep) {
			addAddressCorrectionMungeStep((AddressCorrectionMungeStep)spo, index);
		} else if (spo instanceof MungeResultStep) {
			addMungeResultStep((MungeResultStep)spo, index);
		} else {
			addSQLInputStep((SQLInputStep)spo, index);
		}
	}
	
	private void addSQLInputStep(SQLInputStep spo, int index) {
		sqlInputSteps.add(index, spo);
		fireChildAdded(SQLInputStep.class, spo, index);
	}

	private void addMungeResultStep(MungeResultStep spo, int index) {
		mungeResultSteps.add(index, spo);
		fireChildAdded(MungeResultStep.class, spo, index);
	}

	private void addAddressCorrectionMungeStep(AddressCorrectionMungeStep spo, int index) {
		addressCorrectionMungeSteps.add(index, spo);
		fireChildAdded(AddressCorrectionMungeStep.class, spo, index);
	}

	@Override
	protected boolean removeChildImpl(SPObject spo) {
		boolean removed;
		if(spo instanceof AddressCorrectionMungeStep) {
			removed = removeAddressCorrectionMungeStep((AddressCorrectionMungeStep)spo);
		} else if (spo instanceof MungeResultStep) {
			removed = removeMungeResultStep((MungeResultStep)spo);
		} else {
			removed = removeSQLInputStep((SQLInputStep)spo);
		}
		return removed;
	}

	private boolean removeSQLInputStep(SQLInputStep spo) {
		int index = sqlInputSteps.indexOf(spo);
		boolean removed = sqlInputSteps.remove(spo);
		fireChildRemoved(SQLInputStep.class, spo, index);
		return removed;
	}

	private boolean removeMungeResultStep(MungeResultStep spo) {
		int index = mungeResultSteps.indexOf(spo);
		boolean removed = mungeResultSteps.remove(spo);
		fireChildRemoved(MungeResultStep.class, spo, index);
		return removed;
	}

	public boolean removeAddressCorrectionMungeStep(AddressCorrectionMungeStep spo) {
		int index = addressCorrectionMungeSteps.indexOf(spo);
		boolean removed = addressCorrectionMungeSteps.remove(spo);
		fireChildRemoved(AddressCorrectionMungeStep.class, spo, index);
		return removed;
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
			for (SPObject spo : getChildren()) {
				MungeStep step = (MungeStep) spo;
				if (child instanceof AddressCorrectionMungeStep) {
					((AddressCorrectionMungeStep)step).setInputStep(child);
				}
			}
		} else if (child instanceof MungeResultStep) {
			if (resultStep != null && resultStep != child) {
				throw new IllegalStateException("A transformation can only have one result transformer");
			} else if (resultStep == null) {
				this.resultStep = (MungeResultStep) child;
				for (SQLInputStep input : inputSteps) {
					this.resultStep.addInputStep(input);
				}
			}
		} else if (child instanceof AddressCorrectionMungeStep) {
			for (SQLInputStep input : inputSteps) {
				((AddressCorrectionMungeStep)child).setInputStep(input);
			}
		}
	}
	
	public void removeChildAndInputs(MungeStep ms) {
		try {
			begin("removing child and inputs");
			
			//disconnect inputs
			for (int x = 0; x < ms.getMSOInputs().size(); x++) {
				MungeStepOutput link = (MungeStepOutput) ms.getMSOInputs().get(x);
				if (link != null) {
					ms.disconnectInput(x);
				}
			}
			
			//disconnect outputs
			for (SPObject spo : ms.getChildren()) {
				MungeStepOutput mso = (MungeStepOutput) spo;
				for (SPObject spo2 : getChildren()) {
					MungeStep child = (MungeStep) spo2;
					child.disconnectInput(mso);
				}
			}
			
			try{
				removeChild(ms);
			} catch (ObjectDependentException e) {
				throw new RuntimeException();
			}
			commit();
		} catch(RuntimeException e) {
			rollback(e.getMessage());
			throw e;
		}
	}
	
	public void setChildren(List<MungeStep> children) {
		for (MungeStep ms : children) {
			includeMungeStep(ms);
		}
	}
	
	MungeResultStep getResultStep() {
		return resultStep;
	}

	@Override
	public List<? extends SPObject> getChildren() {
		List<SPObject> children = new ArrayList<SPObject>();
		children.addAll(addressCorrectionMungeSteps);
		children.addAll(mungeResultSteps);
		children.addAll(sqlInputSteps);
		return Collections.unmodifiableList(children);
	}
	
	@Override
	public List<Class<? extends SPObject>> getAllowedChildTypes() {
		return allowedChildTypes;
	}
}
