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


package ca.sqlpower.matchmaker.event;

import java.util.List;

import ca.sqlpower.object.SPObject;
/**
 * An event that is designed to work with match maker objects.
 * @param S source of event
 * @param C child type of S
 */
public class MatchMakerEvent {
	
	private int[] changeIndices;
	private List<SPObject> children;
	private String propertyName;
	private Object oldValue;
	private Object newValue;
	private SPObject source;
	private boolean isUndoEvent;
	private boolean isCompoundEvent;
	
	public boolean isUndoEvent() {
		return isUndoEvent;
	}

	public void setUndoEvent(boolean isUndoEvent) {
		this.isUndoEvent = isUndoEvent;
	}

	public boolean isCompoundEvent() {
		return isCompoundEvent;
	}

	public void setCompoundEvent(boolean isCompoundEvent) {
		this.isCompoundEvent = isCompoundEvent;
	}
	
	public SPObject getSource() {
		return source;
	}
	
	public void setSource(SPObject source) {
	    this.source = source;
	}
	
	public int[] getChangeIndices() {
		return changeIndices;
	}
	
	public void setChangeIndices(int[] changeIndeces) {
	    this.changeIndices = changeIndeces;
	}
	
	public List<SPObject> getChildren() {
		return children;
	}
	
	public void setChildren(List<SPObject> children) {
	    this.children = children;
	}
	
	public Object getNewValue() {
		return newValue;
	}
	
	public void setNewValue(Object newValue) {
	    this.newValue = newValue;
	}
	
	public Object getOldValue() {
		return oldValue;
	}
	
	public void setOldValue(Object oldValue) {
	    this.oldValue = oldValue;
	}
	
	public String getPropertyName() {
		return propertyName;
	}
	
	public void setPropertyName(String propertyName) {
	    this.propertyName = propertyName;
	}
}
