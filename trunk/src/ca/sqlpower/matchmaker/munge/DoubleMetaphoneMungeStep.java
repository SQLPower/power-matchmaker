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

import org.apache.commons.codec.language.DoubleMetaphone;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.Mutator;

/**
 * This munge step will output the double metaphone code of the given input. This
 * supports using the alternate encoding as an option. 
 */
public class DoubleMetaphoneMungeStep extends AbstractMungeStep {

	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(MungeStepOutput.class,MungeStepInput.class)));
	
	/**
	 * This is the name of the parameter that decides whether this step will use
	 * the alternate encoding. It is defaulted to false;
	 */
	private boolean useAlternate = false;
	
	@Constructor
	public DoubleMetaphoneMungeStep() {
		super("Double Metaphone",false);
		MungeStepOutput<String> out = new MungeStepOutput<String>("doubleMetaphoneOutput", String.class);
		addChild(out);
		InputDescriptor desc = new InputDescriptor("doubleMetaphone", String.class);
		super.addInput(desc);
	}
	
	@Override
	public int addInput(InputDescriptor desc) {
		throw new UnsupportedOperationException("Double metaphone munge step does not support addInput()");
	}
	
	@Override
	public boolean removeInput(int index) {
		throw new UnsupportedOperationException("Double metaphoen substitution munge step does not support removeInput()");
	}
	
	public void connectInput(int index, MungeStepOutput o) {
		if (o.getType() != getInputDescriptor(index).getType()) {
			throw new UnexpectedDataTypeException(
				"Double metaphone munge step does not accept non-String inputs");
		} else {
			super.connectInput(index, o);
		}
	}
	
	public Boolean doCall() throws Exception {		
		MungeStepOutput<String> out = getOut();
		MungeStepOutput<String> in = getMSOInputs().get(0);
		String data = in.getData();
		if (data != null) {
			out.setData(new DoubleMetaphone().doubleMetaphone(data, isUseAlternate()));
		} else {
			out.setData(null);
		}
		return true;
	}

	@Mutator
	public void setUseAlternate(boolean useAlternate) {
		boolean oldAltVal = this.useAlternate;
		this.useAlternate = useAlternate;
		firePropertyChange("useAlternate", oldAltVal, useAlternate);
	}

	@Accessor
	public boolean isUseAlternate() {
		return useAlternate;
	}
	
	@Override
	protected void copyPropertiesForDuplicate(MungeStep copy) {
		((DoubleMetaphoneMungeStep) copy).setUseAlternate(isUseAlternate());
	}
}
