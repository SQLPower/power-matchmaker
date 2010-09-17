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


package ca.sqlpower.matchmaker;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;

public class TestingAbstractMatchMakerObject
				extends AbstractMatchMakerObject{
	
	/**
	 * Defines an absolute ordering of the child types of this class.
	 */
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.emptyList();
	
	int i;

	public TestingAbstractMatchMakerObject( ) {
		Random rand  = new Random();
		i = rand.nextInt();
		MatchMakerSession session = new TestingMatchMakerSession();
		((TestingMatchMakerSession)session).setAppUser("app user name");
		this.setSession(session);
	}

	@Override
	public boolean equals(Object obj) {
		return this==obj;
	}

	@Override
	public int hashCode() {
		return i;
	}
	public List<? extends SPObject> getChildren() {
		return Collections.emptyList();
	}

	public boolean hasListener(SPListener listener) {
		return listeners.contains(listener);
	}

	public TestingAbstractMatchMakerObject duplicate(MatchMakerObject parent, MatchMakerSession session) {
		return null;
	}

	@Override
	public List<Class<? extends SPObject>> getAllowedChildTypes() {
		return allowedChildTypes;
	}

	@Override
	protected boolean removeChildImpl(SPObject child) {
		return false;
	}
}
