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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

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

	
	/**
	 * Returns the parameter value associated with the given name.
	 * 
	 * @param name The parameter to retrieve
	 * @return The value associated with the given parameter, or null if no such
	 * parameter exists.
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
	 * Connects the input at index to the given MungeStepOutput which would be
	 * an output of another MungeStep. This method will throw 
	 * {@link UnexpectedDataTypeException} if the give output is not of correct
	 * type or {@link IndexOutOfBoundsException} if given index does not exist.
	 */
	void connectInput(int index, MungeStepOutput o);

	/**
	 * Disconnects the input at the given index by removing the 
	 * MungeStepOutput at that index
	 */
	void disconnectInput(int index);
	
	/**
	 * Returns the list of input sources for this step. These items are actually
	 * outputs that belong to other steps.
	 * 
	 * @return A non-modifiable list of the current inputs to this step.
	 */
	List<MungeStepOutput> getInputs();
	
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
	 * This returns true if this munge step allows for adding new inputs; false if otherwise.
	 */	
	boolean canAddInput();
    
    /**
     * Allocates any resources this step requires while processing its data.
     * Once this method has been called on a MungeStep, it is required that
     * the {@link #close()} method is also called in the future.
     */
    void open() throws Exception;
    
    /**
     * Closes any resources allocated by the {@link open()} method.  It is mandatory
     * to call this method after the {@link #open()} method has been called.
     */
    void close() throws Exception;
    
    /**
     * Returns the first MungeStepOutput it finds with the given name. 
     * Returns null if no such MungeStepOutput exists.
     */
    public MungeStepOutput getOutputByName(String name);
}
