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

/**
 * MungeStepOutput instances represent an output connection point of a MungeStep
 * instance.  They are normally created by the MungeStep they belong to as part of
 * the step's initialization process.
 * <p>
 * The MungeStepOutput object belongs to a MungeStep (the one that determines the output
 * values at run time) and has no children (it's a leaf in the MatchMaker object
 * tree).
 *
 * @param <T> The type of data this output holds.
 */
public class MungeStepOutput<T> extends AbstractMatchMakerObject<MungeStepOutput, MatchMakerObject> {

	/**
	 * The type of data this step can hold.
	 * <p>
	 * This is a bound property. This object will fire a MatchMakerObject property
	 * change event when this property is updated.
	 */
	private final Class<T> type;
	
	/**
	 * The current data value of this step.  This will change with every call to the
	 * parent step at run time.
	 */
	private T data;
	
	/**
	 * Creates a new MungeStepOutput with the given initial name (can be changed
	 * later) and type (permanently fixed at the given value).
	 */
	public MungeStepOutput(String name, Class<T> type) {
		setName(name);
		this.type = type;
	}

	/**
	 * Returns the data type that this output holds.
	 */
	public Class<T> getType() {
		return type;
	}

	/**
	 * Returns the current data in this output.
	 */
	public T getData() {
		return data;
	}

	/**
	 * Sets the 
	 * @param data
	 */
	public void setData(T data) {
		this.data = data;
	}

	/**
	 * Overridden to declare the correct parent type.
	 */
	@Override
	public MungeStep getParent() {
		return (MungeStep) super.getParent();
	}
	
	/**
	 * Determines if this step output is equal to the other based on object identity
	 */
	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}

	/**
	 * Calculates hash code based on name and type.
	 */
	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}

	/**
	 * Not implemented because we're pretty sure we don't want a duplicate system like this.
	 */
	public MungeStepOutput<T> duplicate(MatchMakerObject parent, MatchMakerSession session) {
		throw new UnsupportedOperationException("Duplicate is not supported");
	}
	
	@Override
	public boolean allowsChildren() {
		return false;
	}
	
}
