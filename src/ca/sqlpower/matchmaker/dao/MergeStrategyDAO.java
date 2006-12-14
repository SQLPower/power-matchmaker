package ca.sqlpower.matchmaker.dao;

import ca.sqlpower.matchmaker.TableMergeRules;

/**
 * The Data access interface for merge strategy objects
 *
 * At this point this interface only extends the base DAO interface
 * and is put in for future expansion. 
 *
 * Remember to program to this interface rather than an implemenation
 */
public interface MergeStrategyDAO extends MatchMakerDAO<TableMergeRules> {

}
