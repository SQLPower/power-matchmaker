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

package ca.sqlpower.matchmaker;

import java.sql.Types;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;

/**
 * A collection of tests for the Match class that concentrate on testing the helper
 * methods for coordinating the sourceTable property with the sourceTableName, sourceTableCatalog,
 * and sourceTableSchema properties.  All that behaviour would be better off in a Hibernate
 * user type, but we couldn't get that working, so we moved the logic into the business model.
 * Note that it doesn't depend on Hibernate in any way; it's just that the Hibernate mappings
 * are the only part of the application that use this functionality. 
 * 
 * <p>Note, the testLocateExisting tests are actually verifying that SQLDatabase.getTableByName() works
 * properly, and the testCreateNewX tests are verifying ArchitectUtils.addSimulatedTable() works.
 * This isn't an ideal unit test, but it's the best we can do right now.
 */
public class MatchSQLTableHelperTest extends TestCase {

    TestingMatchMakerSession session;
    Match match;
    
    protected void setUp() throws Exception {
        super.setUp();
        session = new TestingMatchMakerSession();
        session.setDatabase(new SQLDatabase());
        match = new Match();
        match.setSession(session);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }


    /** Creates a table directly under the session's database. */
    private SQLTable setUpScenario1() throws ArchitectException {
        SQLDatabase db = session.getDatabase();
        SQLTable table = new SQLTable(db, "table1", null, "TABLE", true);
        db.addChild(table);
        SQLColumn col = new SQLColumn(table, "you found it", Types.CHAR, 50, 0);
        table.addColumn(col);
        return table;
    }
    
    /** Creates a schema with a table in it directly under the session's database. */
    private SQLTable setUpScenario2() throws ArchitectException {
        SQLDatabase db = session.getDatabase();
        SQLSchema sch = new SQLSchema(db, "schema2", true);
        db.addChild(sch);
        SQLTable table = new SQLTable(sch, "table2", null, "TABLE", true);
        sch.addChild(table);
        SQLColumn col = new SQLColumn(table, "you found it2", Types.CHAR, 50, 0);
        table.addColumn(col);
        return table;
    }
    
    /** Creates a catalog with a table in it directly under the session's database. */
    private SQLTable setUpScenario3() throws ArchitectException {
        SQLDatabase db = session.getDatabase();
        SQLCatalog cat = new SQLCatalog(db, "catalog3", true);
        db.addChild(cat);
        SQLTable table = new SQLTable(cat, "table3", null, "TABLE", true);
        cat.addChild(table);
        SQLColumn col = new SQLColumn(table, "you found it3", Types.CHAR, 50, 0);
        table.addColumn(col);
        return table;
    }
    
    /** Creates a catalog containing a schema containing a table under the session's database. */
    private SQLTable setUpScenario4() throws ArchitectException {
        SQLDatabase db = session.getDatabase();
        SQLCatalog cat = new SQLCatalog(db, "catalog4", true);
        db.addChild(cat);
        SQLSchema sch = new SQLSchema(cat, "schema4", true);
        cat.addChild(sch);
        SQLTable table = new SQLTable(sch, "table4", null, "TABLE", true);
        sch.addChild(table);
        SQLColumn col = new SQLColumn(table, "you found it4", Types.CHAR, 50, 0);
        table.addColumn(col);
        return table;
    }
    
    
    ////////// Test Cases Start Here //////////
    
    /** Tests that the helper can locate an existing table directly under the database. */
    public void testLocateExisting1() throws Exception {
        SQLTable table = setUpScenario1();
        
        match.setSourceTableCatalog(null);
        match.setSourceTableSchema(null);
        match.setSourceTableName("table1");

        assertNull(match.getSourceTableCatalog());
        assertNull(match.getSourceTableSchema());
        assertEquals("table1", match.getSourceTableName());

        SQLTable found = match.getSourceTable();
        assertNotNull(found);
        assertEquals("you found it", found.getColumn(0).getName());
        assertSame(table, found);

        // tests different behaviour than the previous identical assertions
        // (retrieving this information from the cachedSourceTable vs. the plain properties)
        assertNull(match.getSourceTableCatalog());
        assertNull(match.getSourceTableSchema());
        assertEquals("table1", match.getSourceTableName());
    }

    /** Tests that the helper can locate an existing table inside a schema directly under the database. */
    public void testLocateExisting2() throws Exception {
        SQLTable table = setUpScenario2();
        
        match.setSourceTableCatalog(null);
        match.setSourceTableSchema("schema2");
        match.setSourceTableName("table2");

        assertNull(match.getSourceTableCatalog());
        assertEquals("schema2", match.getSourceTableSchema());
        assertEquals("table2", match.getSourceTableName());

        SQLTable found = match.getSourceTable();
        assertNotNull(found);
        assertEquals("you found it2", found.getColumn(0).getName());
        assertSame(table, found);
        
        // tests different behaviour than the previous identical assertions
        // (retrieving this information from the cachedSourceTable vs. the plain properties)
        assertNull(match.getSourceTableCatalog());
        assertEquals("schema2", match.getSourceTableSchema());
        assertEquals("table2", match.getSourceTableName());
    }

    /** Tests that the helper can locate an existing table directly inside a catalog under the database. */
    public void testLocateExisting3() throws Exception {
        SQLTable table = setUpScenario3();
        
        match.setSourceTableCatalog("catalog3");
        match.setSourceTableSchema(null);
        match.setSourceTableName("table3");

        assertEquals("catalog3", match.getSourceTableCatalog());
        assertNull(match.getSourceTableSchema());
        assertEquals("table3", match.getSourceTableName());

        SQLTable found = match.getSourceTable();
        assertNotNull(found);
        assertEquals("you found it3", found.getColumn(0).getName());
        assertSame(table, found);
        
        // tests different behaviour than the previous identical assertions
        // (retrieving this information from the cachedSourceTable vs. the plain properties)
        assertEquals("catalog3", match.getSourceTableCatalog());
        assertNull(match.getSourceTableSchema());
        assertEquals("table3", match.getSourceTableName());
    }

    /** Tests that the helper can locate an existing table nested inside a database, catalog, and schema. */
    public void testLocateExisting4() throws Exception {
        SQLTable table = setUpScenario4();
        
        match.setSourceTableCatalog("catalog4");
        match.setSourceTableSchema("schema4");
        match.setSourceTableName("table4");
        
        assertEquals("catalog4", match.getSourceTableCatalog());
        assertEquals("schema4", match.getSourceTableSchema());
        assertEquals("table4", match.getSourceTableName());

        SQLTable found = match.getSourceTable();
        assertNotNull(found);
        assertEquals("you found it4", found.getColumn(0).getName());
        assertSame(table, found);
        
        // tests different behaviour than the previous identical assertions
        // (retrieving this information from the cachedSourceTable vs. the plain properties)
        assertEquals("catalog4", match.getSourceTableCatalog());
        assertEquals("schema4", match.getSourceTableSchema());
        assertEquals("table4", match.getSourceTableName());
    }

    public void testCreateNew1() throws Exception {
        match.setSourceTableCatalog(null);
        match.setSourceTableSchema(null);
        match.setSourceTableName("table1");

        SQLTable created = match.getSourceTable();
        assertNotNull(created);
        assertEquals("table1", created.getName());
    }

    public void testCreateNew2() throws Exception {
        match.setSourceTableCatalog(null);
        match.setSourceTableSchema("schema2");
        match.setSourceTableName("table2");

        SQLTable created = match.getSourceTable();
        assertNotNull(created);
        assertEquals("table2", created.getName());
    }

    public void testCreateNew3() throws Exception {
        match.setSourceTableCatalog("catalog3");
        match.setSourceTableSchema(null);
        match.setSourceTableName("table3");

        SQLTable created = match.getSourceTable();
        assertNotNull(created);
        assertEquals("table3", created.getName());
    }

    public void testCreateNew4() throws Exception {
        match.setSourceTableCatalog("catalog4");
        match.setSourceTableSchema("schema4");
        match.setSourceTableName("table4");

        SQLTable created = match.getSourceTable();
        assertNotNull(created);
        assertEquals("table4", created.getName());
    }

    public void testSetSourceTable1() throws Exception {
        SQLTable table = setUpScenario1();
        
        match.setSourceTable(table);

        assertNull(match.getSourceTableCatalog());
        assertNull(match.getSourceTableSchema());
        assertEquals("table1", match.getSourceTableName());
    }

    public void testSetSourceTable2() throws Exception {
        SQLTable table = setUpScenario2();
        
        match.setSourceTable(table);

        assertNull(match.getSourceTableCatalog());
        assertEquals("schema2", match.getSourceTableSchema());
        assertEquals("table2", match.getSourceTableName());
    }

    public void testSetSourceTable3() throws Exception {
        SQLTable table = setUpScenario3();
        
        match.setSourceTable(table);

        assertEquals("catalog3", match.getSourceTableCatalog());
        assertNull(match.getSourceTableSchema());
        assertEquals("table3", match.getSourceTableName());
    }

    public void testSetSourceTable4() throws Exception {
        SQLTable table = setUpScenario4();
        
        match.setSourceTable(table);

        assertEquals("catalog4", match.getSourceTableCatalog());
        assertEquals("schema4", match.getSourceTableSchema());
        assertEquals("table4", match.getSourceTableName());
    }
    
    
    /**
     * The metadata for catalog, schema, table might contain bogus hierarchy
     * information (for example, null catalog and schema on SQLServer).  This
     * should generate a warning and then set the table to null.
     * <p>
     * This is only a representative test, not exhaustive like the createNew and setSourceTable
     * families above.  It should be sufficient though.
     */
    public void testBadHierarchy() throws Exception {
        setUpScenario3();
        
        /* scenario 3 has catalogs under the database, so this table is specified in an illegal location */ 
        match.setSourceTableCatalog(null);
        match.setSourceTableSchema(null);
        match.setSourceTableName("bad_hierarchy");
        
        assertEquals("There should be no warnings in the session to start with",
                0, session.getWarnings().size());

        assertNull(
                "the table should not have been created, because location is illegal",
                match.getSourceTable());
        
        assertEquals("Should have generated a warning",
                1, session.getWarnings().size());

    }
}