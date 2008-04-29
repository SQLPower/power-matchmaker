/*
 * Copyright (c) 2007, SQL Power Group Inc.
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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLIndex.Column;
import ca.sqlpower.architect.SQLIndex.IndexType;
import ca.sqlpower.architect.diff.CompareSQL;
import ca.sqlpower.architect.diff.DiffChunk;
import ca.sqlpower.architect.diff.DiffType;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.util.ViewSpec;

/**
 * folder is the parent of project. should be not null.
 */
public class Project extends AbstractMatchMakerObject<Project, MatchMakerFolder> {

    static final Logger logger = Logger.getLogger(Project.class);
    
	public enum ProjectMode {
		FIND_DUPES("Find Duplicates"), BUILD_XREF("Build Cross-Reference"), CLEANSE("Cleanse");

		String displayName;

		private ProjectMode(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String toString() {
			return displayName;
		}

		/**
		 * Get the ProjectType that corresponds to the type string.
		 *
		 * in the old version export, we have type like 'FIND DUPLICATES', etc...
		 * @param type a string detailing the type you want to get
		 * @return the project type that has type as its toString
		 */
		public static ProjectMode getTypeByString(String type){
			ProjectMode[] types = ProjectMode.values();

			for (ProjectMode projectType: types) {
				if (projectType.toString().toLowerCase().equals(type.toLowerCase())) {
					return projectType;
				} else if ("FIND DUPLICATES".toLowerCase().equals(type.toLowerCase())) {
					return FIND_DUPES;
				}


			}
			throw new IllegalArgumentException("There is no project type with a string "+type);
		}
	}

	/** the oid for the project */
	private Long oid;

	/** The type of project */
    private ProjectMode type;

	/** The settings for the match engine */
    private MungeSettings mungeSettings = new MungeSettings();

	/** the settings for the merge engine */
    private MergeSettings mergeSettings = new MergeSettings();

	/** a filter for the tables that are matched */
    private String filter;

	/** an optional source for the match created from a view */
    private ViewSpec view;

    private String description;
    
    /** Folder name for merge rules (table). */
    public static final String MERGE_RULES_FOLDER_NAME = "Merge Rules";
    
    /** Folder name for munge processes. */
    public static final String MUNGE_PROCESSES_FOLDER_NAME = "Munge Processes";
    
    /**
     * Contains the Munge Processes and the Munge Steps
     */
    private MatchMakerFolder<MungeProcess> mungeProcessesFolder =
    	new MatchMakerFolder<MungeProcess>(MungeProcess.class);
    
    /**
     * Cached source table 
     */
    private final CachableTable sourceTablePropertiesDelegate;
    /**
     * Cached result table 
     */
    private final CachableTable resultTablePropertiesDelegate;
    /**
     * Cached xref table 
     */
    private final CachableTable xrefTablePropertiesDelegate;

    /**
	 * The unique index of the source table that we're using. Not necessarily
	 * one of the unique indices defined in the database; the user can pick an
	 * arbitrary set of columns.
	 */
    private TableIndex sourceTableIndex;
    
    /**
     * The Cleansing engine this will be created lazyily, because we only need one instance per project.
     */
    private CleanseEngineImpl cleansingEngine = null;

	public Project() {
	    sourceTablePropertiesDelegate = new CachableTable(this, "sourceTable");
	    resultTablePropertiesDelegate = new CachableTable(this,"resultTable");
	    xrefTablePropertiesDelegate = new CachableTable(this, "xrefTable");
		mungeProcessesFolder.setName(MUNGE_PROCESSES_FOLDER_NAME);
        this.addChild(mungeProcessesFolder);
        
        setType(ProjectMode.FIND_DUPES);
        sourceTableIndex = new TableIndex(this,sourceTablePropertiesDelegate,"sourceTableIndex");
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
     * Returns true if the current resultTable of this project exists
     * in the session's database; false otherwise.
     * @throws ArchitectException If there are problems accessing the session's database
     */
	public static boolean doesResultTableExist(MatchMakerSession session, Project project) throws ArchitectException {
		return session.tableExists(
							project.getResultTableSPDatasource(),
							project.getResultTableCatalog(),
							project.getResultTableSchema(),
							project.getResultTableName());
	}

	/**
	 * Returns true if the source table of this project exists in the
	 * session's database; false otherwise.
	 */
	public static boolean doesSourceTableExist(MatchMakerSession session, Project project) throws ArchitectException {
		return session.tableExists(
				project.getSourceTableSPDatasource(),
				project.getSourceTableCatalog(),
				project.getSourceTableSchema(),
				project.getSourceTableName());
	}

	

	/**
	 * Creates the result table for this Project based on the properties
	 * of the current source table. The result table name will be the
	 * current setting for resultTableName.  The SQLTable itself will
     * be added into its correct location in the correct SQLDatabase
     * child object. 
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
	 * If the source table property of this project is not set yet.
	 * @throws ArchitectException If there is trouble working with the
	 * source table.
	 */
	public SQLTable createResultTable() throws ArchitectException {
		SQLIndex si = getSourceTableIndex();

		if (si == null) {
			throw new IllegalStateException(
					"You have to set up the source table of a project " +
					"before you can create its result table!");
		}
		SQLTable oldResultTable = getResultTable();
		if (oldResultTable == null) {
			throw new IllegalStateException(
					"You have to properly specify the result table " +
					"catalog, schema, and name before calling " +
					"createResultTable()");
		}

		logger.debug("createResultTable: table parent=" +
				(oldResultTable.getParent()==null?"null":oldResultTable.getParent().getClass()) +
				"  name:[" +
				(oldResultTable.getParent()==null?"null":oldResultTable.getParent().getName()) +
				"]");
		logger.debug("createResultTable: si="+si+" si.children.size="+si.getChildCount());
		
		SQLTable t = buildResultTable(oldResultTable, si);
        
        // Now replace the in-memory cached version of the result table
        SQLObject resultTableParent = oldResultTable.getParent();
        resultTableParent.removeChild(oldResultTable);
        resultTableParent.addChild(t);
        
		setResultTable(t);

		return t;
	}
	
	/**
	 * This builds the result table according to given setup information. 
	 *  
	 * @param resultTable This contains the setup information for the result table to be generated.
	 * @param si The unique index upon which the result table should reflect on
	 * @throws ArchitectException
	 */
	public SQLTable buildResultTable(SQLTable resultTable, SQLIndex si) 
		throws ArchitectException {
		
		SQLTable t = new SQLTable(resultTable.getParent(), resultTable.getName(), resultTable.getRemarks(), "TABLE", true);

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
	 * @throws ArchitectException
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
	 * Verify the result table structure and existence in the SQL Database,
	 * the result table should looks like this:
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
	 * to call {@link MatchMakerSession.isThisSQLTableExists()} to verify
	 * the result table existence
	 * <p> 
	 * @return false if the table is not exist in the sql database, or
	 * the table structure does not match above table structure.
	 * 
	 * @throws IllegalStateException If the source table has not been setup 
	 * <p>
	 * <b>or</b>
	 * <p>unique index has not been setup 
	 * <p>
	 * <b>or</b>
	 * <p>session and sql database have not been setup for the match
	 */
	public boolean verifyResultTableStruct() throws ArchitectException {

		MatchMakerSession session = getSession();
		if ( session == null ) {
			throw new IllegalStateException("Session has not been setup " +
					"for the project, you will need session and database " +
					"connection to check the result table");
		}
		SQLDatabase db = session.getDatabase();
		if ( db == null ) {
			throw new IllegalStateException("Database has not been setup " +
					"for the project session, you will need database " +
					"connection to check the result table");
		}
		SQLIndex si = getSourceTableIndex();
		if (si == null) {
			throw new IllegalStateException("No unique index specified " +
					"for the project, I don't know how to vertify the " +
					"result table stucture.");
		}
		SQLTable sourceTable = getSourceTable();
		if ( sourceTable == null) {
			throw new IllegalStateException("No source table specified " +
					"for the project, I don't know how to vertify the " +
					"result table stucture.");
		}

		SQLTable resultTable = getResultTable();
		if (resultTable == null) {
			throw new IllegalStateException(
					"You have to properly specify the result table " +
					"catalog, schema, and name before calling " +
					"vertifyResultTableStruct()");
		}

		SQLTable table = session.findPhysicalTableByName(resultTable.getParentDatabase().getDataSource().getName(),
				resultTable.getCatalogName(),
				resultTable.getSchemaName(),
				resultTable.getName());
		
		if (table == null) {
			throw new IllegalStateException(
					"The result table does not exist in the SQL Database");
		}

		List<SQLTable> inMemory = new ArrayList<SQLTable>();
		inMemory.add(buildResultTable(resultTable, si));
		List<SQLTable> physical = new ArrayList<SQLTable>();
		physical.add(table);
		CompareSQL compare = new CompareSQL(inMemory,physical);
		List<DiffChunk<SQLObject>> tableDiffs = compare.generateTableDiffs();
		logger.debug("Table differences are:");
		int diffCount = 0;
		for ( DiffChunk<SQLObject> diff : tableDiffs) {
			logger.debug(diff.toString());
			/** we have not made the sql Comparator smart enough to handle 
			 * some difference like oracle Date = Date(7) etc. so we can
			 * not count the type=modified. (different type,precision,scale)
			 */
			if ( diff.getType() != DiffType.SAME &&
					diff.getType() != DiffType.MODIFIED) {
				diffCount++;
			}
		}
		
		return diffCount == 0;
	}
	
	
	/**
	 * FIXME Implement me
	 *
	 */
	public void createViewTable() {
		throw new NotImplementedException();
	}

	public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		String oldValue = this.filter;
		this.filter = filter;
		getEventSupport().firePropertyChange("filter", oldValue, this.filter);
	}

	public MungeSettings getMungeSettings() {
		return mungeSettings;
	}

	public void setMungeSettings(MungeSettings mungeSettings) {
		if (mungeSettings==null) throw new NullPointerException("You should not try to set the settings to null");
		MungeSettings oldValue = this.mungeSettings;
		this.mungeSettings = mungeSettings;
		getEventSupport().firePropertyChange("mungeSettings", oldValue,
				this.mungeSettings);
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




	public ProjectMode getType() {
		return type;
	}

	public void setType(ProjectMode type) {
		ProjectMode oldValue = this.type;
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


	public List<MungeProcess> getMungeProcesses() {
		return getMungeProcessesFolder().getChildren();
	}

	public MungeProcess getMungeProcessByName(String name) {
		List <MungeProcess> mungeProcesses = getMungeProcesses();
		for ( MungeProcess g : mungeProcesses) {
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
		if ( !(obj instanceof Project) ) {
			return false;
		}
        if (this == obj) {
            return true;
        }
        final Project other = (Project) obj;
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
        sb.append("Project@").append(System.identityHashCode(this));
        sb.append(": oid=").append(oid);
        sb.append("; type=").append(type);
        
        // Important: don't call get*Table() here because they have side effects
        sb.append("; sourceTableName=").append(getSourceTableName());
        sb.append("; resultTableName=").append(getResultTableName());
        sb.append("; xrefTableName=").append(getXrefTableName());
        
        sb.append("; mungeSettings=").append(mungeSettings);
        sb.append("; mergeSettings=").append(mergeSettings);
        sb.append("; filter=").append(filter);
        sb.append("; view=").append(view);
        return sb.toString();
    }
    
    /**
     * Adds a munge process to the munge process folder of this project
     *
     * @param mungeProcess
     */
    public void addMungeProcess(MungeProcess mungeProcess) {
        // The folder will fire the child inserted event
        mungeProcessesFolder.addChild(mungeProcess);
    }

    /**
     * Removes the munge process from the rule set folder of this process
     *
     * @param process
     */
    public void removeMungeProcess(MungeProcess process) {
        // The folder will fire the child removed event
        mungeProcessesFolder.removeChild(process);
    }

    public void setMungeProcesses(List<MungeProcess> processes){
        mungeProcessesFolder.setChildren(processes);
    }

    public MatchMakerFolder<MungeProcess> getMungeProcessesFolder() {
        return mungeProcessesFolder;
    }

    /**
     * duplicate the project object. by inserting a new set of record 
     * to the matchmaker tables.
     * objects under different id and oid 
     * @return true if nothing wrong.
     */
	public Project duplicate(MatchMakerObject parent,MatchMakerSession s) {
		Project newProject = new Project();
		newProject.setParent(getParent());
		newProject.setName(getName());
		newProject.setFilter(getFilter());
		newProject.setMergeSettings(getMergeSettings().duplicate(newProject,s));
		newProject.setMungeSettings(getMungeSettings().duplicate(newProject,s));
		
		newProject.setSourceTable(getSourceTable());
		newProject.setResultTable(getResultTable());
		
		newProject.setXrefTable(getXrefTable());
		newProject.setType(getType());
		newProject.setView(getView()==null?null:getView().duplicate());
		newProject.setSession(s);
		newProject.setVisible(isVisible());
		
		for (MungeProcess g : getMungeProcesses()) {
			MungeProcess newGroup = g.duplicate(newProject.getMungeProcessesFolder(),s);
			newProject.addMungeProcess(newGroup);
		}
		
		return newProject;
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
    
    public void setSourceTableSPDatasource(String sourceTableSPDName) {
    	sourceTablePropertiesDelegate.setSPDataSource(sourceTableSPDName);
    }
    
    public String getSourceTableSPDatasource() {
    	return sourceTablePropertiesDelegate.getSPDataSourceName();
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

    public void setResultTableSPDatasource(String resultTableSPDName) {
    	resultTablePropertiesDelegate.setSPDataSource(resultTableSPDName);
    }
    
    public String getResultTableSPDatasource() {
    	return resultTablePropertiesDelegate.getSPDataSourceName();
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
    
    public void setXrefTableSPDatasource(String xrefTableSPDName) {
    	xrefTablePropertiesDelegate.setSPDataSource(xrefTableSPDName);
    }
    
    public String getXrefTableSPDatasource() {
    	return xrefTablePropertiesDelegate.getSPDataSourceName();
    }
    
    /**
     * Returns a SQLIndex object which is the set of columns the user
     * wishes the MatchMaker to use for uniquely determining a row.
     * There is no specific check that this set of columns does in fact
     * uniquely identify a row, but the MatchMaker is not required to
     * function properly if this set of columns doesn't have the uniqueness
     * property.
     */
	public SQLIndex getSourceTableIndex() throws ArchitectException {
		return sourceTableIndex.getTableIndex();
	}
	
    /**
     * Sets the SQLIndex object which is the set of columns the user
     * wishes the MatchMaker to use for uniquely determining a row.
     * There is no specific check that this set of columns does in fact
     * uniquely identify a row, but the MatchMaker is not required to
     * function properly if this set of columns doesn't have the uniqueness
     * property.
     * <p>
     * When this value changes, the existing MatchPool and result table
     * will have to be wiped out and re-created from scratch.  They both
     * depend on the chosen set of columns that uniquely identify a row.
     */
	public void setSourceTableIndex(SQLIndex index) {
		sourceTableIndex.setTableIndex(index);
	}
	
	/**
	 * Gets the cleansing engine editor panel. This is done to ensure that only one panel is created per project.
	 * 
	 * @return The editor panel.
	 */
	public CleanseEngineImpl getCleansingEngine() {
		if (cleansingEngine == null) {
			cleansingEngine = new CleanseEngineImpl(getSession(), this); 
		}
		return cleansingEngine;
	}
	
	/**
	 * Returns the connection associated with the source table
	 * @throws SQLException 
	 */
	public Connection createSourceTableConnection() throws SQLException {
		if (sourceTablePropertiesDelegate.getSourceTable() != null) {
			return sourceTablePropertiesDelegate.getSourceTable().getParentDatabase().getDataSource().createConnection();
		} 
		return null;
	}
	
	/**
	 * Returns the connection associated with the result table
	 * @throws SQLException 
	 */
	public Connection createResultTableConnection() throws SQLException {
		if (resultTablePropertiesDelegate.getSourceTable() != null) {
			return resultTablePropertiesDelegate.getSourceTable().getParentDatabase().getDataSource().createConnection();
		} 
		return null;
	}
}