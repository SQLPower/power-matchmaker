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



/**
 * This munge step will taken in a boolean and return a user defined string. This
 * has two string options that represent the string to set it to if its true and 
 * the string to set it to if its false.
 */
public class BooleanToStringMungeStep extends AbstractMungeStep {
	
	/**
	 * The string to return if the boolean taken in is true.
	 */
	public static final String TRUE_STRING_PARAMETER_NAME = "true string";
	
	/**
	 * The string to return if the boolean taken in is false.
	 */
	public static final String FALSE_STRING_PARAMETER_NAME = "false string";
	
	
	public BooleanToStringMungeStep() {
		super("Boolean to String",false);
		MungeStepOutput<String> out = new MungeStepOutput<String>("stringOutput", String.class);
		addChild(out);
		InputDescriptor desc = new InputDescriptor("booleanInput", Boolean.class);
		super.addInput(desc);
		setParameter(TRUE_STRING_PARAMETER_NAME, "True");
		setParameter(FALSE_STRING_PARAMETER_NAME, "False");
	}
	

	public Boolean doCall() throws Exception {
		Boolean in = (Boolean) getMSOInputs().get(0).getData();
		if (in == null) {
			getOut().setData(null);
		} else if (in.booleanValue()) {
			getOut().setData(getParameter(TRUE_STRING_PARAMETER_NAME));
		} else {
			getOut().setData(getParameter(FALSE_STRING_PARAMETER_NAME));
		}
		return true;
	}

}