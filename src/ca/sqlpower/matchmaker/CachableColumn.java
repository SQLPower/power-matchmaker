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
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.sqlobject.SQLTable;

public abstract class CachableColumn extends AbstractMatchMakerObject {
	
	public static final List<Class<? extends SPObject>> allowedChildTypes =
    	Collections.emptyList();
	
	private SQLColumn cachedColumn;
	private String columnName;
	AbstractMatchMakerObject eventSource;
	private String property;
	
    public CachableColumn(String property) {
		this.property = property;
	}

	/**
     * Returns the name of the column this set of criteria applies to.
     * You should use {@link #getColumn()} under normal circumstances.
     */
    public String getColumnName() {
        if (cachedColumn == null){
       		return columnName;
        } else {
        	return cachedColumn.getName();
        }
    }

    /**
     * Sets the column name, and nulls out the cached SQLColumn.  The next
     * call to getColumn() will result in an attempt to resolve the SQLColumn
     * that this columnName string refers to.
     * <p>
     * Note, this property is not bound.  However, it is coordinated with the
     * bound property <tt>column</tt>, so setting the column name like this
     * may eventually result in a property change event for the "column" property.
     * 
     * @param columnName the name of the project's source table column these munge
     * steps are associated with.
     */
    public void setColumnName(String columnName) {
        cachedColumn = null;
        this.columnName = columnName;
    }

    /**
     * Attempts to resolve the given column name to a column of the owning Project
     * object's source table. This functionality is provided for the benefit of
     * the ORM layer, which has difficulty using the business model.
     * <p>
     * WARNING: This method has the side effect of calling setColumn() the first
     * time you call it and columnName isn't null. The values of cachedColumn and
     * columnName may be modified, and if so, a change event will be fired.
     * 
     * @throws NullPointerException
     *             if any of the business objects required for resolving the
     *             column object are missing
     * @throws SQLObjectRuntimeException
     *             if getColumnByName fails
     */
    public SQLColumn getColumn() {
        if (cachedColumn != null) return cachedColumn;
        if (columnName == null) return null;
        
        try {
            SQLTable st = getTable();
            if (st == null) throw new NullPointerException("The table owner has no source table specified");
            SQLColumn newColumn = st.getColumnByName(columnName);

            // did we actually make it here?
            SQLColumn oldVal = this.cachedColumn;
            this.cachedColumn = newColumn;
            this.columnName = (newColumn == null ? null : newColumn.getName());

            return newColumn;
            
        } catch (SQLObjectException ex) {
            throw new SQLObjectRuntimeException(ex);
        }
    }

	public abstract SQLTable getTable();

    /**
     * Sets the cached column as well as the simple columnName string.
     */
    public void setColumn(SQLColumn column) {
        SQLColumn oldVal = this.cachedColumn;
        this.cachedColumn = column;
        this.columnName = (column == null ? null : column.getName());
        //TODO: find the right property name
        firePropertyChange(property, oldVal, column);
    }
    
	public List<SPObject> getChildren() {
		return Collections.emptyList();
	}
	
	public List<Class<? extends SPObject>> getAllowedChildTypes() {
		return allowedChildTypes;
	}


	public MatchMakerObject duplicate(MatchMakerObject parent,
			MatchMakerSession session) {
		throw new RuntimeException("Don't do this duplication because it sucks.");
	}
}
