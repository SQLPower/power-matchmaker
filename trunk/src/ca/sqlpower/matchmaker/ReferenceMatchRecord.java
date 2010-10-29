/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;

/**
 * This class helps us persist the graphs on the MM validation screen as a tree.
 * These hold a reference to the other side of the edge in the graph, and is also
 * a child under the SourceTableRecord
 */
public class ReferenceMatchRecord extends AbstractMatchMakerObject {
	
	private static final Logger logger = Logger.getLogger(ReferenceMatchRecord.class);
    
	/**
	 * Defines an absolute ordering of the child types of this class.
	 */
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.emptyList();
	
	/**
	 * Reference to the other side of the edge in the graphs.
	 */
	final PotentialMatchRecord potentialMatchRecord;
	
	@Constructor
	public ReferenceMatchRecord(
			@ConstructorParameter(propertyName="potentialMatchRecord") PotentialMatchRecord reference) {
		potentialMatchRecord = reference;
		setName("ReferenceMatchRecord");
	}

	@Override
	public MatchMakerObject duplicate(MatchMakerObject parent) {
		return null;
	}

	@Override
	public List<? extends SPObject> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public List<Class<? extends SPObject>> getAllowedChildTypes() {
		return allowedChildTypes;
	}
	
	@Accessor
	public PotentialMatchRecord getPotentialMatchRecord() {
		return potentialMatchRecord;
	}
}
