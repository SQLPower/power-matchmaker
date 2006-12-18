package ca.sqlpower.matchmaker;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
     * The Match object this SourceTableRecord belongs to.
     */
    private final Match match;
    
    /**
     * The values of the unique index columns in the same order as the
     * Index Column objects in the source table's index.  This lets us
     * select the entire match source record when we need it.
     * <p>
     * Note, the contents of this list can never be modified.
     */
    private final List<Object> keyValues;
    
    /**
     * The computed hash code for this object.  It is based on the unmidifiable
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
    
    public List<Object> getKeyValues() {
        return keyValues;
    }
    
    /**
     * Creates a new SourceTableRecord instance in the given MatchMakerSession
     * for the given Match and source table key values.
     * 
     * @param session The MatchMakerSession of the given Match
     * @param match The Match this record is attached to
     * @param keyValues The values of the unique index on the match's source
     * table.  These values must be specified in the same order as the match's
     * sourceTableIndex columns. Not allowed to be null.
     */
    public SourceTableRecord(
            final MatchMakerSession session,
            final Match match,
            List<Object> keyValues) {
        super();
        this.session = session;
        this.match = match;
        this.keyValues = Collections.unmodifiableList(new ArrayList<Object>(keyValues));
        this.computedHashCode = this.keyValues.hashCode();
    }


    /**
     * Looks up and returns the column values for the row this object
     * represents.  The values are returned in the list in the same order
     * as the match's sourceTable's columns are listed in.  Thus, all
     * SourceTableRecords attached to the same Match will return column
     * values in the same order as each other.
     * 
     * @return The values for the row of the source table which is uniquely
     * identified by this sourceTableRecord's keyValues list.
     * @throws ArchitectException, SQLException 
     */
    public List<Object> fetchValues() throws ArchitectException, SQLException {
        SQLTable sourceTable = match.getSourceTable();
        List<Object> values = new ArrayList<Object>(sourceTable.getColumns().size());
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        String lastSQL = null;
        try {
            con = session.getConnection();
            stmt = con.createStatement();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ");
            boolean first = true;
            for (SQLColumn col : sourceTable.getColumns()) {
                if (!first) sql.append(", ");
                sql.append(col.getName());
                first = false;
            }
            sql.append("\n FROM ");
            sql.append(DDLUtils.toQualifiedName(sourceTable));
            sql.append("\n WHERE ");
            first = true;
            for (int col = 0; col < keyValues.size(); col++) {
                SQLIndex.Column icol = match.getSourceTableIndex().getChild(col);
                Object ival = keyValues.get(col);
                if (!first) sql.append(" AND ");
                sql.append(icol.getName());
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
            for (SQLColumn col : sourceTable.getColumns()) {
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
    
    public Collection<PotentialMatchRecord> getPotentialMatches(){
        return potentialMatches;
    }
    
    /**
     * Finds all the PotentialMatchRecord that has this SourceTableRecord recognized as
     * the head node.  
     * IMPORTANT: Note that just because this SourceTableRecord is the head, 
     * it does not mean necessary mean that it is the master record in the database.   
     * see {@link PotentialMatchRecord#getMaster()} method comment
     * 
     * @return a list PotentialMatchRecord that has this SourceTableRecord as its head
     */
    public Collection<PotentialMatchRecord> getInboundEdges(){
        Collection<PotentialMatchRecord> inboundEdges = new ArrayList<PotentialMatchRecord>();
        for (PotentialMatchRecord pmr : potentialMatches){
            if (pmr.getMaster() == this){
                inboundEdges.add(pmr);
            }
        }
        return inboundEdges;
    }
    
    /**
     * Finds all the PotentialMatchRecord that has this SourceTableRecord recognized as
     * the tail node.
     * IMPORTANT: Note that just because this SourceTableRecord is the tail, 
     * it does not mean necessary mean that it is the slave record in the database.   
     * see {@link PotentialMatchRecord#getMaster()} method comment 
     *  
     * @return a list PotentialMatchRecord that has this SourceTableRecord as its tail
     */
    public Collection<PotentialMatchRecord> getOutboundEdges(){
        Collection<PotentialMatchRecord> inboundEdges = new ArrayList<PotentialMatchRecord>();
        for (PotentialMatchRecord pmr : potentialMatches){
            if (pmr.getMaster() != this){
                inboundEdges.add(pmr);
            }
        }
        return inboundEdges;
    }
    
    /**
     * Compiles and returns a list of SourceTableRecord that this SourceTableRecord
     * is linked to by both incoming and outgoing edges.  If it is linked to 
     * another SourceTableRecord by both an incoming edge and outgoing edge, it 
     * will only be counted once
     * @return a list of SourceTableRecord that is being linked to by this SourceTableRecord
     */
    public Collection<SourceTableRecord> getAdjacentNodes(){
        Collection<SourceTableRecord> adjacentNodes = new ArrayList<SourceTableRecord>();
        for (PotentialMatchRecord pmr: getInboundEdges()){
             SourceTableRecord incomingNode = pmr.getDuplicate();
             if (!adjacentNodes.contains(incomingNode)){
                 adjacentNodes.add(incomingNode);
             }
        }
        for (PotentialMatchRecord pmr: getOutboundEdges()){
            SourceTableRecord outgoingNode = pmr.getMaster();
            if (!adjacentNodes.contains(outgoingNode)){
                adjacentNodes.add(outgoingNode);
            }            
       }
       return adjacentNodes;
    }
}
