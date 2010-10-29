/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonProperty;


/**
 * This object represents one row in a Match Source Table, which is half of
 * a row in the Match Output ("cand dup") table.  If you think of the set of
 * potential matches and the source table records they point to as a graph,
 * instances of this class are directed edges in the graph, and instances
 * of SourceTableRecord are nodes.
 */
public class PotentialMatchRecord extends AbstractMatchMakerObject{
	
	/**
	 * Defines an absolute ordering of the child types of this class.
	 */
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.emptyList();
	
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
     * One of the two records originally identified as a potential duplicate. This
     * record is the one that is reference by a PotentialMatchDuplicate.
     */
    private final SourceTableRecord referencedRecord;
    
    /**
     * One of the two records originally identified as a potential duplicate. This
     * record is the one that is directly under the SourceTableRecord.
     */
    private final SourceTableRecord directRecord;

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
        DIRECT_SIDE,
        REFERENCED_SIDE,
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
	 * @param referencedRecord
	 *            one of the SourceTableRecordd attached to this edge
	 * @param directRecord
	 *            the other SourceTableRecord attached to this edge
	 * @param synthetic
	 *            a flag that denotes whether or not this edge was created by
	 *            the engine; true means NOT created by the engine
	 */
    @Constructor
    public PotentialMatchRecord(
            @ConstructorParameter(propertyName="mungeProcess") MungeProcess mungeProcess,
            @ConstructorParameter(propertyName="matchStatus") MatchType matchStatus,
            @ConstructorParameter(propertyName="referencedRecord") SourceTableRecord referencedRecord,
            @ConstructorParameter(propertyName="directRecord") SourceTableRecord directRecord,
            @ConstructorParameter(propertyName="synthetic") boolean synthetic) {
        this.mungeProcess = mungeProcess;
        this.matchStatus = matchStatus;
        this.referencedRecord = referencedRecord;
        this.directRecord = directRecord;
        this.synthetic = synthetic;
        master = MasterSide.NEITHER;
        this.storeState = StoreState.NEW;
        setName("PotentialMatchRecord");
    }

    /**
	 * The current storage state of this match record.
	 */
    @Accessor
    public StoreState getStoreState() {
    	return storeState;
    }

    /**
	 * Modifies the current storage state of this match record.
	 */
    public void setStoreState(StoreState newState) {
    	StoreState old = newState;
    	storeState = newState;
    	firePropertyChange("storeState", old, newState);
    }
    
    /**
     * Sets the store state to dirty only if it was already clean.  If it was
     * new or deleted, it will be left alone.
     */
    private void markDirty() {
        if (pool != null) {
            pool.recordChangedState(this);
        }
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
    @Accessor
    public MatchType getMatchStatus() {
        return matchStatus;
    }

    /**
     * If the match status is no match or unmatch, sets the master as undecided
     * @param matchStatus the type of Match this represents
     */
    @Mutator
    public void setMatchStatus(MatchType matchStatus) {
        if (this.matchStatus == matchStatus) return;
        MatchType old = this.matchStatus;
    	this.matchStatus = matchStatus;
    	firePropertyChange("matchStatus", old, matchStatus);
    	logger.debug("matchStatus was set to " + matchStatus);
        if (matchStatus == MatchType.NOMATCH || matchStatus == MatchType.UNMATCH){
        	setMaster(MasterSide.NEITHER);
        }
        markDirty();
    }

    @Accessor
    public MungeProcess getMungeProcess() {
        return mungeProcess;
    }

    @Accessor
    public SourceTableRecord getReferencedRecord() {
        return referencedRecord;
    }

    @Accessor
    public SourceTableRecord getDirectRecord() {
        return directRecord;
    }

    @NonProperty
    public MatchPool getPool() {
        return (MatchPool)getParent();
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
    @NonProperty
    public void setMasterRecord(SourceTableRecord newMaster, boolean isAutoMatch){
    	MasterSide oldMaster = master;
        if (newMaster == null){
        	setMaster(MasterSide.NEITHER);
            setMatchStatus(MatchType.UNMATCH);
        } else if (directRecord.equals(newMaster)) {
            setMaster(MasterSide.REFERENCED_SIDE);
            if (!isAutoMatch) {
            	setMatchStatus(MatchType.MATCH);
            } else {
            	setMatchStatus(MatchType.AUTOMATCH);
            }
        } else if (referencedRecord.equals(newMaster)) {
        	setMaster(MasterSide.DIRECT_SIDE);
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
     * Set the value of the master property
     */
    @Mutator
    public void setMaster(MasterSide s) {
    	MasterSide old = master;
    	master = s;
    	firePropertyChange("master", old, s);
    }
    
    /** 
     * Similar to {@link #setMasterRecord(SourceTableRecord, boolean)} except the isAutoMatch
     * boolean flag is set to false by default. DO NOT use this version if you are performing an AutoMatch!
     */
    @NonProperty
    public void setMasterRecord(SourceTableRecord newMaster) {
    	setMasterRecord(newMaster, false);
    }

    @NonProperty
    public boolean isReferencedMaster() {
        if (master == MasterSide.REFERENCED_SIDE) {
            return true;
        } else {
            return false;
        }
    }
    
    @NonProperty
    public boolean isDirectMaster() {
        if (master == MasterSide.DIRECT_SIDE) {
            return true;
        } else {
            return false;
        }
    }

    @NonProperty
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
    @NonProperty
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
    @NonProperty
    public SourceTableRecord getMasterRecord() {
        if(master == MasterSide.NEITHER){
            return null;
        } else if (master == MasterSide.DIRECT_SIDE){
            return referencedRecord;
        } else if (master == MasterSide.REFERENCED_SIDE){
            return directRecord;
        } else {
            throw new IllegalStateException("Invalid master state: " + master);
        }
    }
    
    /**
     * Returns the SourceTableRecord that is the master indicated by this record.
     *  
     * @return the master SourceTableRecord or null if no master has been specified
     * @throws IllegalStateException if the master is not en element of MasterSide
     */
    @Accessor
    public MasterSide getMaster() {
        return master;
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
            if (master == MasterSide.DIRECT_SIDE){
                return directRecord;
            } else if (master == MasterSide.REFERENCED_SIDE){
                return referencedRecord;
            } else {
                throw new IllegalStateException("Invalid master state: " + master);
            }
        }
    }
    
    @Override
    public String toString() {
        return "PotentialMatch: origLhs="+referencedRecord+
        "; origRhs="+directRecord+
        "; matchStatus="+matchStatus+
        "; masterSide="+master;
    }

	@Accessor
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
		if ((referencedRecord == null ? other.referencedRecord == null : referencedRecord.equals(other.referencedRecord))
				&& (directRecord == null ? other.directRecord == null : directRecord.equals(other.directRecord))) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		int result = 17;
		result = 37*result + (referencedRecord == null ? 0 : referencedRecord.hashCode());
		result = 37*result + (directRecord == null ? 0 : directRecord.hashCode());
		return result;
	}

	@Override
	public MatchMakerObject duplicate(MatchMakerObject parent) {
		return null;
	}

	@Override
	@NonProperty
	public List<? extends SPObject> getChildren() {
		return Collections.emptyList();
	}

	@Override
	@NonProperty
	public List<Class<? extends SPObject>> getAllowedChildTypes() {
		return allowedChildTypes;
	}
}