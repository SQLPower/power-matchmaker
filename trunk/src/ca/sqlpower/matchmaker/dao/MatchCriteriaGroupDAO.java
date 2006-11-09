package ca.sqlpower.matchmaker.dao;

import ca.sqlpower.matchmaker.MatchMakerCriteriaGroup;
import ca.sqlpower.matchmaker.MatchmakerCriteria;

/**
 * The Data access interface for match criteria group objects
 *
 * At this point this interface only extends the base DAO interface
 * and is put in for future expansion. 
 *
 * Remember to program to this interface rather than an implemenation
 */
public interface MatchCriteriaGroupDAO extends MatchMakerDAO<MatchMakerCriteriaGroup<MatchmakerCriteria>> {

}
