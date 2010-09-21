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
 * This munge step will convert a alphabetical string input
 * to lower case.
 */
public class LowerCaseMungeStep extends AbstractMungeStep {

	public LowerCaseMungeStep() {
		super("Lower Case",false);
		MungeStepOutput<String> out = new MungeStepOutput<String>("lowerCaseOutput", String.class);
		addChild(out);
		InputDescriptor desc = new InputDescriptor("lowerCase", String.class);
		super.addInput(desc);
	}
	
	@Override
	public int addInput(InputDescriptor desc) {
		throw new UnsupportedOperationException(
			"Lower case munge step does not support addInput()");
	}
	
	@Override
	public boolean removeInput(int index) {
		throw new UnsupportedOperationException(
			"Lower case munge step does not support removeInput()");
	}
	
	public Boolean doCall() throws Exception {
		
		MungeStepOutput<String> out = getOut();
		MungeStepOutput<String> in = getMSOInputs().get(0);
		String data = in.getData();
		if (in.getData() != null) {
			data = data.toLowerCase();
		}
		out.setData(data);
		return true;
	}
}
