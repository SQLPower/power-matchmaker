package ca.sqlpower.matchmaker;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;

/**
 * Provides the ability to maintain the SQLTable properties of the Match via
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
     * The name of the Match property we're maintaining (for example,
     * sourceTable, xrefTable, or resultTable).
     */
    private final String propertyName;
    private AbstractMatchMakerObject mmo;
    private String catalogName;
    private String schemaName;
    private String tableName;
    private SQLTable cachedTable;

    /**
     * Create a new cachable table.
     * 
     * Requres and abstract match maker object for event generation and
     * as a way to get the session and the database out of the session
     * So these need to be accessable from the match maker object.
     * 
     * propertyName is the name the property change events use for events
     */
    CachableTable(AbstractMatchMakerObject mmo,String propertyName) {
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
			logger.debug("Match.getSourceTable("+catalogName+","+schemaName+","+tableName+")");
			logger.debug("mmo.parent="+mmo.getParent());
			SQLDatabase db = mmo.getSession().getDatabase();
			if (ArchitectUtils.isCompatibleWithHierarchy(db, catalogName, schemaName, tableName)){
				SQLTable table = db.getTableByName(catalogName, schemaName, tableName);
				if (table == null) {
					Match.logger.debug("     Not found.  Adding simulated...");
					table = ArchitectUtils.addSimulatedTable(db, catalogName, schemaName, tableName);
				} else {
					Match.logger.debug("     Found!");
				}
				cachedTable = table;
				return cachedTable;
			} else {
				mmo.getSession().handleWarning("The location of "+propertyName+" "+catalogName+"."+schemaName+"."+tableName +
						" in Match "+mmo.getName()+ " is not compatible with the "+db.getName() +" database. " +
				"The table selection has been reset to nothing");
				return null;
			}
		} catch (ArchitectException e) {
			throw new ArchitectRuntimeException(e);
		}
    }

    /**
     * Sets the table to the given table, clears the simple string properties, and fires an event.
     * @param table
     */
    public void setTable(SQLTable table) {
        final SQLTable oldValue = cachedTable;
        final SQLTable newValue = table;

        catalogName = null;
        schemaName = null;
        tableName = null;
        cachedTable = table;

        // XXX create an InternalMatchMakerObject package-private interface that lets us do this,
        //     and don't declare mmo as AbstractMatchMakerObject anymore
        mmo.getEventSupport().firePropertyChange(propertyName, oldValue, newValue);
    }

    @Override
    public String toString() {
    	return "CachableTable:" +
    			" mmo="+(mmo == null ? "null" : mmo.getClass().getName() + System.identityHashCode(mmo)) + 
    			" catalogName="+catalogName+
    			" schemaName="+schemaName+
    			" tableName="+tableName+
    			" cachedTable="+(cachedTable == null ? "null" : cachedTable.getName()) +
    			" propertyName="+propertyName;
    }
}