/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.munge;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TypeMap;
import ca.sqlpower.matchmaker.MatchMakerEngine.EngineMode;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.sql.CachedRowSet;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;

/**
 * The SQLInputStep class implements a munge step which provides data
 * from a SQL query.  It has one output for each column in the query's
 * result set.  Each time the call() method is invoked, the query
 * cursor is advanced to the next row, and the step's output data will
 * then reflect the values in that row.
 */
public class SQLInputStep extends AbstractMungeStep {

	/**
	 * Default value to input when setting the ResultSet fetch size using
	 * {@link ResultSet#setFetchSize(int)}. This is to help with a production bug running
	 * the Match Engine on Oracle, which sets a default fetch size of 10. The result is a
	 * lot of network traffic when trying to read thousands of rows from a ResultSet, 
	 * resulting in horrible Match Engine performance.  
	 */
	private static final int DEFAULT_FETCH_SIZE = 1000;
	
    /**
     * The result set that provides input to this step.  The result set cursor will
     * be advanced on every call to {@link #call()}.  The result set is opened by
     * the {@link #open()} method, and closed by {@link #close()}.  If this step
     * has not yet been opened (or has been subsequently closed), this result set
     * reference will be null.
     */
    private ResultSet rs;
    
    /**
     * The table this step selects from.  This value is initialized based on the
     * containing project's source table when {@link #doOpen(Logger)} is called.
     */
    private SQLTable table;
    
    /**
     * This is the connection to the input table's database.  It will not be in
     * auto-commit mode.
     */
    private Connection con;

    /**
     * The output step that is tied to this input step.
     */
    private MungeResultStep outputStep;
    
    /**
     * This will contain the result set that is retrieved from the database for
     * the preview mode on munge processes.
     */
    private CachedRowSet previewRS = null;
    
    public SQLInputStep() {
    	super("Input Step", false);
    }


    public Boolean doCall() throws Exception {
        
        if (!rs.next()) {
            for (int i = 0; i < table.getColumns().size(); i++) {
                MungeStepOutput<?> o = getChildren(MungeStepOutput.class).get(i);
                o.setData(null);
            }
            return false;
        }
        
        for (int i = 0; i < table.getColumns().size(); i++) {
            MungeStepOutput<?> o = getChildren(MungeStepOutput.class).get(i);
            if (o.getType() == String.class) {
                MungeStepOutput<String> oo = (MungeStepOutput<String>) o;
                oo.setData(rs.getString(i + 1));
            } else if (o.getType() == BigDecimal.class) {
                MungeStepOutput<BigDecimal> oo = (MungeStepOutput<BigDecimal>) o;
                oo.setData(rs.getBigDecimal(i + 1));
            } else if (o.getType() == Date.class) {
                MungeStepOutput<Date> oo = (MungeStepOutput<Date>) o;
                oo.setData(rs.getTimestamp(i + 1));
            } else if (o.getType() == Boolean.class) {
                MungeStepOutput<Boolean> oo = (MungeStepOutput<Boolean>) o;
                oo.setData(rs.getBoolean(i + 1));
            } else {
                logger.warn("Column \""+o.getName()+"\" type "+o.getType()+" is not known. Treating as a String.");
                MungeStepOutput<String> oo = (MungeStepOutput<String>) o;
                oo.setData(rs.getString(i + 1));
            }
        }
        
        return true;
    }
    
    @Override
    public void doOpen(EngineMode mode, Logger logger) throws Exception {
    	if ((!isPreviewMode() || previewRS == null) && rs != null) {
    		throw new IllegalStateException("The input step is already open");
    	}
    	
    	refreshAndSetup(logger, false);
    }
    
    @Override
    public void refresh(Logger logger) throws Exception {
    	refreshAndSetup(logger, true);
    }

	/**
	 * Helper refresh method. For a normal refresh the result set should be
	 * nulled out if preview is not enabled. If this method is used to
	 * initialize the step instead by the {@link #doOpen(EngineMode, Logger)}
	 * method the result set should not be nulled out. The behaviour chosen is
	 * selected by the boolean nullOutRS.
	 */
    private void refreshAndSetup(Logger logger, boolean nullOutRS) throws Exception {
    	if (isPreviewMode() && previewRS != null) {
    		previewRS.beforeFirst();
    		rs = previewRS;
    	} else {
    		if (isPreviewMode()) {
    			previewRS = new CachedRowSet();
    		}
    	
    		this.table = getProject().getSourceTable();
    		if (!getName().equals(table.getName())) {
    			setName(table.getName());
    		}

    		setupOutputs();

    		SQLDatabase db = table.getParentDatabase();
    		if (db == null) {
    			throw new RuntimeException("The input table has no parent database defined.");
    		}

    		con = db.getConnection();
    		if (con == null) {
    			throw new RuntimeException("Could not obtain a connection to the input table's database");
    		}
    		con.setAutoCommit(false);

            Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            
            // Some platforms (definitely PostgreSQL) require a non-zero fetch size to enable streaming
            stmt.setFetchSize(100);

    		StringBuilder sql = new StringBuilder();
    		sql.append("SELECT");
    		boolean first = true;
    		for (SQLColumn c : table.getColumns()) {
    			if (!first) sql.append(",");
    			sql.append("\n ").append(c.getName());
    			first = false;
    		}
    		sql.append("\nFROM ").append(DDLUtils.toQualifiedName(table));
    		if (getProject().getFilter() != null && getProject().getFilter().trim().length() > 0) {
    			sql.append("\nWHERE " + getProject().getFilter());
    		}

    		if (isPreviewMode()) {
    		    stmt.setMaxRows(MungePreviewer.MAX_ROWS_PREVIEWED);
    		}
    		logger.debug("Attempting to execute input query: " + sql);
    		ResultSet tempRs = stmt.executeQuery(sql.toString());

    		logger.debug("ResultSet fetch size is: " + tempRs.getFetchSize());
    		if (tempRs.getFetchSize() < DEFAULT_FETCH_SIZE) {
    			tempRs.setFetchSize(DEFAULT_FETCH_SIZE);
    		}
    		
    		if (isPreviewMode()) {
    			previewRS.populate(tempRs);
    			previewRS.beforeFirst();
    			rs = previewRS;
    			tempRs.close();
    			stmt.close();
    		} else if (!nullOutRS) {
    			rs = tempRs;
    		}
    		if (isPreviewMode() || nullOutRS) {
    			con.close();
    		}
    	}
    }

    /**
     * Populates or refreshes the outputs of this step based on the current
     * columns of {@link #table}.  This method will both add missing outputs
     * and remove outputs that correspond to columns the table currently
     * does not have. It will also adjust the data type of outputs whose
     * columns' types have changed.
     */
    private void setupOutputs() throws SQLObjectException {
        Set<MungeStepOutput> orphanOutputs = new HashSet<MungeStepOutput>(getChildren(MungeStepOutput.class));
        for (SQLColumn c : table.getColumns()) {
        	
        	/*
			 * this next line is needed because we need to assign upstream types
			 * to every type in columns otherwise stuff doesn't work right.
			 * Unfortunately, we cannot do this at the source of the problem:
			 * 		SQLTable.fetchColumnsForTable
			 * since we have no session info there
			 */
			c.setType(c.getType());
			c.setType(getSession().getSQLType(c.getType()));
			
            String colName = c.getName();
            MungeStepOutput<?> output = getOutputByName(colName);
            if (output == null) {
                // new column -- create an output and we're done
                MungeStepOutput<?> newOutput = new MungeStepOutput(colName, TypeMap.typeClass(c.getType()));
                addChild(newOutput);
            } else if (output.getType() != TypeMap.typeClass(c.getType())) {
                // existing column changed type -- recreate output with same name and new type
                int idx = getChildren().indexOf(output);
                MungeStepOutput<?> newOutput = new MungeStepOutput(colName, TypeMap.typeClass(c.getType()));
                addChild(newOutput, idx);
                MungeProcess mp = getParent();
                for (MungeStep step : mp.getChildren(MungeStep.class)) {
                    for (int i = 0; i < step.getInputCount(); i++) {
                        InputDescriptor id = step.getInputDescriptor(i);
                        MungeStepOutput input = step.getMSOInputs().get(i);
                        if (input == output) {
                            step.disconnectInput(i);
                            
                            // reconnect the new output if its type is compatible
                            if (id.getType().isAssignableFrom(TypeMap.typeClass(c.getType()))) {
                                step.connectInput(i, newOutput);
                            }
                        }
                    }
                    
                }
                try {
                	removeChild(output);
                } catch (ObjectDependentException e) {
                	throw new RuntimeException(e);
                }
                orphanOutputs.remove(output);
            } else {
                // existing column with existing output -- nothing to do
                orphanOutputs.remove(output);
            }
        }
        
        // clean up outputs whose columns no longer exist in the table
        for (MungeStepOutput mso : orphanOutputs) {
            MungeProcess mp = getParent();
            for (MungeStep step : mp.getChildren(MungeStep.class)) {
                step.disconnectInput(mso);
            }
            try {
            	removeChild(mso);
            } catch (ObjectDependentException e) {
            	throw new RuntimeException(e);
            }
        }
    }
    
    @Override
    public void doCommit() throws Exception {
        logger.debug("Committing " + getName());
        con.commit();
    }
    
    @Override
    public void doRollback() throws Exception {
        logger.debug("Rolling back " + getName());
        if (!isPreviewMode()) {
        	con.rollback();
        }
    }

    @Override
    public void doClose() throws Exception { 	
    	if (!isPreviewMode()) {
    		Statement stmt = rs.getStatement();
    		rs.close();
    		stmt.close();
    		con.close();
    	}
    	rs = null;
    }

    /**
     * Creates or returns the output step for this input step.  There will only
     * ever be one output step created for a given instance of {@link SQLInputStep}.
     */
    public MungeResultStep getOutputStep() throws SQLObjectException {
        return getOutputStep(getProject());
    }
    
    public MungeResultStep getOutputStep(Project project) throws SQLObjectException {
        if (outputStep != null) {
            return outputStep;
        } else if (project.getType() == ProjectMode.CLEANSE || project.getType() == ProjectMode.ADDRESS_CORRECTION) {
    		outputStep = new CleanseResultStep();
    	} else {
    		outputStep = new DeDupeResultStep();
    	}
        return outputStep;
    }
    
    @Override
    public boolean isInputStep() {
    	return true;
    }
    
    public ResultSet getResultSet() {
    	return rs;
    }
}
