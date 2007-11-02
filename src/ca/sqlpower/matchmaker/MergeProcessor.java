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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
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

	private static final Logger logger = Logger.getLogger(MergeProcessor.class);
    
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
    private List<PotentialMatchRecord> pmProcessOrder;
    
    private MatchPoolDirectedGraphModel gm;
    
    private SQLTable sourceTable;
    private Connection con;
    private Statement stmt;
    
    private final Logger engineLogger;
    
	public MergeProcessor(Project project, MatchMakerSession session, Logger logger) 
			throws SQLException, ArchitectException {
		this.project = project;
		this.engineLogger = logger;
		
		pool = new MatchPool(project);
		
		//Initialize the match pool
		pool.findAll(new ArrayList<SQLColumn>());
		
		//Topological sort
        gm = new MatchPoolDirectedGraphModel(pool);
        DepthFirstSearch<SourceTableRecord, PotentialMatchRecord> dfs = new DepthFirstSearch<SourceTableRecord, PotentialMatchRecord>();
        dfs.performSearch(gm);
        processOrder = dfs.getFinishOrder();
        
        pmProcessOrder = new ArrayList<PotentialMatchRecord>();
        for (SourceTableRecord str : processOrder) {
        	Collection<PotentialMatchRecord> edges = gm.getOutboundEdges(str);
        	if (edges.size() > 1) {
				throw new IllegalStateException("Source Table Record: " + str + " has more than one master.");
			}
        	for (PotentialMatchRecord pmr : edges) {
        		pmProcessOrder.add(pmr);
        	}
        }
        engineLogger.debug("Order of processing: " + processOrder);
        
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
		int recordsToProcess;
		if (project.getMergeSettings().getProcessCount() == null) {
			recordsToProcess = 0;
		} else {
			recordsToProcess = project.getMergeSettings().getProcessCount();
		}
		int processCount = 0;
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
			if (cmr.getActionType() != MergeActionType.USE_MASTER_VALUE && cmr.getActionType() != MergeActionType.NA) {
				needsToCheckDup = true;
			}
			mapping.put(cmr.getColumn(), cmr);
		}
		
		if (recordsToProcess == 0) {
			int rowCount = 0;
			for (SourceTableRecord str: processOrder) {
				rowCount += gm.getOutboundEdges(str).size();
			}
			monitorableHelper.setJobSize(rowCount);
		} else {
			monitorableHelper.setJobSize(recordsToProcess);
		}
		
		engineLogger.info("Merging records.");
		for (PotentialMatchRecord pm : pmProcessOrder) {
			if (recordsToProcess != 0 && processCount > recordsToProcess) break;
			if (monitorableHelper.isCancelled()) return Boolean.TRUE;
			processCount++;
			monitorableHelper.incrementProgress();
			
			ResultRow dupKeyValues = new ResultRow(sourceTableMergeRule, pm.getDuplicate().getKeyValues());
			ResultRow masterKeyValues = new ResultRow(sourceTableMergeRule, pm.getMaster().getKeyValues());
			
			// Starts the recursive merging
			mergeChildTables(dupKeyValues, masterKeyValues, sourceTableMergeRule);

			if (needsToCheckDup) {
				engineLogger.debug("Updating source table columns according the merge actions...");
				ResultRow dupRow = findRowByPrimaryKey(sourceTableMergeRule, dupKeyValues);
				ResultRow masterRow = findRowByPrimaryKey(sourceTableMergeRule, masterKeyValues);
				//int rows = updateSourceTableRows(pm, mapping, sourceIndexColumnNames, masterKeyValues);
				int rows = mergeRows(dupRow, masterRow, sourceTableMergeRule);
				
				if (rows != 1) {
					throw new IllegalStateException("The update did not affect the correct " +
							"number of rows: expected 1 but got " + rows); 
				}
			}
		}
		
		
		
		//delete duplicates
		processCount = 0;
		if (sourceTableMergeRule.isDeleteDup()) {
			engineLogger.info("Deleting duplicate records.");
			for (PotentialMatchRecord pm : pmProcessOrder) {
				if (recordsToProcess != 0 && processCount > recordsToProcess) break;
				if (monitorableHelper.isCancelled()) return Boolean.TRUE;
				processCount++;
				ResultRow dupKeyValues = new ResultRow(sourceTableMergeRule, pm.getDuplicate().getKeyValues());
				
				engineLogger.debug("Deleting duplicate record: " + dupKeyValues + " on table: " + sourceTable);
				//delete the duplicate record
				int rows = deleteRowByPrimaryKey(sourceTable, dupKeyValues);
				
				if (rows != 1) {
					throw new IllegalStateException("The delete did not affect the correct " +
						"number of rows: expected 1 but got " + rows); 
				}
			}
		}
		
		//clean up match pool
		processCount = 0;
		engineLogger.info("Cleaning up the match pool.");
		List<PotentialMatchRecord> toBeDeleted = new ArrayList<PotentialMatchRecord>();
		for (PotentialMatchRecord pm : pmProcessOrder) {
			pm.setMatchStatus(MatchType.MERGED);
			SourceTableRecord str = pm.getDuplicate();
			for (PotentialMatchRecord pmr : pool.getPotentialMatches()) {
				if (monitorableHelper.isCancelled()) return Boolean.TRUE;
				//checks if the potential match record contains the duplicate record
				if (pmr.getOriginalLhs().equals(str) || pmr.getOriginalRhs().equals(str)) {
					if (pmr.getMatchStatus() != MatchType.MERGED) {
						toBeDeleted.add(pmr);
					}
				}
			}
			
		}
		
		for (PotentialMatchRecord pm : toBeDeleted) {
			if (monitorableHelper.isCancelled()) return Boolean.TRUE;
			engineLogger.debug("Removing match pool record: " + pm);
			pool.removePotentialMatch(pm);
		}
		
		pool.store();
		stmt.close();
		con.close();
		monitorableHelper.setFinished(true);
		return Boolean.TRUE;
	}
	
	/**
	 * Merge the child tables recursively.
	 * <p>
	 * Note that the keyValues must be in the ordered by the same order of the columns
	 */
	private void mergeChildTables(ResultRow parentDupRow, 
			ResultRow parentMasterRow, 
			TableMergeRules parentTableMergeRule) 
	throws ArchitectException, SQLException {
		
		engineLogger.debug("Merging duplicate record: " + parentDupRow + " into master record: " + parentMasterRow + " on table: " + parentTableMergeRule.getSourceTable());
		SQLTable parentTable = parentTableMergeRule.getSourceTable();
		
		for (TableMergeRules childTableMergeRule : project.getTableMergeRules()) {
			if (parentTable.equals(childTableMergeRule.getParentTable())) {
				
				// populates the data required to merge the grand child tables	
				List<ResultRow> childDupRows = findChildRowsByParentRow(parentDupRow, childTableMergeRule);
				List<ResultRow> childMasterRows = new ArrayList<ResultRow>();
					
				engineLogger.debug("Merge Strategy is: " + childTableMergeRule.getChildMergeAction().toString());
	
				if (childTableMergeRule.getChildMergeAction() == 
					TableMergeRules.ChildMergeActionType.DELETE_ALL_DUP_CHILD) {
					
					for (int i = 0; i < childDupRows.size(); i++) {
						childMasterRows.add(null);
					}
					
				} else if (childTableMergeRule.getChildMergeAction() == 
					TableMergeRules.ChildMergeActionType.UPDATE_FAIL_ON_CONFLICT) {
	
					engineLogger.debug("Creating a copy of the duplicate record's children on table " + childTableMergeRule.getSourceTable());
					
					// cannot update if there are no masterKeyValues
					if (parentMasterRow == null) {
						throw new IllegalStateException(
						"Cannot update when parent table deletes all child duplicate records");
					}
	
					// populates the master child table records
					for (ResultRow row : childDupRows) {
						ResultRow temp = row.duplicate();
						for(ColumnMergeRules cmr : childTableMergeRule.getImportedKey()) {
							Object masterVal = parentMasterRow.getValue(cmr.getImportedKeyColumn().getName());
							temp.setValue(cmr.getColumnName(), masterVal);
						}
						childMasterRows.add(temp);
					}
	
					// Try to see if creating a copy of the old record with the new key values would succeed
					for (ResultRow row : childMasterRows) {
						ResultRow masterRow = findRowByPrimaryKey(childTableMergeRule, row);
						if (masterRow != null) {
							//fail on conflict
							throw new IllegalStateException(
							"Merge Failed: Multiple records with the same primary key created on update.");
						}
					}
	
					// Creates a copy of the old record with the new key values
					for (ResultRow row : childMasterRows) {
						insertRow(childTableMergeRule.getSourceTable(), row);
					}
	
	
				} else if (childTableMergeRule.getChildMergeAction() == 
					TableMergeRules.ChildMergeActionType.UPDATE_DELETE_ON_CONFLICT) {
	
					engineLogger.debug("Creating a copy of the duplicate record's children on table " + childTableMergeRule.getSourceTable());
	
					// cannot update if there are no masterKeyValues
					if (parentMasterRow == null) {
						throw new IllegalStateException(
								"Cannot update when parent table deletes all child duplicate records");
					}
	
					// populates the master child table records
					for (ResultRow row : childDupRows) {
						ResultRow temp = row.duplicate();
						for(ColumnMergeRules cmr : childTableMergeRule.getImportedKey()) {
							Object masterVal = parentMasterRow.getValue(cmr.getImportedKeyColumn().getName());
							temp.setValue(cmr.getColumnName(), masterVal);
						}
						childMasterRows.add(temp);
					}
					
					// Try to see if the update would succeed
					for (ResultRow row : childMasterRows) {
						ResultRow masterRow = findRowByPrimaryKey(childTableMergeRule, row);
						if (masterRow == null) {
							insertRow(childTableMergeRule.getSourceTable(), row);
						}
					}
	
				}  else if (childTableMergeRule.getChildMergeAction() == TableMergeRules.ChildMergeActionType.UPDATE_USING_SQL) {
	
					engineLogger.debug("Creating a copy of the duplicate record's children on table " + childTableMergeRule.getSourceTable());
	
					// cannot update if there are no masterKeyValues
					if (parentMasterRow == null) {
						throw new IllegalStateException(
								"Cannot update when parent table deletes all child duplicate records");
					}
					
					// populates the master child table records
					for (ResultRow row : childDupRows) {
						ResultRow temp = row.duplicate();
	
						for (ColumnMergeRules cmr : childTableMergeRule.getChildren()) {
							if (cmr.getImportedKeyColumn() != null) {
								Object masterVal = parentMasterRow.getValue(cmr.getImportedKeyColumn().getName());
								temp.setValue(cmr.getColumnName(), masterVal);
							} else if (cmr.getUpdateStatement() != null && cmr.getUpdateStatement().length() != 0) {
								ResultSet rs = findUpdateValueByPrimaryKey(childTableMergeRule.getSourceTable(), 
										cmr.getUpdateStatement(), row);
								if (!rs.next()) {
									throw new IllegalStateException("Invalid SQL Update Statement from merge.");
								} else {
									temp.setValue(cmr.getColumnName(), rs.getObject(1));
									if (rs.next()) {
										throw new IllegalStateException("Multiple rows with the same primary key.");
									}
								}
							}
						}
						childMasterRows.add(temp);
						insertRow(childTableMergeRule.getSourceTable(), temp);
					}
					
				}  else if (childTableMergeRule.getChildMergeAction() == TableMergeRules.ChildMergeActionType.MERGE_ON_CONFLICT) {
	
					engineLogger.debug("Creating a copy of the duplicate record's children on table " + childTableMergeRule.getSourceTable());
	
					// cannot update if there are no masterKeyValues
					if (parentMasterRow == null) {
						throw new IllegalStateException(
								"Cannot merge when parent table deletes all child duplicate records");
					}
					// populates the master child table records
					for (ResultRow row : childDupRows) {
						ResultRow temp = row.duplicate();
						for(ColumnMergeRules cmr : childTableMergeRule.getImportedKey()) {
							Object masterVal = parentMasterRow.getValue(cmr.getImportedKeyColumn().getName());
							temp.setValue(cmr.getColumnName(), masterVal);
						}
						childMasterRows.add(temp);
					}
					
					// Try to see if the update would succeed
					for (int i = 0; i < childDupRows.size(); i++) {
						ResultRow masterRow = findRowByPrimaryKey(childTableMergeRule, childMasterRows.get(i));
						if (masterRow == null) {
							insertRow(childTableMergeRule.getSourceTable(), childMasterRows.get(i));
						} else {
							int rowsCount = mergeRows(childDupRows.get(i), masterRow, childTableMergeRule);
							if (rowsCount != 1) {
								throw new IllegalStateException("The update did not affect the correct " +
										"number of rows: expected 1 but got " + rowsCount);
							}
						}
					}
				}
				
				// Recursively merge all the grand child tables
				engineLogger.debug("Merging duplicate's child reocrds on table " + childTableMergeRule.getSourceTable());
				for (int i = 0; i < childDupRows.size(); i++) {
					mergeChildTables(childDupRows.get(i), childMasterRows.get(i), childTableMergeRule);
				}
	
				// Delete the duplicate child records
				engineLogger.debug("Deleting duplicate's child reocrds on table " + childTableMergeRule.getSourceTable());
				deleteRowsByForeignKey(childTableMergeRule, parentDupRow);
			} 
	
			
	
			
		}
	}

	private int deleteRowByPrimaryKey(SQLTable table, ResultRow row) throws SQLException, ArchitectException {
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ");
		sql.append(DDLUtils.toQualifiedName(table));
		sql.append(generateWhereStatement(row));
		return stmt.executeUpdate(sql.toString());
	}


	private int deleteRowsByForeignKey(TableMergeRules tableMergeRule, ResultRow foreignKeyValues) throws SQLException, ArchitectException {
		StringBuilder sql = new StringBuilder();
		boolean first = true;
		
		sql.append("DELETE FROM ");
		sql.append(DDLUtils.toQualifiedName(tableMergeRule.getSourceTable()));
		
		sql.append("\n WHERE ");
		for (ColumnMergeRules column : tableMergeRule.getImportedKey()) {
			if (!first) sql.append(" AND ");
			first = false;
			sql.append(column.getColumnName());
			Object ival = foreignKeyValues.getValue(column.getImportedKeyColumn().getName());
			if (ival == null) {
				sql.append(" IS NULL");
			} else {
				sql.append("=");
				sql.append(formatObjectToSQL(ival));
			} 
		}
		return stmt.executeUpdate(sql.toString());
	}

	private ResultSet findUpdateValueByPrimaryKey(SQLTable table,
			String updateStatement, ResultRow row) throws SQLException, ArchitectException {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT (");
		sql.append(updateStatement);
		sql.append(")\n FROM ");
		sql.append(DDLUtils.toQualifiedName(table));
		sql.append(generateWhereStatement(row));
		return stmt.executeQuery(sql.toString());
	}

	private ResultRow findRowByPrimaryKey(TableMergeRules tableMergeRule, ResultRow row) throws SQLException, ArchitectException {
		StringBuilder sql = new StringBuilder();
		boolean first = true;
		sql.append("SELECT ");
		for (ColumnMergeRules cmr : tableMergeRule.getChildren()) {
			if (!first) sql.append(", ");
			first = false;
			sql.append(cmr.getColumnName());
		}
		sql.append(" FROM ");
		sql.append(DDLUtils.toQualifiedName(tableMergeRule.getSourceTable()));
		sql.append(generateWhereStatement(row));
		ResultSet rs = stmt.executeQuery(sql.toString());
		if (rs.next()) {
			ResultRow result = new ResultRow(tableMergeRule, rs);
			if (rs.next()) {
				throw new IllegalStateException("Multiple rows with the same primary key.");
			}
			return result;
		} else {
			return null;
		}
	}

	private List<ResultRow> findChildRowsByParentRow(
			ResultRow foreignKeyValues, 
			TableMergeRules tableMergeRule) throws SQLException, ArchitectException {
		
		
		boolean first = true;
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		for (ColumnMergeRules cmr : tableMergeRule.getChildren()) {
			if (!first) sql.append(", ");
			first = false;
			sql.append(cmr.getColumnName());
		}
		sql.append(" FROM ");
		sql.append(DDLUtils.toQualifiedName(tableMergeRule.getSourceTable()));
		sql.append("\n WHERE ");
		first = true;
		for (ColumnMergeRules column : tableMergeRule.getImportedKey()) {
			if (!first) sql.append(" AND ");
			first = false;
			sql.append(column.getColumnName());
			Object ival = foreignKeyValues.getValue(column.getImportedKeyColumn().getName());
			if (ival == null) {
				sql.append(" IS NULL");
			} else {
				sql.append("=");
				sql.append(formatObjectToSQL(ival));
			} 
		}
		ResultSet rs = stmt.executeQuery(sql.toString());
		
		List<ResultRow> result = new ArrayList<ResultRow>();
		while (rs.next()) {
			ResultRow tempRow = new ResultRow(tableMergeRule, rs);
			result.add(tempRow);
		}
		return result;
	}

	private void insertRow(SQLTable table, ResultRow row) throws SQLException {
		StringBuilder sql = new StringBuilder();
		StringBuilder sqlValues = new StringBuilder();
		boolean first = true;
		Object ival;
		
		sql.append("INSERT INTO ");
		sql.append(DDLUtils.toQualifiedName(table));
		sql.append("\n (");
		sqlValues.append("\n VALUES (");
		for (int i = 0; i < row.size(); i++) {
			if (!first) {
				sql.append(", ");
				sqlValues.append(", ");
			}
			first = false;
			sql.append(row.getColumnName(i));
			ival = row.getValue(i);
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

	private int mergeRows(ResultRow dupRowValues, ResultRow masterRowValues,
			TableMergeRules tableMergeRules) throws SQLException, ArchitectException {
		boolean first = true;

		//builds the update sql
		StringBuilder sql = new StringBuilder();

		sql.append("UPDATE ");
		sql.append(DDLUtils.toQualifiedName(tableMergeRules.getSourceTable()));
		sql.append("\n SET ");
		for (ColumnMergeRules cmr : tableMergeRules.getChildren()) {
			Object dupVal = dupRowValues.getValue(cmr.getColumnName());
			Object masterVal = masterRowValues.getValue(cmr.getColumnName());
			Object resultVal = null;
			if (cmr == null) {
				throw new IllegalStateException("Column merge rule cannot be null");
			}
			if (cmr.getActionType() != MergeActionType.USE_MASTER_VALUE && cmr.getActionType() != MergeActionType.NA) {
				
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

				sql.append(cmr.getColumnName() + "=");
				if (cmr.getActionType().equals(MergeActionType.SUM)) {
					sql.append(resultVal.toString());
				} else if (resultVal == null) {
					sql.append("null");
				} else {
					sql.append(formatObjectToSQL(resultVal));
				} 
			}
		}
		if (!first) {
			String whereStatement = generateWhereStatement(masterRowValues);
			sql.append(whereStatement);
			return stmt.executeUpdate(sql.toString());
		} else {
			return 1;
		}
	}
	
	private String generateWhereStatement(ResultRow row) throws SQLException, ArchitectException {
		boolean first = true;
		StringBuilder sql = new StringBuilder();
		sql.append("\n WHERE ");
		for (int i = 0; i < row.size(); i ++) {
			if (row.isInPrimaryKey(i)) {
				if (!first) sql.append(" AND ");
				first = false;
				sql.append(row.getColumnName(i));
				Object ival = row.getValue(i);
				if (ival == null) {
					sql.append(" IS NULL");
				} else {
					sql.append("=");
					sql.append(formatObjectToSQL(ival));
				} 
			}
		}
		return sql.toString();
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
	
	/**
	 * A row on the table
	 */
	private class ResultRow {
		private List<Object> values;
		private final TableMergeRules tableMergeRule;
		
		public ResultRow(TableMergeRules tmr, ResultSet rs) throws SQLException {
			tableMergeRule = tmr;
			values = new ArrayList<Object>();
			for (int i = 0; i < tableMergeRule.getChildCount(); i++) {
				values.add(rs.getObject(i+1));
			}		
		}
		
		public ResultRow(TableMergeRules tmr, List<Object> primaryKeyValue) throws Exception {
			this(tmr);
			List<SQLColumn> primaryKeyColumn = tmr.getPrimaryKey();
			int count = 0;
			if (primaryKeyColumn.size() != primaryKeyValue.size()) {
				throw new IllegalStateException("Primary keys columns and primary key values have different size");
			}
			for (SQLColumn column : primaryKeyColumn) {
				setValue(column.getName(), primaryKeyValue.get(count++));
			}
		}
		
		public ResultRow(TableMergeRules tmr) throws SQLException {
			tableMergeRule = tmr;
			values = new ArrayList<Object>();
			for (int i = 0; i < tableMergeRule.getChildCount(); i++) {
				values.add(null);
			}
		}
		
		public void setValue(int column, Object value) {
			values.set(column, value);
		}
		
		public void setValue(String columnName, Object value) {
			int column = 0;
			for (ColumnMergeRules cmr : tableMergeRule.getChildren()) {
				if (cmr.getColumnName().equals(columnName)) {
					setValue(column, value);
					return;
				}
				column++;
			}
			throw new IllegalStateException("Invalid column name");
		}
		
		
		public Object getValue(int column) {
			return values.get(column);
		}
		
		public Object getValue(String columnName) {
			int column = 0;
			for (ColumnMergeRules cmr : tableMergeRule.getChildren()) {
				if (cmr.getColumnName().equals(columnName)) {
					return values.get(column);
				}
				column++;
			}
			return null;
		}
		
		public boolean isInPrimaryKey(int column) throws ArchitectException {
			ColumnMergeRules cmr = tableMergeRule.getChildren().get(column);
			if (tableMergeRule.isSourceMergeRule()) {
				SQLColumn temp = cmr.getColumn();
				return tableMergeRule.getPrimaryKey().contains(temp);
			} else {
				return cmr.isInPrimaryKey();
			}
		}
		
		public int size() {
			return values.size();
		}
		
		public String getColumnName(int index) {
			return tableMergeRule.getChildren().get(index).getColumnName();
		}
		
		public ResultRow duplicate() throws SQLException {
			ResultRow result =  new ResultRow(tableMergeRule);
			for (int i = 0; i < values.size(); i++) {
				result.setValue(i, getValue(i));
			}
			return result;
		}
		
		public String toString(){
			StringBuilder temp = new StringBuilder();
			boolean first = true;
			temp.append("[");
			for (int i = 0; i < values.size(); i++) {
				if (values.get(i) != null) {
					if (!first) temp.append(", ");
					first = false;
					temp.append(tableMergeRule.getChildren().get(i).getColumnName());
					temp.append("=");
					temp.append(values.get(i));
				} 
			}
			temp.append("]");
			return temp.toString();
		}
	}

}