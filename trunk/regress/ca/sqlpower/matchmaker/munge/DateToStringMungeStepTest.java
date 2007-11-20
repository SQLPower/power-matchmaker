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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

public class DateToStringMungeStepTest extends TestCase {

	private DateToStringMungeStep step;
	
	private Date date;
	
	private final String[] EXPECTED_DATE = {"Tuesday, December 11, 2007", "December 11, 2007", "Dec 11, 2007", "12/11/07"};
	private final String[] EXPECTED_TIME = {"11:11:11 AM EST", "11:11:11 AM EST", "11:11:11 AM", "11:11 AM"};
	
	private final List<String> FORMATS = DateToStringMungeStep.FORMATS;
	private final List<String> STYLES = DateToStringMungeStep.STYLES;
	
	private MungeStepOutput testInput;
	
	private final Logger logger = Logger.getLogger("testLogger");
	
	protected void setUp() throws Exception {
		step = new DateToStringMungeStep();
		Calendar c = new GregorianCalendar();
		c.set(2007, 11, 11, 11, 11, 11);
		date = c.getTime();
	}

	public void testCallonDateFormat() throws Exception {
		String result = null;
		
		testInput = new MungeStepOutput<Date>("test", Date.class);
		testInput.setData(date);
		step.connectInput(0, testInput);
		step.setFormat(FORMATS.get(0));
		step.open(logger);
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);

		for (int i=0; i < STYLES.size(); i++) {
			step.setDateStyle(STYLES.get(i));
			step.call();
			result = (String)output.getData();
			assertEquals("Incorrect at " + STYLES.get(i) +
					" style for date only format.", EXPECTED_DATE[i], result);
		}
	}
	
	public void testCallonTimeFormat() throws Exception {
		String[] expected = {"11:11:11 AM EST", "11:11:11 AM EST", "11:11:11 AM", "11:11 AM"};
		String result = null;
		
		testInput = new MungeStepOutput<Date>("test", Date.class);
		testInput.setData(date);
		step.connectInput(0, testInput);
		step.setFormat(FORMATS.get(1));
		step.open(logger);
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);

		for (int i=0; i < STYLES.size(); i++) {
			step.setTimeStyle(STYLES.get(i));
			step.call();
			result = (String)output.getData();
			assertEquals("Incorrect at " + STYLES.get(i) +
					" style for time only format.", EXPECTED_TIME[i], result);
		}
	}
	
	public void testCallonDateTimeFormat() throws Exception {
		String result = null;
		
		testInput = new MungeStepOutput<Date>("test", Date.class);
		testInput.setData(date);
		step.connectInput(0, testInput);
		step.setFormat(FORMATS.get(2));
		step.open(logger);
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);

		for (int i=0; i < STYLES.size(); i++) {
			step.setDateStyle(STYLES.get(i));
			for (int j=0; j < STYLES.size(); j++) {
				step.setTimeStyle(STYLES.get(j));
				step.call();
				result = (String)output.getData();
				assertEquals("Incorrect at " + STYLES.get(i) + " date style, " +
						STYLES.get(j) + " time style for date and time format.",
						EXPECTED_DATE[i] + " " + EXPECTED_TIME[j], result);
			}
		}
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
