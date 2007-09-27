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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.sqlpower.matchmaker.AbstractMatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchRuleSet;

/**
 * An abstract implementation of the MungeStep interface. The only
 * method that extending classes are required to implement would be
 * the {@link #call()} method, which would implement the functionality
 * of a particular MungeStep.
 */
public abstract class AbstractMungeStep extends AbstractMatchMakerObject<MungeStep, MungeStepOutput> implements MungeStep {
	
	/**
	 * A list of MungeStepOutput objects that the parent MungeStep
	 * has output, which this MungeStep will use for its input.
	 */
	private List<Input> inputs = new ArrayList<Input>();
	
	/**
	 * A map of configuration parameters for this MungeStep.
	 */
	private Map<String,String> parameters = new HashMap<String, String>();
	
	/**
	 * The MatchClass that this MungeStep belongs to.
	 */
	private MatchRuleSet parent;
	
	public List<MungeStepOutput> getInputs() {
		List<MungeStepOutput> values = new ArrayList<MungeStepOutput>();
		for (Input in: inputs) {
			values.add(in.current);
		}
		return values;
	}
	
	public void connectInput(int index, MungeStepOutput o) {
		inputs.get(index).current = o;
	}

	public int addInput(InputDescriptor desc) {
		Input in = new Input(null, desc);
		inputs.add(in);
		getEventSupport().firePropertyChange("inputs", null, in);
		return inputs.size()-1;
	}

	public void removeInput(int index) {
		if (index >= inputs.size()) {
			throw new IndexOutOfBoundsException(
			"There is no IOConnector at the give index.");
		}
		getEventSupport().firePropertyChange("inputs", inputs.get(index), null);
		inputs.remove(index);
	}

	public String getParameter(String name) {
		return parameters.get(name);
	}

	public void setParameter(String name, String newValue) {
		String oldValue = parameters.get(name);
		parameters.put(name, newValue);
		getEventSupport().firePropertyChange(name, oldValue, newValue);
	}

	public MungeStep duplicate(MatchMakerObject parent,
			MatchMakerSession session) {
		throw new UnsupportedOperationException("Duplicate is not supported");
	}
	
	public MatchRuleSet getParent() {
		return parent;
	}
	
	// TODO: Investigate whether this will break Hibernate 
	// when implementing the persistence stuff
	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}
	
	// TODO: Investigate whether this will break Hibernate 
	// when implementing the persistence stuff
	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}
	
	public InputDescriptor getInputDescriptor(int inputNumber) {
		return inputs.get(inputNumber).descriptor;
	}
	
	/**
	 * This is the pairing between a MungeStepOutput value and its InputDescriptor.
	 * The reason for this class is to avoid maintaining two separate collections of
	 * MungeStepOutputs and their InputDescriptors.
	 */
	private class Input{
		
		/**
		 * The MungeStepOutput containing the value of this input.
		 */
		MungeStepOutput current;
		
		/**
		 * The attributes of this input.
		 */
		InputDescriptor descriptor;
		
		Input(MungeStepOutput current, InputDescriptor descriptor) {
			this.current = current;
			this.descriptor = descriptor;
		}
	}

    /**
     * Does nothing, because most steps do not need to allocate any resources.
     * If your step needs to allocate resources (perform a database query, open
     * a file, connect to a server, and so on), you should override this method.
     */
    public void open() throws Exception {
        // no op
    }

    /**
     * Does nothing, because most steps do not need to allocate any resources.
     * If you override the {@link #open()} method, you should override this method
     * too and clean up the resources.
     */
    public void close() throws Exception {
        // no op
    }
}
