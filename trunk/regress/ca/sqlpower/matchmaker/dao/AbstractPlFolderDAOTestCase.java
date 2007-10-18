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


package ca.sqlpower.matchmaker.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.Project.ProjectMode;

public abstract class AbstractPlFolderDAOTestCase extends AbstractDAOTestCase<PlFolder<MatchMakerObject>,PlFolderDAO>  {

	int count=0;

	@Override
	public PlFolder<MatchMakerObject> createNewObjectUnderTest() throws Exception {
		count++;
		PlFolder<MatchMakerObject> plFolder =
			new PlFolder<MatchMakerObject>("Test Folder"+count);
		count++;
		plFolder.setSession(getSession());
		plFolder.setName("test "+count);
		plFolder.setFolderDesc("My Desc");
		plFolder.setFolderStatus("Open");
		plFolder.setLastBackupNo(1234L);
		return plFolder;
	}

	@Override
	public List<String> getNonPersitingProperties() {
		List<String> nonPersistingProperties = super.getNonPersitingProperties();
		nonPersistingProperties.add("lastUpdateOSUser");
		nonPersistingProperties.add("lastUpdateDate");
		nonPersistingProperties.add("lastUpdateAppUser");
		return nonPersistingProperties;
	}
	
	public void testProjectPersist() throws Exception {
		PlFolder f = createNewObjectUnderTest();
		Project project = new Project();
		project.setName("child");
		project.setType(Project.ProjectMode.FIND_DUPES);
		
		PlFolderDAO dao = getDataAccessObject();
		
		dao.save(f);
		f.addChild(project);
        dao.save(f);
		List<PlFolder> folders = dao.findAll();
		PlFolder fAgain = folders.get(folders.indexOf(f));
		assertEquals("Wrong number of children", 1,fAgain.getChildCount());
		assertEquals("Wrong child",project, fAgain.getChildren().get(0));
	}

    /**
     * Test if you can properly move a project.
     */
    public void testProjectMove() throws Exception {
        Project m = new Project();
        m.setName("project");
        m.setType(ProjectMode.FIND_DUPES);

        PlFolder<Project> oldFolder = new PlFolder<Project>("old");
        PlFolder<Project> newFolder = new PlFolder<Project>("new");

        oldFolder.addChild(m);
        PlFolderDAO dao = getDataAccessObject();

        dao.save(oldFolder);
        dao.save(newFolder);

        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = getSession().getConnection();
            stmt = con.createStatement();
            try {
                rs = stmt
                        .executeQuery("SELECT folder_name FROM pl_match,pl_folder2 WHERE match_id='"
                                + m.getName() + "' and pl_folder2.folder_oid = pl_match.folder_oid");

                if (!rs.next()) {
                    fail("No results found for project " + m.getName());
                }

                assertEquals("The setup failed to work", "old", rs
                        .getString("folder_name"));
            } finally {
                try {
                    rs.close();
                } catch (Exception e) {
                    System.err.println("Couldn't close result set");
                    e.printStackTrace();
                }
            }

            oldFolder.removeChild(m);
            newFolder.addChild(m);
            dao.save(newFolder);
            dao.save(oldFolder);
            try {
                rs = stmt
                .executeQuery("SELECT folder_name FROM pl_match,pl_folder2 WHERE match_id='"
                        + m.getName() + "' and pl_folder2.folder_oid = pl_match.folder_oid");

                if (!rs.next()) {
                    fail("No results found for project " + m.getName());
                }

                assertEquals("move failed to work", "new", rs
                        .getString("folder_name"));
            } finally {
                try {
                    rs.close();
                } catch (Exception e) {
                    System.err.println("Couldn't close result set");
                    e.printStackTrace();
                }
            }
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
                System.err.println("Couldn't close statement");
                e.printStackTrace();
            }
            // connection didn't come from a pool so we can't close it
        }
    }
}
