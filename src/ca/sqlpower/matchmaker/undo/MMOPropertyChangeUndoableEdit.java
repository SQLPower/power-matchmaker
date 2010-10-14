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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;

import org.apache.log4j.Logger;

import ca.sqlpower.object.SPObject;

public class MMOPropertyChangeUndoableEdit extends AbstractUndoableEdit{
	
	private static final Logger logger = Logger.getLogger(MMOPropertyChangeUndoableEdit.class);
	
	private PropertyChangeEvent undoEvent;

	public MMOPropertyChangeUndoableEdit(PropertyChangeEvent e){
		super();
		undoEvent = e;
	}

	public void undo(){
		super.undo();
		try {
			((SPObject)undoEvent.getSource()).setMagicEnabled(false);
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
			((SPObject)undoEvent.getSource()).setMagicEnabled(true);
		}
	}

	public void redo(){
		super.redo();
		try {
			((SPObject)undoEvent.getSource()).setMagicEnabled(false);
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
			((SPObject)undoEvent.getSource()).setMagicEnabled(true);
		}
	}
	
	private void modifyProperty(Object value) throws IntrospectionException,
	    IllegalArgumentException, IllegalAccessException,
	    InvocationTargetException {
		// We did this using BeanUtils.copyProperty() before, but the error
		// messages were too vague.
		BeanInfo info = Introspector.getBeanInfo(undoEvent.getSource().getClass());
		
		logger.debug("Modifying property " + undoEvent.getPropertyName() + " on object " + undoEvent.getSource() + " with value " + value);
		PropertyDescriptor[] props = info.getPropertyDescriptors();
		for (PropertyDescriptor prop : Arrays.asList(props)) {
		    if (prop.getName().equals(undoEvent.getPropertyName())) {
		        Method writeMethod = prop.getWriteMethod();
		        if (writeMethod != null) {
                    logger.debug("writeMethod is: " + writeMethod);
                    logger.debug("value is: " + (value != null ? value.getClass().getName() : "") + value);
		            writeMethod.invoke(undoEvent.getSource(), new Object[] { value });
		            return;
		        }
		    }
		}
	}
}
