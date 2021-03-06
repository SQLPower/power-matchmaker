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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonProperty;
import ca.sqlpower.object.annotation.Transient;
import ca.sqlpower.sql.SQL;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;

public class SourceTableRecord extends AbstractMatchMakerObject {
    
    private static final Logger logger = Logger.getLogger(SourceTableRecord.class);
	
	/**
	 * Defines an absolute ordering of the child types of this class.
	 */
    @SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.emptyList();
    
    /**
     * The Project object this SourceTableRecord belongs to.
     */
    private final Project project;
    
    /**
     * The values of the unique index columns in the same order as the
     * Index Column objects in the source table's index.  This lets us
     * select the entire match source record when we need it.
     * <p>
     * Note, the contents of this list can never be modified.
     */
    private final List<Object> keyValues;
    
    /**
     * The values of the columns that we wish to display for each
     * SourceTableRecord node in the match validation screen graphs.
     */
    private List<Object> displayValues;
    
    /**
     * The computed hash code for this object.  It is based on the unmodifiable
     * keyValues list, and is computed only once.  We determined by profiling
     * that most of the time spent in graph layout was in recomputing this
     * hash code over and over.
     */
    private final int computedHashCode;
    
    @Accessor
    public List<Object> getKeyValues() {
        return keyValues;
    }
    
    @Accessor
    public Project getProject() {
    	return project;
    }
    
    /**
     * Creates a new SourceTableRecord instance in the given MatchMakerSession
     * for the given Project and source table key values.
     * 
     * @param project The Project this record is attached to
     * @param displayValues The values used to display this record in the UI
     * @param keyValues The values of the unique index on the project's source
     * table.  These values must be specified in the same order as the project's
     * sourceTableIndex columns. Not allowed to be null.
     */
    @Constructor
    public SourceTableRecord(
            @ConstructorParameter(propertyName="project") final Project project,
            @ConstructorParameter(propertyName="displayValues") List<Object> displayValues,
            @ConstructorParameter(propertyName="keyValues") List<Object> keyValues) {
        super();
        this.project = project;
        this.displayValues = displayValues;
        this.keyValues = Collections.unmodifiableList(new ArrayList<Object>(keyValues));
        this.computedHashCode = this.keyValues.hashCode();
        setName("sourceTableRecord:" + keyValues);
    }

    /**
     * Works exactly like {@link #SourceTableRecord(Project, List, List)}
     * but takes key values as a variable length argument list.  Mostly useful in setting up test
     * cases.
     */
    public SourceTableRecord(
            final Project project,
            Object ... keyValues) {
    	this(project, new ArrayList<Object>(),Arrays.asList(keyValues));
    }

    /**
     * Works exactly like {@link #SourceTableRecord(Project, List, List)}
     * except the displayValues is initialized as an empty ArrayList.
     */
    public SourceTableRecord(
            final Project project,
            List<Object> keyValues) {
    	this(project, new ArrayList<Object>(),keyValues);
    }
    
    /**
     * Looks up and returns the column values for the given SQLColumns.  
     * If shownColumns is null, then all values will be returned in the 
     * list in the same order as the project's sourceTable's columns are
     * listed in.  Thus, all SourceTableRecords attached to the same 
     * Project will return column values in the same order as each other.
     * SQLException is thrown if the given columns are not within the source 
     * table.
     * 
     * @param The list of column data you would like to retrieve. 
     * @return The values for the row of the source table which is uniquely
     * identified by this sourceTableRecord's keyValues list.
     * @throws SQLException, SQLObjectException
     */
    public List<Object> fetchValues(List<SQLColumn> shownColumns) throws SQLObjectException, SQLException {
        
    	SQLTable sourceTable = project.getSourceTable();
    	// if null, then retrieve all the row's data
    	if (shownColumns == null) {
    		shownColumns = sourceTable.getColumns();
    	} else {
    		// if no columns should be shown, then don't run a query and just return 
        	// an empty list. 
    		if (shownColumns.isEmpty()) {
    			return Collections.emptyList();
    		}
    		// check to make sure the given columns are in the source table
    		for (SQLColumn col : shownColumns) {
    			if (!sourceTable.getColumns().contains(col)) {
    				throw new SQLException("Column " + col.getName() + " is not in table"
    						+ sourceTable.getName() + "!");
    			}
    		}
    	}
    	List<Object> values = new ArrayList<Object>(shownColumns.size());
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        String lastSQL = null;
        try {
            con =project.createResultTableConnection();
            stmt = con.createStatement();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ");
            boolean first = true;
            
            for (SQLColumn col : shownColumns) {
            	if (!first) sql.append(", ");
            	sql.append(col.getName());
            	first = false;
            }
            sql.append("\n FROM ");
            sql.append(DDLUtils.toQualifiedName(sourceTable));
            sql.append("\n WHERE ");
            first = true;
            for (int col = 0; col < keyValues.size(); col++) {
                SQLIndex.Column icol = project.getSourceTableIndex().getChild(col);
                Object ival = keyValues.get(col);
                if (!first) sql.append(" AND ");
                sql.append(icol.getName());
                sql.append("=");
                if (ival == null) {
                    sql.append(" IS NULL");
                } else if (ival instanceof Date) {
                    sql.append(SQL.escapeDateTime(con, (Date) ival));
                } else if (ival instanceof Number) {
                    sql.append(ival.toString());
                } else {
                    sql.append(SQL.quote(ival.toString()));
                }
                first = false;
            }
            
            lastSQL = sql.toString();
            rs = stmt.executeQuery(lastSQL);
            
            if (!rs.next()) {
                throw new SQLException("No data found in source table!");
            }
            for (SQLColumn col : shownColumns) {
                values.add(rs.getObject(col.getName()));
            }
            if (rs.next()) {
                throw new SQLException("More than one row of data found in source table!");
            }
            
            return values;
            
        } catch (SQLException ex) {
            logger.error("Error in query: "+lastSQL, ex);
            getSession().handleWarning(
                    "Error in SQL Query!" +
                    "\nMessage: "+ex.getMessage() +
                    "\nSQL State: "+ex.getSQLState() +
                    "\nQuery: "+lastSQL);
            throw ex;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ex) { logger.error("Couldn't close result set", ex); }
            if (stmt != null) try { stmt.close(); } catch (SQLException ex) { logger.error("Couldn't close statement", ex); }
            if (con != null) try { con.close(); } catch (SQLException ex) { logger.error("Couldn't close connection", ex); }
        }
    }

    /**
     * Looks up and returns the column values for the row this object
     * represents.  The values are returned in the list in the same order
     * as the project's sourceTable's columns are listed in.  Thus, all
     * SourceTableRecords attached to the same Project will return column
     * values in the same order as each other.
     * <p>
     * This is the same as calling {@link #fetchValues(List)} with a null 
     * parameter.
     * 
     * @return The values for the row of the source table which is uniquely
     * identified by this sourceTableRecord's keyValues list.
     * @throws SQLObjectException, SQLException 
     */
    public List<Object> fetchValues() throws SQLObjectException, SQLException {
    	return fetchValues(null);
    }
    
    /**
     * Two source table records are equal if their primary key values are all the 
     * same.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SourceTableRecord)) {
            return false;
        } 
        SourceTableRecord other = (SourceTableRecord) obj;
        return keyValues.equals(other.getKeyValues());
    }

    /**
     * Returns a hash code dependant only on the keyValues list.
     */
    @Override
    public int hashCode() {
        return computedHashCode;
    }
    
    /**
     * Returns a list of all the PotentialMatchRecords that were originally associtated
     * with this source table record by the match engine.  The original associations are
     * directionless; the user assigns directions during the match validation process.
     *  
     * @return the list of all PotentialMatchRecords that were originally associated with
     * this database record.
     */
	@NonProperty
    public List<PotentialMatchRecord> getOriginalMatchEdges() {
		List<PotentialMatchRecord> pmrl = getCluster().getOriginalMatchEdges(this);
		if(pmrl == null) {
			return Collections.emptyList();
		}
		return pmrl;
    }
	
    /**
     * Returns the edge (PotentialMatchRecord) that makes this node (SourceTableRecord)
     * adjacent to the given other node.  For this method, adjacency is defined as
     * original potential matches as discovered by the match engine.
     * 
     * @param adjacent The node that is adjacent to this one that you want to find the
     * common edge for.
     * @return The edge that makes this node adjacent to the given other node.
     */
	@NonProperty
    public PotentialMatchRecord getMatchRecordByOriginalAdjacentSourceTableRecord(SourceTableRecord adjacent) {
    	return getCluster().getMatchRecordByOriginalAdjacentSourceTableRecord(this, adjacent);
    }
    
    /**
     * Searches this source table record's set of potential matches (the
     * incident edges) for the edge that connects it to the given adjacent
     * node, where adjacency is defined as a user-validated master/duplicate
     * relationship.
     * 
     * @param adjacent The other source table record
     * @return The edge that makes this record adjacent to the given record,
     * or null if they are not adjacent by this method's definition of adjacency.
     */
	@NonProperty
    public PotentialMatchRecord getMatchRecordByValidatedSourceTableRecord(SourceTableRecord adjacent) {
    	return getCluster().getMatchRecordByValidatedSourceTableRecord(this, adjacent);
    }
    
    @Override
    public String toString() {
        return "STR " + "key=" + keyValues;
    }

    @Transient
    @Accessor
	public List<Object> getDisplayValues() {
		return displayValues;
	}

	@Transient
	@Mutator
	public void setDisplayValues(List<Object> displayValues) {
		this.displayValues = displayValues;
	}

	@Override
	public MatchMakerObject duplicate(MatchMakerObject parent) {
		return null;
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

	@NonProperty
	public MatchCluster getCluster() {
		return (MatchCluster) getParent();
	}
}
