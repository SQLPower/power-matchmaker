/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru.
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

package ca.sqlpower.matchmaker.dao.xml;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TestingMatchMakerContext;
import ca.sqlpower.matchmaker.swingui.StubMatchMakerSession;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.testutil.MockJDBCConnection;
import ca.sqlpower.testutil.MockJDBCResultSet;
import ca.sqlpower.testutil.MockJDBCResultSetMetaData;
import ca.sqlpower.util.FakeSQLDatabase;

public class ProjectXMLDAOTest extends TestCase {

    private MatchMakerSession session;

    private FakeSQLDatabase db;
    private TestingMatchMakerContext context;
    private PlDotIni plIni;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        db = new FakeSQLDatabase(
                "jdbc:mock:" +
                "dbmd.catalogTerm=Catalog" +
                "&dbmd.schemaTerm=Schema" +
                "&catalogs=cat" +
                "&schemas.cat=schem" +
                "&tables.cat.schem=match_table,result_table" +
                "&columns.cat.schem.match_table=pk,string_col,number_col,date_col,bool_col");

        SQLTable matchTable = db.getTableByName("cat", "schem", "match_table");
        matchTable.getColumnByName("pk").setType(Types.INTEGER);
        matchTable.getColumnByName("string_col").setType(Types.VARCHAR);
        matchTable.getColumnByName("number_col").setType(Types.NUMERIC);
        matchTable.getColumnByName("date_col").setType(Types.TIMESTAMP);
        matchTable.getColumnByName("bool_col").setType(Types.BOOLEAN);
        
        MockJDBCConnection con = db.getConnection();
        MockJDBCResultSet rs = new MockJDBCResultSet(5);
        rs.addRow(new Object[] { 1, "string value", 100.0, new Timestamp(1234), false });
        MockJDBCResultSetMetaData rsmd = rs.getMetaData();
        rsmd.setColumnName(1, "pk");
        rsmd.setColumnType(1, Types.INTEGER);
        
        rsmd.setColumnName(2, "string_col");
        rsmd.setColumnType(2, Types.VARCHAR);
        
        rsmd.setColumnName(3, "number_col");
        rsmd.setColumnType(3, Types.NUMERIC);
        
        rsmd.setColumnName(4, "date_col");
        rsmd.setColumnType(4, Types.TIMESTAMP);

        rsmd.setColumnName(5, "bool_col");
        rsmd.setColumnType(5, Types.BOOLEAN);

        con.registerResultSet("SELECT.*FROM cat.schem.match_table.*", rs);
        
        plIni = new PlDotIni();
        plIni.addDataSource(db.getDataSource());
        context = new TestingMatchMakerContext() {
            @Override
            public List<JDBCDataSource> getDataSources() {
                return getPlDotIni().getConnections();
            }
            
            @Override
            public DataSourceCollection getPlDotIni() {
                return plIni;
            }
        };
        session = new StubMatchMakerSession() {
            @Override
            public MatchMakerSessionContext getContext() {
                return context;
            }
            @Override
            public SQLDatabase getDatabase() {
                return db;
            }
            @Override
            public SQLDatabase getDatabase(JDBCDataSource dataSource) {
                return db;
            }
        };
    }

    /**
     * This was an absolutely HUGE test case that tried to create a project which
     * exercised every feature of the MatchMaker. Now, it simply opens a document
     * from an XML file and performs a few checks to ensure things are properly
     * loaded. When import and export functionality is fully restored, this should
     * be replaced by a more comprehensive test.
     */
    public void testSaveAndLoad() throws Exception {
        
    	FileInputStream filein = new FileInputStream("testbed/XMLImportTestFile.xml");
        ProjectDAOXML daoIn = new ProjectDAOXML(session, filein);
        Project readBack = daoIn.findAll().get(0);
        
        assertNotNull(readBack.getSession());

        assertEquals("Name was not properly persisted or objects are parented incorrectly",
        		"Concat",readBack.getMungeProcesses().get(0).getMungeSteps().get(0).getName());
        assertNotNull(readBack.getMungeProcesses().get(0).getName());
        
        assertEquals("Step connections were not properly set up.",
        		readBack.getMungeProcesses().get(0).getInputSteps().get(0).getMungeStepOutputs().get(1),
        		readBack.getMungeProcesses().get(0).getMungeSteps().get(0).getMSOInputs().get(0));
        
    }
    
    
    public void testReadNewerVersion() throws Exception {
        String xml = "<?xml version=\"1.0\"?><matchmaker-projects export-format=\"1.2.0\"/>";
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes());
        ProjectDAOXML xmldao = new ProjectDAOXML(session, in);
        try {
            xmldao.findAll();
            fail("Project file is newer format than we support--should have got exception");
        } catch (RuntimeException ex) {
            // good, this should fail
        }
    }
}
