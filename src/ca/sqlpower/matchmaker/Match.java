package ca.sqlpower.matchmaker;

import java.util.List;

import org.apache.log4j.Logger;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.util.SourceTable;
import ca.sqlpower.matchmaker.util.ViewSpec;

/**
 * folder is the parent of match. should be not null.
 */
public class Match extends AbstractMatchMakerObject<Match, MatchMakerFolder> {

    private static final Logger logger = Logger.getLogger(Match.class);
    
	public enum MatchType {
		FIND_DUPES("Find Duplicates"), BUILD_XREF("Build Cross-Reference");

		String displayName;

		private MatchType(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String toString() {
			return displayName;
		}

		/**
		 * Get the MatchType that corrisponds to the type string.
		 *
		 * @param type a string detailing the type you want to get
		 * @return the match type that has type as its toString
		 */
		public static MatchType getTypeByString(String type){
			MatchType[] types = MatchType.values();

			for (MatchType matchType: types) {
				if (matchType.toString().toLowerCase().equals(type.toLowerCase())) {
					return matchType;
				}
			}
			throw new IllegalArgumentException("There is no match type with a string "+type);
		}
	}

	/** the oid for the match */
	private Long oid;

	/** The type of match */
    private MatchType type;

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
    
	public Match( ) {
        matchCriteriaGroupFolder.setName("Match Criteria Groups");
        this.addChild(matchCriteriaGroupFolder);        
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
	 * FIXME Implement me
	 *
	 */
	public void createResultTable() {
		throw new NotImplementedException();
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




	public MatchType getType() {
		return type;
	}

	public void setType(MatchType type) {
		MatchType oldValue = this.type;
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
    public void addMatchCriteriaGroup(MatchMakerCriteriaGroup<MatchmakerCriteria> criteriaGroup) {
        // The folder will fire the child inserted event
        matchCriteriaGroupFolder.addChild(criteriaGroup);
    }
    
    /**
     * Removes the match criteria group from the criteria group folder of this match
     * 
     * @param criteriaGroup 
     */
    public void removeMatchCriteriaGroup(MatchMakerCriteriaGroup<MatchmakerCriteria> criteriaGroup) {
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
        private SQLIndex index;
        private SourceTable cachedTable;
        
        CachableTable(String propertyName) {
            this.propertyName = propertyName;
        }
        
        public String getCatalogName() {
            if (cachedTable != null) {
                if (cachedTable.getTable() == null) return null;
                String catalogName = cachedTable.getTable().getCatalogName();
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
                return cachedTable.getTable().getName();
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
                if (cachedTable.getTable() == null) return null;
                String schemaName = cachedTable.getTable().getSchemaName();
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
        public SourceTable getSourceTable() {
            if (cachedTable != null) {
                return cachedTable;
            }
            if (tableName == null) {
                return new SourceTable();  // this has a null table and index
            }
            
            try {
                logger.debug("MatchWithSQLTableHelper.getSourceTable()");
                MatchMakerSession session = getSession();
                SQLDatabase db = session.getDatabase();
                SQLTable table = db.getTableByName(catalogName, schemaName, tableName);
                if (table == null) {
                    table = ArchitectUtils.addSimulatedTable(db, catalogName, schemaName, tableName);
                }
                SourceTable sourceTable = new SourceTable();
                sourceTable.setTable(table);
                sourceTable.setUniqueIndex(index);
                cachedTable = sourceTable;
                return sourceTable;
            } catch (ArchitectException e) {
                throw new RuntimeException(e);
            }
        }
        
        public void setSourceTable(SourceTable sourceTable) {
            final SourceTable oldSourceTable = this.cachedTable;
            cachedTable = sourceTable;
            getEventSupport().firePropertyChange(propertyName, oldSourceTable, sourceTable);
        }

        /**
         * Sets the table of the cached SourceTable, and fires an event.
         * @param table
         */
        public void setTable(SQLTable table) {
            final SQLTable oldValue = cachedTable == null ? null : cachedTable.getTable();
            final SQLTable newValue = table;
            cachedTable = new SourceTable();
            cachedTable.setTable(newValue);
            getEventSupport().firePropertyChange(propertyName, oldValue, newValue);
        }
        
        public SQLIndex getIndex() {
            return index;
        }
        public void setIndex(SQLIndex index) {
            this.index = index;
        }
    }


    /////// The source table delegate methods //////
    public SourceTable getSourceTable() {
        return sourceTablePropertiesDelegate.getSourceTable();
    }
    public String getSourceTableCatalog() {
        return sourceTablePropertiesDelegate.getCatalogName();
    }
    public SQLIndex getSourceTableIndex() {
        return sourceTablePropertiesDelegate.getIndex();
    }
    public String getSourceTableName() {
        return sourceTablePropertiesDelegate.getTableName();
    }
    public String getSourceTableSchema() {
        return sourceTablePropertiesDelegate.getSchemaName();
    }
    public void setSourceTable(SourceTable sourceTable) {
        sourceTablePropertiesDelegate.setSourceTable(sourceTable);
    }
    public void setSourceTableCatalog(String sourceTableCatalog) {
        sourceTablePropertiesDelegate.setCatalogName(sourceTableCatalog);
    }
    public void setSourceTableIndex(SQLIndex index) {
        sourceTablePropertiesDelegate.setIndex(index);
    }
    public void setSourceTableName(String sourceTableName) {
        sourceTablePropertiesDelegate.setTableName(sourceTableName);
    }
    public void setSourceTableSchema(String sourceTableSchema) {
        sourceTablePropertiesDelegate.setSchemaName(sourceTableSchema);
    }
    

    /////// The result table delegate methods //////
    public SQLTable getResultTable() {
        return resultTablePropertiesDelegate.getSourceTable().getTable();
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
        return xrefTablePropertiesDelegate.getSourceTable().getTable();
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