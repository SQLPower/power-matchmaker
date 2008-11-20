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


package ca.sqlpower.matchmaker.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchPool;
import ca.sqlpower.matchmaker.MatchPoolTest;
import ca.sqlpower.matchmaker.PotentialMatchRecord;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.SourceTableRecord;
import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;
import ca.sqlpower.matchmaker.munge.MungeProcess;

/**
 * This is a class for different utilities used in testing.
 */
public class MMTestUtils {

	Logger logger = Logger.getLogger(MMTestUtils.class);
	
	private MMTestUtils() {
		//This is a utilities class for testing and should not have instances created.
	}

	/**
	 * Creates a graph that looks like this:
	 * <p>
	 * <img src="doc-files/testgraph.png">
	 */
	public static MatchPool createTestingPool(MatchMakerSession session,
			Project m,
			MungeProcess mungeProcessOne,
			MungeProcess mungeProcessTwo) {
		
		MatchPool pool = new MatchPool(m);
		
		//The graph for a
		SourceTableRecord node1 = new SourceTableRecord(session, m, "a1");
		pool.addSourceTableRecord(node1);
		SourceTableRecord node2 = new SourceTableRecord(session, m, "a2");
		pool.addSourceTableRecord(node2);
		SourceTableRecord node3 = new SourceTableRecord(session, m, "a3");
		pool.addSourceTableRecord(node3);
		SourceTableRecord node4;
		SourceTableRecord node5;
		SourceTableRecord node6;
		SourceTableRecord node7;
		
		PotentialMatchRecord pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node2, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node2, node3, false);
		pool.addPotentialMatch(pmr);
		
		//The graph for b
		node1 = new SourceTableRecord(session, m, "b1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "b2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "b3");
		pool.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node2, false);
		pmr.setMaster(node1);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node2, node3, false);
		pool.addPotentialMatch(pmr);
		
		//The graph for c
		node1 = new SourceTableRecord(session, m, "c1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "c2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "c3");
		pool.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node2, false);
		pmr.setMaster(node2);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node2, node3, false);
		pool.addPotentialMatch(pmr);
		
		//The graph for d
		node1 = new SourceTableRecord(session, m, "d1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "d2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "d3");
		pool.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node2, false);
		pmr.setMaster(node2);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		
		//The graph for e
		node1 = new SourceTableRecord(session, m, "e1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "e2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "e3");
		pool.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node2, false);
		pmr.setMaster(node2);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMaster(node2);
		pool.addPotentialMatch(pmr);
		
		//The graph for f
		node1 = new SourceTableRecord(session, m, "f1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "f2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "f3");
		pool.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node2, false);
		pmr.setMaster(node1);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		
		//The graph for g
		node1 = new SourceTableRecord(session, m, "g1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "g2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "g3");
		pool.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(session, m, "g4");
		pool.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node2, false);
		pmr.setMaster(node2);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node2, node3, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node3, node4, false);
		pmr.setMaster(node4);
		pool.addPotentialMatch(pmr);
		
		//The graph for h
		node1 = new SourceTableRecord(session, m, "h1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "h2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "h3");
		pool.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(session, m, "h4");
		pool.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node2, false);
		pmr.setMaster(node1);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node2, node3, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node3, node4, false);
		pmr.setMaster(node4);
		pool.addPotentialMatch(pmr);
		
		//The graph for i
		node1 = new SourceTableRecord(session, m, "i1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "i2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "i3");
		pool.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node2, false);
		pmr.setMaster(node2);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node3, node1, false);
		pool.addPotentialMatch(pmr);
		
		//The graph for j
		node1 = new SourceTableRecord(session, m, "j1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "j2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "j3");
		pool.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(session, m, "j4");
		pool.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node2, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node3, node4, false);
		pmr.setMaster(node4);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node4, false);
		pmr.setMaster(node2);
		pool.addPotentialMatch(pmr);
		
		//The graph for k
		node1 = new SourceTableRecord(session, m, "k1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "k2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "k3");
		pool.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(session, m, "k4");
		pool.addSourceTableRecord(node4);
		node5 = new SourceTableRecord(session, m, "k5");
		pool.addSourceTableRecord(node5);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node2, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node3, node4, false);
		pmr.setMaster(node4);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node5, node4, false);
		pmr.setMaster(node5);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node5, node3, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		
		//The graph for l
		node1 = new SourceTableRecord(session, m, "l1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "l2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "l3");
		pool.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(session, m, "l4");
		pool.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node2, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node4, false);
		pmr.setMaster(node4);
		pool.addPotentialMatch(pmr);
		
		//The graph for m
		node1 = new SourceTableRecord(session, m, "m1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "m2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "m3");
		pool.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(session, m, "m4");
		pool.addSourceTableRecord(node4);
		node5 = new SourceTableRecord(session, m, "m5");
		pool.addSourceTableRecord(node5);
		node6 = new SourceTableRecord(session, m, "m6");
		pool.addSourceTableRecord(node6);
		node7 = new SourceTableRecord(session, m, "m7");
		pool.addSourceTableRecord(node7);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node2, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node3, node5, false);
		pmr.setMaster(node5);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node5, node6, false);
		pmr.setMaster(node6);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node6, node7, false);
		pmr.setMaster(node7);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node6, node4, false);
		pmr.setMaster(node4);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node4, node3, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		
		node1 = new SourceTableRecord(session, m, "n1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "n2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "n3");
		pool.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(session, m, "n4");
		pool.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node2, false);
		pmr.setMaster(node2);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node3, node4, false);
		pmr.setMaster(node4);
		pool.addPotentialMatch(pmr);
		
		node1 = new SourceTableRecord(session, m, "o1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "o2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "o3");
		pool.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.NOMATCH, node1, node3, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		
		node1 = new SourceTableRecord(session, m, "p1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "p2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "p3");
		pool.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(session, m, "p4");
		pool.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.NOMATCH, node1, node2, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node3, node4, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node4, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.NOMATCH, node3, node1, false);
		pool.addPotentialMatch(pmr);
		
		node1 = new SourceTableRecord(session, m, "q1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "q2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "q3");
		pool.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(session, m, "q4");
		pool.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.NOMATCH, node1, node2, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node3, node4, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node4, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.NOMATCH, node3, node1, false);
		pool.addPotentialMatch(pmr);
		
		node1 = new SourceTableRecord(session, m, "r1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "r2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "r3");
		pool.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(session, m, "r4");
		pool.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node2, false);
		pmr.setMaster(node2);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node3, node4, false);
		pmr.setMaster(node4);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node4, false);
		pmr.setMaster(node1);
		pool.addPotentialMatch(pmr);
		
		node1 = new SourceTableRecord(session, m, "s1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "s2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "s3");
		pool.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(session, m, "s4");
		pool.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.NOMATCH, node1, node2, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node2, node3, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node3, node4, false);
		pmr.setMaster(node4);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node4, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node3, false);
		pool.addPotentialMatch(pmr);
		
		node1 = new SourceTableRecord(session, m, "t1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "t2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "t3");
		pool.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(session, m, "t4");
		pool.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node2, false);
		pmr.setMaster(node2);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.NOMATCH, node2, node3, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node3, node4, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node3, false);
		pool.addPotentialMatch(pmr);
		
		node1 = new SourceTableRecord(session, m, "u1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "u2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "u3");
		pool.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node2, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node2, node3, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.NOMATCH, node1, node3, false);
		pool.addPotentialMatch(pmr);
		
		node1 = new SourceTableRecord(session, m, "v1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "v2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "v3");
		pool.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(session, m, "v4");
		pool.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.NOMATCH, node1, node2, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node2, node3, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node3, node4, false);
		pmr.setMaster(node4);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node3, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node4, false);
		pool.addPotentialMatch(pmr);
		
		//The graph for cycle
		node1 = new SourceTableRecord(session, m, "cycle1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "cycle2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "cycle3");
		pool.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node2, false);
		pmr.setMaster(node2);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node3, node1, false);
		pmr.setMaster(node1);
		pool.addPotentialMatch(pmr);
		
		//The graph for the unicycle
		node1 = new SourceTableRecord(session, m, "unicycle");
		pool.addSourceTableRecord(node1);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node1, false);
		pmr.setMaster(node1);
		pool.addPotentialMatch(pmr);
		
		//The graph for the loner
		node1 = new SourceTableRecord(session, m, "loner");
		pool.addSourceTableRecord(node1);
		
		//Here is where AutoMatch graphs start. Rule Sets matter in these ones.
		node1 = new SourceTableRecord(session, m, "w1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "w2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "w3");
		pool.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(mungeProcessTwo, MatchType.UNMATCH, node1, node2, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node2, node3, false);
		pool.addPotentialMatch(pmr);
		
		node1 = new SourceTableRecord(session, m, "x1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "x2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "x3");
		pool.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(session, m, "x4");
		pool.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node2, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessTwo, MatchType.UNMATCH, node2, node3, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node3, node4, false);
		pool.addPotentialMatch(pmr);
		
		node1 = new SourceTableRecord(session, m, "y1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "y2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "y3");
		pool.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(session, m, "y4");
		pool.addSourceTableRecord(node4);		

		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node2, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.NOMATCH, node2, node3, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node3, node4, false);
		pool.addPotentialMatch(pmr);
		
		node1 = new SourceTableRecord(session, m, "z1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "z2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "z3");
		pool.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(session, m, "z4");
		pool.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(mungeProcessTwo, MatchType.MATCH, node1, node2, false);
		pmr.setMaster(node1);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(mungeProcessTwo, MatchType.MATCH, node3, node4, false);
		pmr.setMaster(node4);
		pool.addPotentialMatch(pmr);
		
		return pool;
	}

	/**
	 * Creates the result table used to store information about the graphs
	 * created by
	 * {@link #createTestingPool(MatchMakerSession, Project, MungeProcess, MungeProcess)}.
	 * This table is only required if we want to test against a database.
	 */
	public static void createResultTable(Connection con) throws SQLException {
		Statement stmt = con.createStatement();
		stmt.executeUpdate("CREATE TABLE pl.match_results ("
				+ "\n DUP_CANDIDATE_10 varchar(50) not null,"
				+ "\n DUP_CANDIDATE_20 varchar(50) not null,"
				+ "\n CURRENT_CANDIDATE_10 varchar(50),"
				+ "\n CURRENT_CANDIDATE_20 varchar(50)," 
				+ "\n DUP_ID0 varchar(50),"
				+ "\n MASTER_ID0 varchar(50),"
				+ "\n CANDIDATE_10_MAPPED varchar(1),"
				+ "\n CANDIDATE_20_MAPPED varchar(1),"
				+ "\n MATCH_PERCENT integer," 
				+ "\n GROUP_ID varchar(60),"
				+ "\n MATCH_DATE timestamp," 
				+ "\n MATCH_STATUS varchar(60),"
				+ "\n MATCH_STATUS_DATE timestamp,"
				+ "\n MATCH_STATUS_USER varchar(60),"
				+ "\n DUP1_MASTER_IND  varchar(1)" + "\n)");
		stmt.close();
	}

	/**
	 * Removes the result table created by {@link #createResultTable(Connection)}.
	 */
	public static void dropResultTable(Connection con) throws SQLException {
		Statement stmt = con.createStatement();
		stmt.executeUpdate("DROP TABLE pl.match_results");
		stmt.close();
	}
	
	/**
	 * Creates the source table for testing the MatchPool. See
	 * ({@link MatchPoolTest})
	 * This table is only required if we want to test against a database.
	 */
	public static void createSourceTable(Connection con) throws SQLException {
		Statement stmt = con.createStatement();
		stmt.executeUpdate("CREATE TABLE pl.source_table ("
				+ "\n PK1 varchar(50) not null,"
				+ "\n FOO varchar(10),"
				+ "\n BAR varchar(10)," 
				+ "\n CONSTRAINT SOURCE_TABLE_PK PRIMARY KEY (PK1)" + "\n)");
		stmt.close();
	}
	
	/**
	 * Removes the source table created by {@link #createSourceTable(Connection)}.
	 */
	public static void dropSourceTable(Connection con) throws SQLException {
		Statement stmt = con.createStatement();
		stmt.executeUpdate("DROP TABLE pl.source_table");
		stmt.close();
	}
}
