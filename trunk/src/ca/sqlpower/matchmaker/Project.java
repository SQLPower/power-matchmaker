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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.diff.CompareSQL;
import ca.sqlpower.architect.diff.DiffChunk;
import ca.sqlpower.architect.diff.DiffType;
import ca.sqlpower.matchmaker.address.AddressCorrectionEngine;
import ca.sqlpower.matchmaker.address.AddressPool;
import ca.sqlpower.matchmaker.address.AddressCorrectionEngine.AddressCorrectionEngineMode;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.util.ViewSpec;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLIndex.AscendDescend;
import ca.sqlpower.sqlobject.SQLIndex.Column;
import ca.sqlpower.util.Monitorable;

/**
 * folder is the parent of project. should be not null.
 */
public class Project extends AbstractMatchMakerObject<Project, MatchMakerFolder> {

    static final Logger logger = Logger.getLogger(Project.class);
    
	public enum ProjectMode {
		FIND_DUPES("Find Duplicates"), 
		BUILD_XREF("Build Cross-Reference"), 
		CLEANSE("Cleanse"),
		ADDRESS_CORRECTION("Address Correction");

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

	/**
	 * The settings for the munging engine. This applies to Match, Cleanse, and
	 * Address Correction Projects
	 */
    private MungeSettings mungeSettings = new MungeSettings();

	/** the settings for the merge engine */
    private MergeSettings mergeSettings = new MergeSettings();

	/** Optional SQL WHERE clause for the source table. If no filer is desired, this value is null. */
    private String filter;

	/** an optional source for the match created from a view */
    private ViewSpec view;

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
     * Container for the TableMergeRules 
     * We have these folders so that we don't have to deal with multiple child types
     */ 
    private MatchMakerFolder<TableMergeRules> tableMergeRulesFolder =
    	new MatchMakerFolder<TableMergeRules>(TableMergeRules.class);
    
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

    /**
     * The Merging engine this will be created lazyily, because we only need one instance per project.
     */
    private MatchEngineImpl matchingEngine = null;

    /**
     * The Matching engine this will be created lazyily, because we only need one instance per project.
     */
	private MergeEngineImpl mergingEngine = null;

	/**
	 * The Address Correction Engine will be created lazily, because we only need one instance per project.
	 */
	private AddressCorrectionEngine addressCorrectionEngine = null; 
	
	/**
	 * An {@link AddressCorrectionEngine} particularly set up to not
	 * auto-correct addresses, but to load user-corrected addresses from a
	 * result table and commit them into the source table.
	 */
	private AddressCorrectionEngine addressCommittingEngine = null;
	
    /**
     * The process that holds the engine lock. If no process has an engine lock
     * on this project, the reference will be null. The Monitorable instance
     * itself should provide real information about its progress so that others
     * who are waiting for this lock can see how much longer it will be.
     */
    private final AtomicReference<Monitorable> runningEngine = new AtomicReference<Monitorable>();
    
	public Project() {
	    sourceTablePropertiesDelegate = new CachableTable(this, "sourceTable");
	    resultTablePropertiesDelegate = new CachableTable(this,"resultTable");
	    xrefTablePropertiesDelegate = new CachableTable(this, "xrefTable");
		mungeProcessesFolder.setName(MUNGE_PROCESSES_FOLDER_NAME);
        this.addChild(mungeProcessesFolder);
		tableMergeRulesFolder.setName(MERGE_RULES_FOLDER_NAME);
        this.addChild(tableMergeRulesFolder);
        
        setType(ProjectMode.FIND_DUPES);
        sourceTableIndex = new TableIndex(this,sourceTablePropertiesDelegate,"sourceTableIndex");
	}
	
    /**
     * Returns true if the current resultTable of this project exists
     * in the session's database; false otherwise.
     * @throws SQLObjectException If there are problems accessing the session's database
     */
	public boolean doesResultTableExist() throws SQLObjectException {
		MatchMakerSession session = getSession();
		if (session == null) {
			throw new IllegalStateException("Session has not been setup " +
					"for the project, you will need session to check the result table");
		}
		return session.tableExists(getResultTableSPDatasource(),
				getResultTableCatalog(), getResultTableSchema(),
				getResultTableName());
	}

	/**
	 * Returns true if the source table of this project exists in the session's
	 * database; false otherwise.
	 */
	public boolean doesSourceTableExist() throws SQLObjectException {
		MatchMakerSession session = getSession();
		if (session == null) {
			throw new IllegalStateException("Session has not been setup " +
					"for the project, you will need session to check the source table");
		}
		return session.tableExists(getSourceTableSPDatasource(),
				getSourceTableCatalog(), getSourceTableSchema(),
				getSourceTableName());
	}

	

	/**
	 * Creates the result table for this Project based on the properties of the
	 * current source table. The result table name will be the current setting
	 * for resultTableName. The SQLTable itself will be added into its correct
	 * location in the correct SQLDatabase child object.
	 * <p>
	 * This method only sets up an in-memory SQLTable. You still have to do the
	 * physical creation operation in the database yourself.
	 * 
	 * @throws IllegalStateException
	 *             If the current result table catalog, schema, and name are not
	 *             set up properly to correspond with the session's database.
	 *             <p>
	 *             <b>or</b>
	 *             <p>
	 *             If the source table property of this project is not set yet.
	 * @throws SQLObjectException
	 *             If there is trouble working with the source table.
	 */
	public SQLTable createResultTable() throws SQLObjectException {
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
		
		SQLTable t;
		
		if (type == ProjectMode.FIND_DUPES){
			t = buildDedupeResultTable(oldResultTable, si);
		} else if (getType() == ProjectMode.ADDRESS_CORRECTION) {
			t = AddressPool.buildAddressCorrectionResultTable(oldResultTable, si);
		} else {
			throw new IllegalStateException("Building result table on a project type that does not use result tables! " +
					"Project Name: " + this.getName() + " Project Type: " + this.getType());
		}
        
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
	 * @throws SQLObjectException
	 */
	public SQLTable buildDedupeResultTable(SQLTable resultTable, SQLIndex si) 
		throws SQLObjectException {
		
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

		SQLIndex newidx = new SQLIndex(t.getName()+"_uniq", true, null, null, null);
		for (int i = 0; i < si.getChildCount() * 2; i++) {
			newidx.addChild(newidx.new Column(t.getColumn(i), AscendDescend.ASCENDING));
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
	 * @throws SQLObjectException
	 */
	private void addResultTableColumns(SQLTable t, SQLIndex si, String baseName) throws SQLObjectException {
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
	public boolean verifyResultTableStructure() throws SQLObjectException {

		MatchMakerSession session = getSession();
		if (session == null) {
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
		if (type == ProjectMode.FIND_DUPES) {
			inMemory.add(buildDedupeResultTable(resultTable, si));
		} else if (type == ProjectMode.ADDRESS_CORRECTION){
			inMemory.add(AddressPool.buildAddressCorrectionResultTable(resultTable, si));
		} else {
			throw new IllegalStateException("Checking result table on a project type that does not use result tables! " +
					"Project Name: " + this.getName() + " Project Type: " + this.getType());
		}
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
	 * Verify the source table structure and existence in the SQL Database by
	 * comparing the in-memory to the physical.
	 * 
	 * @return false if the table does not exist in the sql database, or the
	 *         table structure does not match original table structure.
	 * 
	 * @throws IllegalStateException
	 *             If the source table has not been setup session and sql
	 *             database have not been setup for the match
	 */
	public boolean verifySourceTableStructure() throws SQLObjectException {
		MatchMakerSession session = getSession();
		if (session == null) {
			throw new IllegalStateException("Session has not been setup " +
					"for the project, you will need session and database " +
					"connection to check the result table");
		}
		SQLDatabase db = session.getDatabase();
		if (db == null) {
			throw new IllegalStateException("Database has not been setup " +
					"for the project session, you will need database " +
					"connection to check the result table");
		}
		SQLTable sourceTable = getSourceTable();
		if (sourceTable == null) {
			throw new IllegalStateException("No source table specified " +
					"for the project, I don't know how to vertify the stucture.");
		}

		SQLTable table = session.findPhysicalTableByName(sourceTable.getParentDatabase().getDataSource().getName(),
				sourceTable.getCatalogName(),
				sourceTable.getSchemaName(),
				sourceTable.getName());
		if (table == null) {
			throw new IllegalStateException(
					"The source table does not exist in the SQL Database");
		}

		List<SQLTable> inMemory = new ArrayList<SQLTable>();
		inMemory.add(sourceTable);
		List<SQLTable> physical = new ArrayList<SQLTable>();
		physical.add(table);
		CompareSQL compare = new CompareSQL(inMemory,physical);
		List<DiffChunk<SQLObject>> tableDiffs = compare.generateTableDiffs();
		logger.debug("Table differences are:");
		int diffCount = 0;
		for ( DiffChunk<SQLObject> diff : tableDiffs) {
			logger.debug(diff.toString());
			if (diff.getType() != DiffType.SAME) {
				diffCount++;
			}
		}
		
		return diffCount == 0;
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
		if (type == ProjectMode.CLEANSE || type == ProjectMode.ADDRESS_CORRECTION) {
			getTableMergeRulesFolder().setVisible(false);
		}
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
	
	/**
	 * Returns a list of munge processes defined by {@link MungeProcess#isValidate()}
	 */
	public List<MungeProcess> getValidatingMungeProcesses(){
		List<MungeProcess> validatingProcesses = new ArrayList<MungeProcess>();
		for (MungeProcess mp : getMungeProcesses()) {
			if (mp.isValidate()) {
				validatingProcesses.add(mp);
			}
		}
		return validatingProcesses;
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
     * Add a TableMergeRule rule to the TableMergeRules folder of this Project
     */
    public void addTableMergeRule(TableMergeRules rule) {
        // The folder will fire the child inserted event
        tableMergeRulesFolder.addChild(rule);
    }

    /**
     * Removes the TableMergeRule rule from the TableMergeRules folder of this project
     */
    public void removeTableMergeRule(TableMergeRules rule) {
        // The folder will fire the child removed event
    	tableMergeRulesFolder.removeChild(rule);
    }

    public List<TableMergeRules> getTableMergeRules(){
        return tableMergeRulesFolder.getChildren();
    }

    /**
     *  Allow bulk replacement of all table merge rules for this project
     *  This should only be used by the DAOs. Assumes that you never
     *  pass in a null list 
     */
    public void setTableMergeRules(List<TableMergeRules> rules){
    	tableMergeRulesFolder.setChildren(rules);
    }

    public MatchMakerFolder<TableMergeRules> getTableMergeRulesFolder() {
        return tableMergeRulesFolder;
    }
    
    /**
     * Adds a munge process to the munge process folder of this project
     *
     * @param mungeProcess
     */
    public void addMungeProcess(MungeProcess mungeProcess) {
        // The folder will fire the child inserted event
        mungeProcessesFolder.addChild(mungeProcess);
        mungeProcess.setMatchPriority(mungeProcessesFolder.getChildren().indexOf(mungeProcess));
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

		for (TableMergeRules g : getTableMergeRules()) {
			TableMergeRules newMergeRule = g.duplicate(newProject.getTableMergeRulesFolder(),s);
			newProject.addTableMergeRule(newMergeRule);
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
	public SQLIndex getSourceTableIndex() throws SQLObjectException {
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
	 * Gets the cleansing engine implementation. 
	 * This is done to ensure that only one implementation is created per project.
	 * This creates a new one if there isn't one already.
	 * 
	 * @return The engine implementation.
	 */
	public CleanseEngineImpl getCleansingEngine() {
		if (cleansingEngine == null) {
			cleansingEngine = new CleanseEngineImpl(getSession(), this); 
		}
		return cleansingEngine;
	}
	
	/**
	 * Gets the matching engine implementation. 
	 * This is done to ensure that only one implementation is created per project.
	 * This creates a new one if there isn't one already.
	 * 
	 * @return The engine implementation.
	 */
	public MatchEngineImpl getMatchingEngine() {
		if (matchingEngine == null) {
			matchingEngine = new MatchEngineImpl(getSession(), this); 
		}
		return matchingEngine;
	}
	
	/**
	 * Gets the merging engine implementation. 
	 * This is done to ensure that only one implementation is created per project.
	 * This creates a new one if there isn't one already.
	 * 
	 * @return The engine implementation.
	 */
	public MergeEngineImpl getMergingEngine() {
		if (mergingEngine == null) {
			mergingEngine = new MergeEngineImpl(getSession(), this); 
		}
		return mergingEngine;
	}
	
	/**
	 * Returns the contained {@link AddressCorrectionEngine}.
	 * If there isn't one, one will get created. This helps to ensure
	 * only one instance is created per project. 
	 * 
	 * @return The Address Correction Engine contained
	 */
	public AddressCorrectionEngine getAddressCorrectionEngine() {
		if (addressCorrectionEngine == null) {
			addressCorrectionEngine = new AddressCorrectionEngine(getSession(), this, AddressCorrectionEngineMode.ADDRESS_CORRECTION_PARSE_AND_CORRECT_ADDRESSES);
		}
		return addressCorrectionEngine;
	}
	
	public AddressCorrectionEngine getAddressCommittingEngine() {
		if (addressCommittingEngine == null) {
			addressCommittingEngine = new AddressCorrectionEngine(getSession(), this, AddressCorrectionEngineMode.ADDRESS_CORRECTION_WRITE_BACK_ADDRESSES);
		}
		return addressCommittingEngine;
	}
	
	/**
	 * Returns the connection associated with the source table. If the sourceTable
	 * is null, then returns null;
	 * @throws SQLException 
	 */
	public Connection createSourceTableConnection() throws SQLException {
		if (sourceTablePropertiesDelegate.getSourceTable() != null) {
			return sourceTablePropertiesDelegate.getSourceTable().getParentDatabase().getDataSource().createConnection();
		} 
		return null;
	}
	
	/**
	 * Returns the connection associated with the result table. If the result table
	 * is null, then returns null;
	 * @throws SQLException 
	 */
	public Connection createResultTableConnection() throws SQLException {
		if (resultTablePropertiesDelegate.getSourceTable() != null) {
			return resultTablePropertiesDelegate.getSourceTable().getParentDatabase().getDataSource().createConnection();
		} 
		return null;
	}

    /**
     * Acquires the engine lock when it becomes available. If the lock is not
     * currently available, this method will block until the lock is acquired.
     * The monitor passed in must be the same monitor that will later be used to
     * release the lock. The monitor should be able to be used to track the
     * progress of the engine that has acquired the lock. All engines (match,
     * merge, cleanse and even the auto-matcher) that operate on the match pool
     * must acquire this lock before manipulating the pool in any way.
     * <p>
     * To acquire the lock, use code like this:
     * 
     * <pre>
     *   try {
     *      project.acquireEngineLock();
     *      
     *      (do various pooley things)
     *      
     *   } finally {
     *      project.releaseEngineLock();
     *   }
     * </pre>
     * <p>
     * Note that this lock is local to this JVM. In the future, we hope to
     * expand the scope of the lock to cover all users of this project in the
     * session's repository database.
     * 
     * @throws InterruptedException
     */
	public void acquireEngineLock(Monitorable monitor) throws InterruptedException {
	    boolean acquired = false;
	    while (!acquired) {
	        acquired = runningEngine.compareAndSet(null, monitor);
	        synchronized (runningEngine) {
	            if (!acquired) {
	                runningEngine.wait();
	            }
            }
	    }
	}
	
	/**
	 * Releases the engine lock if the monitor passed in is the same monitor that
	 * was used to acquire the lock. Returns true if the lock is successfully released,
	 * returns false otherwise.
	 */
	public void releaseEngineLock(Monitorable monitor) {
	    if (!runningEngine.compareAndSet(monitor, null)) {
	        throw new IllegalMonitorStateException("Can't release lock because you don't own it");
	    }
	    synchronized (runningEngine) {
	        runningEngine.notify();
        }
	}

    /**
     * A reference to monitor the progress of the engine that is currently
     * running in this project. Only one engine may run at any given time in a
     * project.
     * <p>
     * In the future (when we have time), this API is likely to change so that you
     * would have to acquire a shared or "read only" lock instead of polling for
     * the currently-running engine. This will let us properly support multi-user
     * (on different client machines) better. For now, this API assumes that nothing
     * will try to acquire the engine lock except some explicit user action in the
     * local JVM.
     * 
     * @return A way of monitoring the progress of something that holds the engine
     * lock on this project; null if the engine lock is free.
     */
	public Monitorable getRunningEngine() {
	    return runningEngine.get();
	}
}