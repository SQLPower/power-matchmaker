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

import java.util.List;

import ca.sqlpower.matchmaker.dao.MatchMakerDAO;

public class PlFolderDAOStup implements MatchMakerDAO<PlFolder> {
	
	//TODO refactor this to stub?
	public void delete(PlFolder deleteMe) {
	}

	public List<PlFolder> findAll() {
		return null;
	}

	public Class<PlFolder> getBusinessClass() {
		return null;
	}

	public void save(PlFolder saveMe) {
	}

}
