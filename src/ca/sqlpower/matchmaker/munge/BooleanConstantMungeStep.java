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
 * This step returns a constant boolean or null.
 */
public class BooleanConstantMungeStep extends AbstractMungeStep {
	
	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(MungeStepOutput.class,MungeStepInput.class)));

	/**
	 * The constant passed to the output of this step.
	 */
	private Boolean constant;
	
	@Constructor
	public BooleanConstantMungeStep() {
		super("Boolean Constant", false);
		setConstant(true);
		addChild(new MungeStepOutput<Boolean>("boolean out",Boolean.class));
	}
	
	
	public Boolean doCall() throws Exception {
		MungeStepOutput<Boolean> out = getOut();
		if (getConstant() == null) {
			out.setData(null);
		} else if (getConstant()) {
			out.setData(true);
		} else {
			out.setData(false);
		}
		return true;
	}

	@Mutator
	public void setConstant(Boolean constant) {
		Boolean oldConstant = this.constant;
		this.constant = constant;
		firePropertyChange("constant", oldConstant, constant);
	}

	@Accessor
	public Boolean getConstant() {
		return constant;
	}
	
	@Override
	protected void copyPropertiesForDuplicate(MungeStep copy) {
		((BooleanConstantMungeStep) copy).setConstant(getConstant());
	}
}
