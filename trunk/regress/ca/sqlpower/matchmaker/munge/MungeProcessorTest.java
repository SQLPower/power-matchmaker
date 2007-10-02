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

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.MatchRuleSet;

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
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MatchRuleSet mungeProcess = new MatchRuleSet();

        a = new TestingMungeStep("A", 0, 1);
        mungeProcess.addChild(a);

        b = new TestingMungeStep("B", 1, 1);
        b.connectInput(0, a.getChildren().get(0));
        mungeProcess.addChild(b);

        c = new TestingMungeStep("C", 1, 2);
        c.connectInput(0, a.getChildren().get(0));
        mungeProcess.addChild(c);

        d = new TestingMungeStep("D", 1, 1);
        mungeProcess.addChild(d);

        e = new TestingMungeStep("E", 1, 0);
        e.connectInput(0, b.getChildren().get(0));
        mungeProcess.addChild(e);

        f = new TestingMungeStep("F", 3, 0);
        f.connectInput(0, a.getChildren().get(0));
        // purposely leaving input 1 not connected (for testing)
        f.connectInput(2, c.getChildren().get(0));
        mungeProcess.addChild(f);

        g = new TestingMungeStep("G", 1, 1);
        g.connectInput(0, c.getChildren().get(1));
        mungeProcess.addChild(g);

        mp = new MungeProcessorImpl(mungeProcess);
    }
    
    public void testCorrectProcessingOrder() throws Exception {
        mp.call();
        // need to check the steps were call()ed in the correct order
        fail("test not completed");
    }
}
