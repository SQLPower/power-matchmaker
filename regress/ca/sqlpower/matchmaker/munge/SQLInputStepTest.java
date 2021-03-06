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

package ca.sqlpower.matchmaker.munge;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Date;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerTestCase;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.TestingMatchMakerSession;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.testutil.MockJDBCConnection;
import ca.sqlpower.testutil.MockJDBCResultSet;
import ca.sqlpower.util.FakeSQLDatabase;

public class SQLInputStepTest extends MatchMakerTestCase<SQLInputStep> {

	private final Logger logger = Logger.getLogger("testLogger");
	
    SQLInputStep step;
    MungeResultStep resultStep;
    FakeSQLDatabase db;
    Project project;
    MungeProcess process;

	public SQLInputStepTest(String name) {
		super(name);
	}
 
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        db = new FakeSQLDatabase("jdbc:mock:tables=table1");
        
        MockJDBCResultSet rs = new MockJDBCResultSet(3);
        rs.addRow(new Object[] {"row1,1", new Integer(12), new Date(1234)});
        rs.addRow(new Object[] {null,     null,            null});
        rs.addRow(new Object[] {"row3,1", new Integer(32), new Date(5678)});
        
        MockJDBCConnection con = db.getConnection();
        con.registerResultSet("SELECT.*FROM table1.*", rs);

        SQLTable table = new SQLTable(db, true);
        db.addChild(table);
        
        table.setName("table1");
        table.addColumn(new SQLColumn(table, "col1", Types.VARCHAR, 50, 0));
        table.addColumn(new SQLColumn(table, "col2", Types.INTEGER, 50, 0));
        table.addColumn(new SQLColumn(table, "col3", Types.TIMESTAMP, 50, 0));

        project = new Project();
        project.setType(getProjectType());
        project.setSourceTable(table);
        project.setSession(new TestingMatchMakerSession());
        
        process = new MungeProcess();
        project.addChild(process);
        
        step = new SQLInputStep();
        process.addChild(step);
	    resultStep = (MungeResultStep) step.getOutputStep();
	    process.addChild(resultStep);
	    getRootObject().addChild(project, 0);
    }
    
    protected ProjectMode getProjectType() {
		return ProjectMode.FIND_DUPES;
	}

	public void testNoDoubleOpen() throws Exception {
        step.open(logger);
        try {
            step.open(logger);
            fail("Opened step twice (should not be possible)");
        } catch (IllegalStateException ex) {
            // good
        }
        step.mungeRollback();
        step.mungeClose();
    }
    
    public void testNoCallWhenNotYetOpen() throws Exception {
        try {
            step.call();
            fail("call() succeeded when the step was not yet open");
        } catch (IllegalStateException ex) {
            // good
        }
    }

    public void testNoCallAfterClose() throws Exception {
        try {
            step.open(logger);
            step.mungeClose();
            step.call();
            fail("call() succeeded after the step was closed");
        } catch (IllegalStateException ex) {
            // good
        }
    }

    public void testCallReturnsCorrectValues() throws Exception {
        step.open(logger);
        assertTrue(step.call());
        assertTrue(step.call());
        assertTrue(step.call());
        assertFalse(step.call());
        step.mungeCommit();
        step.mungeClose();
    }
    
    public void testOutputsNullAtFirst() throws Exception {
        step.open(logger);
        assertNull(step.getMungeStepOutputs().get(0).getData());
        assertNull(step.getMungeStepOutputs().get(1).getData());
        assertNull(step.getMungeStepOutputs().get(2).getData());
        step.mungeCommit();
        step.mungeClose();
    }

    public void testOutputsNullAfterEnd() throws Exception {
        step.open(logger);
        while (step.call() == Boolean.TRUE);
        assertNull(step.getMungeStepOutputs().get(0).getData());
        assertNull(step.getMungeStepOutputs().get(1).getData());
        assertNull(step.getMungeStepOutputs().get(2).getData());
        step.mungeCommit();
        step.mungeClose();
    }

    public void testOutputsTrackResults() throws Exception {
        step.open(logger);
        step.call();
        assertEquals("row1,1",             step.getMungeStepOutputs().get(0).getData());
        assertEquals(new BigDecimal("12"), step.getMungeStepOutputs().get(1).getData());
        assertEquals(new Date(1234),       step.getMungeStepOutputs().get(2).getData());

        step.call();
        assertEquals(null, step.getMungeStepOutputs().get(0).getData());
        assertEquals(null, step.getMungeStepOutputs().get(1).getData());
        assertEquals(null, step.getMungeStepOutputs().get(2).getData());

        step.call();
        assertEquals("row3,1",             step.getMungeStepOutputs().get(0).getData());
        assertEquals(new BigDecimal("32"), step.getMungeStepOutputs().get(1).getData());
        assertEquals(new Date(5678),       step.getMungeStepOutputs().get(2).getData());
        
        step.mungeCommit();
        step.mungeClose();
    }

	@Override
	protected SQLInputStep getTarget() {
		return step;
	}

	@Override
	protected Class<? extends SPObject> getChildClassType() {
		return MungeStepOutput.class;
	}
	
	@Override
	public void testAllowedChildTypesField() throws Exception {
		// already in AbstractMungeStep
	}
	
	@Override
	public void testDuplicate() throws Exception {
		// Do nothing
	}
}
