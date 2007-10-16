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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.graph.DepthFirstSearch;
import ca.sqlpower.matchmaker.ColumnMergeRules.MergeActionType;
import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;
import ca.sqlpower.matchmaker.graph.MatchPoolDirectedGraphModel;
import ca.sqlpower.sql.SQL;

/**
 * A Processor which takes a List of arrays of MungeStepOutputs, which it would
 * typically get from a MungeProcess, and performs matching on the data, and stores
 * the match results into the match repository. 
 */
public class MergeProcessor extends AbstractProcessor {

	private MatchMakerSession session;
	private Match match;

	/**
	 * Stores the sets of potential matches which is used for merging the data.
	 */
	private MatchPool pool;
	
    /**
     * The list of MungeSteps obtained from the MungeProcess that this processor will
     * process, sorted in the exact order that the processor will process them.
     */
    private List<SourceTableRecord> processOrder;
	
    private MatchPoolDirectedGraphModel gm;
    
    private SQLTable sourceTable;
    private Connection con;
    private Statement stmt;
    
	public MergeProcessor(Match match, MatchMakerSession session) 
			throws SQLException, ArchitectException {
		this.match = match;
		this.session = session;
		pool = new MatchPool(match);
		
		//Initialize the match pool
		pool.findAll(new ArrayList<SQLColumn>());
		
		//Topological sort
        gm = new MatchPoolDirectedGraphModel(pool);
        DepthFirstSearch<SourceTableRecord, PotentialMatchRecord> dfs = new DepthFirstSearch<SourceTableRecord, PotentialMatchRecord>();
        dfs.performSearch(gm);
        processOrder = dfs.getFinishOrder();
        
        sourceTable = match.getSourceTable();
        con = session.getConnection();
        stmt = con.createStatement();
	}
	
	/**
	 * Call this method to run the merge engine. Currently it only merges the data on 
	 * the source table (not the child tables). The avg action is currently not
	 * supported.
	 */
	public Boolean call() throws Exception {

        TableMergeRules sourceTableMergeRule = null;
        boolean needsToCheckDup = false;
        Map<SQLColumn, ColumnMergeRules> mapping = new HashMap<SQLColumn, ColumnMergeRules>();
        
        // Finds the correct table merge rule according to the source table
        sourceTableMergeRule = match.getTableMergeRules().get(0);
        
        if (!sourceTableMergeRule.getSourceTable().equals(sourceTable)) {
        	throw new IllegalStateException("The first merge rule needs to be the source table merge rule.");
        }

		// Finds the columns that needs to be merged and maps it to the 
		// corresponding column merge rule.
		for (ColumnMergeRules cmr : sourceTableMergeRule.getChildren()) {
			if (cmr.getActionType() != MergeActionType.IGNORE) {
				needsToCheckDup = true;
			}
			mapping.put(cmr.getColumn(), cmr);
		}
		

		// Retrieves the sql statements to make the merge changes and executes it.
		for (SourceTableRecord str : processOrder) {
			for (PotentialMatchRecord pm : gm.getOutboundEdges(str)) {
				if (pm.isMatch()) {
					List<Object> keyValues = pm.getDuplicate().getKeyValues();
					List<String> sourceIndexColumnNames = new ArrayList<String>();
					for (SQLIndex.Column c : match.getSourceTableIndex().getChildren()) {
						sourceIndexColumnNames.add(c.getName());
					}
					MergeChildTables(sourceIndexColumnNames, keyValues, sourceTableMergeRule);
					
					
					if (needsToCheckDup) {
						String sql = createUpdateSQL(pm, mapping, sourceIndexColumnNames);
						int rows = stmt.executeUpdate(sql.toString());
						if (rows != 1) {
							throw new IllegalStateException("The update did not affect the correct " +
									"number of rows: expected 1 but got " + rows); 
						}
					}

					
				}

			}
		}

		
		// Retrieves the sql statements to delete the duplicate records and executes it.
		if (sourceTableMergeRule.isDeleteDup()) {
			for (PotentialMatchRecord pm : pool.getPotentialMatches()) {
				if (pm.isMatch()) {
					
					//delete the duplicate record
					String sql = createDeleteSQL(pm);
					int rows = stmt.executeUpdate(sql.toString());
					
					if (rows != 1) {
						throw new IllegalStateException("The update did not affect the correct " +
							"number of rows: expected 1 but got " + rows); 
					}
				}
			}
		}
		
		for (PotentialMatchRecord pm : pool.getPotentialMatches()) {
			if (pm.isMatch()) {
				pm.setMatchStatus(MatchType.MERGED);
			}
		}
		
		pool.store();
		stmt.close();
		con.close();
		return Boolean.TRUE;
	}
	
	/**
	 * Does the merging on the child tables recusively
	 * @param keyValues keyValues must be in the ordered by the same order of the columns
	 */
	private void MergeChildTables(List<String> keyColumnNames, List<Object> keyValues, TableMergeRules parentTableMergeRule) throws ArchitectException, SQLException {
		SQLTable parentTable = parentTableMergeRule.getSourceTable();
		
		
		for (TableMergeRules childTableMergeRule : match.getTableMergeRules()) {
			if (parentTable.equals(childTableMergeRule.getParentTable())) {
				
				if (childTableMergeRule.getChildMergeAction() == TableMergeRules.ChildMergeActionType.DELETE_ALL_DUP_CHILD) {
					StringBuilder sql = new StringBuilder();
					
					// Merge the grand child tables
					List<String> foreignKeyColumnNames = getOrderedForeignKeyColumns(childTableMergeRule, keyColumnNames);
					List<String> childKeyColumnNames = new ArrayList<String>();
					for (ColumnMergeRules cmr : childTableMergeRule.getChildren()) {
						if (cmr.isInPrimaryKey()) {
							childKeyColumnNames.add(cmr.getColumnName());
						}
					}
					List<List<Object>> mrKeyValues = getKeyValuesByParent(childKeyColumnNames ,foreignKeyColumnNames, keyValues, childTableMergeRule);
					for (List<Object> keyVal : mrKeyValues) {
						MergeChildTables(childKeyColumnNames, keyVal, childTableMergeRule);
					}
					
					// Delete the duplicate child records					
					sql.append("DELETE FROM " + DDLUtils.toQualifiedName(childTableMergeRule.getSourceTable()));
					String whereStatement = generateWhereStatement(foreignKeyColumnNames, keyValues);
					sql.append(whereStatement);
					stmt.executeUpdate(sql.toString());
				}
			}
		}
	}
	
	
	
	private List<List<Object>> getKeyValuesByParent(List<String> keyColumnNames, 
			List<String> foreignKeyColumnNames, List<Object> foreignKeyValues, 
			TableMergeRules mr) throws SQLException {
		boolean first = true;
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		for (String columnName : keyColumnNames) {
			if (!first) {
				sql.append(", ");
			}
			first = false;
			sql.append(columnName);
		}
		sql.append("\n FROM ");
		sql.append(DDLUtils.toQualifiedName(mr.getSourceTable()));
		
		
		
		String whereStatement = generateWhereStatement(foreignKeyColumnNames, foreignKeyValues);
		sql.append(whereStatement);
		ResultSet rs = stmt.executeQuery(sql.toString());
		
		List<List<Object>> result = new ArrayList<List<Object>>();
		while (rs.next()) {
			List<Object> tempList = new ArrayList<Object>();
			for (int i = 0; i < keyColumnNames.size(); i++) {
				tempList.add(rs.getObject(i+1));
			}
			result.add(tempList);
		}
		
		return result;
	}

	

	private List<String> getOrderedForeignKeyColumns(TableMergeRules mr, List<String> foreignKeyColumnNames) {
		List<String> curForeignKeyColumnNames = new ArrayList<String>();
		for (ColumnMergeRules cmr : mr.getChildren()) {
			if (cmr.getImportedKeyColumn() != null) {
				for (String colName : foreignKeyColumnNames) {
					if (colName.equals(cmr.getImportedKeyColumn().getName())) {
						curForeignKeyColumnNames.add(cmr.getColumnName());
					}
				}
				if (foreignKeyColumnNames.size() == curForeignKeyColumnNames.size()) {
					break;
				}
			}
		}
		if (foreignKeyColumnNames.size() != curForeignKeyColumnNames.size()) {
			throw new IllegalStateException("The foreign key for merge rule: " + mr.getName() + " is not set properly.");
		}
		return curForeignKeyColumnNames;
	}

	/**
	 * This creates the update sql statement which updates the master record 
	 * according to the merge rules.
	 * @param sourceIndexColumnNames 
	 */
	private String createUpdateSQL(PotentialMatchRecord pm,
			Map<SQLColumn, ColumnMergeRules> mapping, List<String> sourceIndexColumnNames) 
			throws SQLException, ArchitectException {
		List<Object> dupVals;
		List<Object> masterVals;
		dupVals = pm.getDuplicate().fetchValues();
		masterVals = pm.getMaster().fetchValues();
		int count = 0;

		//builds the update sql
		StringBuilder sql = new StringBuilder();

		sql.append("UPDATE ");
		sql.append(DDLUtils.toQualifiedName(sourceTable));
		sql.append("\n SET ");
		boolean first = true;

		for (SQLColumn col : sourceTable.getColumns()) {
			ColumnMergeRules cmr = mapping.get(col);
			Object dupVal = dupVals.get(count);
			Object masterVal = masterVals.get(count);
			Object resultVal = null;
			count++;

			if (cmr != null && cmr.getActionType() != MergeActionType.IGNORE) {

				if (masterVal == null) {
					resultVal = dupVal;
				} else if (dupVal == null) {
					resultVal = masterVal;	
				} else if (cmr.getActionType().equals(MergeActionType.AUGMENT)) {
					resultVal = masterVal;
				} else if (cmr.getActionType().equals(MergeActionType.CONCAT)) {
					resultVal = concatObjects(masterVal, dupVal);
				} else if (cmr.getActionType().equals(MergeActionType.MIN)) {
					resultVal = minOfObjects(masterVal, dupVal);
				} else if (cmr.getActionType().equals(MergeActionType.MAX)) {
					resultVal = maxOfObjects(masterVal, dupVal);
				} else if (cmr.getActionType().equals(MergeActionType.SUM)) {
					resultVal = sumOfObjects(masterVal, dupVal);
				} else {
					throw new UnsupportedOperationException("Impossible...");
				}
				
				if (!first) sql.append(", ");
				first = false;
				sql.append(col.getName() + "=");
				if (cmr.getActionType().equals(MergeActionType.SUM)) {
					sql.append(resultVal.toString());
				} else if (resultVal == null) {
					sql.append("null");
				} else {
					sql.append(formatObjectToSQL(resultVal));
				} 
			}
		}
		String whereStatement = generateWhereStatement(sourceIndexColumnNames, pm.getMaster().getKeyValues());
		sql.append(whereStatement);
		return sql.toString();
	}
	
	/**
	 * This creates the delete sql statement that deletes the duplicates
	 */
	private String createDeleteSQL(PotentialMatchRecord pm) throws ArchitectException, SQLException {
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM " + sourceTable.getName());
		String whereStatement = generateWhereStatement(pm.getDuplicate().getKeyValues());
		sql.append(whereStatement);
		return sql.toString();
	}
	
	private Object sumOfObjects(Object masterVal, Object dupVal) {
		if (masterVal instanceof Number) {
			return masterVal.toString() +  " + " + dupVal.toString();
		} else {
			throw new IllegalStateException("Illegal type for the SUM operation.");
		}
	}

	private Object concatObjects(Object masterVal, Object dupVal) {
		if (masterVal instanceof String) {
			return masterVal.toString() + dupVal.toString();
		} else {
			throw new IllegalStateException("Illegal type for the CONCAT operation (must be String).");
		}
	}
	
	private Object minOfObjects(Object masterVal, Object dupVal) {
		if (masterVal instanceof Number) {
			if (((Number)masterVal).doubleValue() <= ((Number)dupVal).doubleValue()) {
				return masterVal;
			} else {
				return dupVal;
			}
		} else if (masterVal instanceof String) {
			if (((String)masterVal).compareTo((String)dupVal) <= 0) {
				return masterVal;
			} else {
				return dupVal;
			}
		} else if (masterVal instanceof Date) {
			if (((Date)masterVal).compareTo((Date)dupVal) <= 0) {
				return masterVal;
			} else {
				return dupVal;
			}
		} else {
			throw new IllegalStateException("Illegal type for the MIN operation.");
		}
	}
	
	private Object maxOfObjects(Object masterVal, Object dupVal) {
		if (masterVal instanceof Number) {
			if (((Number)masterVal).doubleValue() >= ((Number)dupVal).doubleValue()) {
				return masterVal;
			} else {
				return dupVal;
			}
		} else if (masterVal instanceof String) {
			if (((String)masterVal).compareTo((String)dupVal) >= 0) {
				return masterVal;
			} else {
				return dupVal;
			}
		} else if (masterVal instanceof Date) {
			if (((Date)masterVal).compareTo((Date)dupVal) >= 0) {
				return masterVal;
			} else {
				return dupVal;
			}
		} else {
			throw new IllegalStateException("Illegal type for the MAX operation.");
		}
	}
	
	private String formatObjectToSQL(Object ival) throws SQLException {
		if (ival instanceof Date) {
			return SQL.escapeDateTime(con, (Date) ival);
		} else if (ival instanceof Number) {
			return ival.toString();
		} else {
			return SQL.quote(ival.toString());
		}
	}
	
	private String generateWhereStatement(List<Object> keyValues) throws ArchitectException, SQLException {
		boolean first = true;
		int colIndex = 0;
		StringBuilder sql = new StringBuilder();
		sql.append("\n WHERE ");
		for (Object ival : keyValues) {
			SQLIndex.Column icol = match.getSourceTableIndex().getChild(colIndex++);
			if (!first) sql.append(" AND ");
			sql.append(icol.getName());
			sql.append("=");
			if (ival == null) {
				sql.append(" IS NULL");
			} else {
				sql.append(formatObjectToSQL(ival));
			} 
			first = false;
		}
		return sql.toString();
	}
	
	private String generateWhereStatement(List<String> keyColumnNames, List<Object> keyValues) throws SQLException {
		if (keyColumnNames.size() != keyValues.size()) {
			throw new IllegalStateException("Invalid number of column name and values.");
		}
		boolean first = true;
		int colIndex = 0;
		StringBuilder sql = new StringBuilder();
		sql.append("\n WHERE ");
		for (Object ival : keyValues) {
			String columnName = keyColumnNames.get(colIndex++);
			if (!first) sql.append(" AND ");
			sql.append(columnName);
			sql.append("=");
			if (ival == null) {
				sql.append(" IS NULL");
			} else {
				sql.append(formatObjectToSQL(ival));
			} 
			first = false;
		}
		return sql.toString();
	}

}
