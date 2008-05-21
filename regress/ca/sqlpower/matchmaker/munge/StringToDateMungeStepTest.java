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

import java.sql.Time;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

/**
 * This tests the {@link StringToDateMungeStep} on hardcoded date strings.
 * It currently ignores the exact time in milliseconds and just compares 
 * on what's being tested.
 */
public class StringToDateMungeStepTest extends TestCase {
	MungeStep step;
	
	private final Calendar c = Calendar.getInstance();
	
	private Date date;
	private Date timeOnly;
	private Date dateOnly;
	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		step = new StringToDateMungeStep();
		step.setParameter(StringToDateMungeStep.IGNORE_ERROR_PARAM, false);
		step.open(Logger.getLogger(StringToDateMungeStepTest.class));
		
		c.set(2007, 1, 1, 13, 1, 1);
		date = c.getTime();
		timeOnly = new Time(date.getTime());
		dateOnly  = new java.sql.Date(date.getTime());
	}
	
	public void testDate() throws Exception {
		String[] dates = {"2/1/2007", "2/1/07", "02/01/07", "02/01/2007",
				"07/02/01", "2007/02/01", "01/Feb/07", "Thu/February/01/2007",
				"February/01/2007", "Thursday/01/February/2007", "01/February/2007"};
		
		MungeStepOutput<String> mso = new MungeStepOutput<String>("test", String.class);
		StringToDateMungeStep temp = (StringToDateMungeStep) step;
		temp.connectInput(0, mso);
		MungeStepOutput out = step.getChildren().get(0);
		
		temp.setTimeFormat("");
		
		for (int i = 0; i < dates.length; i++) {
			mso.setData(dates[i]);
			temp.setDateFormat(StringToDateMungeStep.DATE_FORMATS.get(i+1));
			temp.call();
			assertEquals("Incorrect output format!", out.getData().getClass(), java.sql.Date.class);
			assertEquals(dateOnly.toString(), out.getData().toString());
		}
	}
	
	public void testTime() throws Exception {
		String[] times = {"1:01:01 PM",	"01:01:01 PM", "13:01:01", "13:01:01"};
		
		MungeStepOutput<String> mso = new MungeStepOutput<String>("test", String.class);
		StringToDateMungeStep temp = (StringToDateMungeStep) step;
		temp.connectInput(0, mso);
		MungeStepOutput out = step.getChildren().get(0);
		
		temp.setDateFormat("");
		temp.setOutputFormat(StringToDateMungeStep.OUTPUT_FORMATS.get(1));
		
		for (int i = 0; i < times.length; i++) {
			mso.setData(times[i]);
			temp.setTimeFormat(StringToDateMungeStep.TIME_FORMATS.get(i+1));
			temp.call();
			assertEquals("Incorrect output format!", out.getData().getClass(), Time.class);
			assertEquals(timeOnly.toString(), out.getData().toString());
		}
	}
	
	public void testDateTime() throws Exception {
		Date [] dates = new Date[] {dateOnly, timeOnly, date};
		
		MungeStepOutput<String> mso = new MungeStepOutput<String>("test", String.class);
		StringToDateMungeStep temp = (StringToDateMungeStep) step;
		temp.connectInput(0, mso);
		MungeStepOutput out = step.getChildren().get(0);
		mso.setData("Thursday/01/February/2007 01:01:01 PM");

		temp.setDateFormat(StringToDateMungeStep.DATE_FORMATS.get(10));
		temp.setTimeFormat(StringToDateMungeStep.TIME_FORMATS.get(2));

		for (int i=0; i < dates.length; i++) {
			temp.setOutputFormat(StringToDateMungeStep.OUTPUT_FORMATS.get(i));
			temp.call();
			assertEquals("Incorrect output format!", out.getData().getClass(), dates[i].getClass());
			assertEquals(dates[i].toString(), out.getData().toString());
		}
		
	}
	
	public void testExceptionThrown() throws Exception{
		MungeStepOutput<String> mso = new MungeStepOutput<String>("test", String.class);
		mso.setData("One Billion Yen!!!!!");
		step.connectInput(0, mso);
		try {
			step.call();
			fail("ParseException not thrown as expected.");
		} catch (ParseException e) {
			// ParseException thrown as expected.
		}
	}
	
	public void testIgnoreError() throws Exception {
		step.setParameter(StringToDateMungeStep.IGNORE_ERROR_PARAM, true);		
		MungeStepOutput<String> mso = new MungeStepOutput<String>("test", String.class);
		mso.setData("One Billion Yen!!!!!");
		step.connectInput(0, mso);
		step.call();
		MungeStepOutput out = step.getChildren().get(0);
		assertEquals(null, (Date)out.getData());
	}
	
	
}
