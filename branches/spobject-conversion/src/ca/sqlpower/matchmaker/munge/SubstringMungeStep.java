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
 * This munge step will return a substring that begins at the specified beginIndex
 * and extends to the character at index endIndex - 1.
 */
public class SubstringMungeStep extends AbstractMungeStep {

	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(MungeStepOutput.class,MungeStepInput.class)));
	
	/**
	 * The begin index for the output substring of this munge step.
	 */
	private int begIndex;

	/**
	 * The end index for the output substring of this munge step.
	 */
	private int endIndex;
	
	@Constructor
	public SubstringMungeStep() {
		super("Substring",false);
		setBegIndex(0);
		setEndIndex(0);
		MungeStepOutput<String> out = new MungeStepOutput<String>("substringOutput", String.class);
		addChild(out);
		InputDescriptor desc = new InputDescriptor("substring", String.class);
		super.addInput(desc);
	}
	
	@Override
	public int addInput(InputDescriptor desc) {
		throw new UnsupportedOperationException("Substring munge step does not support addInput()");
	}
	
	@Override
	public boolean removeInput(int index) {
		throw new UnsupportedOperationException("Substring munge step does not support removeInput()");
	}
	
	public void connectInput(int index, MungeStepOutput o) {
		if (o.getType() != getInputDescriptor(index).getType()) {
			throw new UnexpectedDataTypeException(
				"Substring munge step does not accept non-String inputs");
		} else {
			super.connectInput(index, o);
		}
	}
	
	/**
	 * This call() throws an {@link IndexOutOfBoundsException} if the given
	 * indices were not in the range of the input
	 */
	public Boolean doCall() throws Exception {

		int beginIndex = getBegIndex();
		int endIndex = getEndIndex();
		MungeStepOutput<String> out = getOut();
		MungeStepOutput<String> in = getMSOInputs().get(0);
		String data = in.getData();
		if (data != null) {
			if (beginIndex < 0) {
				throw new IndexOutOfBoundsException(
					"The begin index can not be less than 0.");
			}
			if (beginIndex >= data.length()) {
				out.setData("");
			} else {
				if (endIndex > data.length()) {
					endIndex = data.length();
				}
				out.setData(data.substring(beginIndex, endIndex));
			}
		} else {
			out.setData(null);
		}
		
		return true;
	}

	@Mutator
	public void setBegIndex(int begIndex) {
			int old = this.begIndex;
			this.begIndex = begIndex;
			firePropertyChange("begIndex", old, begIndex);
	}

	@Accessor
	public int getBegIndex() {
		return begIndex;
	}

	@Mutator
	public void setEndIndex(int endIndex) {
			int old = this.endIndex;
			this.endIndex = endIndex;
			firePropertyChange("endIndex", old, endIndex);
	}

	@Accessor
	public int getEndIndex() {
		return endIndex;
	}
	
	@Override
	protected void copyPropertiesForDuplicate(MungeStep copy) {
		SubstringMungeStep step = (SubstringMungeStep) copy;
		step.setBegIndex(getBegIndex());
		step.setEndIndex(getEndIndex());
	}
}
