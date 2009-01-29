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

package ca.sqlpower.matchmaker.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.sqlobject.SQLObjectUtils;

/**
 * Simple utility class for finding frequently-used words in a column of a table.
 */
public class CommonWordsFinder {

    private static final Logger logger = Logger.getLogger(CommonWordsFinder.class);
    
    private boolean caseSensitive;
    private String wordSeparator;
    
    // TODO filter by word length and min occurrences
    
    public List<WordCount> countWords(SQLColumn column) throws SQLException {
        Map<String, WordCount> wordCounts = new HashMap<String, WordCount>();
        
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = column.getParentTable().getParentDatabase().getConnection();
            stmt = con.createStatement();
            
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ").append(column.getName()).append(" FROM ").append(SQLObjectUtils.toQualifiedName(column.getParentTable(), SQLDatabase.class));
            rs = stmt.executeQuery(sql.toString());
            
            Pattern wordSep = Pattern.compile(wordSeparator);
            
            while (rs.next()) {
                String val = rs.getString(1);
                if (val == null) continue;
                
                String[] words = wordSep.split(val);
                for (String word : words) {
                    if ("".equals(word)) continue;
                    if (!caseSensitive) {
                        word = word.toLowerCase();
                    }
                    WordCount wc = wordCounts.get(word);
                    if (wc == null) {
                        wc = new WordCount(word);
                        wordCounts.put(word, wc);
                    }
                    wc.incrementCount();
                }
            }
            rs.close();
            rs = null;
            stmt.close();
            stmt = null;
            
            List<WordCount> sortedCounts = new ArrayList<WordCount>(wordCounts.values());
            Collections.sort(sortedCounts);
            return sortedCounts;
            
        } catch (SQLObjectException e) {
            throw new SQLObjectRuntimeException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    logger.error("Failed to close result set. Squishing this exception:", ex);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    logger.error("Failed to close statement. Squishing this exception:", ex);
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    logger.error("Failed to close connection. Squishing this exception:", ex);
                }
            }
        }
    }
    
    
}
