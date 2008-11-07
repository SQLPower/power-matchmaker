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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.sql.SQL;

public class SourceTableRecord {
    
    private static final Logger logger = Logger.getLogger(SourceTableRecord.class);
    
    /**
     * The session this record exists in.
     */
    private final MatchMakerSession session;
    
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
    
    /**
     * All of the PotentialMatchRecords that reference this source table record.
     */
    private final Set<PotentialMatchRecord> potentialMatches =
        new HashSet<PotentialMatchRecord>();

    /**
     * The match pool that this source table record belongs to.  The pool
     * should set up this reference when this item is added to it.
     */
    private MatchPool pool;
    
    public List<Object> getKeyValues() {
        return keyValues;
    }
    
    /**
     * Creates a new SourceTableRecord instance in the given MatchMakerSession
     * for the given Project and source table key values.
     * 
     * @param session The MatchMakerSession of the given Project
     * @param project The Project this record is attached to
     * @param displayValues The values used to display this record in the UI
     * @param keyValues The values of the unique index on the project's source
     * table.  These values must be specified in the same order as the project's
     * sourceTableIndex columns. Not allowed to be null.
     */
    public SourceTableRecord(
            final MatchMakerSession session,
            final Project project,
            List<Object> displayValues,
            List<Object> keyValues) {
        super();
        this.session = session;
        this.project = project;
        this.displayValues = displayValues;
        this.keyValues = Collections.unmodifiableList(new ArrayList<Object>(keyValues));
        this.computedHashCode = this.keyValues.hashCode();
    }

    /**
     * Works exactly like {@link #SourceTableRecord(MatchMakerSession, Project, List, List)}
     * but takes key values as a variable length argument list.  Mostly useful in setting up test
     * cases.
     */
    public SourceTableRecord(
            final MatchMakerSession session,
            final Project project,
            Object ... keyValues) {
    	this(session, project, new ArrayList<Object>(),Arrays.asList(keyValues));
    }

    /**
     * Works exactly like {@link #SourceTableRecord(MatchMakerSession, Project, List, List)}
     * except the displayValues is initialized as an empty ArrayList.
     */
    public SourceTableRecord(
            final MatchMakerSession session,
            final Project project,
            List<Object> keyValues) {
    	this(session, project, new ArrayList<Object>(),keyValues);
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
     * @throws SQLException, ArchitectException
     */
    public List<Object> fetchValues(List<SQLColumn> shownColumns) throws ArchitectException, SQLException {
        
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
            session.handleWarning(
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
     * @throws ArchitectException, SQLException 
     */
    public List<Object> fetchValues() throws ArchitectException, SQLException {
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
    
    public void addPotentialMatch(PotentialMatchRecord pmr){
        potentialMatches.add(pmr);
    }
    
    public boolean removePotentialMatch(PotentialMatchRecord pmr){
        return potentialMatches.remove(pmr);
    }
    
    /**
     * Returns a list of all the PotentialMatchRecords that were originally associtated
     * with this source table record by the match engine.  The original associations are
     * directionless; the user assigns directions during the match validation process.
     *  
     * @return the list of all PotentialMatchRecords that were originally associated with
     * this database record.
     */
    public Collection<PotentialMatchRecord> getOriginalMatchEdges(){
        return Collections.unmodifiableCollection(potentialMatches);
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
    public PotentialMatchRecord getMatchRecordByOriginalAdjacentSourceTableRecord(SourceTableRecord adjacent) {
        for (PotentialMatchRecord pmr : potentialMatches) {
            if (pmr.getOriginalLhs() == adjacent || pmr.getOriginalRhs() == adjacent) {
                return pmr;
            }
        }
        return null;
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
    public PotentialMatchRecord getMatchRecordByValidatedSourceTableRecord(SourceTableRecord adjacent) {
        for (PotentialMatchRecord pmr : potentialMatches) {
            if (pmr.getMaster() == adjacent || pmr.getDuplicate() == adjacent) {
                return pmr;
            }
        }
        return null;
    }
    
    /**
     * Returns the pool that this source table record belongs to.
     */
    public MatchPool getPool() {
		return pool;
	}
    
    /**
     * Changes which pool this source table thinks it belongs to.  Normally,
     * only the pool itself should call this method.
     */
    public void setPool(MatchPool pool) {
		this.pool = pool;
	}
    
    @Override
    public String toString() {
        return "SourceTableRecord@"+System.identityHashCode(this)+" key="+keyValues;
    }

	public List<Object> getDisplayValues() {
		return displayValues;
	}

	public void setDisplayValues(List<Object> displayValues) {
		this.displayValues = displayValues;
	}
}
