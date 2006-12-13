package ca.sqlpower.matchmaker;


/** 
 * A holder for folders.
 * 
 * A class that fires events and keeps the database in 
 * sync with the folder parent. 
 */
public class FolderParent extends AbstractMatchMakerObject<FolderParent, PlFolder> {
    /**
     * 
     */
    private final MatchMakerSession session;

    public FolderParent(MatchMakerSession session) {
        this.session = session;
    }
    
    /**
     * Persists the translate group as well as adding it to the child list and 
     * firing the proper events 
     */
    public void addNewChild(PlFolder child) {
        this.session.getDAO(PlFolder.class).save(child);
        super.addChild(child);
    }

    /**
     * Removes from the parent list and deletes the object from the database
     */
    public void deleteAndRemoveChild(PlFolder child) {
    	this.session.getDAO(PlFolder.class).delete(child);
		super.removeChild(child);
	}
    
    
    

    @Override
    public boolean equals(Object obj) {
        return this==obj;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

	public FolderParent duplicate(MatchMakerObject parent, MatchMakerSession session) {
		throw new IllegalAccessError("Folder parent not duplicatable");
	}
}