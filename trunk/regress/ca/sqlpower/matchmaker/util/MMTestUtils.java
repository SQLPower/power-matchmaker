package ca.sqlpower.matchmaker.util;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerCriteriaGroup;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchPool;
import ca.sqlpower.matchmaker.PotentialMatchRecord;
import ca.sqlpower.matchmaker.SourceTableRecord;
import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;

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
	public static MatchPool createTestingPool(MatchMakerSession session, Match m, MatchMakerCriteriaGroup criteriaGroup) {
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
		
		PotentialMatchRecord pmr = new PotentialMatchRecord(criteriaGroup, MatchType.UNMATCH, node1, node2, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.UNMATCH, node2, node3, false);
		pool.addPotentialMatch(pmr);
		
		//The graph for b
		node1 = new SourceTableRecord(session, m, "b1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "b2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "b3");
		pool.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node1, node2, false);
		pmr.setMaster(node1);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.UNMATCH, node2, node3, false);
		pool.addPotentialMatch(pmr);
		
		//The graph for c
		node1 = new SourceTableRecord(session, m, "c1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "c2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "c3");
		pool.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node1, node2, false);
		pmr.setMaster(node2);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.UNMATCH, node2, node3, false);
		pool.addPotentialMatch(pmr);
		
		//The graph for d
		node1 = new SourceTableRecord(session, m, "d1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "d2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "d3");
		pool.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node1, node2, false);
		pmr.setMaster(node2);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node2, node3, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		
		//The graph for e
		node1 = new SourceTableRecord(session, m, "e1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "e2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "e3");
		pool.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node1, node2, false);
		pmr.setMaster(node2);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node2, node3, false);
		pmr.setMaster(node2);
		pool.addPotentialMatch(pmr);
		
		//The graph for f
		node1 = new SourceTableRecord(session, m, "f1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "f2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "f3");
		pool.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node1, node2, false);
		pmr.setMaster(node1);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node2, node3, false);
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
		
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node1, node2, false);
		pmr.setMaster(node2);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.UNMATCH, node2, node3, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node3, node4, false);
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
		
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node1, node2, false);
		pmr.setMaster(node1);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.UNMATCH, node2, node3, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node3, node4, false);
		pmr.setMaster(node4);
		pool.addPotentialMatch(pmr);
		
		//The graph for i
		node1 = new SourceTableRecord(session, m, "i1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "i2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "i3");
		pool.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node1, node2, false);
		pmr.setMaster(node2);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node2, node3, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.UNMATCH, node3, node1, false);
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
		
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.UNMATCH, node1, node2, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node2, node3, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node3, node4, false);
		pmr.setMaster(node4);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node2, node4, false);
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
		
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.UNMATCH, node1, node2, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node2, node3, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node3, node4, false);
		pmr.setMaster(node4);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node5, node4, false);
		pmr.setMaster(node5);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node5, node3, false);
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
		
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.UNMATCH, node1, node2, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node2, node3, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node2, node4, false);
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
		
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.UNMATCH, node1, node2, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node2, node3, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node3, node5, false);
		pmr.setMaster(node5);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node5, node6, false);
		pmr.setMaster(node6);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node6, node7, false);
		pmr.setMaster(node7);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node6, node4, false);
		pmr.setMaster(node4);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node4, node3, false);
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
		
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node1, node2, false);
		pmr.setMaster(node2);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node2, node3, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node3, node4, false);
		pmr.setMaster(node4);
		pool.addPotentialMatch(pmr);
		
		node1 = new SourceTableRecord(session, m, "o1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "o2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "o3");
		pool.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.NOMATCH, node1, node3, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node2, node3, false);
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
		
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.NOMATCH, node1, node2, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node2, node3, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node3, node4, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.UNMATCH, node1, node4, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.NOMATCH, node3, node1, false);
		pool.addPotentialMatch(pmr);
		
		node1 = new SourceTableRecord(session, m, "q1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "q2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "q3");
		pool.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(session, m, "q4");
		pool.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.NOMATCH, node1, node2, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node2, node3, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.UNMATCH, node3, node4, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.UNMATCH, node1, node4, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.NOMATCH, node3, node1, false);
		pool.addPotentialMatch(pmr);
		
		node1 = new SourceTableRecord(session, m, "r1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "r2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "r3");
		pool.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(session, m, "r4");
		pool.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node1, node2, false);
		pmr.setMaster(node2);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node2, node3, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node3, node4, false);
		pmr.setMaster(node4);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node1, node4, false);
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
		
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.NOMATCH, node1, node2, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.UNMATCH, node2, node3, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node3, node4, false);
		pmr.setMaster(node4);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.UNMATCH, node1, node4, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.UNMATCH, node1, node3, false);
		pool.addPotentialMatch(pmr);
		
		node1 = new SourceTableRecord(session, m, "t1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "t2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "t3");
		pool.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(session, m, "t4");
		pool.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node1, node2, false);
		pmr.setMaster(node2);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.NOMATCH, node2, node3, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.UNMATCH, node3, node4, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.UNMATCH, node1, node3, false);
		pool.addPotentialMatch(pmr);
		
		node1 = new SourceTableRecord(session, m, "u1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "u2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "u3");
		pool.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.UNMATCH, node1, node2, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.UNMATCH, node2, node3, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.NOMATCH, node1, node3, false);
		pool.addPotentialMatch(pmr);
		
		node1 = new SourceTableRecord(session, m, "v1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "v2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "v3");
		pool.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(session, m, "v4");
		pool.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.NOMATCH, node1, node2, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.UNMATCH, node2, node3, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node3, node4, false);
		pmr.setMaster(node4);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.UNMATCH, node1, node3, false);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.UNMATCH, node1, node4, false);
		pool.addPotentialMatch(pmr);
		
		//The graph for cycle
		node1 = new SourceTableRecord(session, m, "cycle1");
		pool.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(session, m, "cycle2");
		pool.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(session, m, "cycle3");
		pool.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node1, node2, false);
		pmr.setMaster(node2);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node2, node3, false);
		pmr.setMaster(node3);
		pool.addPotentialMatch(pmr);
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node3, node1, false);
		pmr.setMaster(node1);
		pool.addPotentialMatch(pmr);
		
		//The graph for the unicycle
		node1 = new SourceTableRecord(session, m, "unicycle");
		pool.addSourceTableRecord(node1);
		
		pmr = new PotentialMatchRecord(criteriaGroup, MatchType.MATCH, node1, node1, false);
		pmr.setMaster(node1);
		pool.addPotentialMatch(pmr);
		
		//The graph for the loner
		node1 = new SourceTableRecord(session, m, "loner");
		pool.addSourceTableRecord(node1);
		
		return pool;
	}
}
