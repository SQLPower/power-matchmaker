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

package ca.sqlpower.matchmaker.munge;

import java.util.Collections;
import java.util.List;

import ca.sqlpower.object.AbstractSPObject;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;


/**
 * This class contains the attributes of a particular MungeStep input. If there are
 * additional attributes to describe MungeStep input, it should be added to this class.
 * The attributes should all be immutable.
 */
public class InputDescriptor extends AbstractSPObject {
	
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.emptyList();
	
	private final Class type;
	
	@Constructor
	public InputDescriptor(@ConstructorParameter(propertyName="name") String name, 
			@ConstructorParameter(propertyName="type") Class type) {
		this.type = type;
	}
	
	@Accessor
	public Class getType() {
		return type;
	}
	
	@Override
	protected boolean removeChildImpl(SPObject child) {
		return false;
	}

	@Override
	public List<Class<? extends SPObject>> getAllowedChildTypes() {
		return allowedChildTypes;
	}

	@Override
	public List<? extends SPObject> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public List<? extends SPObject> getDependencies() {
		return Collections.emptyList();
	}

	@Override
	public void removeDependency(SPObject dependency) {
		//do nothing
	}
}
