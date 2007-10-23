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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.AbstractMatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;

/**
 * An abstract implementation of the MungeStep interface. The only
 * method that extending classes are required to implement would be
 * the {@link #call()} method, which would implement the functionality
 * of a particular MungeStep.
 */
public abstract class AbstractMungeStep extends AbstractMatchMakerObject<MungeStep, MungeStepOutput> implements MungeStep {
	
	/**
	 * A list of Input objects, each containing a MungeStepOutput
	 * that the parent MungeStep has output, which this MungeStep 
	 * will use for its input.
	 */
	private List<Input> inputs = new ArrayList<Input>();
	
	/**
	 * A map of configuration parameters for this MungeStep.
	 */
	private Map<String,String> parameters = new HashMap<String, String>();
	
	
	/**
	 * This refers to whether a open() call has been made on this munge step.
	 */
	private boolean opened;
	
	/**
	 * The logger to print the inputs and outputs to
	 */
	private Logger logger;
	
	/**
	 * The session for this object
	 */
	private MatchMakerSession session;
	
	public AbstractMungeStep(MatchMakerSession session) {
		this.session = session;
	}
	
	public List<MungeStepOutput> getInputs() {
		List<MungeStepOutput> values = new ArrayList<MungeStepOutput>();
		for (Input in: inputs) {
			values.add(in.current);
		}
		return values;
	}
	
	public void connectInput(int index, MungeStepOutput o) {
		if (index >= getInputs().size()) {
			throw new IndexOutOfBoundsException("There is no input at the given index");
		}
		inputs.get(index).current = o;
		getEventSupport().firePropertyChange("inputs", index, null, o);
	}

	public void disconnectInput(int index) {
		if (index >= getInputs().size()) {
			throw new IndexOutOfBoundsException("There is no input at the given index");
		}
		getEventSupport().firePropertyChange("inputs", index,
				inputs.get(index).current, null);
		inputs.get(index).current = null;
	}
	
	public int addInput(InputDescriptor desc) {
		Input in = new Input(null, desc);
		inputs.add(in);
		int index = inputs.size()-1;
		getEventSupport().firePropertyChange("inputs", index, null, in);
		return index;
	}

	public void removeInput(int index) {
		if (index >= inputs.size()) {
			throw new IndexOutOfBoundsException(
			"There is no IOConnector at the give index.");
		}
		getEventSupport().firePropertyChange("inputs", index,
				inputs.get(index), null);
		inputs.remove(index);
	}

    public Collection<String> getParameterNames() {
        return Collections.unmodifiableSet(parameters.keySet());
    }
    
	public String getParameter(String name) {
		return parameters.get(name);
	}
	
	public Boolean getBooleanParameter(String name) {
		String param = parameters.get(name);
		if (param != null) {
			return Boolean.valueOf(param);
		} else {
			return null;
		}
	}
	
	public Integer getIntegerParameter(String name) {
		String param = parameters.get(name);
		if (param != null) {
			return Integer.valueOf(param);
		} else {
			return null;
		}
	}
	
	public void setParameter(String name, String newValue) {
		String oldValue = parameters.get(name);
		parameters.put(name, newValue);
		getEventSupport().firePropertyChange(name, oldValue, newValue);
	}
	
	public void setParameter(String name, boolean newValue) {
		String oldValue = parameters.get(name);
		parameters.put(name, newValue + "");
		getEventSupport().firePropertyChange(name, oldValue, newValue + "");
	}
	
	public void setParameter(String name, int newValue) {
		String oldValue = parameters.get(name);
		parameters.put(name, newValue + "");
		getEventSupport().firePropertyChange(name, oldValue, newValue + "");
	}
	
	public MungeStep duplicate(MatchMakerObject parent,
			MatchMakerSession session) {
		throw new UnsupportedOperationException("Duplicate is not supported yet");
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
     * Only sets the logger, because most steps do not need to allocate any resources.
     * If your step needs to allocate resources (perform a database query, open
     * a file, connect to a server, and so on), you should override this method.
     */
    public void open(Logger logger) throws Exception {
    	this.logger = logger;
    	
    	if (logger.isDebugEnabled()) {
    		logger.debug("Opening MungeStep " + getName());
    	}
    	
        opened = true;
    }

    /**
     * Does nothing, because most steps do not need to allocate any resources.
     * If you override the {@link #open()} method, you should override this method
     * too and clean up the resources.
     */
    public void close() throws Exception {
    	if (logger.isDebugEnabled()) {
    		logger.debug("Closing MungeStep " + getName());
    	}
    	logger = null;
        opened = false;
    }
    
    /**
     * Prints all the inputs iff the given logger has 
     * debugging enabled
     */
    protected void printInputs() {
    	if (logger.isDebugEnabled()) {
    		String out = getName() + " Inputs: ";
    		for (MungeStepOutput mso : getInputs()) {
    			if (mso == null) {
    				out += "[ null] ";
    			} else {
    				if (mso.getName() != null && mso.getName().length() != 0) {
    					out += "[ " + mso.getName() + ": " + mso.getData().toString() + " ] ";
    				} else {
    					out += "[ " + mso.getData().toString() + " ] ";
    				}
    			}
    		}
    		logger.debug(out);
    	}
    }
    
    /**
     * Prints all the outputs iff the given logger has 
     * debugging enabled
     */
    protected void printOutputs() {
    	if (logger.isDebugEnabled()) {
    		String out = getName() + " Outputs: ";
    		for (MungeStepOutput mso : getChildren()) {
    			if (mso == null) {
    				out += "[ null] ";
    			} else {
    				if (mso.getName() != null && mso.getName().length() != 0) {
    					out += "[ " + mso.getName() + ": " + mso.getData().toString() + " ] ";
    				} else {
    					out += "[ " + mso.getData().toString() + " ] ";
    				}
    			}
    		}
    		logger.debug(out);
    	}
    }

    
    
    /** 
     * Any implementation of this class that implements call() must call super.call()
     * as this validates that the step has been opened before the call. This will throw
     * an {@link IllegalStateException} if the munge step has not be opened.
     */
    public Boolean call() throws Exception {
    	if (!opened) {
    		throw new IllegalStateException("A munge step must be opened before called.");
    	}
    	printInputs();
    	return true;
    }

    /**
     * Returns the first MungeStepOutput it finds with the given name. 
     * Returns null if no such MungeStepOutput exists.
     */
    public MungeStepOutput getOutputByName(String name) {
    	for(MungeStepOutput o: getChildren()) {
    		if (o.getName().equals(name)) {
    			return o;
    		}
    	}
    	return null;
    }
    
    /**
     * Returns the matchMakerSession associated with this object
     */
    public MatchMakerSession getSession() {
    	return session;
    }
    
    @Override
    public String toString() {
    	StringBuilder result = new StringBuilder();
    	result.append(this.getName() + ": ");
    	result.append("[Inputs:");
    	for (MungeStepOutput mso : getInputs()) {
    		result.append(" " + mso);
    	}
    	result.append("] [Outputs:");
    	for (MungeStepOutput mso : getChildren()) {
    		result.append(" " + mso);
    	}
    	result.append("] [Parameters:");
    	for (String param : getParameterNames()) {
    		result.append(" <" + param + ":" + getParameter(param) + ">");
    	}
    	result.append("]");
    	return result.toString();
    }
}
