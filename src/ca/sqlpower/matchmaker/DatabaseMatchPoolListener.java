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

import java.beans.PropertyChangeEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;
import ca.sqlpower.matchmaker.PotentialMatchRecord.StoreState;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.sql.SQL;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.util.TransactionEvent;

public class DatabaseMatchPoolListener implements SPListener {

	private static Logger logger = Logger.getLogger(DatabaseMatchPoolListener.class);
	
	private MatchPool pool;
	private SQLTable resultTable;
	private Connection con;
	private String lastSQL;
	private PreparedStatement ps;
    private StringBuilder sql;
    private int numKeyValues;
    
    /**
     * This flag tells us whether or not we are in the middle of a large operation like resetting the pool
     * and if so, we will not want to call the store() function until it is over.
     */
    private boolean storeNow = true;
    
	/**
	 * This listens to any changes made by the user on the match pool and the match records
	 * and will store these changes to the result table in the target database. This
	 * is only to be used in the community edition of DQguru.
	 * @param pool The match pool this listener is attached to.
	 */
	public DatabaseMatchPoolListener(MatchPool pool) {
	    this.pool = pool;
	}
	
	@Override
	public void childAdded(SPChildEvent e) {
		if (e.getSource().equals(pool)) {
			e.getChild().addSPListener(this);
		} else if (e.getSource().getClass().equals(SourceTableRecord.class)) {
			if(e.getChildType().equals(PotentialMatchRecord.class)) {
				e.getChild().addSPListener(this);
			}
		}
	}

	@Override
	public void childRemoved(SPChildEvent e) {
		if (e.getSource().equals(pool)) {
			e.getChild().removeSPListener(this);
		} else if (e.getSource().getClass().equals(SourceTableRecord.class)) {
			if(e.getChildType().equals(PotentialMatchRecord.class)) {
				e.getChild().removeSPListener(this);
			}
		}
	}

	@Override
	public void propertyChanged(PropertyChangeEvent evt) {
		if(storeNow && evt.getSource().getClass().equals(PotentialMatchRecord.class)) {
			store();
		}
	}

	@Override
	public void transactionStarted(TransactionEvent e) {
		storeNow = false;
	}

	@Override
	public void transactionEnded(TransactionEvent e) {
		store();
		storeNow = true;
	}
	
	public void store() {
		try { 
	        pool.setProgress(0);
	        pool.setCancelled(false);
	        pool.setFinished(false);
	        
			final List<PotentialMatchRecord> potentialMatchRecords = pool.getPotentialMatchRecords();
			
		    final List<SourceTableRecord> sourceTableRecords = pool.getAllSourceTableRecords();
		    
		    final List<PotentialMatchRecord> deletedMatchRecords = new ArrayList<PotentialMatchRecord>();

		    for(PotentialMatchRecord pmr : potentialMatchRecords) {
		    	if(pmr.getMatchStatus().equals(MatchType.DELETE) || pmr.getMatchStatus().equals(MatchType.MERGED)) {
		    		deletedMatchRecords.add(pmr);
		    	}
		    }
		    for(PotentialMatchRecord pmr : deletedMatchRecords) {
	    		potentialMatchRecords.remove(pmr);
		    }

/*		    int sum = 0;
		    
		    List<PotentialMatchRecord> checkForDup = new ArrayList<PotentialMatchRecord>(); 
		    for(PotentialMatchRecord pmr : potentialMatchRecords) {
		    	int n = checkForDup.indexOf(pmr);
		    	if(n != -1) {
		    		sum++;
		    		PotentialMatchRecord dup = checkForDup.get(n);
		    		logger.debug("Duplicate");
		    	} else {
		    		checkForDup.add(pmr);
		    	}
		    }*/
		    
		    resultTable = pool.getProject().getResultTable();
	        con = null;
	        lastSQL = null;
	        ps = null;
			con = pool.getProject().createResultTableConnection();
			if(sourceTableRecords == null || sourceTableRecords.size() == 0) {
				return;
			}
	        numKeyValues = ((SourceTableRecord)sourceTableRecords.toArray()[0]).getKeyValues().size();
            boolean supportsBatchUpdates = pool.getProject().getMungeSettings().isUseBatchExecution()
            	&& con.getMetaData().supportsBatchUpdates();
            con.setAutoCommit(false);
            sql = new StringBuilder();
            sql.append("DELETE FROM ").append(DDLUtils.toQualifiedName(resultTable));
            sql.append("\n WHERE ");
            for (int i = 0;; i++) {
            	sql.append("DUP_CANDIDATE_1" + i + "=?");
            	sql.append(" AND DUP_CANDIDATE_2" + i + "=?");
            	if (i + 1 >= numKeyValues) break;
            	sql.append(" AND ");
            }
            lastSQL = sql.toString();
            logger.debug("The SQL statement we are running is " + lastSQL);
            
            ps = con.prepareStatement(lastSQL);
            
            int batchCount = 0;
            for (Iterator<PotentialMatchRecord> it = deletedMatchRecords.iterator(); it.hasNext(); ) {
            	pool.incrementProgress();
            	PotentialMatchRecord pmr = it.next();
            	logger.debug("Dropping " + pmr + " from the database.");
            	for (int i = 0; i < numKeyValues; i++) {
            		ps.setObject(i * 2 + 1, pmr.getOrigLHS().getKeyValues().get(i));
            		logger.debug("Param " + (i * 2 + 1) + ": " + pmr.getOrigRHS().getKeyValues().get(i));
            		ps.setObject(i * 2 + 2, pmr.getOrigLHS().getKeyValues().get(i));
                    logger.debug("Param " + (i * 2 + 2) + ": " + pmr.getOrigRHS().getKeyValues().get(i));
            	}
            	
            	// Since not all JDBC drivers support batch updates.
            	if (supportsBatchUpdates) {
	        		batchCount++;
	        		logger.debug("Adding statement to batch");
	        		ps.addBatch();
	        		if (batchCount >= MatchPool.DEFAULT_BATCH_SIZE || !it.hasNext()) {
	        			logger.debug("Executing batch update");
	        			ps.executeBatch();
	        			batchCount = 0;
	        		}
            	} else {
            		logger.debug("Executing update statement");
            		ps.executeUpdate();
            	}
            	it.remove();
            }
            
            sql = new StringBuilder();
            sql.append("UPDATE ");
            sql.append(DDLUtils.toQualifiedName(resultTable)); 
            sql.append("\n SET ");
            sql.append("MATCH_STATUS=?");
            sql.append(", MATCH_STATUS_DATE=" + SQL.escapeDateTime(con, new Date(System.currentTimeMillis())));
            sql.append(", MATCH_STATUS_USER=" + SQL.quote(pool.getSession().getAppUser()));
            sql.append(", DUP1_MASTER_IND=? ");
            sql.append("\n WHERE ");
            for (int i = 0;; i++) {
            	sql.append("DUP_CANDIDATE_1" + i + "=?");
            	sql.append(" AND DUP_CANDIDATE_2" + i + "=?");
            	if (i + 1 >= numKeyValues) break;
            	sql.append(" AND ");
            }
            lastSQL = sql.toString();
            logger.debug("The SQL statement we are running is " + lastSQL);
            
            if (ps != null) ps.close();
            ps = null;
            ps = con.prepareStatement(lastSQL);
            
            batchCount = 0;
            for (Iterator<PotentialMatchRecord> it = potentialMatchRecords.iterator(); it.hasNext();) {
            	pool.incrementProgress();
                PotentialMatchRecord pmr = it.next();
            	if (pmr.getStoreState() == StoreState.DIRTY) {
            		logger.debug("The potential match " + pmr + " was dirty, storing");
            		ps.setObject(1, pmr.getMatchStatus().getCode());
            		if (pmr.isDirectMaster()) {
            			ps.setObject(2, "Y");
                    } else if (pmr.isReferencedMaster()) {
                    	ps.setObject(2, "N");
                    } else {
                    	ps.setNull(2, Types.VARCHAR);
                    }
            		for (int i = 0; i < pmr.getOrigLHS().getKeyValues().size(); i++) {
            			ps.setObject(i * 2 + 3, pmr.getOrigLHS().getKeyValues().get(i));
            			ps.setObject(i * 2 + 4, pmr.getOrigRHS().getKeyValues().get(i));
            		}

            		if (supportsBatchUpdates) {
	            		batchCount++;
	            		logger.debug("Adding statement to batch");
	            		ps.addBatch();
	            		if (batchCount >= MatchPool.DEFAULT_BATCH_SIZE || !it.hasNext()) {
	            			logger.debug("Executing batch update");
	            			ps.executeBatch();
	            			batchCount = 0;
	            		}
            		} else {
            			logger.debug("Executing update statement");
            			ps.executeUpdate();
            		}
            		pmr.setStoreState(StoreState.CLEAN);
            	} else if (!it.hasNext() && supportsBatchUpdates) {
            		// execute remaining batched commands
            		logger.debug("Executing batch update");
            		ps.executeBatch();
            	}
            	
            }
            
            sql = new StringBuilder();
            sql.append("INSERT INTO ").append(DDLUtils.toQualifiedName(resultTable)).append(" ");
            sql.append("(");
            for (int i = 0; i < numKeyValues; i++) {
            	sql.append("DUP_CANDIDATE_1").append(i).append(", ");
            	sql.append("DUP_CANDIDATE_2").append(i).append(", ");
            }
            sql.append("MATCH_PERCENT");
            sql.append(", GROUP_ID");
            sql.append(", MATCH_STATUS");
            sql.append(", DUP1_MASTER_IND");
            sql.append(", MATCH_DATE");
            sql.append(", MATCH_STATUS_DATE");
            sql.append(", MATCH_STATUS_USER");
            
            //These fields are only used for the old merge engine and will be removed when
            //the merging is rewritten in Java
            for (int i = 0; i < numKeyValues; i++) {
            	sql.append(", DUP_ID").append(i);
            	sql.append(", MASTER_ID").append(i);
            }
            
            sql.append(")");
            sql.append("\n VALUES (");
            
            for (int i = 0; i < numKeyValues * 2; i++) {
            	sql.append("?, ");
            }
            sql.append("?, ");
            sql.append("?, ");
            sql.append("?, ");
            sql.append("?, ");
            sql.append(SQL.escapeDateTime(con, new Date(System.currentTimeMillis()))).append(", ");
            sql.append(SQL.escapeDateTime(con, new Date(System.currentTimeMillis()))).append(", ");
            sql.append(SQL.quote(pool.getSession().getAppUser()));
            for (int i = 0; i < numKeyValues * 2; i++) {
            	sql.append(", ?");
            }
            sql.append(")");
            lastSQL = sql.toString();
            logger.debug("The SQL statement we are running is " + lastSQL);
            
            if (ps != null) ps.close();
            ps = null;
            ps = con.prepareStatement(lastSQL);
            
            batchCount = 0;
            
            for (Iterator<PotentialMatchRecord> it = potentialMatchRecords.iterator(); it.hasNext();) {
            	pool.incrementProgress();
                PotentialMatchRecord pmr = it.next();
            	if (pmr.getStoreState() == StoreState.NEW) {
            		logger.debug("The potential match " + pmr + " was new, storing");
            		for (int i = 0; i < numKeyValues; i++) {
            			ps.setObject(i * 2 + 1, pmr.getOrigLHS().getKeyValues().get(i));
            			ps.setObject(i * 2 + 2, pmr.getOrigRHS().getKeyValues().get(i));
            		}
            		ps.setObject(numKeyValues * 2 + 1, pmr.getMungeProcess().getMatchPriority());
            		ps.setObject(numKeyValues * 2 + 2, pmr.getMungeProcess().getName());
            		ps.setObject(numKeyValues * 2 + 3, pmr.getMatchStatus().getCode());
            		
            		SourceTableRecord duplicate;
            		SourceTableRecord master;
            		if (pmr.isDirectMaster()) {
            			ps.setObject(numKeyValues * 2 + 4, "Y");
            			duplicate = pmr.getOrigRHS();
            			master = pmr.getOrigLHS();
                    } else if (pmr.isReferencedMaster()) {
                    	ps.setObject(numKeyValues * 2 + 4, "N");
                    	duplicate = pmr.getOrigLHS();
            			master = pmr.getOrigRHS();
                    } else {
                    	ps.setObject(numKeyValues * 2 + 4, null);
                    	duplicate = null;
            			master = null;
                    }
            		
            		//These fields are only used for the old merge engine and will be removed when
                    //the merging is rewritten in Java
            		if (duplicate != null && master != null) {
            			for (int i = 0; i < numKeyValues; i++) {
            				int baseParamIndex = (numKeyValues + i) * 2;
							ps.setObject(baseParamIndex + 5, duplicate.getKeyValues().get(i));
            				ps.setObject(baseParamIndex + 6, master.getKeyValues().get(i));
            			}
            		} else {
            			for (int i = 0; i < numKeyValues; i++) {
            				int baseParamIndex = (numKeyValues + i) * 2;
							ps.setObject(baseParamIndex + 5, null);
            				ps.setObject(baseParamIndex + 6, null);
            			}
            		}
            		
            		if (supportsBatchUpdates) {
	            		batchCount++;
	            		logger.debug("Adding insert statement to batch");
	            		ps.addBatch();
	            		if (batchCount >= MatchPool.DEFAULT_BATCH_SIZE || !it.hasNext()) {
	            			logger.debug("Executing batch insert");
	            			ps.executeBatch();
	            			batchCount = 0;
	            		}
            		} else {
            			logger.debug("Executing insert statement");
            			ps.executeUpdate();
            		}
            		pmr.setStoreState(StoreState.CLEAN);
            	} else if (!it.hasNext() && supportsBatchUpdates) {
            		// execute remaining batched commands
            		logger.debug("Executing batch insert");
            		ps.executeBatch();
            	}
            }
            
            if (ps != null) ps.close();
            ps = null;
            
            if (pool.isDebug()) {
            	con.rollback();
            } else {
            	con.commit();
            }
		} catch (SQLException ex) {
            logger.error("Error in query: "+lastSQL, ex);
            pool.getSession().handleWarning(
                    "Error in SQL Query while storing the Match Pool!" +
                    "\nMessage: "+ex.getMessage() +
                    "\nSQL State: "+ex.getSQLState() +
                    "\nQuery: "+lastSQL);
            try {
                con.rollback();
            } catch (SQLException doubleException) {
                logger.error("Rollback failed. Squishing this exception since it would shadow the original one:", doubleException);
            }
            throw new RuntimeException(ex);
        } catch (Exception ex) {
            try {
                con.rollback();
            } catch (SQLException doubleException) {
                logger.error("Rollback failed. Squishing this exception since it would shadow the original one:", doubleException);
            }
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            } else {
                throw new RuntimeException(ex);
            }
        } finally {
        	pool.setFinished(true);
            if (ps != null) try { ps.close(); } catch (SQLException ex) { logger.error("Couldn't close prepared statement", ex); }
            if (con != null) try { con.close(); } catch (SQLException ex) { logger.error("Couldn't close connection", ex); }
        }
	}

	@Override
	public void transactionRollback(TransactionEvent e) {
		// DO NOTHING
	}
}
