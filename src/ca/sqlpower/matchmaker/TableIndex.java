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

import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonProperty;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;

public class TableIndex extends AbstractMatchMakerObject{
	
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.emptyList();
	
	/**
     * The unique index of the source table that we're using.  Not necessarily one of the
     * unique indices defined in the database; the user can pick an arbitrary set of columns.
     */
    private SQLIndex tableIndex;
    
    
    private CachableTable table;
	private String indexRole;

	/**
	 * Manages a sqlindex on cachableTable table. 
	 * 
	 * The property argument is the property type the index should use for an event.
	 * We assume all arguments are non-null
	 */
	@Constructor
	public TableIndex(@ConstructorParameter(propertyName="table") CachableTable table, 
					@ConstructorParameter(propertyName="indexRole") String indexRole) {
		this.table = table;
		this.indexRole = indexRole;
	}
	
	@Accessor
	public CachableTable getTable() {
		return table;
	}
	
	@Accessor
	public String getIndexRole() {
		return indexRole;
	}
	
    /**
     * Hooks the index up to the source table, attempts to resolve the
     * column names to actual SQLColumn references on the source table,
     * and then returns it!
     */
	@Accessor
    public SQLIndex getTableIndex() {
		try {
	    	if (tableIndex != null && !tableIndex.isPopulated()) tableIndex.populate();
	    	
	    	if (table.getTable() != null && tableIndex != null) {
	    		tableIndex.setParent(table.getTable());
					resolveTableIndexColumns(tableIndex);
	    	}
		} catch (SQLObjectException e) {
			throw new RuntimeException(e);
		}
    	return tableIndex;
    }

    /**
     * Attempts to set the column property of each index column in the
     * sourceTableColumns.  The UserType for SQLIndex can't do this because
     * the source table isn't populated yet when it's invoked.
     */
    private void resolveTableIndexColumns(SQLIndex si) throws SQLObjectException {
    	SQLTable st = table.getTable();
    	if (!st.isPopulated()) st.populate();
    	for (SQLIndex.Column col : si.getChildren(SQLIndex.Column.class)) {
    		SQLColumn actualColumn = st.getColumnByName(col.getName());
    		col.setColumn(actualColumn);
    	}
	}

    @Mutator
	public void setTableIndex(SQLIndex index) {
    	final SQLIndex oldIndex = tableIndex;
    	tableIndex = index;
    	//TODO: Find the right propertyName
    	firePropertyChange(indexRole, oldIndex, index);
    }

	/**
	 * Returns true if the index is user created.  False if the
	 * index is not user created or is null
	 * we check if the index is user created by if the parent
	 * is null or dosn't contain the sql index.
	 */
	@NonProperty
	public boolean isUserCreated() throws SQLObjectException {
		if (getTableIndex() == null) return false;
		if (getTableIndex().getParent() == null ){ 
			return true;
		} else if (getTableIndex().getParent().getChildren().contains(tableIndex)){
			return false;
		} else {
			return true;
		}
	}
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Table Index @").append(System.identityHashCode(this));
		buf.append(", index: "+tableIndex);
		return buf.toString();
	}

	@Override
	public MatchMakerObject duplicate(MatchMakerObject parent,
			MatchMakerSession session) {
		TableIndex t = new TableIndex(getTable(), getIndexRole());
		t.setTableIndex(tableIndex);
		t.setParent(parent);
		t.setName(getName());
		t.setVisible(isVisible());
		return t;
	}

	@Override
	@NonProperty
	public List<? extends SPObject> getChildren() {
		return Collections.emptyList();
	}

	@Override
	@NonProperty
	public List<Class<? extends SPObject>> getAllowedChildTypes() {
		return allowedChildTypes;
	}
}
