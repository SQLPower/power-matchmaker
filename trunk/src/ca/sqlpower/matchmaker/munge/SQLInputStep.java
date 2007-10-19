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

package ca.sqlpower.matchmaker.munge;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLType;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.Project.ProjectMode;

/**
 * The SQLInputStep class implements a munge step which provides data
 * from a SQL query.  It has one output for each column in the query's
 * result set.  Each time the call() method is invoked, the query
 * cursor is advanced to the next row, and the step's output data will
 * then reflect the values in that row.
 */
public class SQLInputStep extends AbstractMungeStep {

    private static final Logger logger = Logger.getLogger(SQLInputStep.class);
    
    /**
     * The result set that provides input to this step.  The result set cursor will
     * be advanced on every call to {@link #call()}.  The result set is opened by
     * the {@link #open()} method, and closed by {@link #close()}.  If this step
     * has not yet been opened (or has been subsequently closed), this result set
     * reference will be null.
     */
    private ResultSet rs;
    
    /**
     * The table this step selects from.
     */
    private final SQLTable table;
    
    /**
     * This is the connection to the input table's database.
     */
    private Connection con;

	/**
	 * The match we are working on
	 */
	private Project project;

    /**
     * The output step that is tied to this input step.
     */
    private MungeStep outputStep;

    public SQLInputStep(Project project, MatchMakerSession session) throws ArchitectException {
    	super(session);
        this.table = project.getSourceTable();
        this.project = project;
        setName(table.getName());
        for (SQLColumn c : table.getColumns()) {
            MungeStepOutput<?> newOutput = new MungeStepOutput(c.getName(), typeClass(c.getType()));
            addChild(newOutput);
        }
    }

    /**
     * Returns the Java class associated with the given SQL type code.
     * 
     * @param type
     *            The type ID number. See {@link SQLType} for the official list.
     * @return The class for the given type. Defaults to java.lang.String if the
     *         type code is unknown, since almost every SQL type can be
     *         represented as a string if necessary.
     */
    private Class<?> typeClass(int type) {
        switch (type) {
        case Types.VARCHAR:
        case Types.VARBINARY:
        case Types.STRUCT:
        case Types.REF:
        case Types.OTHER:
        case Types.NULL:
        case Types.LONGVARCHAR:
        case Types.LONGVARBINARY:
        case Types.JAVA_OBJECT:
        case Types.DISTINCT:
        case Types.DATALINK:
        case Types.CLOB:
        case Types.CHAR:
        case Types.BLOB:
        case Types.BINARY:
        case Types.ARRAY:
        default:
            return String.class;

        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.REAL:
        case Types.NUMERIC:
        case Types.INTEGER:
        case Types.FLOAT:
        case Types.DOUBLE:
        case Types.DECIMAL:
        case Types.BIGINT:
            return BigDecimal.class;

        case Types.BIT:
        case Types.BOOLEAN:
            return Boolean.class;
        
        case Types.TIMESTAMP:
        case Types.TIME:
        case Types.DATE:
            return Date.class;
        }
    }

    public Boolean call() throws Exception {
        super.call();
        
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
    public void open() throws Exception {
    	super.open();
    	
    	if (rs != null) {
            throw new IllegalStateException("The input step is already open");
        }
        
        SQLDatabase db = table.getParentDatabase();
        if (db == null) {
            throw new RuntimeException("The input table has no parent database defined.");
        }
        
        con = db.getConnection();
        if (con == null) {
            throw new RuntimeException("Could not obtain a connection to the input table's database");
        }
        
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
        
        logger.debug("Attempting to execute input query: " + sql);
        
        rs = stmt.executeQuery(sql.toString());
    }
    
    @Override
    public void close() throws Exception {
    	super.close();
    	
        Statement stmt = rs.getStatement();
        rs.close();
        rs = null;
        stmt.close();
        con.close();
    }

    /**
     * Always returns false. There are no inputs to this step other than the result set.
     */
    public boolean canAddInput() {
        return false;
    }
    
    /**
     * Creates or returns the output step for this input step.  There will only
     * ever be one output step created for a given instance of {@link SQLInputStep}.
     */
    public MungeStep getOuputStep() throws ArchitectException {
        if (outputStep != null) {
            return outputStep;
        } else if (project.getType() == ProjectMode.CLEANSE) {
    		outputStep = new CleanseResultStep(project, getSession());
    	} else {
    		outputStep = new MungeResultStep(project, this, getSession());
    	}
        return outputStep;
    }
    
    public class CleanseResultStep extends AbstractMungeStep {
    	SQLTable table;
    	
		private CleanseResultStep(Project project, MatchMakerSession session) throws ArchitectException {
			super(session);
			table = project.getSourceTable();
			setName(table.getName());
			addInitialInputs();
		}
		
		private void addInitialInputs() throws ArchitectException {
			for (SQLColumn c : table.getColumns()) {
	            InputDescriptor id = new InputDescriptor(c.getName(), typeClass(c.getType()));
	            addInput(id);
			}			
		}
		
		@Override
		public Boolean call() throws Exception {
			super.call();
			
			List<MungeStepOutput> inputs = getInputs(); 
			Object[] mungedData = new Object[inputs.size()];
			
			for (int i = 0; i < inputs.size(); i++) {
				MungeStepOutput output = inputs.get(i);
				if (output != null) {
					mungedData[i] = output.getData();
				} else {
					mungedData[i] = null;
				}
			}
			
			String out = "[";
			for (int x = 0; x < mungedData.length; x++) {
				if (mungedData[x] != null) {
					update(table.getColumn(x).getType(), x+1, mungedData[x]);
					out += mungedData[x];
				} else {
					out += rs.getObject(x+1);
				}
				out += "], [";
			}
			out = out.substring(0, out.length()-3);
			logger.debug(out);
			return Boolean.TRUE;
		}
		
		private void update(int type, int columnIndex, Object data) throws Exception {
			
			
			switch (type) {
				case Types.INTEGER:
				case Types.BIGINT:
				case Types.SMALLINT:
				case Types.TINYINT:
					rs.updateInt(columnIndex, ((BigDecimal) data).intValue());
					break;
				case Types.BOOLEAN:
					rs.updateBoolean(columnIndex, ((Boolean) data).booleanValue());
					break;
				case Types.LONGVARCHAR:
				case Types.CHAR:
				case Types.VARCHAR:
					logger.debug("attempting update : " + data + ", " + columnIndex);
					rs.updateString(columnIndex, ((String) data));
					break;
				case Types.DOUBLE:
				case Types.FLOAT:
				case Types.NUMERIC:
				case Types.DECIMAL:
					rs.updateBigDecimal(columnIndex, ((BigDecimal) data));
					break;
				case Types.DATE:
					Date d = (Date) data;
					rs.updateDate(columnIndex, new java.sql.Date(d.getDate()));
					break;
				case Types.TIME:
					rs.updateTime(columnIndex, new Time(((Date)data).getTime()));
					break;
				case Types.TIMESTAMP:
					rs.updateTimestamp(columnIndex, new Timestamp(((Date)data).getTime()));
					break;
				default:
					logger.error("Unsupported sql type! " + type);
					break;
			}
			logger.debug("attempting updaterow");
			rs.updateRow();
		}

		public boolean canAddInput() {
			return false;
		}
    }
    
}
