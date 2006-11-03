package ca.sqlpower.matchmaker.event;

import ca.sqlpower.matchmaker.MatchMakerObject;
/**
 * An event that is designed to work with match maker objects.
 * 
 *
 */
public class MatchMakerEvent {
	
	private int[] changeIndices;
	private MatchMakerObject[] children;
	private String propertyName;
	private Object oldValue;
	private Object newValue;
	private MatchMakerObject source;
	
	public MatchMakerObject getSource() {
		return source;
	}
	
	public void setSource(MatchMakerObject source) {
		if (this.source != source) {
			this.source = source;
		}
	}
	public int[] getChangeIndices() {
		return changeIndices;
	}
	public void setChangeIndices(int[] changeIndeces) {
		if (this.changeIndices != changeIndeces) {
			this.changeIndices = changeIndeces;
		}
	}
	public MatchMakerObject[] getChildren() {
		return children;
	}
	public void setChildren(MatchMakerObject[] children) {
		if (this.children != children) {
			this.children = children;
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
	

}
