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
	private Object[] mungedData1;
	private Object[] mungedData2;
	
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
		
		mungedData1 = new Object[] {"B", BigDecimal.valueOf(1), Boolean.TRUE, new Date(1)};
		r1.setMungedData(mungedData1);
		
		mungedData2 = new Object[] {"B", BigDecimal.valueOf(1), Boolean.TRUE, new Date(1)};
		r2.setMungedData(mungedData2);
	}

	public void testCompareToEquals() throws Exception {
		int compareResult = r1.compareTo(r2);
		assertEquals(0, compareResult);
	}
	
	public void testCompareToString() throws Exception {
		mungedData2[0] = "A";
		int compareResult = r1.compareTo(r2);
		assertEquals(1, compareResult);
		
		mungedData2[0] = "C";
		compareResult = r1.compareTo(r2);
		assertEquals(-1, compareResult);
	}
	
	public void testCompareToDecimal() throws Exception {
		mungedData2[1] = BigDecimal.valueOf(0);
		int compareResult = r1.compareTo(r2);
		assertEquals(1, compareResult);
		
		mungedData2[1] = BigDecimal.valueOf(2);
		compareResult = r1.compareTo(r2);
		assertEquals(-1, compareResult);
	}
	
	public void testCompareToBoolean() throws Exception {
		mungedData2[2] = Boolean.FALSE;
		int compareResult = r1.compareTo(r2);
		assertEquals(1, compareResult);
		
		compareResult = r2.compareTo(r1);
		assertEquals(-1, compareResult);
	}
	
	public void testCompareToDate() throws Exception {
		mungedData2[3] = new Date(0);
		int compareResult = r1.compareTo(r2);
		assertEquals(1, compareResult);
		
		mungedData2[3] = new Date(2);
		compareResult = r1.compareTo(r2);
		assertEquals(-1, compareResult);
	}
	
	// Test to ensure the String, which is first in the list of MungeStepOutputs
	// takes precedence over the BigDecimal output
	public void testCompareToStringAndDecimal() throws Exception {
		mungedData2[0] = "A";
		mungedData2[1] = BigDecimal.valueOf(2);
		int compareResult = r1.compareTo(r2);
		assertEquals(1, compareResult);
		
		mungedData2[0] = "C";
		mungedData2[1] = BigDecimal.valueOf(0);
		compareResult = r1.compareTo(r2);
		assertEquals(-1, compareResult);
	}
}
