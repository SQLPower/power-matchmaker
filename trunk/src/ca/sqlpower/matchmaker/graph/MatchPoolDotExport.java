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


package ca.sqlpower.matchmaker.graph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.swingui.ColorScheme;
import ca.sqlpower.util.WebColour;

/**
 * A class for exporting a dot file version of a pool of match results.
 * Dot files are a way of representing graph structure and attributes,
 * supported by the popular GraphViz package.  See http://www.graphviz.org/.
 */
public class MatchPoolDotExport {

    private static final Logger logger = Logger.getLogger(MatchPoolDotExport.class);
        
    /**
     * The group names that have been seen by {@link colourName()} in the order it saw them.
     */
    private final List<String> groupNames = new ArrayList<String>();
    
    /**
     * This is the project whose result table we're visualizing.
     */
    private final Project project;
    
    /**
     * The file we will write the dot code to.
     */
    private File dotFile;
    
    /**
     * Creates a new exporter instance that exports the result table of
     * the given project object.
     * 
     * @param project The project whose source table you want to represent as a dot file.
     */
    public MatchPoolDotExport(final Project project) {
        this.project = project;
    }
    
    /**
     * Returns the file that will be written to next time the graph is exported.
     */
    public File getDotFile() {
        return dotFile;
    }
    
    /**
     * Sets the file that will be written to next time the graph is exported.
     */
    public void setDotFile(File dotFile) {
        this.dotFile = dotFile;
    }

    /**
     * Saves a dot file representing the current contents of the match
     * result table.
     * 
     * @throws ArchitectRuntimeException If there are any problems accessing
     * the SQLObjects of the {@link #project}.
     */
    public void exportDotFile() throws SQLException, IOException {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(dotFile)));
            out.println("digraph " + project.getName());
            out.println("{");
            
            con = project.getSession().getConnection();
            stmt = con.createStatement();
            StringBuilder sql = new StringBuilder();
            String sourceTableName = DDLUtils.toQualifiedName(project.getResultTable());
            sql.append("SELECT * FROM ").append(sourceTableName);
            sql.append(" m1 WHERE NOT EXISTS ( SELECT 1 FROM ").append(sourceTableName);
            sql.append(" m2 WHERE m1.dup_candidate_10 = m2.dup_candidate_20 and m1.dup_candidate_20 = m2.dup_candidate_10 and m1.dup_candidate_10 < m2.dup_candidate_10)");
            sql.append(" ORDER BY");
            for (int i = 0; i < project.getSourceTableIndex().getChildCount(); i++) {
                if (i == 0) {
                    sql.append(" ");
                } else {
                    sql.append(", ");
                }
                sql.append(i+1);
            }
            rs = stmt.executeQuery(sql.toString());
            while (rs.next()) {
                out.print("  \"");
                out.print(lhsOriginalNodeName(rs));
                out.print("\" -> \"");
                out.print(rhsOriginalNodeName(rs));
                out.print("\" [color=\"");
                out.print(colourForEdge(rs.getString("GROUP_ID"), true));
                out.print("\" style=\"dotted\" arrowtail=\"none\" arrowhead=\"none\"");
                out.println("];");
                
                out.print("  \"");
                out.print(lhsNodeName(rs));
                out.print("\" -> \"");
                out.print(rhsNodeName(rs));
                out.print("\" [color=\"");
                out.print(colourForEdge(rs.getString("GROUP_ID"), false));
                out.print("\" style=\"");
                out.print(edgeStyle(rs.getString("MATCH_STATUS")));
                out.print("\" arrowtail=\"");
                out.print(arrowTailName(rs.getString("DUP1_MASTER_IND"), "vee"));
                out.print("\" arrowhead=\"");
                out.print(arrowHeadName(rs.getString("DUP1_MASTER_IND"), "vee"));
                out.print("\"");
                out.println("];");
                
            }

            out.println("}");
            
        } catch (ArchitectException ex) {
            throw new ArchitectRuntimeException(ex);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ex) { logger.error("Couldn't close result set", ex); }
            if (stmt != null) try { stmt.close(); } catch (SQLException ex) { logger.error("Couldn't close statement", ex); }
            if (con != null) try { con.close(); } catch (SQLException ex) { logger.error("Couldn't close connection", ex); }
            if (out != null) out.close();
        }
    }

    /**
     * Returns either "vee" or "none" depending on the given direction property.
     * The arrow head points at the Right-Hand Side node, so if dup2 is the master,
     * the head will be painted; otherwise no head is painted.
     * 
     * @param dup1master The "DUP1_MASTER" string from the database: either null, "Y", or "N".
     */
    private String arrowHeadName(String dup1master, String arrowType) {
        if (dup1master == null) {
            return "none";
        } else if (dup1master.equals("N")) {
            return arrowType;
        } else if (dup1master.equals("Y")) {
            return "none";
        } else {
            throw new IllegalArgumentException("Unknown Y/N/null value for dup1_master_ind: \""+dup1master+"\"");
        }
    }
    
    /**
     * Returns either "vee" or "none" depending on the given direction property.
     * The arrow tail points at the Left-Hand Side node, so if dup1 is the master,
     * the head will be painted; otherwise no head is painted.
     * 
     * @param dup1master The "DUP1_MASTER" string from the database: either null, "Y", or "N".
     */
    private String arrowTailName(String dup1master, String arrowType) {
        if (dup1master == null) {
            return "none";
        } else if (dup1master.equals("N")) {
            return "none";
        } else if (dup1master.equals("Y")) {
            return arrowType;
        } else {
            throw new IllegalArgumentException("Unknown Y/N/null value for dup1_master_ind: \""+dup1master+"\"");
        }
    }
    
    /**
     * Returns the appropriate style string for the given match status code.
     * 
     * @param string The match status value (might be null).
     * @return a DOT file style keyword
     */
    private String edgeStyle(String status) {
        if (status == null || status.equals("UNMATCH")) {
            return "dashed";
        } else if (status.equals("MATCH") || status.equals("AUTO_MATCH") || status.equals("NOMATCH")) {
            return "solid";
        } else if (status.equals("MERGED")) {
            return "dotted";
        } else {
            logger.error("Unknown match status: \""+status+"\"");
            return "bold";
        }
    }

    /**
     * Translates the given munge process name to an X11 colour name that GraphViz can use.
     * 
     * @param string The munge process name
     * @return An X11 colour name such that every time this method is called with the same
     * munge process name, the same colour name is returned, but no two munge process names will
     * translate to the same colour for the lifetime of this instance of MatchResultVisualizer.
     * @throws ArrayIndexOutOfBoundsException if you use more munge processes than we have set up
     * colours for.  (see COLOURS and add more items to it if you're running into this problem).
     */
    private String colourForEdge(String groupName, boolean original) {
        int index = groupNames.indexOf(groupName);
        if (index < 0) {
            groupNames.add(groupName);
            index = groupNames.size()-1;
        }
        WebColour c = ColorScheme.BREWER_SET19[index];
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    /**
     * Calls nodeName() with the correct parameters to get the unique identifier
     * values of the right-hand-side record.
     */
    private String rhsOriginalNodeName(ResultSet rs) throws SQLException, ArchitectException {
        List<String> colNames = new ArrayList<String>();
        for (int i = 0; i < project.getSourceTableIndex().getChildCount(); i++) {
            colNames.add("DUP_CANDIDATE_2"+i);
        }
        return nodeName(rs, colNames);
    }

    /**
     * Calls nodeName() with the correct parameters to get the unique identifier
     * values of the left-hand-side record.
     */
    private String lhsOriginalNodeName(ResultSet rs) throws SQLException, ArchitectException {
        List<String> colNames = new ArrayList<String>();
        for (int i = 0; i < project.getSourceTableIndex().getChildCount(); i++) {
            colNames.add("DUP_CANDIDATE_1"+i);
        }
        return nodeName(rs, colNames);
    }

    /**
     * Calls nodeName() with the correct parameters to get the unique identifier
     * values of the right-hand-side record.
     */
    private String rhsNodeName(ResultSet rs) throws SQLException, ArchitectException {
        List<String> colNames = new ArrayList<String>();
        for (int i = 0; i < project.getSourceTableIndex().getChildCount(); i++) {
            colNames.add("CURRENT_CANDIDATE_2"+i);
        }
        return nodeName(rs, colNames);
    }

    /**
     * Calls nodeName() with the correct parameters to get the unique identifier
     * values of the current left-hand-side record.
     */
    private String lhsNodeName(ResultSet rs) throws SQLException, ArchitectException {
        List<String> colNames = new ArrayList<String>();
        for (int i = 0; i < project.getSourceTableIndex().getChildCount(); i++) {
            colNames.add("CURRENT_CANDIDATE_1"+i);
        }
        return nodeName(rs, colNames);
    }

    /**
     * Comes up with a name for a graph node by concatenating the unique key values
     * from the current row of the given result set.
     * <p>
     * This is a subroutine of rhsNodeName and lhsNodeName, which are smart enough
     * to fill in the tricky index parameters for you.
     * 
     * @param rs The result set to get current values from
     * @param startIndex The column number to start getting values from
     * @param count The number of consecutive columns to get values from
     * @return A string containing the concatenation of the given values.
     * @throws SQLException if there is trouble retrieving any column values as strings
     */
    private String nodeName(ResultSet rs, List<String> colNames) throws SQLException {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String colName : colNames) {
            if (!first) sb.append("::");
            sb.append(rs.getString(colName));
            first = false;
        }
        return sb.toString();
    }
}
