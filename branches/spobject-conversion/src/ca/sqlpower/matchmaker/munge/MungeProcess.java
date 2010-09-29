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
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonProperty;

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
				Arrays.asList(MungeStep.class)));
	
	/**
	 * The intermediate munge steps used between start and finish of the process.
	 * They appear in the the middle of getChildren();
	 */
	private final List<MungeStep> mungeSteps = new ArrayList<MungeStep>();

	/**
	 * The MungeStep that is used to accumulate the resulting munged data.
	 * Any class can get the munge results by calling {@link #getResults()},
	 * which will delegate to getting the results from this output step.
	 * This will appear last in getChildren();
	 */
	private MungeResultStep resultStep;
	
	/**
	 * The input steps. It includes things like string constants as well as the main input.
	 * They appear first in getChildren();
	 */
	private final List<SQLInputStep> inputSteps = new ArrayList<SQLInputStep>();
	
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
     * Constructor that sets up a default Munge process.
	 */
	@Constructor
	public MungeProcess() {
	}

	@NonProperty
	public Long getOid() {
        return oid;
    }

	@NonProperty
    public void setOid(Long oid) {
        this.oid = oid;
    }
    
    @Override
    @Accessor
	public Project getParent() {
    	return (Project) super.getParent();
    }
    
    @Override
    @Mutator
    public void setParent(SPObject parent) {
    	if (!(parent instanceof Project)) {
    		throw new IllegalArgumentException("The parent " + parent + 
    				" cannot be the parent of a munge process because it is not a project.");
    	}
    	super.setParent(parent);
    }
    
    @Accessor
	public String getDesc() {
		return desc;
	}

    @Mutator
	public void setDesc(String desc) {
		String oldDesc = this.desc;
		this.desc = desc;
		firePropertyChange("desc", oldDesc, desc);
	}

	@Accessor
	public Integer getMatchPriority() {
		return matchPriority;
	}

	@Mutator
	public void setMatchPriority(Integer matchPriority) {
        Integer oldValue = this.matchPriority;
		this.matchPriority = matchPriority;
		firePropertyChange("matchPriority", oldValue, matchPriority);
	}

	@Accessor
	public boolean getActive() {
		return active;
	}

	@Mutator
	public void setActive(boolean active) {
		boolean oldValue = this.active;
		this.active = active;
		firePropertyChange("active", oldValue, active);
	}

	@Accessor
	public String getFilter() {
		return filter;
	}

	@Mutator
	public void setFilter(String filter) {
		String oldValue = this.filter;
		this.filter = filter;
		firePropertyChange("filter", oldValue, filter);
	}
	
	/**
	 * Indicates whether this munge process will be displayed on the Match Validation screen
	 */
	@Accessor
	public boolean isValidate() {
		return validate;
	}

	/**
	 * Sets whether this munge process will be displayed on the Match Validation screen
	 */
	@Mutator
	public void setValidate(boolean validate) {
		boolean oldValue = this.validate;
		this.validate = validate;
		firePropertyChange("validate", oldValue, validate);
	}

	@Accessor
	public Color getColour() {
        return colour;
    }
    
	@Mutator
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
		
		for (MungeStep step : getChildren(MungeStep.class)) {
            MungeStep newStep = (MungeStep)step.duplicate(mungeProcess,s);
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
	@NonProperty
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

	/**
	 * Add only munge input and middle steps, not results steps using this method.
	 */
	public void addChild(SPObject spo) {
		if(spo instanceof SQLInputStep) {
			addChild(spo, inputSteps.size());
		} else if(spo instanceof MungeResultStep) {
			addChild(spo, 0);
		} else {
			addChild(spo, mungeSteps.size());
		}
	}
	
	@Override
	protected void addChildImpl(SPObject spo, int index) {
		if (spo instanceof SQLInputStep) {
			inputSteps.add(index, (SQLInputStep)spo);
			for(AddressCorrectionMungeStep s : getChildren(AddressCorrectionMungeStep.class)) {
				s.setInputStep((SQLInputStep)spo);
			}
			fireChildAdded(SQLInputStep.class, spo, index);
		} else if (spo instanceof AddressCorrectionMungeStep) {
			for (SQLInputStep input : inputSteps) {
				((AddressCorrectionMungeStep)spo).setInputStep(input);
			}
			mungeSteps.add(index, (MungeStep)spo);
			fireChildAdded(MungeStep.class, spo, index);
		} else if (spo instanceof MungeResultStep) {
			setResultStep((MungeResultStep) spo);
		} else {
			mungeSteps.add(index, (MungeStep)spo);
			fireChildAdded(MungeStep.class, spo, index);
		}
	}

	/**
	 * Sets the result step of a munge process. This can only be done once and
	 * should be done in the constructor but is not yet due to backwards
	 * compatibility with old Hibernate code. This should be fixed sometime soon
	 * in the future.
	 */
	@NonProperty
	public void setResultStep(MungeResultStep step) {
		if (resultStep == null) {
			resultStep = step;
			for (SQLInputStep input : inputSteps) {
				this.resultStep.addInputStep(input);
			}
			resultStep.setParent(this);
		} else {
			throw new IllegalArgumentException("The MungeResultStep should be set at the constructor of MungeProcess.");
		}
	}

	@Override
	protected boolean removeChildImpl(SPObject spo) {
		if (spo instanceof MungeStep)
		{
			int index = mungeSteps.indexOf(spo);
			boolean removed = mungeSteps.remove(spo);
			fireChildRemoved(MungeStep.class, spo, index);
			return removed;
		} else if (spo instanceof SQLInputStep) {
			int index = inputSteps.indexOf(spo);
			boolean removed = inputSteps.remove(spo);
			fireChildRemoved(SQLInputStep.class, spo, index);
			return removed;
		} else if (spo instanceof AddressCorrectionMungeStep) {
			int index = mungeSteps.indexOf(spo);
			boolean removed = mungeSteps.remove(spo);
			fireChildRemoved(AddressCorrectionMungeStep.class, spo, index);
			return removed;
		}
		return false;
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
			for (SPObject spo : ms.getMungeStepOutputs()) {
				MungeStepOutput mso = (MungeStepOutput) spo;
				for (SPObject spo2 : getChildren()) {
					MungeStep child = (MungeStep) spo2;
					child.disconnectInput(mso);
				}
			}
			
			try{
				removeChild(ms);
			} catch (ObjectDependentException e) {
				throw new RuntimeException(e);
			}
			commit();
		} catch(RuntimeException e) {
			rollback(e.getMessage());
			throw e;
		}
	}
	

	/**
	 * Gets the result munge step in the process, i.e. the munge step everything
	 * goes to and has no outputs.
	 */
	@NonProperty
	public MungeResultStep getResultStep() {
		return resultStep;
	}

	@Override
	@NonProperty
	public List<SPObject> getChildren() {
		List<SPObject> children = new ArrayList<SPObject>();
		children.addAll(inputSteps);
		children.addAll(mungeSteps);
		if (resultStep != null) {
			children.add(resultStep);
		}
		return Collections.unmodifiableList(children);
	}

	/**
	 * Gets the input munge steps in the process, i.e. those with only
	 * inputs.
	 */
	@NonProperty
	public List<SQLInputStep> getInputSteps() {
		return Collections.unmodifiableList(inputSteps);
	}
	
	/**
	 * Gets the intermediate munge steps in the process, i.e. those with both
	 * inputs and outputs.
	 */
	@NonProperty
	public List<MungeStep> getMungeSteps() {
		return Collections.unmodifiableList(mungeSteps);
	}
	
	@Override
	@NonProperty
	public List<Class<? extends SPObject>> getAllowedChildTypes() {
		return allowedChildTypes;
	}
}
