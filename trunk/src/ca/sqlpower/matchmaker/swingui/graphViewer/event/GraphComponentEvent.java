package ca.sqlpower.matchmaker.swingui.graphViewer.event;

import ca.sqlpower.matchmaker.swingui.graphViewer.Diedge;
import ca.sqlpower.matchmaker.swingui.graphViewer.GraphComponent;
/**
 * An event that is designed to work with GraphComponent objects.
 */
public class GraphComponentEvent {
	
	private String propertyName;
	private Object oldValue;
	private Object newValue;
	private Diedge edge;
	private GraphComponent source;
	
	public GraphComponent getSource() {
		return source;
	}
	
	public void setSource(GraphComponent source) {
		if (this.source != source) {
			this.source = source;
		}
	}
	
	public Object getNewValue() {
		return newValue;
	}
	public void setNewValue(Object newValue) {
		if (this.newValue != newValue) {
			this.newValue = newValue;
		}
	}
	public Object getOldValue() {
		return oldValue;
	}
	public void setOldValue(Object oldValue) {
		if (this.oldValue != oldValue) {
			this.oldValue = oldValue;
		}
	}
	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String propertyName) {
		if (this.propertyName != propertyName) {
			this.propertyName = propertyName;
		}
	}

	public Diedge getEdge() {
		return edge;
	}

	public void setEdge(Diedge edge) {
		this.edge = edge;
	}
}
