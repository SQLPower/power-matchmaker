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
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TestingMatchMakerContext;
import ca.sqlpower.matchmaker.dao.AbstractDAOTestCase;
import ca.sqlpower.matchmaker.swingui.StubMatchMakerSession;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SPDataSourceType;

public class ProjectXMLDAOTest extends TestCase {

    private ProjectDAOXML daoOut;
    private ByteArrayOutputStream out;

    private MatchMakerSession session;

    private SQLDatabase db;
    private TestingMatchMakerContext context;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        PlDotIni plini = new PlDotIni();
        SPDataSourceType mock = new SPDataSourceType();
        mock.setName("Mock JDBC");
        mock.setJdbcDriver("ca.sqlpower.testutil.MockJDBCDriver");
        SPDataSource newDS = new SPDataSource(plini);
        newDS.setParentType(mock);
        newDS.setName("stub_ds");
        newDS.setUser("mock");
        newDS.setPass("mock");
        newDS.setUrl("jdbc:mock:dbmd.catalogTerm=Catalog&dbmd.schemaTerm=Schema&catalogs=cat&schemas.cat=schem&tables.cat.schem=match_table,result_table");
        plini.addDataSource(newDS);
        
        db = new SQLDatabase(newDS);
        
        context = new TestingMatchMakerContext() {
            @Override
            public List<SPDataSource> getDataSources() {
                return getPlDotIni().getConnections();
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

        context.setPlDotIni(plini);

        out = new ByteArrayOutputStream();
        daoOut = new ProjectDAOXML(out);
    }
    
    public void testSaveAndLoad() throws Exception {
        Project p = new Project();
        p.setSession(session);
        AbstractDAOTestCase.setAllSetters(session, p, getPropertiesToIgnore());

        p.setSourceTableSPDatasource("stub_ds");
        p.setSourceTableCatalog("cat");
        p.setSourceTableSchema("schem");
        p.setSourceTableName("match_table");

        p.setResultTableSPDatasource("stub_ds");
        p.setResultTableCatalog("cat");
        p.setResultTableSchema("schem");
        p.setResultTableName("result_table");

        p.setXrefTable(null);
        
        SQLTable sourceTable = p.getSourceTable();
        SQLColumn pkCol = new SQLColumn(sourceTable, "pk", Types.INTEGER, 10, 0);
        sourceTable.addColumn(pkCol);
        pkCol.setPrimaryKeySeq(0);
        p.setSourceTableIndex(sourceTable.getPrimaryKeyIndex());
        
        daoOut.save(p);
        System.out.println(out.toString("utf-8"));
        
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ProjectDAOXML daoIn = new ProjectDAOXML(session, in);
        Project readBack = daoIn.findAll().get(0);
        
        assertNotNull(readBack.getSession());
        assertPropertiesEqual(p, readBack, "parent", "view");
        // TODO descend into child objects and check their properties too
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
        ignore.add("session");
        ignore.add("mungeProcesses");
        ignore.add("tableMergeRules");
        ignore.add("cleansingEngine");
        ignore.add("matchingEngine");
        ignore.add("mergingEngine");
        
        // tested explicitly elsewhere
        ignore.add("sourceTable");
        ignore.add("resultTable");
        ignore.add("xrefTable");
        
        ignore.add("engineRunning");
        return ignore;
    }
    
    private void assertPropertiesEqual(
            MatchMakerObject expected,
            MatchMakerObject actual,
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
                            "The property "+d.getName() +" was not persisted for object "+this.getClass(),
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
