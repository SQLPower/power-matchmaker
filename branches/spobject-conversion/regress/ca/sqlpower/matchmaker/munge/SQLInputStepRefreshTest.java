/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.munge;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.AbstractRefreshTest;
import ca.sqlpower.matchmaker.munge.ConcatMungeStep;
import ca.sqlpower.matchmaker.munge.DateToStringMungeStep;
import ca.sqlpower.matchmaker.munge.DeDupeResultStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;

/**
 * Tests the ability of the SQLInputStep to refresh itself when the
 * columns of the SQLTable it represents have changed.
 */
public class SQLInputStepRefreshTest extends AbstractRefreshTest {

    private static final Logger logger = Logger.getLogger(SQLInputStepRefreshTest.class);
    
    /**
     * Tests that SQLInputStep notices when its source table loses a column, and
     * drops the corresponding output. In this particular test, the corresponding
     * output is not connected to anything.
     */
    public void testRefreshDisconnectedInputStepWithDroppedColumn() throws Exception {
        sqlx("ALTER TABLE customer DROP COLUMN name");
        db.refresh();
        
        inputStep.refresh(logger);
        
        assertEquals(2, inputStep.getChildren(MungeStepOutput.class).size());
        assertEquals("CUSTOMER_ID", inputStep.getChildren().get(0).getName());
        assertEquals("DOB", inputStep.getChildren().get(1).getName());
    }

    /**
     * Ensures the SQLInputStep picks up new columns in the source table
     * and adds the corresponding inputs.
     */
    public void testRefreshDisconnectedInputStepWithAddedColumn() throws Exception {
        sqlx("ALTER TABLE customer ADD COLUMN responsible_salesperson VARCHAR(42)");
        db.refresh();
        
        inputStep.refresh(logger);
        
        assertEquals(4, inputStep.getChildren(MungeStepOutput.class).size());
        assertEquals("CUSTOMER_ID", inputStep.getChildren().get(0).getName());
        assertEquals("NAME", inputStep.getChildren().get(1).getName());
        assertEquals("DOB", inputStep.getChildren().get(2).getName());
        assertEquals("RESPONSIBLE_SALESPERSON", inputStep.getChildren().get(3).getName());
    }
    
    /**
     * This ensures steps that the outputs of SQLInputStep are properly
     * disconnected when their corresponding source table columns go away. 
     */
    public void testRefreshConnectedInputStepWithDroppedColumn() throws Exception {
        MungeStepOutput<String> nameOutput = inputStep.getOutputByName("NAME");
        assertNotNull(nameOutput);
        
        ConcatMungeStep step = new ConcatMungeStep();
        mungeProcess.addChild(step);
        step.connectInput(0, nameOutput);
        assertSame(nameOutput, step.getInputs().get(0).getCurrent());
        
        sqlx("ALTER TABLE customer DROP COLUMN name");
        db.refresh();
        
        inputStep.refresh(logger);
        
        assertNull("Concat input is still connected to the NAME output, which has been removed from its parent step!",
                step.getInputs().get(0).getCurrent());

    }

    /**
     * This ensures steps update properly when their corresponding source table
     * column undergoes a change of type. In this test, the output is not connected
     * to anything.
     */
    public void testRefreshDisconnectedInputStepChangedType() throws Exception {
        MungeStepOutput<?> dobOutput = inputStep.getOutputByName("DOB");
        assertNotNull(dobOutput);
        
        sqlx("ALTER TABLE customer ALTER COLUMN dob VARCHAR(50)"); // strange idea, but nyeh
        db.refresh();
        
        inputStep.refresh(logger);
        
        assertFalse(inputStep.getChildren().contains(dobOutput));
        MungeStepOutput<?> newDobOutput = inputStep.getOutputByName("DOB");
        assertNotNull("Old String output still on SQL Input Step!", newDobOutput);
        assertEquals(String.class, newDobOutput.getType());
    }
    
    /**
     * This ensures steps update properly when their corresponding source table
     * columns undergo a change of type. In this case, the output for that column
     * is connected to an input that only accepts its old data type. We expect the
     * connection to be broken since the new type is incompatible.
     */
    public void testRefreshConnectedInputStepChangedTypeIncompatibly() throws Exception {
        MungeStepOutput<?> dobOutput = inputStep.getOutputByName("DOB");
        assertNotNull(dobOutput);
        DateToStringMungeStep step = new DateToStringMungeStep();
        mungeProcess.addChild(step);
        step.connectInput(0, dobOutput);
        
        sqlx("ALTER TABLE customer ALTER COLUMN dob VARCHAR(50)"); // strange idea, but nyeh
        db.refresh();
        
        inputStep.refresh(logger);
        
        assertNull(step.getInputs().get(0).getCurrent());
        assertFalse(inputStep.getChildren().contains(dobOutput));
        MungeStepOutput<?> newDobOutput = inputStep.getOutputByName("DOB");
        assertNotNull("Old String output still on SQL Input Step!", newDobOutput);
        assertEquals(String.class, newDobOutput.getType());
    }

    /**
     * This ensures steps update properly when their corresponding source table
     * columns undergo a change of type. In this case, the output for that
     * column is connected to an input that accepts any data type. We expect the
     * connection to remain since the new type is compatible.
     */
    public void testRefreshConnectedInputStepChangedTypeCompatibly() throws Exception {
        MungeStepOutput<?> dobOutput = inputStep.getOutputByName("DOB");
        assertNotNull(dobOutput);
        DeDupeResultStep step = new DeDupeResultStep();
        mungeProcess.addChild(step);
        step.connectInput(0, dobOutput);
        
        sqlx("ALTER TABLE customer ALTER COLUMN dob VARCHAR(50)"); // strange idea, but nyeh
        db.refresh();
        
        inputStep.refresh(logger);
        
        assertFalse(inputStep.getChildren().contains(dobOutput));
        MungeStepOutput<?> newDobOutput = inputStep.getOutputByName("DOB");
        assertNotNull("Old String output still on SQL Input Step!", newDobOutput);
        assertEquals(String.class, newDobOutput.getType());
        assertSame(newDobOutput, step.getInputs().get(0).getCurrent());
    }

}
