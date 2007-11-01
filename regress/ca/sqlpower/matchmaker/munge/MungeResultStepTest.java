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

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.SourceTableRecord;

public class MungeResultStepTest extends TestCase {

	private MungeResultStep step;
	
	private final Logger logger = Logger.getLogger("testLogger");
	
	protected void setUp() throws Exception {
		super.setUp();
		MungeStep inputStep = new TestingMungeStep("input", 0, 3);
		
		Project project = new Project();
		SQLIndex index = new SQLIndex();
		
		SQLColumn col1 = new SQLColumn();
		col1.setName("output_0");
		index.addIndexColumn(col1, false, false);
		inputStep.getOutputByName("output_0").setData("output_0");

		SQLColumn col2 = new SQLColumn();
		col2.setName("output_1");
		index.addIndexColumn(col2, false, false);
		inputStep.getOutputByName("output_1").setData("output_1");
		
		SQLColumn col3 = new SQLColumn();
		col3.setName("output_2");
		index.addIndexColumn(col3, false, false);
		inputStep.getOutputByName("output_2").setData("output_2");
		
		project.setSourceTableIndex(index);
		
		
		step = new MungeResultStep();
        step.setInputStep(inputStep);
		MungeStepOutput<String> output = new MungeStepOutput<String>("munged", String.class);
		output.setData("cow");
		step.connectInput(0, output);
	}

	public void test() throws Exception {
		step.open(logger);
		step.call();

		List<MungeResult> results = step.getResults();
		assertEquals(1, results.size());
		
		MungeResult result = results.get(0);
		SourceTableRecord source = result.getSourceTableRecord();
		List<Object> keyValues = source.getKeyValues();
		
		for (int i=0; i<keyValues.size(); i++) {
			Object key = keyValues.get(i);
			assertEquals(String.class, key.getClass());
			String keyString = (String) key;
			assertEquals("output_" + i, keyString);
		}
		
		Object[] mungedData = result.getMungedData();
		assertEquals(1, mungedData.length);
		String output = (String)mungedData[0];
		assertEquals("cow", output);
	}
}
