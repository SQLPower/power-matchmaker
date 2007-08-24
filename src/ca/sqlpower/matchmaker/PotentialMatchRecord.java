/*
 * Copyright (c) 2007, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.i 
 */

package ca.sqlpower.matchmaker;


/**
 * This object represents one row in a Match Source Table, which is half of
 * a row in the Match Output ("cand dup") table.  If you think of the set of
 * potential matches and the source table records they point to as a graph,
 * instances of this class are directed edges in the graph, and instances
 * of SourceTableRecord are nodes.
 */
public class PotentialMatchRecord {

    /**
     * The pool of matches (graph) that this match record belongs to.
     */
    private MatchPool pool;
    
    /**
     * The group of criteria that caused the two source table records
     * identified here to be considered as potential matches.
     */
    private final MatchMakerCriteriaGroup criteriaGroup;
    
    /**
     * The current status of this potential match (unexamined, confirmed correct,
     * confirmed incorrect, and so on).
     */
    private MatchType matchStatus;
    
    /**
	 * Indicates whether the left-hand, right-hand, or neither record is
	 * considered the "master." If the master is set to null or the match status
	 * is no match then the neither state is used.
	 */
    private MasterSide master;
    
    /**
     * One of the two records originally identified as a potential duplicate.
     */
    private final SourceTableRecord originalLhs;
    
    /**
     * One of the two records originally identified as a potential duplicate.
     */
    private final SourceTableRecord originalRhs;

    /**
     * This variable defines whether or not this potential match record is 
     * synthetic. If an edge is synthetic then the Match Maker created the edge
     * so it may needed to be handled differently (eg: reversing changes).
     */
    private final boolean synthetic;

    /**
     * One of the two records currently identified as a potential duplicate.
     * This exists because the merge engine can only handle paths of length one.
     * Once the merge engine has been rewritten to be in Java this should go away.
     * Avoid using this unless you are going to start the merge engine. The C match
     * engine may be using this as well.
     */
    @Deprecated
    private SourceTableRecord lhs;
    
    /**
     * One of the two records currently identified as a potential duplicate.
     * This exists because the merge engine can only handle paths of length one.
     * Once the merge engine has been rewritten to be in Java this should go away.
     * Avoid using this unless you are going to start the merge engine. The C match
     * engine may be using this as well.
     */
    @Deprecated
    private SourceTableRecord rhs;

    /**
     * The values that are used to keep track of which side of a record is
     * the master.
     */
    public static enum MasterSide {
        LEFT_HAND_SIDE,
        RIGHT_HAND_SIDE,
        NEITHER;
    }
    
    /**
     * An enumeration of all possible match types, along with their official
     * code names from the database.
     */
    public static enum MatchType {
    	
    	/**
    	 * This should be set when we are using the auto match functionality.
    	 */
        AUTOMATCH("AUTO_MATCH"),
        
        /**
         * This appears to represent the case when the two nodes are not related.
         */
        NOMATCH("NO_MATCH"),
        
        /**
         * This appears to represent the case when one of the nodes is a master of
         * the other.
         */
        MATCH("MATCH"),
        
        /**
		 * This should be the value for having been merged. This should be set
		 * by the merge engine.
		 */
        MERGED("MERGED"),
        
        /**
         * This is the property that defines that no user setting of the match record
         * has been done yet. This should be shown as a dashed line.
         */
        UNMATCH("UNMATCH");
        
        /**
         * Returns the MatchType instance that corresponds with the given
         * code.
         * 
         * @param code The code to look up
         * @return The corresponding MatchType object, or null if there is no
         * MatchType for the given code.
         */
        public static MatchType typeForCode(String code) {
            for (MatchType mt : values()) {
                if (mt.code.equals(code)) return mt;
            }
            return null;
        }
        
        private final String code;
        
        MatchType(String code) {
            this.code = code;
        }
        
        public String getCode() {
            return code;
        }
    }
    
    /**
     * Sets up a PotentialMatchRecord which is the business model of an edge in the 
     * MatchValidation graph.  It requires two SourceTableRecord to be identified as the LHS 
     * and RHS of the edge.  By default, the master is not set.
     * 
     * @param pool the MatchPool that this PotentialMatchRecord is in
     * @param criteriaGroup the MatchMakerCriteriaGroup that makes this edge exist
     * @param matchStatus the status of the relationship
     * @param originalLhs one of the SourceTableRecordd attached to this edge
     * @param originalRhs the other SourceTableRecord attached to this edge
     */
    public PotentialMatchRecord(
            MatchMakerCriteriaGroup criteriaGroup,
            MatchType matchStatus,
            SourceTableRecord originalLhs,
            SourceTableRecord originalRhs,
            boolean synthetic) {
        this.criteriaGroup = criteriaGroup;
        this.matchStatus = matchStatus;
        this.originalLhs = originalLhs;
        this.originalRhs = originalRhs;
        this.synthetic = synthetic;
        master = MasterSide.NEITHER;
    }

    public MatchType getMatchStatus() {
        return matchStatus;
    }

    /**
     * If the match status is no match or unmatch, sets the master as undecided
     * @param matchStatus the type of Match this represents
     */
    public void setMatchStatus(MatchType matchStatus) {
        this.matchStatus = matchStatus;
        if (matchStatus == MatchType.NOMATCH || matchStatus == MatchType.UNMATCH){
            master = MasterSide.NEITHER;
        }
    }

    public MatchMakerCriteriaGroup getCriteriaGroup() {
        return criteriaGroup;
    }

    /**
     * This exists because the merge engine can only handle paths of length one.
     * Once the merge engine has been rewritten to be in Java this should go away.
     * Avoid using this unless you are going to start the merge engine. The C match
     * engine may be using this as well.
     */
    @Deprecated
    public SourceTableRecord getLhs() {
        return lhs;
    }

    /**
     * This exists because the merge engine can only handle paths of length one.
     * Once the merge engine has been rewritten to be in Java this should go away.
     * Avoid using this unless you are going to start the merge engine. The C match
     * engine may be using this as well.
     */
    @Deprecated
    public void setLhs(SourceTableRecord lhs) {
        this.lhs = lhs;
    }

    /**
     * This exists because the merge engine can only handle paths of length one.
     * Once the merge engine has been rewritten to be in Java this should go away.
     * Avoid using this unless you are going to start the merge engine. The C match
     * engine may be using this as well.
     */
    @Deprecated
    public SourceTableRecord getRhs() {
        return rhs;
    }

    /**
     * This exists because the merge engine can only handle paths of length one.
     * Once the merge engine has been rewritten to be in Java this should go away.
     * Avoid using this unless you are going to start the merge engine. The C match
     * engine may be using this as well.
     */
    @Deprecated
    public void setRhs(SourceTableRecord rhs) {
        this.rhs = rhs;
    }

    public SourceTableRecord getOriginalLhs() {
        return originalLhs;
    }

    public SourceTableRecord getOriginalRhs() {
        return originalRhs;
    }

    public MatchPool getPool() {
        return pool;
    }

    /**
     * Set the master record to the source table record passed in.  If the
     * value passed is null it sets neither as the master record.  If 
     * newMaster is not in this potential match record it throws an
     * illegal argument exception. The match status also gets set to match
     * if the master is not null or unmatch if the master is null.
     * 
     * @param newMaster the source table record that is participating in this
     *                     potential match that you want to make the master.
     */
    public void setMaster(SourceTableRecord newMaster){
    	
        if (newMaster== null){
            master = MasterSide.NEITHER;
            matchStatus = MatchType.UNMATCH;
        } else if (originalRhs.equals(newMaster)) {
            master = MasterSide.RIGHT_HAND_SIDE;
            matchStatus = MatchType.MATCH;
        } else if (originalLhs.equals(newMaster)) {
            master = MasterSide.LEFT_HAND_SIDE;
            matchStatus = MatchType.MATCH;
        } else {
            throw new IllegalArgumentException("The source table record "+ newMaster + " is not part of this record");
        }  
    }
    
    public boolean isRhsMaster() {
        if (master == MasterSide.RIGHT_HAND_SIDE) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isLhsMaster() {
        if (master == MasterSide.LEFT_HAND_SIDE) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isMasterUndecided() {
        if (master == MasterSide.NEITHER) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the SourceTableRecord that is the master indicated by this record.
     *  
     * @return the master SourceTableRecord or null if no master has been specified
     * @throws IllegalStateException if the master is not en element of MasterSide
     */
    public SourceTableRecord getMaster() {
        if(master == MasterSide.NEITHER){
            return null;
        } else if (master == MasterSide.LEFT_HAND_SIDE){
            return originalLhs;
        } else if (master == MasterSide.RIGHT_HAND_SIDE){
            return originalRhs;
        } else {
            throw new IllegalStateException("Invalid master state: " + master);
        }
    }
    
    /**
     * Returns the SourceTableRecord that is the duplicate indicated by this record.
     *  
     * @return the duplicate (non-master) SourceTableRecord or returns null if the
     * master and duplicate has not been setup yet
     * @throws IllegalStateException if the master is not en element of MasterSide
     */
    public SourceTableRecord getDuplicate() {
        if (master == MasterSide.NEITHER){
            return null;
        } else {
            if (master == MasterSide.LEFT_HAND_SIDE){
                return originalRhs;
            } else if (master == MasterSide.RIGHT_HAND_SIDE){
                return originalLhs;
            } else {
                throw new IllegalStateException("Invalid master state: " + master);
            }
        }
    }
    
    @Override
    public String toString() {
        return "PotentialMatch: origLhs="+originalLhs+
        "; origRhs="+originalRhs+
        "; lhs="+lhs+
        "; rhs="+rhs+
        "; matchStatus="+matchStatus+
        "; masterSide="+master;
    }

	public void setPool(MatchPool matchPool) {
		this.pool = matchPool;
	}

	public boolean isSynthetic() {
		return synthetic;
	}
    
}
