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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ca.sqlpower.matchmaker.swingui.munge.StepDescription;

import junit.framework.TestCase;

/**
 * Test for the Munge Process graph model.  Here's the setup:
 *
 * <pre>
 *         +---A---+          Notes: A has one output, which is
 *         |   |   |                 connected to B, C, and F
 *         v   |   v
 *         B   |   C---+   D         C has two outputs.
 *         |   |   |   |             Output 0 is connected to F
 *         v   v   |   v             Output 1 is connected to G
 *         E   F&lt;--+   G
 * </pre>
 */
public class MungeProcessGraphModelTest extends TestCase {

    /**
     * The graph model, created by the setUp() method.
     */
    private MungeProcessGraphModel gm;
    
    /**
     * Step A as shown in the diagram in the class comment.
     */
    private MungeStep a;
    
    /**
     * Step B as shown in the diagram in the class comment.
     */
    private MungeStep b;
    
    /**
     * Step C as shown in the diagram in the class comment.
     */
    private MungeStep c;
    
    /**
     * Step D as shown in the diagram in the class comment.
     */
    private MungeStep d;
    
    /**
     * Step E as shown in the diagram in the class comment.
     */
    private MungeStep e;
    
    /**
     * Step F as shown in the diagram in the class comment.
     */
    private MungeStep f;
    
    /**
     * Step G as shown in the diagram in the class comment.
     */
    private MungeStep g;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        List<MungeStep> steps = new ArrayList<MungeStep>();

        StepDescription sd = new StepDescription();
        sd.setName("A");
        a = new TestingMungeStep(sd, 0, 1);
        steps.add(a);

        sd = new StepDescription();
        sd.setName("B");
        b = new TestingMungeStep(sd, 1, 1);
        b.connectInput(0, a.getChildren().get(0));
        steps.add(b);

        sd = new StepDescription();
        sd.setName("C");
        c = new TestingMungeStep(sd, 1, 2);
        c.connectInput(0, a.getChildren().get(0));
        steps.add(c);

        sd = new StepDescription();
        sd.setName("D");
        d = new TestingMungeStep(sd, 1, 1);
        steps.add(d);

        sd = new StepDescription();
        sd.setName("E");
        e = new TestingMungeStep(sd, 1, 0);
        e.connectInput(0, b.getChildren().get(0));
        steps.add(e);

        sd = new StepDescription();
        sd.setName("F");
        f = new TestingMungeStep(sd, 3, 0);
        f.connectInput(0, a.getChildren().get(0));
        // purposely leaving input 1 not connected (for testing)
        f.connectInput(2, c.getChildren().get(0));
        steps.add(f);

        sd = new StepDescription();
        sd.setName("G");
        g = new TestingMungeStep(sd, 1, 1);
        g.connectInput(0, c.getChildren().get(1));
        steps.add(g);

        gm = new MungeProcessGraphModel(steps);
    }
    
    
    // ========= Edge tests (inbound and outbound) ===========
    
    public void testSingleOutputMultipleConnections() throws Exception {
        Collection<MungeProcessGraphModel.Edge> obe = gm.getOutboundEdges(a);
        assertEquals(3, obe.size());
        assertTrue(obe.contains(new MungeProcessGraphModel.Edge(a.getChildren().get(0), b)));
        assertTrue(obe.contains(new MungeProcessGraphModel.Edge(a.getChildren().get(0), c)));
        assertTrue(obe.contains(new MungeProcessGraphModel.Edge(a.getChildren().get(0), f)));
    }
    
    public void testSingleInput() throws Exception {
        Collection<MungeProcessGraphModel.Edge> ibe = gm.getInboundEdges(b);
        assertEquals(1, ibe.size());
        assertTrue(ibe.contains(new MungeProcessGraphModel.Edge(a.getChildren().get(0), b)));
    }

    public void testSingleOutput() throws Exception {
        Collection<MungeProcessGraphModel.Edge> obe = gm.getOutboundEdges(b);
        assertEquals(1, obe.size());
        assertTrue(obe.contains(new MungeProcessGraphModel.Edge(b.getChildren().get(0), e)));
    }
    
    public void testMultipleOutputs() throws Exception {
        Collection<MungeProcessGraphModel.Edge> obe = gm.getOutboundEdges(c);
        assertEquals(2, obe.size());
        assertTrue(obe.contains(new MungeProcessGraphModel.Edge(c.getChildren().get(0), f)));
        assertTrue(obe.contains(new MungeProcessGraphModel.Edge(c.getChildren().get(1), g)));
    }
    
    public void testMultipleInputs() throws Exception {
        Collection<MungeProcessGraphModel.Edge> ibe = gm.getInboundEdges(f);
        assertEquals(2, ibe.size());
        assertTrue(ibe.contains(new MungeProcessGraphModel.Edge(a.getChildren().get(0), f)));
        assertTrue(ibe.contains(new MungeProcessGraphModel.Edge(c.getChildren().get(0), f)));
    }

    public void testNoInputs() throws Exception {
        Collection<MungeProcessGraphModel.Edge> ibe = gm.getInboundEdges(a);
        assertEquals(0, ibe.size());
        
        // Double check on a totally disconnected node
        ibe = gm.getInboundEdges(d);
        assertEquals(0, ibe.size());
    }

    public void testNoOutputs() throws Exception {
        Collection<MungeProcessGraphModel.Edge> obe = gm.getOutboundEdges(f);
        assertEquals(0, obe.size());
        
        // Double check on a totally disconnected node
        obe = gm.getOutboundEdges(d);
        assertEquals(0, obe.size());
    }

    
    // ========= Adjacency tests (all adjacency is considered inbound) ===========

    public void testNoneAdjacent() throws Exception {
        Collection<MungeStep> adj = gm.getAdjacentNodes(a);
        assertEquals(0, adj.size());

        // Double check on a totally disconnected node
        adj = gm.getAdjacentNodes(d);
        assertEquals(0, adj.size());
}
    
    public void testManyAdjacentToOneOutput() throws Exception {
        Collection<MungeStep> adj = gm.getAdjacentNodes(f);
        assertEquals(2, adj.size());
        assertTrue(adj.contains(a));
        assertTrue(adj.contains(c));
    }
}
