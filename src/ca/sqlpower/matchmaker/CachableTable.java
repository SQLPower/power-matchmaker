/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.sqlobject.SQLTable;

/**
 * Provides the ability to maintain the SQLTable properties of the Project via
 * simple String properties.
 * <p>
 * All this behaviour would be better off in a Hibernate user type, but we
 * couldn't get that working, so we moved the logic into the business model.
 * Note that it doesn't depend on Hibernate in any way; it's just that the
 * Hibernate mappings are the only part of the application that use this
 * functionality.
 */
public class CachableTable {

	private static final Logger logger = Logger.getLogger(CachableTable.class); 
	/**
     * The name of the Project property we're maintaining (for example,
     * sourceTable, xrefTable, or resultTable).
     */
    private final String propertyName;
    private AbstractMatchMakerObject mmo;
    private String catalogName;
    private String schemaName;
    private String tableName;
    private SQLTable cachedTable;
    
    private String dsName = null;

    /**
     * Creates a new cachable table object that uses the database and
     * session of the given mmo, and fires property change events on its
     * behalf using the given property name.
     * 
     * @param mmo The match maker object this cachable table acts on behalf
     * of.  Unfortunately, we have to explicitly ask for an AbstractMatchMakerObject
     * because this class needs to access its propertyChangeSupport object and
     * its session reference (these things are not declared on the MatchMakerObject
     * interface, and shouldn't be). 
     * @param propertyName the property name that all property change events fired
     * on behalf of mmo will report.
     */
    CachableTable(AbstractMatchMakerObject mmo,String propertyName) {
    	if (mmo == null) throw new NullPointerException("Can't make a cachable table for a null owner");
    	if (propertyName == null) throw new NullPointerException("Can't make a cachable table for a null property name");
		this.propertyName = propertyName;
		this.mmo = mmo;		
    }

    public String getCatalogName() {
        if (cachedTable != null) {
            String catalogName = cachedTable.getCatalogName();
            if (catalogName == null || catalogName.length() == 0) {
                return null;
            } else {
                return catalogName;
            }
        } else {
            return catalogName;
        }
    }

    public void setCatalogName(String sourceTableCatalog) {
        cachedTable = null;
        this.catalogName = sourceTableCatalog;
    }

    public String getTableName() {
        if (cachedTable != null) {
        	return cachedTable.getName();
        } else {
            return tableName;
        }
    }

    public void setTableName(String sourceTableName) {
        cachedTable = null;
        this.tableName = sourceTableName;
    }

    public String getSchemaName() {
        if (cachedTable != null) {
            String schemaName = cachedTable.getSchemaName();
            if (schemaName == null || schemaName.length() == 0) {
                return null;
            } else {
                return schemaName;
            }
        } else {
            return schemaName;
        }
    }

    public void setSchemaName(String sourceTableSchema) {
        cachedTable = null;
        this.schemaName = sourceTableSchema;
    }
    
    /**
     * Sets the database that the table will be taken from. The string
     * that is passed in is the name of the spDataSource taken from the
     * list of connections.
     * 
     * @param spDataSourceName The name of the datasource.
     */
    public void setSPDataSource(String spDataSourceName) {
    	cachedTable = null;
    	this.dsName = spDataSourceName;
    }
    
    /**
     * Gets the name of the spDataSource that the database is coming
     * from.
     * 
     */
    public String getSPDataSourceName() {
    	if (cachedTable != null && cachedTable.getParentDatabase() != null && 
    			cachedTable.getParentDatabase().getDataSource() != null) {
    		return cachedTable.getParentDatabase().getDataSource().getName();
    	}
    	return dsName == null ? "" : dsName;
    }
    
    /**
     * Returns the SPDataSource for the current table
     */
    public SPDataSource getSPDataSource() {
    	if (cachedTable != null) {
    		return cachedTable.getParentDatabase().getDataSource();
    	}

    	// if no data source is specified, should default to repository data source
        if (dsName == null || dsName.length() == 0) {
            return null;
        }
    	
    	MatchMakerSession session = mmo.getSession();
        MatchMakerSessionContext context = session.getContext();
        List<SPDataSource> dataSources = context.getDataSources();
        for (SPDataSource spd : dataSources) {
			if (spd != null && dsName.equals(spd.getName())) {
				return spd;
			}
		}
        
    	throw new IllegalArgumentException("Error: No database connection named " + dsName + 
    			". Please create a database connection named " + dsName + " and try again.");
    }

    /**
     * Performs some magic to synchronize the sourceTableCatalog,
     * sourceTableSchema, and sourceTableName properties with the sourceTable
     * property. Calling this getter may result in a SQLDatabase lookup of the
     * table specified by the combination of the sourceTableXXX properties.
     *
     * @return The most recently-returned SQLTable instance (the "cached
     *         sourceTable") unless one of the setSourceTableXXX methods has
     *         been called since the last call to this method.
     *         <p>
     *         Returns null if the sourceTableName property is null and there is
     *         no cached sourceTable.
     *         <p>
     *         If there is no cached sourceTable and the sourceTableName is not
     *         null, this method returns a new SQLTable which is either looked
     *         up in the session's SQLDatabase, or created in the session's
     *         SQLDatabase if the lookup failed.
     */
    public SQLTable getSourceTable() {  // XXX rename to getTable
    	logger.debug("GetSourceTable(): "+this);
    	
        if (cachedTable != null) {
        	return cachedTable;
        }
        if (tableName == null) {
        	return null;
        }

        try {
			logger.debug("getSourceTable(" + dsName + ","+catalogName+","+schemaName+","+tableName+")");
			logger.debug("mmo.parent="+mmo.getParent());
			
			SQLDatabase db = null;
			if (getSPDataSource() == null) {
				db = mmo.getSession().getDatabase();
			} else {
				db = mmo.getSession().getDatabase(getSPDataSource());
			}
			
			if (ArchitectUtils.isCompatibleWithHierarchy(db, catalogName, schemaName, tableName)){
				SQLTable table = db.getTableByName(catalogName, schemaName, tableName);
				if (table == null) {
					logger.debug("     Not found.  Adding simulated...");
					table = ArchitectUtils.addSimulatedTable(db, catalogName, schemaName, tableName);
				} else {
					logger.debug("     Found!");
				}
				cachedTable = table;
				return cachedTable;
			} else {
				mmo.getSession().handleWarning("The location of "+propertyName+" "+catalogName+"."+schemaName+"."+tableName +
						" in Project "+mmo.getName()+ " is not compatible with the "+db.getName() +" database. " +
				"The table selection has been reset to nothing");
				return null;
			}
		} catch (SQLObjectException e) {
			throw new SQLObjectRuntimeException(e);
		}
    }

    /**
     * Sets the table to the given table, clears the simple string properties, and fires an event.
     * @param table
     */
    public void setTable(SQLTable table) {
    	logger.debug("Set Table: " + table);
    	
        final SQLTable oldValue = cachedTable;
        final SQLTable newValue = table;

        catalogName = null;
        schemaName = null;
        tableName = null;
        cachedTable = table;
        dsName = null;

        // XXX create an InternalMatchMakerObject package-private interface that lets us do this,
        //     and don't declare mmo as AbstractMatchMakerObject anymore
        mmo.getEventSupport().firePropertyChange(propertyName, oldValue, newValue);
    }

    @Override
    public String toString() {
    	return "CachableTable:" +
    			" mmo="+(mmo == null ? "null" : mmo.getClass().getName() + System.identityHashCode(mmo)) + 
    			" ds=" + getSPDataSourceName() +
    			" catalogName=" + catalogName +
    			" schemaName="+schemaName+
    			" tableName="+tableName+
    			" cachedTable="+(cachedTable == null ? "null" : cachedTable.getName()) +
    			" propertyName="+propertyName;
    }
}