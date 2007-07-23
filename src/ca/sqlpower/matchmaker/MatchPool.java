package ca.sqlpower.matchmaker;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

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
    private final Set<PotentialMatchRecord> potentialMatches;

    /**
     * A map of keys to node instances for this graph.  The values() set of
     * this map is the node set for the graph.
     */
    private final Map<List<Object>, SourceTableRecord> sourceTableRecords =
        new HashMap<List<Object>, SourceTableRecord>();
    
    public MatchPool(Match match) {
        this(match, new HashSet<PotentialMatchRecord>());
    }
    
    public MatchPool(Match match, Set<PotentialMatchRecord> potentialMatches) {
        this.match = match;
        this.session = match.getSession();
        this.potentialMatches = potentialMatches;
    }

    public Match getMatch() {
        return match;
    }
   
    public List<PotentialMatchRecord> getAllPotentialMatchByMatchCriteriaGroup
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
          
    /**
     * Finds all the potential match record (edges in the graph) that belongs to the
     * particular match group
     * @param matchGroupName
     * @return a list of potential match records that belong to the match critieria group
     */
    public List<PotentialMatchRecord> getAllPotentialMatchByMatchCriteriaGroup(MatchMakerCriteriaGroup criteriaGroup) {
        List<PotentialMatchRecord> matchList =
            new ArrayList<PotentialMatchRecord>();
        for (PotentialMatchRecord pmr : potentialMatches){
            if (pmr.getCriteriaGroup() == criteriaGroup){
                matchList.add(pmr);
            }
        }
        return matchList;
    }
    
    public void removePotentialMatchesInMatchGroup(String groupName){
        potentialMatches.removeAll(getAllPotentialMatchByMatchCriteriaGroup(groupName));        
    }
    
    /**
     * Executes SQL statements to initialize nodes {@link SourceTableRecord} and 
     * edges {@link PotentialMatchRecord}.
     * <p>
     * IMPORTANT NOTE ABOUT SIDE EFFECTS: before searching the table, this method will
     * attempt to remove redundant records from the match result table.  Its name implies
     * that it only reads the database.  This is not the case.  For details, see
     * {@link #deleteRedundantMatchRecords()}.
     * 
     * @throws SQLException if an unexpected error occurred running the SQL statements
     */
    public void findAll() throws SQLException {
        
        deleteRedundantMatchRecords();
        
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
                SourceTableRecord lhs = makeSourceTableRecord(lhsKeyValues);
                SourceTableRecord rhs = makeSourceTableRecord(rhsKeyValues);
                PotentialMatchRecord pmr =
                    new PotentialMatchRecord(this, criteriaGroup, matchStatus, lhs, rhs);                
                potentialMatches.add(pmr);
                lhs.addPotentialMatch(pmr);
                rhs.addPotentialMatch(pmr);
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
    
    /**
     * Attempts to look up the existing SourceTableRecord instance in
     * the cache, but makes a new one and puts it in the cache if not found.
     * 
     * @param keyValues The values for this record's unique index
     * @return The source table record that corresponds with the given key values.
     * The return value is never null.
     */
    private SourceTableRecord makeSourceTableRecord(List<Object> keyValues) {
        SourceTableRecord node = sourceTableRecords.get(keyValues);
        if (node == null) {
            node = new SourceTableRecord(session, match, this, keyValues);
            sourceTableRecords.put(keyValues, node);
        }
        return node;
    }

    /**
     * Returns the set of PotentialMatchRecords in this match pool.  
     * Before calling this, you should populate the pool by calling
     * one of the findXXX() methods.
     * <p>
     * Potential Match records are the edges of this graph of matching records.
     * For the nodes, see {@link #getSourceTableRecords()}.
     * 
     * @return The current list of potential match records.
     */
    public Set<PotentialMatchRecord> getPotentialMatches() {
        return potentialMatches;
    }
    
    public Collection<SourceTableRecord> getSourceTableRecords() {
        return Collections.unmodifiableCollection(sourceTableRecords.values());
    }
    
    /**
     * For historical reasons, the match engine populates the result table
     * with two copies of each match record: one a-b, and one b-a.  We don't
     * want to deal with this duplication (isn't this a de-duping tool?), so
     * this method is designed to de-dupe the de-duping table.
     * @throws SQLException if there is a problem executing the DELETE statement
     *
     */
    private void deleteRedundantMatchRecords() throws SQLException {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        String lastSQL = null;
        try {
            con = session.getConnection();
            stmt = con.createStatement();
            StringBuilder sql = new StringBuilder();

            sql.append("DELETE FROM ").append(DDLUtils.toQualifiedName(match.getResultTable())).append(" M1");
            sql.append("\n WHERE EXISTS( SELECT 1 FROM ").append(DDLUtils.toQualifiedName(match.getResultTable())).append(" M2");
            sql.append("\n  WHERE ");
            for (int i = 0; i < match.getSourceTableIndex().getChildCount(); i++) {
                if (i > 0) sql.append("\n   AND ");
                sql.append("M1.DUP_CANDIDATE_1").append(i).append(" = M2.DUP_CANDIDATE_2").append(i);
                sql.append("\n AND ");
                sql.append("M1.DUP_CANDIDATE_2").append(i).append(" = M2.DUP_CANDIDATE_1").append(i);
                sql.append("\n AND ");
                sql.append("M1.DUP_CANDIDATE_1").append(i).append(" < M2.DUP_CANDIDATE_1").append(i);
            }
            sql.append(")");
            
            lastSQL = sql.toString();
            stmt.executeUpdate(lastSQL);
            
            con.commit();
            
        } catch (SQLException ex) {
            logger.error("Error in query: "+lastSQL, ex);
            session.handleWarning(
                    "Error in SQL Statement!" +
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
}
