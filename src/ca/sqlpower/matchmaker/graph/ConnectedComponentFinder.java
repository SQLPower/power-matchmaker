package ca.sqlpower.matchmaker.graph;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Implements an algorithm that partitions a graph into its set of
 * connected components
 */
public class ConnectedComponentFinder<V, E> {

    private static final Logger logger = Logger.getLogger(ConnectedComponentFinder.class);
    
    public Set<Set<V>> findConnectedComponents(GraphModel<V, E> model) {
        
        // all nodes in the graph that we have not yet assigned to a component
        final Set<V> undiscovered = new HashSet<V>();
        undiscovered.addAll(model.getNodes());

        // the current component of the graph we're discovering using the BFS
        final Set<V> thisComponent = new HashSet<V>();
        
        // the components we've finished discovering
        Set<Set<V>> components = new HashSet<Set<V>>();
        
        BreadthFirstSearch<V, E> bfs = new BreadthFirstSearch<V, E>();
        bfs.addBreadthFirstSearchListener(new BreadthFirstSearchListener<V>() {
            public void nodeDiscovered(V node) {
                undiscovered.remove(node);
                thisComponent.add(node);
            }
        });
        
        while (!undiscovered.isEmpty()) {
            
            logger.debug("Starting new BFS");
            
            V node = undiscovered.iterator().next();
            bfs.performSearch(model, node);
            
            if (logger.isDebugEnabled()) {
                logger.debug("  Search found "+thisComponent.size()+" nodes");
            }
            
            components.add(new HashSet<V>(thisComponent));
            thisComponent.clear();
        }
        
        return components;
    }
}
