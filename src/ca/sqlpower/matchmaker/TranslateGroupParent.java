package ca.sqlpower.matchmaker;


/** 
 * A holder for translate groups
 */
public class TranslateGroupParent extends AbstractMatchMakerObject<TranslateGroupParent, MatchMakerTranslateGroup> {
    /**
     * 
     */
    private final MatchMakerSession session;

    public TranslateGroupParent(MatchMakerSession session) {
        this.session = session;
        
    }
    
    /**
     * Persists the translate group as well as adding it to the child list and 
     * firing the proper events 
     */
    public void addNewChild(MatchMakerTranslateGroup child) {
        this.session.getDAO(MatchMakerTranslateGroup.class).save(child);
        super.addChild(child);
    }

    /**
     * Deletes the translate group as well as adding it to the child list and 
     * firing the proper events.  If the group is in use it gives a warning to the session.
     */
    public void removeAndDeleteChild(MatchMakerTranslateGroup child) {
        if(!isInUseInBusinessModel(child)) {
            this.session.getDAO(MatchMakerTranslateGroup.class).delete(child);
            super.removeChild(child);
        } else {
            this.session.handleWarning("Sorry the translate group "+child.getName()+" is in use and can't be deleted");
        }
    }

    /**
     * check and see if the translate group tg exists in the folder hierachy.
     * @param tg the translate group.
     * @return true if tg exists in the folder hierachy false if it dosn't.
     */
    public boolean isInUseInBusinessModel(MatchMakerTranslateGroup tg) {
        for (MatchMakerObject mmo :this.session.getCurrentFolderParent().getChildren()){
            // found the translate group
            if(checkMMOContainsTranslateGroup(mmo,tg) == true) return true;
        }
        return false;
    }

    /**
     * recursivly check and see if there is a criterion with the passed in translate group. 
     * @param mmo the object and decendents we want to check.
     * @param tg the translate group that we want to know if it is used.
     * @return true if tg is used by mmo or a decendent false otherwise.
     */
    private boolean checkMMOContainsTranslateGroup(MatchMakerObject mmo,MatchMakerTranslateGroup tg){
        if (mmo instanceof MatchMakerCriteria){
            MatchMakerCriteria criteria = (MatchMakerCriteria) mmo;
            if(tg.equals(criteria.getTranslateGroup())){
                return true;
            }
        }
        if ( mmo instanceof Match ){
            Match matchChild = (Match) mmo;
            for (MatchMakerCriteriaGroup critGroup : matchChild.getMatchCriteriaGroups()){
                if (checkMMOContainsTranslateGroup(critGroup, tg)) return true;
            }
        } else {
            for (int i = 0; i< mmo.getChildCount(); i++){
                MatchMakerObject child = (MatchMakerObject) mmo.getChildren().get(i);
                if (checkMMOContainsTranslateGroup(child, tg)) return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return this==obj;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}