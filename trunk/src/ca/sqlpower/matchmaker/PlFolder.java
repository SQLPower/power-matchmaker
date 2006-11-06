package ca.sqlpower.matchmaker;

/** 
 * A container class desigend to hold match maker objects (for now), 
 * we need to make it generic to hold other sqlPower database objects like Power Loader
 * jobs and transactions. Then the child type will be ca.sqlpower.sql.DatabaseObject and
 * this class can be relocated to the <tt>ca.sqlpower.sql</tt> package.
 * 
 * <p>All setter methods in this class fire the appropriate events.
 */
public class PlFolder<C extends MatchMakerObject> extends AbstractMatchMakerObject<C> {

	/**
	 * The name of this folder (visible to the user).
	 */
	private String folderName;
	
	/**
	 * This folder's description.
	 */
    private String folderDesc;
    
    /**
     * XXX I don't know what this property is for.
     */
    private String folderStatus;
    
    /**
     * The last number assigned to a backup of this folder. This will be incremented by
     * 1 every time a new backup is made.
     * 
     * <p>XXX we'd like to do away with this property and just determine the correct next
     * backup number by searching the database.
     */
    private Long lastBackupNo = 0L;
    
    /**
     * Creates a new PlFolder whose audit info will record changes as being done by the given user.
     * 
     * @param appUserName The login name of the current user.
     */
	public PlFolder(String appUserName) {
		super(appUserName);
	}
	
	public String getFolderDesc() {
		return folderDesc;
	}

	public void setFolderDesc(String folderDesc) {
		String oldValue = this.folderDesc;
		this.folderDesc = folderDesc;
		getEventSupport().firePropertyChange("folderDesc", oldValue, folderDesc);
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		String oldValue = this.folderName;
		this.folderName = folderName;
		getEventSupport().firePropertyChange("folderName", oldValue, folderName);
	}
	
	public String getFolderStatus() {
		return folderStatus;
	}

	public void setFolderStatus(String folderStatus) {
		String oldValue = this.folderStatus;
		this.folderStatus = folderStatus;
		getEventSupport().firePropertyChange("folderStatus", oldValue, folderStatus);
	}

	public Long getLastBackupNo() {
		return lastBackupNo;
	}

	public void setLastBackupNo(Long lastBackupNo) {
		long oldValue = this.lastBackupNo;
		this.lastBackupNo = lastBackupNo;
		getEventSupport().firePropertyChange("lastBackupNo", oldValue, lastBackupNo);
	}

}
