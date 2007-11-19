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




/**
 * This munge step will return a substring that begins at the specified beginIndex
 * and extends to the character at index endIndex - 1.
 */
public class SubstringMungeStep extends AbstractMungeStep {

	/**
	 * This is the name of the parameter with the value of the beginIndex.
	 */
	public static final String BEGIN_PARAMETER_NAME = "beginIndex";

	/**
	 * This is the name of the parameter with the value of the endIndex.
	 */
	public static final String END_PARAMETER_NAME = "endIndex";
	
	public SubstringMungeStep() {
		super("Substring",false);
		setParameter(BEGIN_PARAMETER_NAME, 0);
		setParameter(END_PARAMETER_NAME, 0);
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
	public void removeInput(int index) {
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

		int beginIndex = getIntegerParameter(BEGIN_PARAMETER_NAME);
		int endIndex = getIntegerParameter(END_PARAMETER_NAME);
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
		
		printOutputs();
		return true;
	}
}
