package ca.sqlpower.matchmaker.dao;

import java.util.List;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.PlFolder;

public abstract class AbstractPlFolderDAOTestCase extends AbstractDAOTestCase<PlFolder<MatchMakerObject>,PlFolderDAO>  {

	int count=0;

	@Override
	public PlFolder<MatchMakerObject> getNewObjectUnderTest() {
		count++;
		PlFolder<MatchMakerObject> plFolder =
			new PlFolder<MatchMakerObject>("Test Folder");
		plFolder.setSession(this.session);
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
	
	public void testMatchesPersist(){
		PlFolder f = getNewObjectUnderTest();
		Match match = new Match();
		match.setName("child");
		match.setType(Match.MatchType.FIND_DUPES);
		
		PlFolderDAO dao = getDataAccessObject();
		
		dao.save(f);
		f.addChild(match);
        dao.save(f);
		List<PlFolder> folders = dao.findAll();
		PlFolder fAgain = folders.get(folders.indexOf(f));
		assertEquals("Wrong number of children", 1,fAgain.getChildCount());
		assertEquals("Wrong child",match, fAgain.getChildren().get(0));
	}

}
