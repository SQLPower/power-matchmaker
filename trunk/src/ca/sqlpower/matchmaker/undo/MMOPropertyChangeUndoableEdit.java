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

package ca.sqlpower.matchmaker.undo;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.munge.InputDescriptor;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;

public class MMOPropertyChangeUndoableEdit extends AbstractUndoableEdit{
	
	private static final Logger logger = Logger.getLogger(MMOPropertyChangeUndoableEdit.class);
	
	private MatchMakerEvent undoEvent;

	public MMOPropertyChangeUndoableEdit(MatchMakerEvent e){
		super();
		undoEvent = e;
	}

	public void undo(){
		super.undo();
		try {
			undoEvent.getSource().setUndoing(true);
		    modifyProperty(undoEvent.getOldValue());
		} catch (IllegalAccessException e) {
			logger.error("Couldn't access setter for "+
					undoEvent.getPropertyName(), e);
			throw new CannotUndoException();
		} catch (InvocationTargetException e) {
			logger.error("Setter for "+undoEvent.getPropertyName()+
					" on "+undoEvent.getSource()+" threw exception", e);
			throw new CannotUndoException();
		} catch (IntrospectionException e) {
			logger.error("Couldn't introspect source object "+
					undoEvent.getSource(), e);
			throw new CannotUndoException();
		} finally {
			undoEvent.getSource().setUndoing(false);
		}
	}

	public void redo(){
		super.redo();
		try {
			undoEvent.getSource().setUndoing(true);
		    modifyProperty(undoEvent.getNewValue());
		} catch (IllegalAccessException e) {
			logger.error("Couldn't access setter for "+
					undoEvent.getPropertyName(), e);
			throw new CannotUndoException();
		} catch (InvocationTargetException e) {
			logger.error("Setter for "+undoEvent.getPropertyName()+
					" on "+undoEvent.getSource()+" threw exception", e);
			throw new CannotUndoException();
		} catch (IntrospectionException e) {
			logger.error("Couldn't introspect source object "+
					undoEvent.getSource(), e);
			throw new CannotUndoException();
		} finally {
			undoEvent.getSource().setUndoing(false);
		}
	}
	
	private void modifyProperty(Object value) throws IntrospectionException,
	    IllegalArgumentException, IllegalAccessException,
	    InvocationTargetException {
		// We did this using BeanUtils.copyProperty() before, but the error
		// messages were too vague.
		BeanInfo info = Introspector.getBeanInfo(undoEvent.getSource().getClass());
		
		logger.debug("Modifying property " + undoEvent.getPropertyName() + " on object " + undoEvent.getSource() + " with value " + value);
		if (logger.isDebugEnabled() && undoEvent.getChangeIndices() != null) {
			for (int i : undoEvent.getChangeIndices()) {
				logger.debug("Change on index " + i);
			}
		}
		PropertyDescriptor[] props = info.getPropertyDescriptors();
		for (PropertyDescriptor prop : Arrays.asList(props)) {
		    if (prop.getName().equals(undoEvent.getPropertyName())) {
		        Method writeMethod = prop.getWriteMethod();
		        if (writeMethod != null && !(undoEvent.getSource() instanceof MungeStep && undoEvent.getPropertyName().equals("inputs"))) {
                    logger.debug("writeMethod is: " + writeMethod);
                    logger.debug("value is: " + (value != null ? value.getClass().getName() : "") + value);
		            writeMethod.invoke(undoEvent.getSource(), new Object[] { value });
		            return;
		        }
		    }
		}
		logger.debug("Undoing specific types");
		if (undoEvent.getSource() instanceof MungeStep) {
			if (undoEvent.getPropertyName().equals("inputs")) {
				MungeStep step = (MungeStep)undoEvent.getSource();
				if (value == null) {
					for(int index : undoEvent.getChangeIndices()) {
						step.disconnectInput(index);
					}
				} else {
					if (value instanceof MungeStepOutput) { 
						for(int index : undoEvent.getChangeIndices()) {
							step.connectInput(index, (MungeStepOutput) value);
						}
					} else {
						throw new IllegalStateException("inputs of wrong type: " + value.getClass());
					}
				}
			} else if (undoEvent.getPropertyName().equals("addInputs")) {
				MungeStep step = (MungeStep)undoEvent.getSource();
				if (value == null) {
					for(int index : undoEvent.getChangeIndices()) {
						step.removeInput(index);
					}
				} else {
					if (value instanceof InputDescriptor) { 
						for(int index : undoEvent.getChangeIndices()) {
							step.addInput((InputDescriptor) value, index);
						}
					} else {
						throw new IllegalStateException("input descriptor of wrong type: " + value.getClass());
					}
				}
			} else {
				MungeStep step = (MungeStep)undoEvent.getSource();
				step.setParameter(undoEvent.getPropertyName(), (String) value);
			}
		}
	}
}
