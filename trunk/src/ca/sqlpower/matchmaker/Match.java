package ca.sqlpower.matchmaker;

import java.sql.Types;
import java.util.List;

import org.apache.log4j.Logger;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLIndex.Column;
import ca.sqlpower.architect.SQLIndex.IndexType;
import ca.sqlpower.matchmaker.util.ViewSpec;

/**
 * folder is the parent of match. should be not null.
 */
public class Match extends AbstractMatchMakerObject<Match, MatchMakerFolder> {

    private static final Logger logger = Logger.getLogger(Match.class);

	public enum MatchMode {
		FIND_DUPES("Find Duplicates"), BUILD_XREF("Build Cross-Reference");

		String displayName;

		private MatchMode(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String toString() {
			return displayName;
		}

		/**
		 * Get the MatchType that corrisponds to the type string.
		 *
		 * in the old version export, we have type like 'FIND DUPLICATES', etc...
		 * @param type a string detailing the type you want to get
		 * @return the match type that has type as its toString
		 */
		public static MatchMode getTypeByString(String type){
			MatchMode[] types = MatchMode.values();

			for (MatchMode matchType: types) {
				if (matchType.toString().toLowerCase().equals(type.toLowerCase())) {
					return matchType;
				} else if ("FIND DUPLICATES".toLowerCase().equals(type.toLowerCase())) {
					return FIND_DUPES;
				}


			}
			throw new IllegalArgumentException("There is no match type with a string "+type);
		}
	}

	/** the oid for the match */
	private Long oid;

	/** The type of match */
    private MatchMode type;

	/** The settings for the match engine */
    private MatchSettings matchSettings = new MatchSettings();;

	/** the settings for the merge engine */
    private MergeSettings mergeSettings = new MergeSettings();;

	/** a filter for the tables that are matched */
    private String filter;

	/** an optional source for the match created from a view */
    private ViewSpec view;

    /**
     * Contains the match criteria and the match critera groups
     */
    private MatchMakerFolder<MatchMakerCriteriaGroup> matchCriteriaGroupFolder =
    	new MatchMakerFolder<MatchMakerCriteriaGroup>();

    private CachableTable sourceTablePropertiesDelegate = new CachableTable("sourceTable");
    private CachableTable resultTablePropertiesDelegate = new CachableTable("resultTable");
    private CachableTable xrefTablePropertiesDelegate = new CachableTable("xrefTable");

    /**
     * The unique index of the source table that we're using.  Not necessarily one of the
     * unique indices defined in the database; the user can pick an arbitrary set of columns.
     */
    private SQLIndex sourceTableIndex;

	public Match() {
        matchCriteriaGroupFolder.setName("Match Criteria Groups");
        this.addChild(matchCriteriaGroupFolder);
        setType(MatchMode.FIND_DUPES);
	}
	/**
	 * FIXME Implement me
	 *
	 */
	public void execute() {
		throw new NotImplementedException();
	}

	/**
	 * FIXME Implement me
	 *
	 */
	public boolean checkValid() {
		throw new NotImplementedException();
	}

    /**
     * Returns true if the current resultTable of this match exists
     * in the session's database; false otherwise.
     * @throws ArchitectException If there are problems accessing the session's database
     */
	public static boolean doesResultTableExist(MatchMakerSession session, Match match) throws ArchitectException {
		return tableExists(session,
							match.getResultTableCatalog(),
							match.getResultTableSchema(),
							match.getResultTableName());
	}

	/**
	 * Returns true if the source table of this match exists in the
	 * session's database; false otherwise.
	 * @throws ArchitectException
	 */
	static boolean doesSourceTableExist(MatchMakerSession session, Match match) throws ArchitectException {
		return tableExists(session,
				match.getSourceTableCatalog(),
				match.getSourceTableSchema(),
				match.getSourceTableName());
	}

	/**
     * Returns true if the SQL table exists
     * in the session's database; false otherwise.
     * @throws ArchitectException If there are problems accessing the session's database
     */
	private static boolean tableExists(MatchMakerSession session,
			String catalog, String schema, String tableName)
		throws ArchitectException {
		SQLDatabase currentDB = session.getDatabase();
		SQLDatabase tempDB = null;
		try {
			tempDB = new SQLDatabase(currentDB.getDataSource());
			return tempDB.getTableByName(
					catalog,
					schema,
					tableName) != null;
		} finally {
			if (tempDB != null) tempDB.disconnect();
		}
	}

	/**
	 * Creates the result table for this Match based on the properties
	 * of the current source table. The result table name will be the
	 * current setting for resultTableName.
	 * <p>
	 * This method only sets up an in-memory SQLTable.  You still have
	 * to do the physical creation operation in the database yourself.
	 *
	 * @throws IllegalStateException If the current result table catalog,
	 * schema, and name are not set up properly to correspond with the
	 * session's database.
	 * <p>
	 * <b>or</b>
	 * <p>
	 * If the source table property of this match is not set yet.
	 * @throws ArchitectException If there is trouble working with the
	 * source table.
	 */
	public SQLTable createResultTable() throws ArchitectException {
		SQLIndex si = getSourceTableIndex();

		if (si == null) {
			throw new IllegalStateException(
					"You have to set up the source table of a match " +
					"before you can create its result table!");
		}
		SQLTable oldResultTable = getResultTable();
		if (oldResultTable == null) {
			throw new IllegalStateException(
					"You have to properly specify the result table " +
					"catalog, schema, and name before calling " +
					"createResultTable()");
		}
		SQLTable t = new SQLTable(oldResultTable.getParent(), oldResultTable.getName(), oldResultTable.getRemarks(), "TABLE", true);

		logger.debug("createResultTable: si="+si+" si.children.size="+si.getChildCount());

		addResultTableColumns(t, si, "dup_candidate_1");
		addResultTableColumns(t, si, "dup_candidate_2");
		addResultTableColumns(t, si, "current_candidate_1");
		addResultTableColumns(t, si, "current_candidate_2");
		addResultTableColumns(t, si, "dup_id");
		addResultTableColumns(t, si, "master_id");

		SQLColumn col;
		for (int i = 0; i < si.getChildCount(); i++) {
			col = new SQLColumn(t, "candidate_1"+i+"_mapped", Types.VARCHAR, 1, 0);
			t.addColumn(col);
		}

		for (int i = 0; i < si.getChildCount(); i++) {
			col = new SQLColumn(t, "candidate_2"+i+"_mapped", Types.VARCHAR, 1, 0);
			t.addColumn(col);
		}

		col = new SQLColumn(t, "match_percent", Types.INTEGER, 10, 0);
		t.addColumn(col);

		col = new SQLColumn(t, "group_id", Types.VARCHAR, 30, 0);
		t.addColumn(col);

		col = new SQLColumn(t, "match_date", Types.TIMESTAMP, 0, 0);
		t.addColumn(col);

		col = new SQLColumn(t, "match_status", Types.VARCHAR, 15, 0);
		t.addColumn(col);

		col = new SQLColumn(t, "match_status_date", Types.TIMESTAMP, 0, 0);
		t.addColumn(col);

		col = new SQLColumn(t, "match_status_user", Types.VARCHAR, 35, 0);
		t.addColumn(col);

		col = new SQLColumn(t, "dup1_master_ind", Types.VARCHAR, 1, 0);
		t.addColumn(col);

		SQLIndex newidx = new SQLIndex(t.getName()+"_uniq", true, null, IndexType.HASHED, null);
		for (int i = 0; i < si.getChildCount() * 2; i++) {
			newidx.addChild(newidx.new Column(t.getColumn(i), true, false));
		}
		t.addIndex(newidx);
		setResultTable(t);

		return t;
	}

	/**
	 * Adds one columns to the given table for each column of the given index.
	 * The columns will be named baseName0, baseName1, ... and their type, precision,
	 * and scale will correspond with those of the columns in the given index.
	 *
	 * @param t The table to add columns to
	 * @param si The index to iterate over for type, precision, scale of the
	 * new columns.
	 * @param baseName The base name of the new columns.
	 */
	private void addResultTableColumns(SQLTable t, SQLIndex si, String baseName) throws ArchitectException {
		for (int i = 0; i < si.getChildCount(); i++) {
			SQLColumn idxCol = ((Column) si.getChild(i)).getColumn();
			logger.debug("addColumn: i="+i+" idx="+si.getChild(i)+" idxcol="+idxCol);
			SQLColumn newCol = new SQLColumn(t, baseName+i, idxCol.getType(), idxCol.getPrecision(), idxCol.getScale());
			t.addColumn(newCol);
		}
	}

	/**
	 * FIXME Implement me
	 *
	 */
	public void createViewTable() {
		throw new NotImplementedException();
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		String oldValue = this.filter;
		this.filter = filter;
		getEventSupport().firePropertyChange("filter", oldValue, this.filter);
	}

	public MatchSettings getMatchSettings() {
		return matchSettings;
	}

	public void setMatchSettings(MatchSettings matchSettings) {
		if (matchSettings==null) throw new NullPointerException("You should not try to set the settings to null");
		MatchSettings oldValue = this.matchSettings;
		this.matchSettings = matchSettings;
		getEventSupport().firePropertyChange("matchSettings", oldValue,
				this.matchSettings);
	}

	public MergeSettings getMergeSettings() {
		return mergeSettings;
	}

	public void setMergeSettings(MergeSettings mergeSettings) {
		MergeSettings oldValue = this.mergeSettings;
		this.mergeSettings = mergeSettings;
		getEventSupport().firePropertyChange("mergeSettings", oldValue,
				this.mergeSettings);
	}




	public MatchMode getType() {
		return type;
	}

	public void setType(MatchMode type) {
		MatchMode oldValue = this.type;
		this.type = type;
		getEventSupport().firePropertyChange("type", oldValue, this.type);
	}

	public ViewSpec getView() {
		return view;
	}

	public void setView(ViewSpec view) {
		ViewSpec oldValue = this.view;
		this.view = view;
		getEventSupport().firePropertyChange("view", oldValue, this.view);
	}


	public List<MatchMakerCriteriaGroup> getMatchGroups() {
		return getMatchCriteriaGroupFolder().getChildren();
	}

	public MatchMakerCriteriaGroup getMatchCriteriaGroupByName(String name) {
		List <MatchMakerCriteriaGroup> groups = getMatchCriteriaGroups();
		for ( MatchMakerCriteriaGroup g : groups) {
			if ( g.getName() != null && g.getName().equals(name)) {
				return g;
			}
		}
		return null;
	}

	@Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 0;
        result = PRIME * result + ((getName() == null) ? 0 : getName().hashCode());
        return result;
    }

	@Override
    public boolean equals(Object obj) {
		if ( !(obj instanceof Match) ) {
			return false;
		}
        if (this == obj) {
            return true;
        }
        final Match other = (Match) obj;
        if (getName() == null) {
            if (other.getName() != null)
                return false;
        } else if (!getName().equals(other.getName()))
            return false;
        return true;
    }
	public Long getOid() {
		return oid;
	}
	public void setOid(Long oid) {
		this.oid = oid;
	}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Match@").append(System.identityHashCode(this));
        sb.append(": oid=").append(oid);
        sb.append("; type=").append(type);
        sb.append("; sourceTable=").append(getSourceTable());
        sb.append("; resultTable=").append(getResultTable());
        sb.append("; xrefTable=").append(getXrefTable());
        sb.append("; matchSettings=").append(matchSettings);
        sb.append("; mergeSettings=").append(mergeSettings);
        sb.append("; filter=").append(filter);
        sb.append("; view=").append(view);
        return sb.toString();
    }

    /**
     * Adds a match criteria group to the criteria group folder of this match
     *
     * @param criteriaGroup
     */
    public void addMatchCriteriaGroup(MatchMakerCriteriaGroup criteriaGroup) {
        // The folder will fire the child inserted event
        matchCriteriaGroupFolder.addChild(criteriaGroup);
    }

    /**
     * Removes the match criteria group from the criteria group folder of this match
     *
     * @param criteriaGroup
     */
    public void removeMatchCriteriaGroup(MatchMakerCriteriaGroup criteriaGroup) {
        // The folder will fire the child removed event
        matchCriteriaGroupFolder.removeChild(criteriaGroup);
    }

    public List<MatchMakerCriteriaGroup> getMatchCriteriaGroups(){
        return matchCriteriaGroupFolder.getChildren();
    }

    public void setMatchCriteriaGroups(List<MatchMakerCriteriaGroup> groups){
        matchCriteriaGroupFolder.setChildren(groups);
    }

    public MatchMakerFolder<MatchMakerCriteriaGroup> getMatchCriteriaGroupFolder() {
        return matchCriteriaGroupFolder;
    }

    /**
     * duplicate the match object. by inserting a new set of record 
     * to the matchmaker tables.
     * objects under different id and oid 
     * @return true if nothing wrong.
     * @throws ArchitectException 
     */
	public Match duplicate(String targetName) throws ArchitectException {
		Match newMatch = new Match();
		newMatch.setParent(getParent());
		newMatch.setName(targetName);
		newMatch.setFilter(getFilter()==null?null:new String(getFilter()));
		newMatch.setMergeSettings(getMergeSettings().duplicate());
		newMatch.setMatchSettings(getMatchSettings().duplicate());
		newMatch.setSourceTable(getSourceTable());
		newMatch.setResultTable(getResultTable());
		newMatch.setXrefTable(getXrefTable());
		newMatch.setType(getType());
		newMatch.setView(getView()==null?null:getView().duplicate());
		newMatch.setSession(getSession());
		
		for (MatchMakerCriteriaGroup g : getMatchCriteriaGroups()) {
			MatchMakerCriteriaGroup newGroup = g.duplicate();
			newMatch.addMatchCriteriaGroup(newGroup);
		}
	
		return newMatch;
	}
	

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
    private class CachableTable {

        /**
         * The name of the Match property we're maintaining (for example,
         * sourceTable, xrefTable, or resultTable).
         */
        private final String propertyName;

        private String catalogName;
        private String schemaName;
        private String tableName;
        private SQLTable cachedTable;

        CachableTable(String propertyName) {
            this.propertyName = propertyName;
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
            if (cachedTable != null) {
                return cachedTable;
            }
            if (tableName == null) {
            	return null;
            }

            try {
                logger.debug("Match.getSourceTable("+catalogName+","+schemaName+","+tableName+")");
                MatchMakerSession session = getSession();
                SQLDatabase db = session.getDatabase();
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
                    session.handleWarning("The location of "+propertyName+" "+catalogName+"."+schemaName+"."+tableName +
                                    " in Match "+getName()+ " is not compatible with the "+db.getName() +" database. " +
                                    "The table selection has been reset to nothing");
                    return null;
                }
            } catch (ArchitectException e) {
                throw new RuntimeException(e);
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

            getEventSupport().firePropertyChange(propertyName, oldValue, newValue);
        }

    }


    /////// The source table delegate methods //////
    public SQLTable getSourceTable() {
        return sourceTablePropertiesDelegate.getSourceTable();
    }
    public String getSourceTableCatalog() {
        return sourceTablePropertiesDelegate.getCatalogName();
    }
    public String getSourceTableSchema() {
    	return sourceTablePropertiesDelegate.getSchemaName();
    }
    public String getSourceTableName() {
        return sourceTablePropertiesDelegate.getTableName();
    }
    public void setSourceTable(SQLTable sourceTable) {
        sourceTablePropertiesDelegate.setTable(sourceTable);
    }
    public void setSourceTableCatalog(String sourceTableCatalog) {
        sourceTablePropertiesDelegate.setCatalogName(sourceTableCatalog);
    }
    public void setSourceTableSchema(String sourceTableSchema) {
    	sourceTablePropertiesDelegate.setSchemaName(sourceTableSchema);
    }
    public void setSourceTableName(String sourceTableName) {
        sourceTablePropertiesDelegate.setTableName(sourceTableName);
    }

    /**
     * Hooks the index up to the source table, attempts to resolve the
     * column names to actual SQLColumn references on the source table,
     * and then returns it!
     */
    public SQLIndex getSourceTableIndex() throws ArchitectException {
    	if (getSourceTable() != null && sourceTableIndex != null) {
    		sourceTableIndex.setParent(getSourceTable().getIndicesFolder());
    		resolveSourceTableIndexColumns(sourceTableIndex);
    	}
    	return sourceTableIndex;
    }

    /**
     * Attempts to set the column property of each index column in the
     * sourceTableColumns.  The UserType for SQLIndex can't do this because
     * the source table isn't populated yet when it's invoked.
     */
    private void resolveSourceTableIndexColumns(SQLIndex si) throws ArchitectException {
    	SQLTable st = getSourceTable();
    	for (SQLIndex.Column col : (List<SQLIndex.Column>) si.getChildren()) {
    		SQLColumn actualColumn = st.getColumnByName(col.getName());
    		col.setColumn(actualColumn);
    	}
	}

	public void setSourceTableIndex(SQLIndex index) {
    	final SQLIndex oldIndex = sourceTableIndex;
    	sourceTableIndex = index;
    	getEventSupport().firePropertyChange("sourceTableIndex", oldIndex, index);
    }

    /////// The result table delegate methods //////
    public SQLTable getResultTable() {
        return resultTablePropertiesDelegate.getSourceTable();
    }
    public String getResultTableCatalog() {
        return resultTablePropertiesDelegate.getCatalogName();
    }
    public String getResultTableName() {
        return resultTablePropertiesDelegate.getTableName();
    }
    public String getResultTableSchema() {
        return resultTablePropertiesDelegate.getSchemaName();
    }
    public void setResultTable(SQLTable resultTable) {
        resultTablePropertiesDelegate.setTable(resultTable);
    }
    public void setResultTableCatalog(String resultTableCatalog) {
        resultTablePropertiesDelegate.setCatalogName(resultTableCatalog);
    }
    public void setResultTableName(String resultTableName) {
        resultTablePropertiesDelegate.setTableName(resultTableName);
    }
    public void setResultTableSchema(String resultTableSchema) {
        resultTablePropertiesDelegate.setSchemaName(resultTableSchema);
    }


    /////// The xref table delegate methods //////
    public SQLTable getXrefTable() {
        return xrefTablePropertiesDelegate.getSourceTable();
    }
    public String getXrefTableCatalog() {
        return xrefTablePropertiesDelegate.getCatalogName();
    }
    public String getXrefTableName() {
        return xrefTablePropertiesDelegate.getTableName();
    }
    public String getXrefTableSchema() {
        return xrefTablePropertiesDelegate.getSchemaName();
    }
    public void setXrefTable(SQLTable xrefTable) {
        xrefTablePropertiesDelegate.setTable(xrefTable);
    }
    public void setXrefTableCatalog(String xrefTableCatalog) {
        xrefTablePropertiesDelegate.setCatalogName(xrefTableCatalog);
    }
    public void setXrefTableName(String xrefTableName) {
        xrefTablePropertiesDelegate.setTableName(xrefTableName);
    }
    public void setXrefTableSchema(String xrefTableSchema) {
        xrefTablePropertiesDelegate.setSchemaName(xrefTableSchema);
    }
}