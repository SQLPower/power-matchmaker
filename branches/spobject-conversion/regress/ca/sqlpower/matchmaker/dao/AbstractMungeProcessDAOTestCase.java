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


package ca.sqlpower.matchmaker.dao;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.dao.hibernate.MatchMakerHibernateSession;
import ca.sqlpower.matchmaker.dao.hibernate.PlFolderDAOHibernate;
import ca.sqlpower.matchmaker.dao.hibernate.ProjectDAOHibernate;
import ca.sqlpower.matchmaker.munge.InputDescriptor;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.matchmaker.munge.SQLInputStep;
import ca.sqlpower.sqlobject.SQLTable;

public abstract class AbstractMungeProcessDAOTestCase extends AbstractDAOTestCase<MungeProcess,MungeProcessDAO>  {

	Long count=0L;
    Project project;
    PlFolder folder;
    
    public abstract MatchMakerHibernateSession getSession() throws Exception;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        PlFolderDAOHibernate plFolderDAO = new PlFolderDAOHibernate((MatchMakerHibernateSession) getSession());
        folder = new PlFolder("main test folder");
        plFolderDAO.save(folder);
        
        project= new Project();
        project.setName("Munge Process Test Project");
        project.setType(ProjectMode.BUILD_XREF);
        project.setParent(folder);
        project.setResultTable(new SQLTable());
        try {
        	project.setSession(getSession());
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
        
        assertNotNull(project.getSession());
        ProjectDAO matchDAO = new ProjectDAOHibernate(getSession());
        matchDAO.save(project);
    }

	@Override
	public MungeProcess createNewObjectUnderTest() throws Exception {
		count++;
		MungeProcess mungeProcess = new MungeProcess();
        mungeProcess.setSession(getSession());
		try {
			setAllSetters(getSession(), mungeProcess, getNonPersitingProperties());
            mungeProcess.setName("Munge Process "+count);
            project.addChild(mungeProcess);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return mungeProcess;
	}

	@Override
	public List<String> getNonPersitingProperties() {
		List<String> nonPersistingProperties = super.getNonPersitingProperties();
		nonPersistingProperties.add("lastUpdateOSUser");
		nonPersistingProperties.add("lastUpdateDate");
		nonPersistingProperties.add("lastUpdateAppUser");
		nonPersistingProperties.add("session");
        nonPersistingProperties.add("parent");
        nonPersistingProperties.add("parentProject");
        nonPersistingProperties.add("results");
        nonPersistingProperties.add("oid");

		return nonPersistingProperties;
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = { "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE" }, 
			justification = "This is simply a unit test, so we are not so concerned with performance or security concerns here.")
    public void testDelete() throws Exception {
        Connection con = getSession().getConnection();
        Statement stmt = null;
        try {

            MungeProcess process = project.getMungeProcesses().get(0);
            String processName = process.getName();

            MungeProcessDAO dao = getDataAccessObject();
            dao.save(process);
            
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM mm_munge_process WHERE process_name = '"+processName+"'");
            assertTrue("munge process didn't save?!", rs.next());
            rs.close();

            dao.delete(process);

            rs = stmt.executeQuery("SELECT * FROM mm_munge_process WHERE process_name = '"+processName+"'");
            assertFalse("munge process didn't delete", rs.next());
            rs.close();
        } finally {
            stmt.close();
        }
    }
    
    public void testSaveAndLoadInTwoSessionsWithChildren() throws Exception {
		MungeProcessDAO dao = getDataAccessObject();
		List<MungeProcess> all;
		MungeProcess item1 = createNewObjectUnderTest();
		item1.setVisible(true);
		MungeStep ms = new SQLInputStep();
		item1.addChild(ms);
		MungeStepOutput<String> mso = new MungeStepOutput<String>("TEST_MSO", String.class);
		ms.addChild(mso,0);
		MungeStepOutput<String> mso2 = new MungeStepOutput<String>("TEST_MSO2", String.class);
		ms.addChild(mso2,1);
		ms.addInput(new InputDescriptor("asdf", String.class));
		ms.connectInput(0, mso2);

		dao.save(item1);
		
		resetSession();
		dao = getDataAccessObject();
		all = dao.findAll();
        assertTrue("We want at least one item", 1 <= all.size());
        MungeProcess savedItem1 = all.get(0);
		for (MungeProcess item: all){
			item.setSession(getSession());
		}
		assertEquals("MungeProcess should have 1 child.", 1, savedItem1.getChildren().size());
		MungeStep saveMS = savedItem1.getChildren(MungeStep.class).get(0);
		assertEquals("MungeStep should have 2 child.", 2, saveMS.getChildren().size());
		MungeStepOutput saveMSO2 = saveMS.getChildren(MungeStepOutput.class).get(1);
		assertEquals("MungeStep should have its first input equals its second output.", saveMSO2, saveMS.getMSOInputs().get(0));
	}
    
    public void testSaveAndLoadInTwoSessionsWithEmptyInput() throws Exception {
		MungeProcessDAO dao = getDataAccessObject();
		List<MungeProcess> all;
		MungeProcess item1 = createNewObjectUnderTest();
		item1.setVisible(true);
		MungeStep ms = new SQLInputStep();
		item1.addChild(ms);
		MungeStepOutput<String> mso = new MungeStepOutput<String>("TEST_MSO", String.class);
		ms.addChild(mso, 0);
		MungeStepOutput<String> mso2 = new MungeStepOutput<String>("TEST_MSO2", String.class);
		ms.addChild(mso2, 1);
		ms.addInput(new InputDescriptor("TEST_INPUT", String.class));

		dao.save(item1);
		
		resetSession();
		dao = getDataAccessObject();
		all = dao.findAll();
        assertTrue("We want at least one item", 1 <= all.size());
        MungeProcess savedItem1 = all.get(0);
		for (MungeProcess item: all){
			item.setSession(getSession());
		}
		assertEquals("MungeProcess should have 1 child.", 1, savedItem1.getChildren().size());
		MungeStep saveMS = savedItem1.getChildren(MungeStep.class).get(0);
		assertEquals("MungeStep should have 2 child.", 2, saveMS.getChildren().size());

		assertNull("MungeStep should have a null input.", saveMS.getMSOInputs().get(0));
		assertEquals("MungeStep should still have same input descriptor.", "TEST_INPUT", saveMS.getInputDescriptor(0).getName());
	}
}
