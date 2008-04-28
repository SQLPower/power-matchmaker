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
        tablesToDelete.add("pl_match_translate");
        tablesToDelete.add("pl_match_translate_group");
        tablesToDelete.add("mm_munge_step_parameter");
        tablesToDelete.add("mm_munge_step_input");
        tablesToDelete.add("mm_munge_step_output");
        tablesToDelete.add("mm_munge_step");
        tablesToDelete.add("mm_munge_process");
        tablesToDelete.add("mm_column_merge_rule");
        tablesToDelete.add("mm_table_merge_rule");
        tablesToDelete.add("pl_match_xref_map");
        tablesToDelete.add("mm_project");
        tablesToDelete.add("pl_folder2");
  
        Statement stmt = con.createStatement();
        try {
            for (String table: tablesToDelete) {
                logger.debug(table);
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
