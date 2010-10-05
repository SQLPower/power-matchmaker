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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerTestCase;
import ca.sqlpower.object.SPObject;

public class StringToBooleanMungeStepTest extends MatchMakerTestCase<StringToBooleanMungeStep> {

	public StringToBooleanMungeStepTest(String name) {
		super(name);
	}

	private StringToBooleanMungeStep step;
	
	private final Logger logger = Logger.getLogger("testLogger");
	
	protected void setUp() throws Exception {
		step = new StringToBooleanMungeStep();
		super.setUp();
		MungeProcess process = (MungeProcess) createNewValueMaker(
        		getRootObject(), null).makeNewValue(
        				MungeProcess.class, null, "parent process");
        process.addMungeStep(step, process.getMungeSteps().size());
	}
	
	private void setCaseSensitive(boolean cs) {
		step.setCaseSensitive(cs);
	}
	
	private void setRegex(boolean rx) {
		step.setUseRegex(rx);
	}
	
	private void setNeither(Boolean d) {
		step.setNeither(d);
	}
	
	private void setTrueList(String list) {
		step.setTrueList(list);
	}
	
	private void setFalseList(String list) {
		step.setFalseList(list);
	}
	
	private void run(List<String> in, List<Boolean> out) throws Exception {
		step.open(logger);
		for (int x = 0; x<in.size();x++) {
			MungeStepOutput<String> msoIn = new MungeStepOutput<String>("input",String.class);
			msoIn.setData(in.get(x));
			step.connectInput(0,msoIn);
			step.call();
			
			assertEquals("Error in item " + x + ": " + in.get(x) + ", ", out.get(x), step.getOut().getData());
		}
		step.mungeRollback();
		step.mungeClose();
	}

	public void testOneTrue() throws Exception {
		setCaseSensitive(false);
		setRegex(false);
		List<String> in = new ArrayList<String>();
		List<Boolean> out = new ArrayList<Boolean>();
		
		setTrueList("true");
		
		in.add("true");
		out.add(true);
		run(in,out);
	}
	
	public void testTrueAndFalse() throws Exception {
		setCaseSensitive(false);
		setRegex(false);
		setNeither(null);
		List<String> in = new ArrayList<String>();
		List<Boolean> out = new ArrayList<Boolean>();
		
		setTrueList("true");
		setFalseList("false");
		
		in.add("true");
		out.add(true);
		
		in.add("false");
		out.add(false);
		
		in.add("TRUe");
		out.add(true);
		
		in.add("falSE");
		out.add(false);
		
		in.add("YES");
		out.add(null);
		
		in.add("TRUEANDFALSE");
		out.add(null);
		
		run(in,out);
	}
	
	public void testList() throws Exception {
		setCaseSensitive(false);
		setRegex(false);
		setNeither(null);
		List<String> in = new ArrayList<String>();
		List<Boolean> out = new ArrayList<Boolean>();
		
		setTrueList("true,yes");
		setFalseList("false,no");
		
		in.add("true");
		out.add(true);
		
		in.add("false");
		out.add(false);
		
		in.add("TRUe");
		out.add(true);
		
		in.add("falSE");
		out.add(false);
		
		in.add("NO");
		out.add(false);
		
		in.add("YES");
		out.add(true);
		
		in.add("TRUEANDFALSE");
		out.add(null);
		
		run(in,out);
	}
	
	public void testRegex() throws Exception {
		setCaseSensitive(false);
		setRegex(true);
		setNeither(null);
		List<String> in = new ArrayList<String>();
		List<Boolean> out = new ArrayList<Boolean>();
		
		setTrueList("t.*");
		setFalseList("f.*");
		
		in.add("true");
		out.add(true);
		
		in.add("false");
		out.add(false);
		
		in.add("TRUe");
		out.add(true);
		
		in.add("falSE");
		out.add(false);
		
		in.add("ffffffffffffff");
		out.add(false);
		
		in.add("t");
		out.add(true);
		
		in.add("NO");
		out.add(null);
		
		in.add("YES");
		out.add(null);
		
		in.add("ITRUEANDFALSE");
		out.add(null);
		
		run(in,out);
	}
	
	public void testDefaults() throws Exception {
		setCaseSensitive(false);
		setRegex(true);
		setNeither(null);
		List<String> in = new ArrayList<String>();
		List<Boolean> out = new ArrayList<Boolean>();
		
		setTrueList("");
		setFalseList("");
		
		in.add("nothing");
		out.add(null);
		
		run(in,out);
		out.clear();
		
		setNeither(true);
		out.add(true);
		run(in,out);
		out.clear();
		
		setNeither(false);
		out.add(false);
		run(in,out);
		out.clear();
	}
	
	public void testEscComma() throws Exception {
		setCaseSensitive(false);
		setRegex(false);
		setNeither(null);
		List<String> in = new ArrayList<String>();
		List<Boolean> out = new ArrayList<Boolean>();
		
		setTrueList("true,\\,,T\\,");
		setFalseList("false");
		
		in.add("true");
		out.add(true);
		
		in.add("false");
		out.add(false);
		
		in.add(",");
		out.add(true);
		
		in.add("T,");
		out.add(true);
		
		run(in,out);
	}

	@Override
	protected StringToBooleanMungeStep getTarget() {
		logger.debug("Stub call: MatchMakerTestCase<StringToBooleanMungeStep>.getTarget()");
		return step;
	}

	@Override
	protected Class<? extends SPObject> getChildClassType() {
		return MungeStepOutput.class;
	}
	
	@Override
	public void testAllowedChildTypesField() throws Exception {
		// no-op
	}
}
