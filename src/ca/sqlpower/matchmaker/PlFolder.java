package ca.sqlpower.matchmaker;

/**
 * A container class desigend to hold match maker objects (for now),
 * we need to make it generic to hold other sqlPower database objects like Power Loader
 * jobs and transactions. Then the child type will be ca.sqlpower.sql.DatabaseObject and
 * this class can be relocated to the <tt>ca.sqlpower.sql</tt> package.
 *
 * <p>All setter methods in this class fire the appropriate events.
 */
public class PlFolder<C extends MatchMakerObject>
	extends AbstractMatchMakerObject<PlFolder, C> {

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
     * Creates a new folder with a null name.  You'll have to call setName()
     * before expecting the folder to do much useful stuff.
     *
     * @param appUserName The login name of the current user.
     */
	public PlFolder() {
		this(null);
	}

	/**
	 * @param name The name of this folder.
	 */
	public PlFolder(String name){
		setName(name);
	}

	public String getFolderDesc() {
		return folderDesc;
	}

	public void setFolderDesc(String folderDesc) {
		String oldValue = this.folderDesc;
		this.folderDesc = folderDesc;
		getEventSupport().firePropertyChange("folderDesc", oldValue, folderDesc);
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


	public int hashCode() {
		if (getName() == null){ 
			return 0;
		} else {
			return getName().hashCode();
		}
	}


	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final PlFolder other = (PlFolder) obj;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		return true;
	}


}
