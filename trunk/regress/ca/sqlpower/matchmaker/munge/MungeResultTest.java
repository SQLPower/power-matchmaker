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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.SourceTableRecord;
import ca.sqlpower.matchmaker.swingui.StubMatchMakerSession;

public class MungeResultTest extends TestCase {

	private MungeResult r1;
	private MungeResult r2;
	private MungeStepOutput<String> outString1;
	private MungeStepOutput<String> outString2;
	private MungeStepOutput<BigDecimal> outDecimal1;
	private MungeStepOutput<BigDecimal> outDecimal2;
	private MungeStepOutput<Boolean> outBoolean1;
	private MungeStepOutput<Boolean> outBoolean2;
	private MungeStepOutput<Date> outDate1;
	private MungeStepOutput<Date> outDate2;
	private MungeStepOutput[] mungedData2;
	private MungeStepOutput[] mungedData1;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		MatchMakerSession session = new StubMatchMakerSession();
		r1 = new MungeResult();
		List<Object> keyValues = new ArrayList<Object>();
		keyValues.add("test1");
		r1.setSourceTableRecord(new SourceTableRecord(session, new Match(), keyValues));

		r2 = new MungeResult();
		keyValues = new ArrayList<Object>();
		keyValues.add("test2");
		r2.setSourceTableRecord(new SourceTableRecord(session, new Match(), keyValues));
		
		outString1 = new MungeStepOutput<String>("string", String.class);
		outString1.setData("B");
		outDecimal1 = new MungeStepOutput<BigDecimal>("bigdecimal", BigDecimal.class);
		outDecimal1.setData(BigDecimal.valueOf(1));
		outBoolean1 = new MungeStepOutput<Boolean>("boolean", Boolean.class);
		outBoolean1.setData(Boolean.TRUE);
		outDate1 = new MungeStepOutput<Date>("date", Date.class);
		outDate1.setData(new Date(1));
		mungedData1 = new MungeStepOutput[] {outString1, outDecimal1, outBoolean1, outDate1};
		r1.setMungedData(mungedData1);
		
		outString2 = new MungeStepOutput<String>("string", String.class);
		outString2.setData("B");
		outDecimal2 = new MungeStepOutput<BigDecimal>("bigdecimal", BigDecimal.class);
		outDecimal2.setData(BigDecimal.valueOf(1));
		outBoolean2 = new MungeStepOutput<Boolean>("boolean", Boolean.class);
		outBoolean2.setData(Boolean.TRUE);
		outDate2 = new MungeStepOutput<Date>("date", Date.class);
		outDate2.setData(new Date(1));
		mungedData2 = new MungeStepOutput[] {outString2, outDecimal2, outBoolean2, outDate2};
		r2.setMungedData(mungedData2);
	}

	public void testCompareToEquals() throws Exception {
		int compareResult = r1.compareTo(r2);
		assertEquals(0, compareResult);
	}
	
	public void testCompareToString() throws Exception {
		outString2.setData("A");
		int compareResult = r1.compareTo(r2);
		assertEquals(1, compareResult);
		
		outString2.setData("C");
		compareResult = r1.compareTo(r2);
		assertEquals(-1, compareResult);
	}
	
	public void testCompareToDecimal() throws Exception {
		outDecimal2.setData(BigDecimal.valueOf(0));
		int compareResult = r1.compareTo(r2);
		assertEquals(1, compareResult);
		
		outDecimal2.setData(BigDecimal.valueOf(2));
		compareResult = r1.compareTo(r2);
		assertEquals(-1, compareResult);
	}
	
	public void testCompareToBoolean() throws Exception {
		outBoolean2.setData(Boolean.FALSE);
		int compareResult = r1.compareTo(r2);
		assertEquals(1, compareResult);
		
		compareResult = r2.compareTo(r1);
		assertEquals(-1, compareResult);
	}
	
	public void testCompareToDate() throws Exception {
		outDate2.setData(new Date(0));
		int compareResult = r1.compareTo(r2);
		assertEquals(1, compareResult);
		
		outDate2.setData(new Date(2));
		compareResult = r1.compareTo(r2);
		assertEquals(-1, compareResult);
	}
	
	// Test to ensure the String, which is first in the list of MungeStepOutputs
	// takes precedence over the BigDecimal output
	public void testCompareToStringAndDecimal() throws Exception {
		outString2.setData("A");
		outDecimal2.setData(BigDecimal.valueOf(2));
		int compareResult = r1.compareTo(r2);
		assertEquals(1, compareResult);
		
		outString2.setData("C");
		outDecimal2.setData(BigDecimal.valueOf(0));
		compareResult = r1.compareTo(r2);
		assertEquals(-1, compareResult);
	}
}
