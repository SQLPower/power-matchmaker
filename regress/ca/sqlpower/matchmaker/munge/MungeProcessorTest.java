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

import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Project;

import junit.framework.TestCase;

/**
 * Test for the Munge Processor itself.  Here's the setup:
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
 * 
 * Note, this setup was originally identical to the setup in the
 * MungeProcessGraphModelTest, but in order to allow modification
 * of this setup for one test case or the other, we elected to copy
 * the setup rather than factor it out, where a modification could
 * disturb all the test cases sharing it.
 */
public class MungeProcessorTest extends TestCase {

    /**
     * The munge processor under test.  It will be set up to have
     * all MungeStep objects a-g as its children, and those
     * children will be connected according to the diagram in the
     * class-level comment of this test case.
     */
    MungeProcessor mp;
    
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
    
    private final Logger logger = Logger.getLogger("testLogger");
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MungeProcess mungeProcess = new MungeProcess();
        mungeProcess.setParentProject(new Project());

        a = new TestingMungeStep("A", 0, 1);

        b = new TestingMungeStep("B", 1, 1);
        b.connectInput(0, a.getChildren().get(0));

        c = new TestingMungeStep("C", 1, 2);
        c.connectInput(0, a.getChildren().get(0));

        d = new TestingMungeStep("D", 1, 1, false);

        e = new TestingMungeStep("E", 1, 0);
        e.connectInput(0, b.getChildren().get(0));

        f = new TestingMungeStep("F", 3, 0);
        f.connectInput(0, a.getChildren().get(0));
        // purposely leaving input 1 not connected (for testing)
        f.connectInput(2, c.getChildren().get(0));

        g = new TestingMungeStep("G", 1, 1);
        g.connectInput(0, c.getChildren().get(1));
        

        // The order in which MungeSteps are added should be
        // randomized to ensure the processor isn't just processing
        // the steps in the order in which it was given.
        mungeProcess.addChild(a);
        mungeProcess.addChild(g);
        mungeProcess.addChild(b);
        mungeProcess.addChild(f);
        mungeProcess.addChild(c);
        mungeProcess.addChild(e);
        mungeProcess.addChild(d);

        mp = new MungeProcessor(mungeProcess, logger);
    }
    
    /**
     * Tests to ensure that the MungeSteps are being processed in a valid order.
     * The idea is that no MungeStep should be called before its parents, if any.
     */
    public void testCorrectProcessingOrder() throws Exception {
        List<MungeStep> processOrder = mp.getProcessOrder();

        for (MungeStep step: processOrder) {
        	List<MungeStepOutput> stepInputs = step.getInputs();
        	
        	for (MungeStepOutput output: stepInputs) {
        		if (output != null) {
        			// Check if each parent step would be called before the current
        			String called = output.getParent().getParameter("called");
        			assertEquals("true", called);
        		}
        	}
        	// mark the current step as 'called'
        	step.setParameter("called", "true");
        }
    }
}
