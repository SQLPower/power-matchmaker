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

import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.NonProperty;
import ca.sqlpower.sqlobject.SQLTable;


public class ColumnMergeRulesImportedCachableColumn extends CachableColumn {
	
	@Constructor
	public ColumnMergeRulesImportedCachableColumn() {
		super("importedKeyColumn");
	}
	
	@NonProperty
	public SQLTable getTable() {
		TableMergeRules tableMergeRules = (TableMergeRules) getParent().getParent();
        if (tableMergeRules == null) throw new NullPointerException("Not attached to a parent");
        if (tableMergeRules.getParentMergeRule() == null) {
        	return null;
        } else {
        	SQLTable st = tableMergeRules.getParentMergeRule().getSourceTable();
        	return st;
        }
	}

	public MatchMakerObject duplicate(MatchMakerObject parent,
			MatchMakerSession session) {
		ColumnMergeRulesImportedCachableColumn c = new ColumnMergeRulesImportedCachableColumn();
		c.setSession(session);
		c.setName(getName());
		c.setParent(parent);
		c.setColumnName(getColumnName());
		return c;
	}
}
