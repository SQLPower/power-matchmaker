package ca.sqlpower.matchmaker;

import java.util.Date;

public interface Auditable {

	public String getLastUpdateOSUser();
	public String getLastUpdateAppUser();
	public Date getLastUpdateDate();
	public void registerUpdate();

}
