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

package ca.sqlpower.matchmaker.munge;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TypeMap;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.sql.CachedRowSet;

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
     * containing project's source table when setParent() is called.
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
                MungeStepOutput<?> o = getChildren().get(i);
                o.setData(null);
            }
            return false;
        }
        
        for (int i = 0; i < table.getColumns().size(); i++) {
            MungeStepOutput<?> o = getChildren().get(i);
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
    public void doOpen(Logger logger) throws Exception {
    	
    	if (isPreviewMode() && previewRS != null) {
    		previewRS.beforeFirst();
    		rs = previewRS;
    	} else {
    		if (isPreviewMode()) {
    			previewRS = new CachedRowSet();
    		}
    	
    		if (rs != null) {
    			throw new IllegalStateException("The input step is already open");
    		}

    		this.table = getProject().getSourceTable();
    		if (!getName().equals(table.getName())) {
    			setName(table.getName());
    		}

    		// TODO: Verify that outputs are the same with the table's columns.
    		if (getChildCount() == 0) {
    			for (SQLColumn c : table.getColumns()) {
    				MungeStepOutput<?> newOutput = new MungeStepOutput(c.getName(), TypeMap.typeClass(c.getType()));
    				addChild(newOutput);
    			}
    		}

    		SQLDatabase db = table.getParentDatabase();
    		if (db == null) {
    			throw new RuntimeException("The input table has no parent database defined.");
    		}

    		con = db.getConnection();
    		if (con == null) {
    			throw new RuntimeException("Could not obtain a connection to the input table's database");
    		}
    		con.setAutoCommit(false);

    		Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

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
//    			stmt.setFetchSize(MungePreviewer.MAX_ROWS_PREVIEWED);
    		}
    		logger.debug("Attempting to execute input query: " + sql);
    		rs = stmt.executeQuery(sql.toString());

    		logger.debug("ResultSet fetch size is: " + rs.getFetchSize());
    		if (rs.getFetchSize() < DEFAULT_FETCH_SIZE) {
    			rs.setFetchSize(DEFAULT_FETCH_SIZE);
    		}
    		
    		if (isPreviewMode()) {
    			previewRS.populate(rs);
    			previewRS.beforeFirst();
    			rs = previewRS;
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
    public MungeResultStep getOutputStep() throws ArchitectException {
        return getOutputStep(getProject());
    }
    
    public MungeResultStep getOutputStep(Project project) throws ArchitectException {
        if (outputStep != null) {
            return outputStep;
        } else if (project.getType() == ProjectMode.CLEANSE) {
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
