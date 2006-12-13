package ca.sqlpower.matchmaker;

/**
 * A container class desigend to hold match maker objects
 */
public class MatchMakerFolder<C extends MatchMakerObject>
	extends AbstractMatchMakerObject<MatchMakerFolder, C> {

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

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 0;
		result = PRIME * result + ((getName() == null) ? 0 : getName().hashCode());
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
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		return true;
	}

	public MatchMakerFolder duplicate(MatchMakerObject parent, MatchMakerSession s) {
		throw new IllegalAccessError("The match maker folder should never be duplicated.  It should be managed by the Match object");
	}




}
