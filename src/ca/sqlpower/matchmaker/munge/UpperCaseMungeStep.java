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

import java.util.List;


/**
 * This munge step will convert all alphabetical string inputs
 * to upper case.
 */
public class UpperCaseMungeStep extends AbstractMungeStep {

	private MungeStepOutput<String> out;

	public UpperCaseMungeStep() {
		out = new MungeStepOutput<String>("upperCaseOutput", String.class);
		addChild(out);
	}
	
	@Override
	public void addInput(MungeStepOutput o) {
		if (o.getType() != String.class) {
			throw new UnexpectedDataTypeException(
					"Upper case munge step does not accept non-String inputs");
		}
		super.addInput(o);
	}

	public List<MungeStepOutput> call() throws Exception {
		for (MungeStepOutput<String> in: getInputs()) {
			String data = in.getData();
			if (in.getData() != null) {
				data = data.toUpperCase();
			}
			out.setData(data);
		}
		return getChildren();
	}
}
