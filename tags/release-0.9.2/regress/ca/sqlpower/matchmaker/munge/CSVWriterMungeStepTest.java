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

import java.io.File;
import java.util.Scanner;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.TestingMatchMakerSession;

public class CSVWriterMungeStepTest extends TestCase {

	private CSVWriterMungeStep step;
	
	private MungeStepOutput testInput;
	
	private String fileName = "test.csv";
	
	private final Logger logger = Logger.getLogger("testLogger");
	
	protected void setUp() throws Exception {
		super.setUp();
		step = new CSVWriterMungeStep();
		step.setSession(new TestingMatchMakerSession());
		step.setFilePath(fileName);
	}
	
	public void testCallOnStringAndNull() throws Exception {
		int i = 0;
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData(null);
		step.connectInput(i++, testInput);
		
		testInput = new MungeStepOutput<String>("123", String.class);
		testInput.setData("123");
		step.connectInput(i++, testInput);
		
		step.removeUnusedInput();
		step.open(logger);
		step.call();
		step.commit();
		step.close();
		
		File f = new File(fileName);
		assertTrue(f.exists());
		Scanner s = new Scanner(f);
		assertEquals(",\"123\"", s.nextLine());
		s.close();
	}
	
	public void testCallOnAbsolutePath() throws Exception {
		int i = 0;
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("test");
		step.connectInput(i++, testInput);
		step.setFilePath(new File(fileName).getAbsolutePath());
		
		step.removeUnusedInput();
		step.open(logger);
		step.call();
		step.commit();
		step.close();
		
		File f = new File(fileName);
		assertTrue(f.exists());
		Scanner s = new Scanner(f);
		assertEquals("\"test\"", s.nextLine());
		s.close();	
	}
	
	public void testCallOnAppend() throws Exception {
		step.setDoClearFile(false);
		
		int i = 0;
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("test");
		step.connectInput(i++, testInput);
		
		step.removeUnusedInput();
		step.open(logger);
		step.call();
		step.commit();
		step.close();
		
		File f = new File(fileName);
		assertTrue(f.exists());
		Scanner s = new Scanner(f);
		assertEquals("\"test\"", s.nextLine());
		s.close();
		
		testInput.setData("test2");
		
		step.open(logger);
		step.call();
		step.commit();
		step.close();
		
		f = new File(fileName);
		assertTrue(f.exists());
		s = new Scanner(f);
		assertEquals("\"test\"", s.nextLine());
		assertEquals("\"test2\"", s.nextLine());
		s.close();
	}
	
	public void testCallOnRollback() throws Exception {
		int i = 0;
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("test");
		step.connectInput(i++, testInput);
		
		step.removeUnusedInput();
		step.open(logger);
		step.call();
		step.rollback();
		step.close();
		
		File f = new File(fileName);
		assertFalse(f.exists());
	}
	
	public void testCallOnSeparator() throws Exception {
		step.setSeparator(':');
		
		int i = 0;
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("test");
		step.connectInput(i++, testInput);
		testInput = new MungeStepOutput<String>("test2", String.class);
		testInput.setData("test2");
		step.connectInput(i++, testInput);
		
		step.removeUnusedInput();
		step.open(logger);
		step.call();
		step.commit();
		step.close();
		
		File f = new File(fileName);
		assertTrue(f.exists());
		Scanner s = new Scanner(f);
		assertEquals("\"test\":\"test2\"", s.nextLine());
		s.close();
	}
	
	@Override
	protected void tearDown() throws Exception {
		File f = new File(fileName);
		f.delete();
	}
}
