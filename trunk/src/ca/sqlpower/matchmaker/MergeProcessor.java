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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
 * Implements the merge behaviour of the MatchMaker product.  The behaviour of
 * the merge is configured via the project's {@link MergeSettings} and its
 * set of {@link TableMergeRules}. Logging that is user-readable is done to
 * a special Logger instance passed in via the constructor--this log information
 * is expected to be presented to the end user of the product, so developers
 * maintaining this class should keep that fact in mind.
 */
public class MergeProcessor extends AbstractProcessor {

    /**
     * The project we're doing the merging for.
     */
	private final Project project;

	/**
	 * Stores the sets of potential matches which is used for merging the data.
	 */
	private MatchPool pool;
	
    /**
     * The list of MungeSteps obtained from the MungeProcess that this processor will
     * process, sorted in the exact order that the processor will process them.
     */
    private List<PotentialMatchRecord> pmProcessOrder;
    
    /**
     * Connection to the database that contains the source table. This connection
     * is the one given in the constructor.
     */
    private final Connection con;
    
    private Statement stmt;
    
    /**
     * The special merge rule that applies to the project's source table. This is
     * set up in {@link #initVariables()}.
     */
    private TableMergeRules sourceTableMergeRule = null;
    
    /**
     * A flag that controls an optimisation in the call() method: If none
     * of the merge rules on the source (match) table require us to see the
     * value in the duplicate row, then we don't need to retrieve the duplicate
     * row at all. In this case, needsToCheckDup is false. Otherwise, we need
     * to retrieve the duplicate row before deleting it in order to merge its
     * information into the master row.
     */
    private boolean needsToCheckDup = false;
    
    private Map<SQLColumn, ColumnMergeRules> columnMergeRuleMap = 
    	new HashMap<SQLColumn, ColumnMergeRules>();

    /**
     * This is the logger that the user sees. Its output appears on screen in
     * the Swing user interface, and the messages are also written out to a file.
     */
    private final Logger engineLogger;

    /**
     * Keeps track of how many rows have been updated in each table touched by this
     * merge processor.
     */
    private final Map<SQLTable, Integer> updateCounts = new HashMap<SQLTable, Integer>();

    /**
     * Keeps track of how many rows have been inserted in each table touched by this
     * merge processor.
     */
    private final Map<SQLTable, Integer> insertCounts = new HashMap<SQLTable, Integer>();

    /**
     * Keeps track of how many rows have been deleted in each table touched by this
     * merge processor.
     */
    private final Map<SQLTable, Integer> deleteCounts = new HashMap<SQLTable, Integer>();

    /**
     * Creates a new merge processor for the given project. To start the merge
     * processing, you call the {@link #call()} method.
     * 
     * @param project
     *            The project the merging is for.
     * @param con
     *            Database connection to the database that contains the
     *            project's match (source) table.
     * @param logger
     *            The logger that we should log end-user-visible messages to.
     * @throws SQLException
     *             If there are any problems with the database
     * @throws ArchitectException
     *             If any of the SQLObjects in play cannot populate.
     */
	public MergeProcessor(Project project, Connection con, Logger logger) 
			throws SQLException, ArchitectException {
		this.project = project;
		this.engineLogger = logger;
		this.con = con;
	}
	
	/**
	 * Call this method to run the merge engine. The avg action is 
	 * currently not supported.
	 */
	public Boolean call() throws Exception {
		
		try {
			monitorableHelper.setStarted(true);
			monitorableHelper.setFinished(false);
			
			engineLogger.info("Starting merge operation for project " + project.getName());

			stmt = con.createStatement();

			initVariables();
			
			for (PotentialMatchRecord pm : pmProcessOrder) {
                checkCancelled();
				monitorableHelper.incrementProgress();
				
				engineLogger.info(
				        " ***** Merging record " + monitorableHelper.getProgress() +
				        " of " + monitorableHelper.getJobSize() + " *****");
				
				ResultRow dupKeyValues = new ResultRow(sourceTableMergeRule, pm.getDuplicate().getKeyValues());
				ResultRow masterKeyValues = new ResultRow(sourceTableMergeRule, pm.getMaster().getKeyValues());
				
				engineLogger.debug("Duplicate record: " + dupKeyValues + "; master record: " + masterKeyValues);
				
				// Starts the recursive merging
				mergeChildTables(dupKeyValues, masterKeyValues, sourceTableMergeRule);
	
				if (needsToCheckDup) {
					engineLogger.debug("Updating source table columns according the merge actions...");
					ResultRow dupRow = findRowByPrimaryKey(sourceTableMergeRule, dupKeyValues);
					ResultRow masterRow = findRowByPrimaryKey(sourceTableMergeRule, masterKeyValues);
					int rows = mergeRows(dupRow, masterRow, sourceTableMergeRule);
					
					if (rows != 1) {
						throw new IllegalStateException("The update did not affect the correct " +
								"number of rows: expected 1 but got " + rows); 
					}
				}
				
				//delete the duplicate record
				engineLogger.debug("Deleting duplicate record: " + dupKeyValues + 
						" on table: " + sourceTableMergeRule.getSourceTable());
				int rows = deleteRowByPrimaryKey(sourceTableMergeRule.getSourceTable(), dupKeyValues);
				if (rows != 1) {
					throw new IllegalStateException("The delete did not affect the correct " +
							"number of rows: expected 1 but got " + rows); 
				}
				
				//clean up match pool
				pm.setMatchStatus(MatchType.MERGED);
				
				// delete all potential match record related to the
				// duplicate record (which is deleted). We need to add the
				// pmr to a toBeDeleted list because we can't remove it
				// from the pool as we are looping through the pool
				SourceTableRecord str = pm.getDuplicate();
				List<PotentialMatchRecord> toBeDeleted = new ArrayList<PotentialMatchRecord>();
				for (PotentialMatchRecord pmr : pool.getPotentialMatches()) {
					if (pmr.getOriginalLhs().equals(str) || pmr.getOriginalRhs().equals(str)) {
						if (pmr.getMatchStatus() != MatchType.MERGED) {
							engineLogger.debug("Removing match pool record: " + pmr);
							toBeDeleted.add(pmr);
						}
					}
				}
				for (PotentialMatchRecord pmr : toBeDeleted) {
					pool.removePotentialMatch(pmr);
				}
			}

            checkCancelled();

			pool.store();
			return Boolean.TRUE;
		} finally {
		    monitorableHelper.setFinished(true);
			stmt.close();
			engineLogger.info("\n" + getActivitySummary());
		}
		
	}
	
	private void initVariables() throws SQLException, ArchitectException {
		//Initialize the match pool
		engineLogger.info("Loading match pool...");
		pool = new MatchPool(project);
		pool.findAll(new ArrayList<SQLColumn>());
		
		engineLogger.debug("Found " + pool.getSourceTableRecords().size() + " source table records in pool");
		
		//Topological sort so that chains of matches are merged in the right order
		engineLogger.info("Sorting matches...");
		MatchPoolDirectedGraphModel gm = new MatchPoolDirectedGraphModel(pool);
        DepthFirstSearch<SourceTableRecord, PotentialMatchRecord> dfs = 
        	new DepthFirstSearch<SourceTableRecord, PotentialMatchRecord>();
        dfs.performSearch(gm);
        List<SourceTableRecord> processOrder = dfs.getFinishOrder();
        
        //Finds the correct potential match records in the correct order
        pmProcessOrder = new ArrayList<PotentialMatchRecord>();
        for (SourceTableRecord str : processOrder) {
            
            // Because of the MatchPoolDirectedGraphModel, this list will only contain edges
            // that are MATCH or AUTOMATCH; potential matches and merged records will not be in here.
        	Collection<PotentialMatchRecord> edges = gm.getOutboundEdges(str);

        	if (edges.size() > 1) {
				throw new IllegalStateException(
						"Source Table Record: " + str + " has more than one master.");
			}
        	for (PotentialMatchRecord pmr : edges) {
        		pmProcessOrder.add(pmr);
        	}
        }
        
        if (engineLogger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Matches have been sorted; " + pmProcessOrder.size() + " merge operations to process");
            sb.append("\nPlanned processing order ([duplicate key] into [master key]):");
            for (PotentialMatchRecord pmr : pmProcessOrder) {
                sb.append("\n    ").append(pmr.getDuplicate().getKeyValues())
                .append(" into ").append(pmr.getMaster().getKeyValues());
            }
            engineLogger.debug(sb.toString());
        }
        
        // Sets the jobsize
        Integer recordsToProcess = project.getMergeSettings().getProcessCount();
		if (recordsToProcess != null && recordsToProcess > 0 && pmProcessOrder.size() > recordsToProcess) {
		    engineLogger.debug("Truncating processing list to user setting of " + recordsToProcess + " items.");
		    pmProcessOrder = pmProcessOrder.subList(0, recordsToProcess);
		}
		monitorableHelper.setJobSize(pmProcessOrder.size());
		
		// Finds the correct table merge rule according to the source table
        sourceTableMergeRule = project.getTableMergeRules().get(0);
        if (!sourceTableMergeRule.isSourceMergeRule()) {
        	throw new IllegalStateException(
        			"The first merge rule needs to be the source table merge rule.");
        }
        
        StringBuilder mergeRuleMessage = new StringBuilder();
        if (engineLogger.isDebugEnabled()) {
            mergeRuleMessage.append("Merge rules for the source table's columns:");
        }
        
		// Finds the columns that needs to be merged and maps it to the 
		// corresponding column merge rule.
		for (ColumnMergeRules cmr : sourceTableMergeRule.getChildren()) {
		    if (engineLogger.isDebugEnabled()) {
		        mergeRuleMessage.append(
		            String.format("\n    %-40s %s",
		                    cmr.getColumnName(),
		                    cmr.getActionType()));
		    }
			if (cmr.getActionType() != MergeActionType.USE_MASTER_VALUE 
					&& cmr.getActionType() != MergeActionType.NA) {
				needsToCheckDup = true;
			}
			columnMergeRuleMap.put(cmr.getColumn(), cmr);
		}
		
		if (engineLogger.isDebugEnabled()) {
		    engineLogger.debug(mergeRuleMessage.toString());
		    if (!needsToCheckDup) {
		        engineLogger.debug(": " + needsToCheckDup);
		    }
		}
	}
	
    /**
     * Logs a message to the engine log (DEBUG level) and also increments the
     * update count for the given row's table. You should call this just after
     * executing the statement that performs the operation.
     * 
     * @param updatedRow The row that is just about to be updated.
     * @param count The number of rows updated.
     */
    private void logUpdate(SQLTable table, int count) {
        engineLogger.debug("Modified " + count + " row(s) of " + DDLUtils.toQualifiedName(table));
        Integer updateCount = updateCounts.get(table);
        if (updateCount == null) {
            updateCount = count;
        } else {
            updateCount += count;
        }
        updateCounts.put(table, updateCount);
    }

    /**
     * Logs a message to the engine log (DEBUG level) and also increments the
     * insert count for the given row's table. You should call this just after
     * executing the statement that performs the operation.
     * 
     * @param insertedRow The row that is just about to be inserted.
     * @param count The number of rows inserted.
     */
    private void logInsert(SQLTable table, int count) {
        engineLogger.debug("Inserted " + count + " row(s) into " + DDLUtils.toQualifiedName(table));
        Integer insertCount = insertCounts.get(table);
        if (insertCount == null) {
            insertCount = count;
        } else {
            insertCount += count;
        }
        insertCounts.put(table, insertCount);
    }
    
    /**
     * Logs a message to the engine log (DEBUG level) and also increments the
     * delete count for the given row's table.
     * 
     * @param deletedRow The row that is just about to be deleted.
     * @param count The number of rows deleted.
     */
    private void logDelete(SQLTable table, int count) {
        engineLogger.debug("Deleted " + count + " row(s) from " + DDLUtils.toQualifiedName(table));
        Integer deleteCount = deleteCounts.get(table);
        if (deleteCount == null) {
            deleteCount = count;
        } else {
            deleteCount += count;
        }
        deleteCounts.put(table, deleteCount);
    }

    /**
     * Generates a multi-line activity summary string which can be sent to the log.
     * This summary includes update, insert, and delete counts for all affected tables.
     * If no tables were affected since this processor was created, this method prints
     * a short message to that effect instead of the table.
     */
    private String getActivitySummary() {
        
        // Using a map because we want alphabetical order by name of
        // the union of the tables in these three other maps.
        // But we also need the original SQLTable objects so we can go back
        // and get the counts when creating the table of values.
        Map<String, SQLTable> affectedTables = new TreeMap<String, SQLTable>();
        for (SQLTable t : deleteCounts.keySet()) {
            affectedTables.put(DDLUtils.toQualifiedName(t), t);
        }
        for (SQLTable t : updateCounts.keySet()) {
            affectedTables.put(DDLUtils.toQualifiedName(t), t);
        }
        for (SQLTable t : insertCounts.keySet()) {
            affectedTables.put(DDLUtils.toQualifiedName(t), t);
        }
        
        if (affectedTables.size() == 0) {
            return "No tables were affected";
        }
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("Summary of modifications by table:\n");
        
        // format the summary table
        sb.append(String.format("%-50s %6s %6s %6s\n", "Table", "Update", "Delete", "Insert"));
        
        for (Map.Entry<String, SQLTable> ent : affectedTables.entrySet()) {
            String name = ent.getKey();
            SQLTable t = ent.getValue();
            int updates = updateCounts.get(t) == null ? 0 : updateCounts.get(t);
            int deletes = deleteCounts.get(t) == null ? 0 : deleteCounts.get(t);
            int inserts = insertCounts.get(t) == null ? 0 : insertCounts.get(t);
            sb.append(String.format("%-50s %6d %6d %6d\n", name, updates, deletes, inserts));
        }
        
        return sb.toString();
    }
    
	/**
	 * Merges the child tables recursively, in an order that is safe given the foreign
	 * key constraints between the child tables.
	 */
	private void mergeChildTables(ResultRow parentDupRow, 
			ResultRow parentMasterRow, 
			TableMergeRules parentTableMergeRule) 
	throws ArchitectException, SQLException {
		
		engineLogger.debug("Merging child records of " + parentTableMergeRule.getTableName() + " ...");
		engineLogger.debug("Duplicate: " + 
		        (parentDupRow == null ? "none specified" : parentDupRow.values) +
		        "; Master: " +
		        (parentMasterRow == null ? "none specified" : parentMasterRow.values));

		try {
		    for (TableMergeRules childTableMergeRule : project.getTableMergeRules()) {
		        if (parentTableMergeRule != childTableMergeRule.getParentMergeRule()) {
		            continue;
		        }
		        
				// populates the data required to merge the grand child tables	
				List<ResultRow> childDupRows = findChildRowsByParentRow(parentDupRow, childTableMergeRule);
				List<ResultRow> childMasterRows = new ArrayList<ResultRow>();
				
				engineLogger.debug("Child table " + childTableMergeRule.getTableName() + " has "+childDupRows.size()+" child records");
				engineLogger.debug("Merge Action is: " + childTableMergeRule.getChildMergeAction().toString());
	
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
							"Merge Failed: Multiple records in table '" + childTableMergeRule.getTableName() + "' with the same primary key (" 
								+ row.tableMergeRule.getPrimaryKey()  + ") would be created on update.");
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
				engineLogger.debug("Merging duplicate's child records on table " + childTableMergeRule.getSourceTable());
				for (int i = 0; i < childDupRows.size(); i++) {
					mergeChildTables(childDupRows.get(i), childMasterRows.get(i), childTableMergeRule);
				}
	
				// Delete the duplicate child records
				engineLogger.debug("Deleting duplicate's child records on table " + childTableMergeRule.getSourceTable());
				deleteRowsByForeignKey(childTableMergeRule, parentDupRow);
			} 
		} finally {
		    engineLogger.debug("Finished merging on table " + parentTableMergeRule.getTableName());
		}
	}

	private int deleteRowByPrimaryKey(SQLTable table, ResultRow row) throws SQLException, ArchitectException {
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ");
		sql.append(DDLUtils.toQualifiedName(table));
		sql.append(generateWhereStatement(row));
		int count = stmt.executeUpdate(sql.toString());
		logDelete(table, count);
        return count;
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
		engineLogger.debug(sql.toString());
		int count = stmt.executeUpdate(sql.toString());
		logDelete(tableMergeRule.getSourceTable(), count);
        return count;
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
		String whereStatement = generateWhereStatement(row);
		if (whereStatement.length() == 0) return null;
		sql.append(whereStatement);
		engineLogger.debug("MergeProcessor.findRowByPrimaryKey: Executing SQL Statement: " + sql.toString());
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
		int count = stmt.executeUpdate(sql.toString());
		logInsert(table, count);
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
			int count = stmt.executeUpdate(sql.toString());
			logUpdate(tableMergeRules.getSourceTable(), count);
            return count;
		} else {
			return 1;
		}
	}
	
	/**
	 * Returns a WHERE clause for use in a SQL statement to find the specific
	 * row given by primary key of the given parameter 'row'.
	 * 
	 * @param row
	 *            The specific row for which we are trying to form a WHERE
	 *            clause to find based on its primary key.
	 * @return The WHERE clause in String form. If the given row does not have
	 *         any columns in the primary key, then it returns an empty String.
	 * @throws SQLException
	 * @throws ArchitectException
	 */
	private String generateWhereStatement(ResultRow row) throws SQLException, ArchitectException {
		boolean first = true;
		StringBuilder sql = new StringBuilder();
		for (int i = 0; i < row.size(); i ++) {
			if (row.isInPrimaryKey(i)) {
				if (!first) {
					sql.append(" AND ");
				} else {
					sql.append("\n WHERE ");
					first = false;
				}
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
		
		/**
		 * Check if the column specified with the given index number is a
		 * primary key column.
		 * 
		 * @param column
		 *            An int representing the index of the column we are
		 *            checking.
		 * @return Returns true if the ColumnMergeRule with the given index is
		 *         for a column in the primary key. Otherwise returns false;
		 * @throws ArchitectException
		 */
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
		
		/**
		 * Creates a copy of this Result Row. The actual data values are shared
		 * with the copy, but the list structure is not.
		 */
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
