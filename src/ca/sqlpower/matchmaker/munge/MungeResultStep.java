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

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.SourceTableRecord;

/**
 * This MungeStep is used to store the final results of a MungeProcess.
 * As such, it contains no outputs, and only inputs. For each row
 * that gets processed in a MungeProcess, this step will store the
 * result.
 */
public class MungeResultStep extends AbstractMungeStep {

	/**
	 * A list of MungeResults that represent the munged data
	 * coming out of the Munging process. Any process after
	 * munging can extract this data for it's own use.
	 */
	private List<MungeResult> results = 
		new ArrayList<MungeResult>();
	
	/**
	 * The match project that this MungeResultStep is working on.
	 */
	private final Match match;
	
	/**
	 * A SQLIndex representing the unique index chosen by the user to
	 * represent the data in this table.
	 */
	private final SQLIndex uniqueIndex;
	
	/**
	 * The input MungeStep, from which this step will obtain the unique
	 * key value data for each row that was processed in munging
	 */
	private final MungeStep inputStep;
	
	/**
	 * The munge step outputs of the input step that contain the
	 * input table's key column values.  This array is set up by
	 * matching on index column names when the {@link #open()}
	 * method is called.
	 */
	private MungeStepOutput[] indexValues;
	
	public MungeResultStep(Match match, MungeStep inputStep) throws ArchitectException {
		this.match = match;
		this.inputStep = inputStep;
		this.uniqueIndex = match.getSourceTableIndex();
		setName("Munge Results");
		InputDescriptor desc = new InputDescriptor("result1", Object.class);
		super.addInput(desc);
	}

	@Override
	public void open() throws Exception {
		super.open();
		
		indexValues = new MungeStepOutput[uniqueIndex.getChildCount()];
		for (int i=0; i < uniqueIndex.getChildren().size(); i++) {
			SQLIndex.Column c = uniqueIndex.getChild(i);
			indexValues[i] = inputStep.getOutputByName(c.getName());
		}
	}
	
	@Override
	public Boolean call() throws Exception {
		super.call();
		
		List<MungeStepOutput> inputs = getInputs(); 
		
		MungeResult result = new MungeResult();
		result.setMungedData(inputs.toArray(new MungeStepOutput[inputs.size()]));
		
		List<Object> indexValueList = new ArrayList<Object>();
		
		for (MungeStepOutput o: indexValues) {
			indexValueList.add(o.getData());
		}
		
		SourceTableRecord source = new SourceTableRecord(getSession(), match, indexValueList);
		
		result.setSourceTableRecord(source);
		
		results.add(result);
		
		return Boolean.TRUE;
	}
	
	public boolean canAddInput() {
		return true;
	}
	
	public List<MungeResult> getResults() {
		return results;
	}
}
