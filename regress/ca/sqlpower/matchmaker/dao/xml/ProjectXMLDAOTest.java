/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.dao.xml;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.util.FakeSQLDatabase;
import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.MatchMakerFolder;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.TestingMatchMakerContext;
import ca.sqlpower.matchmaker.ColumnMergeRules.MergeActionType;
import ca.sqlpower.matchmaker.dao.AbstractDAOTestCase;
import ca.sqlpower.matchmaker.munge.ConcatMungeStep;
import ca.sqlpower.matchmaker.munge.InputDescriptor;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.matchmaker.munge.SQLInputStep;
import ca.sqlpower.matchmaker.swingui.StubMatchMakerSession;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.testutil.MockJDBCConnection;
import ca.sqlpower.testutil.MockJDBCResultSet;
import ca.sqlpower.testutil.MockJDBCResultSetMetaData;

public class ProjectXMLDAOTest extends TestCase {

    private static final Logger logger = Logger.getLogger(ProjectDAOXML.class);
    
    private ProjectDAOXML daoOut;
    private ByteArrayOutputStream out;

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
            public List<SPDataSource> getDataSources() {
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
            public SQLDatabase getDatabase(SPDataSource dataSource) {
                return db;
            }
        };

        out = new ByteArrayOutputStream();
        daoOut = new ProjectDAOXML(out);
    }

    /**
     * This is an absolutely HUGE test case that tries to create a project which
     * exercises every feature of the MatchMaker. To do this, it uses Java
     * reflection to discover all the read/write bean properties on all objects
     * involved, and makes up new values for them. This ensures no properties
     * are left at their default values.
     * <p>
     * Once a comprehensive project is set up, we save it to an XML document and
     * immediately read that document back in. Then we proceed to compare all
     * the readable bean properties between the original version we saved and
     * the new copy we loaded back in.
     * <p>
     * The reason for all the reflective access is that we intend that this test
     * case should fail whenever a new property is added to any
     * MatchMakerObject, until the developer has either updated the XML
     * persistence code to handle it, or has decided the property should be
     * transient and adds it to the ignore list.
     */
    public void testSaveAndLoad() throws Exception {
        Project p = new Project();
        p.setSession(session);
        AbstractDAOTestCase.setAllSetters(session, p, getPropertiesToIgnore());

        
        p.setSourceTableSPDatasource(db.getDataSource().getName());
        p.setSourceTableCatalog("cat");
        p.setSourceTableSchema("schem");
        p.setSourceTableName("match_table");

        p.setResultTableSPDatasource(db.getDataSource().getName());
        p.setResultTableCatalog("cat");
        p.setResultTableSchema("schem");
        p.setResultTableName("result_table");

        p.setXrefTable(null);
        
        SQLTable sourceTable = p.getSourceTable();
        sourceTable.getColumn(0).setPrimaryKeySeq(0);
        p.setSourceTableIndex(sourceTable.getPrimaryKeyIndex());
        
        MungeProcess mp = new MungeProcess();
        AbstractDAOTestCase.setAllSetters(session, mp, getPropertiesToIgnore());
        p.addMungeProcess(mp);
        
        SQLInputStep inputStep = new SQLInputStep();
        mp.addChild(inputStep);
        inputStep.setParameter("my_boolean_value", true);
        inputStep.setParameter("my_numeric_value", 1234);
        inputStep.setParameter("my_string_value", "farnsworth");
        inputStep.open(logger); // this allows the step to grab the column information from the source table
        inputStep.rollback();
        inputStep.close();
        
        MungeStep step = new ConcatMungeStep();
        mp.addChild(step);
        step.connectInput(0, inputStep.getChildren().get(1)); // string_col
        
        TableMergeRules tmr = new TableMergeRules();
        tmr.setTable(p.getSourceTable());
        tmr.setTableIndex(p.getSourceTableIndex());
        int actionTypeIndex = 0;
        for (SQLColumn col : sourceTable.getColumns()) {
            ColumnMergeRules cmr = new ColumnMergeRules();
            cmr.setColumn(col);
            cmr.setActionType(MergeActionType.values()[actionTypeIndex % MergeActionType.values().length]);
            actionTypeIndex++;
            tmr.addChild(cmr);
        }
        tmr.getChildren().get(0).setUpdateStatement("test update statement");
        AbstractDAOTestCase.setAllSetters(session, tmr.getChildren().get(1), getPropertiesToIgnore());
        p.addTableMergeRule(tmr);
        
        tmr = new TableMergeRules();
        AbstractDAOTestCase.setAllSetters(session, tmr, getPropertiesToIgnore());
        tmr.setSpDataSource(db.getDataSource().getName());
        tmr.setCatalogName("cat");
        tmr.setSchemaName("schem");
        tmr.setTableName("fake_table_to_merge");
        tmr.setParentMergeRule(p.getTableMergeRules().get(0));
        p.getChildren().get(1).addChild(0, tmr); // putting child before parent on purpose to test that ordering is not sensitive to this
        
        // ========================= Now the save and load =========================
        
        daoOut.save(p);
        System.out.println(out.toString("utf-8"));
        
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ProjectDAOXML daoIn = new ProjectDAOXML(session, in);
        Project readBack = daoIn.findAll().get(0);
        
        assertNotNull(readBack.getSession());
        assertPropertiesEqual(p, readBack, "parent", "view");
        
        for (MungeProcess originalProcess : p.getMungeProcesses()) {
            MungeProcess readBackProcess = readBack.getMungeProcessByName(originalProcess.getName());
            assertNotNull(readBackProcess);
            assertPropertiesEqual(originalProcess, readBackProcess);
            
            for (int i = 0; i < originalProcess.getChildCount(); i++) {
                MungeStep originalStep = originalProcess.getChildren().get(i);
                MungeStep readBackStep = readBackProcess.getChildren().get(i);
                assertPropertiesEqual(originalStep, readBackStep, "project", "rolledBack", "inputs");
                
                for (int j = 0; j < originalStep.getChildCount(); j++) {
                    MungeStepOutput originalOutput = originalStep.getChildren().get(j);
                    MungeStepOutput readBackOutput = readBackStep.getChildren().get(j);
                    assertPropertiesEqual(originalOutput, readBackOutput, "parent");
                }

                for (int j = 0; j < originalStep.getInputCount(); j++) {
                    InputDescriptor originalInput = originalStep.getInputDescriptor(j);
                    InputDescriptor readBackInput = readBackStep.getInputDescriptor(j);
                    assertPropertiesEqual(originalInput, readBackInput);
                }
                
                for (String paramName : originalStep.getParameterNames()) {
                    assertEquals("Parameter \""+paramName+"\" differs after saving and loading back",
                            originalStep.getParameter(paramName), readBackStep.getParameter(paramName));
                }
            }
        }
        
        MatchMakerFolder<TableMergeRules> originalMergeRulesFolder = p.getChildren().get(1);
        MatchMakerFolder<TableMergeRules> readBackMergeRulesFolder = readBack.getChildren().get(1);
        for (int i = 0; i < originalMergeRulesFolder.getChildCount(); i++) {
            TableMergeRules originalTMR = originalMergeRulesFolder.getChildren().get(i);
            TableMergeRules readBackTMR = readBackMergeRulesFolder.getChildren().get(i);
            assertPropertiesEqual(originalTMR, readBackTMR);
            
            for (int j = 0; j < originalTMR.getChildCount(); j++) {
                ColumnMergeRules originalCMR = originalTMR.getChildren().get(j);
                ColumnMergeRules readBackCMR = readBackTMR.getChildren().get(j);
                assertPropertiesEqual(originalCMR, readBackCMR);
            }
        }
    }
    
    /**
     * Returns a list of property names that are not expected to maintain their
     * values when saved and loaded back.
     */
    public List<String> getPropertiesToIgnore(){
        ArrayList<String> ignore = new ArrayList<String>();
        ignore.add("oid");
        ignore.add("session");
        ignore.add("undoing");
        ignore.add("children");
        ignore.add("lastUpdateDate");
        ignore.add("parent");
        
        // Project things
        ignore.add("mungeProcesses");
        ignore.add("tableMergeRules");
        ignore.add("cleansingEngine");
        ignore.add("matchingEngine");
        ignore.add("mergingEngine");
        ignore.add("addressCorrectionEngine");
        ignore.add("sourceTable");
        ignore.add("resultTable");
        ignore.add("xrefTable");
        ignore.add("engineRunning");
        
        // MungeProcess things
        ignore.add("parentProject");
        ignore.add("results");
        
        // ColumnMergeRules things
        ignore.add("importedKeyColumn");
        
        return ignore;
    }
    
    private void assertPropertiesEqual(
            Object expected,
            Object actual,
            String ... additionalPropertiesToIgnore)
        throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        
        List<PropertyDescriptor> properties;
        properties = Arrays.asList(PropertyUtils.getPropertyDescriptors(expected.getClass()));

        // list all the readable properties
        List<PropertyDescriptor> gettableProperties = new ArrayList<PropertyDescriptor>();
        for (PropertyDescriptor d: properties){
            if( d.getReadMethod() != null ) {
                gettableProperties.add(d);
            }
        }

        // compare the values of each readable property
        Set<String> ignore = new HashSet<String>(getPropertiesToIgnore());
        ignore.addAll(Arrays.asList(additionalPropertiesToIgnore));
        for (PropertyDescriptor d: gettableProperties){
            if (!ignore.contains(d.getName())) {
                try {
                    Object old = BeanUtils.getSimpleProperty(expected, d.getName());
                    Object newItem = BeanUtils.getSimpleProperty(actual, d.getName());
                    assertEquals(
                            "The property "+d.getName() +" was not persisted for type "+expected.getClass().getName(),
                            String.valueOf(old),
                            String.valueOf(newItem));

                } catch (Exception e) {
                    throw new RuntimeException("Error accessing property "+d.getName(), e);
                }
            }
        }

    }
    
    public void testReadNewerVersion() throws Exception {
        String xml = "<?xml version=\"1.0\"?><matchmaker-projects export-format=\"1.1.0\"/>";
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
