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
    private final MatchPool pool;
    
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
     * considered the "master."
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
     * One of the two records currently identified as a potential duplicate.
     */
    private SourceTableRecord lhs;
    
    /**
     * One of the two records currently identified as a potential duplicate.
     */
    private SourceTableRecord rhs;

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
    
    public PotentialMatchRecord(
            MatchPool pool,
            MatchMakerCriteriaGroup criteriaGroup,
            MatchType matchStatus,
            SourceTableRecord originalLhs,
            SourceTableRecord originalRhs) {
        this.pool = pool;    
        this.criteriaGroup = criteriaGroup;
        this.matchStatus = matchStatus;
        this.originalLhs = originalLhs;
        this.originalRhs = originalRhs;
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
     * NOTE: If the master is undecided, left hand side SourceTableRecord is considered
     * the master.
     *  
     * @return the master SourceTableRecord or the left-hand side record if no master
     * is declared.
     */
    public SourceTableRecord getMaster() {
        if (isRhsMaster()){
            return rhs;
        } else {
            return lhs;
        }
    }
    
    /**
     * Returns the SourceTableRecord that is the duplicate indicated by this record.
     * NOTE: If the master is undecided, left hand side SourceTableRecord is considered
     * the master.
     *  
     * @return the duplicate (non-master) SourceTableRecord or the right-hand side record
     * if no master is declared.
     */
    public SourceTableRecord getDuplicate() {
        if (!isRhsMaster()){
            return rhs;
        } else {
            return lhs;
        }
    }
}
