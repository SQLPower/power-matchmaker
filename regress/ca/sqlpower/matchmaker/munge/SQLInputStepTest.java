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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Types;
import java.util.Date;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SPDataSourceType;
import ca.sqlpower.testutil.MockJDBCConnection;
import ca.sqlpower.testutil.MockJDBCResultSet;

public class SQLInputStepTest extends TestCase {

	private final Logger logger = Logger.getLogger("testLogger");
	
    private class FakeDatabase extends SQLDatabase {
        
        private MockJDBCConnection con;

        public FakeDatabase() {
            String url = "jdbc:mock:tables=table1";
            con = new MockJDBCConnection(url, new Properties());
            
            MockJDBCResultSet rs = new MockJDBCResultSet(3);
            rs.addRow(new Object[] {"row1,1", new Integer(12), new Date(1234)});
            rs.addRow(new Object[] {null,     null,            null});
            rs.addRow(new Object[] {"row3,1", new Integer(32), new Date(5678)});
            
            con.registerResultSet("SELECT.*FROM table1.*", rs);
        }
        
        public Connection getConnection() {
            return con;
        }
        
        @Override
        public SPDataSource getDataSource() {
        	return new SPDataSource((DataSourceCollection)null) {
        		@Override
        		public SPDataSourceType getParentType() {
        			return new SPDataSourceType() {
        				@Override
        				public boolean getSupportsUpdateableResultSets() {
        					return true;
        				}
        			};
        		}
        	};
        };
    }
    
    SQLInputStep step;
    MungeResultStep resultStep;
    SQLDatabase db;
    Project project;
    MungeProcess process;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        db = new FakeDatabase();
        SQLTable table = new SQLTable(db, true);
        db.addChild(table);
        
        table.setName("table1");
        table.addColumn(new SQLColumn(table, "col1", Types.VARCHAR, 50, 0));
        table.addColumn(new SQLColumn(table, "col2", Types.INTEGER, 50, 0));
        table.addColumn(new SQLColumn(table, "col3", Types.TIMESTAMP, 50, 0));

        project = new Project();
        project.setType(getProjectType());
        project.setSourceTable(table);
        
        process = new MungeProcess();
        project.addMungeProcess(process);
        
        step = new SQLInputStep();
        process.addChild(step);
	    resultStep = (MungeResultStep) step.getOutputStep();
	    process.addChild(resultStep);
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
        step.rollback();
        step.close();
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
            step.close();
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
        step.commit();
        step.close();
    }
    
    public void testOutputsNullAtFirst() throws Exception {
        step.open(logger);
        assertNull(step.getChildren().get(0).getData());
        assertNull(step.getChildren().get(1).getData());
        assertNull(step.getChildren().get(2).getData());
        step.commit();
        step.close();
    }

    public void testOutputsNullAfterEnd() throws Exception {
        step.open(logger);
        while (step.call() == Boolean.TRUE);
        assertNull(step.getChildren().get(0).getData());
        assertNull(step.getChildren().get(1).getData());
        assertNull(step.getChildren().get(2).getData());
        step.commit();
        step.close();
    }

    public void testOutputsTrackResults() throws Exception {
        step.open(logger);
        step.call();
        assertEquals("row1,1",             step.getChildren().get(0).getData());
        assertEquals(new BigDecimal("12"), step.getChildren().get(1).getData());
        assertEquals(new Date(1234),       step.getChildren().get(2).getData());

        step.call();
        assertEquals(null, step.getChildren().get(0).getData());
        assertEquals(null, step.getChildren().get(1).getData());
        assertEquals(null, step.getChildren().get(2).getData());

        step.call();
        assertEquals("row3,1",             step.getChildren().get(0).getData());
        assertEquals(new BigDecimal("32"), step.getChildren().get(1).getData());
        assertEquals(new Date(5678),       step.getChildren().get(2).getData());
        
        step.commit();
        step.close();
    }
}
