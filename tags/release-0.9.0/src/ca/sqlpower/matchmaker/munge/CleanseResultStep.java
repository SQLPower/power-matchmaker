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
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLType;
import ca.sqlpower.matchmaker.Project;

public class CleanseResultStep extends AbstractMungeStep implements MungeResultStep {
	private SQLTable table;
	private SQLInputStep inputStep;

	public CleanseResultStep() throws ArchitectException {
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
	public Boolean call() throws Exception {
		super.call();

		List<MungeStepOutput> inputs = getMSOInputs(); 
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
				out += inputStep.getResultSet().getObject(x+1);
			}
			out += "], [";
		}
		out = out.substring(0, out.length()-3);
		super.logger.debug(out);
		return Boolean.TRUE;
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

	public boolean canAddInput() {
		return false;
	}

	public List<MungeResult> getResults() {
		return Collections.emptyList();
	}
	
	@Override
	public void open(Logger logger) throws Exception {
		open(logger, getProject());
	}
	
	public void open(Logger logger, Project project) throws Exception {
		super.open(logger);
		
		table = project.getSourceTable();
		setName(table.getName());
		addInitialInputs();
	}
    
    /**
     * This override has no effect, but it is here for documentation purposes.
     * <p>
     * This step shares a connection, statement, and result set with the SQLInputStep 
     * it's attached to.  That step is responsible for rolling back or committing the
     * connection.
     */
    @Override
    public void rollback() throws Exception {
        super.rollback();
    }

    /**
     * This override has no effect, but it is here for documentation purposes.
     * <p>
     * This step shares a connection, statement, and result set with the SQLInputStep 
     * it's attached to.  That step is responsible for rolling back or committing the
     * connection.
     */
    @Override
    public void commit() throws Exception {
        super.commit();
    }
}
