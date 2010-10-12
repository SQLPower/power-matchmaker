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

package ca.sqlpower.matchmaker.dao;

import ca.sqlpower.dao.upgrade.AbstractUpgradePersisterManager;

public class MatchMakerUpgradePersisterManager extends
		AbstractUpgradePersisterManager {

	/**
	 * This is the initial state of the saved data. When this is updated, be sure
	 * to document the changes made.
	 */
	public static final int VERSION = 1;
	
	@Override
	public int getStateVersion() {
		return VERSION;
	}

}
