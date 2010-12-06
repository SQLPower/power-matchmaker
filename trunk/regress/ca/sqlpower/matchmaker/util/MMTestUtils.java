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


package ca.sqlpower.matchmaker.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchCluster;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchPool;
import ca.sqlpower.matchmaker.MatchPoolTest;
import ca.sqlpower.matchmaker.PotentialMatchRecord;
import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.SourceTableRecord;
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
	public static void createTestingPool(MatchMakerSession session,
			Project m,
			MungeProcess mungeProcessOne,
			MungeProcess mungeProcessTwo) {
		
		MatchPool pool = m.getMatchPool();
		
		//The graph for a
		MatchCluster a = new MatchCluster();
		SourceTableRecord node1 = new SourceTableRecord(m, "a1");
		a.addSourceTableRecord(node1);
		SourceTableRecord node2 = new SourceTableRecord(m, "a2");
		a.addSourceTableRecord(node2);
		SourceTableRecord node3 = new SourceTableRecord(m, "a3");
		a.addSourceTableRecord(node3);
		SourceTableRecord node4;
		SourceTableRecord node5;
		SourceTableRecord node6;
		SourceTableRecord node7;
		
		PotentialMatchRecord pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node2, false);
		a.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node2, node3, false);
		a.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(a);
		
		//The graph for b
		MatchCluster b = new MatchCluster();
		node1 = new SourceTableRecord(m, "b1");
		b.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(m, "b2");
		b.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(m, "b3");
		b.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node2, false);
		pmr.setMasterRecord(node1);
		b.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node2, node3, false);
		b.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(b);
		
		//The graph for c
		MatchCluster c = new MatchCluster();
		node1 = new SourceTableRecord(m, "c1");
		c.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(m, "c2");
		c.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(m, "c3");
		c.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node2, false);
		pmr.setMasterRecord(node2);
		c.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node2, node3, false);
		c.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(c);
		
		//The graph for d
		MatchCluster d = new MatchCluster();
		node1 = new SourceTableRecord(m, "d1");
		d.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(m, "d2");
		d.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(m, "d3");
		d.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node2, false);
		pmr.setMasterRecord(node2);
		d.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMasterRecord(node3);
		d.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(d);
		
		//The graph for e
		MatchCluster e = new MatchCluster();
		node1 = new SourceTableRecord(m, "e1");
		e.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(m, "e2");
		e.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(m, "e3");
		e.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node2, false);
		pmr.setMasterRecord(node2);
		e.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMasterRecord(node2);
		e.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(e);
		
		//The graph for f
		MatchCluster f = new MatchCluster();
		node1 = new SourceTableRecord(m, "f1");
		f.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(m, "f2");
		f.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(m, "f3");
		f.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node2, false);
		pmr.setMasterRecord(node1);
		f.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMasterRecord(node3);
		f.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(f);
		
		//The graph for g
		MatchCluster g = new MatchCluster();
		node1 = new SourceTableRecord(m, "g1");
		g.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(m, "g2");
		g.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(m, "g3");
		g.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(m, "g4");
		g.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node2, false);
		pmr.setMasterRecord(node2);
		g.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node2, node3, false);
		g.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node3, node4, false);
		pmr.setMasterRecord(node4);
		g.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(g);
		
		//The graph for h
		MatchCluster h = new MatchCluster();
		node1 = new SourceTableRecord(m, "h1");
		h.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(m, "h2");
		h.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(m, "h3");
		h.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(m, "h4");
		h.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node2, false);
		pmr.setMasterRecord(node1);
		h.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node2, node3, false);
		h.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node3, node4, false);
		pmr.setMasterRecord(node4);
		h.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(h);
		
		//The graph for i
		MatchCluster i = new MatchCluster();
		node1 = new SourceTableRecord(m, "i1");
		h.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(m, "i2");
		h.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(m, "i3");
		h.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node2, false);
		pmr.setMasterRecord(node2);
		h.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMasterRecord(node3);
		h.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node3, node1, false);
		h.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(i);
		
		//The graph for j
		MatchCluster j = new MatchCluster();
		node1 = new SourceTableRecord(m, "j1");
		j.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(m, "j2");
		j.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(m, "j3");
		j.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(m, "j4");
		j.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node2, false);
		j.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMasterRecord(node3);
		j.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node3, node4, false);
		pmr.setMasterRecord(node4);
		j.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node4, false);
		pmr.setMasterRecord(node2);
		j.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(j);
		
		//The graph for k
		MatchCluster k = new MatchCluster();
		node1 = new SourceTableRecord(m, "k1");
		k.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(m, "k2");
		k.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(m, "k3");
		k.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(m, "k4");
		k.addSourceTableRecord(node4);
		node5 = new SourceTableRecord(m, "k5");
		k.addSourceTableRecord(node5);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node2, false);
		k.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMasterRecord(node3);
		k.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node3, node4, false);
		pmr.setMasterRecord(node4);
		k.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node5, node4, false);
		pmr.setMasterRecord(node5);
		k.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node5, node3, false);
		pmr.setMasterRecord(node3);
		k.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(k);
		
		//The graph for l
		MatchCluster l = new MatchCluster();
		node1 = new SourceTableRecord(m, "l1");
		l.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(m, "l2");
		l.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(m, "l3");
		l.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(m, "l4");
		l.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node2, false);
		l.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMasterRecord(node3);
		l.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node4, false);
		pmr.setMasterRecord(node4);
		l.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(l);
		
		//The graph for m
		MatchCluster mm = new MatchCluster();
		node1 = new SourceTableRecord(m, "m1");
		mm.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(m, "m2");
		mm.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(m, "m3");
		mm.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(m, "m4");
		mm.addSourceTableRecord(node4);
		node5 = new SourceTableRecord(m, "m5");
		mm.addSourceTableRecord(node5);
		node6 = new SourceTableRecord(m, "m6");
		mm.addSourceTableRecord(node6);
		node7 = new SourceTableRecord(m, "m7");
		mm.addSourceTableRecord(node7);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node2, false);
		mm.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMasterRecord(node3);
		mm.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node3, node5, false);
		pmr.setMasterRecord(node5);
		mm.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node5, node6, false);
		pmr.setMasterRecord(node6);
		mm.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node6, node7, false);
		pmr.setMasterRecord(node7);
		mm.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node6, node4, false);
		pmr.setMasterRecord(node4);
		mm.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node4, node3, false);
		pmr.setMasterRecord(node3);
		mm.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(mm);
		
		//The graph for n
		MatchCluster n = new MatchCluster();
		node1 = new SourceTableRecord(m, "n1");
		n.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(m, "n2");
		n.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(m, "n3");
		n.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(m, "n4");
		n.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node2, false);
		pmr.setMasterRecord(node2);
		n.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMasterRecord(node3);
		n.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node3, node4, false);
		pmr.setMasterRecord(node4);
		n.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(n);

		//The graph for o
		MatchCluster o = new MatchCluster();
		node1 = new SourceTableRecord(m, "o1");
		o.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(m, "o2");
		o.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(m, "o3");
		o.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.NOMATCH, node1, node3, false);
		o.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMasterRecord(node3);
		o.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(o);

		//The graph for p
		MatchCluster p = new MatchCluster();
		node1 = new SourceTableRecord(m, "p1");
		p.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(m, "p2");
		p.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(m, "p3");
		p.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(m, "p4");
		p.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.NOMATCH, node1, node2, false);
		p.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMasterRecord(node3);
		p.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node3, node4, false);
		pmr.setMasterRecord(node3);
		p.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node4, false);
		p.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.NOMATCH, node3, node1, false);
		p.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(p);

		//The graph for q
		MatchCluster q = new MatchCluster();
		node1 = new SourceTableRecord(m, "q1");
		q.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(m, "q2");
		q.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(m, "q3");
		q.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(m, "q4");
		q.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.NOMATCH, node1, node2, false);
		q.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMasterRecord(node3);
		q.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node3, node4, false);
		q.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node4, false);
		q.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.NOMATCH, node3, node1, false);
		q.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(q);

		//The graph for r
		MatchCluster r = new MatchCluster();
		node1 = new SourceTableRecord(m, "r1");
		r.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(m, "r2");
		r.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(m, "r3");
		r.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(m, "r4");
		r.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node2, false);
		pmr.setMasterRecord(node2);
		r.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMasterRecord(node3);
		r.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node3, node4, false);
		pmr.setMasterRecord(node4);
		r.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node4, false);
		pmr.setMasterRecord(node1);
		r.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(r);

		//The graph for s
		MatchCluster s = new MatchCluster();
		node1 = new SourceTableRecord(m, "s1");
		s.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(m, "s2");
		s.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(m, "s3");
		s.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(m, "s4");
		s.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.NOMATCH, node1, node2, false);
		s.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node2, node3, false);
		s.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node3, node4, false);
		pmr.setMasterRecord(node4);
		s.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node4, false);
		s.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node3, false);
		s.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(s);

		//The graph for t
		MatchCluster t = new MatchCluster();
		node1 = new SourceTableRecord(m, "t1");
		t.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(m, "t2");
		t.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(m, "t3");
		t.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(m, "t4");
		t.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node2, false);
		pmr.setMasterRecord(node2);
		t.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.NOMATCH, node2, node3, false);
		t.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node3, node4, false);
		t.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node3, false);
		t.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(t);

		//The graph for u
		MatchCluster u = new MatchCluster();
		node1 = new SourceTableRecord(m, "u1");
		u.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(m, "u2");
		u.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(m, "u3");
		u.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node2, false);
		u.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node2, node3, false);
		u.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.NOMATCH, node1, node3, false);
		u.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(u);

		//The graph for v
		MatchCluster v = new MatchCluster();
		node1 = new SourceTableRecord(m, "v1");
		v.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(m, "v2");
		v.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(m, "v3");
		v.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(m, "v4");
		v.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.NOMATCH, node1, node2, false);
		v.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node2, node3, false);
		v.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node3, node4, false);
		pmr.setMasterRecord(node4);
		v.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node3, false);
		v.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node4, false);
		v.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(v);
		
		//The graph for cycle
		MatchCluster cycle = new MatchCluster();
		node1 = new SourceTableRecord(m, "cycle1");
		cycle.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(m, "cycle2");
		cycle.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(m, "cycle3");
		cycle.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node2, false);
		pmr.setMasterRecord(node2);
		cycle.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		pmr.setMasterRecord(node3);
		cycle.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node3, node1, false);
		pmr.setMasterRecord(node1);
		cycle.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(cycle);
		
		//The graph for the unicycle
		MatchCluster unicycle = new MatchCluster();
		node1 = new SourceTableRecord(m, "unicycle");
		unicycle.addSourceTableRecord(node1);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node1, node1, false);
		pmr.setMasterRecord(node1);
		unicycle.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(unicycle);
		
		//The graph for the loner
		MatchCluster loner = new MatchCluster();
		node1 = new SourceTableRecord(m, "loner");
		loner.addSourceTableRecord(node1);
		pool.addMatchCluster(loner);
		
		//Here is where AutoMatch graphs start. Rule Sets matter in these ones.
		//The graph for w
		MatchCluster w = new MatchCluster();
		node1 = new SourceTableRecord(m, "w1");
		w.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(m, "w2");
		w.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(m, "w3");
		w.addSourceTableRecord(node3);
		
		pmr = new PotentialMatchRecord(mungeProcessTwo, MatchType.UNMATCH, node1, node2, false);
		w.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node2, node3, false);
		w.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(w);

		//The graph for x
		MatchCluster x = new MatchCluster();
		node1 = new SourceTableRecord(m, "x1");
		x.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(m, "x2");
		x.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(m, "x3");
		x.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(m, "x4");
		x.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node2, false);
		x.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessTwo, MatchType.UNMATCH, node2, node3, false);
		x.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node3, node4, false);
		x.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(x);

		//The graph for y
		MatchCluster y = new MatchCluster();
		node1 = new SourceTableRecord(m, "y1");
		y.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(m, "y2");
		y.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(m, "y3");
		y.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(m, "y4");
		y.addSourceTableRecord(node4);		

		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node1, node2, false);
		y.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.NOMATCH, node2, node3, false);
		y.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.UNMATCH, node3, node4, false);
		y.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(y);

		//The graph for z
		MatchCluster z = new MatchCluster();
		node1 = new SourceTableRecord(m, "z1");
		z.addSourceTableRecord(node1);
		node2 = new SourceTableRecord(m, "z2");
		z.addSourceTableRecord(node2);
		node3 = new SourceTableRecord(m, "z3");
		z.addSourceTableRecord(node3);
		node4 = new SourceTableRecord(m, "z4");
		z.addSourceTableRecord(node4);
		
		pmr = new PotentialMatchRecord(mungeProcessTwo, MatchType.MATCH, node1, node2, false);
		pmr.setMasterRecord(node1);
		z.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessOne, MatchType.MATCH, node2, node3, false);
		z.addPotentialMatchRecord(pmr);
		pmr = new PotentialMatchRecord(mungeProcessTwo, MatchType.MATCH, node3, node4, false);
		pmr.setMasterRecord(node4);
		z.addPotentialMatchRecord(pmr);
		pool.addMatchCluster(z);
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
