package ca.sqlpower.matchmaker;



/** 
 * A container class desigend to hold match maker objects
 *	
 * FIXME implement class
 */
public class MatchMakerFolder<C extends MatchMakerObject> extends AbstractMatchMakerObject<C> {

	private String folderName;
    private String folderDesc;
    
	public MatchMakerFolder(String appUserName) {
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


}
