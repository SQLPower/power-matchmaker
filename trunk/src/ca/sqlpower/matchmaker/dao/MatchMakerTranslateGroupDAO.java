package ca.sqlpower.matchmaker.dao;

import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.MatchMakerTranslateWord;

/**
 * The Data access interface for match maker translate group objects
 *
 * At this point this interface only extends the base DAO interface
 * and is put in for future expansion. 
 *
 * Remember to program to this interface rather than an implemenation
 */
public interface MatchMakerTranslateGroupDAO extends MatchMakerDAO<MatchMakerTranslateGroup<MatchMakerTranslateWord>> {

}
