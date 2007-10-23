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

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.SourceTableRecord;

/**
 * This MungeStep is used to store the final results of a MungeProcess.
 * As such, it contains no outputs, and only inputs. For each row
 * that gets processed in a MungeProcess, this step will store the
 * result. 
 * <p>
 * This step makes an important assumption that the input step that it takes in
 * contains MungeStepOutputs corresponding to the source table's unique key, and
 * that each MungeStepOutput's name is the same as the corresponding column's name.
 * If the MungeStepOutputs have different names, then this step will not be able
 * to find them, as it uses the source table's index key to find them.
 */
public class MungeResultStep extends AbstractMungeStep {

	private static final Logger logger = Logger.getLogger(MungeResultStep.class);
	
	/**
	 * A list of MungeResults that represent the munged data
	 * coming out of the Munging process. Any process after
	 * munging can extract this data for it's own use.
	 */
	private List<MungeResult> results = 
		new ArrayList<MungeResult>();
	
	/**
	 * The project that this MungeResultStep is working on.
	 */
	private final Project project;
	
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
	
	public MungeResultStep(Project project, MungeStep inputStep, MatchMakerSession session) throws ArchitectException {
		super(session);
		this.project = project;
		this.inputStep = inputStep;
		this.uniqueIndex = project.getSourceTableIndex();
		setName("Munge Results");
		InputDescriptor desc = new InputDescriptor("result1", Object.class);
		super.addInput(desc);
	}

	
	@Override
	/**
	 * This override of the open method initializes the array of MungeStepOutputs (MSO)
	 * which contains the MSOs containing the unique key values. This assumes
	 * that the input step contains outputs which correspond to the table's unique
	 * key columns. If they are missing, then call() will throw a NullPointerException
	 */
	public void open(Logger logger) throws Exception {
		super.open(logger);
		// results must be emptied out, or otherwise, it will
		// contain the munge results from the last munge processor run.
		results.clear();
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
			mungedData[i] = output.getData();
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
		
		SourceTableRecord source = new SourceTableRecord(getSession(), project, indexValueList);
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
}
