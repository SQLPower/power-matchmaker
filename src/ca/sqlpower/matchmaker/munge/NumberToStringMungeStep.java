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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Constructor;


/**
 * This munge step will convert a number to a string.
 */
public class NumberToStringMungeStep extends AbstractMungeStep {
	
	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(MungeStepOutput.class,MungeStepInput.class)));

	@Constructor
	public NumberToStringMungeStep() {
		super("Number to String",false);
		MungeStepOutput<String> out = new MungeStepOutput<String>("NumberToStringOutput", String.class);
		addChild(out);
		InputDescriptor desc = new InputDescriptor("Number", BigDecimal.class);
		super.addInput(desc);
	}
	
	public Boolean doCall() throws Exception {
		MungeStepOutput<String> out = getOut();
		MungeStepOutput<BigDecimal> in = getMSOInputs().get(0);
		BigDecimal data = in.getData();
		String ret = null;
		if (in.getData() != null) {
			ret = data.toPlainString();
		}
		
		out.setData(ret);
		return true;
	}
}



