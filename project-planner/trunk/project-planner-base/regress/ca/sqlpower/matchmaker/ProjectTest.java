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

import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.matchmaker.event.MatchMakerEventCounter;
import ca.sqlpower.matchmaker.munge.MungeProcess;

public class ProjectTest extends MatchMakerTestCase<Project> {

    Project project;
	private TestingMatchMakerSession session;

    protected void setUp() throws Exception {
    	// The following two are ignored because they are to be used only by hibernate
    	// so they don't throw events
        propertiesToIgnoreForEventGeneration.add("mungeProcesses");
        propertiesToIgnoreForEventGeneration.add("tableMergeRules");
        // Ignored because they are delegates to support functions for the chached table class
        propertiesToIgnoreForEventGeneration.add("sourceTableCatalog");
        propertiesToIgnoreForEventGeneration.add("sourceTableSchema");
        propertiesToIgnoreForEventGeneration.add("sourceTableName");
        propertiesToIgnoreForEventGeneration.add("sourceTableSPDatasource");
        propertiesToIgnoreForEventGeneration.add("resultTableCatalog");
        propertiesToIgnoreForEventGeneration.add("resultTableSchema");
        propertiesToIgnoreForEventGeneration.add("resultTableName");
        propertiesToIgnoreForEventGeneration.add("resultTableSPDatasource");
        propertiesToIgnoreForEventGeneration.add("xrefTableCatalog");
        propertiesToIgnoreForEventGeneration.add("xrefTableSchema");
        propertiesToIgnoreForEventGeneration.add("xrefTableName");
        propertiesToIgnoreForEventGeneration.add("xrefTableSPDatasource");
        propertiesToIgnoreForEventGeneration.add("spDatasource");
        propertiesToIgnoreForDuplication.add("mungeProcesses");
        
        propertiesToIgnoreForDuplication.add("resultTableSPDatasource");
        propertiesToIgnoreForDuplication.add("sourceTableSPDatasource");
        propertiesToIgnoreForDuplication.add("xrefTableSPDatasource");

        // These set other properties to null that describe the same object
        propertiesToIgnoreForDuplication.add("resultTable");
        propertiesToIgnoreForDuplication.add("sourceTable");
        propertiesToIgnoreForDuplication.add("xrefTable");
        
        
        //These don't really differ on set and get but there are checks in
        //place that ensure that the dataSource exists and this on will not. 
        propertiesThatDifferOnSetAndGet.add("xrefTableSPDatasource");
        propertiesThatDifferOnSetAndGet.add("sourceTableSPDatasource");
        propertiesThatDifferOnSetAndGet.add("resultTableSPDatasource");
        
        
        
        propertiesToIgnoreForDuplication.add("sourceTableIndex");
        
        propertiesThatHaveSideEffects.add("xrefTable");
        propertiesThatHaveSideEffects.add("sourceTable");
        propertiesThatHaveSideEffects.add("resultTable");
        super.setUp();
        project = new Project();
        session = new TestingMatchMakerSession();
		session.setDatabase(new SQLDatabase());
        project.setSession(session);
    }
    @Override
    protected Project getTarget() {
        return project;
    }


	public void testEqual() {
		Project m1 = new Project();
		Project m2 = new Project();
		assertTrue("Project1 <> project2", (m1 != m2) );
		assertFalse(m1.equals(m2) );
		m1.setName("project1");
		m2.setName("project2");
		assertFalse(m1.equals(m2) );
		m1.setName("project");
		m2.setName("project");
		assertFalse(m1.equals(m2) );
	}

    public void testMatchMakerFolderFiresEventForMungeProcesses(){
        MatchMakerEventCounter l = new MatchMakerEventCounter();
        project.getMungeProcessesFolder().addMatchMakerListener(l);
        List<MungeProcess> mmoList = new ArrayList<MungeProcess>();
        project.setMungeProcesses(mmoList);
        assertEquals("Wrong number of events fired",1,l.getAllEventCounts());
        assertEquals("Wrong type of event fired",1,l.getStructureChangedCount());
    }
}
