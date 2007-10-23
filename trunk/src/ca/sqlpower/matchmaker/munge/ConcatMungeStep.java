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

import ca.sqlpower.matchmaker.MatchMakerSession;

/**
 * This munge step will concat all alphabetical string inputs.
 */
public class ConcatMungeStep extends AbstractMungeStep {

	private MungeStepOutput<String> out;
	
	public ConcatMungeStep(MatchMakerSession session) {
		super(session);
		setName("Concat");
		out = new  MungeStepOutput<String>("concatOutput", String.class);
		addChild(out);
		InputDescriptor desc1 = new InputDescriptor("concat1", String.class);
		InputDescriptor desc2 = new InputDescriptor("concat2", String.class);
		super.addInput(desc1);
		super.addInput(desc2);
	}
	
	public int addInput(InputDescriptor desc) {
		return super.addInput(desc);
	}
	
	public void connectInput(int index, MungeStepOutput o) {
		if (o != null && o.getType() != getInputDescriptor(index).getType()) {
			throw new UnexpectedDataTypeException(
				"Concatenate munge step does not accept non-String inputs");
		} else {
			super.connectInput(index, o);
		}
	}
	
	public Boolean call() throws Exception {
		super.call();

		boolean allNulls = true;
		StringBuilder data = new StringBuilder();
		for (MungeStepOutput<String> in: getInputs()) {
			if (in != null && in.getData() != null) {
				data.append(in.getData());
				allNulls = false;
			}
		}
		out.setData(data.toString());
		if (allNulls) {
			out.setData(null);
		}
		printOutputs();
		return true;
	}

	public boolean canAddInput() {
		return true;
	}
}
