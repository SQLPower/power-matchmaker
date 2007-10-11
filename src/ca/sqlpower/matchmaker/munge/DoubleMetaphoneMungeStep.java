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

import org.apache.commons.codec.language.DoubleMetaphone;

import ca.sqlpower.matchmaker.MatchMakerSession;

/**
 * This munge step will output the double metaphone code of the given input. This
 * supports using the alternate encoding as an option. 
 */
public class DoubleMetaphoneMungeStep extends AbstractMungeStep {

	private MungeStepOutput<String> out;
	
	/**
	 * This is the name of the parameter that decides whether this step will use
	 * the alternate encoding. The only values accepted by the parameter
	 * are "true" and "false". It is defaulted as false.
	 */
	public static final String USE_ALTERNATE_PARAMETER_NAME = "useAlternate";
	
	public DoubleMetaphoneMungeStep(MatchMakerSession session) {
		super(session);
		setName("Double Metaphone");
		out = new MungeStepOutput<String>("doubleMetaphoneOutput", String.class);
		addChild(out);
		InputDescriptor desc = new InputDescriptor("doubleMetaphone", String.class);
		setParameter(USE_ALTERNATE_PARAMETER_NAME, false);
		super.addInput(desc);
	}
	
	@Override
	public int addInput(InputDescriptor desc) {
		throw new UnsupportedOperationException("Double metaphone munge step does not support addInput()");
	}
	
	@Override
	public void removeInput(int index) {
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
	
	public Boolean call() throws Exception {
		super.call();

		boolean useAlternate = getBooleanParameter(USE_ALTERNATE_PARAMETER_NAME);
		MungeStepOutput<String> in = getInputs().get(0);
		String data = in.getData();
		if (data != null) {
			out.setData(new DoubleMetaphone().doubleMetaphone(data, useAlternate));
		} else {
			out.setData(null);
		}
		return true;
	}

	public boolean canAddInput() {
		return false;
	}
}
