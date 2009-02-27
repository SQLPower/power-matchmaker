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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerObject;

/**
 * Defines a special type of MatchMakerObject which is capable of being part of a
 * data manipulation process.  In order to produce potential matches in the match
 * pool which identify pairs of records which might represent the same information,
 * the user can specify a set of operations to perform on each row of data before
 * searching for identical rows.  These manipulations will typically make the data
 * less specific, or attempt to conform the data to some standard.
 * <p>
 * For instance, a munge step might convert a string to uppercase, so that the
 * matching becomes case insensitive.  Or it might strip all spaces from the value
 * so the match considers all words starting with the same three-letter sequence
 * as equivalent.  In terms of conforming data, a munge step might format a North
 * American telephone number or Canadian postal code.
 */
public interface MungeStep extends MatchMakerObject<MungeStep, MungeStepOutput>, Callable<Boolean> {

	public static final String MUNGECOMPONENT_X = "x";
	public static final String MUNGECOMPONENT_Y = "y";
	public static final String MUNGECOMPONENT_EXPANDED ="expanded";
	
	/**
     * Returns the String representation of the parameter value associated with
     * the given name.
     * 
     * @param name
     *            The parameter to retrieve
     * @return The value associated with the given parameter, or null if no such
     *         parameter exists.
     */
	String getParameter(String name);
	
	/**
	 * Sets a configuration parameter for this munge step.  Which parameter names
	 * are meaningful is dependent on the actual implementation class of the step.
	 * 
	 * @param name The parameter name, which has a defined meaning to the current
	 * step implementation.
	 * @param value The value to associate with the named parameter.
	 */
	void setParameter(String name, String newValue);

    /**
     * Sets a boolean-valued configuration parameter for this munge step. Which
     * parameter names are meaningful is dependent on the actual implementation
     * class of the step.
     * 
     * @param name
     *            The parameter name, which has a defined meaning to the current
     *            step implementation.
     * @param value
     *            The value to associate with the named parameter.
     */
	void setParameter(String name, boolean newValue);
	
    /**
     * Sets an integer-valued configuration parameter for this munge step. Which
     * parameter names are meaningful is dependent on the actual implementation
     * class of the step.
     * 
     * @param name
     *            The parameter name, which has a defined meaning to the current
     *            step implementation.
     * @param value
     *            The value to associate with the named parameter.
     */
	void setParameter(String name, int newValue);

	/**
	 * sets the position of the munge step
	 */
	void setPosition(int x, int y);
	
    /**
     * Enumerates the list of all parameter names currently in place in this munge step.
     */
    Collection<String> getParameterNames();
    
	/**
	 * Adds a IOConnectors with the given InputDescriptor.
	 * Any class that extends AbstractMungeStep that is expecting 
	 * particular data types in its input will specify the expectation in their
	 * InputDescriptor.
	 * <p>
	 * Additionally, this method fires a property change event using the
	 * {@ MatchMakerEventSupport#firePropertyChange(String, Object, Object)
	 * , with property name of "inputs" and old and new values.
	 * <p>
	 * Note that this method may throw {@link UnsupportedOperationException)
	 * if the munge step does not allow adding new IOConnectors.
	 */
    int addInput(InputDescriptor desc);
    
    /**
	 * Adds a IOConnectors with the given InputDescriptor at the 
	 * specified index. For more information, see 
	 * {@link MungeStep#addInput(InputDescriptor)}
	 */
	void addInput(InputDescriptor desc, int index);

	/**
	 * Removes the IOConnectors at given index from this step.  This method is normally
	 * only useful at munging algorithm design time, not at run time when the
	 * data is being processed.
	 * 
	 * @param index
	 *            The index of the IOConnect to remove. The method call will
	 *            throw an {@link IndexOutOfBoundsException} if the given index does
	 *            not exist.
	 * @return true if the given IOConnector was removed from the step; false if it
	 *         wasn't.
	 */
	void removeInput(int index);
	
	/**
	 * Removes the inputs that are not used.
	 */
	void removeUnusedInput();

	/**
	 * Connects the input at index to the given MungeStepOutput which would be
	 * an output of another MungeStep. This method will throw 
	 * {@link UnexpectedDataTypeException} if the give output is not of correct
	 * type or {@link IndexOutOfBoundsException} if given index does not exist.
	 */
	void connectInput(int index, MungeStepOutput<?> o);

	/**
	 * Disconnects the input at the given index by removing the 
	 * MungeStepOutput at that index
	 */
	void disconnectInput(int index);
	
	/**
	 * Disconnects the input at the given index by removing the 
	 * MungeStepOutput from every input of this step it was connected to.
	 * 
	 * @return the number of inputs the given output was disconnected from.
	 */
	int disconnectInput(MungeStepOutput mso);
	
	/**
	 * Returns the list of input sources for this step. These items are actually
	 * outputs that belong to other steps.
	 * 
	 * @return A non-modifiable list of the current inputs to this step.
	 */
	List<MungeStepOutput> getMSOInputs();
	
	/**
	 * Causes this munge step to evaluate its current input values and produce
	 * the corresponding output values, which are then stored in this step's
	 * outputs.
     * <p>
     * You have to open a MungeStep before invoking this method on it.  Open
     * a step by calling {@link #open()}.
	 * 
	 * @return A Boolean object with value set to true if the munging process
	 * should continue after this step. Otherwise, return false. 
	 */
	Boolean call() throws Exception;
	
	/**
	 *  Returns an InputDescriptor containing the expected attributes for inputs
	 *  for the given input number.
	 *  
	 *  @Return The InputDescriptor for the input, or NULL if the given number is out of bounds
	 */
	InputDescriptor getInputDescriptor(int inputNumber);
	
	/**
	 * Returns the number of inputs on this step.
	 */
	int getInputCount();
	
	/**
	 * This returns true if this munge step allows for adding new inputs; false if otherwise.
	 */	
	boolean canAddInput();
    
    /**
     * Allocates any resources this step requires while processing its data.
     * Once this method has been called on a MungeStep, it is required that
     * the {@link #close()} method is also called in the future.
     * <p>
     * Opening this step clears its previous committed and rolled back
     * state from its previous (open, call, commit|rollback, close) sequence.
     */
    void open(Logger logger) throws Exception;
    
    /**
     * Closes any resources allocated by the {@link open()} method.  For users of
     * this step, it is mandatory to call this method after the {@link #open()}
     * method has been called.
     */
    void close() throws Exception;
    
    /**
     * Returns the first MungeStepOutput it finds with the given name. 
     * Returns null if no such MungeStepOutput exists.
     */
    public MungeStepOutput getOutputByName(String name);
    
    /**
     * Returns true if this MungeStep is an input step, false otherwise. 
     */
    public boolean isInputStep();

    /**
     * Causes this step to undo any changes it has effected since it was opened.
     * For a step that modifies a database resource, this will be a database
     * rollback operation. Many steps do not have permanent side-effects, and in
     * that case this method is a no-op.
     * <p>
     * Lifecycle note: The processor will call this method if there was a fatal
     * error during the process execution, or if the user requests the process
     * to be aborted. Even if this method is called, the processor will still
     * call {@link close()} at a later time.
     */
    void rollback() throws Exception;

    /**
     * Causes this step to commit (make permanent) any changes it has effected
     * since it was opened. For a step that modifies a database resource, this
     * will be a database commit operation. Many steps do not have permanent
     * side-effects, and in that case this method is a no-op.
     * <p>
     * Lifecycle note: The processor will call this method after all steps have
     * completed normally. After this method is called, the processor will still
     * call {@link close()} at a later time.
     */
    void commit() throws Exception;

    /**
     * Returns true if rollback() has been called on this step since it was last
     * opened.
     */
    boolean isRolledBack();
    
    /**
     * Returns true if commit() has been called on this step since it was last
     * opened.
     */
    boolean isCommitted();
    
    /**
     * Returns true if this step is currently open, meaning the open() method has
     * been called at least once, and close() has not been called since the most
     * recent call to open().
     */
    boolean isOpen();

	/**
	 * Intended to check if at least one of the Inputs in this MungeStep have
	 * a MungeStepOutput connected to it.
	 * 
	 * @return True if at least one Input has a MungeStepOutput associated with
	 *         it. False if otherwise.
	 */
    boolean hasConnectedInputs();
}
