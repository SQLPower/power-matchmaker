/*
 * Copyright (c) 2008, SQL Power Group Inc.
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
 * This munge step will trim white space from the start and end of strings. 
 */
public class TrimSpacesMungeStep extends AbstractMungeStep {

	public TrimSpacesMungeStep() {
		super("Trim Spaces",false);
		MungeStepOutput<String> out = new MungeStepOutput<String>("trimSpacesOutput", String.class);
		addChild(out);
		InputDescriptor desc = new InputDescriptor("trimSpaces", String.class);
		super.addInput(desc);
	}
	
	public Boolean doCall() throws Exception {
		MungeStepOutput<String> out = getOut();
		MungeStepOutput<String> in = getMSOInputs().get(0);
		String data = in.getData();
		if (in.getData() != null) {
			data = data.trim();
		}
		
		out.setData(data);
		return true;
	}
}



