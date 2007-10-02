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

/**
 * A simple munge step implementation that can be created with any number of inputs
 * and outputs.  The outputs are always null, and the <tt>call</tt> method does
 * nothing.
 */
public class TestingMungeStep extends AbstractMungeStep {

    /**
     * Creates a new instance with the given name, number of input slots, 
     * and number of outputs (which are children of the step from the tree
     * model point of view).  All inputs and outputs are of type String.
     * 
     * @param name This step's name
     * @param inputs The number of input slots for this step. All slots will
     * be initially not connected.
     * @param outputs The number of outputs for this step
     */
    public TestingMungeStep(String name, int inputs, int outputs) {
        setName(name);
        for (int i = 0; i < inputs; i++) {
            addInput(new InputDescriptor("input_" + i, String.class));
        }
        for (int i = 0; i < outputs; i++) {
            addChild(new MungeStepOutput<String>("output_"+i, String.class));
        }
    }
    
    /**
     * Returns false.
     */
    public boolean canAddInput() {
        return false;
    }
    
}
