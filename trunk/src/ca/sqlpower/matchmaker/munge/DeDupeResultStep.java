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

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.SourceTableRecord;

/**
 * This is a specific result step for a deduping project.
 */
public class DeDupeResultStep extends AbstractMungeStep implements MungeResultStep {

	private static final Logger logger = Logger.getLogger(DeDupeResultStep.class);
	
	/**
	 * A list of MungeResults that represent the munged data
	 * coming out of the Munging process. Any process after
	 * munging can extract this data for it's own use.
	 */
	private List<MungeResult> results = 
		new ArrayList<MungeResult>();
	
	/**
	 * The input MungeStep, from which this step will obtain the unique
	 * key value data for each row that was processed in munging.  This
     * value must be set up by client code before the call to {@link #open()}.
	 */
	private MungeStep inputStep;
	
	/**
	 * The munge step outputs of the input step that contain the
	 * input table's key column values.  This array is set up by
	 * matching on index column names when the {@link #open()}
	 * method is called.
	 */
	private MungeStepOutput[] indexValues;

	public DeDupeResultStep() {
		super();
		setName("Munge Results");
		InputDescriptor desc = new InputDescriptor("result1", Object.class);
		super.addInput(desc);
	}


    /**
     * Sets the input step associated with this result step.  This has to
     * be done before calling {@link #open(Logger)}.
     */
    public void setInputStep(MungeStep step) {
        this.inputStep = step;
    }

	/**
	 * This override of the open method initializes the array of MungeStepOutputs (MSO)
	 * which contains the MSOs containing the unique key values. This assumes
	 * that the input step contains outputs which correspond to the table's unique
	 * key columns. If they are missing, then call() will throw a NullPointerException.
     * Also, it is important that you have called {@link #setInputStep(MungeStep)} before
     * attempting to open this munge step.
	 */
    @Override
	public void open(Logger logger) throws Exception {
		super.open(logger);
        
        if (inputStep == null) {
            throw new IllegalStateException("Can't open when input step is null.");
        }
        
		// results must be emptied out, or otherwise, it will
		// contain the munge results from the last munge processor run.
		results.clear();

        Project project = getProject();
        SQLIndex uniqueIndex = project.getSourceTableIndex();

		indexValues = new MungeStepOutput[uniqueIndex.getChildCount()];
		for (int i=0; i < uniqueIndex.getChildren().size(); i++) {
			SQLIndex.Column c = uniqueIndex.getChild(i);
			logger.debug("Searching for MungeStepOuput with name " + c.getName());
			indexValues[i] = inputStep.getOutputByName(c.getName());
			logger.debug("Found MungeStepOuput " + indexValues[i]);
		}
	}
	
	@Override
	public Boolean call() throws Exception {
		super.call();
		
		List<MungeStepOutput> inputs = getInputs(); 
		Object[] mungedData = new Object[inputs.size()];
		
		for (int i = 0; i < inputs.size(); i++) {
			MungeStepOutput output = inputs.get(i);
			if (output != null) {
				mungedData[i] = output.getData();
			} else {
				mungedData[i] = null;
			}
		}
		
		MungeResult result = new MungeResult();
		result.setMungedData(mungedData);
		
		List<Object> indexValueList = new ArrayList<Object>();
		
		for (MungeStepOutput o: indexValues) {
			if (o == null) {
				throw new NullPointerException("Input step is missing unique key values!");
			}
			indexValueList.add(o.getData());
		}
		
		SourceTableRecord source = new SourceTableRecord(getSession(), getProject(), indexValueList);
		result.setSourceTableRecord(source);
		
		logger.debug("Adding MungeResult " + result);
		results.add(result);
		printOutputs();
		return Boolean.TRUE;
	}
	
	public boolean canAddInput() {
		return true;
	}
	
	public List<MungeResult> getResults() {
		return results;
	}

	public void addInputStep(SQLInputStep inputStep) {
		this.inputStep = inputStep;
		
	}
}
