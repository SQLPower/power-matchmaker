package ca.sqlpower.matchmaker.swingui.graphViewer;

import junit.framework.TestCase;

public class DefaultDigraphTest extends TestCase {

	Digraph defaultDigraph;
	protected void setUp() throws Exception {
		defaultDigraph = new DefaultDigraph();
	}
	
	public void testRemoveNodeCascades() {
	
		Node n1 = new DefaultNode("N1",new DefaultEdgeFactory());
		Node n2 = new DefaultNode("N2",new DefaultEdgeFactory());
		defaultDigraph.addNode(n1);
		defaultDigraph.addNode(n2);
		n1.addAdjacentNode(n2);
		
		defaultDigraph.removeNode(n2);
		assertFalse("Removing a node from a graph should cut all the adjacent edges",n1.isAdjacent(n2));
	}
	
}
