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

import org.apache.commons.codec.language.Metaphone;

/**
 * This munge step will output the metaphone code of the given input.
 */
public class MetaphoneMungeStep extends AbstractMungeStep {

	private MungeStepOutput<String> out;
	
	public MetaphoneMungeStep() {
		setName("Metaphone");
		out = new MungeStepOutput<String>("metaphoneOutput", String.class);
		addChild(out);
		InputDescriptor desc = new InputDescriptor("metaphone", String.class);
		super.addInput(desc);
	}
	
	@Override
	public int addInput(InputDescriptor desc) {
		throw new UnsupportedOperationException("Metaphone munge step does not support addInput()");
	}
	
	@Override
	public void removeInput(int index) {
		throw new UnsupportedOperationException("Metaphone soundex substitution munge step does not support removeInput()");
	}
	
	public void connectInput(int index, MungeStepOutput o) {
		if (o.getType() != getInputDescriptor(index).getType()) {
			throw new UnexpectedDataTypeException(
				"Metaphone munge step does not accept non-String inputs");
		} else {
			super.connectInput(index, o);
		}
	}
	
	public Boolean call() throws Exception {
		super.call();

		MungeStepOutput<String> in = getInputs().get(0);
		String data = in.getData();
		if (data != null) {
			out.setData(new Metaphone().metaphone(data));
		} else {
			out.setData(null);
		}
		return true;
	}

	public boolean canAddInput() {
		return false;
	}
}
