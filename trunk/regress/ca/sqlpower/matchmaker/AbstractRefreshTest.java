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

package ca.sqlpower.matchmaker;

import java.io.IOException;
import java.sql.Connection;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.SQLInputStep;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.DatabaseConnectedTestCase;
import ca.sqlpower.sqlobject.SQLTable;

/**
 * Provides a setup for junit test classes that test the ability for a specific type
 * of MatchMaker object to cope with an update to the SQLObjects that underlie them.
 * This is currently more of an end-to-end functional test than it needs to be:
 * the SQLObjects are actually populated from an HSQLDB table, which is then in turn
 * modified before refreshing the SQLObjects. It would have been possible to set this
 * as more of a unit test, where the HSQLDB doesn't exist, and the SQLObjects are crafted
 * and modified directly via their API.
 */
public abstract class AbstractRefreshTest extends DatabaseConnectedTestCase {

    private static final Logger logger = Logger.getLogger(AbstractRefreshTest.class);
    
    /**
     * The session everything belongs to. Created in {@link #setUp()}.
     */
    protected TestingMatchMakerSession session;

    /**
     * The project that holds the munge process. Belongs to {@link #session}.
     * Created in {@link #setUp()}.
     */
    protected Project p;

    /**
     * The sole munge process of {@link #p}. Has one munge step child:
     * {@link #inputStep}.
     */
    protected MungeProcess mungeProcess;
    
    /**
     * The input step, which has already been linked to the SQLTable called CUSTOMER.
     * The setUp method also opens and closes this step, so its outputs correspond
     * with the columns of CUSTOMER at the time your test method is invoked.
     */
    protected SQLInputStep inputStep;

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
}
