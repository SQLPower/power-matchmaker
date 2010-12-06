/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.PotentialMatchRecord.MasterSide;
import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;
import ca.sqlpower.matchmaker.PotentialMatchRecord.StoreState;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;


public class DatabaseMatchPoolReader extends AbstractMatchPoolReader {

	private static Logger logger = Logger.getLogger(DatabaseMatchPoolReader.class);
	private List<MatchCluster> clusterCache = new ArrayList<MatchCluster>();
	
	public DatabaseMatchPoolReader(MatchPool target) {
		super(target);
	}
	
	public void getClusters(int from, int to) {
		if(clusterCache.isEmpty()) {
			read();
		}
		if(to > matchPool.getClusterCount()) {
			to = matchPool.getClusterCount();
		}
		for(MatchCluster mc : clusterCache.subList(from, to)) {
			matchPool.addMatchCluster(mc);
		}
	}
	
	public void getAllPreviousMatches() {
		read();
		for(MatchCluster mc : clusterCache) {
			matchPool.addMatchCluster(mc);
		}
		clear();
	}
	
	public void clear() {
		clusterCache.clear();
	}
	
	public void read() {
		List<SQLColumn> displayColumns = matchPool.getDisplayColumns();
		Map<List<Object>,SourceTableRecord> sourceTableRecords = new HashMap<List<Object>,SourceTableRecord>();
		Set<PotentialMatchRecord> potentialMatchRecords = new HashSet<PotentialMatchRecord>();
		SQLTable resultTable = matchPool.getProject().getResultTable();
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        String lastSQL = null;
        try {
        	
        	//Read in all the records in the database
        	
            con = matchPool.getProject().createResultTableConnection();
            stmt = con.createStatement();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ");
            boolean first = true;
            for (SQLColumn col : resultTable.getColumns()) {
                if (!first) sql.append(",\n");
                sql.append(" result.");
                sql.append(col.getName());
                first = false;
            }
            SQLIndex sourceTableIndex = matchPool.getProject().getSourceTableIndex();
			if (displayColumns == null || displayColumns.size() == 0) {
            	displayColumns = new ArrayList<SQLColumn>();
            	for (SQLIndex.Column col : sourceTableIndex.getChildren(SQLIndex.Column.class)) {
            		displayColumns.add(col.getColumn());
            	}
            }
            for (SQLColumn col : displayColumns) {
            	sql.append(",\n");
            	sql.append(" source1.");
            	sql.append(col.getName());
            	sql.append(" as disp1" + displayColumns.indexOf(col));
            	
            	sql.append(",\n");
            	sql.append(" source2.");
            	sql.append(col.getName());
            	sql.append(" as disp2" + displayColumns.indexOf(col));
            }
            
            sql.append("\n FROM ");
            sql.append(DDLUtils.toQualifiedName(resultTable));
            sql.append(" result,");
            SQLTable sourceTable = matchPool.getProject().getSourceTable();
			sql.append(DDLUtils.toQualifiedName(sourceTable));
            sql.append(" source1,");
            sql.append(DDLUtils.toQualifiedName(sourceTable));
            sql.append(" source2");
            int index = 0;
        	for (SQLIndex.Column col : sourceTableIndex.getChildren(SQLIndex.Column.class)) {
        		if (index == 0) { 
            		sql.append("\n WHERE");
            	} else {
            		sql.append(" AND");
            	}
            	sql.append(" result.");
            	sql.append("DUP_CANDIDATE_1"+index);
            	sql.append("=");
            	sql.append("source1." + col.getColumn().getName());
            	sql.append(" AND");
            	sql.append(" result.");
            	sql.append("DUP_CANDIDATE_2"+index);
            	sql.append("=");
            	sql.append("source2." + col.getColumn().getName());
            	index++;
            }
            lastSQL = sql.toString();
            logger.debug("MatchmatchPool's findAll method SQL: \n" + lastSQL);
            rs = stmt.executeQuery(lastSQL);
            while (rs.next()) {
                MungeProcess mungeProcess = matchPool.getProject().getMungeProcessByName(rs.getString("GROUP_ID"));
                String statusCode = rs.getString("MATCH_STATUS");
                MatchType matchStatus = MatchType.typeForCode(statusCode);
                if (statusCode != null && matchStatus == null) {
                    matchPool.getSession().handleWarning(
                            "Found a match record with the " +
                            "unknown/invalid match status \""+statusCode+
                            "\". Ignoring it.");
                    continue;
                }
                
				if (mungeProcess != null) {
                	if (!mungeProcess.isValidate()) {
                		continue;
                	}
                }
                int indexSize = sourceTableIndex.getChildCount();
                List<Object> lhsKeyValues = new ArrayList<Object>(indexSize);
                List<Object> rhsKeyValues = new ArrayList<Object>(indexSize);
                for (int i = 0; i < indexSize; i++) {
                	Class typeClass = TypeMap.typeClass(sourceTableIndex.getChild(i).getColumn().getType());
					if (typeClass == BigDecimal.class) {
                		lhsKeyValues.add(rs.getBigDecimal("DUP_CANDIDATE_1"+i));
                		rhsKeyValues.add(rs.getBigDecimal("DUP_CANDIDATE_2"+i));
                	} else if (typeClass == Date.class) {
                		lhsKeyValues.add(rs.getDate("DUP_CANDIDATE_1"+i));
                		rhsKeyValues.add(rs.getDate("DUP_CANDIDATE_2"+i));
                	} else if (typeClass == Boolean.class) {
                		lhsKeyValues.add(rs.getBoolean("DUP_CANDIDATE_1"+i));
                		rhsKeyValues.add(rs.getBoolean("DUP_CANDIDATE_2"+i));
                	} else {
                		lhsKeyValues.add(rs.getString("DUP_CANDIDATE_1"+i));
                		rhsKeyValues.add(rs.getString("DUP_CANDIDATE_2"+i));
                	}
                }
                
                List<Object> lhsDisplayValues = new ArrayList<Object>();
                List<Object> rhsDisplayValues = new ArrayList<Object>();
                for (int i = 0; i < displayColumns.size(); i++) {
                    lhsDisplayValues.add(rs.getObject("disp1"+i));
                    rhsDisplayValues.add(rs.getObject("disp2"+i));
                }
                
                logger.debug("Found a match bewteen " + lhsKeyValues + " and " + rhsKeyValues + ". Adding to pool.");
                
                SourceTableRecord rhs = sourceTableRecords.get(rhsKeyValues);
            	SourceTableRecord lhs = sourceTableRecords.get(lhsKeyValues);
               	if(rhs == null) {
            		rhs = new SourceTableRecord(matchPool.getProject(), rhsDisplayValues, rhsKeyValues);
            		sourceTableRecords.put(rhsKeyValues, rhs);
               	}
               	if(lhs == null) {
               		lhs = new SourceTableRecord(matchPool.getProject(), lhsDisplayValues, lhsKeyValues);
            		sourceTableRecords.put(lhsKeyValues, lhs);
               	}
               	PotentialMatchRecord pmr = new PotentialMatchRecord(mungeProcess, matchStatus, lhs, rhs, false);
                pmr.setStoreState(StoreState.CLEAN);
               	String master = (String)(rs.getObject("DUP1_MASTER_IND"));
               	if ("Y".equals(master)) {
               		pmr.setMaster(MasterSide.LHS);
               	} else if ("N".equals(master)) {
               		pmr.setMaster(MasterSide.RHS);
               	}
               	if(mungeProcess != null && !pmr.getMatchStatus().equals(MatchType.MERGED)) {
	               	potentialMatchRecords.add(pmr);
               	} else {
               		pmr.setMatchStatus(MatchType.DELETE);
	               	potentialMatchRecords.add(pmr);
               	}
            }
            
        } catch (SQLException ex) {
            logger.error("Error in query: "+lastSQL, ex);
            matchPool.getSession().handleWarning(
                    "Error in SQL Query!" +
                    "\nMessage: "+ex.getMessage() +
                    "\nSQL State: "+ex.getSQLState() +
                    "\nQuery: "+lastSQL);
            throw new RuntimeException(ex);
        } catch (SQLObjectException ex) {
            throw new RuntimeException(ex);
		} finally {
            if (rs != null) try { rs.close(); } catch (SQLException ex) { logger.error("Couldn't close result set", ex); }
            if (stmt != null) try { stmt.close(); } catch (SQLException ex) { logger.error("Couldn't close statement", ex); }
            if (con != null) try { con.close(); } catch (SQLException ex) { logger.error("Couldn't close connection", ex); }
        }
		
		//We must sort the read records and make them into clusters
		clusterCache = MatchPool.sortMatches(sourceTableRecords.values(), potentialMatchRecords);
		
		matchPool.setClusterCount(clusterCache.size());
	}
}
