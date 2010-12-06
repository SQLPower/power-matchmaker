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

import java.beans.PropertyChangeEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.graph.BreadthFirstSearch;
import ca.sqlpower.graph.DijkstrasAlgorithm;
import ca.sqlpower.graph.GraphModel;
import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;
import ca.sqlpower.matchmaker.graph.GraphConsideringOnlyGivenNodes;
import ca.sqlpower.matchmaker.graph.NonDirectedUserValidatedMatchPoolGraphModel;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonBound;
import ca.sqlpower.object.annotation.NonProperty;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.util.TransactionEvent;

/**
 * The MatchPool class represents the set of matching records for
 * a particular Match instance.  Taken together, it is a graph of
 * matching (and potentially matching) source table records, with
 * the edges between those records represented by the list of
 * PotentialMatchRecords.
 * <p>
 * MatchPool is monitorable. The activity monitored is the process
 * of storing the match pool back to the database.
 */
public class MatchPool extends MatchMakerMonitorableImpl {
    
    private static final Logger logger = Logger.getLogger(MatchPool.class);

	/**
	 * Defines an absolute ordering of the child types of this class.
	 */
	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(MatchCluster.class)));
	
    static final int DEFAULT_BATCH_SIZE = 1000;
    static final int DEFAULT_LIMIT = 5;
    
    /**
     * List of the clusters that the match pool is currently showing or saving (also its children)
     */
    private List<MatchCluster> matchClusters = new ArrayList<MatchCluster>();
    
    /**
     * A flag to keep track of whether or not to use batch updates when writing to the database.
     */
    private boolean useBatchUpdates;
    
    /**
     * A flag to keep track of whether or not we want to use debug mode.
     */
    private boolean debug;
    
    /**
     * This integer keeps track of the number of results we wish to display to the user at a time on the
     * match result visualizer screen.
     */
    private int limit;
    
    /**
     * This integer keeps track of which results we are currently displaying.
     */
    private int currentMatchNumber;
    
    /**
     * This holds the total number of clusters calculated from the last save to the
     * database.
     */
    private int clusterCount;

	/**
     * This reads the database or server and finds all of the potential matches in community edition.
     */
    private MatchPoolReader matchPoolReader;
    
    /**
     * This stores the found matches to the database or server.
     */
    private DatabaseMatchPoolListener matchPoolListener;

	private List<SQLColumn> displayColumns;
	
	/**
	 * This method sort a list of potential match records and source table records and sorts
	 * them into their respectice match clusters.
	 */
	public static List<MatchCluster> sortMatches(Collection<SourceTableRecord> sourceTableRecords, 
			Collection<PotentialMatchRecord> potentialMatchRecords) {
		
		/*
		 * First create a map of source table records to their potential match records. This
		 * should speed up this process by having them all already found.
		 */
		
		Map<SourceTableRecord, Set<PotentialMatchRecord>> recordMap
			= new HashMap<SourceTableRecord, Set<PotentialMatchRecord>>();
		
		for(SourceTableRecord src : sourceTableRecords) {
			recordMap.put(src, new HashSet<PotentialMatchRecord>());
		}
		
		for(PotentialMatchRecord pmr : potentialMatchRecords) {
			recordMap.get(pmr.getOrigLHS()).add(pmr);
			recordMap.get(pmr.getOrigRHS()).add(pmr);
		}
		
		//Now we can sort the matches
		
		List<MatchCluster> matchClusters = new ArrayList<MatchCluster>();
		Queue<SourceTableRecord> untouchedNodes = new LinkedList<SourceTableRecord>();
		untouchedNodes.addAll(sourceTableRecords);
		Queue<SourceTableRecord> stillToProcess = new LinkedList<SourceTableRecord>();
		MatchCluster mc = null;
		do {
			if(!stillToProcess.isEmpty()) {
				SourceTableRecord src = stillToProcess.poll();
				if(untouchedNodes.contains(src)) {
					untouchedNodes.remove(src);
				}
				List<SourceTableRecord> neighbours = new ArrayList<SourceTableRecord>();
				for(PotentialMatchRecord pmr : recordMap.get(src)) {
					SourceTableRecord foundSrc;
					if(pmr.getOrigRHS() == src) {
						foundSrc = pmr.getOrigLHS();
					} else {
						foundSrc = pmr.getOrigRHS();
					}
					if(!mc.getSourceTableRecords().contains(foundSrc)) {
						mc.addSourceTableRecord(foundSrc);
						neighbours.add(foundSrc);
					}
					if(untouchedNodes.contains(foundSrc)) {
						untouchedNodes.remove(foundSrc);
					}
					mc.addPotentialMatchRecord(pmr);
				}
				stillToProcess.addAll(neighbours);
				mc.addSourceTableRecord(src);
			} else {
				if(!untouchedNodes.isEmpty()) {
					stillToProcess.add(untouchedNodes.poll());
					if (mc != null) {
						matchClusters.add(mc);
					}
					mc = new MatchCluster();
				}
			}
		} while(!untouchedNodes.isEmpty() || !stillToProcess.isEmpty());
		if (mc != null) {
			matchClusters.add(mc);
		}
		return matchClusters;
	}
	
    public MatchPool() {
        this.limit = DEFAULT_LIMIT;
        this.clusterCount = 0;
        this.currentMatchNumber = 0;
        matchPoolReader = new DatabaseMatchPoolReader(this);
        matchPoolListener = new DatabaseMatchPoolListener(this);
        addSPListener(matchPoolListener);
        addSPListener(matchListener);
    }

	@NonProperty
    public Project getProject() {
        return (Project)getParent();
    }
   
    /**
     * Finds all the potential match record (edges in the graph) that belongs to the
     * particular munge process
     * @param mungeProcessName
     * @return a list of potential match records that belong to the munge process
     */
	@NonProperty
    public List<PotentialMatchRecord> getAllPotentialMatchByMungeProcess (String mungeProcessName) {
        List<PotentialMatchRecord> matchList =
            new ArrayList<PotentialMatchRecord>();
        for(MatchCluster mc : matchClusters) {
	        for (PotentialMatchRecord pmr : mc.getPotentialMatchRecords()){
	            if (pmr.getMungeProcess().getName().equals(mungeProcessName)){
	                matchList.add(pmr);
	            }
	        }
        }
        return matchList;
    }
          
    /**
     * Finds all the potential match record (edges in the graph) that belongs to the
     * particular munge process
     * @param mungeProcess
     * @return a list of potential match records that belong to the munge process
     */
	@NonProperty
    public List<PotentialMatchRecord> getAllPotentialMatchByMungeProcess(MungeProcess mungeProcess) {
        List<PotentialMatchRecord> matchList =
            new ArrayList<PotentialMatchRecord>();
        for (PotentialMatchRecord pmr : getPotentialMatchRecords()){
            if (pmr.getMungeProcess() == mungeProcess){
                matchList.add(pmr);
            }
        }
        return matchList;
    }
    
    /**
     * Tells whichever reader is currently hooked up to read in a certain number of matches from
     * either its cache (if database) or server (if, kinda obviously, server).
     * 
     * @param displayColumns A list of which SQLColumns to use to represent a SourceTableRecord
     * in the user interface. If null or empty, then the default will be the SourceTableRecord's 
     * primary key.
     * 
     * @throws SQLException if an unexpected error occurred running the SQL statements
     * @throws SQLObjectException if SQLObjects fail to populate its children
     */
    public void find(List<SQLColumn> displayColumns) throws SQLException, SQLObjectException {
    	this.displayColumns = displayColumns;
		if (matchPoolReader != null) {
    		matchPoolReader.getClusters(currentMatchNumber, currentMatchNumber + limit);
    	}
    }
    
    /**
     * Tells whichever reader is currently hooked up to read in all of the old matches.
     * either its cache (if database) or server (if, kinda obviously, server).
     * 
     * @param displayColumns A list of which SQLColumns to use to represent a SourceTableRecord
     * in the user interface. If null or empty, then the default will be the SourceTableRecord's 
     * primary key.
     * 
     * @throws SQLException if an unexpected error occurred running the SQL statements
     * @throws SQLObjectException if SQLObjects fail to populate its children
     */
    public void findOld(List<SQLColumn> displayColumns) throws SQLException, SQLObjectException {
    	this.displayColumns = displayColumns;
		if (matchPoolReader != null) {
			 matchPoolReader.getAllPreviousMatches();
    	}
    }
    
    @NonProperty
    public List<SQLColumn> getDisplayColumns() {
    	return displayColumns;
    }

	@NonProperty
    public int getNumberOfPotentialMatches() {
    	int sum = 0;
    	for(MatchCluster mc : matchClusters) {
    		sum += mc.getPotentialMatchRecords().size();
    	}
    	return sum;
    }
    
    /**
	 * Gets the first PotentialMatchRecord in the pool that has the given nodes
	 * as its original left and right nodes. Null is returned if no PotentialMatchRecord
	 * is found.
	 */
	@NonProperty
    public PotentialMatchRecord getPotentialMatchFromOriginals(SourceTableRecord node1, SourceTableRecord node2) {
    	for (PotentialMatchRecord pmr : node1.getOriginalMatchEdges()) {
    		if (pmr.getOrigLHS() == node1 && pmr.getOrigRHS() == node2 
    				|| pmr.getOrigLHS() == node2 && pmr.getOrigRHS() == node1) {
    			return pmr;
    		}
    	}
    	return null;
    }

	@NonProperty
    public List<MatchCluster> getMatchClusters() {
        return Collections.unmodifiableList(matchClusters);
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
	 * looking for the ultimate master.</li>
	 * <li>Find a graph of all the nodes reachable from the master and
	 * duplicate along decided edges. The graph will include only these nodes
	 * and only the edges connecting these nodes, whether they are decided or
	 * not.</li>
	 * <li>Find the ultimate master, the master of the chain of nodes that has
	 * no master itself, from the master node. Note: If there is a cycle in the
	 * graph then the node decided to be the ultimate master may be surprising
	 * for the user as it may not be the most obvious one but we guarantee that
	 * there will be no cycles in the end result.</li>
	 * <li>Using Dijkstra's algorithm, using all edges in the graph, find the
	 * shortest path to all nodes in the graph. If the duplicate cannot be
	 * reached at this point then a synthetic edge is needed between the
	 * duplicate and the master and Dijkstra's algorithm needs to be run again.</li>
	 * <li>Turn all of the edges walked using Dijkstra's algorithm into decided
	 * edges pointing in the direction of the ultimate master. All other edges
	 * will be undecided (or UNMATCH).</li>
	 * </ol>
	 * <p>
	 * Additionally, there is an isAutoMatch boolean flag that should be used when
	 * the method is being called by the AutoMatch feature. In this case, all of the
	 * PotentialMatchRecords will have their match status set to AUTOMATCH, and the
	 * respective records in the match result table will also be updated as AUTOMATCH 
	 * @param master The SourceTableRecord that we are defining as the master
	 * @param duplicate The SourceTableRecord that we are defining as a duplicate of the master
	 * @param isAutoMatch Indicate that this method is being used by the AutoMatch feature
	 */
    public void defineMaster(SourceTableRecord master, SourceTableRecord duplicate, boolean isAutoMatch) throws SQLObjectException {
    	if (duplicate == master) {
    		defineMasterOfAll(master);
    		return;
    	}

    	logger.debug("DefineMaster: master="+master+"; duplicate="+duplicate);
    	
    	logger.debug("Remove the master from the edge between the master and duplicate if the master exists.");
    	PotentialMatchRecord potentialMatch = getPotentialMatchFromOriginals(master, duplicate);
    	if (potentialMatch != null) {
    		potentialMatch.setMasterRecord(null);
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
        SourceTableRecord ultimateMaster = findUltimateMaster(considerGivenNodesGraph,
				master, new ArrayList<SourceTableRecord>());
        
        if (ultimateMaster == duplicate) {
        	ultimateMaster = master;
        }
        
        logger.debug("Find the shortest path to all nodes in the graph");
    	DijkstrasAlgorithm<SourceTableRecord, PotentialMatchRecord> da
    		= new DijkstrasAlgorithm<SourceTableRecord, PotentialMatchRecord>();
    	Map<SourceTableRecord, SourceTableRecord> masterMapping = da.calculateShortestPaths(considerGivenNodesGraph, ultimateMaster);
    	
    	if (masterMapping.get(duplicate) == null) {
    		logger.debug("We could not reach the duplicate from the ultimate master in the current graph. A synthetic edge will be created");
    		addSyntheticPotentialMatchRecord(master, duplicate);
    		masterMapping = da.calculateShortestPaths(considerGivenNodesGraph, ultimateMaster);
    	}
    	
    	defineMatchEdges(considerGivenNodesGraph, masterMapping, isAutoMatch);
	}
    
    /**
     * Similar to {@link #defineMaster(SourceTableRecord, SourceTableRecord, boolean)} except the isAutoMatch
     * boolean flag is set to false by default. DO NOT use this version if you are performing an AutoMatch!
     * @throws SQLObjectException
     */
    public void defineMaster(SourceTableRecord master, SourceTableRecord duplicate) throws SQLObjectException {
    	defineMaster(master, duplicate, false);
    }

    /**
	 * This method adds a match rule set to the match in this pool
	 * for synthetic edges if the rule set does not already exist.
	 * IMPORTANT NOTE: In the case that the new munge process for synthetic edges
	 * had to be created, this pool's match object will be saved using the current
	 * Match DAO from the getSession().  This is a bit of a strange side effect of this
	 * method, so be careful!
	 * <p>
	 * Once the munge process for synthetic edges has been located or created, a
	 * new potential match record that is synthetic (created by the Match Maker)
	 * is added to the pool. This edge (which is a potential match record) belongs
	 * to the special synthetic edges rule set. The match type
	 * of the new potential match record is set to UNMATCH by default.
	 * <p>
	 * This new potential match record will be stored back to the match result table
	 * in the database next time you call the {@link #store()} method on this pool.
	 * 
	 * @param record1
	 *            One of the source table records attached to the new potential
	 *            match record.
	 * @param record
	 *            2 One of the source table records attached to the new
	 *            potential match record.
	 * @return The new potential match record that was added to the pool.
	 */
    
	private PotentialMatchRecord addSyntheticPotentialMatchRecord(SourceTableRecord record1,
			SourceTableRecord record2) {
		MungeProcess syntheticMungeProcess = getProject().getMungeProcessByName(MungeProcess.SYNTHETIC_MATCHES);
		if (syntheticMungeProcess == null) {
			syntheticMungeProcess = new MungeProcess();
			syntheticMungeProcess.setName(MungeProcess.SYNTHETIC_MATCHES);
			getProject().addChild(syntheticMungeProcess);
		}
		
		PotentialMatchRecord pmr = new PotentialMatchRecord(syntheticMungeProcess, MatchType.UNMATCH, record1, record2, true);
		record1.getCluster().addChild(pmr);
		
		return pmr;
	}
	
    /**
	 * This method defines the given node to be the master of all nodes
	 * reachable by either a defined or undefined path. We use Dijkstra's
	 * algorithm to find the shortest path to the nodes and to make sure
	 * that we have no cycles.
	 */
	public void defineMasterOfAll(SourceTableRecord master) throws SQLObjectException {
		GraphModel<SourceTableRecord, PotentialMatchRecord> nonDirectedGraph =
    		new NonDirectedUserValidatedMatchPoolGraphModel(this, new HashSet<PotentialMatchRecord>());
		BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord> bfs =
            new BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord>();
        Set<SourceTableRecord> reachable = new HashSet<SourceTableRecord>(bfs.performSearch(nonDirectedGraph, master));
        Set<SourceTableRecord> noMatchNodes = findNoMatchNodes(reachable);
        
        for (PotentialMatchRecord pmr : master.getOriginalMatchEdges()) {
        	if (pmr.getMatchStatus() == MatchType.UNMATCH) {
        		logger.debug("Looking at record " + pmr);
        		SourceTableRecord str;
        		if (pmr.getOrigLHS() == master) {
        			str = pmr.getOrigRHS();
        		} else {
        			str = pmr.getOrigLHS();
        		}
        		if (noMatchNodes.contains(str)) continue;
        		logger.debug("Adding " + str + " to reachable nodes");
        		GraphModel<SourceTableRecord, PotentialMatchRecord> newNonDirectedGraph =
            		new NonDirectedUserValidatedMatchPoolGraphModel(this, new HashSet<PotentialMatchRecord>());
                Set<SourceTableRecord> newReachableNodes = new HashSet<SourceTableRecord>(bfs.performSearch(newNonDirectedGraph, str));
                reachable.addAll(newReachableNodes);
        		noMatchNodes.addAll(findNoMatchNodes(newReachableNodes));
        	}
        }
        
        
        GraphModel<SourceTableRecord, PotentialMatchRecord> considerGivenNodesGraph =
        	new GraphConsideringOnlyGivenNodes(this, reachable);
        
        DijkstrasAlgorithm<SourceTableRecord, PotentialMatchRecord> da
        = new DijkstrasAlgorithm<SourceTableRecord, PotentialMatchRecord>();
    	Map<SourceTableRecord, SourceTableRecord> masterMapping = da.calculateShortestPaths(considerGivenNodesGraph, master);
    	
    	defineMatchEdges(considerGivenNodesGraph, masterMapping);
	}
	
	/**
	 * This method finds all of the source table records that are connected to
	 * the given node by a no match edge directly and all source table records
	 * that are connected to a different node by a no match edge where the
	 * different node is connected to the given node by matched edges.
	 */
	Set<SourceTableRecord> findNoMatchNodes(Set<SourceTableRecord> strs) {
		Set<SourceTableRecord> noMatchNodes = new HashSet<SourceTableRecord>();
        for (SourceTableRecord reachableNode : strs) {
        	for (PotentialMatchRecord pmr : reachableNode.getOriginalMatchEdges()) {
        		if (pmr.getMatchStatus() == MatchType.NOMATCH) {
        			SourceTableRecord str;
        			if (pmr.getOrigLHS() == reachableNode) {
        				str =pmr.getOrigRHS();
        			} else {
        				str = pmr.getOrigLHS();
        			}
        			GraphModel<SourceTableRecord, PotentialMatchRecord> nonDirectedGraph =
        	    		new NonDirectedUserValidatedMatchPoolGraphModel(this, new HashSet<PotentialMatchRecord>());
        			BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord> bfs =
        	            new BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord>();
        	        Set<SourceTableRecord> reachable = new HashSet<SourceTableRecord>(bfs.performSearch(nonDirectedGraph, str));
        	        noMatchNodes.addAll(reachable);
        		}
        	}
        }
		
		return noMatchNodes;
	}
	
	/**
	 * This sets the two given SourceTableRecords to have no match between them.
	 * If the source table records are the same all potential match records
	 * connected to the node will be set to no match. If no edge exists between
	 * the nodes than an edge will be created to store the fact that there is no
	 * match between the nodes for later use. If the records are connected
	 * somehow the lhs and rhs will be separated as in the
	 * {@link #defineUnmatched(SourceTableRecord, SourceTableRecord)} method.
	 */
    public void defineNoMatch(SourceTableRecord lhs, SourceTableRecord rhs) throws SQLObjectException {
    	if (lhs == rhs) {
    		defineNoMatchOfAny(lhs);
    		return;
    	}
        PotentialMatchRecord pmr = getPotentialMatchFromOriginals(lhs, rhs);
        if (pmr != null && pmr.getMatchStatus() == MatchType.NOMATCH) {
        	return;
        } 
        
        defineUnmatched(lhs, rhs);
        if (pmr != null) {
        	pmr.setMatchStatus(MatchType.NOMATCH);
        } else {
        	addSyntheticPotentialMatchRecord(lhs, rhs).setMatchStatus(MatchType.NOMATCH);
        }
    }

    /**
	 * This method sets all of the potential match records connecting the given
	 * source table record to any other source table record to be no match.
	 */
	public void defineNoMatchOfAny(SourceTableRecord record1) throws SQLObjectException {
		for (PotentialMatchRecord pmr : record1.getOriginalMatchEdges()) {
			if (pmr.getOrigLHS() == pmr.getOrigRHS()) continue;
			logger.debug("Setting no match between " + pmr.getOrigLHS() + " and " + pmr.getOrigRHS());
			if (pmr.getOrigLHS() == record1) {
				defineNoMatch(pmr.getOrigRHS(), pmr.getOrigLHS());
			} else {
				defineNoMatch(pmr.getOrigLHS(), pmr.getOrigRHS());
			}
		}
	}
	
	/**
	 * Sets the potential match record between two source table records to be an
	 * undefined match. If the source table records are the same then the
	 * {@link #defineUnmatchAll(SourceTableRecord)} algorithm will be run
	 * instead. If no potential match record exists between the two source table
	 * records then no new potential match record will be created. If the nodes
	 * are connected by decided edges then the rhs record will be removed from
	 * the chain of matches. 
	 * <p>
	 * The algorithm below is as follows:
	 * <ol>
	 * <li>Use bfs to check if the nodes are connected by decided edges.</li>
	 * <li>If the nodes are connected make a graph of all of the connected
	 * edges.</li>
	 * <li>Find the ultimate master of all of the nodes. If the rhs record is
	 * the ultimate master then set the ultimate master to be the previous
	 * record as we will be removing the ultimate master from this chain.</li>
	 * <li>Remove the rhs record from the graph.</li>
	 * <li>Use Dijkstra to find the shortest path to all of the nodes in the
	 * graph. If there is a node that was not reached in the graph after running
	 * Dijkstra add a synthetic edge between the node and the ultimate master
	 * and run this step again.</li>
	 * <li>Set all edges walked by Dijkstra to be decided edges and all other
	 * edges to be undecided.</li>
	 * </ol>
	 */
	public void defineUnmatched(SourceTableRecord lhs, SourceTableRecord rhs) throws SQLObjectException {
		logger.debug("Unmatching " + rhs + " from " + lhs);
		if (lhs == rhs) {
    		defineUnmatchAll(lhs);
    	}
		
		PotentialMatchRecord possibleNoMatchEdge = getPotentialMatchFromOriginals(lhs, rhs);
		if (possibleNoMatchEdge != null && possibleNoMatchEdge.getMatchStatus() == MatchType.NOMATCH) {
			possibleNoMatchEdge.setMatchStatus(MatchType.UNMATCH);
		}
		
    	GraphModel<SourceTableRecord, PotentialMatchRecord> nonDirectedGraph =
    		new NonDirectedUserValidatedMatchPoolGraphModel(this, new HashSet<PotentialMatchRecord>());
    	BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord> bfs =
            new BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord>();
        Set<SourceTableRecord> reachable = new HashSet<SourceTableRecord>(bfs.performSearch(nonDirectedGraph, lhs));
        if (!reachable.contains(rhs)) {
        	logger.debug("The node " + lhs + " could not reach " + rhs + " already.");
        	return;
        }

        GraphModel<SourceTableRecord, PotentialMatchRecord> considerGivenNodesGraph =
        	new GraphConsideringOnlyGivenNodes(this, reachable);
        logger.debug("Graph contains " + considerGivenNodesGraph.getNodes().size() + " nodes.");

        logger.debug("Find the ultimate master");
        SourceTableRecord ultimateMaster = findUltimateMaster(considerGivenNodesGraph,
				rhs, new ArrayList<SourceTableRecord>());
        if (rhs == ultimateMaster) {
        	List<SourceTableRecord> nodesToSkip = new ArrayList<SourceTableRecord>();
        	nodesToSkip.add(rhs);
        	ultimateMaster = findUltimateMaster(considerGivenNodesGraph, lhs, nodesToSkip);
        }
        
        reachable.remove(rhs);
        considerGivenNodesGraph = new GraphConsideringOnlyGivenNodes(this, reachable);
        for (PotentialMatchRecord pmr : rhs.getOriginalMatchEdges()) {
        	if (pmr.isMatch()) {
        		pmr.setMasterRecord(null);
        	}
        }
        logger.debug("Graph now contains " + considerGivenNodesGraph.getNodes().size() + " nodes.");
        
        logger.debug("Find the shortest path to all nodes in the graph");
    	DijkstrasAlgorithm<SourceTableRecord, PotentialMatchRecord> da = new DijkstrasAlgorithm<SourceTableRecord, PotentialMatchRecord>();
    	Map<SourceTableRecord, SourceTableRecord> masterMapping = da.calculateShortestPaths(considerGivenNodesGraph, ultimateMaster);
    	
    	for (SourceTableRecord str : reachable) {
    		if (masterMapping.get(str) == null && str != ultimateMaster) {
    			logger.debug("We could not reach " + str + " from the ultimate master in the current graph. A synthetic edge will be created");
    			addSyntheticPotentialMatchRecord(ultimateMaster, str);
    			masterMapping = da.calculateShortestPaths(considerGivenNodesGraph, ultimateMaster);
    		}
    	}
    	
    	defineMatchEdges(considerGivenNodesGraph, masterMapping);
	}

	/**
	 * This method sets the edges defined by the <duplicate, master> pairs of
	 * the map to be matched edges with the master the master node in the pair.
	 * All other edges in the graph are set to be undefined.
	 * <p>
	 * Additionally, there is an isAutoMatch boolean flag that should be used when
	 * the method is being called by the AutoMatch feature. In this case, all of the
	 * PotentialMatchRecords will have their match status set to AUTOMATCH, and the
	 * respective records in the match result table will also be updated as AUTOMATCH 
	 * 
	 * @param graph
	 *            The graph that contains the edges to modify.
	 * @param masterMapping
	 *            The mapping of all matched edges in the graph given in
	 *            <duplicate, master> pairs.
	 @param isAutoMatch Indicate that this method is being used by the AutoMatch feature
	 */
	private void defineMatchEdges(
			GraphModel<SourceTableRecord, PotentialMatchRecord> graph,
			Map<SourceTableRecord, SourceTableRecord> masterMapping,
			boolean isAutoMatch) throws SQLObjectException {
		logger.debug("Removing all decided edges from the given graph");
    	for (PotentialMatchRecord pmr : graph.getEdges()) {
   			pmr.setMasterRecord(null);
    	}
    	
    	logger.debug("Setting the new decided edges for this graph of nodes");
    	//XXX This is a fairly poor way of obtaining the potential match records. We should be able to make this faster.
    	for (Map.Entry<SourceTableRecord, SourceTableRecord> nodeMasterPair : masterMapping.entrySet()) {
    		logger.debug("Setting " + nodeMasterPair.getValue() + " to be the master of " + nodeMasterPair.getKey());
    		PotentialMatchRecord matchRecord = getPotentialMatchFromOriginals(nodeMasterPair.getValue(), nodeMasterPair.getKey());
   			matchRecord.setMasterRecord(nodeMasterPair.getValue(), isAutoMatch);
   			decidedRecordsCache.add(matchRecord);
    	}
	}
	
	/**
	 * Similar to {@link #defineMatchEdges(GraphModel, Map, boolean)} except the isAutoMatch
	 * boolean flag is set to false by default. DO NOT use this version if you are performing an AutoMatch!
	 * @throws SQLObjectException
	 */
	private void defineMatchEdges(
			GraphModel<SourceTableRecord, PotentialMatchRecord> graph,
			Map<SourceTableRecord, SourceTableRecord> masterMapping) throws SQLObjectException {
		defineMatchEdges(graph, masterMapping, false);
	}

	/**
	 * This method finds the ultimate master, the master with no masters, of a
	 * given node on a given graph ignoring the nodes already contained in the
	 * given list.
	 * 
	 * @param graph
	 *            The graph to traverse to find the ultimate master.
	 * @param startingPoint
	 *            The starting point to travel from to find the ultimate master.
	 * @param nodesToSkip
	 *            A starting list of nodes that will not be crossed when looking
	 *            for the ultimate master.
	 * @return The source table record that represents the ultimate master
	 */
	private SourceTableRecord findUltimateMaster(
			GraphModel<SourceTableRecord, PotentialMatchRecord> graph,
			SourceTableRecord startingPoint,
			List<SourceTableRecord> nodesToSkip) {
		SourceTableRecord ultimateMaster = startingPoint;
		List<SourceTableRecord> nodesCrossed = new ArrayList<SourceTableRecord>(nodesToSkip);
		while (!graph.getOutboundEdges(ultimateMaster).isEmpty()) {
        	logger.debug("The outbound edges for " + ultimateMaster + " is " + graph.getOutboundEdges(ultimateMaster));
        	nodesCrossed.add(ultimateMaster);
        	
        	SourceTableRecord newUltimateMaster = ultimateMaster;
        	for (PotentialMatchRecord pmr : graph.getOutboundEdges(ultimateMaster)) {
        		if (pmr.getOrigLHS() != ultimateMaster) {
        			if (!nodesCrossed.contains(pmr.getOrigLHS())) {
        				newUltimateMaster = pmr.getOrigLHS();
        				break;
        			}
        		} else {
        			if (!nodesCrossed.contains(pmr.getOrigRHS())) {
        				newUltimateMaster = pmr.getOrigRHS();
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
		return ultimateMaster;
	}

	/**
	 * Sets all potential match records connected to the given source table record
	 * to be undefined matches.
	 */
	public void defineUnmatchAll(SourceTableRecord record1) throws SQLObjectException {
		logger.debug("unmatching " + record1 + " from everything");
        for (PotentialMatchRecord pmr : record1.getOriginalMatchEdges()) {
        	if (pmr.getOrigLHS() == pmr.getOrigRHS()) continue;
        	if (pmr.getOrigLHS() == record1) {
        		defineUnmatched(pmr.getOrigRHS(), pmr.getOrigLHS());
        	} else {
        		defineUnmatched(pmr.getOrigLHS(), pmr.getOrigRHS());
        	}
        }
	}
	
	/**
	 * This resets all of the edges in the entire pool to be unmatched. Edges
	 * that are synthetic as they were created by the MatchMaker will be
	 * removed.
	 */
	public void resetPool() {
		for (MatchCluster mc : matchClusters) {
			try {
				mc.reset();
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (ObjectDependentException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Performs the 'Auto-Match' function which is either for lazy people who
	 * think their rule set is perfect or for people who like to gamble.
	 * Either way, we warn them heavily in the UI that this will make mistakes.
	 * Our goal here is to make as many matches along edges in the supplied
	 * rule set without putting the graph into an illegal state or taking
	 * forever. We do not claim, nor should we, that the total number of matches
	 * created by this method is maximal or predictable.
	 * <p>
	 * NOTE: This depends on {@link #findNoMatchNodes(Set)} to return the set
	 * of nodes that should never be matched to any node in the provided set
	 * because of the current no-match and match edge configuration
	 * <p>
	 * This is the current algorithm:
	 * <ol>
	 * <li> Define a set of nodes called 'visited'. Add to it all nodes that are
	 * not incident with an edge that:
	 * <ul>
	 * <li> Is not a NoMatch edge and </li>
	 * <li> Belongs to the provided rule set </li>
	 * </ul>
	 * </li>
	 * <li> Pick a node in the graph that is not in the 'visited' set, call it
	 * 'selected' and add it to the 'visited' set. </li>
	 * <li> Find all edges incident with the selected node that are in the
	 * rule set and are not NoMatch edges. Add the nodes that are incident
	 * with the edges we just found and are not in the visited set to a list.
	 * </li>
	 * <li> Set the selected node to be the master of each node in the newly
	 * created list via the
	 * {@link #defineMaster(SourceTableRecord, SourceTableRecord)} to ensure
	 * that the graph's state is kept legal. </li>
	 * <li> For each node in the list, call one of them 'selected', add it to
	 * the visited set and go to 3 </li>
	 * </ol>
	 * 
	 * @param rule
	 *            The name of the rule along which to make matches
	 * @throws SQLObjectException
	 * @throws SQLException
	 */
	public void doAutoMatch(MungeProcess mungeProcess) throws SQLException, SQLObjectException {
		if (mungeProcess == null) {
			throw new IllegalArgumentException("Auto-Match invoked with an " +
					"invalid munge process");
		}
		List<SourceTableRecord> records = new ArrayList<SourceTableRecord>();
		for(MatchCluster mc : matchClusters) {
			records.addAll(mc.getSourceTableRecords());
		}
		
		logger.debug("Auto-Matching with " + records.size() + " records.");
		
		Set<SourceTableRecord> visited = new HashSet<SourceTableRecord>();
		SourceTableRecord selected = null;
		for (SourceTableRecord record : records) {
			boolean addToVisited = true;
			for (PotentialMatchRecord pmr : record.getOriginalMatchEdges()) {
				if (pmr.getMatchStatus() != MatchType.NOMATCH
						&& pmr.getMungeProcess() == mungeProcess) {
					addToVisited = false;
				}
			}
			if (addToVisited) {
				visited.add(record);
			} else {
				// rather than iterating through all the records again, looking
				// for one that isn't in visited...
				selected = record;
			}
		}
		
		logger.debug("The size of visited is " + visited.size());

		Set<SourceTableRecord> neighbours = findAutoMatchNeighbours(mungeProcess, selected, visited);
		makeAutoMatches(mungeProcess, selected, neighbours, visited);
		//If we haven't visited all the nodes, we are not done!
		while (visited.size() != records.size()) {
			if(visited.size() == 103) {
					System.out.println("sop");
			}
			SourceTableRecord temp = null;
			for (SourceTableRecord record : records) {
				if (!visited.contains(record)) {
					temp = record;
					break;
				}
			}
			neighbours = findAutoMatchNeighbours(mungeProcess, temp, visited);
			makeAutoMatches(mungeProcess, temp, neighbours, visited);
		}
	}

	/**
	 * Creates the matches necessary in an auto-match while maintaining the
	 * 'visited' set and propagating the algorithm to neighbours of selected
	 * nodes.
	 */
	private void makeAutoMatches(MungeProcess mungeProcess,
			SourceTableRecord selected,
			Set<SourceTableRecord> neighbours,
			Set<SourceTableRecord> visited) throws SQLException, SQLObjectException {
		logger.debug("makeAutoMatches called, selected's key values = " + selected.getKeyValues());
		visited.add(selected);
		GraphModel<SourceTableRecord, PotentialMatchRecord> nonDirectedGraph =
			new NonDirectedUserValidatedMatchPoolGraphModel(this, new HashSet<PotentialMatchRecord>());
		BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord> bfs =
			new BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord>();
		Set<SourceTableRecord> reachable = new HashSet<SourceTableRecord>(bfs.performSearch(nonDirectedGraph, selected));
		Set<SourceTableRecord> noMatchNodes = findNoMatchNodes(reachable);
		for (SourceTableRecord record : neighbours) {
			if (!noMatchNodes.contains(record)) {
				defineMaster(selected, record, true);
				nonDirectedGraph = new NonDirectedUserValidatedMatchPoolGraphModel(this, new HashSet<PotentialMatchRecord>());
				bfs = new BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord>();
				reachable = new HashSet<SourceTableRecord>(bfs.performSearch(nonDirectedGraph, selected));
				noMatchNodes = findNoMatchNodes(reachable);
			}
		}
		for (SourceTableRecord record : neighbours) {
			if (!visited.contains(record)) {
				makeAutoMatches(mungeProcess, record, findAutoMatchNeighbours(mungeProcess, record, visited), visited);
			}
		}
	}

	/**
	 * Finds all the neighbours that auto-match worries about as explained in
	 * the comment for doAutoMatch in the context that 'record' is selected in
	 * step 3
	 */
	private Set<SourceTableRecord> findAutoMatchNeighbours(MungeProcess mungeProcess,
			SourceTableRecord record,
			Set<SourceTableRecord> visited) {
		logger.debug("The size of visited is " + visited.size());
		Set<SourceTableRecord> ret = new HashSet<SourceTableRecord>();
		for (PotentialMatchRecord pmr : record.getOriginalMatchEdges()) {
			if (pmr.getMungeProcess() == mungeProcess 
					&& pmr.getMatchStatus() != MatchType.NOMATCH) {
				if (record == pmr.getOrigLHS() && !visited.contains(pmr.getOrigRHS())) {
					ret.add(pmr.getOrigRHS());
				} else if (record == pmr.getOrigRHS() && !visited.contains(pmr.getOrigLHS())) {
					ret.add(pmr.getOrigLHS());
				}
			}
		}
		logger.debug("findAutoMatchNeighbours: The neighbours to automatch for " + record + " are " + ret);
		return ret;
	}
	
	/**
	 * Completely removes all SourceTableRecords and PotentialMatchRecords, and also
	 * removes all PotentialMatchRecords in the database repository for this MatchPool
	 * 
	 * @param aborter
	 *            {@link Aborter} to alert the MatchPool to stop the clear operation if
	 *            the user cancels it
	 */
	public void clear(Aborter aborter) throws SQLException {
		SQLTable resultTable = getProject().getResultTable();
        Connection con = null;
        String lastSQL = null;
        Statement stmt = null;
        try {
            con = getProject().createResultTableConnection();
            con.setAutoCommit(false);
            StringBuilder sql = new StringBuilder();
            sql.append("DELETE FROM ").append(DDLUtils.toQualifiedName(resultTable));
            sql.append("\n WHERE 0 = 0");
            lastSQL = sql.toString();
            logger.debug("The SQL statement we are running is " + lastSQL);
            
            stmt = con.createStatement();
            
            if (aborter != null) {
        	    aborter.checkCancelled();
            }
            
            stmt.executeUpdate(lastSQL);
            con.commit();
        } catch (SQLException ex) {
            logger.error("Error in query: "+lastSQL, ex);
            getSession().handleWarning(
                    "Error in SQL Query while clearing the Match Pool!" +
                    "\nMessage: "+ex.getMessage() +
                    "\nSQL State: "+ex.getSQLState() +
                    "\nQuery: "+lastSQL);
            try {
                con.rollback();
            } catch (SQLException doubleException) {
                logger.error("Rollback failed. Squishing this exception since it would shadow the original one:", doubleException);
            }
            throw ex;
        } catch (Exception ex) {
            try {
                con.rollback();
            } catch (SQLException doubleException) {
                logger.error("Rollback failed. Squishing this exception since it would shadow the original one:", doubleException);
            }
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            } else {
                throw new RuntimeException(ex);
            }
        } finally {
        	setFinished(true);
            if (stmt != null) try { stmt.close(); } catch (SQLException ex) { logger.error("Couldn't close prepared statement", ex); }
            if (con != null) try { con.close(); } catch (SQLException ex) { logger.error("Couldn't close connection", ex); }
        }
		clearRecords();
	}
	
	/**
	 * Calls the regular clear with a null Aborter
	 */
	public void clear() throws SQLException {
		clear(null);
	}
	
	/**
	 * Completely removes all clusters from the match pool. This will not fire any events because we
	 * want it to be silent so the server doesn't suddenly remove them all after adding them.
	 */
	public void clearRecords() {
		matchClusters.clear();
		decidedRecordsCache.clear();
	}
	
	/**
	 * Completely removes all clusters from the cache in the listener. They will have to be read anew.
	 */
	public void clearCache() {
		matchPoolReader.clear();
	}
	
	@Override
	@NonProperty
	public synchronized Integer getJobSize() {
		return new Integer(getPotentialMatchRecords().size() * 2);
	}
	
	@NonProperty
	public List<PotentialMatchRecord> getPotentialMatchRecords() {
		List<PotentialMatchRecord> pmrl = new ArrayList<PotentialMatchRecord>();
		for (MatchCluster mc : matchClusters) {
			pmrl.addAll(mc.getPotentialMatchRecords());
		}
		return pmrl;
	}

	/**
	 * A cache of potential match records that have been decided as matching.
	 * This is an optimization to graph algorithms such as auto-match which
	 * need to treat the match pool as a graph where the edges are the decided
	 * PMR's.
	 */
	private final List<PotentialMatchRecord> decidedRecordsCache = new ArrayList<PotentialMatchRecord>();
	
	private final SPListener matchListener = new SPListener() {
		public void childAdded(SPChildEvent e) {
			if(e.getSource() instanceof MatchPool) {
				MatchCluster mc = (MatchCluster)e.getChild();
				mc.addSPListener(this);
				for(PotentialMatchRecord pmr : mc.getPotentialMatchRecords()) {
					if(pmr.getMatchStatus().equals(MatchType.MATCH)) {
						decidedRecordsCache.add(pmr);
					}
				}
			}
			if(e.getSource() instanceof MatchCluster && e.getChild() instanceof PotentialMatchRecord) {
				PotentialMatchRecord pmr = (PotentialMatchRecord)e.getChild();
				if(pmr.getMatchStatus().equals(MatchType.MATCH)) {
					decidedRecordsCache.add(pmr);
				}
			}
		}

		@Override
		public void childRemoved(SPChildEvent e) {
			if(e.getChild() instanceof MatchCluster) {
				MatchCluster mc = (MatchCluster)e.getChild();
				mc.removeSPListener(this);
			}
		}

		@Override
		public void transactionStarted(TransactionEvent e) {
		}

		@Override
		public void transactionEnded(TransactionEvent e) {
		}

		@Override
		public void transactionRollback(TransactionEvent e) {
		}

		@Override
		public void propertyChanged(PropertyChangeEvent evt) {
		}
		
	};
	
    public void recordChangedState(PotentialMatchRecord potentialMatchRecord) {
        if (potentialMatchRecord.isMasterUndecided()) {
            decidedRecordsCache.remove(potentialMatchRecord);
        } else {
            decidedRecordsCache.add(potentialMatchRecord);
        }
    }

	@NonProperty
    public List<PotentialMatchRecord> getDecidedRecordsCache() {
        return decidedRecordsCache;
    }

	@Override
	public MatchMakerObject duplicate(MatchMakerObject parent) {
		return null;
	}

	@Override
	@NonProperty
	public List<SPObject> getChildren() {
		List<SPObject> children = new ArrayList<SPObject>();
		children.addAll(matchClusters);
		return children;
	}

	@Override
	@NonProperty
	public List<Class<? extends SPObject>> getAllowedChildTypes() {
		return allowedChildTypes;
	}
	
	@Override
	protected void addChildImpl(SPObject child, int index) {
		if(child instanceof MatchCluster) {
			addMatchCluster((MatchCluster)child, index);
		}
	}

	public void addMatchCluster(MatchCluster mc, int index) {
		matchClusters.add(index, mc);
		fireChildAdded(MatchCluster.class, mc, index);
	}
	
	public void addMatchCluster(MatchCluster mc) {
		addChild(mc, matchClusters.size());
	}
	
	@Override
	protected boolean removeChildImpl(SPObject child) {
		if(child instanceof MatchCluster) {
			return removeMatchCluster((MatchCluster)child);
		}
		return false;
	}

	private boolean removeMatchCluster(MatchCluster child) {
		int index = matchClusters.indexOf(child);
		boolean removed = matchClusters.remove(child);
		if(removed) {
			fireChildRemoved(MatchCluster.class, child, index);
		}
		return removed;
	}

	@NonBound
	public void setUseBatchUpdates(boolean useBatchUpdates) {
			this.useBatchUpdates = useBatchUpdates;
		
	}

	@NonBound
	public boolean isUseBatchUpdates() {
		return useBatchUpdates;
	}

	@NonBound
	public void setDebug(boolean debug) {
			this.debug = debug;
	}

	@NonBound
	public boolean isDebug() {
		return debug;
	}

	@NonBound
	public void setLimit(int limit) {
		this.limit = limit;
	}

	@NonBound
	public int getLimit() {
		return limit;
	}

	@NonBound
    public int getCurrentMatchNumber() {
		return currentMatchNumber;
	}

	@NonBound
	public void setCurrentMatchNumber(int currentMatchNumber) {
		this.currentMatchNumber = currentMatchNumber;
	}

	@Mutator
	public void setClusterCount(int clusterCount) {
		int old = this.clusterCount;
		this.clusterCount = clusterCount;
		firePropertyChange("clusterCount", old, clusterCount);
	}

	@Accessor
	public int getClusterCount() {
		return clusterCount;
	}
	
	@NonProperty
	public List<SourceTableRecord> getAllSourceTableRecords() {
		List<SourceTableRecord> srcl = new ArrayList<SourceTableRecord>();
		for(MatchCluster mc : matchClusters) {
			for(SourceTableRecord src : mc.getSourceTableRecords()) {
				if(src != null) {
					srcl.add(src);
				}
			}
		}
		return srcl;
	}

	public MatchCluster alreadyContains(SourceTableRecord src1, SourceTableRecord src2) {
		for(MatchCluster mc : matchClusters) {
			if(mc.getSourceTableRecords().contains(src1) ||
					mc.getSourceTableRecords().contains(src2)) {
				return mc;
			}
		}
		return null;
	}

	/**
	 * Takes in a list of foreign clusters and checks, for each match cluster, if it has
	 * any duplicate source table records to any clusters currently in the match pool.
	 * If so, then it will merge them into a single larger cluster and remove the old one.
	 * Otherwise it will add the match cluster to the match pool.
	 * @param foundMatchClusters List of foreign match clusters
	 */
	public void mergeInClusters(List<MatchCluster> foundMatchClusters) {
		boolean merged;
		for(MatchCluster mc : foundMatchClusters) {
			merged = false;
			for(SourceTableRecord src : mc.getSourceTableRecords()) {
				for(MatchCluster mc2 : matchClusters) {
					if(!merged && mc2.getSourceTableRecords().contains(src)){
						mergeCluster(mc2, mc);
						merged = true;
					}
				}
			}
			if(!merged) {
				addMatchCluster(mc);
			}
		}
	}
	
	/**
	 * Takes in two match records, and will merge one into the other. It does this by
	 * adding in any extra source table records to the first cluster. Then it will transfer
	 * the potential match records, reassigning origLHS and origRHS as necessary.
	 * @param to MergeCluster to merge into
	 * @param from MergeCluster to take records from
	 */
	public void mergeCluster(MatchCluster to, MatchCluster from) {
		List<SourceTableRecord> srcl = new ArrayList<SourceTableRecord>();
		List<PotentialMatchRecord> pmrl = new ArrayList<PotentialMatchRecord>();
		srcl.addAll(from.getSourceTableRecords());
		pmrl.addAll(from.getPotentialMatchRecords());
		
		for(PotentialMatchRecord pmr : pmrl) {
			for(SourceTableRecord src : to.getSourceTableRecords()) {
				if(pmr.getOrigLHS().equals(src)) {
					pmr.setOrigLHS(src);
				} else if(pmr.getOrigRHS().equals(src)) {
					pmr.setOrigRHS(src);
				}
			}
		}
		
		for(SourceTableRecord src : srcl) {
			if(!to.getSourceTableRecords().contains(src)) {
				to.addSourceTableRecord(src);
			}
		}
		
		for(SourceTableRecord src : to.getSourceTableRecords()) {
			src.setParent(to);
		}
		
		for(PotentialMatchRecord pmr : pmrl) {
			to.addPotentialMatchRecord(pmr);
		}
	}

	@NonProperty
	public SourceTableRecord getSourceTableRecord(List<? extends Object> keyList) {
		SourceTableRecord src;
		for(MatchCluster mc : getMatchClusters()) {
			src = mc.getSourceTableRecord(keyList);
			if(src != null) return src;
		}
		return null;
	}
	
	/**
	 * This function should only be used for testing purposes.
	 */
	public void addPotentialMatchRecord(PotentialMatchRecord pmr) {
		MatchCluster mc = new MatchCluster();
		mc.addSourceTableRecord(pmr.getOrigLHS());
		mc.addSourceTableRecord(pmr.getOrigRHS());
		mc.addPotentialMatchRecord(pmr);
		mergeInClusters(Collections.singletonList(mc));
	}
	
	/**
	 * This function should only be used for testing purposes.
	 */
	public boolean removePotentialMatchRecord(PotentialMatchRecord pmr) {
		MatchCluster toCheck = null;
		for(MatchCluster mc : matchClusters) {
			if(mc.getPotentialMatchRecords().contains(pmr)) {
				toCheck = mc;
			}
		}
		if(toCheck != null) {
			try {
				if(toCheck.getPotentialMatchRecords().size() == 1) {
					removeChild(toCheck);
				}
				return toCheck.removeChild(pmr);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (ObjectDependentException e) { 
				throw new RuntimeException(e);
			}
		} else {
			return false;
		}
	}
	
	/**
	 * Stores the current MatchPool cache back into the database.
	 */
	public void store() {
		matchPoolListener.store();
	}
}