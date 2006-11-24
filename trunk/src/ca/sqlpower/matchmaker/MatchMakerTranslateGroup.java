package ca.sqlpower.matchmaker;



public class MatchMakerTranslateGroup
	extends AbstractMatchMakerObject<MatchMakerTranslateGroup, MatchMakerTranslateWord> {

	private Long oid;

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((getName() == null) ? 0 : getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if(!(obj instanceof MatchMakerTranslateGroup)) 
            return false;
		final MatchMakerTranslateGroup other = (MatchMakerTranslateGroup) obj;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		return true;
	}

	public MatchMakerTranslateGroup() {
	}
    
    /** 
     * Set the translate words to a monitonly increasing form in the order of the parent
     * and add any newly created children to this translation group
     */
    public void syncChildrenSeqNo(){
        for (Long i = 0L; i < getChildCount(); i++){
            MatchMakerTranslateWord child = getChildren().get(i.intValue());
            child.setLocation(i);
        }
    }

	@Override
	public String toString() {
		return getName();
	}
}