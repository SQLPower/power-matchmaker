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

package ca.sqlpower.matchmaker;

import java.util.Collections;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;
import ca.sqlpower.matchmaker.PotentialMatchRecord.StoreState;
import ca.sqlpower.matchmaker.munge.MungeProcess;

public class PotentialMatchRecordTest extends TestCase {

	private static final Logger logger = Logger.getLogger(PotentialMatchRecordTest.class);
	
	PotentialMatchRecord pmr;
	
	@Override
	protected void setUp() throws Exception {
		MatchMakerSession session = new TestingMatchMakerSession();
		Project project = new Project();
		project.setSession(session);
		MungeProcess mungeProcess = new MungeProcess();
		mungeProcess.setName("mungeprocess");
		project.addChild(mungeProcess);
		MatchPool pool = new MatchPool(project);
		SourceTableRecord str1 = new SourceTableRecord(session, project, Collections.singletonList("str1"));
		SourceTableRecord str2 = new SourceTableRecord(session, project, Collections.singletonList("str2"));
		pmr = new PotentialMatchRecord(mungeProcess, MatchType.UNMATCH, str1, str2, false);
		pool.addPotentialMatch(pmr);
	}
	
	public void testDirtyAfterPropertyChange() {
		pmr.setStoreState(StoreState.CLEAN);
		pmr.setMatchStatus(MatchType.AUTOMATCH);
		pmr.setMatchStatus(MatchType.NOMATCH);
		assertSame(StoreState.DIRTY, pmr.getStoreState());
		pmr.setStoreState(StoreState.CLEAN);
		pmr.setMaster(pmr.getOriginalLhs());
		pmr.setMaster(pmr.getOriginalRhs());
		assertSame(StoreState.DIRTY, pmr.getStoreState());
	}
	
	public void testAlwaysNewAfterPropertyChange() {
		pmr.setStoreState(StoreState.NEW);
		pmr.setMatchStatus(MatchType.AUTOMATCH);
		pmr.setMatchStatus(MatchType.NOMATCH);
		assertSame(StoreState.NEW, pmr.getStoreState());
		pmr.setStoreState(StoreState.NEW);
		pmr.setMaster(pmr.getOriginalLhs());
		pmr.setMaster(pmr.getOriginalRhs());
		assertSame(StoreState.NEW, pmr.getStoreState());
	}
	
	public void testIsMatch() {
		pmr.setMaster(pmr.getOriginalLhs());
		pmr.setMatchStatus(MatchType.MATCH);
		assertTrue(pmr.isMatch());
		
		pmr.setMatchStatus(MatchType.AUTOMATCH);
		assertTrue(pmr.isMatch());
		
		pmr.setMaster(pmr.getOriginalRhs());
		assertTrue(pmr.isMatch());
		
		pmr.setMatchStatus(MatchType.MATCH);
		assertTrue(pmr.isMatch());
		
		pmr.setMaster(null);
		pmr.setMatchStatus(MatchType.UNMATCH);
		assertFalse(pmr.isMatch());
		
		pmr.setMatchStatus(MatchType.NOMATCH);
		assertFalse(pmr.isMatch());
		
		pmr.setMatchStatus(MatchType.MATCH);
		try {
			pmr.isMatch();
			fail("isMatch should have thrown an IllegalStateException!");
		} catch (IllegalStateException e) {
			// Do nothing
		}
		
		pmr.setMatchStatus(MatchType.AUTOMATCH);
		try {
			pmr.isMatch();
			fail("isMatch should have thrown an IllegalStateException!");
		} catch (IllegalStateException e) {
			// Do nothing
		}
	}
}
