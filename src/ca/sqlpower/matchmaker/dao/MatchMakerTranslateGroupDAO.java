package ca.sqlpower.matchmaker.dao;

import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;

/**
 * The Data access interface for match maker translate group objects
 *
 * At this point this interface only extends the base DAO interface
 * and is put in for future expansion. 
 *
 * Remember to program to this interface rather than an implemenation
 */
public interface MatchMakerTranslateGroupDAO extends MatchMakerDAO<MatchMakerTranslateGroup> {
    /**
     * Finds the Translte Group having the given name (case sensitive).
     * @param name The name of the translate group to look for
     * @return The translate group object with the given name, or null if there
     * is no such translate group.
     */
    public MatchMakerTranslateGroup findByName(String name);
}
