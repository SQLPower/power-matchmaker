package ca.sqlpower.matchmaker.dao;

import java.util.List;

import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.PlFolder;

public abstract class AbstractPlFolderDAOTestCase extends AbstractDAOTestCase<PlFolder<MatchMakerObject>,PlFolderDAO>  {

	int count=0;

	@Override
	public PlFolder<MatchMakerObject> getNewObjectUnderTest() {
		count++;
		PlFolder<MatchMakerObject> plFolder = new PlFolder<MatchMakerObject>();
		plFolder.setSession(this.session);
		plFolder.setFolderName("test "+count);
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
	
}
