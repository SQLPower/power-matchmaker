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

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;
import ca.sqlpower.matchmaker.graph.BreadthFirstSearch;
import ca.sqlpower.matchmaker.graph.DijkstrasAlgorithm;
import ca.sqlpower.matchmaker.graph.GraphConsideringOnlyGivenNodes;
import ca.sqlpower.matchmaker.graph.GraphModel;
import ca.sqlpower.matchmaker.graph.MatchPoolGraphModel;
import ca.sqlpower.matchmaker.graph.NonDirectedUserValidatedMatchPoolGraphModel;

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
     * @throws ArchitectException if SQLObjects fail to populate its children
     */
    public void findAll() throws SQLException, ArchitectException {
        
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
                addPotentialMatch(
                    new PotentialMatchRecord(criteriaGroup, matchStatus, lhs, rhs));
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
            node = new SourceTableRecord(session, match, keyValues);
            addSourceTableRecord(node);
        }
        return node;
    }
    
    /**
     * Adds the given source table record to this match pool.  This is normally only
     * done from the test suite, which sets up various scenarios to test.  In real life,
     * source table records get added to the match pool on demand, and they come directly
     * from SQL SELECTs on the source table.  See {@link #findAll()} for details.
     * 
     * @param str The record to add. Its parent pool will be modified to point to
     * this pool.
     */
    public void addSourceTableRecord(SourceTableRecord str) {
    	str.setPool(this);
        sourceTableRecords.put(str.getKeyValues(), str);
    }

    /**
     * Adds the given potential match to this pool.  This is normally only
     * done from the test suite, which sets up various scenarios to test.  In real life,
     * source table records get added to the match pool via a
     * SQL SELECT on the match result table.  See {@link #findAll()} for details.
     * 
     * @param pmr The record to add
     */
    public void addPotentialMatch(PotentialMatchRecord pmr) {
    	pmr.setPool(this);
    	potentialMatches.add(pmr);
    	pmr.getOriginalLhs().addPotentialMatch(pmr);
        pmr.getOriginalRhs().addPotentialMatch(pmr);
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
    
    /**
	 * Gets the first PotentialMatchRecord in the pool that has the given nodes
	 * as its original left and right nodes. Null is returned if no PotentialMatchRecord
	 * is found.
	 */
    public PotentialMatchRecord getPotentialMatchFromOriginals(SourceTableRecord node1, SourceTableRecord node2) {
    	for (PotentialMatchRecord pmr : potentialMatches) {
    		if (pmr.getOriginalLhs() == node1 && pmr.getOriginalRhs() == node2 
    				|| pmr.getOriginalLhs() == node2 && pmr.getOriginalRhs() == node1) {
    			return pmr;
    		}
    	}
    	return null;
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
     * @throws ArchitectException if the source table index can't be populated
     */
    private void deleteRedundantMatchRecords() throws SQLException, ArchitectException {
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
    
    public SourceTableRecord getSourceTableRecord(List<Object> key) {
    	return sourceTableRecords.get(key);
    }

    /**
	 * Locates all records which are currently reachable from this record and
	 * the given (formerly potential) duplicate of it by user-validated matches,
	 * and points them to this record as the master (all the reachable records
	 * will be considered duplicates of this "offical version of the truth").
	 * All nodes in the decided edges of the duplicate will have their master
	 * set to the duplicate, or a duplicate of the duplicate, etc.
	 * 
	 * The graph used in this method should not have cycles in it as our output
	 * is guaranteed to not have cycles.
	 * 
	 * This method is done in several steps.
	 * <ol>
	 * <li>Set the edge between the duplicate and master to be undecided or
	 * unmatched if the match exists. This is for the case that the duplicate
	 * was originally the master and we don't want to follow this path when
	 * looking for the ultimate master.
	 * <li>Find a graph of all the nodes reachable from the master and
	 * duplicate along decided edges. The graph will include only these nodes
	 * and only the edges connecting these nodes, whether they are decided or
	 * not.
	 * <li>Find the ultimate master, the master of the chain of nodes that has
	 * no master itself, from the master node. Note: If there is a cycle in the
	 * graph then the node decided to be the ultimate master may be surprising
	 * for the user as it may not be the most obvious one but we guarantee that
	 * there will be no cycles in the end result.
	 * <li>Using Dijkstra's algorithm, using all edges in the graph, find the
	 * shortest path to all nodes in the graph. If the duplicate cannot be
	 * reached at this point then a synthetic edge is needed between the
	 * duplicate and the master and Dijkstra's algorithm needs to be run again.
	 * <li>Turn all of the edges walked using Dijkstra's algorithm into decided
	 * edges pointing in the direction of the ultimate master. All other edges
	 * will be undecided (or UNMATCH).
	 * </ol>
	 */
    public void defineMaster(SourceTableRecord master, SourceTableRecord duplicate) {
    	if (duplicate == master) {
    		defineMasterOfAll(master);
    		return;
    	}

    	logger.debug("DefineMaster: master="+master+"; duplicate="+duplicate);
    	
    	logger.debug("Remove the master from the edge between the master and duplicate if the master exists.");
    	PotentialMatchRecord potentialMatch = getPotentialMatchFromOriginals(master, duplicate);
    	if (potentialMatch != null) {
    		potentialMatch.setMaster(null);
    	}

    	logger.debug("Create the graph that contains only nodes reachable by decided edges");
    	GraphModel<SourceTableRecord, PotentialMatchRecord> nonDirectedGraph =
    		new NonDirectedUserValidatedMatchPoolGraphModel(this, new HashSet<PotentialMatchRecord>());
    	BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord> bfs =
            new BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord>();
        Set<SourceTableRecord> reachable = new HashSet<SourceTableRecord>(bfs.performSearch(nonDirectedGraph, master));
        reachable.addAll(bfs.performSearch(nonDirectedGraph, duplicate));

        GraphModel<SourceTableRecord, PotentialMatchRecord> considerGivenNodesGraph =
        	new GraphConsideringOnlyGivenNodes(this, reachable);
        logger.debug("Graph contains " + considerGivenNodesGraph.getNodes() + " nodes.");

        logger.debug("Find the ultimate master");
        //We need to keep track of where we've been so we don't go back to old possible ultimate masters
        //in the case of cycles.
        SourceTableRecord ultimateMaster = master;
        List<SourceTableRecord> nodesCrossed = new ArrayList<SourceTableRecord>();
        nodesCrossed.add(duplicate); //Don't want to set the duplicate as the ultimate master if we can get here somehow.
        while (!considerGivenNodesGraph.getOutboundEdges(ultimateMaster).isEmpty()) {
        	logger.debug("The outbound edges for " + ultimateMaster + " is " + considerGivenNodesGraph.getOutboundEdges(ultimateMaster));
        	nodesCrossed.add(ultimateMaster);
        	
        	SourceTableRecord newUltimateMaster = ultimateMaster;
        	for (PotentialMatchRecord pmr : considerGivenNodesGraph.getOutboundEdges(ultimateMaster)) {
        		if (pmr.getOriginalLhs() != ultimateMaster) {
        			if (!nodesCrossed.contains(pmr.getOriginalLhs())) {
        				newUltimateMaster = pmr.getOriginalLhs();
        				break;
        			}
        		} else {
        			if (!nodesCrossed.contains(pmr.getOriginalRhs())) {
        				newUltimateMaster = pmr.getOriginalRhs();
        				break;
        			}
        		}
        	}
        	if (newUltimateMaster == ultimateMaster) {
        		break;
        	}
        	ultimateMaster = newUltimateMaster;
        }
        logger.debug("The ultimate master is " + ultimateMaster);
        
        logger.debug("Find the shortest path to all nodes in the graph");
    	DijkstrasAlgorithm da = new DijkstrasAlgorithm<SourceTableRecord, PotentialMatchRecord>();
    	Map<SourceTableRecord, SourceTableRecord> masterMapping = da.calculateShortestPaths(considerGivenNodesGraph, ultimateMaster);
    	
    	if (masterMapping.get(duplicate) == null) {
    		logger.debug("We could not reach the duplicate from the ultimate master in the current graph. A synthetic edge will be created");
    		MatchMakerCriteriaGroup syntheticCriteria = match.getMatchCriteriaGroupByName(MatchMakerCriteriaGroup.SYNTHETIC_MATCHES);
    		if (syntheticCriteria == null) {
    			syntheticCriteria = new MatchMakerCriteriaGroup();
    			syntheticCriteria.setName(MatchMakerCriteriaGroup.SYNTHETIC_MATCHES);
    			match.addMatchCriteriaGroup(syntheticCriteria);
    		}
    		addPotentialMatch(
                    new PotentialMatchRecord(syntheticCriteria, MatchType.UNMATCH, master, duplicate));
    		//XXX we still need to store the new potential match in the database.
    		masterMapping = da.calculateShortestPaths(considerGivenNodesGraph, ultimateMaster);
    	}
    	
    	logger.debug("Removing all decided edges from the given graph");
    	for (PotentialMatchRecord pmr : potentialMatches) {
    		if (considerGivenNodesGraph.getEdges().contains(pmr.getOriginalLhs()) && considerGivenNodesGraph.getEdges().contains(pmr.getOriginalRhs())) {
    			pmr.setMaster(null);
    		}
    	}
    	
    	logger.debug("Setting the new decided edges for this graph of nodes");
    	//XXX This is a fairly poor way of obtaining the potential match records. We should be able to make this faster.
    	for (Map.Entry<SourceTableRecord, SourceTableRecord> nodeMasterPair : masterMapping.entrySet()) {
    		logger.debug("Setting " + nodeMasterPair.getValue() + " to be the master of " + nodeMasterPair.getKey());
    		PotentialMatchRecord matchRecord = getPotentialMatchFromOriginals(nodeMasterPair.getValue(), nodeMasterPair.getKey());
   			matchRecord.setMaster(nodeMasterPair.getValue());
    	}
	}

    /**
	 * This method defines the given node to be the master of all nodes
	 * reachable by either a defined or undefined path. We use Dijkstra's
	 * algorithm to find the shortest path to the nodes and to make sure
	 * that we have no cycles.
	 */
	public void defineMasterOfAll(SourceTableRecord master) {
		GraphModel<SourceTableRecord, PotentialMatchRecord> poolGraph = new MatchPoolGraphModel(this);
		BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord> bfs =
            new BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord>();
        Set<SourceTableRecord> reachable = new HashSet<SourceTableRecord>(bfs.performSearch(poolGraph, master));
        GraphModel<SourceTableRecord, PotentialMatchRecord> considerGivenNodesGraph =
        	new GraphConsideringOnlyGivenNodes(this, reachable);
        
        DijkstrasAlgorithm da = new DijkstrasAlgorithm<SourceTableRecord, PotentialMatchRecord>();
    	Map<SourceTableRecord, SourceTableRecord> masterMapping = da.calculateShortestPaths(considerGivenNodesGraph, master);
    	
    	for (PotentialMatchRecord pmr : potentialMatches) {
    		if (considerGivenNodesGraph.getEdges().contains(pmr.getOriginalLhs()) && considerGivenNodesGraph.getEdges().contains(pmr.getOriginalRhs())) {
    			pmr.setMaster(null);
    		}
    	}
    	
    	//XXX This is a fairly poor way of obtaining the potential match records. We should be able to make this faster.
    	for (Map.Entry<SourceTableRecord, SourceTableRecord> nodeMasterPair : masterMapping.entrySet()) {
    		logger.debug("Setting " + nodeMasterPair.getValue() + " to be the master of " + nodeMasterPair.getKey());
    		PotentialMatchRecord matchRecord = getPotentialMatchFromOriginals(nodeMasterPair.getValue(), nodeMasterPair.getKey());
   			matchRecord.setMaster(nodeMasterPair.getValue());
    	}
	}
	
	/**
	 * This sets the two given SourceTableRecords to have no match between them.
	 */
    public void defineNoMatch(SourceTableRecord record1, SourceTableRecord record2) {
        PotentialMatchRecord pmr = getPotentialMatchFromOriginals(record1, record2);
        if (pmr != null) {
        	pmr.setMatchStatus(MatchType.NOMATCH);
        }
    }
}
