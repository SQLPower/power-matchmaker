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

package ca.sqlpower.matchmaker.util;

import java.io.IOException;
import java.sql.Connection;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.DBTestUtil;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TestingMatchMakerSession;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.munge.ConcatMungeStep;
import ca.sqlpower.matchmaker.munge.DateToStringMungeStep;
import ca.sqlpower.matchmaker.munge.DeDupeResultStep;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.matchmaker.munge.SQLInputStep;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.DatabaseConnectedTestCase;
import ca.sqlpower.sqlobject.SQLTable;

public class ProjectRefreshTest extends DatabaseConnectedTestCase {

    private static final Logger logger = Logger.getLogger(ProjectRefreshTest.class);
    
    private TestingMatchMakerSession session;
    private Project p;

    private SQLInputStep inputStep;

    private MungeProcess mungeProcess;
    
    @Override
    protected SPDataSource getDataSource() throws IOException {
        // for performance reasons, most MatchMaker tests do not start off with
        // a clean, empty schema. They reuse the same connection.
        // this test wants to start fresh with every test method, so we have to
        // use a slightly different connection spec.
        SPDataSource ds = new SPDataSource(DBTestUtil.getHSQLDBInMemoryDS());
        ds.setUrl("jdbc:hsqldb:mem:refresh?shutdown=true");
        return ds;
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        sqlx(   "CREATE TABLE public.customer (" +
                " customer_id INT NOT NULL," +
                " name VARCHAR(40)," +
                " dob DATE," +
                " CONSTRAINT customer_pk PRIMARY KEY (customer_id))");
        
        SQLTable customerTable = db.getTableByName(null, "PUBLIC", "CUSTOMER");
        session = new TestingMatchMakerSession() {
            @Override
            public Connection getConnection() {
                try {
                    return db.getConnection();
                } catch (Exception ex) {
                    throw new RuntimeException();
                }
            }
        };
        session.setDatabase(db);
        
        p = new Project();
        p.setSession(session);
        p.setType(ProjectMode.FIND_DUPES);
        p.setName("Testing Project");
        p.setSourceTable(customerTable);
        p.setSourceTableIndex(customerTable.getPrimaryKeyIndex());
        
        mungeProcess = new MungeProcess();
        mungeProcess.setName("Simple");
        p.addMungeProcess(mungeProcess);
        inputStep = new SQLInputStep();
        mungeProcess.addChild(inputStep);
        inputStep.open(logger);
        inputStep.rollback();
        inputStep.close();
        
        assertEquals(3, inputStep.getChildCount());
        assertEquals("CUSTOMER_ID", inputStep.getChildren().get(0).getName());
        assertEquals("NAME", inputStep.getChildren().get(1).getName());
        assertEquals("DOB", inputStep.getChildren().get(2).getName());
        
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    /**
     * Tests that SQLInputStep notices when its source table loses a column, and
     * drops the corresponding output. In this particular test, the corresponding
     * output is not connected to anything.
     */
    public void testRefreshDisconnectedInputStepWithDroppedColumn() throws Exception {
        sqlx("ALTER TABLE customer DROP COLUMN name");
        db.refresh();
        
        inputStep.open(logger);
        inputStep.rollback();
        inputStep.close();
        
        assertEquals(2, inputStep.getChildCount());
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
        
        inputStep.open(logger);
        inputStep.rollback();
        inputStep.close();
        
        assertEquals(4, inputStep.getChildCount());
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
        
        inputStep.open(logger);
        inputStep.rollback();
        inputStep.close();
        
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
        
        inputStep.open(logger);
        inputStep.rollback();
        inputStep.close();
        
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
        
        inputStep.open(logger);
        inputStep.rollback();
        inputStep.close();
        
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
        
        inputStep.open(logger);
        inputStep.rollback();
        inputStep.close();
        
        assertFalse(inputStep.getChildren().contains(dobOutput));
        MungeStepOutput<?> newDobOutput = inputStep.getOutputByName("DOB");
        assertNotNull("Old String output still on SQL Input Step!", newDobOutput);
        assertEquals(String.class, newDobOutput.getType());
        assertSame(newDobOutput, step.getInputs().get(0).getCurrent());
    }

}
