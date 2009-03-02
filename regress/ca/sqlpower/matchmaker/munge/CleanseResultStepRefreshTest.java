/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.AbstractRefreshTest;
import ca.sqlpower.matchmaker.munge.CleanseResultStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.matchmaker.munge.StringConstantMungeStep;

public class CleanseResultStepRefreshTest extends AbstractRefreshTest {

    private static final Logger logger = Logger.getLogger(CleanseResultStepRefreshTest.class);
    
    /**
     * The result step that we're testing the refresh behaviour of.
     */
    private CleanseResultStep resultStep;
    
    /**
     * Sets up the cleanse result step.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        resultStep = new CleanseResultStep();
        mungeProcess.addChild(resultStep);
        resultStep.open(logger);
        resultStep.rollback();
        resultStep.close();
        
        assertEquals(3, resultStep.getInputCount());
    }
    
    public void testRefreshDisconnectedOutputStepWithDroppedColumn() throws Exception {
        sqlx("ALTER TABLE customer DROP COLUMN name");
        db.refresh();
        resultStep.open(logger);
        resultStep.rollback();
        resultStep.close();
        
        assertEquals(2, resultStep.getInputCount());
        assertEquals("CUSTOMER_ID", resultStep.getInputDescriptor(0).getName());
        assertEquals("DOB", resultStep.getInputDescriptor(1).getName());
    }

    public void testRefreshDisconnectedOutputStepWithAddedColumn() throws Exception {
        sqlx("ALTER TABLE customer ADD COLUMN eats_chitlins BOOLEAN");
        db.refresh();
        resultStep.open(logger);
        resultStep.rollback();
        resultStep.close();

        assertEquals(4, resultStep.getInputCount());
        assertEquals("CUSTOMER_ID", resultStep.getInputDescriptor(0).getName());
        assertEquals("NAME", resultStep.getInputDescriptor(1).getName());
        assertEquals("DOB", resultStep.getInputDescriptor(2).getName());
        assertEquals("EATS_CHITLINS", resultStep.getInputDescriptor(3).getName());
    }
    
    public void testRefreshConnectedOutputStepWithDroppedColumn() throws Exception {
        StringConstantMungeStep stringStep = new StringConstantMungeStep();
        mungeProcess.addChild(stringStep);
        MungeStepOutput connectedOutput = stringStep.getChildren().get(0);
        resultStep.connectInput(1, connectedOutput);
        assertSame(connectedOutput, resultStep.getInputs().get(1).getCurrent());
        
        sqlx("ALTER TABLE customer DROP COLUMN name");
        db.refresh();
        resultStep.open(logger);
        resultStep.rollback();
        resultStep.close();

        assertEquals(2, resultStep.getInputCount());
        assertEquals("CUSTOMER_ID", resultStep.getInputDescriptor(0).getName());
        assertEquals("DOB", resultStep.getInputDescriptor(1).getName());
        
        // nothing to check for disconnection since outputs have no reference to
        // the inputs they feed
    }

    public void testRefreshDisconnectedOutputStepChangedType() throws Exception {
        assertEquals(String.class, resultStep.getInputDescriptor(1).getType());
        
        sqlx("ALTER TABLE customer ALTER COLUMN name DECIMAL(10)");
        db.refresh();
        resultStep.open(logger);
        resultStep.rollback();
        resultStep.close();

        assertEquals(3, resultStep.getInputCount());
        assertEquals("CUSTOMER_ID", resultStep.getInputDescriptor(0).getName());
        assertEquals(BigDecimal.class, resultStep.getInputDescriptor(0).getType());
        assertEquals("NAME", resultStep.getInputDescriptor(1).getName());
        assertEquals(BigDecimal.class, resultStep.getInputDescriptor(1).getType());
        assertEquals("DOB", resultStep.getInputDescriptor(2).getName());
        assertEquals(Date.class, resultStep.getInputDescriptor(2).getType());
    }

    public void testRefreshConnectedOutputStepChangedTypeIncompatibly() throws Exception {
        assertEquals(String.class, resultStep.getInputDescriptor(1).getType());
        
        StringConstantMungeStep stringStep = new StringConstantMungeStep();
        mungeProcess.addChild(stringStep);
        MungeStepOutput connectedOutput = stringStep.getChildren().get(0);
        resultStep.connectInput(1, connectedOutput);
        assertSame(connectedOutput, resultStep.getInputs().get(1).getCurrent());

        sqlx("ALTER TABLE customer ALTER COLUMN name DECIMAL(10)");
        db.refresh();
        resultStep.open(logger);
        resultStep.rollback();
        resultStep.close();

        assertEquals(3, resultStep.getInputCount());
        assertEquals("NAME", resultStep.getInputDescriptor(1).getName());
        assertEquals(BigDecimal.class, resultStep.getInputDescriptor(1).getType());
        assertNull(resultStep.getInputs().get(1).getCurrent());
    }


}
