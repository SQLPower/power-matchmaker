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

package ca.sqlpower.matchmaker.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.AbstractMatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;

public class MMOChildrenInsertUndoableEdit extends AbstractUndoableEdit{
	
	private static final Logger logger = Logger.getLogger(MMOChildrenInsertUndoableEdit.class);
	
	private MatchMakerEvent undoEvent;
	private MatchMakerObject mmo;

	public MMOChildrenInsertUndoableEdit(MatchMakerEvent e, MatchMakerObject mmo){
		super();
		undoEvent = e;
		this.mmo = mmo;
	}

	public void undo(){
		super.undo();
		try {
			undoEvent.getSource().setUndoing(true);
			if (!(undoEvent.getSource() instanceof AbstractMatchMakerObject)) {
				throw new CannotUndoException();
			}
			AbstractMatchMakerObject ammo = (AbstractMatchMakerObject) undoEvent.getSource();
			for (Object insertedChild : undoEvent.getChildren()) {
				if (!(insertedChild instanceof AbstractMatchMakerObject)) {
					throw new CannotUndoException();
				}
				AbstractMatchMakerObject child = (AbstractMatchMakerObject) insertedChild;
				ammo.removeChild(child);
			}
		} finally {
			undoEvent.getSource().setUndoing(false);
		}
	}

	public void redo(){
		super.redo();
		try {
			undoEvent.getSource().setUndoing(true);
			if (!(undoEvent.getSource() instanceof AbstractMatchMakerObject)) {
				throw new CannotUndoException();
			}
			AbstractMatchMakerObject ammo = (AbstractMatchMakerObject) undoEvent.getSource();
			for (int i = 0; i < undoEvent.getChangeIndices().length; i++) {
				if (!(undoEvent.getChildren().get(i) instanceof AbstractMatchMakerObject)) {
					throw new CannotUndoException();
				} 
				AbstractMatchMakerObject child = (AbstractMatchMakerObject) undoEvent.getChildren().get(i);
				ammo.addChild(undoEvent.getChangeIndices()[i], child);
			}
			
		} finally {
			undoEvent.getSource().setUndoing(false);
		}
	}
}
