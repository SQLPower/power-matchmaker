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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLType;
import ca.sqlpower.architect.SQLIndex.Column;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.Project;

/**
 * The Cleanse Result Step is the ultimate destination for munge data in a data
 * cleansing project. Each of its inputs represents one column in the table
 * being cleansed; you can think of it as a mirror to the SQL Input Step, which
 * has one <i>output</i> per column in your table.
 * <p>
 * For every row in the table you are cleansing, this step reads the value on
 * each of its inputs and uses those values to update the row. For inputs that
 * are not connected to anything, their corresponding columns remain untouched.
 * This means, for example, that it is perfectly safe to create a munge process
 * that only affects a single column: Just connect a munge step to that column's
 * input, and leave all the other columns' inputs disconnected. They will not be
 * affected.
 */
public class CleanseResultStep extends AbstractMungeStep implements MungeResultStep {
	private SQLTable table;
	private SQLInputStep inputStep;
	private PreparedStatement ps = null;
	private boolean usePS = false; 
	
	
	public CleanseResultStep() throws ArchitectException {
		super("Table!", false);
	}

	private void addInitialInputs() throws ArchitectException {
		if (getMSOInputs().size() == 0) {
			for (SQLColumn c : table.getColumns()) {
				InputDescriptor id = new InputDescriptor(c.getName(), typeClass(c.getType()));
				addInput(id);
			}
		}
	}
	
	public void addInputStep(SQLInputStep inputStep) {
		this.inputStep = inputStep;
	}

	@Override
	public Boolean doCall() throws Exception {
		
		if (isPreviewMode()) {
			return Boolean.TRUE;
		}
		
		//This is the Updatable stuff
		if (usePS && ps == null) {
			ps = getUpdateStatment(getMSOInputs());
		}
		
		List<MungeStepOutput> inputs = getMSOInputs(); 

		StringBuilder out = new StringBuilder();
		int count = 0;
		for (int x = 0; x < inputs.size(); x++) {
		    if (x > 0) out.append(", ");
		    MungeStepOutput output = inputs.get(x);
			if (output != null) {
				count++;
                Object data = output.getData();
                if (ps == null) {
                	update(table.getColumn(x).getType(), x+1, data);
                } else {
                	updatePS(table.getColumn(x).getType(),count,data);
                }
				out.append("[").append(data).append("]");
			} else {
				out.append("<not connected>");
			}
		}
		if (ps != null) {
			ResultSet rs = inputStep.getResultSet();
			SQLIndex pk = getProject().getSourceTable().getPrimaryKeyIndex();
			for (Column col : pk.getChildren()) {
				count++;
				int pos = getProject().getSourceTable().getColumns().indexOf(col.getColumn());
				//pos is shifted by 1 to account for the result set data starting at 1
				Object curr = readObjectAs(rs, typeClass(col.getColumn().getType()), pos+1);
				updatePS(col.getColumn().getType(), count, curr);
			}
			
			ps.execute();
		}
		super.logger.debug(out);
		return Boolean.TRUE;
	}

	private Object readObjectAs(ResultSet rs, Class<?> typeClass, int index) throws SQLException{
		if (typeClass.equals(String.class)) {
			return rs.getString(index);
		} else if (typeClass.equals(BigDecimal.class)) {
			return rs.getBigDecimal(index);
		} else if (typeClass.equals(Date.class)) {
			return rs.getDate(index);
		} else if (typeClass.equals(Boolean.class)) {
			return rs.getBoolean(index);
		} 
		return null;
	}

	/**
	 * Adds the given data to the prepared statment so it can be executed 
	 * later.
	 * 
	 * @param type The type of the input
	 * @param count The position to place the data in the ps
	 * @param data The data to use
	 */
    private void updatePS(int type, int count, Object data) throws SQLException {
		switch (type) {
		case Types.INTEGER:
		case Types.BIGINT:
		case Types.SMALLINT:
		case Types.TINYINT:
			if (data instanceof Integer) {
				ps.setInt(count, ((Integer)data).intValue());
			} else {
				ps.setInt(count, ((BigDecimal) data).intValue());
			}
			break;
		case Types.BOOLEAN:
			ps.setBoolean(count, ((Boolean) data).booleanValue());
			break;
		case Types.LONGVARCHAR:
		case Types.CHAR:
		case Types.VARCHAR:
			ps.setString(count, ((String) data));
			break;
		case Types.DOUBLE:
		case Types.FLOAT:
		case Types.NUMERIC:
		case Types.DECIMAL:
			ps.setBigDecimal(count, ((BigDecimal) data));
			break;
		case Types.DATE:
			Date d = (Date) data;
			ps.setDate(count, new java.sql.Date(d.getTime()));
			break;
		case Types.TIME:
			ps.setTime(count, new Time(((Date)data).getTime()));
			break;
		case Types.TIMESTAMP:
			ps.setTimestamp(count, new Timestamp(((Date)data).getTime()));
			break;
		default:
			logger.error("Unsupported sql type! " + type);
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

	private void update(int type, int columnIndex, Object data) throws Exception {
		ResultSet rs = inputStep.getResultSet();
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
			rs.updateDate(columnIndex, new java.sql.Date(d.getTime()));
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
	
	public List<MungeResult> getResults() {
		return Collections.emptyList();
	}
	
	@Override
	public void doOpen(Logger logger) throws Exception {		
		table = getProject().getSourceTable();
		
		//checks if the database support updatable result sets.
		usePS = !table.getParentDatabase().getDataSource().getParentType().getSupportsUpdateableResultSets();
		
		setName(table.getName());
		addInitialInputs();
	}
	
	
	@Override
	public Project getProject() {
		Project p = super.getProject();
		if (p == null && inputStep != null) {
			p = inputStep.getProject();
		}
		return p;
	}
    
    /**
     * This override has no effect, but it is here for documentation purposes.
     * <p>
     * This step shares a connection, statement, and result set with the SQLInputStep 
     * it's attached to.  That step is responsible for rolling back or committing the
     * connection.
     */
    @Override
    public void doRollback() throws Exception {
    }

    /**
     * This override has no effect, but it is here for documentation purposes.
     * <p>
     * This step shares a connection, statement, and result set with the SQLInputStep 
     * it's attached to.  That step is responsible for rolling back or committing the
     * connection.
     */
    @Override
    public void doCommit() throws Exception {
    }
    
    private PreparedStatement getUpdateStatment(List<MungeStepOutput> inputs) throws ArchitectException, SQLException{
		boolean first = true;
		String sql = "UPDATE " + DDLUtils.toQualifiedName(getProject().getSourceTable()) + " SET ";
		for (int x = 0; x<inputs.size(); x++) {
			if (inputs.get(x) != null) {
				if (!first) {
					sql += ", ";
				}
				sql += getProject().getSourceTable().getColumn(x).getName() + "=?";
				first = false;
			}
		}
		sql += " WHERE ";
		SQLIndex pk = getProject().getSourceTable().getPrimaryKeyIndex();
		first = true;
		for (Column col : pk.getChildren()) {
			if (!first) {
				sql += " AND ";
			}
			sql += col.getName() + "=?";
		}
		return getProject().createSourceTableConnection().prepareStatement(sql);
    }
}
