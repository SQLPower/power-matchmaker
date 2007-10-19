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
	private Project project;

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
    
	public MergeProcessor(Project project, MatchMakerSession session) 
			throws SQLException, ArchitectException {
		this.project = project;
		this.session = session;
		pool = new MatchPool(project);
		
		//Initialize the match pool
		pool.findAll(new ArrayList<SQLColumn>());
		
		//Topological sort
        gm = new MatchPoolDirectedGraphModel(pool);
        DepthFirstSearch<SourceTableRecord, PotentialMatchRecord> dfs = new DepthFirstSearch<SourceTableRecord, PotentialMatchRecord>();
        dfs.performSearch(gm);
        processOrder = dfs.getFinishOrder();
        
        sourceTable = project.getSourceTable();
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
        
		monitorableHelper.setStarted(true);
		monitorableHelper.setFinished(false);
        
        // Finds the correct table merge rule according to the source table
        sourceTableMergeRule = project.getTableMergeRules().get(0);
        
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
		
		int rowCount = 0;
		for (SourceTableRecord str: processOrder) {
			rowCount += gm.getOutboundEdges(str).size();
		}
		monitorableHelper.setJobSize(rowCount);
		
		for (SourceTableRecord str : processOrder) {
			for (PotentialMatchRecord pm : gm.getOutboundEdges(str)) {
				monitorableHelper.incrementProgress();
				if (pm.isMatch()) {
					List<Object> dupKeyValues = pm.getDuplicate().getKeyValues();
					List<Object> masterKeyValues = pm.getMaster().getKeyValues();
					List<String> sourceIndexColumnNames = getSourceIndexColumnNames();
					
					// Starts the recursive merging
					mergeChildTables(sourceIndexColumnNames, dupKeyValues, masterKeyValues, sourceTableMergeRule);

					if (needsToCheckDup) {
						int rows = updateSourceTableRows(pm, mapping, sourceIndexColumnNames, masterKeyValues);
						if (rows != 1) {
							throw new IllegalStateException("The update did not affect the correct " +
									"number of rows: expected 1 but got " + rows); 
						}
					}
				}
			}
		}
		
		if (sourceTableMergeRule.isDeleteDup()) {
			for (PotentialMatchRecord pm : pool.getPotentialMatches()) {
				if (pm.isMatch()) {
					List<Object> dupKeyValues = pm.getDuplicate().getKeyValues();
					List<String> sourceIndexColumnNames = getSourceIndexColumnNames();
					//delete the duplicate record
					int rows = deleteRows(sourceTable, sourceIndexColumnNames, dupKeyValues);
					
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
		monitorableHelper.setFinished(true);
		return Boolean.TRUE;
	}
	
	private List<String> getSourceIndexColumnNames() throws ArchitectException {
		List<String> sourceIndexColumnNames = new ArrayList<String>();
		for (SQLIndex.Column c : project.getSourceTableIndex().getChildren()) {
			sourceIndexColumnNames.add(c.getName());
		}
		return sourceIndexColumnNames;
	}

	/**
	 * Merge the child tables recusively.
	 * <p>
	 * Note that the keyValues must be in the ordered by the same order of the columns
	 */
	private void mergeChildTables(List<String> parentKeyColumnNames, 
			List<Object> dupKeyValues, List<Object> masterKeyValues, 
			TableMergeRules parentTableMergeRule) throws ArchitectException, SQLException {
		SQLTable parentTable = parentTableMergeRule.getSourceTable();
		
		for (TableMergeRules childTableMergeRule : project.getTableMergeRules()) {
			if (parentTable.equals(childTableMergeRule.getParentTable())) {
				
				// populates the data required to merge the grand child tables	
				List<String> foreignKeyColumnNames = getOrderedForeignKeyColumns(
						childTableMergeRule, parentKeyColumnNames);
				List<String> childKeyColumnNames = getKeyColumnNames(childTableMergeRule);
				List<List<Object>> childDupKeyValues = getDupKeyValues(
						childKeyColumnNames, foreignKeyColumnNames, dupKeyValues, 
						childTableMergeRule);
				List<List<Object>> childMasterKeyValues = null; 
					
				if (childTableMergeRule.getChildMergeAction() == 
					TableMergeRules.ChildMergeActionType.DELETE_ALL_DUP_CHILD) {
					
					childMasterKeyValues = new ArrayList<List<Object>>();
					for (int i = 0; i < childDupKeyValues.size(); i++) {
						childMasterKeyValues.add(null);
					}
					
				} else if (childTableMergeRule.getChildMergeAction() == 
					TableMergeRules.ChildMergeActionType.UPDATE_FAIL_ON_CONFLICT) {
					
					// cannot update if there are no masterKeyValues
					if (masterKeyValues == null) {
						throw new IllegalStateException(
						"Cannot update when parent table deletes all child duplicate records");
					}

					// populates the data required to merge the grand child tables	
					childMasterKeyValues = getMasterKeyValues(
							parentKeyColumnNames, childKeyColumnNames ,foreignKeyColumnNames, 
							dupKeyValues, masterKeyValues, childTableMergeRule, childDupKeyValues);

					// Try to see if creating a copy of the old record with the new key values would succeed
					for (List<Object> newKeyVals : childMasterKeyValues) {
						if (findRows(childTableMergeRule.getSourceTable(), 
								childKeyColumnNames, newKeyVals)) {
							//fail on conflict
							throw new IllegalStateException(
							"Merge Failed: Multiple records with the same primary key created on update.");
						}
					}

					// Creates a copy of the old record with the new key values
					for (int i = 0; i < childDupKeyValues.size(); i++) {
						List<Object> oldRow = getRow(childKeyColumnNames, 
								childDupKeyValues.get(i), childTableMergeRule);
						insertRow(oldRow, childKeyColumnNames, childMasterKeyValues.get(i), 
								childTableMergeRule);
					}

				} else if (childTableMergeRule.getChildMergeAction() == 
					TableMergeRules.ChildMergeActionType.UPDATE_DELETE_ON_CONFLICT) {

					// cannot update if there are no masterKeyValues
					if (masterKeyValues == null) {
						throw new IllegalStateException(
								"Cannot update when parent table deletes all child duplicate records");
					}

					// populates the data required to merge the grand child tables	
					childMasterKeyValues = getMasterKeyValues(
							parentKeyColumnNames, childKeyColumnNames ,foreignKeyColumnNames, 
							dupKeyValues, masterKeyValues, childTableMergeRule, childDupKeyValues);
					
					// Try to see if the update would succeed
					for (int i = 0; i < childDupKeyValues.size(); i++) {
						if (!findRows(childTableMergeRule.getSourceTable(), childKeyColumnNames, 
								childMasterKeyValues.get(i))) {
							//insert a new row if now conflict
							List<Object> oldRow = getRow(childKeyColumnNames, 
									childDupKeyValues.get(i), childTableMergeRule);
							insertRow(oldRow, childKeyColumnNames, childMasterKeyValues.get(i), 
									childTableMergeRule);
						}
					}

				}  else if (childTableMergeRule.getChildMergeAction() == TableMergeRules.ChildMergeActionType.UPDATE_USING_SQL) {
					// cannot update if there are no masterKeyValues
					if (masterKeyValues == null) {
						throw new IllegalStateException(
								"Cannot update when parent table deletes all child duplicate records");
					}
					
					childMasterKeyValues = new ArrayList<List<Object>>();
					for (List<Object> childDupKeyValue : childDupKeyValues) {
						List<Object> newRow = getUpdatedRow(childKeyColumnNames, 
								childDupKeyValue, childTableMergeRule, foreignKeyColumnNames, masterKeyValues);
						List<Object> childMasterKeyValue = new ArrayList<Object>();
						for (int i = 0; i < newRow.size(); i++) {
							if (childTableMergeRule.getChildren().get(i).isInPrimaryKey()) {
								childMasterKeyValue.add(newRow.get(i));
							}
						}
						childMasterKeyValues.add(childMasterKeyValue);
						insertRow(newRow, childTableMergeRule);
					}
				}
				
				// Recursively merge all the grand child tables
				for (int i = 0; i < childDupKeyValues.size(); i++) {
					mergeChildTables(childKeyColumnNames, childDupKeyValues.get(i), childMasterKeyValues.get(i), childTableMergeRule);
				}

				// Delete the duplicate child records
				deleteRows(childTableMergeRule.getSourceTable(), foreignKeyColumnNames, dupKeyValues);
			} 

			

			
		}
	}
	
	private void insertRow(List<Object> newRow, TableMergeRules tableMergeRule) throws SQLException {
		StringBuilder sql = new StringBuilder();
		StringBuilder sqlValues = new StringBuilder();
		boolean first = true;
		Object ival;
		int colIndex = 0;
		
		sql.append("INSERT INTO ");
		sql.append(DDLUtils.toQualifiedName(tableMergeRule.getSourceTable()));
		sql.append("\n (");
		sqlValues.append("\n VALUES (");
		for (ColumnMergeRules cmr : tableMergeRule.getChildren()) {
			if (!first) {
				sql.append(", ");
				sqlValues.append(", ");
			}
			first = false;
			sql.append(cmr.getColumnName());
			ival = newRow.get(colIndex++);
			if (ival == null) {
				sqlValues.append("NULL");
			} else {
				sqlValues.append(formatObjectToSQL(ival));
			} 
		}
		sql.append(")");
		sqlValues.append(")");
		sql.append(sqlValues.toString());
		stmt.executeUpdate(sql.toString());
		
	}

	private List<Object> getUpdatedRow(List<String> columnNames, List<Object> columnVals, TableMergeRules tableMergeRule, List<String> foreignKeyColumnNames, List<Object> foreignKeyValues) throws SQLException {
		List<Object> resultRow = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder();
		boolean first = true;
		Object ival;
		
		sql.append("SELECT ");
		for (ColumnMergeRules cmr : tableMergeRule.getChildren()) {
			boolean inForeignKey = false;
			if (!first) {
				sql.append(", ");
			}
			first = false;
			for (int i = 0; i < foreignKeyColumnNames.size(); i++) {
				if (cmr.getColumnName().equals(foreignKeyColumnNames.get(i))) {
					ival = foreignKeyValues.get(i);
					sql.append("(");
					if (ival == null) {
						sql.append("NULL");
					} else {
						sql.append(formatObjectToSQL(ival));
					} 
					sql.append(") AS ");
					inForeignKey = true;
					break;
				}
			}
			if (!inForeignKey) {
				if (!"".equals(cmr.getUpdateStatement())) {
					sql.append("(");
					sql.append(cmr.getUpdateStatement());
					sql.append(") AS ");
				}
			}
			sql.append(cmr.getColumnName());
		}
		sql.append("\n FROM ");
		sql.append(DDLUtils.toQualifiedName(tableMergeRule.getSourceTable()));
		String whereStatement = generateWhereStatement(columnNames, columnVals);
		sql.append(whereStatement);
		ResultSet rs = stmt.executeQuery(sql.toString());
		if (!rs.next()) {
			throw new IllegalStateException("Record with primary key not found");
		}
		
		for (ColumnMergeRules cmr : tableMergeRule.getChildren()) {
			resultRow.add(rs.getObject(cmr.getColumnName()));
		}
		
		if (rs.next()) {
			throw new IllegalStateException("Multiple records found with same primary key");
		}
		return resultRow;
	}

	private void insertRow(List<Object> oldRow, List<String> keyColumnNames, List<Object> keyValues, TableMergeRules tableMergeRule) throws SQLException {
		StringBuilder sql = new StringBuilder();
		StringBuilder sqlValues = new StringBuilder();
		boolean first = true;
		int colIndex = 0;
		Object ival;
		
		sql.append("INSERT INTO ");
		sql.append(DDLUtils.toQualifiedName(tableMergeRule.getSourceTable()));
		sql.append("\n (");
		sqlValues.append("\n VALUES (");
		
		for (ColumnMergeRules cmr : tableMergeRule.getChildren()) {
			if (!keyColumnNames.contains(cmr.getColumnName())) {
				if (!first) {
					sql.append(", ");
					sqlValues.append(", ");
				}
				first = false;
				sql.append(cmr.getColumnName());
				
				ival = oldRow.get(colIndex); 
				if (ival == null) {
					sqlValues.append("NULL");
				} else {
					sqlValues.append(formatObjectToSQL(ival));
				} 
			}
			colIndex++;
		}
		colIndex = 0;
		for (String colName : keyColumnNames) {

			if (!first) {
				sql.append(", ");
				sqlValues.append(", ");
			}
			first = false;
			sql.append(colName);

			ival = keyValues.get(colIndex); 
			if (ival == null) {
				sqlValues.append("NULL");
			} else {
				sqlValues.append(formatObjectToSQL(ival));
			} 
			colIndex++;
		}
		sql.append(")");
		sqlValues.append(")");
		sql.append(sqlValues.toString());
		stmt.executeUpdate(sql.toString());
	}

	private List<Object> getRow(List<String> columnNames, List<Object> columnVals, TableMergeRules tableMergeRule) throws SQLException {
		List<Object> resultRow = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder();
		
		sql.append("SELECT * FROM ");
		sql.append(DDLUtils.toQualifiedName(tableMergeRule.getSourceTable()));
		String whereStatement = generateWhereStatement(columnNames, columnVals);
		sql.append(whereStatement);
		ResultSet rs = stmt.executeQuery(sql.toString());
		if (!rs.next()) {
			throw new IllegalStateException("Record with primary key not found");
		}
		
		for (ColumnMergeRules cmr : tableMergeRule.getChildren()) {
			resultRow.add(rs.getObject(cmr.getColumnName()));
		}
		
		if (rs.next()) {
			throw new IllegalStateException("Multiple records found with same primary key");
		}
		return resultRow;
	}

	private List<Object> updateRowsUsingSQL(List<String> columnNames, List<Object> columnVals, TableMergeRules tableMergeRule, List<String> foreignKeyColumnNames, List<Object> masterKeyValues) throws SQLException {
		StringBuilder sql = new StringBuilder();
		boolean first = true;
		int colIndex = 0;
		Object ival;
		
		sql.append("UPDATE ");
		sql.append(DDLUtils.toQualifiedName(tableMergeRule.getSourceTable()));
		sql.append("\n SET ");
		for (ColumnMergeRules cmr : tableMergeRule.getChildren()) {
			if (!"".equals(cmr.getUpdateStatement())) {
				if (foreignKeyColumnNames.contains(cmr.getColumnName())) {
					throw new IllegalStateException("Can't have sql update statement for a foreign key column");
				}
				if (!first) {
					sql.append(", ");
				}
				first = false;
				sql.append(cmr.getColumnName());
				sql.append("=");
				sql.append(cmr.getUpdateStatement());
			}
		}
		for (String ColumnName : foreignKeyColumnNames) {
			if (!first) {
				sql.append(", ");
			}
			first = false;
			sql.append(ColumnName);
			sql.append("=");

			ival = masterKeyValues.get(colIndex++); 
			if (ival == null) {
				sql.append("NULL");
			} else {
				sql.append(formatObjectToSQL(ival));
			} 
		}
		
		String whereStatement = generateWhereStatement(columnNames, columnVals);
		sql.append(whereStatement);
		
		
		stmt.executeUpdate(sql.toString());
		return masterKeyValues;

	}

	private void updateRows(SQLTable table, List<String> foreignKeyColumnNames, List<Object> dupKeyValues, List<Object> masterKeyValues) throws SQLException {
		StringBuilder sql = new StringBuilder();
		int colIndex = 0;
		Object ival;
		
		sql.append("UPDATE ");
		sql.append(DDLUtils.toQualifiedName(table));
		sql.append("\n SET ");
		boolean first = true;
		for (String ColumnName : foreignKeyColumnNames) {
			if (!first) {
				sql.append(", ");
			}
			first = false;
			sql.append(ColumnName);
			sql.append("=");

			ival = masterKeyValues.get(colIndex++); 
			if (ival == null) {
				sql.append("NULL");
			} else {
				sql.append(formatObjectToSQL(ival));
			} 
		}
		String whereStatement = generateWhereStatement(foreignKeyColumnNames, dupKeyValues);
		sql.append(whereStatement);
		stmt.executeUpdate(sql.toString());
	}
	
	private List<String> getKeyColumnNames(TableMergeRules tableMergeRule) {
		List<String> childKeyColumnNames = new ArrayList<String>();
		for (ColumnMergeRules cmr : tableMergeRule.getChildren()) {
			if (cmr.isInPrimaryKey()) {
				childKeyColumnNames.add(cmr.getColumnName());
			}
		}
		return childKeyColumnNames;
	}

	private int deleteRows(SQLTable table, List<String> columnNames, List<Object> columnValues) throws SQLException {
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ");
		sql.append(DDLUtils.toQualifiedName(table));
		String whereStatement = generateWhereStatement(columnNames, columnValues);
		sql.append(whereStatement);
		return stmt.executeUpdate(sql.toString());
	}
	
	private boolean findRows(SQLTable table, List<String> columnNames, List<Object> columnValues) throws SQLException {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ");
		sql.append(DDLUtils.toQualifiedName(table));
		String whereStatement = generateWhereStatement(columnNames, columnValues);
		sql.append(whereStatement);
		ResultSet rs = stmt.executeQuery(sql.toString());
		if (rs.next()) {
			return true;
		}
		return false;
	}
	
	private List<List<Object>> getMasterKeyValues(List<String> parentKeyColumnNames, 
			List<String> childKeyColumnNames, List<String> foreignKeyColumnNames, 
			List<Object> dupKeyValues, List<Object> masterKeyValues, TableMergeRules mr, 
			List<List<Object>> childDupKeyValues) throws SQLException {
		
		List<List<Object>> result = new ArrayList<List<Object>>();
		
		
		for (List<Object> keyVals : childDupKeyValues) {
			List<Object> temp = new ArrayList<Object>();
			int count = 0;
			for (String columnName : childKeyColumnNames) {
				for (ColumnMergeRules cmr : mr.getChildren()) {
					if (columnName.equals(cmr.getColumnName())) {
						if (cmr.getImportedKeyColumn() != null) {
							//its both a foreign key and primary key, set it to the new value
							int parentColIndex = 0;
							for (String parentColumnName : parentKeyColumnNames) {
								if (parentColumnName.equals(cmr.getImportedKeyColumn().getName())) {
									temp.add(masterKeyValues.get(parentColIndex));
									break;
								}
								parentColIndex++;
							}
						}
						else {
							//its a primary key but not a foreign key
							temp.add(keyVals.get(count));
						}
						break;
					}
				}
				count++;
			}
			result.add(temp);
		}
		if (childDupKeyValues.size() != result.size()) {
			throw new IllegalStateException("Unexpected number of child master records.");
		}
		
		return result;
	}

	private List<List<Object>> getDupKeyValues(List<String> keyColumnNames, 
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
	 * @param masterKeyValues 
	 */
	private int updateSourceTableRows(PotentialMatchRecord pm,
			Map<SQLColumn, ColumnMergeRules> mapping, List<String> sourceIndexColumnNames, List<Object> masterKeyValues) 
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
					throw new UnsupportedOperationException("Mysterious action discovered!");
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
		String whereStatement = generateWhereStatement(sourceIndexColumnNames, masterKeyValues);
		sql.append(whereStatement);
		return stmt.executeUpdate(sql.toString());
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
			if (ival == null) {
				sql.append(" IS NULL");
			} else {
				sql.append("=");
				sql.append(formatObjectToSQL(ival));
			} 
			first = false;
		}
		return sql.toString();
	}

}
