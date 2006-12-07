package ca.sqlpower.matchmaker;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;

/**
 * The MatchPool class represents the set of matching records for
 * a particular Match instance.  Taken together, it is a graph of
 * matching (and potentially matching) source table records, with
 * the edges between those records represented by the list of
 * PotentialMatchRecords.
 */
public class MatchPool {
    
    private static final Logger logger = Logger.getLogger(MatchPool.class);
    
    private final Match match;
    
    private final MatchMakerSession session;
    
    /**
     * The edge list for this graph.
     */
    private final List<PotentialMatchRecord> potentialMatches;
    
    public MatchPool(Match match) {
        this(match, new ArrayList<PotentialMatchRecord>());
    }
    
    public MatchPool(Match match, List<PotentialMatchRecord> potentialMatches) {
        this.match = match;
        this.session = match.getSession();
        this.potentialMatches = potentialMatches;
    }

    public Match getMatch() {
        return match;
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
    
    public void findAll() throws SQLException, ArchitectException {
        SQLTable resultTable = match.getResultTable();
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        String lastSQL = null;
        try {
            con = session.getConnection();
            stmt = con.createStatement();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ");
            boolean first = true;
            for (SQLColumn col : resultTable.getColumns()) {
                if (!first) sql.append(", ");
                sql.append(col.getName());
                first = false;
            }
            sql.append("\n FROM ");
            sql.append(DDLUtils.toQualifiedName(resultTable));           
            lastSQL = sql.toString();
            rs = stmt.executeQuery(lastSQL);
            while (rs.next()) {
                MatchMakerCriteriaGroup criteriaGroup = match.getMatchCriteriaGroupByName(rs.getString("GROUP_ID"));
                if (criteriaGroup == null) {
                    session.handleWarning(
                            "Found a match record that refers to the " +
                            "non-existant criteria group \""+rs.getString("GROUP_ID")+
                            "\". Ignoring it.");
                    continue;
                }
                String statusCode = rs.getString("MATCH_STATUS");
                MatchType matchStatus = MatchType.typeForCode(statusCode);
                if (statusCode != null && matchStatus == null) {
                    session.handleWarning(
                            "Found a match record with the " +
                            "unknown/invalid match status \""+statusCode+
                            "\". Ignoring it.");
                    continue;
                }
                int indexSize = match.getSourceTableIndex().getChildCount();
                List<Object> lhsKeyValues = new ArrayList<Object>(indexSize);
                List<Object> rhsKeyValues = new ArrayList<Object>(indexSize);
                for (int i = 0; i < indexSize; i++) {
                    lhsKeyValues.add(rs.getObject("DUP_CANDIDATE_1"+i));
                    rhsKeyValues.add(rs.getObject("DUP_CANDIDATE_2"+i));
                }
                SourceTableRecord lhs = new SourceTableRecord(session, match, lhsKeyValues);
                SourceTableRecord rhs = new SourceTableRecord(session, match, rhsKeyValues);
                PotentialMatchRecord pmr =
                    new PotentialMatchRecord(this, criteriaGroup, matchStatus, lhs, rhs);                
                potentialMatches.add(pmr);
            }
            
        } catch (SQLException ex) {
            logger.error("Error in query: "+lastSQL, ex);
            session.handleWarning(
                    "Error in SQL Query!" +
                    "\nMessage: "+ex.getMessage() +
                    "\nSQL State: "+ex.getSQLState() +
                    "\nQuery: "+lastSQL);
            throw ex;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ex) { logger.error("Couldn't close result set", ex); }
            if (stmt != null) try { stmt.close(); } catch (SQLException ex) { logger.error("Couldn't close statement", ex); }
            if (con != null) try { con.close(); } catch (SQLException ex) { logger.error("Couldn't close connection", ex); }
        }

    }
    
    public List<PotentialMatchRecord> getPotentialMatches() {
        return potentialMatches;
    }
}
