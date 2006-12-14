package ca.sqlpower.matchmaker;

import java.sql.Types;
import java.util.List;

import org.apache.log4j.Logger;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import ca.sqlpower.architect.ArchitectException;
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

    static final Logger logger = Logger.getLogger(Match.class);

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

    private final CachableTable sourceTablePropertiesDelegate;
    private final CachableTable resultTablePropertiesDelegate;
    private final CachableTable xrefTablePropertiesDelegate;

    /**
     * The unique index of the source table that we're using.  Not necessarily one of the
     * unique indices defined in the database; the user can pick an arbitrary set of columns.
     */
    private SQLIndex sourceTableIndex;

	public Match() {
	    sourceTablePropertiesDelegate = new CachableTable(this, "sourceTable");
	    resultTablePropertiesDelegate = new CachableTable(this,"resultTable");
	    xrefTablePropertiesDelegate = new CachableTable(this, "xrefTable");
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
		return session.tableExists(
							match.getResultTableCatalog(),
							match.getResultTableSchema(),
							match.getResultTableName());
	}

	/**
	 * Returns true if the source table of this match exists in the
	 * session's database; false otherwise.
	 * @throws ArchitectException
	 */
	public static boolean doesSourceTableExist(MatchMakerSession session, Match match) throws ArchitectException {
		return session.tableExists(
				match.getSourceTableCatalog(),
				match.getSourceTableSchema(),
				match.getSourceTableName());
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

		logger.debug("createResultTable: table parent=" +
				(oldResultTable.getParent()==null?"null":oldResultTable.getParent().getClass()) +
				"  name:[" +
				(oldResultTable.getParent()==null?"null":oldResultTable.getParent().getName()) +
				"]");
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
	 * compare column name and datatype
	 * @param c1 the column has the correct datatype
	 * @param c2 the column that you want to check
	 * @param name that name of the column that we were looking for
	 * @return true if name of c2 match name and type of c1 == type of c2
	 */
	private boolean compareColumnNameAndType(SQLColumn c1, SQLColumn c2, String name) {
		if (c2 == null) {
			logger.debug("Column name mismatched: excepted:" +
					name + "but not found.");
			return false;
		}
		if (c2.getType() != c1.getType()) {
			logger.debug("Column [" + c2.getName() +
					"] datatype mismatched: excepted:[" +
					 + c1.getType() +
					 "] but was:[" + c2.getType() + "]");
			return false;
		}
		return true;
	}
	
	/**
	 * Vetify the result table structure, the result table should looks 
	 * like these:
	 * <p>
	 * <p>dup_candidate_1xxx  [yyy],
	 * <p>dup_candidate_2xxx  [yyy],
	 * <p>current_candidate_1xxx [yyy],
	 * <p>current_candidate_2xxx [yyy],
	 * <p>dup_idxxx [yyy],
	 * <p>master_idxxx [yyy],
	 * <p>candidate_1xxx_mapped VARCHAR(1),
	 * <p>candidate_2xxx_mapped VARCHAR(1),
	 * <p>match_percent INTEGER(10),
	 * <p>group_id  VARCHAR(30),
	 * <p>match_date TIMESTAMP,
	 * <p>match_status VARCHAR(15),
	 * <p>match_status_date  TIMESTAMP,
	 * <p>match_status_user  VARCHAR(35),
	 * <p>dup1_master_ind  VARCHAR(1)
	 * <p>
	 * where xxx is a sequence from 0 to the total number of unique
	 * index column - 1 of the source table. yyy is the datatype of 
	 * column datatype of the unique index of the source table.
	 * 
	 * the result table may not exists in the database (in-memory), and
	 * the source unique index also may not in the database. you may need
	 * to call {@link MatchMakerSession.isThisSQLTableExists()} to vertify
	 * the result table existence
	 * <p> 
	 * @return false if the table is not exist in the sql database, or
	 * the table structure does not match above table structure.
	 * @throws ArchitectException if something unexcepted wrong
	 * 
	 * @throws IllegalStateException If the source table has not been setup 
	 * <p>
	 * <b>or</b>
	 * <p>unique index has not been setup 
	 * <p>
	 * <b>or</b>
	 * <p>session and sql database have not been setup for the match
	 */
	public boolean vertifyResultTableStruct() throws ArchitectException {

		MatchMakerSession session = getSession();
		if ( session == null ) {
			throw new IllegalStateException("Session has not been setup " +
					"for the match, you will need session and database " +
					"connection to check the result table");
		}
		SQLDatabase db = session.getDatabase();
		if ( db == null ) {
			throw new IllegalStateException("Database has not been setup " +
					"for the match session, you will need database " +
					"connection to check the result table");
		}
		SQLIndex si = getSourceTableIndex();
		if (si == null) {
			throw new IllegalStateException("No unique index specified " +
					"for the match, I don't know how to vertify the " +
					"result table stucture.");
		}
		SQLTable sourceTable = getSourceTable();
		if ( sourceTable == null) {
			throw new IllegalStateException("No source table specified " +
					"for the match, I don't know how to vertify the " +
					"result table stucture.");
		}

		SQLTable oldResultTable = getResultTable();
		if (oldResultTable == null) {
			throw new IllegalStateException(
					"You have to properly specify the result table " +
					"catalog, schema, and name before calling " +
					"vertifyResultTableStruct()");
		}

		String colName;
		SQLColumn column;
		SQLColumn indexColumn;
		for (int i = 0; i < si.getChildCount(); i++) {
			indexColumn = si.getChild(i).getColumn();
			colName = "dup_candidate_1"+i;
			column = oldResultTable.getColumnByName(colName);
			if ( !compareColumnNameAndType(indexColumn,column,colName)) {
				return false;
			}
			colName = "dup_candidate_2"+i;
			column = oldResultTable.getColumnByName(colName);
			if ( !compareColumnNameAndType(indexColumn,column,colName)) {
				return false;
			}
			colName = "current_candidate_1"+i;
			column = oldResultTable.getColumnByName(colName);
			if ( !compareColumnNameAndType(indexColumn,column,colName)) {
				return false;
			}
			colName = "current_candidate_2"+i;
			column = oldResultTable.getColumnByName(colName);
			if ( !compareColumnNameAndType(indexColumn,column,colName)) {
				return false;
			}
			colName = "dup_id"+i;
			column = oldResultTable.getColumnByName(colName);
			if ( !compareColumnNameAndType(indexColumn,column,colName)) {
				return false;
			}
			colName = "master_id"+i;
			column = oldResultTable.getColumnByName(colName);
			if ( !compareColumnNameAndType(indexColumn,column,colName)) {
				return false;
			}
			
			colName = "candidate_1"+i+"_mapped";
			column = oldResultTable.getColumnByName(colName);
			indexColumn = new SQLColumn(null,colName, Types.VARCHAR, 1, 0);
			if ( !compareColumnNameAndType(indexColumn,column,colName)) {
				return false;
			}
			colName = "candidate_2"+i+"_mapped";
			column = oldResultTable.getColumnByName(colName);
			if ( !compareColumnNameAndType(indexColumn,column,colName)) {
				return false;
			}
		}

		colName = "match_percent";
		column = oldResultTable.getColumnByName(colName);
		indexColumn = new SQLColumn(null, colName, Types.INTEGER, 10, 0);
		if ( !compareColumnNameAndType(indexColumn,column,colName)) {
			return false;
		}

		colName = "group_id";
		column = oldResultTable.getColumnByName(colName);
		indexColumn = new SQLColumn(null, colName, Types.VARCHAR, 30, 0);
		if ( !compareColumnNameAndType(indexColumn,column,colName)) {
			return false;
		}

		colName = "match_date";
		column = oldResultTable.getColumnByName(colName);
		indexColumn = new SQLColumn(null, colName, Types.TIMESTAMP, 0, 0);
		if ( !compareColumnNameAndType(indexColumn,column,colName)) {
			return false;
		}
		
		colName = "match_status";
		column = oldResultTable.getColumnByName(colName);
		indexColumn = new SQLColumn(null, colName, Types.VARCHAR, 15, 0);
		if ( !compareColumnNameAndType(indexColumn,column,colName)) {
			return false;
		}

		colName = "match_status_date";
		column = oldResultTable.getColumnByName(colName);
		indexColumn = new SQLColumn(null, colName, Types.TIMESTAMP, 0, 0);
		if ( !compareColumnNameAndType(indexColumn,column,colName)) {
			return false;
		}
		
		colName = "match_status_user";
		column = oldResultTable.getColumnByName(colName);
		indexColumn = new SQLColumn(null, colName, Types.VARCHAR, 35, 0);
		if ( !compareColumnNameAndType(indexColumn,column,colName)) {
			return false;
		}

		colName = "dup1_master_ind";
		column = oldResultTable.getColumnByName(colName);
		indexColumn = new SQLColumn(null, colName, Types.VARCHAR, 1, 0);
		if ( !compareColumnNameAndType(indexColumn,column,colName)) {
			return false;
		}
		return true;
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
	public Match duplicate(MatchMakerObject parent,MatchMakerSession s) {
		Match newMatch = new Match();
		newMatch.setParent(getParent());
		newMatch.setName(getName());
		newMatch.setFilter(getFilter());
		newMatch.setMergeSettings(getMergeSettings().duplicate(newMatch,s));
		newMatch.setMatchSettings(getMatchSettings().duplicate(newMatch,s));
		newMatch.setSourceTable(getSourceTable());
		newMatch.setResultTable(getResultTable());
		newMatch.setXrefTable(getXrefTable());
		newMatch.setType(getType());
		newMatch.setView(getView()==null?null:getView().duplicate());
		newMatch.setSession(s);
		
		for (MatchMakerCriteriaGroup g : getMatchCriteriaGroups()) {
			MatchMakerCriteriaGroup newGroup = g.duplicate(newMatch,s);
			newMatch.addMatchCriteriaGroup(newGroup);
		}
	
		return newMatch;
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