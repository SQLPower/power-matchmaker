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
     * 
     * Because you can get update collisions if you change the 
     * sequence numbers to one that already exists in the database, we
     * have to use a non-overlapping set.  To avoid marchig off 
     * to infinity.  We start from zero if there is enough space
     * before the current lowest sequence number to fit all words.
     * 
     * 
     */
    public void syncChildrenSeqNo(){
    	long minSeqNo = Long.MAX_VALUE;
    	long maxSeqNo = 0;
    	for (MatchMakerTranslateWord w: getChildren()) {
    		if (w.getLocation() != null){
    			minSeqNo = Math.min(minSeqNo, w.getLocation());
    			maxSeqNo = Math.max(maxSeqNo, w.getLocation());
    		}
    	}
    	long startNumber;
    	if (minSeqNo > getChildCount()){
    		startNumber = 0;
    	} else {
    		startNumber = maxSeqNo+1;
    	}
        for (Long i = 0L; i < getChildCount(); i++){
            MatchMakerTranslateWord child = getChildren().get(i.intValue());
            child.setLocation(i+startNumber);
        }
    }

	@Override
	public String toString() {
		return getName();
	}

	public MatchMakerTranslateGroup duplicate(MatchMakerObject parent, MatchMakerSession s) {
		MatchMakerTranslateGroup g = new MatchMakerTranslateGroup();
		g.setSession(s);
		g.setParent(parent);
		g.setName(this.getName());
		for (MatchMakerTranslateWord w: getChildren()){
			g.addChild(w.duplicate(g,s));
		}
		return g;
	}
}