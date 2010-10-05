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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.AbstractMatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonProperty;
import ca.sqlpower.object.annotation.Transient;

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
public class MungeStepOutput<T> extends AbstractMatchMakerObject 
								implements Comparable<MungeStepOutput<T>> {
	
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.emptyList();

	private static final Logger logger = Logger.getLogger(MungeStepOutput.class);
	
	/**
     * The object identifier for this munge step instance.  Required by
     * the persistence layer, but otherwise unused.
     */
    @SuppressWarnings("unused")
    private Long oid;
    
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
	@Constructor
	public MungeStepOutput(@ConstructorParameter(propertyName="Name") String name,
						@ConstructorParameter(propertyName="type") Class<T> type) {
		setName(name);
		this.type = type;
	}

	/**
	 * Returns the data type that this output holds.
	 */
	@Accessor
	public Class<T> getType() {
		return type;
	}
	
	//XXX does this need to be persisted?

	/**
	 * Returns the current data in this output.
	 */
	@Transient
	@Accessor
	public T getData() {
		return data;
	}

	/**
	 * Sets the 
	 * @param data
	 */
	@Transient
	@Mutator
	public void setData(T data) {
		T oldData = data;
		this.data = data;
		firePropertyChange("data", oldData, data);
	}

	/**
	 * Overridden to declare the correct parent type.
	 */
	@Override
	public MungeStep getParent() {
		return (MungeStep) super.getParent();
	}
	
	/**
	 * Not implemented because we're pretty sure we don't want a duplicate system like this.
	 */
	public MungeStepOutput<T> duplicate(MatchMakerObject parent) {
		throw new UnsupportedOperationException("Duplicate is not supported");
	}
	
	@Override
	public boolean allowsChildren() {
		return false;
	}

	public int compareTo(MungeStepOutput<T> o) {
		if (!type.equals(o.getType())) {
			throw new IllegalStateException("Cannot compare two MungeStepOutputs " +
					"that have different data types: " + type + " and " + o.getType());
		} 
		
		int compareValue = 0;
		
		Object otherData = o.getData();
		
		if (data == null || otherData == null) {
			if (data != null) {
				logger.debug("data was null");
				compareValue = 1;
			} else if (otherData != null) {
				logger.debug("otherData was null");
				compareValue = -1;
			}
		} else if (type.equals(String.class)){
			logger.debug("comparing Strings " + data + " and " + otherData);
			compareValue = ((String)data).compareTo((String)otherData);
		} else if (type.equals(BigDecimal.class)) {
			logger.debug("comparing BigDecimals " + data + " and " + otherData);
			compareValue = ((BigDecimal) data).compareTo((BigDecimal)otherData);
		} else if (type.equals(Boolean.class)) {
			logger.debug("comparing Booleans " + data + " and " + otherData);
			compareValue = ((Boolean) data).compareTo((Boolean)otherData);
		} else if (type.equals(Date.class)) {
			logger.debug("comparing Dates " + data + " and " + otherData);
			compareValue = ((Date) data).compareTo((Date)otherData);
		} else {
			throw new IllegalStateException("MungeStepOutput" +
					"contain an unsupported data type:" + type);
		}
		logger.debug("compareValue is " + compareValue);
		return compareValue;
	}
	
	@Override
	public String toString() {
		return "<" + getName() + ": " + getData() + ">";
	}

	@Override
	@NonProperty
	public List<Class<? extends SPObject>> getAllowedChildTypes() {
		return allowedChildTypes;
	}

	@Override
	@NonProperty
	public List<? extends SPObject> getChildren() {
		return Collections.emptyList();
	}
}
