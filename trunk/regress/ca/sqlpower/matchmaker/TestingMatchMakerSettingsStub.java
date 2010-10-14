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

import ca.sqlpower.object.SPObject;

public class TestingMatchMakerSettingsStub extends MatchMakerSettings {
	
	public static final List<Class<? extends SPObject>> allowedChildTypes =
        Collections.emptyList();
	
	public MatchMakerSettings duplicate(MatchMakerObject parent) {
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
}
