/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */


package ca.sqlpower.matchmaker.event;

import java.util.List;

import ca.sqlpower.matchmaker.MatchMakerObject;
/**
 * An event that is designed to work with match maker objects.
 * @param S source of event
 * @param C child type of S
 */
public class MatchMakerEvent<S extends MatchMakerObject,C extends MatchMakerObject> {
	
	private int[] changeIndices;
	private List<C> children;
	private String propertyName;
	private Object oldValue;
	private Object newValue;
	private S source;
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
	
	public S getSource() {
		return source;
	}
	
	public void setSource(S source) {
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
	public List<C> getChildren() {
		return children;
	}
	public void setChildren(List<C> children) {
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
