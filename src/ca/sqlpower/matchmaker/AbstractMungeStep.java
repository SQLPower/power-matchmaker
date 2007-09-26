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

package ca.sqlpower.matchmaker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	 * A list of MungeStepOutput objects that this MungeStep will
	 * output.
	 */
	private List<MungeStepOutput> outputs = new ArrayList<MungeStepOutput>();
	
	/**
	 * A map of configuration parameters for this MungeStep.
	 */
	private Map<String,String> parameters = new HashMap<String, String>();
	
	/**
	 * The MatchClass that this MungeStep belongs to
	 */
	private MatchRuleSet parent;
	
	public void addInput(MungeStepOutput o) {
		inputs.add(o);
	}
	
	public List<MungeStepOutput> getInputs() {
		return inputs;
	}

	public List<MungeStepOutput> getOutputs() {
		return outputs;
	}
	
	public String getParameter(String name) {
		return parameters.get(name);
	}

	public boolean removeInput(MungeStepOutput o) {
		if (!o.equals(null)) {
			return inputs.remove(o);
		}
		return false;
	}

	public void setParameter(String name, String value) {
		parameters.put(name, value);
	}

	public MungeStep duplicate(MatchMakerObject parent,
			MatchMakerSession session) {
		throw new UnsupportedOperationException("Duplicate is not supported");
	}
	
	public MatchRuleSet getParent() {
		return parent;
	}
}
