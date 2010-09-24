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

import java.math.BigDecimal;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
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
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sqlobject.SQLType;
import ca.sqlpower.validation.ValidateResult;

/**
 * An abstract implementation of the MungeStep interface. The only
 * method that extending classes are required to implement would be
 * the {@link #call()} method, which would implement the functionality
 * of a particular MungeStep.
 */
public abstract class AbstractMungeStep extends AbstractMatchMakerObject implements MungeStep {
	
	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(MungeStepOutput.class,Input.class)));
	
	protected final List<MungeStepOutput> mungeStepOutputs = new ArrayList<MungeStepOutput>();
	
	private final List<Input> inputs = new ArrayList<Input>();
	
    /**
     * The object identifier for this munge step instance.  Required by
     * the persistence layer, but otherwise unused.
     */
    @SuppressWarnings("unused")
    private Long oid;
    
	
	/**
	 * A map of configuration parameters for this MungeStep.
	 */
	private Map<String,String> parameters = new HashMap<String, String>();
	
    /**
     * Tracks whether a open() call has been made on this munge step.
     */
    private boolean opened;

    /**
     * Tracks whether a commit() call has been made on this munge step.
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
	 * Subclasses that override this method should keep in mind that this method
	 * could be called before the step has its parameters properly configured.
	 */
	public void doOpen(EngineMode mode, Logger log) throws Exception {}

	/**
	 * Refreshes the munge step. This will do different operations depending on
	 * the munge step including refreshing the outputs and inputs on the step.
	 * Default is a no-op. Subclasses that override this method should keep in
	 * mind that this method could be called before the step has its parameters
	 * properly configured.
	 */
    public void refresh(Logger logger) throws Exception {
    	//do nothing on default cases.
    }
	
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
		
        inputs.get(index).setCurrent(o);
        
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
		inputs.get(index).disconnect();
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
			if (in.getCurrent() == mso) {
				in.disconnect();
				disconnectCount++;
			}
		}
		return disconnectCount;
	}
	
	@Override
	protected void addChildImpl(SPObject ob, int index) {
		mungeStepOutputs.add((MungeStepOutput) ob);
		fireChildAdded(SQLInputStep.class, ob, mungeStepOutputs.size());
	}
	
	public List<SPObject> getChildren() {
		List<SPObject> children = new ArrayList<SPObject>();
		children.addAll(mungeStepOutputs);
		children.addAll(inputs);
		return Collections.unmodifiableList(children);
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
		fireChildAdded(Input.class, in, index);
	}

	public boolean removeInput(int index) {
		if (index >= inputs.size()) {
			throw new IndexOutOfBoundsException(
			"There is no IOConnector at the given index.");
		}
		Input in = inputs.get(index);
		boolean b = inputs.remove(in);
		fireChildRemoved(Input.class, in, index);
		return b;
	}
	
	public boolean removeInput(Input in) {
		if (!inputs.contains(in)) {
			throw new IndexOutOfBoundsException(
				"The given connector is not an input in the list");
		}
		int dex = inputs.indexOf(in);
		boolean b = inputs.remove(in);
		fireChildRemoved(Input.class,in,dex);
		return b;
	}
	
	public void removeUnusedInput() {
		try {
			begin("Removing input");
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
			commit();
		} catch (RuntimeException e) {
			rollback(e.getMessage());
			throw e;
		}
	}
	
	protected boolean removeChildImpl(SPObject spo) {
		boolean removed;
		if(spo instanceof MungeStepOutput) {
			removed = removeMungeStepOutput((MungeStepOutput)spo);
		} else {
			removed = removeInput((Input)spo);
		}
		return removed;
	}
	
	private boolean removeMungeStepOutput(MungeStepOutput mso) {
		int index = mungeStepOutputs.indexOf(mso);
		boolean removed = mungeStepOutputs.remove(mso);
		fireChildRemoved(MungeStepOutput.class, mso, index);
		return removed;
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
			begin("Setting position");
			setParameter(MUNGECOMPONENT_X, x);
			setParameter(MUNGECOMPONENT_Y, y);
			commit();
		} catch (RuntimeException e) {
			rollback(e.getMessage());
			throw e;
		}
	}
	
	public void setParameter(String name, String newValue) {
		String oldValue = parameters.get(name);
		parameters.put(name, newValue);
		firePropertyChange(name, oldValue, newValue);
	}
	
	public void setParameter(String name, boolean newValue) {
		String oldValue = parameters.get(name);
		parameters.put(name, newValue + "");
		firePropertyChange(name, oldValue, newValue + "");
	}
	
	public void setParameter(String name, int newValue) {
		String oldValue = parameters.get(name);
		parameters.put(name, newValue + "");
		firePropertyChange(name, oldValue, newValue + "");
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
	public static class Input extends AbstractMatchMakerObject {
		
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
		
		public void disconnect() {
			MungeStepOutput old = current;
			current = null;
			firePropertyChange("current",old,null);
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

        public MungeStepOutput getCurrent() {
			return current;
		}

        public void setCurrent(MungeStepOutput current) {
        	MungeStepOutput former = this.current;
			this.current = current;
			firePropertyChange("current", former, current);
		}

        public String getName() {
			return descriptor.getName();
		}

		/**
         * Returns the data type expected by this input.
         */
        public Class<?> getType() {
			return descriptor.getType();
		}

		/**
		 * only used by hibernate
		 */
		@SuppressWarnings("unused")
		private void setType(Class type) {
			descriptor.setType(type);
		}

		@Override
		public MatchMakerObject duplicate(MatchMakerObject parent,
				MatchMakerSession session) {
			return null;
		}

		@Override
		public List<Class<? extends SPObject>> getAllowedChildTypes() {
			return Collections.emptyList();
		}

		@Override
		public List<? extends SPObject> getChildren() {
			return Collections.emptyList();
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
     * a file, connect to a server, and so on), you should override this method. This does
     * not relate to the main persistence engine, and is only used in writing files
     * as part of a mungestep.
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
     * Called when the step tries to commit, in cases where the MungeStep writes to
     * a file. This is unrelated to the larger commit. This calls doCommit(), which should
     * be overridden if the step is to do anything when commit is called.
     */
    public final void mungeCommit() throws Exception {
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
    public final void mungeRollback() throws Exception {
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
     * Called when this step is closed. This calls doClose(). 
     * doClose should be overridden if the step needs to do anything on close.
     */
    public final void mungeClose() throws Exception {
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
    		for (MungeStepOutput mso : getChildren(MungeStepOutput.class)) {
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
    	for(MungeStepOutput o: getChildren(MungeStepOutput.class)) {
    		if (o.getName().equals(name)) {
    			return o;
    		}
    	}
    	return null;
    }

    /**
     * Returns the mungeStepOutput children;
     */
    public List<MungeStepOutput> getMungeStepOutput() {
    	return Collections.unmodifiableList(mungeStepOutputs);
    }

    /**
     * Returns the mungeStepIntput children (the actual class name is AbstractMungeStep.Input)
     */
    public List<AbstractMungeStep.Input> getMungeStepInputs() {
    	return Collections.unmodifiableList(inputs);
    }

    /**
     * Returns the MMO ancestor of this munge step that is a Project.
     * Returns null if there is no such ancestor.
     * XXX There is a utility method that you can use instead of this. (getAncestor(class)).
     */
    public Project getProject() {
        for (MatchMakerObject mmo = getParent(); mmo != null; mmo = (MatchMakerObject) mmo.getParent()) {
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
    	for (MungeStepOutput mso : getChildren(MungeStepOutput.class)) {
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
		if (mungeStepOutputs.size() != 1) {
			throw new IllegalStateException(
					"This step has the incorrect number of outputs");
		}
		return getChildren(MungeStepOutput.class).get(0);
    }
    
    /**
	 * Probably only needed by xml.
	 */
    public void setInputs(List<Input> inputs) {
    	this.inputs.clear();
    	this.inputs.addAll(inputs);
    }
    
    /**
	 * Only needed by XML DAO.
	 */
    public void setChildren(List<MungeStepOutput> xy) {
    	try {
    		begin("Updating entire children list");
	        for (MungeStepOutput spo : getChildren(MungeStepOutput.class)) {
	        	try {
					removeChild(spo);
				} catch (RuntimeException e) {
					e.printStackTrace();
				} catch (ObjectDependentException e) {
					e.printStackTrace();
				}
	        }
	        for (MungeStepOutput mso : xy) {
	        	addChild(mso);
	        }
    	} catch (RuntimeException e) {
    		rollback(e.getMessage());
    		throw e;
    	}
    	
    }
	
	/**
     * Probably only needed by xml.
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
    
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
    	return allowedChildTypes;
    }

}
