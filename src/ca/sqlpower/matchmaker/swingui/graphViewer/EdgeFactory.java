package ca.sqlpower.matchmaker.swingui.graphViewer;

public interface EdgeFactory {
	Diedge createEdge(Node head, Node tail);
}
