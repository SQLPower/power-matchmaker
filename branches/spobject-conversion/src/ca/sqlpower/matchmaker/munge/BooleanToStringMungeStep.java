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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.Mutator;



/**
 * This munge step will taken in a boolean and return a user defined string. This
 * has two string options that represent the string to set it to if its true and 
 * the string to set it to if its false.
 */
public class BooleanToStringMungeStep extends AbstractMungeStep {
	
	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(MungeStepOutput.class,MungeStepInput.class)));
	
	/**
	 * The string to return if the boolean taken in is true.
	 */
	private String trueString;
	
	/**
	 * The string to return if the boolean taken in is false.
	 */
	private String falseString;
	
	@Constructor
	public BooleanToStringMungeStep() {
		super("Boolean to String",false);
		MungeStepOutput<String> out = new MungeStepOutput<String>("stringOutput", String.class);
		addChild(out);
		InputDescriptor desc = new InputDescriptor("booleanInput", Boolean.class);
		super.addInput(desc);
		setTrueString("True");
		setFalseString("False");
	}
	

	public Boolean doCall() throws Exception {
		Boolean in = (Boolean) getMSOInputs().get(0).getData();
		MungeStepOutput<String> out = getOut();
		if (in == null) {
			out.setData(null);
		} else if (in.booleanValue()) {
			out.setData(getTrueString());
		} else {
			out.setData(getFalseString());
		}
		return true;
	}

	@Mutator
	public void setTrueString(String trueString) {
		String oldTrueString = this.trueString;
		this.trueString = trueString;
		firePropertyChange("trueString", oldTrueString, trueString);
	}

	@Accessor
	public String getTrueString() {
		return trueString;
	}

	@Mutator
	public void setFalseString(String falseString) {
		String oldFalseString = this.falseString;
		this.falseString = falseString;
		firePropertyChange("falseString", oldFalseString, falseString);
	}

	@Accessor
	public String getFalseString() {
		return falseString;
	}

}