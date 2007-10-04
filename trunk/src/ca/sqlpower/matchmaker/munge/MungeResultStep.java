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

import java.util.ArrayList;
import java.util.List;

/**
 * This MungeStep is used to store the final results of a MungeProcess.
 * As such, it contains no outputs, and only inputs. For each row
 * that gets processed in a MungeProcess, this step will store the
 * result.
 */
public class MungeResultStep extends AbstractMungeStep {

	private List<MungeResult> results = 
		new ArrayList<MungeResult>();
	
	public MungeResultStep() {
		setName("Munge Results");
		InputDescriptor desc = new InputDescriptor("result1", Object.class);
		super.addInput(desc);
	}

	@Override
	public Boolean call() throws Exception {
		super.call();
		
		List<MungeStepOutput> inputs = getInputs(); 
		
		MungeResult result = new MungeResult();
		result.setMungedData(inputs.toArray(new MungeStepOutput[inputs.size()]));
		// Need to set the primary key
		// result.setPrimaryKey(??);
		
		return Boolean.TRUE;
	}
	
	public boolean canAddInput() {
		return true;
	}
	
	public List<MungeResult> getResults() {
		return results;
	}
}
