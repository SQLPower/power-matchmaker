package ca.sqlpower.matchmaker;

import java.util.List;

/**
 * This object represents one row in a Match Source Table, which is half of
 * a row in the Match Output ("cand dup") table.
 */
public class PotentialMatchRecord {

    private MatchPool pool;
    private MatchMakerCriteriaGroup criteriaGroup;
    private MatchType matchStatus;
    private MasterSide master;
    
    private SourceTableRecord lhs;
    private SourceTableRecord rhs;
    /**
     * The values of the unique index columns in the same order as the
     * Index Column objects in the source table's index.  This lets us
     * select the entire match source record when we need it.
     */
    private List<Object> keyValues;
    
    
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
        AUTOMATCH("AUTO_MATCH"),
        NOMATCH("NO_MATCH"),
        MATCH("MATCH"),
        MERGED("MERGED"),
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
    
    public PotentialMatchRecord(MatchPool pool, MatchMakerCriteriaGroup criteriaGroup, MatchType matchStatus){
        this.pool = pool;    
        this.criteriaGroup = criteriaGroup;
        this.matchStatus = matchStatus;
    }

    public MatchType getMatchStatus() {
        return matchStatus;
    }

    public void setMatchStatus(MatchType matchStatus) {
        this.matchStatus = matchStatus;
    }


    public MatchMakerCriteriaGroup getCriteriaGroup() {
        return criteriaGroup;
    }

    public SourceTableRecord getLhs() {
        return lhs;
    }

    public void setLhs(SourceTableRecord lhs) {
        this.lhs = lhs;
    }

    public SourceTableRecord getRhs() {
        return rhs;
    }

    public void setRhs(SourceTableRecord rhs) {
        this.rhs = rhs;
    }

    public void setCriteriaGroup(MatchMakerCriteriaGroup criteriaGroup) {
        this.criteriaGroup = criteriaGroup;
    }
    /**
     * Set the master record to the source table record passed in.  If the
     * value passed is null it sets neither as the master record.  If 
     * newMaster is not in this potential match record it throws an
     * illegal argument exception.
     * 
     * @param newMaster the source table record that is participating in this
     *                     potential match that you want to make the master.
     */
    public void setMaster(SourceTableRecord newMaster){
        if (newMaster== null){
            master = MasterSide.NEITHER;
        } else if (rhs.equals(newMaster)) {
            master = MasterSide.RIGHT_HAND_SIDE;
        } else if (lhs.equals(newMaster)) {
            master = MasterSide.LEFT_HAND_SIDE;
        } else {
            throw new IllegalArgumentException("The source table record "+ newMaster + " is not part of this record");
        }  
    }
    
    public boolean isRHSMaster() {
        if (master == MasterSide.RIGHT_HAND_SIDE) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isLHSMaster() {
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
}
