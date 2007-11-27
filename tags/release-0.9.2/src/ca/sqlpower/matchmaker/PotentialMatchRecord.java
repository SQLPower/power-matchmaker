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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.matchmaker.munge.MungeProcess;


/**
 * This object represents one row in a Match Source Table, which is half of
 * a row in the Match Output ("cand dup") table.  If you think of the set of
 * potential matches and the source table records they point to as a graph,
 * instances of this class are directed edges in the graph, and instances
 * of SourceTableRecord are nodes.
 */
public class PotentialMatchRecord {

	private static final Logger logger = Logger.getLogger(PotentialMatchRecord.class);
	
    /**
     * The pool of matches (graph) that this match record belongs to.
     */
    private MatchPool pool;
    
    /**
     * The set of rules that caused the two source table records
     * identified here to be considered as potential matches.
     */
    private final MungeProcess mungeProcess;
    
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
     * This parameter keeps track of which state the record is in so we know when we
     * need to update its information in the database.
     */
    private StoreState storeState;

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
	 * An enumeration to note if this record has been modified or not, or if it
	 * has been recently created or if it has been marked for deletion. This
	 * information will be used to decide how the information in the database
	 * this record represents will be updated.
	 */
    public static enum StoreState {
    	
    	/**
		 * This is the property to denote that no changes have occurred to this
		 * record since it was last retrieved from or stored into the database.
		 */
    	CLEAN("CLEAN"),
    	
    	/**
		 * This is the property to denote that changes have occurred to this
		 * record since it was last retrieved from or stored into the database.
		 */
    	DIRTY("DIRTY"),
    	
    	/**
		 * This is the property to denote that this record has been recently
		 * created and has not been added to the database.
		 */
    	NEW("NEW");
    	
    	private final String code;
        
        StoreState(String code) {
            this.code = code;
        }
        
        public String getCode() {
            return code;
        }
    	
    }
    
    /**
	 * Sets up a PotentialMatchRecord which is the business model of an edge in
	 * the MatchValidation graph. It requires two SourceTableRecord to be
	 * identified as the LHS and RHS of the edge. By default, the master is not
	 * set.
	 * 
	 * @param mungeProcess
	 *            the MungeProcess that makes this edge exist
	 * @param matchStatus
	 *            the status of the relationship
	 * @param originalLhs
	 *            one of the SourceTableRecordd attached to this edge
	 * @param originalRhs
	 *            the other SourceTableRecord attached to this edge
	 * @param synthetic
	 *            a flag that denotes whether or not this edge was created by
	 *            the engine; true means NOT created by the engine
	 */
    public PotentialMatchRecord(
            MungeProcess mungeProcess,
            MatchType matchStatus,
            SourceTableRecord originalLhs,
            SourceTableRecord originalRhs,
            boolean synthetic) {
        this.mungeProcess = mungeProcess;
        this.matchStatus = matchStatus;
        this.originalLhs = originalLhs;
        this.originalRhs = originalRhs;
        this.synthetic = synthetic;
        master = MasterSide.NEITHER;
        this.storeState = StoreState.NEW;
    }

    /**
	 * The current storage state of this match record. Exposed as
	 * package-private because both the match pool and unit tests need
	 * to know.
	 */
    StoreState getStoreState() {
    	return storeState;
    }

    /**
	 * Modifies the current storage state of this match record. Exposed as
	 * package-private because both the match pool and unit tests need
	 * to be able to change it.
	 */
    void setStoreState(StoreState newState) {
    	storeState = newState;
    }
    
    /**
     * Sets the store state to dirty only if it was already clean.  If it was
     * new or deleted, it will be left alone.
     */
    private void markDirty() {
    	if (storeState == StoreState.CLEAN) {
    		setStoreState(StoreState.DIRTY);
    	}
    }

    /**
     * Gets the matchStatus. If you are using this to determine whether the PotentialMatchRecord
     * is a match edge or not, use {@link #isMatch()} instead, as it will cover both MATCH and
     * AUTOMATCH edge cases, as well as detect if the PotentialMatchRecord is in an inconsistent state
     * (such as being set as a Match edge but not have a master defined)
     */
    public MatchType getMatchStatus() {
        return matchStatus;
    }

    /**
     * If the match status is no match or unmatch, sets the master as undecided
     * @param matchStatus the type of Match this represents
     */
    public void setMatchStatus(MatchType matchStatus) {
        if (this.matchStatus == matchStatus) return;
    	this.matchStatus = matchStatus;
    	logger.debug("matchStatus was set to " + matchStatus);
        if (matchStatus == MatchType.NOMATCH || matchStatus == MatchType.UNMATCH){
            master = MasterSide.NEITHER;
        }
        markDirty();
    }

    public MungeProcess getMungeProcess() {
        return mungeProcess;
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
	 * Sets the master record to the source table record passed in and modifies
	 * the match status to the correct value (left-hand master, right-hand
	 * master, or no master). If the value passed is null it sets neither as the
	 * master record. If newMaster is not in this potential match record it
	 * throws an illegal argument exception. The match status also gets set to
	 * match if the master is not null or unmatch if the master is null.
	 * <p>
	 * This object's persistence state will only change to dirty as a result of
	 * this call if either the master or matchType property values actually
	 * changed.
	 * <p>
	 * Additionally, there is an isAutoMatch boolean flag that should be set
	 * to true only if this method is being called from the AutoMatch feature.
	 * 
	 * @param newMaster
	 *            the source table record that is participating in this
	 *            potential match that you want to make the master.
	 * @param isAutoMatch
	 * 			  Should be set to true if this method is being called from
	 * 			  an AutoMatch method. Otherwise, set to false.
	 */
    public void setMaster(SourceTableRecord newMaster, boolean isAutoMatch){
    	MasterSide oldMaster = master;
        if (newMaster == null){
            master = MasterSide.NEITHER;
            setMatchStatus(MatchType.UNMATCH);
        } else if (originalRhs.equals(newMaster)) {
            master = MasterSide.RIGHT_HAND_SIDE;
            if (!isAutoMatch) {
            	setMatchStatus(MatchType.MATCH);
            } else {
            	setMatchStatus(MatchType.AUTOMATCH);
            }
        } else if (originalLhs.equals(newMaster)) {
            master = MasterSide.LEFT_HAND_SIDE;
            if (!isAutoMatch) {
            	setMatchStatus(MatchType.MATCH);
            } else {
            	setMatchStatus(MatchType.AUTOMATCH);
            }
        } else {
            throw new IllegalArgumentException("The source table record "+ newMaster + " is not part of this record");
        }
        
        if (master != oldMaster) {
        	markDirty();
        }
    }
    
    /** 
     * Similar to {@link #setMaster(SourceTableRecord, boolean)} except the isAutoMatch
     * boolean flag is set to false by default. DO NOT use this version if you are performing an AutoMatch!
     * @throws ArchitectException
     */
    public void setMaster(SourceTableRecord newMaster) {
    	setMaster(newMaster, false);
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
     * Returns true if this PotentialMatchRecord is a match edge, which is true if the matchStatus
     * is MatchType.MATCH or MatchType.AUTOMATCH, and if master is not MasterSide.NEITHER.
     * <p>
     * Otherwise, it returns false if master is set to MasterSide.NEITHER, and the matchStatus is
     * neither MatchType.MATCH or MatchType.AUTOMATCH.
     * <p>
     * It will throw an IllegalStateException if the PotentialMatchRecord has a master set but
     * its matchStatus is neither MatchType.MATCH or MatchType.AUTOMATCH, or if the master is set to 
     * MasterSide.NEITHER and matchStatus is set to MasterType.MATCH or MasterType.AUTOMATCH
     * @return
     */
    public boolean isMatch() {
    	logger.debug("master is " + master);
    	if (master != MasterSide.NEITHER) {
    		logger.debug("matchStatus is " + matchStatus);
    		if (matchStatus == MatchType.AUTOMATCH || matchStatus == MatchType.MATCH) {
    			return true;
    		} else {
    			throw new IllegalStateException("PotentialMatchRecord is marked as a match but has no master.");
    		}
    	} else {
    		logger.debug("matchStatus is " + matchStatus);
    		if (matchStatus != MatchType.AUTOMATCH && matchStatus != MatchType.MATCH) {
    			return false;
    		} else {
    			throw new IllegalStateException("PotentialMatchRecord has a master but is not marked as a match.");
    		}
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
        "; matchStatus="+matchStatus+
        "; masterSide="+master;
    }

	public void setPool(MatchPool matchPool) {
		this.pool = matchPool;
	}

	public boolean isSynthetic() {
		return synthetic;
	}
	
	/**
	 * This override of the equals method evaluates equality on PotentialMatchRecords based on
	 * their mungeProcess, originalLhs, and originalRhs. We also considered including the 
	 * match pool in the evaluation, however, we feel that the usefulness of a 'Query by example'
	 * feature, (which would search for PotentialMatchRecords based on an example PotentialMatchRecord
	 * which may not include the match pool) is enough to leave it out. Also, for now, each match
	 * does not use multiple MatchPools anyway, although if that changes, we may have to re-evaluate
	 * this implementation of the equals method. 
	 */
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof PotentialMatchRecord)) {
			return false;
		}
		PotentialMatchRecord other = (PotentialMatchRecord)obj;
		if ((originalLhs == null ? other.originalLhs == null : originalLhs.equals(other.originalLhs))
				&& (originalRhs == null ? other.originalRhs == null : originalRhs.equals(other.originalRhs))) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		int result = 17;
		result = 37*result + (originalLhs == null ? 0 : originalLhs.hashCode());
		result = 37*result + (originalRhs == null ? 0 : originalRhs.hashCode());
		return result;
	}
}