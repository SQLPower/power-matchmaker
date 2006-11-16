package ca.sqlpower.matchmaker.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class DatabaseCleanup {
    private static final Logger logger = Logger.getLogger(DatabaseCleanup.class);
  
    /**
     * Empties out the match portion of a pl database
     * 
     * @param con this connection should be valid and you need to close it yourself!
     * @throws SQLException
     */
    public static void clearDatabase(Connection con) throws SQLException {
        List<String> tablesToDelete;
        tablesToDelete = new ArrayList<String>();
        //tablesToDelete.add("pl_match_translate");
       // tablesToDelete.add("pl_match_translate_group");
        tablesToDelete.add("pl_match_criteria");
        tablesToDelete.add("pl_match_group");
        tablesToDelete.add("pl_merge_consolidate_criteria");
        tablesToDelete.add("pl_merge_criteria");
        tablesToDelete.add("pl_match_xref_map");
        tablesToDelete.add("pl_match");
        tablesToDelete.add("pl_folder");
  
        Statement stmt = con.createStatement();
        try {
            for (String table: tablesToDelete) {
                logger.debug(table);
                System.err.println(table);
                stmt.execute("delete from "+table);
            }            
        } finally {
            try {
                stmt.close();
            } catch (SQLException ex) {
                logger.error("Failed to close statement!");
            }
        }
    }
    

}
