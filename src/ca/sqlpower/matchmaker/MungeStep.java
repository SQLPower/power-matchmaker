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

import java.util.List;
import java.util.concurrent.Callable;

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
public interface MungeStep extends MatchMakerObject<MungeStep, MungeStepOutput>, Callable<List<MungeStepOutput>> {

	/**
	 * A special return value from getMAxInputs if there is no max.
	 */
	public static final int UNLIMITED_INPUTS = -1;
	
	/**
	 * Returns the parent to this step, which is a MatchRuleSet object.
	 */
	MatchRuleSet getParent();
	
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
	void setParameter(String name, String value);

	/**
	 * Adds the given input (which is an output from another step) to this step.
	 * This method is normally only useful at munging algorithm design time, not
	 * at run time when the data is being processed.
	 * 
	 * @param o
	 *            The output of another step which should be the input to this
	 *            one.
	 */
	void addInput(MungeStepOutput o);

	/**
	 * Removes the given input source from this step.  This method is normally
	 * only useful at munging algorithm design time, not at run time when the
	 * data is being processed.
	 * 
	 * @param o
	 *            The input source to remove. If this object was not already an
	 *            input to this step, the method call has no effect.
	 * @return true if the given step was removed from the list; false if it
	 *         wasn't (because o was not an input to this step).
	 */
	boolean removeInput(MungeStepOutput o);
	
	/**
	 * Returns the list of input sources for this step. These items are actually
	 * outputs that belong to other steps.
	 * 
	 * @return A non-modifiable list of the current inputs to this step.
	 */
	List<MungeStepOutput> getInputs();

	/**
	 * Returns the list of outputs. Outputs are usually fixed for the life of a
	 * step, although the name of the output may change over time to reflect its
	 * current expected value.
	 * 
	 * @return A non-modifiable list of this step's outputs.
	 */
	List<MungeStepOutput> getOutputs();
	
	/**
	 * Causes this munge step to evaluate its current input values and produce
	 * the corresponding output values, which are then stored in this step's
	 * outputs.
	 * 
	 * @return The outputs of this step, which now contain the newly calculated
	 *         output values.
	 */
	List<MungeStepOutput> call() throws Exception;

	/**
	 * Gets the maximum number of inputs that the step can receive.
	 * 
	 * @return Maximum number of inputs or UNLIMITED_INPUTS if there is no max
	 */
	int getMaxInputs();
}
