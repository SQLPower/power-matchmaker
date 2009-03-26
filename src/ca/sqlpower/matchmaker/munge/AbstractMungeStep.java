/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

import java.math.BigDecimal;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.AbstractMatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.MatchMakerEngine.EngineMode;
import ca.sqlpower.sqlobject.SQLType;
import ca.sqlpower.validation.ValidateResult;

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
	
	/**
	 * Stores if the munge step can have inputs added to it "on the fly"
	 */
	private boolean canAddInputs;
	
	/**
	 * Tracks if this step is in preview mode.
	 */
	private boolean previewMode;
	
	/**
	 * The default object type of this Munge Step's input. The default value is {@link Object#class}.
	 * This is used for Munge Steps with variable inputs as the default class to use when adding an new input
	 * which now happens when connecting an input into the last empty input. 
	 * When extending the AbstractMungeStep, if your expected input object type isn't Object (for example, 
	 * {@link ConcatMungeStep} expects String), then use the {@link #setDefaultInputClass(Class)} to set your
	 * expected input type.   
	 */
	private Class defaultInputClass = Object.class;
	
	protected EngineMode mode;
	
	public AbstractMungeStep(String name, boolean canAddInputs) {
		setName(name);
		this.canAddInputs = canAddInputs;
	}
	
	@Override
	public MungeProcess getParent() {
	    return (MungeProcess) super.getParent();
	}
	
	//The set of methods that can be to be overwritten by the subclasses. 
	/**
	 * A method that is called when a step is opened. Default is No-op. 
	 */
	public void doOpen(EngineMode mode, Logger log) throws Exception {}
	
	/**
	 * A method that is called when an step is "run/called". Default is No-op. 
	 */
	public Boolean doCall() throws Exception{return Boolean.TRUE;}
	/**
	 * A method that is called when a step is attempting to rollback. Default is No-op. 
	 */
	public void doRollback() throws Exception {}
	
	/**
	 * A method that is called when a step is trying to commit. Default is No-op. 
	 */
	public void doCommit() throws Exception{}
	
	/**
	 * A method that is called when a step is closed. Default is No-op. 
	 */
	public void doClose() throws Exception{}
	
	public List<MungeStepOutput> getMSOInputs() {
		List<MungeStepOutput> values = new ArrayList<MungeStepOutput>();
		for (Input in: inputs) {
			values.add(in.current);
		}
		return values;
	}
	
	public void connectInput(int index, MungeStepOutput<?> o) {
		if (index >= getMSOInputs().size()) {
			throw new IndexOutOfBoundsException(
                    "There is no input at index " + index +
                    " (inputs.size = " + getMSOInputs().size() + ")");
		}
        if (!inputs.get(index).getType().isAssignableFrom(o.getType())) {
            throw new UnexpectedDataTypeException(
                    "Input " + index + " of step " + getName() +
                    " does not support data type " + o.getType());
        }
		inputs.get(index).current = o;
		getEventSupport().firePropertyChange("inputs", index, null, o);
		boolean noEmptyInputs = true;
		for (Input input: inputs) {
			if (input.getCurrent() == null) noEmptyInputs = false;
		}
		if (canAddInput() && noEmptyInputs) {
			addInput(new InputDescriptor("", defaultInputClass));
		}
	}

	public void disconnectInput(int index) {
		if (index >= getMSOInputs().size()) {
			throw new IndexOutOfBoundsException("There is no input at the given index");
		}
		getEventSupport().firePropertyChange("inputs", index,
				inputs.get(index).current, null);
		inputs.get(index).current = null;
	}
	
	/**
	 * Disconnects the input at the given index by removing the 
	 * MungeStepOutput, note that this may remove more than
	 * one input
	 */
	public int disconnectInput(MungeStepOutput mso) {
	    int disconnectCount = 0;
		for (int i = 0; i < inputs.size(); i++) {
			Input in = inputs.get(i);
			if (in.current == mso) {
				getEventSupport().firePropertyChange("inputs", i,
					in.current, null);
				in.current = null;
				disconnectCount++;
			}
		}
		return disconnectCount;
	}
	
	public int addInput(InputDescriptor desc) {
		int index = inputs.size();
		addInput(desc, index);
		return index;
	}
	
	public void addInput(InputDescriptor desc, int index) {
		if (index > inputs.size()) {
			throw new IndexOutOfBoundsException(
					"Cannot add at position: " + index);
		}
		Input in = new Input(null, desc, this);
		inputs.add(index, in);
		getEventSupport().firePropertyChange("addInputs", index, null, desc);
	}

	public void removeInput(int index) {
		InputDescriptor old = inputs.get(index).descriptor;
		if (index >= inputs.size()) {
			throw new IndexOutOfBoundsException(
				"There is no IOConnector at the give index.");
		}
		inputs.remove(index);
		getEventSupport().firePropertyChange("addInputs", index,
				old, null);
	}
	
	public void removeUnusedInput() {
		try {
			startCompoundEdit();
			Queue<Integer> freeIndexQueue = new LinkedList<Integer>();
	
			for (int i = 0; i < inputs.size(); i++) {
				if (inputs.get(i).current == null) {
					freeIndexQueue.offer(i);
				} else {
					if (freeIndexQueue.size() > 0) {
						//swap inputs
						MungeStepOutput temp = inputs.get(i).getCurrent();
						int index = freeIndexQueue.remove();
						disconnectInput(i);
						connectInput(index, temp);
						freeIndexQueue.add(i);
					}
				}
			}
			while (inputs.get(inputs.size() - 1).getCurrent() == null && inputs.size() > 1) {
				removeInput(inputs.size() - 1);
			}
		} finally {
			endCompoundEdit();
		}
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
	
	public void setPosition(int x, int y) {
		try {
			startCompoundEdit();
			setParameter(MUNGECOMPONENT_X, x);
			setParameter(MUNGECOMPONENT_Y, y);
		} finally {
			endCompoundEdit();
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
	
	public int getInputCount() {
	    return inputs.size();
	}
	
	/**
	 * This is the pairing between a MungeStepOutput value and its InputDescriptor.
	 * The reason for this class is to avoid maintaining two separate collections of
	 * MungeStepOutputs and their InputDescriptors.
	 * <p>
	 * Note this class is only public because the XML DAO needs access to it. For
	 * normal use of the MatchMaker API, there is no need to refer to this class
	 * directly.
	 */
	public static class Input{
		
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
		
		public Input(MungeStepOutput current, InputDescriptor descriptor, AbstractMungeStep step) {
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
        public MungeStepOutput getCurrent() {
			return current;
		}

		/**
		 * only used by hibernate
		 */
		@SuppressWarnings("unused")
        public void setCurrent(MungeStepOutput current) {
			this.current = current;
		}

		/**
		 * only used by hibernate
		 */
		@SuppressWarnings("unused")
        public String getName() {
			return descriptor.getName();
		}

		/**
         * Returns the data type expected by this input.
         * <p>
         * Note: This method is used reflectively by Hibernate. Do not remove
         * this method even if it appears unused.
         */
		@SuppressWarnings("unused")
        public Class<?> getType() {
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
	 * returns if the method can have inputs added
	 */
	public final boolean canAddInput() {
		return canAddInputs;
	}

	public final void open(Logger logger) throws Exception {
		open(null, logger);
	}
	
	/**
     * Only sets the logger, because most steps do not need to allocate any resources.
     * If your step needs to allocate resources (perform a database query, open
     * a file, connect to a server, and so on), you should override this method.
     */
    public final void open(EngineMode mode, Logger logger) throws Exception {
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
        
        doOpen(mode, logger);

        opened = true;
        committed = false;
        rolledBack = false;
    }
    
    /**
     * Called when the step tries to commit. This called doCommit(). doCommit should
     * be overridden if the step is to do anything when commit is called.
     */
    public final void commit() throws Exception {
        if (!opened) {
            throw new IllegalStateException("Can't commit because step is not opened");
        }
        if (committed || rolledBack) {
            throw new IllegalStateException(
                    "Can't commit because step is already committed or rolled back" +
                    " (committed="+committed+"; rolledBack="+rolledBack+")");
        }
        doCommit();
        committed = true;
    }
    
    /**
     * Called when the step tries to rollback. This called doRollback(). doRollback() should
     * be overridden if the step is to do anything when commit is called.
     */
    public final void rollback() throws Exception {
        if (!opened) {
            throw new IllegalStateException("Can't roll back because step is not opened");
        }
        if (committed || rolledBack) {
            throw new IllegalStateException(
                    "Can't roll back because step is already committed or rolled back" +
                    " (committed="+committed+"; rolledBack="+rolledBack+")");
        }
        doRollback();
        rolledBack = true;
    }

    /**
     * Called when this step is closed. This calls doClose(). doClose should be overridden if 
     * the step needs to do anything on close.
     */
    public final void close() throws Exception {
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
        doClose();
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
     * The method called to start the step. This is a final method and cannot be
     * over written. This step calls doCall(), any subclass should override doCall 
     * if the step is to do anything. 
     */
    public final Boolean call() throws Exception {
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
    	Boolean ret =  doCall();
    	printOutputs();
    	return ret;
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
					"This step has the incorrect number of outputs");
		}
		return getChildren().get(0);
    }
    
    /**
	 * Only used by hibernate and xml import/export.
	 */
    public void setInputs(List<Input> inputs) {
    	this.inputs = inputs;
    }
	
	/**
     * Only used by hibernate and xml import/export.
	 */
    public List<Input> getInputs() {
    	return inputs;
    }

	/**
	 * Sets the expected input object type for new Inputs. Typically used by classes that extend AbstractMungeStep
	 * and expect a default Input class that is not Object. It is currently default access for now, since all
	 * Munge Steps are placed in the ca.sqlpower.matchmaker.munge package anyway. 
	 */
	void setDefaultInputClass(Class defaultInputClass) {
		this.defaultInputClass = defaultInputClass;
	}

	public boolean isPreviewMode() {
		return previewMode;
	}

	public void setPreviewMode(boolean previewMode) {
		this.previewMode = previewMode;
	}
	
	public boolean hasConnectedInputs() {
		boolean result = false;
		for (Input in: inputs) {
			result |= (in.current != null);
			if (result) break;
		}
		return result;
	}
	
	/**
     * Returns the Java class associated with the given SQL type code.
     * 
     * @param type
     *            The type ID number. See {@link SQLType} for the official list.
     * @return The class for the given type. Defaults to java.lang.String if the
     *         type code is unknown, since almost every SQL type can be
     *         represented as a string if necessary.
     */
    protected Class<?> typeClass(int type) {
        switch (type) {
        case Types.VARCHAR:
        case Types.VARBINARY:
        case Types.STRUCT:
        case Types.REF:
        case Types.OTHER:
        case Types.NULL:
        case Types.LONGVARCHAR:
        case Types.LONGVARBINARY:
        case Types.JAVA_OBJECT:
        case Types.DISTINCT:
        case Types.DATALINK:
        case Types.CLOB:
        case Types.CHAR:
        case Types.BLOB:
        case Types.BINARY:
        case Types.ARRAY:
        default:
            return String.class;

        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.REAL:
        case Types.NUMERIC:
        case Types.INTEGER:
        case Types.FLOAT:
        case Types.DOUBLE:
        case Types.DECIMAL:
        case Types.BIGINT:
            return BigDecimal.class;

        case Types.BIT:
        case Types.BOOLEAN:
            return Boolean.class;
        
        case Types.TIMESTAMP:
        case Types.TIME:
        case Types.DATE:
            return Date.class;
        }
    }
    
    public List<ValidateResult> checkPreconditions() {
    	return Collections.emptyList();
    }
}
