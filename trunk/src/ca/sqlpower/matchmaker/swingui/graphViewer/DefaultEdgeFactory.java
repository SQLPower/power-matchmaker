package ca.sqlpower.matchmaker.swingui.graphViewer;

public class DefaultEdgeFactory implements EdgeFactory {

	public Diedge createEdge(Node head, Node tail) {
		return new DefaultEdge(head,tail);
	}

}
