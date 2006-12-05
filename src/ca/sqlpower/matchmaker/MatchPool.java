package ca.sqlpower.matchmaker;

import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;

/**
 * The MatchPool class represents the set of matching records for
 * a particular Match instance.
 */
public class MatchPool {
    
    private final Match match;
    private final List<PotentialMatchRecord> potentialMatches;
    private boolean isMaster;
    
    public MatchPool (Match match){
        this.match = match;
        potentialMatches = new ArrayList<PotentialMatchRecord>();
    }
    
    public MatchPool(Match match, List<PotentialMatchRecord> potentialMatches){
        this.match = match;
        this.potentialMatches = potentialMatches;
    }

    public Match getMatch() {
        return match;
    }
    
    
    public void addPotentialMatches(
            Match match, MatchMakerCriteriaGroup matchGroup, MatchType matchStatus) {
        if (match!= null){
            potentialMatches.add(new PotentialMatchRecord(
                    MatchPool.this,matchGroup, matchStatus));
        }
    }
    
 
   
    
    /**
     * Finds all the potentialMatchRecordInfo that has the passed in groupName and
     * update the status of the potentialMatchRecordInfo 
     * 
     * @param matchGroup the name of the match group that is to be updated 
     * @param newMatchType the new status set to the group
     */
    public void updateStatusToMatchGroup(String matchGroup, MatchType newMatchType){
        for (PotentialMatchRecord pmri : getAllPotentialMatchByMatchGroupName(matchGroup)) {
            pmri.setMatchStatus(newMatchType);
        }
    }
    
    public List<PotentialMatchRecord> getAllPotentialMatchByMatchGroupName
                        (String matchGroupName) {
        List<PotentialMatchRecord> matchList =
            new ArrayList<PotentialMatchRecord>();
        for (PotentialMatchRecord pmr : potentialMatches){
            if (pmr.getCriteriaGroup().getName().equals(matchGroupName)){
                matchList.add(pmr);
            }
        }
        return matchList;
    }
    
    public void removePotentialMatchesInMatchGroup(String groupName){
        potentialMatches.removeAll(getAllPotentialMatchByMatchGroupName(groupName));        
    }
    
    public List<PotentialMatchRecord> getPotentialMatches() {
        return potentialMatches;
    }
}
