package ca.sqlpower.matchmaker.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.PlFolder;

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
	
	public void testMatchesPersist() throws Exception {
		PlFolder f = createNewObjectUnderTest();
		Match match = new Match();
		match.setName("child");
		match.setType(Match.MatchMode.FIND_DUPES);
		
		PlFolderDAO dao = getDataAccessObject();
		
		dao.save(f);
		f.addChild(match);
        dao.save(f);
		List<PlFolder> folders = dao.findAll();
		PlFolder fAgain = folders.get(folders.indexOf(f));
		assertEquals("Wrong number of children", 1,fAgain.getChildCount());
		assertEquals("Wrong child",match, fAgain.getChildren().get(0));
	}

    /**
     * Test if you can properly move a match.
     */
    public void testMatchMove() throws Exception {
        Match m = new Match();
        m.setName("match");

        PlFolder<Match> oldFolder = new PlFolder<Match>("old");
        PlFolder<Match> newFolder = new PlFolder<Match>("new");

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
                    fail("No results found for match " + m.getName());
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
                    fail("No results found for match " + m.getName());
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
