package ca.sqlpower.matchmaker.swingui.graphViewer.event;


public interface GraphComponentListener{
	void gcPropertyChanged(GraphComponentEvent evt);
	void gcEdgeDirectionSwap(GraphComponentEvent evt);
	void gcEdgeCut(GraphComponentEvent evt);
	void gcNewEdgeAddedToNode(GraphComponentEvent evt);
}
