/*
 * Copyright (c) 2007, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.munge;


/**
 * This class contains the attributes of a particular MungeStep input. If there are
 * additional attributes to describe MungeStep input, it should be added to this class.
 * The attributes should all be immutable. 
 */
public class InputDescriptor {
	
	private final Class type;
	private final String name;
	
	public InputDescriptor(String name, Class type) {
		this.type = type;
		this.name = name;
	}
	
	public Class getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
}
