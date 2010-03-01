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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

public class DateToStringMungeStepTest extends TestCase {

	private DateToStringMungeStep step;
	
	private Date date;
	
	private MungeStepOutput testInput;
	
	private final Logger logger = Logger.getLogger("testLogger");
	
	protected void setUp() throws Exception {
		step = new DateToStringMungeStep();
		Calendar c = Calendar.getInstance();
		c.set(2007, 1, 1, 13, 1, 1);
		date = c.getTime();
	}

	public void testCallonDateOnly() throws Exception {
		String[] exDate = {"", "2/1/2007", "2/1/07", "02/01/07", "02/01/2007",
				"07/02/01", "2007/02/01", "01/Feb/07", "Thu/February/01/2007",
				"February/01/2007", "Thursday/01/February/2007", "01/February/2007"};
		int i = 0;
		testInput = new MungeStepOutput<Date>("test", Date.class);
		testInput.setData(date);
		step.connectInput(0, testInput);
		step.open(logger);
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		for (String format : DateToStringMungeStep.DATE_FORMATS) {
			step.setFormat(format);
			step.call();
			assertEquals(exDate[i++], output.getData());
		}
	}
	
	public void testCallonTimeOnly() throws Exception {
		String[] exTime = {"", "1:01:01 PM",
				"01:01:01 PM", "13:01:01", "13:01:01"};
		int i = 0;
		testInput = new MungeStepOutput<Date>("test", Date.class);
		testInput.setData(date);
		step.connectInput(0, testInput);
		step.open(logger);
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		for (String format : DateToStringMungeStep.TIME_FORMATS) {
			step.setFormat(format);
			step.call();
			assertEquals(exTime[i++], output.getData());
		}
	}
	
	public void testCallonDateTime() throws Exception {
		testInput = new MungeStepOutput<Date>("test", Date.class);
		testInput.setData(date);
		step.connectInput(0, testInput);
		step.open(logger);
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		step.call();
		assertEquals("2/1/2007 1:01:01 PM", output.getData());
		
		step.setFormat(DateToStringMungeStep.DATE_FORMATS.get(10) + " " + DateToStringMungeStep.TIME_FORMATS.get(2));
		step.call();
		assertEquals("Thursday/01/February/2007 01:01:01 PM", output.getData());
	}
	
	public void testCallonNull() throws Exception {
		testInput = new MungeStepOutput<Date>("test", Date.class);
		testInput.setData(null);
		step.connectInput(0, testInput);
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals(null, result);
	}
	
	public void testCallonInteger() throws Exception {
		testInput = new MungeStepOutput<Integer>("test", Integer.class);
		testInput.setData(new Integer(1));
		try {
			step.connectInput(0, testInput);
			fail("UnexpectedDataTypeException was not thrown as expected");
		} catch (UnexpectedDataTypeException ex) {
			// UnexpectedDataTypeException was thrown as expected
		}
	}
}
