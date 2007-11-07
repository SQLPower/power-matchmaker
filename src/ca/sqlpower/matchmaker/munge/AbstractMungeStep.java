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
import ca.sqlpower.matchmaker.Project;

/**
 * An abstract implementation of the MungeStep interface. The only
 * method that extending classes are required to implement would be
 * the {@link #call()} method, which would implement the functionality
 * of a particular MungeStep.
 */
public abstract class AbstractMungeStep extends AbstractMatchMakerObject<MungeStep, MungeStepOutput> implements MungeStep {
	
    /**
     * The object identifier for this munge step instance.  Required by
     * the persistence layer, but otherwise unused.
     */
    @SuppressWarnings("unused")
    private Long oid;
    
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
     * Tracks whether a open() call has been made on this munge step.
     */
    private boolean opened;

    /**
     * Ttacks whether a commit() call has been made on this munge step.
     */
    private boolean committed;

    /**
     * Tracks whether a rollback() call has been made on this munge step.
     */
    private boolean rolledBack;

	/**
	 * The logger to print the inputs and outputs to
	 */
	protected Logger logger;
	
	public AbstractMungeStep() {
	}
	
	public List<MungeStepOutput> getMSOInputs() {
		List<MungeStepOutput> values = new ArrayList<MungeStepOutput>();
		for (Input in: inputs) {
			values.add(in.current);
		}
		return values;
	}
	
	public void connectInput(int index, MungeStepOutput o) {
		if (index >= getMSOInputs().size()) {
			throw new IndexOutOfBoundsException("There is no input at the given index");
		}
		inputs.get(index).current = o;
		getEventSupport().firePropertyChange("inputs", index, null, o);
	}

	public void disconnectInput(int index) {
		if (index >= getMSOInputs().size()) {
			throw new IndexOutOfBoundsException("There is no input at the given index");
		}
		getEventSupport().firePropertyChange("inputs", index,
				inputs.get(index).current, null);
		inputs.get(index).current = null;
	}
	
	public int addInput(InputDescriptor desc) {
		Input in = new Input(null, desc, this);
		inputs.add(in);
		int index = inputs.size()-1;
		getEventSupport().firePropertyChange("addInputs", index, null, desc);
		return index;
	}

	public void removeInput(int index) {
		if (index >= inputs.size()) {
			throw new IndexOutOfBoundsException(
			"There is no IOConnector at the give index.");
		}
		getEventSupport().firePropertyChange("addInputs", index,
				inputs.get(index).descriptor, null);
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
		Class stepClass = getClass();
		AbstractMungeStep step = null;
		try {
			step = (AbstractMungeStep) stepClass.newInstance();
			step.parameters = new HashMap<String, String>(this.parameters);
			step.setParent(parent);
			step.setSession(session);
			step.setUndoing(this.isUndoing());
			step.setVisible(this.isVisible());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return step;
	}
	
	/**
	 * Warning: One should never mix instances--that is, one
	 * should never put detached instances from different 
	 * sessions into the same set because it will break this
	 * implementation of equals and hashCode.
	 */  
	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}
	
	/**
	 * Warning: One should never mix instances--that is, one
	 * should never put detached instances from different 
	 * sessions into the same set because it will break this
	 * implementation of equals and hashCode.
	 */  
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
	private static class Input{
		
		/**
		 * used by hibernate
		 */
		@SuppressWarnings("unused")
		private Long oid;
		
		private AbstractMungeStep parentStep;
		
		/**
		 * The MungeStepOutput containing the value of this input.
		 */
		private MungeStepOutput current;
		
		/**
		 * The attributes of this input.
		 */
		private InputDescriptor descriptor;
		
		/**
		 * only used by hibernate
		 */
		@SuppressWarnings("unused")
		private Input() {
			descriptor = new InputDescriptor(null, null);
		}
		
		Input(MungeStepOutput current, InputDescriptor descriptor, AbstractMungeStep step) {
			this.current = current;
			this.descriptor = descriptor;
			this.parentStep = step;
		}

		/**
		 * only used by hibernate
		 */
		@SuppressWarnings("unused")
		private AbstractMungeStep getParentStep() {
			return parentStep;
		}

		/**
		 * only used by hibernate
		 */
		@SuppressWarnings("unused")
		private void setParentStep(AbstractMungeStep parentStep) {
			this.parentStep = parentStep;
		}

		/**
		 * only used by hibernate
		 */
		@SuppressWarnings("unused")
		private MungeStepOutput getCurrent() {
			return current;
		}

		/**
		 * only used by hibernate
		 */
		@SuppressWarnings("unused")
		private void setCurrent(MungeStepOutput current) {
			this.current = current;
		}

		/**
		 * only used by hibernate
		 */
		@SuppressWarnings("unused")
		private String getName() {
			return descriptor.getName();
		}

		/**
		 * only used by hibernate
		 */
		@SuppressWarnings("unused")
		private Class getType() {
			return descriptor.getType();
		}

		/**
		 * only used by hibernate
		 */
		@SuppressWarnings("unused")
		private void setName(String name) {
			descriptor.setName(name);
		}

		/**
		 * only used by hibernate
		 */
		@SuppressWarnings("unused")
		private void setType(Class type) {
			descriptor.setType(type);
		}
	}

    /**
     * Only sets the logger, because most steps do not need to allocate any resources.
     * If your step needs to allocate resources (perform a database query, open
     * a file, connect to a server, and so on), you should override this method.
     */
    public void open(Logger logger) throws Exception {
    	this.logger = logger;
        if (logger == null) {
            throw new NullPointerException("Step " + getClass().getName() + " was given a null logger");
        }

    	if (logger.isDebugEnabled()) {
    		logger.debug("Opening MungeStep " + getName());
    	}
    	
        if (opened) {
            throw new IllegalStateException("Step is already opened");
        }
        
        opened = true;
        committed = false;
        rolledBack = false;
    }
    
    public void commit() throws Exception {
        if (!opened) {
            throw new IllegalStateException("Can't commit because step is not opened");
        }
        if (committed || rolledBack) {
            throw new IllegalStateException(
                    "Can't commit because step is already committed or rolled back" +
                    " (committed="+committed+"; rolledBack="+rolledBack+")");
        }
        committed = true;
    }
    
    public void rollback() throws Exception {
        if (!opened) {
            throw new IllegalStateException("Can't roll back because step is not opened");
        }
        if (committed || rolledBack) {
            throw new IllegalStateException(
                    "Can't roll back because step is already committed or rolled back" +
                    " (committed="+committed+"; rolledBack="+rolledBack+")");
        }
        rolledBack = true;
    }

    /**
     * Does nothing, because most steps do not need to allocate any resources.
     * If you override the {@link #open()} method, you should override this method
     * too and clean up the resources.
     */
    public void close() throws Exception {
        if (!opened) {
            throw new IllegalStateException("Step not opened");
        }
        if (! (committed || rolledBack)) {
            throw new IllegalStateException(
                    "Can't close until step has been committed or rolled back" +
                    " (committed="+committed+"; rolledBack="+rolledBack+")");
        }
        if (logger == null) {
            System.err.println("Warning: Step " + getClass().getName() + " lost its logger");
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Closing MungeStep " + getName());
            }
            logger = null;
        }
        opened = false;
    }
    
    /**
     * Prints all the inputs iff the given logger has 
     * debugging enabled
     */
    protected void printInputs() {
    	if (logger.isDebugEnabled()) {
    		String out = getName() + " Inputs: ";
    		for (MungeStepOutput mso : getMSOInputs()) {
    			if (mso == null) {
    				out += "[ null ] ";
    			} else {
    				if (mso.getName() != null && mso.getName().length() != 0) {
    					out += "[ " + mso.getName() + ": " + mso.getData() + " ] ";
    				} else {
    					out += "[ " + mso.getData() + " ] ";
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
    				out += "[ null ] ";
    			} else {
    				if (mso.getName() != null && mso.getName().length() != 0) {
    					out += "[ " + mso.getName() + ": " + mso.getData() + " ] ";
    				} else {
    					out += "[ " + mso.getData() + " ] ";
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
    		throw new IllegalStateException("A munge step must be opened before it is called.");
    	}
        if (rolledBack || committed) {
            throw new IllegalStateException(
                    "This step is already committed or rolled back." +
                    " It must be closed and reopened before calling it again." +
                    " (committed="+committed+"; rolledBack="+rolledBack+")");
        }
        if (logger == null) {
            throw new NullPointerException("Step " + getClass().getName() + " lost its logger");
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
     * Returns the MMO ancestor of this munge step that is a Project.
     * Returns null if there is no such ancestor.
     */
    public Project getProject() {
        for (MatchMakerObject<?, ?> mmo = getParent(); mmo != null; mmo = mmo.getParent()) {
            if (mmo instanceof Project) {
                return (Project) mmo;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
    	StringBuilder result = new StringBuilder();
    	result.append(this.getName()).append(":");
        result.append(" opened=").append(opened);
        result.append(" committed=").append(committed);
        result.append(" rolledBack=").append(rolledBack);
    	result.append(" [Inputs:");
    	for (MungeStepOutput mso : getMSOInputs()) {
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
    
    public boolean isInputStep() {
    	return false;
    }
    
    public boolean isOpen() {
        return opened;
    }
    
    public boolean isCommitted() {
        return committed;
    }
    
    public boolean isRolledBack() {
        return rolledBack;
    }
    
    /**
	 * returns the first child output
	 */
    protected MungeStepOutput getOut() {
		if (getChildCount() != 1) {
			throw new IllegalStateException(
					"The concat step has the incorrect number of outputs");
		}
		return getChildren().get(0);
    }
    
    /**
	 * only used by hibernate
	 */
	@SuppressWarnings("unused")
    private void setInputs(List<Input> inputs) {
    	this.inputs = inputs;
    }
	
	/**
	 * only used by hibernate
	 */
	@SuppressWarnings("unused")
    private List<Input> getInputs() {
    	return inputs;
    }
}
