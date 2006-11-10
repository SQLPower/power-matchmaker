package ca.sqlpower.matchmaker;



/** 
 * A container class desigend to hold match maker objects
 *	
 * FIXME implement class
 */
public class MatchMakerFolder<C extends MatchMakerObject> extends AbstractMatchMakerObject<C> {

	private String folderName;
    private String folderDesc;
    
	public MatchMakerFolder( ) {
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

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 0;
		result = PRIME * result + ((folderName == null) ? 0 : folderName.hashCode());
		result = PRIME * result + ((getParent() == null) ? 0 : getParent().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final MatchMakerFolder other = (MatchMakerFolder) obj;
		if (getParent() == null) {
			if (other.getParent() != null)
				return false;
		} else if (!getParent().equals(other.getParent()))
			return false;
		if (folderName == null) {
			if (other.folderName != null)
				return false;
		} else if (!folderName.equals(other.folderName))
			return false;
		return true;
	}



	
}
