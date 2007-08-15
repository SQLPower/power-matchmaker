package ca.sqlpower.matchmaker.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class DijkstrasAlgorithm<V, E> {

	Logger logger = Logger.getLogger(DijkstrasAlgorithm.class);
	
	private Map<V, Integer> d = new HashMap<V, Integer>();
	
	private Map<V, V> pi = new HashMap<V, V>();
	
	/**
     * Performs Dijkstra's algorithm on the given graph, starting
     * with the given node.  This algorithm is described in "Introduction
     * to Algorithms" by Cormen et al, Chapter 25.
     * 
     * @param graph The graph to run Dijkstra's algorithm on
     * @param startingNode The node to start running Dijkstra's algorithm from
     * @return 
     */
	public List<V> performSearch(GraphModel<V, E> graph, V start) {
		initializeSingleSource(graph, start);
		List<V> S = new ArrayList<V>();
		List<V> Q = new ArrayList<V>(graph.getNodes());
		while (!Q.isEmpty()) {
			V u = extractMin(Q);
			S.add(u);
			for (V v: graph.getAdjacentNodes(u)) {
				relax(u, v);
			}
		}
		return new ArrayList<V>();
	}

	private void relax(V u, V v) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DijkstrasAlgorithm.relax()");
		
	}

	private V extractMin(List<V> q) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DijkstrasAlgorithm.extractMin()");
		return null;
	}

	private void initializeSingleSource(GraphModel<V, E> graph, V start) {
		for(V v: graph.getNodes()) {
			
		}
	}
}
