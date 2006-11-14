package ca.sqlpower.matchmaker;

import java.util.Date;

/**
 * an interface provides basic auditing information
 * call registerUpdate to populate auditing information
 */
public interface Auditable {
	public Date getCreateDate();
	public String getLastUpdateOSUser();
	public String getLastUpdateAppUser();
	public Date getLastUpdateDate();
	public void registerUpdate();
}
