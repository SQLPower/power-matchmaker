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
	private List<MungeStepOutput> inputs = new ArrayList<MungeStepOutput>();
	
	/**
	 * A map of configuration parameters for this MungeStep.
	 */
	private Map<String,String> parameters = new HashMap<String, String>();
	
	/**
	 * The MatchClass that this MungeStep belongs to
	 */
	private MatchRuleSet parent;
	
	public List<MungeStepOutput> getInputs() {
		return inputs;
	}

	/**
	 * Adds the given MungeStepOuput as in input for this MungeStep.
	 * Any class that extends AbstractMungeStep that is expecting 
	 * particular data types in its input should override this method,
	 * as this implementation does not do any type checking on the input.
	 * <p>
	 * Additionally, this method fires a property change event using the
	 * {@ MatchMakerEventSupport#firePropertyChange(String, Object, Object)
	 * , with property name of "inputs" and old and new values set to null.
	 * <p>
	 * Note that this method may throw unchecked exceptions if the 
	 * input's data type does not match what the munge step expects
	 */
	public void addInput(MungeStepOutput o) {
		inputs.add(o);
		getEventSupport().firePropertyChange("inputs", null, o);
	}

	public boolean removeInput(MungeStepOutput o) {
		if (!o.equals(null)) {
			getEventSupport().firePropertyChange("inputs", o, null);
			return inputs.remove(o);
		}
		return false;
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
}
